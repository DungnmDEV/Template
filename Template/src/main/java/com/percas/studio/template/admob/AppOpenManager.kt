package com.percas.studio.template.admob

import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Window
import android.widget.LinearLayout
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.ads.AdActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.percas.studio.template.R
import java.util.Date

object AppOpenManager : Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {
    private const val TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"
    private const val MAX_CACHE_AGE_MS = 4 * 60 * 60 * 1000L

    private var currentActivity: Activity? = null

    private var startupDialog: Dialog? = null
    private var isStartupShowing = false

    private var resumeApplication: Application? = null
    private var resumeAdUnitId: String = ""
    private var resumeAd: AppOpenAd? = null
    private var isResumeLoading = false
    private var isResumeShowing = false
    private var resumeLoadTime = 0L
    private var resumeInitialized = false
    private var resumeEnabled = true
    private var resumeDialog: Dialog? = null
    private val disabledResumeActivities = mutableSetOf<Class<out Activity>>()

    var lastTimeShowAd: Long = 0L
        internal set
    var timeWaitToShow: Long = 0L

    fun showOnSlash(
        activity: Activity,
        adUnitId: String,
        timeout: Long,
        listener: AppOpenAdListener
    ) {
        val resolvedId = resolveAdUnitId(activity, adUnitId)
        if (resolvedId == null) {
            listener.onAdFail(AdErrorInfo(AdErrorCode.BLANK_AD_UNIT_ID, "Ad Id is blank"))
            return
        }
        if (!AdmobCore.isEnableAd) {
            listener.onAdFail(AdErrorInfo(AdErrorCode.ADS_DISABLED, "Ads is DISABLE now"))
            return
        }
        if (AdmobCore.isOverlayAdShowing || isResumeShowing || isStartupShowing) {
            listener.onAdFail(AdErrorInfo(AdErrorCode.ALREADY_SHOWING, "Other ad is showing"))
            return
        }

        val handler = Handler(Looper.getMainLooper())
        var finished = false
        isStartupShowing = true
        AdmobCore.isOverlayAdShowing = true
        setResumeModeEnabled(false)

        val timeoutRunnable = Runnable {
            if (!finished) {
                finished = true
                isStartupShowing = false
                AdmobCore.isOverlayAdShowing = false
                setResumeModeEnabled(true)
                dismissDialog(startupDialog)
                listener.onAdFail(AdErrorInfo(AdErrorCode.TIMEOUT, "Time out"))
            }
        }
        handler.postDelayed(timeoutRunnable, timeout)

        AppOpenAd.load(
            activity,
            resolvedId,
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    if (finished) return
                    finished = true
                    handler.removeCallbacks(timeoutRunnable)
                    isStartupShowing = false
                    AdmobCore.isOverlayAdShowing = false
                    setResumeModeEnabled(true)
                    listener.onAdFail(AdErrorInfo(AdErrorCode.LOAD_FAILED, loadAdError.message))
                }

                override fun onAdLoaded(ad: AppOpenAd) {
                    if (finished) return
                    val dialog = createLoadingDialog(activity)
                    startupDialog = dialog
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            if (finished) return
                            finished = true
                            handler.removeCallbacks(timeoutRunnable)
                            dismissDialog(dialog)
                            isStartupShowing = false
                            AdmobCore.isOverlayAdShowing = false
                            setResumeModeEnabled(true)
                            lastTimeShowAd = System.currentTimeMillis()
                            listener.onAdClose()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            if (finished) return
                            finished = true
                            handler.removeCallbacks(timeoutRunnable)
                            dismissDialog(dialog)
                            isStartupShowing = false
                            AdmobCore.isOverlayAdShowing = false
                            setResumeModeEnabled(true)
                            listener.onAdFail(AdErrorInfo(AdErrorCode.SHOW_FAILED, adError.message))
                        }

                        override fun onAdShowedFullScreenContent() {
                            lastTimeShowAd = System.currentTimeMillis()
                        }
                    }
                    ad.setOnPaidEventListener {
                        listener.onAdPaid(
                            it,
                            ad.adUnitId,
                            ad.responseInfo?.mediationAdapterClassName ?: "GoogleAdmob"
                        )
                    }
                    handler.postDelayed({
                        if (!finished && !activity.isFinishing) {
                            ad.show(activity)
                        }
                    }, 300)
                }
            }
        )
    }

    fun enableResumeMode(
        application: Application,
        adUnitId: String,
        minIntervalMillis: Long = 0L
    ) {
        resumeApplication = application
        resumeAdUnitId = resolveAdUnitId(application, adUnitId).orEmpty()
        timeWaitToShow = minIntervalMillis
        if (!resumeInitialized) {
            application.registerActivityLifecycleCallbacks(this)
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
            resumeInitialized = true
        }
        maybeLoadResumeAd()
    }

    override fun onStart(owner: LifecycleOwner) {
        showResumeAdIfAvailable()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity.javaClass == AdActivity::class.java) return
        currentActivity = activity
    }

    override fun onActivityStarted(activity: Activity) = Unit

    override fun onActivityResumed(activity: Activity) {
        if (activity.javaClass == AdActivity::class.java) return
        if (!isResumeShowing) {
            currentActivity = activity
        }
        maybeLoadResumeAd()
    }

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) {
        if (activity.javaClass == AdActivity::class.java) return
        if (currentActivity == activity) {
            currentActivity = null
        }
        if (resumeDialog?.isShowing == true) {
            dismissDialog(resumeDialog)
        }
    }

    internal fun isResumeModeInitialized(): Boolean = resumeInitialized

    internal fun setResumeModeEnabled(enabled: Boolean) {
        resumeEnabled = enabled
    }

    fun disableResumeForActivity(activityClass: Class<out Activity>) {
        disabledResumeActivities.add(activityClass)
    }

    fun enableResumeForActivity(activityClass: Class<out Activity>) {
        disabledResumeActivities.remove(activityClass)
    }

    private fun maybeLoadResumeAd() {
        val application = resumeApplication ?: return
        if (resumeAdUnitId.isBlank()) return
        if (isResumeLoading || isResumeAdAvailable()) return

        isResumeLoading = true
        AppOpenAd.load(
            application,
            resumeAdUnitId,
            AdRequest.Builder().setHttpTimeoutMillis(5000).build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    resumeAd = ad
                    isResumeLoading = false
                    resumeLoadTime = Date().time
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isResumeLoading = false
                }
            }
        )
    }

    private fun showResumeAdIfAvailable() {
        val activity = currentActivity ?: return
        if (!resumeInitialized) return
        if (!resumeEnabled) return
        if (!AdmobCore.isEnableAd) return
        if (AdmobCore.isOverlayAdShowing || isStartupShowing || isResumeShowing) return
        if (resumeAdUnitId.isBlank()) return
        if (System.currentTimeMillis() - lastTimeShowAd < timeWaitToShow) return
        if (disabledResumeActivities.any { it.name == activity.javaClass.name }) return
        if (!isResumeAdAvailable()) {
            maybeLoadResumeAd()
            return
        }

        val ad = resumeAd ?: return
        val dialog = createLoadingDialog(activity, R.layout.dialog_full_screen_onresume)
        resumeDialog = dialog
        isResumeShowing = true
        AdmobCore.isOverlayAdShowing = true
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                dismissDialog(dialog)
                resumeAd = null
                isResumeShowing = false
                AdmobCore.isOverlayAdShowing = false
                maybeLoadResumeAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                dismissDialog(dialog)
                resumeAd = null
                isResumeShowing = false
                AdmobCore.isOverlayAdShowing = false
                maybeLoadResumeAd()
            }

            override fun onAdShowedFullScreenContent() {
                lastTimeShowAd = System.currentTimeMillis()
            }
        }
        Handler(Looper.getMainLooper()).postDelayed({
            if (resumeAd != null && currentActivity != null) {
                ad.show(activity)
            }
        }, 150)
    }

    private fun isResumeAdAvailable(): Boolean {
        val adLoaded = resumeAd != null
        val notExpired = Date().time - resumeLoadTime < MAX_CACHE_AGE_MS
        return adLoaded && notExpired
    }

    private fun resolveAdUnitId(context: Context, requestedId: String): String? {
        val adId = if (AdmobCore.isTestAd) TEST_AD_UNIT_ID else requestedId.trim()
        return adId.takeIf { it.isNotBlank() }
    }

    private fun createLoadingDialog(activity: Activity, layoutRes: Int = R.layout.dialog_full_screen): Dialog {
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(layoutRes)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        dialog.findViewById<LottieAnimationView>(R.id.imageView3)?.setAnimation(R.raw.gifloading)
        try {
            if (!activity.isFinishing && !dialog.isShowing) {
                dialog.show()
            }
        } catch (_: Exception) {
        }
        return dialog
    }

    private fun dismissDialog(dialog: Dialog?) {
        try {
            if (dialog?.isShowing == true) {
                dialog.dismiss()
            }
        } catch (_: Exception) {
        }
    }

    interface AppOpenAdListener {
        fun onAdClose()
        fun onAdFail(error: AdErrorInfo)
        fun onAdPaid(adValue: AdValue, adUnitAds: String, mediationNetwork: String)
    }
}
