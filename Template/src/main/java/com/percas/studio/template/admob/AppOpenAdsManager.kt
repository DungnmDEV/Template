package com.percas.studio.template.admob

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.percas.studio.template.R
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("PrivatePropertyName")
class AppOpenAdsManager(
    private val activity: Activity,
    private val appOpenID: String,
    val timeOut: Long,
    val appOpenAdsListener: AppOpenAdListener
) {
    private var appOpenAd: AppOpenAd? = null
    private val ID_TEST = "ca-app-pub-3940256099942544/9257395921"
    var isShowingAd = true
    private var isLoading = true
    private var dialogFullScreen: Dialog? = null
    private val TAG = "APP OPEN ADS"
    private var isStart = true
    private val adRequest: AdRequest
        get() = AdRequest.Builder().build()

    private val isAdAvailable: Boolean
        get() = appOpenAd != null

    fun loadAndShowAoA() {
        var idAoa = appOpenID
        if (appOpenID.isBlank()) {
            idAoa = ID_TEST
        }

        if (!AdmobManager.isEnableAd) {
            appOpenAdsListener.onAdFail("isShowAds false")
            return
        }
        //Check timeout show inter
        val job = CoroutineScope(Dispatchers.Main).launch {
            delay(timeOut)
            if (isLoading && isStart) {
                isStart = false
                isLoading = false
                onAoaDestroyed()
                appOpenAdsListener.onAdFail("Time out")
                Log.d(TAG, "TimeOut")
            }
        }
        if (isAdAvailable) {
            job.cancel()
            appOpenAdsListener.onAdFail("isAdAvailable true")
            return
        } else {
            Log.d(TAG, "fetching... ")
            isShowingAd = false
            val request = adRequest
            AppOpenAd.load(activity, idAoa, request, object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    isLoading = false
                    super.onAdFailedToLoad(p0)
                    if (isStart) {
                        isStart = false
                        appOpenAdsListener.onAdFail(p0.message)
                    }
                    job.cancel()
                    Log.d(TAG, "onAppOpenAdFailedToLoad: $p0")
                }

                override fun onAdLoaded(ad: AppOpenAd) {
                    super.onAdLoaded(ad)
                    appOpenAd = ad
                    job.cancel()
                    Log.d(TAG, "isAdAvailable = true")
                    if (!AppResumeAdsManager.getInstance().isShowingAd && !isShowingAd) {
                        showAdIfAvailable()
                    }
                }
            })
        }
    }

    fun showAdIfAvailable() {
        Log.d(TAG, "$isShowingAd - $isAdAvailable")
        if (!isShowingAd && isAdAvailable && isLoading) {
            isLoading = false
            if (AppResumeAdsManager.getInstance().isInitialized) {
                AppResumeAdsManager.getInstance().isAppResumeEnabled = false
            }
            Log.d(TAG, "will show ad ")
            val fullScreenContentCallback: FullScreenContentCallback =
                object : FullScreenContentCallback() {

                    override fun onAdDismissedFullScreenContent() {
                        try {
                            dialogFullScreen?.dismiss()
                        } catch (ignored: Exception) {
                        }
                        appOpenAd = null
                        isShowingAd = true
                        Log.d(TAG, "Dismiss... ")
                        if (isStart) {
                            isStart = false
                            appOpenAdsListener.onAdClose()
                        }
                        if (AppResumeAdsManager.getInstance().isInitialized) {
                            AppResumeAdsManager.getInstance().isAppResumeEnabled = true
                        }
                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        try {
                            dialogFullScreen?.dismiss()
                        } catch (ignored: Exception) {
                        }
                        isShowingAd = true
                        if (isStart) {
                            isStart = false
                            appOpenAdsListener.onAdFail(p0.message)
                            Log.d(TAG, "Failed... $p0")
                        }
                        if (AppResumeAdsManager.getInstance().isInitialized) {
                            AppResumeAdsManager.getInstance().isAppResumeEnabled = true
                        }
                    }

                    override fun onAdShowedFullScreenContent() {
                        isShowingAd = true
                    }
                }
            appOpenAd?.run {
                this.fullScreenContentCallback = fullScreenContentCallback
                dialogFullScreen = Dialog(activity)
                dialogFullScreen?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialogFullScreen?.setContentView(R.layout.dialog_full_screen)
                dialogFullScreen?.setCancelable(false)
                dialogFullScreen?.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
                dialogFullScreen?.window?.setLayout(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                val img = dialogFullScreen?.findViewById<LottieAnimationView>(R.id.imageView3)
                img?.setAnimation(R.raw.gifloading)
                try {
                    if (!activity.isFinishing && dialogFullScreen != null && dialogFullScreen?.isShowing == false) {
                        dialogFullScreen?.show()
                    }
                } catch (ignored: Exception) {
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    if (!AppResumeAdsManager.getInstance().isShowingAd && !isShowingAd) {
                        Log.d(TAG, "Show")
                        try {
                            val txt = dialogFullScreen?.findViewById<TextView>(R.id.txtLoading)
                            img?.visibility = View.INVISIBLE
                            txt?.visibility = View.INVISIBLE
                        } catch (ignored: Exception) {
                        }
                        setOnPaidEventListener { appOpenAdsListener.onAdPaid(it, adUnitId) }
                        show(activity)
                    } else {
                        appOpenAdsListener.onAdFail("AOA can't show")
                    }
                }, 800)
            }
        } else {
            appOpenAdsListener.onAdFail("AOA can't show in background!")
        }
    }

    private fun onAoaDestroyed() {
        isShowingAd = true
        isLoading = false
        try {
            if (!activity.isFinishing && dialogFullScreen != null && dialogFullScreen?.isShowing == true) {
                dialogFullScreen?.dismiss()
            }
            appOpenAd?.fullScreenContentCallback?.onAdDismissedFullScreenContent()
        } catch (ignored: Exception) {
        }
    }

    interface AppOpenAdListener {
        fun onAdClose()
        fun onAdFail(error: String)
        fun onAdPaid(adValue: AdValue, adUnitAds: String)
    }
}