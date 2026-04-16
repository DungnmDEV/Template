@file:Suppress("DEPRECATION")

package com.percas.studio.template.admob

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
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

internal object NativeAdManager {

    private data class NativeAdState(
        var nativeAd: NativeAd? = null,
        var isLoading: Boolean = false,
        val liveData: MutableLiveData<NativeAd?> = MutableLiveData(null),
    )

    private val nativeAds = mutableMapOf<String, NativeAdState>()
    private val fullscreenNativeAds = mutableMapOf<String, NativeAdState>()

    fun loadNativeAd(
        context: Context,
        idAd: String,
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

        val resolvedId = resolveNativeAdId(context, idAd, isFullscreen = false)
        if (resolvedId == null) {
            Log.e(tag, "Ad Id is blank!")
            adCallBack.onAdFailed("Ad Id is blank!")
            return
        }

        val state = nativeAds.getOrPut(resolvedId) { NativeAdState() }
        if (state.nativeAd != null) {
            adCallBack.onAdFailed("This Native ad is not empty. Don't need to load again!")
            Log.e(tag, "This Native ad is not empty. Don't need to load again!")
            return
        }
        if (state.isLoading) {
            adCallBack.onAdFailed("Native ad is loading!")
            Log.e(tag, "Native ad is loading!")
            return
        }

        state.isLoading = true
        VideoOptions.Builder().setStartMuted(false).build()

        val adLoader = AdLoader.Builder(context, resolvedId)
            .forNativeAd { nativeAd ->
                state.nativeAd = nativeAd
                state.isLoading = false
                state.liveData.value = nativeAd
                nativeAd.setOnPaidEventListener { adValue: AdValue? ->
                    adValue?.let {
                        adCallBack.onAdPaid(
                            it,
                            resolvedId,
                            nativeAd.responseInfo?.mediationAdapterClassName ?: "GoogleAdmob"
                        )
                    }
                }
                adCallBack.onAdLoaded()
                Log.d(tag, "onAdLoaded")
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    state.nativeAd = null
                    state.isLoading = false
                    state.liveData.value = null
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
            state.isLoading = false
            adCallBack.onAdFailed("Admob is not init now. Check it before load ad!")
            Log.e(tag, "Admob is not init now. Check it before load ad!")
        }
    }

    fun <T : ViewBinding> showNativeAd(
        activity: Activity,
        idAd: String,
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

        val resolvedId = resolveNativeAdId(activity, idAd, isFullscreen = false)
        if (resolvedId == null) {
            Log.e(tag, "Ad Id is blank!")
            adCallBack.onAdFailed("Ad Id is blank!")
            return
        }

        val state = nativeAds.getOrPut(resolvedId) { NativeAdState() }
        AdmobManager.shimmerFrameLayout?.stopShimmer()
        viewNativeAd.removeAllViews()

        if (!state.isLoading) {
            if (state.nativeAd != null) {
                renderNativeAd(activity, viewNativeAd, renderer, state.nativeAd!!)
                state.liveData.removeObservers(activity as LifecycleOwner)
                adCallBack.onAdShowed()
                Log.d(tag, "Ad Showed")
            } else {
                state.liveData.removeObservers(activity as LifecycleOwner)
                adCallBack.onAdFailed("Native is not loaded!")
                Log.e(tag, "Native is not loaded!")
            }
            return
        }

        observeLoadingNativeAd(activity, viewNativeAd, renderer, state, tag, adCallBack)
    }

    fun <T : ViewBinding> loadAndShowNativeAd(
        activity: Activity,
        idAd: String,
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

        val resolvedId = resolveNativeAdId(activity, idAd, isFullscreen = false)
        if (resolvedId == null) {
            Log.e(tag, "Ad Id is blank!")
            adCallBack.onAdFailed("Ad Id is blank!")
            return
        }

        viewNativeAd.removeAllViews()
        val tagView = createNativeLoadingView(activity, renderer.loadingStyle)
        viewNativeAd.addView(tagView, 0)

        if (AdmobManager.shimmerFrameLayout == null) {
            AdmobManager.shimmerFrameLayout = tagView.findViewById(R.id.shimmer_view_container)
        }
        AdmobManager.shimmerFrameLayout?.startShimmer()

        val adLoader = AdLoader.Builder(activity, resolvedId)
            .forNativeAd { nativeAd ->
                adCallBack.onAdLoaded()
                Log.d(tag, "Ad Loaded")
                renderNativeAd(activity, viewNativeAd, renderer, nativeAd)
                adCallBack.onAdShowed()
                Log.d(tag, "Ad Showed")
                nativeAd.setOnPaidEventListener { adValue: AdValue ->
                    adCallBack.onAdPaid(
                        adValue,
                        resolvedId,
                        nativeAd.responseInfo?.mediationAdapterClassName ?: "GoogleAdmob"
                    )
                }
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    AdmobManager.shimmerFrameLayout?.stopShimmer()
                    viewNativeAd.removeAllViews()
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

        val resolvedId = resolveNativeAdId(activity, idNativeAd, isFullscreen = true)
        if (resolvedId == null) {
            Log.e(tag, "Ad Id is blank!")
            adCallBack.onAdFailed("Ad Id is blank!")
            return
        }

        viewNativeAd.removeAllViews()
        val tagView = createNativeLoadingView(activity, renderer.loadingStyle)
        viewNativeAd.addView(tagView, 0)
        AdmobManager.shimmerFrameLayout = tagView.findViewById(R.id.shimmer_view_container)
        AdmobManager.shimmerFrameLayout?.startShimmer()

        val builder = AdLoader.Builder(activity, resolvedId)
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
                        resolvedId,
                        nativeAd.responseInfo?.mediationAdapterClassName ?: "GoogleAdmob"
                    )
                }
            }
            renderNativeAd(activity, viewNativeAd, renderer, nativeAd)
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
        idAd: String,
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

        val resolvedId = resolveNativeAdId(context, idAd, isFullscreen = true)
        if (resolvedId == null) {
            Log.e(tag, "Ad Id is blank!")
            adCallBack.onAdFailed("Ad Id is blank!")
            return
        }

        val state = fullscreenNativeAds.getOrPut(resolvedId) { NativeAdState() }
        if (state.nativeAd != null) {
            adCallBack.onAdFailed("This Native ads is not empty. Don't need to load again!")
            Log.e(tag, "This Native ads is not empty. Don't need to load again!")
            return
        }
        if (state.isLoading) {
            adCallBack.onAdFailed("Native ad is loading!")
            Log.e(tag, "Native ad is loading!")
            return
        }

        state.isLoading = true
        val videoOptions =
            VideoOptions.Builder().setStartMuted(false).setCustomControlsRequested(true).build()
        val adOptions = NativeAdOptions.Builder()
            .setMediaAspectRatio(mediaAspectRatio)
            .setVideoOptions(videoOptions)
            .build()
        val adLoader = AdLoader.Builder(context, resolvedId)
        adLoader.withNativeAdOptions(adOptions)
        adLoader.forNativeAd { nativeAd ->
            state.nativeAd = nativeAd
            state.isLoading = false
            state.liveData.value = nativeAd
            nativeAd.setOnPaidEventListener { adValue: AdValue? ->
                adValue?.let {
                    adCallBack.onAdPaid(
                        it,
                        resolvedId,
                        nativeAd.responseInfo?.mediationAdapterClassName ?: "GoogleAdmob"
                    )
                }
            }
            adCallBack.onAdLoaded()
            Log.d(tag, "onAdLoaded")
        }
        adLoader.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                state.nativeAd = null
                state.isLoading = false
                state.liveData.value = null
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
            state.isLoading = false
            adCallBack.onAdFailed("Admob is not init now. Check it before load ads!")
            Log.e(tag, "Admob is not init now. Check it before load ads!")
        }
    }

    fun <T : ViewBinding> showNativeAdFullScreen(
        activity: Activity,
        idAd: String,
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

        val resolvedId = resolveNativeAdId(activity, idAd, isFullscreen = true)
        if (resolvedId == null) {
            Log.e(tag, "Ad Id is blank!")
            adCallBack.onAdFailed("Ad Id is blank!")
            return
        }

        val state = fullscreenNativeAds.getOrPut(resolvedId) { NativeAdState() }
        AdmobManager.shimmerFrameLayout?.stopShimmer()
        viewNativeAd.removeAllViews()

        if (!state.isLoading) {
            if (state.nativeAd != null) {
                renderNativeAd(activity, viewNativeAd, renderer, state.nativeAd!!)
                state.liveData.removeObservers(activity as LifecycleOwner)
                adCallBack.onAdShowed()
                Log.d(tag, "onAdShowed")
            } else {
                state.liveData.removeObservers(activity as LifecycleOwner)
                adCallBack.onAdFailed("Load native Ad before show it or use LoadAndShowNativeAd!")
                Log.e(tag, "Load native Ad before show it or use LoadAndShowNativeAd!")
            }
            return
        }

        observeLoadingNativeAd(activity, viewNativeAd, renderer, state, tag, adCallBack)
    }

    private fun <T : ViewBinding> observeLoadingNativeAd(
        activity: Activity,
        viewNativeAd: ViewGroup,
        renderer: NativeAdRenderer<T>,
        state: NativeAdState,
        tag: String,
        adCallBack: AdmobManager.ShowAdCallBack
    ) {
        val overlayLoading = createNativeLoadingView(activity, renderer.loadingStyle)
        viewNativeAd.addView(overlayLoading, 0)

        if (AdmobManager.shimmerFrameLayout == null) {
            AdmobManager.shimmerFrameLayout = overlayLoading.findViewById(R.id.shimmer_view_container)
        }

        AdmobManager.shimmerFrameLayout?.startShimmer()
        state.liveData.observe(activity as LifecycleOwner) { nativeAd: NativeAd? ->
            if (nativeAd != null) {
                nativeAd.setOnPaidEventListener {
                    adCallBack.onAdPaid(
                        it,
                        nativeAd.responseInfo?.loadedAdapterResponseInfo?.adSourceName ?: "NativeAd",
                        nativeAd.responseInfo?.mediationAdapterClassName ?: "GoogleAdmob"
                    )
                }
                renderNativeAd(activity, viewNativeAd, renderer, nativeAd)
                adCallBack.onAdShowed()
                Log.d(tag, "Ad Showed")
                state.liveData.removeObservers(activity as LifecycleOwner)
            } else {
                AdmobManager.shimmerFrameLayout?.stopShimmer()
                adCallBack.onAdFailed("Load native Ad before show it or use LoadAndShowNativeAd")
                Log.e(tag, "Load native Ad before show it or use LoadAndShowNativeAd!")
                state.liveData.removeObservers(activity as LifecycleOwner)
            }
        }
    }

    private fun <T : ViewBinding> renderNativeAd(
        activity: Activity,
        viewNativeAd: ViewGroup,
        renderer: NativeAdRenderer<T>,
        nativeAd: NativeAd
    ) {
        val binding = renderer.inflate(activity.layoutInflater, viewNativeAd)
        renderer.bind(binding, nativeAd)
        val adView = renderer.root(binding)
        AdmobManager.shimmerFrameLayout?.stopShimmer()
        viewNativeAd.removeAllViews()
        viewNativeAd.addView(adView)
    }

    private fun resolveNativeAdId(
        context: Context,
        requestedId: String,
        isFullscreen: Boolean
    ): String? {
        val adId = if (AdmobManager.isTestAd) {
            if (isFullscreen) {
                context.getString(R.string.id_test_native_admob_fullscrren)
            } else {
                context.getString(R.string.id_test_native_admob)
            }
        } else {
            requestedId
        }
        return adId.takeIf { it.isNotBlank() }
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
