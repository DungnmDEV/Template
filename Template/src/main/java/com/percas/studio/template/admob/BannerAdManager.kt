@file:Suppress("DEPRECATION")

package com.percas.studio.template.admob

import android.app.Activity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.ViewGroup
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.percas.studio.template.R

internal object BannerAdManager {

    fun loadAndShowBannerAd(
        activity: Activity,
        idBannerAd: String,
        viewBannerAd: ViewGroup,
        adCallBack: AdmobManager.LoadAndShowAdCallBack
    ) {
        val tag = "Load and show BANNER AD"
        if (!AdmobCore.isEnableAd) {
            Log.e(tag, "Ads is Disable now!")
            adCallBack.onAdFailed("Ads is Disable now!")
            return
        }
        if (!activity.isNetworkConnected()) {
            Log.e(tag, "No internet!")
            adCallBack.onAdFailed("No internet!")
            return
        }
        val adView = AdView(activity)
        adView.adUnitId = if (AdmobCore.isTestAd) {
            activity.getString(R.string.id_test_banner_admob)
        } else {
            idBannerAd
        }

        if (adView.adUnitId.isBlank()) {
            Log.e(tag, "Ad Id is blank!")
            adCallBack.onAdFailed("Ad Id is blank!")
            return
        }

        adView.setAdSize(getAdSize(activity, viewBannerAd.width.toFloat()))

        viewBannerAd.removeAllViews()
        val overlayView = activity.layoutInflater.inflate(R.layout.layout_banner_loading, null, false)
        viewBannerAd.addView(overlayView, 0)
        viewBannerAd.addView(adView, 1)

        AdmobCore.shimmerFrameLayout = overlayView.findViewById(R.id.shimmerBanner)
        AdmobCore.shimmerFrameLayout?.startShimmer()

        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                adView.onPaidEventListener = OnPaidEventListener { adValue ->
                    adCallBack.onAdPaid(
                        adValue,
                        adView.adUnitId,
                        adView.responseInfo?.mediationAdapterClassName ?: "GoogleAdmob"
                    )
                }
                AdmobCore.shimmerFrameLayout?.stopShimmer()
                viewBannerAd.removeView(overlayView)
                adCallBack.onAdLoaded()
                adCallBack.onAdShowed()
                Log.d(tag, "onAdLoaded and showed")
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                AdmobCore.shimmerFrameLayout?.stopShimmer()
                viewBannerAd.removeView(overlayView)
                adCallBack.onAdFailed(adError.message + "\nCause:\n" + adError.cause)
                Log.e(tag, adError.message + "\nCause:\n" + adError.cause)
            }

            override fun onAdClicked() {
                Log.d(tag, "onAdClicked")
                adCallBack.onAdClicked()
            }

            override fun onAdImpression() {}
            override fun onAdClosed() {}
            override fun onAdOpened() {}
        }

        if (AdmobCore.adRequest != null) {
            adView.loadAd(AdmobCore.adRequest!!)
        } else {
            Log.d(tag, "Admob is not init now. Check it before load ad!")
            adCallBack.onAdFailed("Admob is not init now. Check it before load ad!")
        }
    }

    fun loadAndShowBannerCollapsibleAd(
        activity: Activity,
        idBannerCollapAd: String,
        isBottomCollapsible: Boolean,
        viewBanner: ViewGroup,
        adCallBack: AdmobManager.LoadAndShowAdCallBack
    ) {
        val tag = "Load and show BANNER COLLAPSIBLE AD"
        if (!AdmobCore.isEnableAd) {
            adCallBack.onAdFailed("Ads is Disable now!")
            Log.e(tag, "Ads is Disable now!")
            return
        }

        if (!activity.isNetworkConnected()) {
            Log.e(tag, "No Internet!")
            adCallBack.onAdFailed("No internet!")
            return
        }
        val adView = AdView(activity)
        adView.adUnitId = if (AdmobCore.isTestAd) {
            activity.getString(R.string.id_test_collapsible_banner_admob)
        } else {
            idBannerCollapAd
        }
        if (adView.adUnitId.isBlank()) {
            Log.e(tag, "Ad Id is blank!")
            adCallBack.onAdFailed("Ad Id is blank!")
            return
        }
        val adSize = getAdSize(activity, viewBanner.width.toFloat())
        adView.setAdSize(adSize)

        viewBanner.removeAllViews()
        val overlayView = activity.layoutInflater.inflate(R.layout.layout_banner_loading, null, false)
        viewBanner.addView(overlayView, 0)
        viewBanner.addView(adView, 1)

        AdmobCore.shimmerFrameLayout = overlayView.findViewById(R.id.shimmer_view_container)
        AdmobCore.shimmerFrameLayout?.startShimmer()

        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                adView.onPaidEventListener = OnPaidEventListener { adValue ->
                    adCallBack.onAdPaid(
                        adValue,
                        adView.adUnitId,
                        adView.responseInfo?.mediationAdapterClassName ?: "GoogleAdmob"
                    )
                }
                AdmobCore.shimmerFrameLayout?.stopShimmer()
                viewBanner.removeView(overlayView)
                overlayView.destroyDrawingCache()
                adCallBack.onAdLoaded()
                Log.d(tag, "onAdLoaded")

                val params: ViewGroup.LayoutParams = viewBanner.layoutParams
                params.height = adSize.getHeightInPixels(activity)
                viewBanner.layoutParams = params
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                AdmobCore.shimmerFrameLayout?.stopShimmer()
                viewBanner.removeView(overlayView)
                adCallBack.onAdFailed(adError.message + "\nCause\n" + adError.cause)
                Log.e(tag, "onAdFailedToLoad: " + adError.message + "\nCause\n" + adError.cause)
            }

            override fun onAdOpened() {}

            override fun onAdClicked() {
                adCallBack.onAdClicked()
                Log.d(tag, "onAdClicked")
            }

            override fun onAdClosed() {}
        }
        val extras = Bundle()
        val positionCollapsible = if (!isBottomCollapsible) "top" else "bottom"
        extras.putString("collapsible", positionCollapsible)
        val adRequest = AdRequest.Builder()
            .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
            .build()
        adView.loadAd(adRequest)
    }

    private fun getAdSize(activity: Activity, adWidth: Float): AdSize {
        val display = activity.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)
        val density = outMetrics.density
        var adWidthPixels = adWidth
        if (adWidthPixels == 0f) {
            adWidthPixels = outMetrics.widthPixels.toFloat()
        }
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
            activity,
            (adWidthPixels / density).toInt()
        )
    }

    private fun Activity.isNetworkConnected(): Boolean = AdmobCore.run { isNetworkConnected() }
}
