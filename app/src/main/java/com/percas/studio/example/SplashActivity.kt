package com.percas.studio.example

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.MediaAspectRatio
import com.percas.studio.template.admob.AdmobManager
import com.percas.studio.template.admob.AppOpenAdsManager
import com.percas.studio.template.model.InterAdHolder
import com.percas.studio.template.model.NativeAdHolder
import com.percas.studio.template.model.RewardInterAdHolder

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private val TAG = "TAG ==="
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        loadNativeAd(this, Ads.nativeHolder)
        loadNativeAdFullScreen(this, Ads.nativeHolder2, MediaAspectRatio.PORTRAIT)
        loadInterstitialAd(this, Ads.interholder)
        loadInterRewardAd(this, Ads.interRewardHolder)


        val appOpenID = "ca-app-pub-3940256099942544/3419835294"
        val appOpenAdsManager = AppOpenAdsManager(this,appOpenID,
            timeOut = 10000, object : AppOpenAdsManager.AppOpenAdListener {
            override fun onAdClose() {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            }

            override fun onAdFail(error: String) {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            }

            override fun onAdPaid(adValue: AdValue, adUnitAds: String, mediationNetwork: String) {
            }
        })
        
        appOpenAdsManager.loadAndShowAoA()
    }
    private fun loadNativeAd(context: Context, nativeAdHolder: NativeAdHolder){
        AdmobManager.loadNativeAd(context, nativeAdHolder, object : AdmobManager.LoadAdCallBack{
            override fun onAdLoaded() {
                
            }

            override fun onAdFailed(error: String) {
                
            }

            override fun onAdClicked() {
                
            }

            override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {
                TODO("Not yet implemented")
            }


        })
    }
    
    private fun loadNativeAdFullScreen(context: Context, nativeAdHolder: NativeAdHolder, mediaAspectRatio: Int){
        AdmobManager.loadNativeAdFullScreen(context, nativeAdHolder, mediaAspectRatio, object : AdmobManager.LoadAdCallBack{
            override fun onAdLoaded() {
                
            }

            override fun onAdFailed(error: String) {
                
            }

            override fun onAdClicked() {
                
            }

            override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {
                
            }

        })
    }
    private fun loadInterstitialAd(context: Context, interAdHolder: InterAdHolder){
        AdmobManager.loadInterstitialAd(context, interAdHolder, object : AdmobManager.LoadAdCallBack{
            override fun onAdLoaded() {
            }

            override fun onAdFailed(error: String) {
            }

            override fun onAdClicked() {
            }

            override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {
            }
        })
    }
    private fun loadInterRewardAd(context: Context, rewardInterAdHolder: RewardInterAdHolder){
        AdmobManager.loadInterReward(context, rewardInterAdHolder, object : AdmobManager.LoadAdCallBack{
            override fun onAdLoaded() {
            }

            override fun onAdFailed(error: String) {
            }

            override fun onAdClicked() {
            }

            override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {
                
            }

        })
    }
}