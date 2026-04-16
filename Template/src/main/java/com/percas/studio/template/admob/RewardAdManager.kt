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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.airbnb.lottie.LottieAnimationView
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
        if (!AdmobCore.isEnableAd) {
            adCallback.onAdFailed("Ads is DISABLE now!")
            Log.e(tag, "Ads is DISABLE now!")
            return
        }

        if (!activity.isNetworkConnected()) {
            adCallback.onAdFailed("No Internet!")
            Log.e(tag, "No Internet!")
            return
        }
        if (AdmobCore.isOverlayAdShowing) {
            adCallback.onAdFailed("Other ad is showing!")
            Log.e(tag, "Other ad is showing!")
            return
        }
        if (AdmobCore.adRequest == null) {
            AdmobCore.initAdRequest(AdmobCore.getTimeout())
        }

        val idReward = resolveRewardId(activity, admobId)
        if (idReward == null) {
            Log.e(tag, "Ad Id is blank!")
            adCallback.onAdFailed("Ad Id is blank!")
            return
        }

        val loadingDialog = createLoadingDialog(activity)
        AdmobCore.isOverlayAdShowing = true
        disableResumeAdsIfNeeded()

        RewardedAd.load(activity, idReward, AdmobCore.adRequest!!, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                adCallback.onAdFailed(loadAdError.message + "\nCause:\n" + loadAdError.cause)
                Log.e(tag, loadAdError.message + "\nCause:\n" + loadAdError.cause)
                dismissDialog(loadingDialog)
                enableResumeAdsIfNeeded()
                AdmobCore.isOverlayAdShowing = false
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
                        AdmobCore.isOverlayAdShowing = true
                        adCallback.onAdShowed()
                        Log.d(tag, "onAdShowed")
                        disableResumeAdsIfNeeded()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        AdmobCore.isOverlayAdShowing = false
                        adCallback.onAdFailed(adError.message + "\nCause:n\n" + adError.cause)
                        Log.e(tag, adError.message + "\nCause:n\n" + adError.cause)
                        dismissDialog(loadingDialog)
                        enableResumeAdsIfNeeded()
                    }

                    override fun onAdDismissedFullScreenContent() {
                        AdmobCore.isOverlayAdShowing = false
                        adCallback.onAdClosed()
                        Log.d(tag, "onAdClosed")
                        dismissDialog(loadingDialog)
                        enableResumeAdsIfNeeded()
                    }
                }

                if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    disableResumeAdsIfNeeded()
                    rewardedAd.show(activity) {
                        adCallback.onAdEarned()
                        Log.d(tag, "onAdEarned")
                        dismissDialog(loadingDialog)
                    }
                    AdmobCore.isOverlayAdShowing = true
                } else {
                    dismissDialog(loadingDialog)
                    AdmobCore.isOverlayAdShowing = false
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
        if (!AdmobCore.isEnableAd) {
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

        if (AdmobCore.adRequest == null) {
            AdmobCore.initAdRequest(AdmobCore.getTimeout())
        }

        RewardedInterstitialAd.load(
            context,
            resolvedId,
            AdmobCore.adRequest!!,
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
        if (AdmobCore.adRequest == null) {
            AdmobCore.initAdRequest(AdmobCore.getTimeout())
        }
        if (AdmobCore.isOverlayAdShowing) {
            adCallback.onAdFailed("Other ad is showing")
            return
        }
        if (!AdmobCore.isEnableAd) {
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
        val handler = Handler(Looper.getMainLooper())

        AdmobCore.isOverlayAdShowing = true
        val loadingDialog = createLoadingDialog(activity)

        if (rewardState.isLoading) {
            val timeoutRunnable = Runnable {
                if (rewardState.isLoading) {
                    rewardState.liveData.removeObservers(activity as LifecycleOwner)
                    enableResumeAdsIfNeeded()
                    AdmobCore.isOverlayAdShowing = false
                    dismissDialog(loadingDialog)
                    adCallback.onAdFailed("Time out!")
                }
            }
            handler.postDelayed(timeoutRunnable, AdmobCore.getTimeout().toLong())

            rewardState.liveData.observe(activity as LifecycleOwner) { reward: RewardedInterstitialAd? ->
                reward?.let {
                    handler.removeCallbacks(timeoutRunnable)
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
                            handler.removeCallbacks(timeoutRunnable)
                            InternalAdCache.clearRewardedInterstitial(resolvedId)
                            rewardState.liveData.removeObservers(activity)
                            enableResumeAdsIfNeeded()
                            AdmobCore.isOverlayAdShowing = false
                            dismissDialog(loadingDialog)
                            adCallback.onAdClosed()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            handler.removeCallbacks(timeoutRunnable)
                            InternalAdCache.clearRewardedInterstitial(resolvedId)
                            rewardState.liveData.removeObservers(activity)
                            enableResumeAdsIfNeeded()
                            AdmobCore.isOverlayAdShowing = false
                            dismissDialog(loadingDialog)
                            adCallback.onAdFailed(adError.message + "\nCause:\n" + adError.cause)
                        }

                        override fun onAdShowedFullScreenContent() {
                            handler.removeCallbacks(timeoutRunnable)
                            AdmobCore.isOverlayAdShowing = true
                            adCallback.onAdShowed()
                            dismissDialog(loadingDialog)
                        }
                    }
                    it.show(activity) {
                        handler.removeCallbacks(timeoutRunnable)
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
                        AdmobCore.isOverlayAdShowing = false
                        dismissDialog(loadingDialog)
                        adCallback.onAdClosed()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        InternalAdCache.clearRewardedInterstitial(resolvedId)
                        rewardState.liveData.removeObservers(activity as LifecycleOwner)
                        enableResumeAdsIfNeeded()
                        AdmobCore.isOverlayAdShowing = false
                        dismissDialog(loadingDialog)
                        adCallback.onAdFailed(adError.message + "\nCause:\n" + adError.cause)
                    }

                    override fun onAdShowedFullScreenContent() {
                        AdmobCore.isOverlayAdShowing = true
                        adCallback.onAdShowed()
                        dismissDialog(loadingDialog)
                    }
                }
                rewardState.ad?.show(activity) { adCallback.onAdEarned() }
            } else {
                AdmobCore.isOverlayAdShowing = false
                adCallback.onAdFailed("Ad is null. Load Inter Reward before show it!")
                dismissDialog(loadingDialog)
                enableResumeAdsIfNeeded()
            }
        }
    }

    private fun resolveRewardId(context: Context, requestedId: String): String? {
        val adId = if (AdmobCore.isTestAd) {
            context.getString(R.string.id_test_reward_admob)
        } else {
            requestedId
        }
        return adId.takeIf { it.isNotBlank() }
    }

    private fun resolveRewardInterstitialId(context: Context, requestedId: String): String? {
        val adId = if (AdmobCore.isTestAd) {
            context.getString(R.string.id_test_reward_inter_admob)
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

    private fun disableResumeAdsIfNeeded() {
        if (AppOpenManager.isResumeModeInitialized()) {
            AppOpenManager.setResumeModeEnabled(false)
        }
    }

    private fun enableResumeAdsIfNeeded() {
        if (AppOpenManager.isResumeModeInitialized()) {
            AppOpenManager.setResumeModeEnabled(true)
        }
    }
}
