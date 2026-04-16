package com.percas.studio.example

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.MediaAspectRatio
import com.percas.studio.example.databinding.ActivityMainBinding
import com.percas.studio.example.nativead.FullscreenNativeAdRenderer
import com.percas.studio.example.nativead.MediumNativeAdRenderer
import com.percas.studio.example.nativead.SmallNativeAdRenderer
import com.percas.studio.template.admob.AdmobManager
import com.percas.studio.template.admob.renderer.NativeAdRenderer
import com.percas.studio.template.model.InterAdHolder
import com.percas.studio.template.model.NativeAdHolder
import com.percas.studio.template.model.RewardInterAdHolder


class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val TAG = "TAG ==="
    private var isshow = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.btnLoadandshowCollap.isSelected = true
        binding.btnNativeSmall.isSelected = true
        binding.btnLoadAndShowreward.isSelected = true
        binding.btnLoadandshowinter.isSelected = true

        binding.flBanner.visibility = View.VISIBLE
        binding.line.visibility = View.VISIBLE
        loadAndShowBannerCollapsibleAd(this, "", true, binding.flBanner, binding.line)



        binding.btnBanner.setOnClickListener {
            binding.flNativeMedium.visibility = View.GONE
            binding.flNativeSmall.visibility = View.GONE
            binding.flBanner.visibility = View.VISIBLE
            binding.line.visibility = View.VISIBLE
            loadAndShowBanner(this, "", binding.flBanner, binding.line)
        }

        binding.btnLoadandshowCollap.setOnClickListener {
            binding.flNativeMedium.visibility = View.GONE
            binding.flNativeSmall.visibility = View.GONE
            binding.flBanner.visibility = View.VISIBLE
            binding.line.visibility = View.VISIBLE
            loadAndShowBannerCollapsibleAd(this, "", true, binding.flBanner, binding.line)
        }

        binding.btnNativeMedium.setOnClickListener {
            binding.line.visibility = View.GONE
            binding.flBanner.visibility = View.GONE
            binding.flNativeSmall.visibility = View.GONE
            showNativeAd(
                this,
                Ads.nativeHolder,
                binding.flNativeMedium,
                MediumNativeAdRenderer()
            )
        }
        binding.btnNativeSmall.setOnClickListener {
            binding.line.visibility = View.GONE
            binding.flBanner.visibility = View.GONE
            binding.flNativeMedium.visibility = View.GONE
            showNativeAd(
                this,
                Ads.nativeHolder,
                binding.flNativeSmall,
                SmallNativeAdRenderer()
            )
        }

        binding.btnLoadandshow.setOnClickListener {
            binding.line.visibility = View.GONE
            binding.flBanner.visibility = View.GONE
            binding.flNativeSmall.visibility = View.GONE
            binding.flNativeMedium.visibility = View.VISIBLE
            loadAndShowNativeAds(
                this,
                Ads.nativeHolder3,
                binding.flNativeMedium,
                MediumNativeAdRenderer()
            )
        }

        binding.btnShownativefullscreen.setOnClickListener {
            showNativeAdFullScreen(
                this,
                Ads.nativeHolder2,
                binding.flNativeFullscreen,
                FullscreenNativeAdRenderer()
            )
        }
        binding.btnLoadandshownativefullscreen.setOnClickListener {
            loadAndShowNativeFullScreen(
                this,
                "",
                binding.flNativeFullscreen,
                FullscreenNativeAdRenderer(),
                MediaAspectRatio.PORTRAIT
            )
        }
        binding.btnInterNoload.setOnClickListener { 
            AdmobManager.loadAndShowInterstitialAdWithoutLoadingScreen(this, Ads.interholder, object :AdmobManager.LoadAndShowAdCallBack{
                override fun onAdLoaded() {
                    
                }

                override fun onAdShowed() {
                    
                }

                override fun onAdFailed(error: String) {
                    
                }

                override fun onAdClosed() {
                    
                }

                override fun onAdClicked() {
                    
                }

                override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {
                    
                }

            })
        }

        binding.btnShowinter.setOnClickListener {
            showInterstitialAd(this, Ads.interholder)
        }

        binding.btnLoadandshowinter.setOnClickListener {
            loadAndShowInterstitialAd(this, Ads.interholder2)
        }

        binding.btnLoadAndShowreward.setOnClickListener {
            loadAndShowRewardAd(this, "")
        }
        binding.btnShowInterReward.setOnClickListener {
            showRewardInterAd(this, Ads.interRewardHolder)
        }
        binding.btnShowbanner.setOnClickListener {
            isshow = !isshow
            if (isshow) {
                binding.flBanner.visibility = View.VISIBLE
                binding.line.visibility = View.VISIBLE
            } else {
                binding.flBanner.visibility = View.GONE
                binding.line.visibility = View.GONE
            }
        }

    }

    fun loadAndShowBanner(
        activity: Activity,
        idBannerAd: String,
        viewBannerAd: ViewGroup,
        viewLine: View
    ) {
        AdmobManager.loadAndShowBannerAd(
            activity,
            idBannerAd,
            viewBannerAd,
            object : AdmobManager.LoadAndShowAdCallBack {
                override fun onAdLoaded() {}

                override fun onAdShowed() {
                    viewBannerAd.visibility = View.VISIBLE
                    viewLine.visibility = View.VISIBLE
                }

                override fun onAdFailed(error: String) {
                    viewBannerAd.visibility = View.GONE
                    viewLine.visibility = View.GONE
                }

                override fun onAdClosed() {}

                override fun onAdClicked() {}

                override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {}

            })
    }

    fun loadAndShowBannerCollapsibleAd(
        activity: Activity,
        idBannerCollapsible: String,
        isBottomCollapsible: Boolean,
        viewBannerCollapsibleAd: ViewGroup,
        viewLine: View
    ) {
        AdmobManager.loadAndShowBannerCollapsibleAd(
            activity,
            idBannerCollapsible,
            isBottomCollapsible,
            viewBannerCollapsibleAd,
            object : AdmobManager.LoadAndShowAdCallBack {
                override fun onAdLoaded() {
                }

                override fun onAdShowed() {

                }

                override fun onAdFailed(error: String) {
                    viewBannerCollapsibleAd.visibility = View.GONE
                    viewLine.visibility = View.GONE
                }

                override fun onAdClosed() {

                }

                override fun onAdClicked() {

                }

                override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {

                }

            })
    }

    fun <T : ViewBinding> showNativeAd(
        activity: Activity,
        nativeAdHolder: NativeAdHolder,
        viewNativeAd: ViewGroup,
        renderer: NativeAdRenderer<T>
    ) {
        AdmobManager.showNativeAd(
            activity,
            nativeAdHolder,
            viewNativeAd,
            renderer,
            object : AdmobManager.ShowAdCallBack {
                override fun onAdShowed() {
                    viewNativeAd.visibility = View.VISIBLE
                }

                override fun onAdFailed(error: String) {
                    viewNativeAd.visibility = View.GONE
                }

                override fun onAdClosed() {

                }

                override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {

                }

            })
    }


    fun <T : ViewBinding> loadAndShowNativeAds(
        activity: Activity,
        nativeAdHolder: NativeAdHolder,
        viewNativeAd: ViewGroup,
        renderer: NativeAdRenderer<T>
    ) {
        AdmobManager.loadAndShowNativeAd(activity,
            nativeAdHolder,
            viewNativeAd,
            renderer,
            object : AdmobManager.LoadAndShowAdCallBack {
                override fun onAdLoaded() {

                }

                override fun onAdShowed() {

                }

                override fun onAdFailed(error: String) {
                    viewNativeAd.visibility = View.GONE
                }

                override fun onAdClosed() {

                }

                override fun onAdClicked() {

                }

                override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {

                }

            })
    }

    fun <T : ViewBinding> showNativeAdFullScreen(
        activity: Activity,
        nativeAdHolder: NativeAdHolder,
        viewNativeAd: ViewGroup,
        renderer: NativeAdRenderer<T>
    ) {
        AdmobManager.showNativeAdFullScreen(
            activity,
            nativeAdHolder,
            viewNativeAd,
            renderer,
            object : AdmobManager.ShowAdCallBack {
                override fun onAdShowed() {
                    viewNativeAd.visibility = View.VISIBLE
                    Handler(Looper.getMainLooper()).postDelayed({
                        viewNativeAd.visibility = View.GONE
                    }, 10000)
                }

                override fun onAdFailed(error: String) {
                    viewNativeAd.visibility = View.GONE
                }

                override fun onAdClosed() {

                }

                override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {

                }

            })
    }

    fun <T : ViewBinding> loadAndShowNativeFullScreen(
        activity: Activity,
        idNativeAd: String,
        viewNativeAd: ViewGroup,
        renderer: NativeAdRenderer<T>,
        mediaAspectRatio: Int
    ) {
        AdmobManager.loadAndShowNativeAdFullScreen(
            activity,
            idNativeAd,
            viewNativeAd,
            renderer,
            mediaAspectRatio,
            object : AdmobManager.LoadAndShowAdCallBack {
                override fun onAdLoaded() {

                }

                override fun onAdShowed() {
                    viewNativeAd.visibility = View.VISIBLE
                    Handler(Looper.getMainLooper()).postDelayed({
                        viewNativeAd.visibility = View.GONE
                    }, 10000)
                }

                override fun onAdFailed(error: String) {
                    viewNativeAd.visibility = View.GONE
                }

                override fun onAdClosed() {

                }

                override fun onAdClicked() {

                }

                override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {

                }

            })
    }

    fun showInterstitialAd(activity: Activity, interAdHolder: InterAdHolder) {
        AdmobManager.showInterstitialAd(
            activity,
            interAdHolder,
            object : AdmobManager.ShowAdCallBack {
                override fun onAdShowed() {

                }

                override fun onAdFailed(error: String) {
                }

                override fun onAdClosed() {
                }

                override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {

                }

            })
    }

    fun loadAndShowInterstitialAd(activity: Activity, interAdHolder: InterAdHolder) {
        AdmobManager.loadAndShowInterstitialAd(
            activity,
            interAdHolder,
            object : AdmobManager.LoadAndShowAdCallBack {
                override fun onAdLoaded() {

                }

                override fun onAdShowed() {

                }

                override fun onAdFailed(error: String) {
                }

                override fun onAdClosed() {
                }

                override fun onAdClicked() {

                }

                override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {

                }

            })
    }

    fun loadAndShowRewardAd(activity: Activity, idRewardAd: String) {
        AdmobManager.loadAndShowRewardAd(
            activity,
            idRewardAd,
            object : AdmobManager.LoadAndShowRewardAdCallBack {
                override fun onAdLoaded() {
                }

                override fun onAdShowed() {
                }

                override fun onAdFailed(error: String) {
                }

                override fun onAdClosed() {
                }

                override fun onAdEarned() {
                    Log.d(TAG, "onAdEarned: Collected reward!")
                    Toast.makeText(this@MainActivity, "Collected Reward", Toast.LENGTH_LONG).show()
                }

                override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {
                }

            })
    }

    fun showRewardInterAd(activity: Activity, rewardInterAdHolder: RewardInterAdHolder) {
        AdmobManager.showInterReward(
            activity,
            rewardInterAdHolder,
            object : AdmobManager.ShowRewardAdCallBack {
                override fun onAdShowed() {

                }

                override fun onAdClosed() {
                }

                override fun onAdEarned() {
                    Log.d(TAG, "onAdEarned: Collected reward!")
                    Toast.makeText(this@MainActivity, "Collected Reward", Toast.LENGTH_LONG).show()
                }

                override fun onAdFailed(error: String) {
                }

                override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {
                }

            })
    }

}
