package com.percas.studio.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdValue
import com.percas.studio.example.databinding.ActivityRewardTestBinding
import com.percas.studio.template.admob.AdErrorInfo
import com.percas.studio.template.admob.AdmobManager

class RewardTestActivity : AppCompatActivity() {
    private val binding by lazy { ActivityRewardTestBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnLoadShowReward.setOnClickListener {
            AdmobManager.loadAndShowRewardAd(this, "", object : AdmobManager.LoadAndShowRewardAdCallBack {
                override fun onAdLoaded() {
                    binding.tvStatus.text = "Reward loaded"
                }

                override fun onAdShowed() {
                    binding.tvStatus.text = "Reward showed"
                }

                override fun onAdFailed(error: AdErrorInfo) {
                    binding.tvStatus.text = "Reward failed: ${error.code} - ${error.message}"
                }

                override fun onAdClosed() {
                    binding.tvStatus.text = "Reward closed"
                }

                override fun onAdEarned() {
                    binding.tvStatus.text = "Reward earned"
                }

                override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {
                    binding.tvStatus.text = "Reward paid: $adUnit / $mediationNetwork"
                }
            })
        }

        binding.btnLoadRewardInter.setOnClickListener {
            AdmobManager.loadInterReward(this, "", object : AdmobManager.LoadAdCallBack {
                override fun onAdLoaded() {
                    binding.tvStatus.text = "Reward interstitial loaded"
                }

                override fun onAdFailed(error: AdErrorInfo) {
                    binding.tvStatus.text = "Load reward interstitial failed: ${error.code} - ${error.message}"
                }

                override fun onAdClicked() {
                    binding.tvStatus.text = "Reward interstitial clicked"
                }

                override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {
                    binding.tvStatus.text = "Reward interstitial paid: $adUnit / $mediationNetwork"
                }
            })
        }

        binding.btnShowRewardInter.setOnClickListener {
            AdmobManager.showInterReward(this, "", object : AdmobManager.ShowRewardAdCallBack {
                override fun onAdShowed() {
                    binding.tvStatus.text = "Reward interstitial showed"
                }

                override fun onAdClosed() {
                    binding.tvStatus.text = "Reward interstitial closed"
                }

                override fun onAdEarned() {
                    binding.tvStatus.text = "Reward interstitial earned"
                }

                override fun onAdFailed(error: AdErrorInfo) {
                    binding.tvStatus.text = "Show reward interstitial failed: ${error.code} - ${error.message}"
                }

                override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {
                    binding.tvStatus.text = "Reward interstitial paid: $adUnit / $mediationNetwork"
                }
            })
        }
    }
}
