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
import com.percas.studio.template.model.InterAdHolder
import com.percas.studio.template.model.NativeAdHolder
import com.percas.studio.template.model.RewardInterAdHolder

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

    private fun initTestDeviceIds() {
        testDeviceIds.add("d7e28f987358016e")
    }

    @JvmStatic
    fun Context.isNetworkConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        return cm?.activeNetworkInfo?.isConnected == true
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


    @JvmStatic
    fun loadAndShowBannerAd(
        activity: Activity,
        idBannerAd: String,
        viewBannerAd: ViewGroup,
        adCallBack: LoadAndShowAdCallBack
    ) {
        val TAG = "Load and show BANNER AD"
        if (!isEnableAd) {
            Log.e(TAG, "Ads is Disable now!")
            adCallBack.onAdFailed("Ads is Disable now!")
            return
        }
        if (!activity.isNetworkConnected()) {
            Log.e(TAG, "No internet!")
            adCallBack.onAdFailed("No internet!")
            return
        }
        val mAdView = AdView(activity)
        mAdView.adUnitId = if (isTestAd) {
            activity.getString(R.string.id_test_banner_admob)
        } else {
            idBannerAd
        }

        if (mAdView.adUnitId.isBlank()) {
            Log.e(TAG, "Ad Id is blank!")
            adCallBack.onAdFailed("Ad Id is blank!")
            return
        }

        mAdView.setAdSize(getAdSize(activity, viewBannerAd.width.toFloat()))



        viewBannerAd.removeAllViews()
        val overlayView =
            activity.layoutInflater.inflate(R.layout.layout_banner_loading, null, false)
        viewBannerAd.addView(overlayView, 0)
        viewBannerAd.addView(mAdView, 1)

        shimmerFrameLayout = overlayView.findViewById(R.id.shimmerBanner)
        shimmerFrameLayout?.startShimmer()

        mAdView.adListener = object : AdListener() {

            override fun onAdLoaded() {
                mAdView.onPaidEventListener =
                    OnPaidEventListener { adValue ->

                        adCallBack.onAdPaid(
                            adValue,
                            mAdView.adUnitId,
                            mAdView.responseInfo?.mediationAdapterClassName ?: "GoogleAdmob"
                        )
                    }
                shimmerFrameLayout?.stopShimmer()
                viewBannerAd.removeView(overlayView)
                adCallBack.onAdLoaded()
                adCallBack.onAdShowed()
                Log.d(TAG, "onAdLoaded and showed")
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                shimmerFrameLayout?.stopShimmer()
                viewBannerAd.removeView(overlayView)
                adCallBack.onAdFailed(adError.message + "\nCause:\n" + adError.cause)
                Log.e(TAG, adError.message + "\nCause:\n" + adError.cause)
            }

            override fun onAdClicked() {
                Log.d(TAG, "onAdClicked")
                adCallBack.onAdClicked()
            }

            override fun onAdImpression() {}

            override fun onAdClosed() {}

            override fun onAdOpened() {}
        }

        if (adRequest != null) {
            mAdView.loadAd(adRequest!!)
        } else {
            Log.d(TAG, "Admob is not init now. Check it before load ad!")
            adCallBack.onAdFailed("Admob is not init now. Check it before load ad!")
        }
    }

    fun loadAndShowBannerCollapsibleAd(
        activity: Activity,
        idBannerCollapAd: String,
        isBottomCollapsible: Boolean,
        viewBanner: ViewGroup,
        adCallBack: LoadAndShowAdCallBack
    ) {
        val TAG = "Load and show BANNER COLLAPSIBLE AD"
        if (!isEnableAd) {
            adCallBack.onAdFailed("Ads is Disable now!")
            Log.e(TAG, "Ads is Disable now!")
            return
        }

        if (!activity.isNetworkConnected()) {
            Log.e(TAG, "No Internet!")
            adCallBack.onAdFailed("No internet!")
            return
        }
        val adView = AdView(activity)
        adView.adUnitId = if (isTestAd) {
            activity.getString(R.string.id_test_collapsible_banner_admob)
        } else {
            idBannerCollapAd
        }
        if (adView.adUnitId.isBlank()) {
            Log.e(TAG, "Ad Id is blank!")
            adCallBack.onAdFailed("Ad Id is blank!")
            return
        }
        val adSize = getAdSize(activity, viewBanner.width.toFloat())
        adView.setAdSize(adSize)

        viewBanner.removeAllViews()
        val overlayView =
            activity.layoutInflater.inflate(R.layout.layout_banner_loading, null, false)
        viewBanner.addView(overlayView, 0)
        viewBanner.addView(adView, 1)

        shimmerFrameLayout = overlayView.findViewById(R.id.shimmer_view_container)
        shimmerFrameLayout?.startShimmer()

        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                adView.onPaidEventListener =
                    OnPaidEventListener { adValue ->
                        adCallBack.onAdPaid(
                            adValue,
                            adView.adUnitId,
                            adView.responseInfo?.mediationAdapterClassName ?: "GoogleAdmob"
                        )
                    }
                shimmerFrameLayout?.stopShimmer()
                viewBanner.removeView(overlayView)
                overlayView.destroyDrawingCache()
                adCallBack.onAdLoaded()

                Log.d(TAG, "onAdLoaded")

                val params: ViewGroup.LayoutParams = viewBanner.layoutParams
                params.height = adSize.getHeightInPixels(activity)
                viewBanner.layoutParams = params
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                shimmerFrameLayout?.stopShimmer()
                viewBanner.removeView(overlayView)
                adCallBack.onAdFailed(adError.message + "\nCause\n" + adError.cause)
                Log.e(TAG, "onAdFailedToLoad: " + adError.message + "\nCause\n" + adError.cause)
            }

            override fun onAdOpened() {}
            override fun onAdClicked() {
                adCallBack.onAdClicked()
                Log.d(TAG, "onAdClicked")
            }

            override fun onAdClosed() {}
        }
        val extras = Bundle()

        val positionCollapsible = if (!isBottomCollapsible) {
            "top"
        } else {
            "bottom"
        }
        extras.putString("collapsible", positionCollapsible)
        val adRequest2 =
            AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, extras).build()
        adView.loadAd(adRequest2)
    }

    @JvmStatic
    fun loadNativeAd(
        context: Context,
        nativeHolder: NativeAdHolder,
        adCallBack: LoadAdCallBack
    ) {
        NativeAdManager.loadNativeAd(context, nativeHolder, adCallBack)
    }

    @JvmStatic
    fun <T : ViewBinding> showNativeAd(
        activity: Activity,
        nativeHolder: NativeAdHolder,
        viewNativeAd: ViewGroup,
        renderer: NativeAdRenderer<T>,
        adCallBack: ShowAdCallBack
    ) {
        NativeAdManager.showNativeAd(activity, nativeHolder, viewNativeAd, renderer, adCallBack)
    }


    @JvmStatic
    fun <T : ViewBinding> loadAndShowNativeAd(
        activity: Activity,
        nativeHolder: NativeAdHolder,
        viewNativeAd: ViewGroup,
        renderer: NativeAdRenderer<T>,
        adCallBack: LoadAndShowAdCallBack
    ) {
        NativeAdManager.loadAndShowNativeAd(activity, nativeHolder, viewNativeAd, renderer, adCallBack)
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
        nativeHolder: NativeAdHolder,
        mediaAspectRatio: Int,
        adCallBack: LoadAdCallBack
    ) {
        NativeAdManager.loadNativeAdFullScreen(context, nativeHolder, mediaAspectRatio, adCallBack)
    }

    @JvmStatic
    fun <T : ViewBinding> showNativeAdFullScreen(
        activity: Activity,
        nativeHolder: NativeAdHolder,
        viewNativeAd: ViewGroup,
        renderer: NativeAdRenderer<T>,
        adCallBack: ShowAdCallBack

    ) {
        NativeAdManager.showNativeAdFullScreen(activity, nativeHolder, viewNativeAd, renderer, adCallBack)
    }


    @JvmStatic
    fun loadInterstitialAd(
        activity: Context,
        interHolder: InterAdHolder,
        adLoadCallback: LoadAdCallBack
    ) {
        val TAG = "Load INTERSTITIAL AD"
        if (!isEnableAd) {
            adLoadCallback.onAdFailed("Ads is DISABLE now!")
            Log.e(TAG, "Ads is DISABLE now!")
            return
        }

        if (!activity.isNetworkConnected()) {
            adLoadCallback.onAdFailed("No Internet!")
            Log.e(TAG, "No Internet!")
            return
        }
        if (interHolder.inter != null) {
            Log.e(TAG, "This Interstitial Ad is not empty. Don't need to load again!")
            adLoadCallback.onAdFailed("This Interstitial Ad is not empty. Don't need to load again!")
            return
        }
        interHolder.isloading = true

        if (adRequest == null) {
            initAdRequest(timeOut)
        }

        if (isTestAd) {
            interHolder.ads = activity.getString(R.string.id_test_interstitial_admob)
        }
        if (interHolder.ads.isBlank() && !isTestAd) {
            Log.e(TAG, "Ad Id is blank!")
            adLoadCallback.onAdFailed("Ad Id is blank!")
            return
        }
        InterstitialAd.load(
            activity,
            interHolder.ads,
            adRequest!!,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    if (isOverlayAdShowing) {
                        interHolder.mutable.value = interstitialAd
                    }
                    interHolder.inter = interstitialAd
                    interHolder.isloading = false
                    adLoadCallback.onAdLoaded()
                    Log.d(TAG, "onAdLoaded")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interHolder.isloading = false
                    if (isOverlayAdShowing) {
                        interHolder.mutable.value = null
                    }
                    adLoadCallback.onAdFailed(loadAdError.message + "\nCause\n" + loadAdError.cause)
                    Log.e(TAG, loadAdError.message + "\nCause\n" + loadAdError.cause)
                }
            })
    }

    @JvmStatic
    fun showInterstitialAd(
        activity: Activity,
        interHolder: InterAdHolder,
        adCallback: ShowAdCallBack,
    ) {
        val TAG = "Show INTERSTITIAL AD"
        if (isOverlayAdShowing) {
            adCallback.onAdFailed("Other Ads is showing now!")
            Log.e(TAG, "Other Ads is showing now!")
            return
        }

        if (!isEnableAd) {
            isOverlayAdShowing = false
            if (AppResumeAdsManager.getInstance().isInitialized) {
                AppResumeAdsManager.getInstance().isAppResumeEnabled = true
            }
            adCallback.onAdFailed("Ads is DISABLE now!")
            Log.e(TAG, "Ads is DISABLE now!")
            return
        }

        if (!activity.isNetworkConnected()) {
            isOverlayAdShowing = false
            if (AppResumeAdsManager.getInstance().isInitialized) {
                AppResumeAdsManager.getInstance().isAppResumeEnabled = true
            }
            adCallback.onAdFailed("No Internet!")
            Log.e(TAG, "No Internet!")
            return
        }
        if (interHolder.ads.isBlank() && !isTestAd) {
            Log.e(TAG, "Ad Id is blank!")
            adCallback.onAdFailed("Ad Id is blank!")
            return
        }
        isOverlayAdShowing = true


        val handler = Handler(Looper.getMainLooper())
        val runnable = Runnable {
            if (interHolder.isloading) {
                if (AppResumeAdsManager.getInstance().isInitialized) {
                    AppResumeAdsManager.getInstance().isAppResumeEnabled = true
                }
                isOverlayAdShowing = false
                interHolder.mutable.removeObservers((activity as LifecycleOwner))
                dismissAdDialog()
                adCallback.onAdFailed("Time out!")
                Log.e(TAG, "Time out!")
            }
        }
        handler.postDelayed(runnable, 10000)

        if (interHolder.isloading) {
            dialogLoading(activity)

            interHolder.mutable.observe((activity as LifecycleOwner)) { interstitialAd: InterstitialAd? ->
                if (interstitialAd != null) {

                    interHolder.mutable.removeObservers((activity as LifecycleOwner))

                    Handler(Looper.getMainLooper()).postDelayed({

                        interstitialAd.fullScreenContentCallback =
                            object : FullScreenContentCallback() {
                                override fun onAdDismissedFullScreenContent() {
                                    isOverlayAdShowing = false

                                    if (AppResumeAdsManager.getInstance().isInitialized) {
                                        AppResumeAdsManager.getInstance().isAppResumeEnabled = true
                                    }
                                    interHolder.inter = null
                                    interHolder.mutable.removeObservers((activity as LifecycleOwner))
                                    interHolder.mutable.value = null
                                    adCallback.onAdClosed()
                                    Log.d(TAG, "onAdClosed")
                                    dismissAdDialog()
                                }

                                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                    isOverlayAdShowing = false
                                    if (AppResumeAdsManager.getInstance().isInitialized) {
                                        AppResumeAdsManager.getInstance().isAppResumeEnabled = true
                                    }
                                    isOverlayAdShowing = false
                                    interHolder.inter = null
                                    dismissAdDialog()
                                    interHolder.mutable.removeObservers((activity as LifecycleOwner))
                                    interHolder.mutable.value = null
                                    handler.removeCallbacksAndMessages(null)
                                    adCallback.onAdFailed(adError.message + " \ncause: \n" + adError.cause)
                                    Log.e(TAG, adError.message + " \ncause: \n" + adError.cause)
                                }

                                override fun onAdShowedFullScreenContent() {
                                    handler.removeCallbacksAndMessages(null)
                                    isOverlayAdShowing = true
                                    adCallback.onAdShowed()
                                    Log.d(TAG, "onAdShowed")
                                    try {
                                        interstitialAd.setOnPaidEventListener { adValue ->
                                            adCallback.onAdPaid(
                                                adValue,
                                                interHolder.inter!!.adUnitId,
                                                interstitialAd.responseInfo.mediationAdapterClassName
                                                    ?: "GoogleAdmob"
                                            )
                                        }
                                    } catch (_: Exception) {
                                    }
                                }
                            }
                        showInterstitialAdNew(activity, interstitialAd, adCallback)
                    }, 400)
                } else {
                    interHolder.isloading = true
                }
            }
            return
        }

        if (interHolder.inter == null) {
            isOverlayAdShowing = false
            if (AppResumeAdsManager.getInstance().isInitialized) {
                AppResumeAdsManager.getInstance().isAppResumeEnabled = true
            }
            adCallback.onAdFailed("Inter Ad is null. Load inter ad before show!")
            Log.e(TAG, "Inter Ad is null. Load inter ad before show!")
            handler.removeCallbacksAndMessages(null)
        } else {
            dialogLoading(activity)
            Handler(Looper.getMainLooper()).postDelayed({
                interHolder.inter?.fullScreenContentCallback =
                    object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            isOverlayAdShowing = false
                            if (AppResumeAdsManager.getInstance().isInitialized) {
                                AppResumeAdsManager.getInstance().isAppResumeEnabled = true
                            }
                            interHolder.mutable.removeObservers((activity as LifecycleOwner))
                            interHolder.inter = null
                            adCallback.onAdClosed()
                            Log.d(TAG, "onAdClosed")
                            dismissAdDialog()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            isOverlayAdShowing = false
                            if (AppResumeAdsManager.getInstance().isInitialized) {
                                AppResumeAdsManager.getInstance().isAppResumeEnabled = true
                            }
                            handler.removeCallbacksAndMessages(null)
                            interHolder.inter = null
                            interHolder.mutable.removeObservers((activity as LifecycleOwner))
                            dismissAdDialog()
                            adCallback.onAdFailed(adError.message + " \ncause:\n" + adError.cause)
                            Log.e(TAG, adError.message + " \ncause:\n" + adError.cause)
                        }

                        override fun onAdShowedFullScreenContent() {
                            handler.removeCallbacksAndMessages(null)
                            isOverlayAdShowing = true
                            adCallback.onAdShowed()
                            Log.d(TAG, "onAdShowed")
                        }
                    }
                showInterstitialAdNew(activity, interHolder.inter, adCallback)
            }, 400)
        }
    }

    fun loadAndShowInterstitialAd(
        activity: Activity,
        interAdHolder: InterAdHolder,
        adCallback: LoadAndShowAdCallBack
    ) {
        val TAG = "Load and show INTERSTITIAL AD"
        if (adRequest == null) {
            initAdRequest(timeOut)
        }
        if (isOverlayAdShowing) {
            adCallback.onAdFailed("Other ad is showing!")
            Log.e(TAG, "Other ad is showing!")
            return
        }
        if (!isEnableAd) {
            adCallback.onAdFailed("Ads is DISABLE now!")
            Log.e(TAG, "Ads is DISABLE now!")
            if (AppResumeAdsManager.getInstance().isInitialized) {
                AppResumeAdsManager.getInstance().isAppResumeEnabled = true
            }
            return
        }

        if (!activity.isNetworkConnected()) {
            adCallback.onAdFailed("No Internet!")
            Log.e(TAG, "No Internet!")
            if (AppResumeAdsManager.getInstance().isInitialized) {
                AppResumeAdsManager.getInstance().isAppResumeEnabled = true
            }
            return
        }
        if (isTestAd) {
            interAdHolder.ads = activity.getString(R.string.id_test_interstitial_admob)
        }
        if (interAdHolder.ads.isBlank() && !isTestAd) {
            Log.e(TAG, "Ad Id is blank!")
            adCallback.onAdFailed("Ad Id is blank!")
            return
        }
        isOverlayAdShowing = true
        dialogLoading(activity)


        InterstitialAd.load(
            activity,
            interAdHolder.ads,
            adRequest!!,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    super.onAdLoaded(interstitialAd)

                    adCallback.onAdLoaded()
                    interAdHolder.inter = interstitialAd
                    interAdHolder.inter!!.onPaidEventListener =
                        OnPaidEventListener { adValue: AdValue? ->
                            adCallback.onAdPaid(
                                adValue!!,
                                interAdHolder.ads,
                                interstitialAd.responseInfo.mediationAdapterClassName
                                    ?: "GoogleAdmob"
                            )
                        }
                    interAdHolder.inter!!.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                adCallback.onAdFailed(adError.message + "\nCause\n" + adError.cause)
                                Log.e(TAG, adError.message + "\nCause\n" + adError.cause)
                                isOverlayAdShowing = false
                                if (AppResumeAdsManager.getInstance().isInitialized) {
                                    AppResumeAdsManager.getInstance().isAppResumeEnabled = true
                                }
                                if (interAdHolder.inter != null) {
                                    interAdHolder.inter = null
                                }
                                dismissAdDialog()
                            }

                            override fun onAdDismissedFullScreenContent() {
                                adCallback.onAdClosed()
                                Log.d(TAG, "onAdClosed")
                                dismissAdDialog()
                                if (interAdHolder.inter != null) {
                                    interAdHolder.inter = null
                                }
                                isOverlayAdShowing = false
                                if (AppResumeAdsManager.getInstance().isInitialized) {
                                    AppResumeAdsManager.getInstance().isAppResumeEnabled = true
                                }
                            }

                            override fun onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent()
                                adCallback.onAdShowed()
                                Log.d(TAG, "onAdShowed")
                                dismissAdDialog()
                            }
                        }

                    if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && interAdHolder.inter != null) {
                        isOverlayAdShowing = true
                        adCallback.onAdShowed()
                        Log.d(TAG, "onAdShowed")
                        interAdHolder.inter!!.setOnPaidEventListener { adValue ->
                            adCallback.onAdPaid(
                                adValue,
                                interAdHolder.inter!!.adUnitId,
                                interAdHolder.inter!!.responseInfo.mediationAdapterClassName
                                    ?: "GoogleAdmob"
                            )
                        }
                        interAdHolder.inter!!.show(activity)
                    } else {
                        isOverlayAdShowing = false
                        if (AppResumeAdsManager.getInstance().isInitialized) {
                            AppResumeAdsManager.getInstance().isAppResumeEnabled = true
                        }
                        dismissAdDialog()
                        adCallback.onAdFailed("Your App is showing on resume ad or inter ad is null!")
                        Log.e(TAG, "Your App is showing on resume ad or inter ad is null!")
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    interAdHolder.inter = null
                    if (AppResumeAdsManager.getInstance().isInitialized) {
                        AppResumeAdsManager.getInstance().isAppResumeEnabled = true
                    }
                    isOverlayAdShowing = false
                    adCallback.onAdFailed(loadAdError.message + "\nCause\n" + loadAdError.cause)
                    Log.e(TAG, loadAdError.message + "\nCause\n" + loadAdError.cause)
                    dismissAdDialog()
                }
            })
    }

    fun loadAndShowInterstitialAdWithoutLoadingScreen(
        activity: Activity,
        interAdHolder: InterAdHolder,
        adCallback: LoadAndShowAdCallBack
    ) {
        val TAG = "Load and show INTERSTITIAL AD"
        if (adRequest == null) {
            initAdRequest(timeOut)
        }
        if (isOverlayAdShowing) {
            adCallback.onAdFailed("Other ad is showing!")
            Log.e(TAG, "Other ad is showing!")
            return
        }
        if (!isEnableAd) {
            adCallback.onAdFailed("Ads is DISABLE now!")
            Log.e(TAG, "Ads is DISABLE now!")
            if (AppResumeAdsManager.getInstance().isInitialized) {
                AppResumeAdsManager.getInstance().isAppResumeEnabled = true
            }
            return
        }

        if (!activity.isNetworkConnected()) {
            adCallback.onAdFailed("No Internet!")
            Log.e(TAG, "No Internet!")
            if (AppResumeAdsManager.getInstance().isInitialized) {
                AppResumeAdsManager.getInstance().isAppResumeEnabled = true
            }
            return
        }
        if (isTestAd) {
            interAdHolder.ads = activity.getString(R.string.id_test_interstitial_admob)
        }
        if (interAdHolder.ads.isBlank() && !isTestAd) {
            Log.e(TAG, "Ad Id is blank!")
            adCallback.onAdFailed("Ad Id is blank!")
            return
        }
        isOverlayAdShowing = true
//        dialogLoading(activity)


        InterstitialAd.load(
            activity,
            interAdHolder.ads,
            adRequest!!,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    super.onAdLoaded(interstitialAd)

                    adCallback.onAdLoaded()
                    interAdHolder.inter = interstitialAd
                    interAdHolder.inter!!.onPaidEventListener =
                        OnPaidEventListener { adValue: AdValue? ->
                            adCallback.onAdPaid(
                                adValue!!,
                                interAdHolder.ads,
                                interstitialAd.responseInfo.mediationAdapterClassName
                                    ?: "GoogleAdmob"
                            )
                        }
                    interAdHolder.inter!!.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                adCallback.onAdFailed(adError.message + "\nCause\n" + adError.cause)
                                Log.e(TAG, adError.message + "\nCause\n" + adError.cause)
                                isOverlayAdShowing = false
                                if (AppResumeAdsManager.getInstance().isInitialized) {
                                    AppResumeAdsManager.getInstance().isAppResumeEnabled = true
                                }
                                if (interAdHolder.inter != null) {
                                    interAdHolder.inter = null
                                }
                                dismissAdDialog()
                            }

                            override fun onAdDismissedFullScreenContent() {
                                adCallback.onAdClosed()
                                Log.d(TAG, "onAdClosed")
                                dismissAdDialog()
                                if (interAdHolder.inter != null) {
                                    interAdHolder.inter = null
                                }
                                isOverlayAdShowing = false
                                if (AppResumeAdsManager.getInstance().isInitialized) {
                                    AppResumeAdsManager.getInstance().isAppResumeEnabled = true
                                }
                            }

                            override fun onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent()
                                adCallback.onAdShowed()
                                Log.d(TAG, "onAdShowed")
                                dismissAdDialog()
                            }
                        }

                    if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && interAdHolder.inter != null) {
                        isOverlayAdShowing = true
                        adCallback.onAdShowed()
                        Log.d(TAG, "onAdShowed")
                        interAdHolder.inter!!.setOnPaidEventListener { adValue ->
                            adCallback.onAdPaid(
                                adValue,
                                interAdHolder.inter!!.adUnitId,
                                interAdHolder.inter!!.responseInfo.mediationAdapterClassName
                                    ?: "GoogleAdmob"
                            )
                        }
                        interAdHolder.inter!!.show(activity)
                    } else {
                        isOverlayAdShowing = false
                        if (AppResumeAdsManager.getInstance().isInitialized) {
                            AppResumeAdsManager.getInstance().isAppResumeEnabled = true
                        }
                        dismissAdDialog()
                        adCallback.onAdFailed("Your App is showing on resume ad or inter ad is null!")
                        Log.e(TAG, "Your App is showing on resume ad or inter ad is null!")
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    interAdHolder.inter = null
                    if (AppResumeAdsManager.getInstance().isInitialized) {
                        AppResumeAdsManager.getInstance().isAppResumeEnabled = true
                    }
                    isOverlayAdShowing = false
                    adCallback.onAdFailed(loadAdError.message + "\nCause\n" + loadAdError.cause)
                    Log.e(TAG, loadAdError.message + "\nCause\n" + loadAdError.cause)
                    dismissAdDialog()
                }
            })
    }

    fun dismissAdDialog() {
        try {
            if (dialogFullScreen != null && dialogFullScreen?.isShowing == true) {
                dialogFullScreen?.dismiss()
            }
        } catch (_: Exception) {

        }
    }

    @JvmStatic
    private fun showInterstitialAdNew(
        activity: Activity,
        mInterstitialAd: InterstitialAd?,
        adcallback: ShowAdCallBack
    ) {
        val TAG = "Show INTERSTITIAL AD new"
        if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && mInterstitialAd != null) {
            isOverlayAdShowing = true
            Handler(Looper.getMainLooper()).postDelayed({
                adcallback.onAdShowed()
                Log.d(TAG, "showInterstitialAdNew")
                mInterstitialAd.setOnPaidEventListener { adValue ->
                    adcallback.onAdPaid(
                        adValue,
                        mInterstitialAd.adUnitId,
                        mInterstitialAd.responseInfo.mediationAdapterClassName ?: "GoogleAdmob"
                    )
                }
                mInterstitialAd.show(activity)
            }, 400)
        } else {
            isOverlayAdShowing = false
            if (AppResumeAdsManager.getInstance().isInitialized) {
                AppResumeAdsManager.getInstance().isAppResumeEnabled = true
            }
            dismissAdDialog()
            adcallback.onAdFailed("Your App is showing on resume ad or inter ad is null!")
            Log.e(TAG, "Your App is showing on resume ad or inter ad is null!")
        }
    }

    private fun dialogLoading(context: Activity) {
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
        val TAG = "Load and show REWARD AD"
        if (!isEnableAd) {
            adCallback.onAdFailed("Ads is DISABLE now!")
            Log.e(TAG, "Ads is DISABLE now!")
            return
        }

        if (!activity.isNetworkConnected()) {
            adCallback.onAdFailed("No Internet!")
            Log.e(TAG, "No Internet!")
            return
        }
        if (isOverlayAdShowing) {
            adCallback.onAdFailed("Other ad is showing!")
            Log.e(TAG, "Other ad is showing!")
            return
        }
        if (adRequest == null) {
            initAdRequest(timeOut)
        }

        val idReward = if (isTestAd) {
            activity.getString(R.string.id_test_reward_admob)
        } else {
            admobId
        }
        if (idReward.isBlank() && !isTestAd) {
            Log.e(TAG, "Ad Id is blank!")
            adCallback.onAdFailed("Ad Id is blank!")
            return
        }

        dialogLoading(activity)

        isOverlayAdShowing = true

        if (AppResumeAdsManager.getInstance().isInitialized) {
            AppResumeAdsManager.getInstance().isAppResumeEnabled = false
        }

        RewardedAd.load(activity, idReward,
            adRequest!!, object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    adCallback.onAdFailed(loadAdError.message + "\nCause:\n" + loadAdError.cause)
                    Log.e(TAG, loadAdError.message + "\nCause:\n" + loadAdError.cause)
                    dismissAdDialog()
                    if (AppResumeAdsManager.getInstance().isInitialized) {
                        AppResumeAdsManager.getInstance().isAppResumeEnabled = true
                    }
                    isOverlayAdShowing = false
                }

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    adCallback.onAdLoaded()
                    Log.d(TAG, "onAdLoaded")
                    rewardedAd.setOnPaidEventListener {
                        adCallback.onAdPaid(
                            it,
                            rewardedAd.adUnitId,
                            rewardedAd.responseInfo.mediationAdapterClassName ?: "GoogleAdmob"
                        )
                    }

                    rewardedAd.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdShowedFullScreenContent() {
                                isOverlayAdShowing = true
                                adCallback.onAdShowed()
                                Log.d(TAG, "onAdShowed")
                                if (AppResumeAdsManager.getInstance().isInitialized) {
                                    AppResumeAdsManager.getInstance().isAppResumeEnabled = false
                                }
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                isOverlayAdShowing = false
                                adCallback.onAdFailed(adError.message + "\nCause:n\n" + adError.cause)
                                Log.e(TAG, adError.message + "\nCause:n\n" + adError.cause)
                                dismissAdDialog()

                                if (AppResumeAdsManager.getInstance().isInitialized) {
                                    AppResumeAdsManager.getInstance().isAppResumeEnabled = true
                                }
                            }

                            override fun onAdDismissedFullScreenContent() {
                                isOverlayAdShowing = false
                                adCallback.onAdClosed()
                                Log.d(TAG, "onAdClosed")
                                dismissAdDialog()
                                if (AppResumeAdsManager.getInstance().isInitialized) {
                                    AppResumeAdsManager.getInstance().isAppResumeEnabled = true
                                }
                            }
                        }

                    if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        if (AppResumeAdsManager.getInstance().isInitialized) {
                            AppResumeAdsManager.getInstance().isAppResumeEnabled = false
                        }
                        rewardedAd.show(activity) {
                            adCallback.onAdEarned()
                            Log.d(TAG, "onAdEarned")
                            dismissAdDialog()
                        }
                        isOverlayAdShowing = true
                    } else {
                        dismissAdDialog()
                        isOverlayAdShowing = false
                        if (AppResumeAdsManager.getInstance().isInitialized) {
                            AppResumeAdsManager.getInstance().isAppResumeEnabled = true
                        }
                        adCallback.onAdFailed("Your App is showing on resume ad!")
                        Log.e(TAG, "Your App is showing on resume ad!")
                    }
                }
            })
    }

    @JvmStatic
    fun loadInterReward(
        context: Context,
        rewardInterAdHolder: RewardInterAdHolder,
        adLoadCallback: LoadAdCallBack
    ) {
        val TAG = "Load INTERSTITIAL REWARD AD"
        if (!isEnableAd) {
            adLoadCallback.onAdFailed("Ads is DISABLE now!")
            Log.e(TAG, "Ads is DISABLE now!")
            return
        }

        if (!context.isNetworkConnected()) {
            adLoadCallback.onAdFailed("No Internet!")
            Log.e(TAG, "No Internet!")
            return
        }
        if (rewardInterAdHolder.rewardInterAd != null) {
            adLoadCallback.onAdFailed("This Interstitial Ad is not empty. Don't need to load again!")
            Log.e(TAG, "This Interstitial Ad is not empty. Don't need to load again!")
            return
        }
        rewardInterAdHolder.isLoading = true

        if (adRequest == null) {
            initAdRequest(timeOut)
        }

        if (isTestAd) {
            rewardInterAdHolder.ads = context.getString(R.string.id_test_reward_inter_admob)
        }

        if (rewardInterAdHolder.ads.isBlank() && !isTestAd) {
            Log.e(TAG, "Ad Id is blank!")
            adLoadCallback.onAdFailed("Ad Id is blank!")
            return
        }

        RewardedInterstitialAd.load(
            context,
            rewardInterAdHolder.ads,
            adRequest!!,
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialRewardAd: RewardedInterstitialAd) {
                    rewardInterAdHolder.rewardInterAd = interstitialRewardAd
                    rewardInterAdHolder.mutable.value = interstitialRewardAd
                    rewardInterAdHolder.isLoading = false
                    adLoadCallback.onAdLoaded()
                    Log.d(TAG, "onAdLoaded")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardInterAdHolder.rewardInterAd = null
                    rewardInterAdHolder.isLoading = false
                    rewardInterAdHolder.mutable.value = null
                    adLoadCallback.onAdFailed(loadAdError.message + "\nCause\n" + loadAdError.cause)
                    Log.e(TAG, loadAdError.message + "\nCause\n" + loadAdError.cause)
                }
            })
    }


    @JvmStatic
    fun showInterReward(
        activity: Activity,
        rewardInterAdHolder: RewardInterAdHolder,
        adCallback: ShowRewardAdCallBack
    ) {
        if (adRequest == null) {
            initAdRequest(timeOut)
        }
        if (isOverlayAdShowing) {
            adCallback.onAdFailed("Other ad is showing")
            return
        }
        if (!isEnableAd) {
            adCallback.onAdFailed("Ads is DISABLE now")
            if (AppResumeAdsManager.getInstance().isInitialized) {
                AppResumeAdsManager.getInstance().isAppResumeEnabled = true
            }
            return
        }

        if (!activity.isNetworkConnected()) {
            adCallback.onAdFailed("No Internet")
            if (AppResumeAdsManager.getInstance().isInitialized) {
                AppResumeAdsManager.getInstance().isAppResumeEnabled = true
            }
            return
        }

        if (rewardInterAdHolder.ads.isBlank() && !isTestAd) {
            adCallback.onAdFailed("Ad Id is blank!")
            return
        }

        isOverlayAdShowing = true

        dialogLoading(activity)

        if (rewardInterAdHolder.isLoading) {
            rewardInterAdHolder.mutable.observe(activity as LifecycleOwner) { reward: RewardedInterstitialAd? ->
                reward?.let {
                    rewardInterAdHolder.mutable.removeObservers((activity as LifecycleOwner))
                    it.setOnPaidEventListener { value ->
                        adCallback.onAdPaid(
                            value,
                            rewardInterAdHolder.rewardInterAd!!.adUnitId,
                            reward.responseInfo.mediationAdapterClassName ?: "GoogleAdmob"
                        )
                    }
                    rewardInterAdHolder.rewardInterAd?.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                rewardInterAdHolder.rewardInterAd = null
                                rewardInterAdHolder.mutable.removeObservers((activity as LifecycleOwner))
                                rewardInterAdHolder.mutable.value = null
                                if (AppResumeAdsManager.getInstance().isInitialized) {
                                    AppResumeAdsManager.getInstance().isAppResumeEnabled = true
                                }
                                isOverlayAdShowing = false
                                dismissAdDialog()
                                adCallback.onAdClosed()
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                rewardInterAdHolder.rewardInterAd = null
                                rewardInterAdHolder.mutable.removeObservers((activity as LifecycleOwner))
                                rewardInterAdHolder.mutable.value = null
                                if (AppResumeAdsManager.getInstance().isInitialized) {
                                    AppResumeAdsManager.getInstance().isAppResumeEnabled = true
                                }
                                isOverlayAdShowing = false
                                dismissAdDialog()
                                adCallback.onAdFailed(adError.message + "\nCause:\n" + adError.cause)
                            }

                            override fun onAdShowedFullScreenContent() {
                                isOverlayAdShowing = true
                                adCallback.onAdShowed()
                                dismissAdDialog()
                            }
                        }
                    it.show(activity) {
                        adCallback.onAdEarned()
                    }
                }
            }
        } else {
            if (rewardInterAdHolder.rewardInterAd != null) {

                rewardInterAdHolder.rewardInterAd?.setOnPaidEventListener {
                    adCallback.onAdPaid(
                        it,
                        rewardInterAdHolder.rewardInterAd!!.adUnitId,
                        rewardInterAdHolder.rewardInterAd!!.responseInfo.mediationAdapterClassName
                            ?: "GoogleAdmob"
                    )
                }
                rewardInterAdHolder.rewardInterAd?.fullScreenContentCallback =
                    object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            rewardInterAdHolder.rewardInterAd = null
                            rewardInterAdHolder.mutable.removeObservers((activity as LifecycleOwner))
                            rewardInterAdHolder.mutable.value = null
                            if (AppResumeAdsManager.getInstance().isInitialized) {
                                AppResumeAdsManager.getInstance().isAppResumeEnabled = true
                            }
                            isOverlayAdShowing = false
                            dismissAdDialog()
                            adCallback.onAdClosed()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            rewardInterAdHolder.rewardInterAd = null
                            rewardInterAdHolder.mutable.removeObservers((activity as LifecycleOwner))
                            rewardInterAdHolder.mutable.value = null
                            if (AppResumeAdsManager.getInstance().isInitialized) {
                                AppResumeAdsManager.getInstance().isAppResumeEnabled = true
                            }
                            isOverlayAdShowing = false
                            dismissAdDialog()
                            adCallback.onAdFailed(adError.message + "\nCause:\n" + adError.cause)
                        }

                        override fun onAdShowedFullScreenContent() {
                            isOverlayAdShowing = true
                            adCallback.onAdShowed()
                            dismissAdDialog()
                        }
                    }
                rewardInterAdHolder.rewardInterAd?.show(activity) { adCallback.onAdEarned() }

            } else {
                isOverlayAdShowing = false
                adCallback.onAdFailed("Ad is null. Load Inter Reward before show it!")
                dismissAdDialog()
                if (AppResumeAdsManager.getInstance().isInitialized) {
                    AppResumeAdsManager.getInstance().isAppResumeEnabled = true
                }
            }
        }
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
