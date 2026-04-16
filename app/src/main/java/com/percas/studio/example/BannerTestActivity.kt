package com.percas.studio.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdValue
import com.percas.studio.example.databinding.ActivityBannerTestBinding
import com.percas.studio.template.admob.AdErrorInfo
import com.percas.studio.template.admob.AdmobManager

class BannerTestActivity : AppCompatActivity() {
    private val binding by lazy { ActivityBannerTestBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnShowBanner.setOnClickListener {
            AdmobManager.loadAndShowBannerAd(
                activity = this,
                idBannerAd = "",
                viewBannerAd = binding.flBanner,
                adCallBack = object : AdmobManager.LoadAndShowAdCallBack {
                    override fun onAdLoaded() {
                        binding.tvStatus.text = "Banner loaded"
                    }

                    override fun onAdShowed() {
                        binding.tvStatus.text = "Banner showed"
                        binding.line.visibility = android.view.View.VISIBLE
                    }

                    override fun onAdFailed(error: AdErrorInfo) {
                        binding.tvStatus.text = "Banner failed: ${error.code} - ${error.message}"
                        binding.line.visibility = android.view.View.GONE
                    }

                    override fun onAdClosed() {
                        binding.tvStatus.text = "Banner closed"
                    }

                    override fun onAdClicked() {
                        binding.tvStatus.text = "Banner clicked"
                    }

                    override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {
                        binding.tvStatus.text = "Banner paid: $adUnit / $mediationNetwork"
                    }
                }
            )
        }

        binding.btnShowCollapsible.setOnClickListener {
            AdmobManager.loadAndShowBannerCollapsibleAd(
                activity = this,
                idBannerCollapAd = "",
                isBottomCollapsible = true,
                viewBanner = binding.flBanner,
                adCallBack = object : AdmobManager.LoadAndShowAdCallBack {
                    override fun onAdLoaded() {
                        binding.tvStatus.text = "Collapsible banner loaded"
                    }

                    override fun onAdShowed() {
                        binding.tvStatus.text = "Collapsible banner showed"
                        binding.line.visibility = android.view.View.VISIBLE
                    }

                    override fun onAdFailed(error: AdErrorInfo) {
                        binding.tvStatus.text = "Collapsible failed: ${error.code} - ${error.message}"
                        binding.line.visibility = android.view.View.GONE
                    }

                    override fun onAdClosed() {
                        binding.tvStatus.text = "Collapsible banner closed"
                    }

                    override fun onAdClicked() {
                        binding.tvStatus.text = "Collapsible banner clicked"
                    }

                    override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {
                        binding.tvStatus.text = "Collapsible paid: $adUnit / $mediationNetwork"
                    }
                }
            )
        }
    }
}
