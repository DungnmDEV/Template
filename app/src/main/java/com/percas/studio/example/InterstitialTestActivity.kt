package com.percas.studio.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdValue
import com.percas.studio.example.databinding.ActivityInterstitialTestBinding
import com.percas.studio.template.admob.AdErrorInfo
import com.percas.studio.template.admob.AdmobManager

class InterstitialTestActivity : AppCompatActivity() {
    private val binding by lazy { ActivityInterstitialTestBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnLoadInterstitial.setOnClickListener {
            AdmobManager.loadInterstitialAd(this, "", object : AdmobManager.LoadAdCallBack {
                override fun onAdLoaded() {
                    binding.tvStatus.text = "Interstitial loaded"
                }

                override fun onAdFailed(error: AdErrorInfo) {
                    binding.tvStatus.text = "Load failed: ${error.code} - ${error.message}"
                }

                override fun onAdClicked() {
                    binding.tvStatus.text = "Interstitial clicked"
                }

                override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {
                    binding.tvStatus.text = "Interstitial paid: $adUnit / $mediationNetwork"
                }
            })
        }

        binding.btnShowInterstitial.setOnClickListener {
            AdmobManager.showInterstitialAd(this, "", object : AdmobManager.ShowAdCallBack {
                override fun onAdShowed() {
                    binding.tvStatus.text = "Interstitial showed"
                }

                override fun onAdFailed(error: AdErrorInfo) {
                    binding.tvStatus.text = "Show failed: ${error.code} - ${error.message}"
                }

                override fun onAdClosed() {
                    binding.tvStatus.text = "Interstitial closed"
                }

                override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {
                    binding.tvStatus.text = "Interstitial paid: $adUnit / $mediationNetwork"
                }
            })
        }

        binding.btnLoadShowInterstitial.setOnClickListener {
            AdmobManager.loadAndShowInterstitialAd(this, "", object : AdmobManager.LoadAndShowAdCallBack {
                override fun onAdLoaded() {
                    binding.tvStatus.text = "Load+Show interstitial loaded"
                }

                override fun onAdShowed() {
                    binding.tvStatus.text = "Load+Show interstitial showed"
                }

                override fun onAdFailed(error: AdErrorInfo) {
                    binding.tvStatus.text = "Load+Show failed: ${error.code} - ${error.message}"
                }

                override fun onAdClosed() {
                    binding.tvStatus.text = "Load+Show interstitial closed"
                }

                override fun onAdClicked() {
                    binding.tvStatus.text = "Load+Show interstitial clicked"
                }

                override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {
                    binding.tvStatus.text = "Load+Show paid: $adUnit / $mediationNetwork"
                }
            })
        }

        binding.btnLoadShowInterstitialNoLoading.setOnClickListener {
            AdmobManager.loadAndShowInterstitialAdWithoutLoadingScreen(
                this,
                "",
                object : AdmobManager.LoadAndShowAdCallBack {
                    override fun onAdLoaded() {
                        binding.tvStatus.text = "No loading screen: loaded"
                    }

                    override fun onAdShowed() {
                        binding.tvStatus.text = "No loading screen: showed"
                    }

                    override fun onAdFailed(error: AdErrorInfo) {
                        binding.tvStatus.text = "No loading screen failed: ${error.code} - ${error.message}"
                    }

                    override fun onAdClosed() {
                        binding.tvStatus.text = "No loading screen: closed"
                    }

                    override fun onAdClicked() {
                        binding.tvStatus.text = "No loading screen: clicked"
                    }

                    override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {
                        binding.tvStatus.text = "No loading paid: $adUnit / $mediationNetwork"
                    }
                }
            )
        }
    }
}
