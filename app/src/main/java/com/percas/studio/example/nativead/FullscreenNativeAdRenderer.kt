package com.percas.studio.example.nativead

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.percas.studio.example.databinding.AdUnifiedFullscreenBinding
import com.percas.studio.template.admob.renderer.NativeAdLoadingStyle
import com.percas.studio.template.admob.renderer.NativeAdRenderer

class FullscreenNativeAdRenderer : NativeAdRenderer<AdUnifiedFullscreenBinding> {
    override val loadingStyle = NativeAdLoadingStyle.FULLSCREEN

    override fun inflate(
        layoutInflater: LayoutInflater,
        parent: ViewGroup
    ): AdUnifiedFullscreenBinding = AdUnifiedFullscreenBinding.inflate(layoutInflater, parent, false)

    override fun root(binding: AdUnifiedFullscreenBinding): NativeAdView = binding.root

    override fun bind(binding: AdUnifiedFullscreenBinding, nativeAd: NativeAd) {
        binding.root.mediaView = binding.adMedia
        binding.root.headlineView = binding.adHeadline
        binding.root.bodyView = binding.adBody
        binding.root.callToActionView = binding.adCallToAction
        binding.root.iconView = binding.adAppIcon

        binding.adHeadline.text = nativeAd.headline
        binding.adMedia.mediaContent = nativeAd.mediaContent

        binding.adBody.apply {
            text = nativeAd.body
            visibility = if (nativeAd.body.isNullOrBlank()) View.INVISIBLE else View.VISIBLE
        }

        binding.adCallToAction.apply {
            text = nativeAd.callToAction
            visibility = if (nativeAd.callToAction.isNullOrBlank()) View.INVISIBLE else View.VISIBLE
        }

        if (nativeAd.icon == null) {
            binding.adAppIcon.visibility = View.GONE
        } else {
            binding.adAppIcon.setImageDrawable(nativeAd.icon!!.drawable)
            binding.adAppIcon.visibility = View.VISIBLE
        }

        nativeAd.mediaContent?.videoController?.takeIf { it.hasVideoContent() }?.videoLifecycleCallbacks =
            object : com.google.android.gms.ads.VideoController.VideoLifecycleCallbacks() {}

        binding.root.setNativeAd(nativeAd)
    }
}
