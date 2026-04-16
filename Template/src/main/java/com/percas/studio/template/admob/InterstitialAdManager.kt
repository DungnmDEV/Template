package com.percas.studio.template.admob

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
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
            adLoadCallback.onAdFailed("Ads is DISABLE now!")
            Log.e(tag, "Ads is DISABLE now!")
            return
        }

        if (!activity.isNetworkConnected()) {
            adLoadCallback.onAdFailed("No Internet!")
            Log.e(tag, "No Internet!")
            return
        }
        val resolvedId = resolveInterstitialId(activity, idAd)
        if (resolvedId == null) {
            Log.e(tag, "Ad Id is blank!")
            adLoadCallback.onAdFailed("Ad Id is blank!")
            return
        }

        val interState = InternalAdCache.interstitial(resolvedId)
        if (interState.ad != null) {
            Log.e(tag, "This Interstitial Ad is not empty. Don't need to load again!")
            adLoadCallback.onAdFailed("This Interstitial Ad is not empty. Don't need to load again!")
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
                    adLoadCallback.onAdFailed(loadAdError.message + "\nCause\n" + loadAdError.cause)
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
            adCallback.onAdFailed("Other Ads is showing now!")
            Log.e(tag, "Other Ads is showing now!")
            return
        }

        if (!AdmobCore.isEnableAd) {
            AdmobCore.isOverlayAdShowing = false
            enableResumeAdsIfNeeded()
            adCallback.onAdFailed("Ads is DISABLE now!")
            Log.e(tag, "Ads is DISABLE now!")
            return
        }

        if (!activity.isNetworkConnected()) {
            AdmobCore.isOverlayAdShowing = false
            enableResumeAdsIfNeeded()
            adCallback.onAdFailed("No Internet!")
            Log.e(tag, "No Internet!")
            return
        }
        val resolvedId = resolveInterstitialId(activity, idAd)
        if (resolvedId == null) {
            Log.e(tag, "Ad Id is blank!")
            adCallback.onAdFailed("Ad Id is blank!")
            return
        }
        val interState = InternalAdCache.interstitial(resolvedId)
        AdmobCore.isOverlayAdShowing = true

        val handler = Handler(Looper.getMainLooper())
        val runnable = Runnable {
            if (interState.isLoading) {
                enableResumeAdsIfNeeded()
                AdmobCore.isOverlayAdShowing = false
                interState.liveData.removeObservers(activity as LifecycleOwner)
                AdmobCore.dismissAdDialog()
                adCallback.onAdFailed("Time out!")
                Log.e(tag, "Time out!")
            }
        }
        handler.postDelayed(runnable, 10000)

        if (interState.isLoading) {
            AdmobCore.dialogLoading(activity)

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
                                AdmobCore.dismissAdDialog()
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                AdmobCore.isOverlayAdShowing = false
                                enableResumeAdsIfNeeded()
                                InternalAdCache.clearInterstitial(resolvedId)
                                AdmobCore.dismissAdDialog()
                                interState.liveData.removeObservers(activity)
                                handler.removeCallbacksAndMessages(null)
                                adCallback.onAdFailed(adError.message + " \ncause: \n" + adError.cause)
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
                        showInterstitialAdNew(activity, interstitialAd, adCallback)
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
            adCallback.onAdFailed("Inter Ad is null. Load inter ad before show!")
            Log.e(tag, "Inter Ad is null. Load inter ad before show!")
            handler.removeCallbacksAndMessages(null)
        } else {
            AdmobCore.dialogLoading(activity)
            Handler(Looper.getMainLooper()).postDelayed({
                interState.ad?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        AdmobCore.isOverlayAdShowing = false
                        enableResumeAdsIfNeeded()
                        interState.liveData.removeObservers(activity as LifecycleOwner)
                        InternalAdCache.clearInterstitial(resolvedId)
                        adCallback.onAdClosed()
                        Log.d(tag, "onAdClosed")
                        AdmobCore.dismissAdDialog()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        AdmobCore.isOverlayAdShowing = false
                        enableResumeAdsIfNeeded()
                        handler.removeCallbacksAndMessages(null)
                        InternalAdCache.clearInterstitial(resolvedId)
                        interState.liveData.removeObservers(activity as LifecycleOwner)
                        AdmobCore.dismissAdDialog()
                        adCallback.onAdFailed(adError.message + " \ncause:\n" + adError.cause)
                        Log.e(tag, adError.message + " \ncause:\n" + adError.cause)
                    }

                    override fun onAdShowedFullScreenContent() {
                        handler.removeCallbacksAndMessages(null)
                        AdmobCore.isOverlayAdShowing = true
                        adCallback.onAdShowed()
                        Log.d(tag, "onAdShowed")
                    }
                }
                showInterstitialAdNew(activity, interState.ad, adCallback)
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
            adCallback.onAdFailed("Other ad is showing!")
            Log.e(tag, "Other ad is showing!")
            return
        }
        if (!AdmobCore.isEnableAd) {
            adCallback.onAdFailed("Ads is DISABLE now!")
            Log.e(tag, "Ads is DISABLE now!")
            enableResumeAdsIfNeeded()
            return
        }

        if (!activity.isNetworkConnected()) {
            adCallback.onAdFailed("No Internet!")
            Log.e(tag, "No Internet!")
            enableResumeAdsIfNeeded()
            return
        }
        val resolvedId = resolveInterstitialId(activity, idAd)
        if (resolvedId == null) {
            Log.e(tag, "Ad Id is blank!")
            adCallback.onAdFailed("Ad Id is blank!")
            return
        }
        AdmobCore.isOverlayAdShowing = true
        AdmobCore.dialogLoading(activity)

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
                            adCallback.onAdFailed(adError.message + "\nCause\n" + adError.cause)
                            Log.e(tag, adError.message + "\nCause\n" + adError.cause)
                            AdmobCore.isOverlayAdShowing = false
                            enableResumeAdsIfNeeded()
                            InternalAdCache.clearInterstitial(resolvedId)
                            AdmobCore.dismissAdDialog()
                        }

                        override fun onAdDismissedFullScreenContent() {
                            adCallback.onAdClosed()
                            Log.d(tag, "onAdClosed")
                            AdmobCore.dismissAdDialog()
                            InternalAdCache.clearInterstitial(resolvedId)
                            AdmobCore.isOverlayAdShowing = false
                            enableResumeAdsIfNeeded()
                        }

                        override fun onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent()
                            adCallback.onAdShowed()
                            Log.d(tag, "onAdShowed")
                            AdmobManager.dismissAdDialog()
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
                        AdmobCore.dismissAdDialog()
                        adCallback.onAdFailed("Your App is showing on resume ad or inter ad is null!")
                        Log.e(tag, "Your App is showing on resume ad or inter ad is null!")
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    InternalAdCache.clearInterstitial(resolvedId)
                    enableResumeAdsIfNeeded()
                    AdmobCore.isOverlayAdShowing = false
                    adCallback.onAdFailed(loadAdError.message + "\nCause\n" + loadAdError.cause)
                    Log.e(tag, loadAdError.message + "\nCause\n" + loadAdError.cause)
                    AdmobCore.dismissAdDialog()
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
            adCallback.onAdFailed("Other ad is showing!")
            Log.e(tag, "Other ad is showing!")
            return
        }
        if (!AdmobCore.isEnableAd) {
            adCallback.onAdFailed("Ads is DISABLE now!")
            Log.e(tag, "Ads is DISABLE now!")
            enableResumeAdsIfNeeded()
            return
        }

        if (!activity.isNetworkConnected()) {
            adCallback.onAdFailed("No Internet!")
            Log.e(tag, "No Internet!")
            enableResumeAdsIfNeeded()
            return
        }
        val resolvedId = resolveInterstitialId(activity, idAd)
        if (resolvedId == null) {
            Log.e(tag, "Ad Id is blank!")
            adCallback.onAdFailed("Ad Id is blank!")
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
                            adCallback.onAdFailed(adError.message + "\nCause\n" + adError.cause)
                            Log.e(tag, adError.message + "\nCause\n" + adError.cause)
                            AdmobCore.isOverlayAdShowing = false
                            enableResumeAdsIfNeeded()
                            InternalAdCache.clearInterstitial(resolvedId)
                            AdmobCore.dismissAdDialog()
                        }

                        override fun onAdDismissedFullScreenContent() {
                            adCallback.onAdClosed()
                            Log.d(tag, "onAdClosed")
                            AdmobCore.dismissAdDialog()
                            InternalAdCache.clearInterstitial(resolvedId)
                            AdmobCore.isOverlayAdShowing = false
                            enableResumeAdsIfNeeded()
                        }

                        override fun onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent()
                            adCallback.onAdShowed()
                            Log.d(tag, "onAdShowed")
                            AdmobManager.dismissAdDialog()
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
                        AdmobCore.dismissAdDialog()
                        adCallback.onAdFailed("Your App is showing on resume ad or inter ad is null!")
                        Log.e(tag, "Your App is showing on resume ad or inter ad is null!")
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    InternalAdCache.clearInterstitial(resolvedId)
                    enableResumeAdsIfNeeded()
                    AdmobCore.isOverlayAdShowing = false
                    adCallback.onAdFailed(loadAdError.message + "\nCause\n" + loadAdError.cause)
                    Log.e(tag, loadAdError.message + "\nCause\n" + loadAdError.cause)
                    AdmobCore.dismissAdDialog()
                }
            })
    }

    private fun showInterstitialAdNew(
        activity: Activity,
        mInterstitialAd: InterstitialAd?,
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
            AdmobCore.dismissAdDialog()
            adcallback.onAdFailed("Your App is showing on resume ad or inter ad is null!")
            Log.e(tag, "Your App is showing on resume ad or inter ad is null!")
        }
    }

    private fun resolveInterstitialId(context: Context, requestedId: String): String? {
        val adId = if (AdmobCore.isTestAd) {
            context.getString(R.string.id_test_interstitial_admob)
        } else {
            requestedId
        }
        return adId.takeIf { it.isNotBlank() }
    }

    private fun Context.isNetworkConnected(): Boolean = AdmobCore.run { isNetworkConnected() }

    private fun enableResumeAdsIfNeeded() {
        if (AppResumeAdsManager.getInstance().isInitialized) {
            AppResumeAdsManager.getInstance().isAppResumeEnabled = true
        }
    }
}
