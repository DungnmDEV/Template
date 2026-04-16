@file:Suppress("DEPRECATION", "LocalVariableName")

package com.percas.studio.template.admob

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import android.widget.Toast
import androidx.viewbinding.ViewBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.percas.studio.template.admob.renderer.NativeAdRenderer
import com.percas.studio.template.cmp.ConsentConfig
import com.percas.studio.template.cmp.ConsentManager
import com.percas.studio.template.cmp.ConsentResult

@SuppressLint("InflateParams")
object AdmobManager {

    private val testDeviceIds: ArrayList<String> = ArrayList()
    private var isConsentRequestedThisSession = false
    private var pendingConsentCallbacks: MutableList<(ConsentResult) -> Unit> = mutableListOf()
    private var lastConsentResult: ConsentResult? = null

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

    @JvmStatic
    fun initAdmob(context: Context?, config: AdmobConfig) {
        initCore(context, config)
        if (context is Activity) {
            requestConsentOnce(context, ConsentConfig())
        }
    }

    @JvmStatic
    fun initAdmob(
        activity: Activity,
        config: AdmobConfig,
        consentConfig: ConsentConfig = ConsentConfig(),
        onConsentResult: ((ConsentResult) -> Unit)? = null,
    ) {
        initCore(activity, config)
        requestConsentOnce(activity, consentConfig, onConsentResult)
    }

    @JvmStatic
    fun ensureConsent(
        activity: Activity,
        consentConfig: ConsentConfig = ConsentConfig(),
        onConsentResult: ((ConsentResult) -> Unit)? = null,
    ) {
        requestConsentOnce(activity, consentConfig, onConsentResult)
    }

    private fun initCore(context: Context?, config: AdmobConfig) {
        if (config.requestTimeoutMillis < 5000 && config.requestTimeoutMillis != 0) {
            Toast.makeText(context, "Limit time ~10000", Toast.LENGTH_LONG).show()
        }
        AdmobCore.updateConfig(config)

        MobileAds.initialize(context!!) {}

        initTestDeviceIds()

        val configuration = RequestConfiguration.Builder()
            .setTestDeviceIds(testDeviceIds)
            .build()
        MobileAds.setRequestConfiguration(configuration)

        initAdRequest(config.requestTimeoutMillis)
    }

    private fun requestConsentOnce(
        activity: Activity,
        consentConfig: ConsentConfig,
        onConsentResult: ((ConsentResult) -> Unit)? = null,
    ) {
        synchronized(this) {
            if (isConsentRequestedThisSession) {
                lastConsentResult?.let { result ->
                    onConsentResult?.invoke(result)
                    return
                }
                onConsentResult?.let { pendingConsentCallbacks.add(it) }
                return
            }
            isConsentRequestedThisSession = true
            AdmobCore.markConsentPending()
            onConsentResult?.let { pendingConsentCallbacks.add(it) }
        }

        val consentManager = ConsentManager(activity, consentConfig)
        consentManager.gatherConsent { result ->
            val callbacks: List<(ConsentResult) -> Unit>
            synchronized(this) {
                lastConsentResult = result
                callbacks = pendingConsentCallbacks.toList()
                pendingConsentCallbacks.clear()
            }
            if (result.formError != null) {
                AdmobCore.markConsentError(result.formError.message ?: "Consent flow error")
            } else if (result.canRequestAds) {
                AdmobCore.markConsentGranted()
            } else {
                AdmobCore.markConsentDenied("Consent is required before requesting ads")
            }
            callbacks.forEach { callback -> callback(result) }
        }
    }

    private fun consentError(): AdErrorInfo {
        val code = if (AdmobCore.consentStatus == AdmobCore.ConsentStatus.ERROR) {
            AdErrorCode.CONSENT_FLOW_ERROR
        } else {
            AdErrorCode.CONSENT_REQUIRED
        }
        return AdErrorInfo(code, AdmobCore.consentMessage)
    }

    private inline fun requireConsent(onFail: (AdErrorInfo) -> Unit, block: () -> Unit) {
        if (!AdmobCore.canRequestAdsByConsent()) {
            onFail(consentError())
            return
        }
        block()
    }

    @JvmStatic
    fun initAdmob(context: Context?, timeOut: Int, isTestAd: Boolean, isEnableAd: Boolean) {
        initAdmob(
            context,
            AdmobConfig(
                requestTimeoutMillis = timeOut,
                isTestAd = isTestAd,
                isEnableAd = isEnableAd
            )
        )
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
        requireConsent(onFail = adCallBack::onAdFailed) {
            BannerAdManager.loadAndShowBannerAd(activity, idBannerAd, viewBannerAd, adCallBack)
        }
    }

    fun loadAndShowBannerCollapsibleAd(
        activity: Activity,
        idBannerCollapAd: String,
        isBottomCollapsible: Boolean,
        viewBanner: ViewGroup,
        adCallBack: LoadAndShowAdCallBack
    ) {
        requireConsent(onFail = adCallBack::onAdFailed) {
            BannerAdManager.loadAndShowBannerCollapsibleAd(
                activity,
                idBannerCollapAd,
                isBottomCollapsible,
                viewBanner,
                adCallBack
            )
        }
    }

    @JvmStatic
    fun loadNativeAd(
        context: Context,
        idAd: String,
        adCallBack: LoadAdCallBack
    ) {
        requireConsent(onFail = adCallBack::onAdFailed) {
            NativeAdManager.loadNativeAd(context, idAd, adCallBack)
        }
    }

    @JvmStatic
    fun <T : ViewBinding> showNativeAd(
        activity: Activity,
        idAd: String,
        viewNativeAd: ViewGroup,
        renderer: NativeAdRenderer<T>,
        adCallBack: ShowAdCallBack
    ) {
        requireConsent(onFail = adCallBack::onAdFailed) {
            NativeAdManager.showNativeAd(activity, idAd, viewNativeAd, renderer, adCallBack)
        }
    }


    @JvmStatic
    fun <T : ViewBinding> loadAndShowNativeAd(
        activity: Activity,
        idAd: String,
        viewNativeAd: ViewGroup,
        renderer: NativeAdRenderer<T>,
        adCallBack: LoadAndShowAdCallBack
    ) {
        requireConsent(onFail = adCallBack::onAdFailed) {
            NativeAdManager.loadAndShowNativeAd(activity, idAd, viewNativeAd, renderer, adCallBack)
        }
    }

    fun <T : ViewBinding> loadAndShowNativeAdFullScreen(
        activity: Activity,
        idNativeAd: String,
        viewNativeAd: ViewGroup,
        renderer: NativeAdRenderer<T>,
        mediaAspectRatio: Int,
        adCallBack: LoadAndShowAdCallBack
    ) {
        requireConsent(onFail = adCallBack::onAdFailed) {
            NativeAdManager.loadAndShowNativeAdFullScreen(
                activity,
                idNativeAd,
                viewNativeAd,
                renderer,
                mediaAspectRatio,
                adCallBack
            )
        }
    }

    fun loadNativeAdFullScreen(
        context: Context,
        idAd: String,
        mediaAspectRatio: Int,
        adCallBack: LoadAdCallBack
    ) {
        requireConsent(onFail = adCallBack::onAdFailed) {
            NativeAdManager.loadNativeAdFullScreen(context, idAd, mediaAspectRatio, adCallBack)
        }
    }

    @JvmStatic
    fun <T : ViewBinding> showNativeAdFullScreen(
        activity: Activity,
        idAd: String,
        viewNativeAd: ViewGroup,
        renderer: NativeAdRenderer<T>,
        adCallBack: ShowAdCallBack

    ) {
        requireConsent(onFail = adCallBack::onAdFailed) {
            NativeAdManager.showNativeAdFullScreen(activity, idAd, viewNativeAd, renderer, adCallBack)
        }
    }


    @JvmStatic
    fun loadInterstitialAd(
        activity: Context,
        idAd: String,
        adLoadCallback: LoadAdCallBack
    ) {
        requireConsent(onFail = adLoadCallback::onAdFailed) {
            InterstitialAdManager.loadInterstitialAd(activity, idAd, adLoadCallback)
        }
    }

    @JvmStatic
    fun showInterstitialAd(
        activity: Activity,
        idAd: String,
        adCallback: ShowAdCallBack,
    ) {
        requireConsent(onFail = adCallback::onAdFailed) {
            InterstitialAdManager.showInterstitialAd(activity, idAd, adCallback)
        }
    }

    fun loadAndShowInterstitialAd(
        activity: Activity,
        idAd: String,
        adCallback: LoadAndShowAdCallBack
    ) {
        requireConsent(onFail = adCallback::onAdFailed) {
            InterstitialAdManager.loadAndShowInterstitialAd(activity, idAd, adCallback)
        }
    }

    fun loadAndShowInterstitialAdWithoutLoadingScreen(
        activity: Activity,
        idAd: String,
        adCallback: LoadAndShowAdCallBack
    ) {
        requireConsent(onFail = adCallback::onAdFailed) {
            InterstitialAdManager.loadAndShowInterstitialAdWithoutLoadingScreen(activity, idAd, adCallback)
        }
    }

    fun loadAndShowRewardAd(
        activity: Activity,
        admobId: String,
        adCallback: LoadAndShowRewardAdCallBack
    ) {
        requireConsent(onFail = adCallback::onAdFailed) {
            RewardAdManager.loadAndShowRewardAd(activity, admobId, adCallback)
        }
    }

    @JvmStatic
    fun loadInterReward(
        context: Context,
        idAd: String,
        adLoadCallback: LoadAdCallBack
    ) {
        requireConsent(onFail = adLoadCallback::onAdFailed) {
            RewardAdManager.loadInterReward(context, idAd, adLoadCallback)
        }
    }


    @JvmStatic
    fun showInterReward(
        activity: Activity,
        idAd: String,
        adCallback: ShowRewardAdCallBack
    ) {
        requireConsent(onFail = adCallback::onAdFailed) {
            RewardAdManager.showInterReward(activity, idAd, adCallback)
        }
    }

    interface LoadAdCallBack {
        fun onAdLoaded()
        fun onAdFailed(error: AdErrorInfo)
        fun onAdClicked()
        fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String)
    }

    interface ShowAdCallBack {
        fun onAdShowed()
        fun onAdFailed(error: AdErrorInfo)
        fun onAdClosed()
        fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String)
    }

    interface LoadAndShowAdCallBack {
        fun onAdLoaded()
        fun onAdShowed()
        fun onAdFailed(error: AdErrorInfo)
        fun onAdClosed()
        fun onAdClicked()
        fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String)
    }

    interface ShowRewardAdCallBack {
        fun onAdShowed()
        fun onAdClosed()
        fun onAdEarned()
        fun onAdFailed(error: AdErrorInfo)
        fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String)
    }

    interface LoadAndShowRewardAdCallBack {
        fun onAdLoaded()
        fun onAdShowed()
        fun onAdFailed(error: AdErrorInfo)
        fun onAdClosed()
        fun onAdEarned()
        fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String)
    }
}
