@file:Suppress("DEPRECATION")

package com.percas.studio.template.admob

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.percas.studio.template.R
import com.percas.studio.template.admob.renderer.NativeAdLoadingStyle
import com.percas.studio.template.admob.renderer.NativeAdRenderer
import com.percas.studio.template.model.NativeAdHolder

internal object NativeAdManager {

    fun loadNativeAd(
        context: Context,
        nativeHolder: NativeAdHolder,
        adCallBack: AdmobManager.LoadAdCallBack
    ) {
        val tag = "Load NATIVE AD"
        if (!AdmobManager.isEnableAd) {
            adCallBack.onAdFailed("Ads is Disable now!")
            Log.e(tag, "Ads is Disable now!")
            return
        }
        if (!context.isNetworkConnected()) {
            adCallBack.onAdFailed("No internet!")
            Log.e(tag, "No Internet!")
            return
        }

        if (nativeHolder.nativeAd != null) {
            adCallBack.onAdFailed("This Native ad is not empty. Don't need to load again!")
            Log.e(tag, "This Native ad is not empty. Don't need to load again!")
            return
        }

        if (AdmobManager.isTestAd) {
            nativeHolder.ads = context.getString(R.string.id_test_native_admob)
        }
        if (nativeHolder.ads.isBlank() && !AdmobManager.isTestAd) {
            Log.e(tag, "Ad Id is blank!")
            adCallBack.onAdFailed("Ad Id is blank!")
            return
        }
        nativeHolder.isLoading = true

        VideoOptions.Builder().setStartMuted(false).build()

        val adLoader: AdLoader = AdLoader.Builder(context, nativeHolder.ads)
            .forNativeAd { nativeAd ->
                nativeHolder.nativeAd = nativeAd
                nativeHolder.isLoading = false
                nativeHolder.native_mutable.value = nativeAd
                nativeAd.setOnPaidEventListener { adValue: AdValue? ->
                    adValue?.let {
                        adCallBack.onAdPaid(
                            it,
                            nativeHolder.ads,
                            nativeAd.responseInfo?.mediationAdapterClassName ?: "GoogleAdmob"
                        )
                    }
                }
                adCallBack.onAdLoaded()
                Log.d(tag, "onAdLoaded")
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    nativeHolder.nativeAd = null
                    nativeHolder.isLoading = false
                    nativeHolder.native_mutable.value = null
                    adCallBack.onAdFailed(adError.message + "\nCause\n" + adError.cause)
                    Log.e(tag, "onAdFailedToLoad: " + adError.message + "\nCause\n" + adError.cause)
                }

                override fun onAdClicked() {
                    Log.d(tag, "onAdClicked")
                    adCallBack.onAdClicked()
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()

        if (AdmobManager.adRequest != null) {
            adLoader.loadAd(AdmobManager.adRequest!!)
        } else {
            adCallBack.onAdFailed("Admob is not init now. Check it before load ad!")
            Log.e(tag, "Admob is not init now. Check it before load ad!")
        }
    }

    fun <T : ViewBinding> showNativeAd(
        activity: Activity,
        nativeHolder: NativeAdHolder,
        viewNativeAd: ViewGroup,
        renderer: NativeAdRenderer<T>,
        adCallBack: AdmobManager.ShowAdCallBack
    ) {
        val tag = "Show NATIVE AD"
        if (!AdmobManager.isEnableAd) {
            adCallBack.onAdFailed("Ads is DISABLE now!")
            Log.e(tag, "Ads is DISABLE now!")
            return
        }
        if (!activity.isNetworkConnected()) {
            adCallBack.onAdFailed("No Internet!")
            Log.e(tag, "No internet!")
            return
        }

        if (nativeHolder.ads.isBlank() && !AdmobManager.isTestAd) {
            Log.e(tag, "Ad Id is blank!")
            adCallBack.onAdFailed("Ad Id is blank!")
            return
        }

        AdmobManager.shimmerFrameLayout?.stopShimmer()
        viewNativeAd.removeAllViews()

        if (!nativeHolder.isLoading) {
            if (nativeHolder.nativeAd != null) {
                val binding = renderer.inflate(activity.layoutInflater, viewNativeAd)
                renderer.bind(binding, nativeHolder.nativeAd!!)
                val adView = renderer.root(binding)
                AdmobManager.shimmerFrameLayout?.stopShimmer()
                nativeHolder.native_mutable.removeObservers(activity as LifecycleOwner)
                viewNativeAd.removeAllViews()
                viewNativeAd.addView(adView)
                adCallBack.onAdShowed()
                Log.d(tag, "Ad Showed")
            } else {
                AdmobManager.shimmerFrameLayout?.stopShimmer()
                nativeHolder.native_mutable.removeObservers(activity as LifecycleOwner)
                adCallBack.onAdFailed("Native is not loaded!")
                Log.e(tag, "Native is not loaded!")
            }
            return
        }

        val overlayLoading = createNativeLoadingView(activity, renderer.loadingStyle)
        viewNativeAd.addView(overlayLoading, 0)

        if (AdmobManager.shimmerFrameLayout == null) {
            AdmobManager.shimmerFrameLayout = overlayLoading.findViewById(R.id.shimmer_view_container)
        }

        AdmobManager.shimmerFrameLayout?.startShimmer()
        nativeHolder.native_mutable.observe(activity as LifecycleOwner) { nativeAd: NativeAd? ->
            if (nativeAd != null) {
                nativeAd.setOnPaidEventListener {
                    adCallBack.onAdPaid(
                        it,
                        nativeHolder.ads,
                        nativeAd.responseInfo?.mediationAdapterClassName ?: "GoogleAdmob"
                    )
                }
                val binding = renderer.inflate(activity.layoutInflater, viewNativeAd)
                renderer.bind(binding, nativeAd)
                val adView = renderer.root(binding)
                AdmobManager.shimmerFrameLayout?.stopShimmer()
                viewNativeAd.removeAllViews()
                viewNativeAd.addView(adView)
                adCallBack.onAdShowed()
                Log.d(tag, "Ad Showed")
                nativeHolder.native_mutable.removeObservers(activity)
            } else {
                AdmobManager.shimmerFrameLayout?.stopShimmer()
                adCallBack.onAdFailed("Load native Ad before show it or use LoadAndShowNativeAd")
                Log.e(tag, "Load native Ad before show it or use LoadAndShowNativeAd!")
                nativeHolder.native_mutable.removeObservers(activity)
            }
        }
    }

    fun <T : ViewBinding> loadAndShowNativeAd(
        activity: Activity,
        nativeHolder: NativeAdHolder,
        viewNativeAd: ViewGroup,
        renderer: NativeAdRenderer<T>,
        adCallBack: AdmobManager.LoadAndShowAdCallBack
    ) {
        val tag = "Load and show NATIVE AD"
        if (!AdmobManager.isEnableAd) {
            adCallBack.onAdFailed("Ads is DISABLE now")
            Log.e(tag, "Ads is DISABLE now!")
            return
        }
        if (!activity.isNetworkConnected()) {
            adCallBack.onAdFailed("No Internet")
            Log.e(tag, "No Internet!")
            return
        }

        viewNativeAd.removeAllViews()

        if (AdmobManager.isTestAd) {
            nativeHolder.ads = activity.getString(R.string.id_test_native_admob)
        }

        if (nativeHolder.ads.isBlank() && !AdmobManager.isTestAd) {
            Log.e(tag, "Ad Id is blank!")
            adCallBack.onAdFailed("Ad Id is blank!")
            return
        }

        val tagView = createNativeLoadingView(activity, renderer.loadingStyle)
        viewNativeAd.addView(tagView, 0)

        if (AdmobManager.shimmerFrameLayout == null) {
            AdmobManager.shimmerFrameLayout = tagView.findViewById(R.id.shimmer_view_container)
        }

        AdmobManager.shimmerFrameLayout?.startShimmer()

        val adLoader = AdLoader.Builder(activity, nativeHolder.ads)
            .forNativeAd { nativeAd ->
                adCallBack.onAdLoaded()
                Log.d(tag, "Ad Loaded")
                val binding = renderer.inflate(activity.layoutInflater, viewNativeAd)
                renderer.bind(binding, nativeAd)
                val adView = renderer.root(binding)
                AdmobManager.shimmerFrameLayout?.stopShimmer()
                viewNativeAd.removeAllViews()
                viewNativeAd.addView(adView)
                adCallBack.onAdShowed()
                Log.d(tag, "Ad Showed")
                nativeAd.setOnPaidEventListener { adValue: AdValue ->
                    adCallBack.onAdPaid(
                        adValue,
                        nativeHolder.ads,
                        nativeAd.responseInfo?.mediationAdapterClassName ?: "GoogleAdmob"
                    )
                }
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    AdmobManager.shimmerFrameLayout?.stopShimmer()
                    viewNativeAd.removeAllViews()
                    nativeHolder.isLoading = false
                    adCallBack.onAdFailed(adError.message + "\nError Code Ads:\n" + adError.cause)
                    Log.e(tag, adError.message + "\nError Code Ads:\n" + adError.cause)
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    adCallBack.onAdClicked()
                    Log.d(tag, "onAdClicked")
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()

        if (AdmobManager.adRequest != null) {
            adLoader.loadAd(AdmobManager.adRequest!!)
        } else {
            adCallBack.onAdFailed("Admob is not init now. Check it before load ads!")
            Log.e(tag, "Admob is not init now. Check it before load ads!")
        }
    }

    fun <T : ViewBinding> loadAndShowNativeAdFullScreen(
        activity: Activity,
        idNativeAd: String,
        viewNativeAd: ViewGroup,
        renderer: NativeAdRenderer<T>,
        mediaAspectRatio: Int,
        adCallBack: AdmobManager.LoadAndShowAdCallBack
    ) {
        val tag = "Load and show NATIVE FULL SCREEN"
        if (!AdmobManager.isEnableAd) {
            adCallBack.onAdFailed("Ads is DISABLE now!")
            Log.d(tag, "Ads is DISABLE now!")
            return
        }
        if (!activity.isNetworkConnected()) {
            adCallBack.onAdFailed("No Internet!")
            Log.d(tag, "No internet!")
            return
        }

        val adMobId = if (AdmobManager.isTestAd) {
            activity.getString(R.string.id_test_native_admob_fullscrren)
        } else {
            idNativeAd
        }
        if (adMobId.isBlank() && !AdmobManager.isTestAd) {
            Log.e(tag, "Ad Id is blank!")
            adCallBack.onAdFailed("Ad Id is blank!")
            return
        }

        viewNativeAd.removeAllViews()
        val tagView = createNativeLoadingView(activity, renderer.loadingStyle)
        viewNativeAd.addView(tagView, 0)
        AdmobManager.shimmerFrameLayout = tagView.findViewById(R.id.shimmer_view_container)
        AdmobManager.shimmerFrameLayout?.startShimmer()

        val builder = AdLoader.Builder(activity, adMobId)
        val videoOptions =
            VideoOptions.Builder().setStartMuted(false).setCustomControlsRequested(false).build()

        val adOptions = NativeAdOptions.Builder()
            .setMediaAspectRatio(mediaAspectRatio)
            .setVideoOptions(videoOptions)
            .build()

        builder.withNativeAdOptions(adOptions)
        builder.forNativeAd { nativeAd ->
            nativeAd.setOnPaidEventListener { adValue: AdValue? ->
                adValue?.let {
                    adCallBack.onAdPaid(
                        adValue,
                        adMobId,
                        nativeAd.responseInfo?.mediationAdapterClassName ?: "GoogleAdmob"
                    )
                }
            }
            val binding = renderer.inflate(activity.layoutInflater, viewNativeAd)
            renderer.bind(binding, nativeAd)
            val adView = renderer.root(binding)
            viewNativeAd.removeAllViews()
            AdmobManager.shimmerFrameLayout?.stopShimmer()
            viewNativeAd.addView(adView)
            adCallBack.onAdShowed()
            Log.d(tag, "onAdShowed")
        }
        builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                AdmobManager.shimmerFrameLayout?.stopShimmer()
                adCallBack.onAdFailed(loadAdError.message + "\nCause\n" + loadAdError.cause)
                Log.e(tag, loadAdError.message + "\nCause\n" + loadAdError.cause)
            }
        })
        if (AdmobManager.adRequest != null) {
            builder.build().loadAd(AdmobManager.adRequest!!)
        } else {
            adCallBack.onAdFailed("Admob is not init now. Check it before load ads!")
            Log.e(tag, "Admob is not init now. Check it before load ads!")
        }
    }

    fun loadNativeAdFullScreen(
        context: Context,
        nativeHolder: NativeAdHolder,
        mediaAspectRatio: Int,
        adCallBack: AdmobManager.LoadAdCallBack
    ) {
        val tag = "Load NATIVE AD FULL SCREEN"
        if (!AdmobManager.isEnableAd) {
            adCallBack.onAdFailed("Ads is DISABLE now!")
            Log.e(tag, "Ads is DISABLE now!")
            return
        }
        if (!context.isNetworkConnected()) {
            adCallBack.onAdFailed("No Internet!")
            Log.e(tag, "No Internet!")
            return
        }
        if (nativeHolder.nativeAd != null) {
            adCallBack.onAdFailed("This Native ads is not empty. Don't need to load again!")
            Log.e(tag, "This Native ads is not empty. Don't need to load again!")
            return
        }

        if (AdmobManager.isTestAd) {
            nativeHolder.ads = context.getString(R.string.id_test_native_admob_fullscrren)
        }

        if (nativeHolder.ads.isBlank() && !AdmobManager.isTestAd) {
            Log.e(tag, "Ad Id is blank!")
            adCallBack.onAdFailed("Ad Id is blank!")
            return
        }

        nativeHolder.isLoading = true
        val videoOptions =
            VideoOptions.Builder().setStartMuted(false).setCustomControlsRequested(true).build()
        val adOptions = NativeAdOptions.Builder()
            .setMediaAspectRatio(mediaAspectRatio)
            .setVideoOptions(videoOptions)
            .build()
        val adLoader = AdLoader.Builder(context, nativeHolder.ads)
        adLoader.withNativeAdOptions(adOptions)
        adLoader.forNativeAd { nativeAd ->
            nativeHolder.nativeAd = nativeAd
            nativeHolder.isLoading = false
            nativeHolder.native_mutable.value = nativeAd
            nativeAd.setOnPaidEventListener { adValue: AdValue? ->
                adValue?.let {
                    adCallBack.onAdPaid(
                        it,
                        nativeHolder.ads,
                        nativeAd.responseInfo?.mediationAdapterClassName ?: "GoogleAdmob"
                    )
                }
            }
            adCallBack.onAdLoaded()
            Log.d(tag, "onAdLoaded")
        }
        adLoader.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                nativeHolder.nativeAd = null
                nativeHolder.isLoading = false
                nativeHolder.native_mutable.value = null
                adCallBack.onAdFailed(adError.message + "\nCause\n" + adError.cause)
                Log.e(tag, adError.message + "\nCause\n" + adError.cause)
            }

            override fun onAdClicked() {
                Log.d(tag, "onAdClicked")
                adCallBack.onAdClicked()
            }
        })
        if (AdmobManager.adRequest != null) {
            adLoader.build().loadAd(AdmobManager.adRequest!!)
        } else {
            adCallBack.onAdFailed("Admob is not init now. Check it before load ads!")
            Log.e(tag, "Admob is not init now. Check it before load ads!")
        }
    }

    fun <T : ViewBinding> showNativeAdFullScreen(
        activity: Activity,
        nativeHolder: NativeAdHolder,
        viewNativeAd: ViewGroup,
        renderer: NativeAdRenderer<T>,
        adCallBack: AdmobManager.ShowAdCallBack
    ) {
        val tag = "Show NATIVE AD FULL SCREEN"
        if (!AdmobManager.isEnableAd) {
            adCallBack.onAdFailed("Ads is DISABLE now!")
            Log.e(tag, "Ads is DISABLE now!")
            return
        }
        if (!activity.isNetworkConnected()) {
            adCallBack.onAdFailed("No Internet!")
            Log.e(tag, "No Internet!")
            return
        }

        if (nativeHolder.ads.isBlank() && !AdmobManager.isTestAd) {
            Log.e(tag, "Ad Id is blank!")
            adCallBack.onAdFailed("Ad Id is blank!")
            return
        }

        AdmobManager.shimmerFrameLayout?.stopShimmer()
        viewNativeAd.removeAllViews()

        if (!nativeHolder.isLoading) {
            if (nativeHolder.nativeAd != null) {
                val binding = renderer.inflate(activity.layoutInflater, viewNativeAd)
                renderer.bind(binding, nativeHolder.nativeAd!!)
                val adView = renderer.root(binding)
                AdmobManager.shimmerFrameLayout?.stopShimmer()
                nativeHolder.native_mutable.removeObservers(activity as LifecycleOwner)
                viewNativeAd.removeAllViews()
                viewNativeAd.addView(adView)
                adCallBack.onAdShowed()
                Log.d(tag, "onAdShowed")
            } else {
                AdmobManager.shimmerFrameLayout?.stopShimmer()
                nativeHolder.native_mutable.removeObservers(activity as LifecycleOwner)
                adCallBack.onAdFailed("Load native Ad before show it or use LoadAndShowNativeAd!")
                Log.e(tag, "Load native Ad before show it or use LoadAndShowNativeAd!")
            }
            return
        }

        val overlayLoading = createNativeLoadingView(activity, renderer.loadingStyle)
        viewNativeAd.addView(overlayLoading, 0)

        if (AdmobManager.shimmerFrameLayout == null) {
            AdmobManager.shimmerFrameLayout = overlayLoading.findViewById(R.id.shimmer_view_container)
        }
        AdmobManager.shimmerFrameLayout?.startShimmer()

        nativeHolder.native_mutable.observe(activity as LifecycleOwner) { nativeAd: NativeAd? ->
            if (nativeAd != null) {
                nativeAd.setOnPaidEventListener {
                    adCallBack.onAdPaid(
                        it,
                        nativeHolder.ads,
                        nativeAd.responseInfo?.mediationAdapterClassName ?: "GoogleAdmob"
                    )
                }
                val binding = renderer.inflate(activity.layoutInflater, viewNativeAd)
                renderer.bind(binding, nativeHolder.nativeAd!!)
                val adView = renderer.root(binding)
                AdmobManager.shimmerFrameLayout?.stopShimmer()
                viewNativeAd.removeAllViews()
                viewNativeAd.addView(adView)
                adCallBack.onAdShowed()
                Log.d(tag, "onAdShowed")
                nativeHolder.native_mutable.removeObservers(activity)
            } else {
                AdmobManager.shimmerFrameLayout?.stopShimmer()
                adCallBack.onAdFailed("Load native Ad before show it or use LoadAndShowNativeAd!")
                Log.e(tag, "Load native Ad before show it or use LoadAndShowNativeAd")
                nativeHolder.native_mutable.removeObservers(activity)
            }
        }
    }

    private fun createNativeLoadingView(
        activity: Activity,
        loadingStyle: NativeAdLoadingStyle
    ): View {
        val loadingLayout = when (loadingStyle) {
            NativeAdLoadingStyle.SMALL -> R.layout.layoutnative_loading_small
            NativeAdLoadingStyle.MEDIUM -> R.layout.layoutnative_loading_medium
            NativeAdLoadingStyle.FULLSCREEN -> R.layout.layoutnative_loading_fullscreen
        }
        return activity.layoutInflater.inflate(loadingLayout, null, false)
    }

    private fun Context.isNetworkConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        return cm?.activeNetworkInfo?.isConnected == true
    }
}
