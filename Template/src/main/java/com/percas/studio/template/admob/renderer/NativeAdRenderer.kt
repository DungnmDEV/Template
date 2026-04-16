package com.percas.studio.template.admob.renderer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

enum class NativeAdLoadingStyle {
    SMALL,
    MEDIUM,
    FULLSCREEN,
}

interface NativeAdRenderer<T : ViewBinding> {
    val loadingStyle: NativeAdLoadingStyle

    fun inflate(layoutInflater: LayoutInflater, parent: ViewGroup): T

    fun root(binding: T): NativeAdView

    fun bind(binding: T, nativeAd: NativeAd)
}
