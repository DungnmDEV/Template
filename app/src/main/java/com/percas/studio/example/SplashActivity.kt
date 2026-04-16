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
import com.percas.studio.template.cmp.CMP_Manager

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private val TAG = "TAG ==="
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        loadNativeAd(this, "")
        loadNativeAdFullScreen(this, "", MediaAspectRatio.PORTRAIT)
        loadInterstitialAd(this, "")
        loadInterRewardAd(this, "")



        val cmpManager = CMP_Manager(this)
        cmpManager.checkEnableShowCMP {

        }

        val appOpenID = "ca-app-pub-3940256099942544/3419835294"
        val appOpenAdsManager = AppOpenAdsManager(this,appOpenID,
            timeOut = 10000, object : AppOpenAdsManager.AppOpenAdListener {
            override fun onAdClose() {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            }

            override fun onAdFail(error: String) {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            }

            override fun onAdPaid(adValue: AdValue, adUnitAds: String, mediationNetwork: String) {
            }
        })
        
        appOpenAdsManager.loadAndShowAoA()
    }
    private fun loadNativeAd(context: Context, idAd: String){
        AdmobManager.loadNativeAd(context, idAd, object : AdmobManager.LoadAdCallBack{
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
    
    private fun loadNativeAdFullScreen(context: Context, idAd: String, mediaAspectRatio: Int){
        AdmobManager.loadNativeAdFullScreen(context, idAd, mediaAspectRatio, object : AdmobManager.LoadAdCallBack{
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
    private fun loadInterstitialAd(context: Context, idAd: String){
        AdmobManager.loadInterstitialAd(context, idAd, object : AdmobManager.LoadAdCallBack{
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
    private fun loadInterRewardAd(context: Context, idAd: String){
        AdmobManager.loadInterReward(context, idAd, object : AdmobManager.LoadAdCallBack{
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
