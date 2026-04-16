@file:Suppress("DEPRECATION", "LocalVariableName")

package com.percas.studio.template.admob

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.viewbinding.ViewBinding
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.percas.studio.template.admob.renderer.NativeAdRenderer

@SuppressLint("InflateParams")
object AdmobManager {

    private val testDeviceIds: ArrayList<String> = ArrayList()

    internal var adRequest: AdRequest?
        get() = AdmobCore.adRequest
        set(value) {
            AdmobCore.adRequest = value
        }

    var isEnableAd: Boolean
        get() = AdmobCore.isEnableAd
        set(value) {
            AdmobCore.isEnableAd = value
        }

    var isOverlayAdShowing: Boolean
        get() = AdmobCore.isOverlayAdShowing
        set(value) {
            AdmobCore.isOverlayAdShowing = value
        }

    var isTestAd: Boolean
        get() = AdmobCore.isTestAd
        set(value) {
            AdmobCore.isTestAd = value
        }

    var shimmerFrameLayout
        get() = AdmobCore.shimmerFrameLayout
        set(value) {
            AdmobCore.shimmerFrameLayout = value
        }

    @JvmStatic
    fun initAdmob(context: Context?, timeOut: Int, isTestAd: Boolean, isEnableAd: Boolean) {

        if (timeOut < 5000 && timeOut != 0) {
            Toast.makeText(context, "Limit time ~10000", Toast.LENGTH_LONG).show()
        }
        AdmobCore.updateConfig(timeOut, isTestAd, isEnableAd)

        MobileAds.initialize(context!!) {}

        initTestDeviceIds()

        val configuration = RequestConfiguration.Builder()
            .setTestDeviceIds(testDeviceIds)
            .build()
        MobileAds.setRequestConfiguration(configuration)

        initAdRequest(timeOut)
    }

    @JvmStatic
    fun initAdRequest(timeOut: Int) {
        AdmobCore.initAdRequest(timeOut)
    }

    internal fun getTimeout(): Int = AdmobCore.getTimeout()

    private fun initTestDeviceIds() {
        testDeviceIds.add("d7e28f987358016e")
    }

    @JvmStatic
    fun Context.isNetworkConnected(): Boolean {
        return AdmobCore.run { isNetworkConnected() }
    }


    @JvmStatic
    fun loadAndShowBannerAd(
        activity: Activity,
        idBannerAd: String,
        viewBannerAd: ViewGroup,
        adCallBack: LoadAndShowAdCallBack
    ) {
        BannerAdManager.loadAndShowBannerAd(activity, idBannerAd, viewBannerAd, adCallBack)
    }

    fun loadAndShowBannerCollapsibleAd(
        activity: Activity,
        idBannerCollapAd: String,
        isBottomCollapsible: Boolean,
        viewBanner: ViewGroup,
        adCallBack: LoadAndShowAdCallBack
    ) {
        BannerAdManager.loadAndShowBannerCollapsibleAd(
            activity,
            idBannerCollapAd,
            isBottomCollapsible,
            viewBanner,
            adCallBack
        )
    }

    @JvmStatic
    fun loadNativeAd(
        context: Context,
        idAd: String,
        adCallBack: LoadAdCallBack
    ) {
        NativeAdManager.loadNativeAd(context, idAd, adCallBack)
    }

    @JvmStatic
    fun <T : ViewBinding> showNativeAd(
        activity: Activity,
        idAd: String,
        viewNativeAd: ViewGroup,
        renderer: NativeAdRenderer<T>,
        adCallBack: ShowAdCallBack
    ) {
        NativeAdManager.showNativeAd(activity, idAd, viewNativeAd, renderer, adCallBack)
    }


    @JvmStatic
    fun <T : ViewBinding> loadAndShowNativeAd(
        activity: Activity,
        idAd: String,
        viewNativeAd: ViewGroup,
        renderer: NativeAdRenderer<T>,
        adCallBack: LoadAndShowAdCallBack
    ) {
        NativeAdManager.loadAndShowNativeAd(activity, idAd, viewNativeAd, renderer, adCallBack)
    }

    fun <T : ViewBinding> loadAndShowNativeAdFullScreen(
        activity: Activity,
        idNativeAd: String,
        viewNativeAd: ViewGroup,
        renderer: NativeAdRenderer<T>,
        mediaAspectRatio: Int,
        adCallBack: LoadAndShowAdCallBack
    ) {
        NativeAdManager.loadAndShowNativeAdFullScreen(
            activity,
            idNativeAd,
            viewNativeAd,
            renderer,
            mediaAspectRatio,
            adCallBack
        )
    }

    fun loadNativeAdFullScreen(
        context: Context,
        idAd: String,
        mediaAspectRatio: Int,
        adCallBack: LoadAdCallBack
    ) {
        NativeAdManager.loadNativeAdFullScreen(context, idAd, mediaAspectRatio, adCallBack)
    }

    @JvmStatic
    fun <T : ViewBinding> showNativeAdFullScreen(
        activity: Activity,
        idAd: String,
        viewNativeAd: ViewGroup,
        renderer: NativeAdRenderer<T>,
        adCallBack: ShowAdCallBack

    ) {
        NativeAdManager.showNativeAdFullScreen(activity, idAd, viewNativeAd, renderer, adCallBack)
    }


    @JvmStatic
    fun loadInterstitialAd(
        activity: Context,
        idAd: String,
        adLoadCallback: LoadAdCallBack
    ) {
        InterstitialAdManager.loadInterstitialAd(activity, idAd, adLoadCallback)
    }

    @JvmStatic
    fun showInterstitialAd(
        activity: Activity,
        idAd: String,
        adCallback: ShowAdCallBack,
    ) {
        InterstitialAdManager.showInterstitialAd(activity, idAd, adCallback)
    }

    fun loadAndShowInterstitialAd(
        activity: Activity,
        idAd: String,
        adCallback: LoadAndShowAdCallBack
    ) {
        InterstitialAdManager.loadAndShowInterstitialAd(activity, idAd, adCallback)
    }

    fun loadAndShowInterstitialAdWithoutLoadingScreen(
        activity: Activity,
        idAd: String,
        adCallback: LoadAndShowAdCallBack
    ) {
        InterstitialAdManager.loadAndShowInterstitialAdWithoutLoadingScreen(activity, idAd, adCallback)
    }

    fun dismissAdDialog() {
        AdmobCore.dismissAdDialog()
    }

    internal fun dialogLoading(context: Activity) {
        AdmobCore.dialogLoading(context)
    }

    fun loadAndShowRewardAd(
        activity: Activity,
        admobId: String,
        adCallback: LoadAndShowRewardAdCallBack
    ) {
        RewardAdManager.loadAndShowRewardAd(activity, admobId, adCallback)
    }

    @JvmStatic
    fun loadInterReward(
        context: Context,
        idAd: String,
        adLoadCallback: LoadAdCallBack
    ) {
        RewardAdManager.loadInterReward(context, idAd, adLoadCallback)
    }


    @JvmStatic
    fun showInterReward(
        activity: Activity,
        idAd: String,
        adCallback: ShowRewardAdCallBack
    ) {
        RewardAdManager.showInterReward(activity, idAd, adCallback)
    }

    interface LoadAdCallBack {
        fun onAdLoaded()
        fun onAdFailed(error: String)
        fun onAdClicked()
        fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String)
    }

    interface ShowAdCallBack {
        fun onAdShowed()
        fun onAdFailed(error: String)
        fun onAdClosed()
        fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String)
    }

    interface LoadAndShowAdCallBack {
        fun onAdLoaded()
        fun onAdShowed()
        fun onAdFailed(error: String)
        fun onAdClosed()
        fun onAdClicked()
        fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String)
    }

    interface ShowRewardAdCallBack {
        fun onAdShowed()
        fun onAdClosed()
        fun onAdEarned()
        fun onAdFailed(error: String)
        fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String)
    }

    interface LoadAndShowRewardAdCallBack {
        fun onAdLoaded()
        fun onAdShowed()
        fun onAdFailed(error: String)
        fun onAdClosed()
        fun onAdEarned()
        fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String)
    }
}
