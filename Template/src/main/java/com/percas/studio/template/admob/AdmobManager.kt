@file:Suppress("DEPRECATION", "LocalVariableName")

package com.percas.studio.template.admob

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.viewbinding.ViewBinding
import com.airbnb.lottie.LottieAnimationView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.percas.studio.template.R
import com.percas.studio.template.admob.renderer.NativeAdRenderer

@SuppressLint("InflateParams")
object AdmobManager {

    var isEnableAd = false
    var isOverlayAdShowing = false

    private var timeOut = 10000

    internal var adRequest: AdRequest? = null

    private val testDeviceIds: ArrayList<String> = ArrayList()

    var shimmerFrameLayout: ShimmerFrameLayout? = null

    private var dialogFullScreen: Dialog? = null

    var isTestAd = true

    @JvmStatic
    fun initAdmob(context: Context?, timeOut: Int, isTestAd: Boolean, isEnableAd: Boolean) {

        if (timeOut < 5000 && timeOut != 0) {
            Toast.makeText(context, "Limit time ~10000", Toast.LENGTH_LONG).show()
        }
        AdmobManager.timeOut = if (timeOut > 0) {
            timeOut
        } else {
            10000
        }
        AdmobManager.isEnableAd = isEnableAd

        AdmobManager.isTestAd = isTestAd

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
        adRequest = AdRequest.Builder()
            .setHttpTimeoutMillis(timeOut)
            .build()
    }

    internal fun getTimeout(): Int = timeOut

    private fun initTestDeviceIds() {
        testDeviceIds.add("d7e28f987358016e")
    }

    @JvmStatic
    fun Context.isNetworkConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        return cm?.activeNetworkInfo?.isConnected == true
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
        try {
            if (dialogFullScreen != null && dialogFullScreen?.isShowing == true) {
                dialogFullScreen?.dismiss()
            }
        } catch (_: Exception) {

        }
    }

    internal fun dialogLoading(context: Activity) {
        dialogFullScreen = Dialog(context)
        dialogFullScreen?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogFullScreen?.setContentView(R.layout.dialog_full_screen)
        dialogFullScreen?.setCancelable(false)
        dialogFullScreen?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogFullScreen?.window!!.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        val img = dialogFullScreen?.findViewById<LottieAnimationView>(R.id.imageView3)
        img?.setAnimation(R.raw.gifloading)
        try {
            if (!context.isFinishing && dialogFullScreen != null && dialogFullScreen?.isShowing == false) {
                dialogFullScreen?.show()
            }
        } catch (ignored: Exception) {
        }

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
