package com.percas.studio.template.admob

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.percas.studio.template.R

internal object RewardAdManager {

    fun loadAndShowRewardAd(
        activity: Activity,
        admobId: String,
        adCallback: AdmobManager.LoadAndShowRewardAdCallBack
    ) {
        val tag = "Load and show REWARD AD"
        if (!AdmobManager.isEnableAd) {
            adCallback.onAdFailed("Ads is DISABLE now!")
            Log.e(tag, "Ads is DISABLE now!")
            return
        }

        if (!activity.isNetworkConnected()) {
            adCallback.onAdFailed("No Internet!")
            Log.e(tag, "No Internet!")
            return
        }
        if (AdmobManager.isOverlayAdShowing) {
            adCallback.onAdFailed("Other ad is showing!")
            Log.e(tag, "Other ad is showing!")
            return
        }
        if (AdmobManager.adRequest == null) {
            AdmobManager.initAdRequest(AdmobManager.getTimeout())
        }

        val idReward = resolveRewardId(activity, admobId)
        if (idReward == null) {
            Log.e(tag, "Ad Id is blank!")
            adCallback.onAdFailed("Ad Id is blank!")
            return
        }

        AdmobManager.dialogLoading(activity)
        AdmobManager.isOverlayAdShowing = true
        disableResumeAdsIfNeeded()

        RewardedAd.load(activity, idReward, AdmobManager.adRequest!!, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                adCallback.onAdFailed(loadAdError.message + "\nCause:\n" + loadAdError.cause)
                Log.e(tag, loadAdError.message + "\nCause:\n" + loadAdError.cause)
                AdmobManager.dismissAdDialog()
                enableResumeAdsIfNeeded()
                AdmobManager.isOverlayAdShowing = false
            }

            override fun onAdLoaded(rewardedAd: RewardedAd) {
                adCallback.onAdLoaded()
                Log.d(tag, "onAdLoaded")
                rewardedAd.setOnPaidEventListener {
                    adCallback.onAdPaid(
                        it,
                        rewardedAd.adUnitId,
                        rewardedAd.responseInfo.mediationAdapterClassName ?: "GoogleAdmob"
                    )
                }

                rewardedAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdShowedFullScreenContent() {
                        AdmobManager.isOverlayAdShowing = true
                        adCallback.onAdShowed()
                        Log.d(tag, "onAdShowed")
                        disableResumeAdsIfNeeded()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        AdmobManager.isOverlayAdShowing = false
                        adCallback.onAdFailed(adError.message + "\nCause:n\n" + adError.cause)
                        Log.e(tag, adError.message + "\nCause:n\n" + adError.cause)
                        AdmobManager.dismissAdDialog()
                        enableResumeAdsIfNeeded()
                    }

                    override fun onAdDismissedFullScreenContent() {
                        AdmobManager.isOverlayAdShowing = false
                        adCallback.onAdClosed()
                        Log.d(tag, "onAdClosed")
                        AdmobManager.dismissAdDialog()
                        enableResumeAdsIfNeeded()
                    }
                }

                if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    disableResumeAdsIfNeeded()
                    rewardedAd.show(activity) {
                        adCallback.onAdEarned()
                        Log.d(tag, "onAdEarned")
                        AdmobManager.dismissAdDialog()
                    }
                    AdmobManager.isOverlayAdShowing = true
                } else {
                    AdmobManager.dismissAdDialog()
                    AdmobManager.isOverlayAdShowing = false
                    enableResumeAdsIfNeeded()
                    adCallback.onAdFailed("Your App is showing on resume ad!")
                    Log.e(tag, "Your App is showing on resume ad!")
                }
            }
        })
    }

    fun loadInterReward(
        context: Context,
        idAd: String,
        adLoadCallback: AdmobManager.LoadAdCallBack
    ) {
        val tag = "Load INTERSTITIAL REWARD AD"
        if (!AdmobManager.isEnableAd) {
            adLoadCallback.onAdFailed("Ads is DISABLE now!")
            Log.e(tag, "Ads is DISABLE now!")
            return
        }

        if (!context.isNetworkConnected()) {
            adLoadCallback.onAdFailed("No Internet!")
            Log.e(tag, "No Internet!")
            return
        }
        val resolvedId = resolveRewardInterstitialId(context, idAd)
        if (resolvedId == null) {
            Log.e(tag, "Ad Id is blank!")
            adLoadCallback.onAdFailed("Ad Id is blank!")
            return
        }
        val rewardState = InternalAdCache.rewardedInterstitial(resolvedId)
        if (rewardState.ad != null) {
            adLoadCallback.onAdFailed("This Interstitial Ad is not empty. Don't need to load again!")
            Log.e(tag, "This Interstitial Ad is not empty. Don't need to load again!")
            return
        }
        rewardState.isLoading = true

        if (AdmobManager.adRequest == null) {
            AdmobManager.initAdRequest(AdmobManager.getTimeout())
        }

        RewardedInterstitialAd.load(
            context,
            resolvedId,
            AdmobManager.adRequest!!,
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialRewardAd: RewardedInterstitialAd) {
                    rewardState.ad = interstitialRewardAd
                    rewardState.liveData.value = interstitialRewardAd
                    rewardState.isLoading = false
                    adLoadCallback.onAdLoaded()
                    Log.d(tag, "onAdLoaded")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardState.ad = null
                    rewardState.isLoading = false
                    rewardState.liveData.value = null
                    adLoadCallback.onAdFailed(loadAdError.message + "\nCause\n" + loadAdError.cause)
                    Log.e(tag, loadAdError.message + "\nCause\n" + loadAdError.cause)
                }
            })
    }

    fun showInterReward(
        activity: Activity,
        idAd: String,
        adCallback: AdmobManager.ShowRewardAdCallBack
    ) {
        if (AdmobManager.adRequest == null) {
            AdmobManager.initAdRequest(AdmobManager.getTimeout())
        }
        if (AdmobManager.isOverlayAdShowing) {
            adCallback.onAdFailed("Other ad is showing")
            return
        }
        if (!AdmobManager.isEnableAd) {
            adCallback.onAdFailed("Ads is DISABLE now")
            enableResumeAdsIfNeeded()
            return
        }

        if (!activity.isNetworkConnected()) {
            adCallback.onAdFailed("No Internet")
            enableResumeAdsIfNeeded()
            return
        }

        val resolvedId = resolveRewardInterstitialId(activity, idAd)
        if (resolvedId == null) {
            adCallback.onAdFailed("Ad Id is blank!")
            return
        }
        val rewardState = InternalAdCache.rewardedInterstitial(resolvedId)

        AdmobManager.isOverlayAdShowing = true
        AdmobManager.dialogLoading(activity)

        if (rewardState.isLoading) {
            rewardState.liveData.observe(activity as LifecycleOwner) { reward: RewardedInterstitialAd? ->
                reward?.let {
                    rewardState.liveData.removeObservers(activity)
                    it.setOnPaidEventListener { value ->
                        adCallback.onAdPaid(
                            value,
                            reward.adUnitId,
                            reward.responseInfo.mediationAdapterClassName ?: "GoogleAdmob"
                        )
                    }
                    reward.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            InternalAdCache.clearRewardedInterstitial(resolvedId)
                            rewardState.liveData.removeObservers(activity)
                            enableResumeAdsIfNeeded()
                            AdmobManager.isOverlayAdShowing = false
                            AdmobManager.dismissAdDialog()
                            adCallback.onAdClosed()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            InternalAdCache.clearRewardedInterstitial(resolvedId)
                            rewardState.liveData.removeObservers(activity)
                            enableResumeAdsIfNeeded()
                            AdmobManager.isOverlayAdShowing = false
                            AdmobManager.dismissAdDialog()
                            adCallback.onAdFailed(adError.message + "\nCause:\n" + adError.cause)
                        }

                        override fun onAdShowedFullScreenContent() {
                            AdmobManager.isOverlayAdShowing = true
                            adCallback.onAdShowed()
                            AdmobManager.dismissAdDialog()
                        }
                    }
                    it.show(activity) {
                        adCallback.onAdEarned()
                    }
                }
            }
        } else {
            if (rewardState.ad != null) {
                rewardState.ad?.setOnPaidEventListener {
                    adCallback.onAdPaid(
                        it,
                        rewardState.ad!!.adUnitId,
                        rewardState.ad!!.responseInfo.mediationAdapterClassName ?: "GoogleAdmob"
                    )
                }
                rewardState.ad?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        InternalAdCache.clearRewardedInterstitial(resolvedId)
                        rewardState.liveData.removeObservers(activity as LifecycleOwner)
                        enableResumeAdsIfNeeded()
                        AdmobManager.isOverlayAdShowing = false
                        AdmobManager.dismissAdDialog()
                        adCallback.onAdClosed()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        InternalAdCache.clearRewardedInterstitial(resolvedId)
                        rewardState.liveData.removeObservers(activity as LifecycleOwner)
                        enableResumeAdsIfNeeded()
                        AdmobManager.isOverlayAdShowing = false
                        AdmobManager.dismissAdDialog()
                        adCallback.onAdFailed(adError.message + "\nCause:\n" + adError.cause)
                    }

                    override fun onAdShowedFullScreenContent() {
                        AdmobManager.isOverlayAdShowing = true
                        adCallback.onAdShowed()
                        AdmobManager.dismissAdDialog()
                    }
                }
                rewardState.ad?.show(activity) { adCallback.onAdEarned() }
            } else {
                AdmobManager.isOverlayAdShowing = false
                adCallback.onAdFailed("Ad is null. Load Inter Reward before show it!")
                AdmobManager.dismissAdDialog()
                enableResumeAdsIfNeeded()
            }
        }
    }

    private fun resolveRewardId(context: Context, requestedId: String): String? {
        val adId = if (AdmobManager.isTestAd) {
            context.getString(R.string.id_test_reward_admob)
        } else {
            requestedId
        }
        return adId.takeIf { it.isNotBlank() }
    }

    private fun resolveRewardInterstitialId(context: Context, requestedId: String): String? {
        val adId = if (AdmobManager.isTestAd) {
            context.getString(R.string.id_test_reward_inter_admob)
        } else {
            requestedId
        }
        return adId.takeIf { it.isNotBlank() }
    }

    private fun Context.isNetworkConnected(): Boolean = AdmobManager.run { isNetworkConnected() }

    private fun disableResumeAdsIfNeeded() {
        if (AppResumeAdsManager.getInstance().isInitialized) {
            AppResumeAdsManager.getInstance().isAppResumeEnabled = false
        }
    }

    private fun enableResumeAdsIfNeeded() {
        if (AppResumeAdsManager.getInstance().isInitialized) {
            AppResumeAdsManager.getInstance().isAppResumeEnabled = true
        }
    }
}
