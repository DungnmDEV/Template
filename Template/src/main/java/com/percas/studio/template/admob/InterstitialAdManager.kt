package com.percas.studio.template.admob

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Window
import android.widget.LinearLayout
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.percas.studio.template.R

internal object InterstitialAdManager {

    fun loadInterstitialAd(
        activity: Context,
        idAd: String,
        adLoadCallback: AdmobManager.LoadAdCallBack
    ) {
        val tag = "Load INTERSTITIAL AD"
        if (!AdmobCore.isEnableAd) {
            adLoadCallback.onAdFailed(error(AdErrorCode.ADS_DISABLED, "Ads is DISABLE now!"))
            Log.e(tag, "Ads is DISABLE now!")
            return
        }

        if (!activity.isNetworkConnected()) {
            adLoadCallback.onAdFailed(error(AdErrorCode.NO_INTERNET, "No Internet!"))
            Log.e(tag, "No Internet!")
            return
        }
        val resolvedId = resolveInterstitialId(activity, idAd)
        if (resolvedId == null) {
            Log.e(tag, "Ad Id is blank!")
            adLoadCallback.onAdFailed(error(AdErrorCode.BLANK_AD_UNIT_ID, "Ad Id is blank!"))
            return
        }

        val interState = InternalAdCache.interstitial(resolvedId)
        if (interState.ad != null) {
            Log.e(tag, "This Interstitial Ad is not empty. Don't need to load again!")
            adLoadCallback.onAdFailed(error(AdErrorCode.ALREADY_LOADED, "This Interstitial Ad is not empty. Don't need to load again!"))
            return
        }
        interState.isLoading = true

        if (AdmobCore.adRequest == null) {
            AdmobCore.initAdRequest(AdmobCore.getTimeout())
        }
        InterstitialAd.load(
            activity,
            resolvedId,
            AdmobCore.adRequest!!,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    if (AdmobCore.isOverlayAdShowing) {
                        interState.liveData.value = interstitialAd
                    }
                    interState.ad = interstitialAd
                    interState.isLoading = false
                    adLoadCallback.onAdLoaded()
                    Log.d(tag, "onAdLoaded")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interState.isLoading = false
                    if (AdmobCore.isOverlayAdShowing) {
                        interState.liveData.value = null
                    }
                    adLoadCallback.onAdFailed(error(AdErrorCode.LOAD_FAILED, loadAdError.message + "\nCause\n" + loadAdError.cause))
                    Log.e(tag, loadAdError.message + "\nCause\n" + loadAdError.cause)
                }
            })
    }

    fun showInterstitialAd(
        activity: Activity,
        idAd: String,
        adCallback: AdmobManager.ShowAdCallBack,
    ) {
        val tag = "Show INTERSTITIAL AD"
        if (AdmobCore.isOverlayAdShowing) {
            adCallback.onAdFailed(error(AdErrorCode.ALREADY_SHOWING, "Other Ads is showing now!"))
            Log.e(tag, "Other Ads is showing now!")
            return
        }

        if (!AdmobCore.isEnableAd) {
            AdmobCore.isOverlayAdShowing = false
            enableResumeAdsIfNeeded()
            adCallback.onAdFailed(error(AdErrorCode.ADS_DISABLED, "Ads is DISABLE now!"))
            Log.e(tag, "Ads is DISABLE now!")
            return
        }

        if (!activity.isNetworkConnected()) {
            AdmobCore.isOverlayAdShowing = false
            enableResumeAdsIfNeeded()
            adCallback.onAdFailed(error(AdErrorCode.NO_INTERNET, "No Internet!"))
            Log.e(tag, "No Internet!")
            return
        }
        val resolvedId = resolveInterstitialId(activity, idAd)
        if (resolvedId == null) {
            Log.e(tag, "Ad Id is blank!")
            adCallback.onAdFailed(error(AdErrorCode.BLANK_AD_UNIT_ID, "Ad Id is blank!"))
            return
        }
        val interState = InternalAdCache.interstitial(resolvedId)
        AdmobCore.isOverlayAdShowing = true
        var loadingDialog: Dialog? = null

        val handler = Handler(Looper.getMainLooper())
        val runnable = Runnable {
            if (interState.isLoading) {
                enableResumeAdsIfNeeded()
                AdmobCore.isOverlayAdShowing = false
                interState.liveData.removeObservers(activity as LifecycleOwner)
                dismissDialog(loadingDialog)
                adCallback.onAdFailed(error(AdErrorCode.TIMEOUT, "Time out!"))
                Log.e(tag, "Time out!")
            }
        }
        handler.postDelayed(runnable, 10000)

        if (interState.isLoading) {
            loadingDialog = createLoadingDialog(activity)

            interState.liveData.observe((activity as LifecycleOwner)) { interstitialAd: InterstitialAd? ->
                if (interstitialAd != null) {
                    interState.liveData.removeObservers(activity)

                    Handler(Looper.getMainLooper()).postDelayed({
                        interstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                AdmobCore.isOverlayAdShowing = false
                                enableResumeAdsIfNeeded()
                            InternalAdCache.clearInterstitial(resolvedId)
                            interState.liveData.removeObservers(activity as LifecycleOwner)
                                adCallback.onAdClosed()
                                Log.d(tag, "onAdClosed")
                                dismissDialog(loadingDialog)
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                AdmobCore.isOverlayAdShowing = false
                                enableResumeAdsIfNeeded()
                                InternalAdCache.clearInterstitial(resolvedId)
                                dismissDialog(loadingDialog)
                                interState.liveData.removeObservers(activity)
                                handler.removeCallbacksAndMessages(null)
                                adCallback.onAdFailed(error(AdErrorCode.SHOW_FAILED, adError.message + " \ncause: \n" + adError.cause))
                                Log.e(tag, adError.message + " \ncause: \n" + adError.cause)
                            }

                            override fun onAdShowedFullScreenContent() {
                                handler.removeCallbacksAndMessages(null)
                                AdmobCore.isOverlayAdShowing = true
                                adCallback.onAdShowed()
                                Log.d(tag, "onAdShowed")
                                try {
                                    interstitialAd.setOnPaidEventListener { adValue ->
                                        adCallback.onAdPaid(
                                            adValue,
                                            interstitialAd.adUnitId,
                                            interstitialAd.responseInfo.mediationAdapterClassName
                                                ?: "GoogleAdmob"
                                        )
                                    }
                                } catch (_: Exception) {
                                }
                            }
                        }
                        showInterstitialAdNew(activity, interstitialAd, loadingDialog, adCallback)
                    }, 400)
                } else {
                    interState.isLoading = true
                }
            }
            return
        }

        if (interState.ad == null) {
            AdmobCore.isOverlayAdShowing = false
            enableResumeAdsIfNeeded()
            adCallback.onAdFailed(error(AdErrorCode.AD_NOT_READY, "Inter Ad is null. Load inter ad before show!"))
            Log.e(tag, "Inter Ad is null. Load inter ad before show!")
            handler.removeCallbacksAndMessages(null)
        } else {
            loadingDialog = createLoadingDialog(activity)
            Handler(Looper.getMainLooper()).postDelayed({
                interState.ad?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        AdmobCore.isOverlayAdShowing = false
                        enableResumeAdsIfNeeded()
                        interState.liveData.removeObservers(activity as LifecycleOwner)
                        InternalAdCache.clearInterstitial(resolvedId)
                        adCallback.onAdClosed()
                        Log.d(tag, "onAdClosed")
                        dismissDialog(loadingDialog)
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        AdmobCore.isOverlayAdShowing = false
                        enableResumeAdsIfNeeded()
                        handler.removeCallbacksAndMessages(null)
                        InternalAdCache.clearInterstitial(resolvedId)
                        interState.liveData.removeObservers(activity as LifecycleOwner)
                        dismissDialog(loadingDialog)
                        adCallback.onAdFailed(error(AdErrorCode.SHOW_FAILED, adError.message + " \ncause:\n" + adError.cause))
                        Log.e(tag, adError.message + " \ncause:\n" + adError.cause)
                    }

                    override fun onAdShowedFullScreenContent() {
                        handler.removeCallbacksAndMessages(null)
                        AdmobCore.isOverlayAdShowing = true
                        adCallback.onAdShowed()
                        Log.d(tag, "onAdShowed")
                    }
                }
                showInterstitialAdNew(activity, interState.ad, loadingDialog, adCallback)
            }, 400)
        }
    }

    fun loadAndShowInterstitialAd(
        activity: Activity,
        idAd: String,
        adCallback: AdmobManager.LoadAndShowAdCallBack
    ) {
        val tag = "Load and show INTERSTITIAL AD"
        if (AdmobCore.adRequest == null) {
            AdmobCore.initAdRequest(AdmobCore.getTimeout())
        }
        if (AdmobCore.isOverlayAdShowing) {
            adCallback.onAdFailed(error(AdErrorCode.ALREADY_SHOWING, "Other ad is showing!"))
            Log.e(tag, "Other ad is showing!")
            return
        }
        if (!AdmobCore.isEnableAd) {
            adCallback.onAdFailed(error(AdErrorCode.ADS_DISABLED, "Ads is DISABLE now!"))
            Log.e(tag, "Ads is DISABLE now!")
            enableResumeAdsIfNeeded()
            return
        }

        if (!activity.isNetworkConnected()) {
            adCallback.onAdFailed(error(AdErrorCode.NO_INTERNET, "No Internet!"))
            Log.e(tag, "No Internet!")
            enableResumeAdsIfNeeded()
            return
        }
        val resolvedId = resolveInterstitialId(activity, idAd)
        if (resolvedId == null) {
            Log.e(tag, "Ad Id is blank!")
            adCallback.onAdFailed(error(AdErrorCode.BLANK_AD_UNIT_ID, "Ad Id is blank!"))
            return
        }
        var loadingDialog: Dialog? = createLoadingDialog(activity)
        AdmobCore.isOverlayAdShowing = true

        InterstitialAd.load(
            activity,
            resolvedId,
            AdmobCore.adRequest!!,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    super.onAdLoaded(interstitialAd)

                    adCallback.onAdLoaded()
                    interstitialAd.onPaidEventListener = OnPaidEventListener { adValue: AdValue? ->
                        adCallback.onAdPaid(
                            adValue!!,
                            resolvedId,
                            interstitialAd.responseInfo.mediationAdapterClassName ?: "GoogleAdmob"
                        )
                    }
                    interstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            Log.e(tag, adError.message + "\nCause\n" + adError.cause)
                            AdmobCore.isOverlayAdShowing = false
                            enableResumeAdsIfNeeded()
                            InternalAdCache.clearInterstitial(resolvedId)
                            dismissDialog(loadingDialog)
                            adCallback.onAdFailed(error(AdErrorCode.SHOW_FAILED, adError.message + "\nCause\n" + adError.cause))
                        }

                        override fun onAdDismissedFullScreenContent() {
                            adCallback.onAdClosed()
                            Log.d(tag, "onAdClosed")
                            dismissDialog(loadingDialog)
                            InternalAdCache.clearInterstitial(resolvedId)
                            AdmobCore.isOverlayAdShowing = false
                            enableResumeAdsIfNeeded()
                        }

                        override fun onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent()
                            adCallback.onAdShowed()
                            Log.d(tag, "onAdShowed")
                            dismissDialog(loadingDialog)
                        }
                    }

                    if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        AdmobCore.isOverlayAdShowing = true
                        adCallback.onAdShowed()
                        Log.d(tag, "onAdShowed")
                        interstitialAd.setOnPaidEventListener { adValue ->
                            adCallback.onAdPaid(
                                adValue,
                                interstitialAd.adUnitId,
                                interstitialAd.responseInfo.mediationAdapterClassName ?: "GoogleAdmob"
                            )
                        }
                        interstitialAd.show(activity)
                    } else {
                        AdmobCore.isOverlayAdShowing = false
                        enableResumeAdsIfNeeded()
                        dismissDialog(loadingDialog)
                        adCallback.onAdFailed(error(AdErrorCode.BACKGROUND_STATE, "Your App is showing on resume ad or inter ad is null!"))
                        Log.e(tag, "Your App is showing on resume ad or inter ad is null!")
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    InternalAdCache.clearInterstitial(resolvedId)
                    enableResumeAdsIfNeeded()
                    AdmobCore.isOverlayAdShowing = false
                    adCallback.onAdFailed(error(AdErrorCode.LOAD_FAILED, loadAdError.message + "\nCause\n" + loadAdError.cause))
                    Log.e(tag, loadAdError.message + "\nCause\n" + loadAdError.cause)
                    dismissDialog(loadingDialog)
                }
            })
    }

    fun loadAndShowInterstitialAdWithoutLoadingScreen(
        activity: Activity,
        idAd: String,
        adCallback: AdmobManager.LoadAndShowAdCallBack
    ) {
        val tag = "Load and show INTERSTITIAL AD"
        if (AdmobCore.adRequest == null) {
            AdmobCore.initAdRequest(AdmobCore.getTimeout())
        }
        if (AdmobCore.isOverlayAdShowing) {
            adCallback.onAdFailed(error(AdErrorCode.ALREADY_SHOWING, "Other ad is showing!"))
            Log.e(tag, "Other ad is showing!")
            return
        }
        if (!AdmobCore.isEnableAd) {
            adCallback.onAdFailed(error(AdErrorCode.ADS_DISABLED, "Ads is DISABLE now!"))
            Log.e(tag, "Ads is DISABLE now!")
            enableResumeAdsIfNeeded()
            return
        }

        if (!activity.isNetworkConnected()) {
            adCallback.onAdFailed(error(AdErrorCode.NO_INTERNET, "No Internet!"))
            Log.e(tag, "No Internet!")
            enableResumeAdsIfNeeded()
            return
        }
        val resolvedId = resolveInterstitialId(activity, idAd)
        if (resolvedId == null) {
            Log.e(tag, "Ad Id is blank!")
            adCallback.onAdFailed(error(AdErrorCode.BLANK_AD_UNIT_ID, "Ad Id is blank!"))
            return
        }
        AdmobCore.isOverlayAdShowing = true

        InterstitialAd.load(
            activity,
            resolvedId,
            AdmobCore.adRequest!!,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    super.onAdLoaded(interstitialAd)

                    adCallback.onAdLoaded()
                    interstitialAd.onPaidEventListener = OnPaidEventListener { adValue: AdValue? ->
                        adCallback.onAdPaid(
                            adValue!!,
                            resolvedId,
                            interstitialAd.responseInfo.mediationAdapterClassName ?: "GoogleAdmob"
                        )
                    }
                    interstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            Log.e(tag, adError.message + "\nCause\n" + adError.cause)
                            AdmobCore.isOverlayAdShowing = false
                            enableResumeAdsIfNeeded()
                            InternalAdCache.clearInterstitial(resolvedId)
                            adCallback.onAdFailed(error(AdErrorCode.SHOW_FAILED, adError.message + "\nCause\n" + adError.cause))
                        }

                        override fun onAdDismissedFullScreenContent() {
                            adCallback.onAdClosed()
                            Log.d(tag, "onAdClosed")
                            InternalAdCache.clearInterstitial(resolvedId)
                            AdmobCore.isOverlayAdShowing = false
                            enableResumeAdsIfNeeded()
                        }

                        override fun onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent()
                            adCallback.onAdShowed()
                            Log.d(tag, "onAdShowed")
                        }
                    }

                    if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        AdmobCore.isOverlayAdShowing = true
                        adCallback.onAdShowed()
                        Log.d(tag, "onAdShowed")
                        interstitialAd.setOnPaidEventListener { adValue ->
                            adCallback.onAdPaid(
                                adValue,
                                interstitialAd.adUnitId,
                                interstitialAd.responseInfo.mediationAdapterClassName ?: "GoogleAdmob"
                            )
                        }
                        interstitialAd.show(activity)
                    } else {
                        AdmobCore.isOverlayAdShowing = false
                        enableResumeAdsIfNeeded()
                        adCallback.onAdFailed(error(AdErrorCode.BACKGROUND_STATE, "Your App is showing on resume ad or inter ad is null!"))
                        Log.e(tag, "Your App is showing on resume ad or inter ad is null!")
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    InternalAdCache.clearInterstitial(resolvedId)
                    enableResumeAdsIfNeeded()
                    AdmobCore.isOverlayAdShowing = false
                    adCallback.onAdFailed(error(AdErrorCode.LOAD_FAILED, loadAdError.message + "\nCause\n" + loadAdError.cause))
                    Log.e(tag, loadAdError.message + "\nCause\n" + loadAdError.cause)
                }
            })
    }

    fun loadAndShowInterstitialAdOnSplash(
        activity: Activity,
        idAd: String,
        timeoutMillis: Long,
        adCallback: AdmobManager.LoadAndShowAdCallBack,
    ) {
        val tag = "Load and show SPLASH INTERSTITIAL AD"
        if (AdmobCore.adRequest == null) {
            AdmobCore.initAdRequest(AdmobCore.getTimeout())
        }
        if (AdmobCore.isOverlayAdShowing) {
            adCallback.onAdFailed(error(AdErrorCode.ALREADY_SHOWING, "Other ad is showing!"))
            Log.e(tag, "Other ad is showing!")
            return
        }
        if (!AdmobCore.isEnableAd) {
            adCallback.onAdFailed(error(AdErrorCode.ADS_DISABLED, "Ads is DISABLE now!"))
            Log.e(tag, "Ads is DISABLE now!")
            enableResumeAdsIfNeeded()
            return
        }
        if (!activity.isNetworkConnected()) {
            adCallback.onAdFailed(error(AdErrorCode.NO_INTERNET, "No Internet!"))
            Log.e(tag, "No Internet!")
            enableResumeAdsIfNeeded()
            return
        }

        val resolvedId = resolveInterstitialId(activity, idAd)
        if (resolvedId == null) {
            Log.e(tag, "Ad Id is blank!")
            adCallback.onAdFailed(error(AdErrorCode.BLANK_AD_UNIT_ID, "Ad Id is blank!"))
            return
        }

        val loadingDialog = createLoadingDialog(activity)
        val lifecycleOwner = activity as? LifecycleOwner
        val handler = Handler(Looper.getMainLooper())

        AdmobCore.isOverlayAdShowing = true

        var finished = false
        var pendingAd: InterstitialAd? = null
        var resumeObserver: DefaultLifecycleObserver? = null

        fun cleanUpAndReleaseOverlay() {
            dismissDialog(loadingDialog)
            AdmobCore.isOverlayAdShowing = false
            enableResumeAdsIfNeeded()
        }

        val timeoutRunnable = Runnable {
            if (finished) return@Runnable
            finished = true
            resumeObserver?.let { observer ->
                lifecycleOwner?.lifecycle?.removeObserver(observer)
            }
            pendingAd = null
            cleanUpAndReleaseOverlay()
            adCallback.onAdFailed(error(AdErrorCode.TIMEOUT, "Splash interstitial timeout!"))
            Log.e(tag, "Splash interstitial timeout!")
        }
        handler.postDelayed(timeoutRunnable, timeoutMillis)

        InterstitialAd.load(
            activity,
            resolvedId,
            AdmobCore.adRequest!!,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    if (finished) return
                    adCallback.onAdLoaded()

                    interstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            if (finished) return
                            finished = true
                            handler.removeCallbacks(timeoutRunnable)
                            InternalAdCache.clearInterstitial(resolvedId)
                            cleanUpAndReleaseOverlay()
                            adCallback.onAdFailed(
                                error(
                                    AdErrorCode.SHOW_FAILED,
                                    adError.message + "\nCause\n" + adError.cause
                                )
                            )
                            Log.e(tag, adError.message + "\nCause\n" + adError.cause)
                        }

                        override fun onAdDismissedFullScreenContent() {
                            if (finished) return
                            finished = true
                            handler.removeCallbacks(timeoutRunnable)
                            InternalAdCache.clearInterstitial(resolvedId)
                            cleanUpAndReleaseOverlay()
                            adCallback.onAdClosed()
                            Log.d(tag, "onAdClosed")
                        }

                        override fun onAdShowedFullScreenContent() {
                            if (finished) return
                            adCallback.onAdShowed()
                            dismissDialog(loadingDialog)
                            Log.d(tag, "onAdShowed")
                        }
                    }

                    interstitialAd.setOnPaidEventListener { adValue ->
                        adCallback.onAdPaid(
                            adValue,
                            interstitialAd.adUnitId,
                            interstitialAd.responseInfo.mediationAdapterClassName ?: "GoogleAdmob"
                        )
                    }

                    if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        interstitialAd.show(activity)
                        return
                    }

                    if (lifecycleOwner == null) {
                        finished = true
                        handler.removeCallbacks(timeoutRunnable)
                        cleanUpAndReleaseOverlay()
                        adCallback.onAdFailed(
                            error(
                                AdErrorCode.BACKGROUND_STATE,
                                "Activity is not lifecycle-aware for splash interstitial"
                            )
                        )
                        return
                    }

                    pendingAd = interstitialAd
                    resumeObserver = object : DefaultLifecycleObserver {
                        override fun onResume(owner: LifecycleOwner) {
                            val adToShow = pendingAd ?: return
                            if (finished) return
                            if (!owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) return
                            pendingAd = null
                            owner.lifecycle.removeObserver(this)
                            adToShow.show(activity)
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(resumeObserver!!)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    if (finished) return
                    finished = true
                    handler.removeCallbacks(timeoutRunnable)
                    InternalAdCache.clearInterstitial(resolvedId)
                    cleanUpAndReleaseOverlay()
                    adCallback.onAdFailed(
                        error(
                            AdErrorCode.LOAD_FAILED,
                            loadAdError.message + "\nCause\n" + loadAdError.cause
                        )
                    )
                    Log.e(tag, loadAdError.message + "\nCause\n" + loadAdError.cause)
                }
            }
        )
    }

    private fun showInterstitialAdNew(
        activity: Activity,
        mInterstitialAd: InterstitialAd?,
        loadingDialog: Dialog?,
        adcallback: AdmobManager.ShowAdCallBack
    ) {
        val tag = "Show INTERSTITIAL AD new"
        if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && mInterstitialAd != null) {
            AdmobCore.isOverlayAdShowing = true
            Handler(Looper.getMainLooper()).postDelayed({
                adcallback.onAdShowed()
                Log.d(tag, "showInterstitialAdNew")
                mInterstitialAd.setOnPaidEventListener { adValue ->
                    adcallback.onAdPaid(
                        adValue,
                        mInterstitialAd.adUnitId,
                        mInterstitialAd.responseInfo.mediationAdapterClassName ?: "GoogleAdmob"
                    )
                }
                mInterstitialAd.show(activity)
            }, 400)
        } else {
            AdmobCore.isOverlayAdShowing = false
            enableResumeAdsIfNeeded()
            dismissDialog(loadingDialog)
            adcallback.onAdFailed(error(AdErrorCode.BACKGROUND_STATE, "Your App is showing on resume ad or inter ad is null!"))
            Log.e(tag, "Your App is showing on resume ad or inter ad is null!")
        }
    }

    private fun error(code: AdErrorCode, message: String) = AdErrorInfo(code, message)

    private fun resolveInterstitialId(context: Context, requestedId: String): String? {
        val adId = if (AdmobCore.isTestAd) {
            context.getString(R.string.id_test_interstitial_admob)
        } else {
            requestedId
        }
        return adId.takeIf { it.isNotBlank() }
    }

    private fun createLoadingDialog(activity: Activity): Dialog {
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_full_screen)
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

    private fun Context.isNetworkConnected(): Boolean = AdmobCore.run { isNetworkConnected() }

    private fun enableResumeAdsIfNeeded() {
        if (AppOpenManager.isResumeModeInitialized()) {
            AppOpenManager.setResumeModeEnabled(true)
        }
    }
}
