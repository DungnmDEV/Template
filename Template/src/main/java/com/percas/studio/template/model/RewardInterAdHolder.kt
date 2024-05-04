package com.percas.studio.template.model

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd

open class RewardInterAdHolder(var ads: String) {
    var rewardInterAd: RewardedInterstitialAd? = null
    val mutable: MutableLiveData<RewardedInterstitialAd> = MutableLiveData(null)
    var isLoading = false
}