package com.percas.studio.template.model

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.ads.interstitial.InterstitialAd

open class InterAdHolder(var ads: String) {
    var inter: InterstitialAd? = null
    val mutable: MutableLiveData<InterstitialAd> = MutableLiveData(null)
    var isloading = false
}