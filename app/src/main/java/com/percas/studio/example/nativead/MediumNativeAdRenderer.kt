package com.percas.studio.example.nativead

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.percas.studio.example.databinding.AdUnifiedMediumBinding
import com.percas.studio.template.admob.renderer.NativeAdLoadingStyle
import com.percas.studio.template.admob.renderer.NativeAdRenderer

class MediumNativeAdRenderer : NativeAdRenderer<AdUnifiedMediumBinding> {
    override val loadingStyle = NativeAdLoadingStyle.MEDIUM

    override fun inflate(
        layoutInflater: LayoutInflater,
        parent: ViewGroup
    ): AdUnifiedMediumBinding = AdUnifiedMediumBinding.inflate(layoutInflater, parent, false)

    override fun root(binding: AdUnifiedMediumBinding): NativeAdView = binding.root

    override fun bind(binding: AdUnifiedMediumBinding, nativeAd: NativeAd) {
        binding.root.mediaView = binding.adMedia
        binding.root.headlineView = binding.adHeadline
        binding.root.bodyView = binding.adBody
        binding.root.callToActionView = binding.adCallToAction
        binding.root.iconView = binding.adAppIcon
        binding.root.starRatingView = binding.adStars

        binding.adHeadline.text = nativeAd.headline
        binding.adMedia.setImageScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE)
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

        if (nativeAd.starRating == null) {
            binding.adStars.visibility = View.GONE
        } else {
            binding.adStars.rating = nativeAd.starRating!!.toFloat()
            binding.adStars.visibility = View.VISIBLE
        }

        nativeAd.mediaContent?.videoController?.takeIf { it.hasVideoContent() }?.videoLifecycleCallbacks =
            object : com.google.android.gms.ads.VideoController.VideoLifecycleCallbacks() {}

        binding.root.setNativeAd(nativeAd)
    }
}
