package com.percas.studio.template.admob

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd

internal object InternalAdCache {
    data class InterstitialState(
        var ad: InterstitialAd? = null,
        var isLoading: Boolean = false,
        val liveData: MutableLiveData<InterstitialAd?> = MutableLiveData(null),
    )

    data class RewardedInterstitialState(
        var ad: RewardedInterstitialAd? = null,
        var isLoading: Boolean = false,
        val liveData: MutableLiveData<RewardedInterstitialAd?> = MutableLiveData(null),
    )

    val interstitialAds = mutableMapOf<String, InterstitialState>()
    val rewardedInterstitialAds = mutableMapOf<String, RewardedInterstitialState>()

    fun interstitial(idAd: String): InterstitialState =
        interstitialAds.getOrPut(idAd) { InterstitialState() }

    fun rewardedInterstitial(idAd: String): RewardedInterstitialState =
        rewardedInterstitialAds.getOrPut(idAd) { RewardedInterstitialState() }

    fun clearInterstitial(idAd: String) {
        interstitialAds.remove(idAd)?.liveData?.value = null
    }

    fun clearRewardedInterstitial(idAd: String) {
        rewardedInterstitialAds.remove(idAd)?.liveData?.value = null
    }
}
