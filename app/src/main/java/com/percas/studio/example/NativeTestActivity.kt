package com.percas.studio.example

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.MediaAspectRatio
import com.percas.studio.example.databinding.ActivityNativeTestBinding
import com.percas.studio.example.nativead.FullscreenNativeAdRenderer
import com.percas.studio.example.nativead.MediumNativeAdRenderer
import com.percas.studio.example.nativead.SmallNativeAdRenderer
import com.percas.studio.template.admob.AdErrorInfo
import com.percas.studio.template.admob.AdmobManager
import com.percas.studio.template.admob.renderer.NativeAdRenderer

class NativeTestActivity : AppCompatActivity() {
    private val binding by lazy { ActivityNativeTestBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnShowMedium.setOnClickListener {
            clearNativeViews()
            showNativeAd(binding.flNativeMedium, MediumNativeAdRenderer())
        }

        binding.btnShowSmall.setOnClickListener {
            clearNativeViews()
            showNativeAd(binding.flNativeSmall, SmallNativeAdRenderer())
        }

        binding.btnLoadShowMedium.setOnClickListener {
            clearNativeViews()
            loadAndShowNativeAd(binding.flNativeMedium, MediumNativeAdRenderer())
        }

        binding.btnShowFullscreen.setOnClickListener {
            clearNativeViews()
            showNativeFullScreen(binding.flNativeFullscreen, FullscreenNativeAdRenderer())
        }

        binding.btnLoadShowFullscreen.setOnClickListener {
            clearNativeViews()
            loadAndShowNativeFullScreen(binding.flNativeFullscreen, FullscreenNativeAdRenderer())
        }
    }

    private fun clearNativeViews() {
        binding.flNativeMedium.visibility = View.GONE
        binding.flNativeSmall.visibility = View.GONE
        binding.flNativeFullscreen.visibility = View.GONE
    }

    private fun <T : ViewBinding> showNativeAd(container: android.view.ViewGroup, renderer: NativeAdRenderer<T>) {
        AdmobManager.showNativeAd(
            activity = this,
            idAd = "",
            viewNativeAd = container,
            renderer = renderer,
            adCallBack = object : AdmobManager.ShowAdCallBack {
                override fun onAdShowed() {
                    container.visibility = View.VISIBLE
                    binding.tvStatus.text = "Native showed"
                }

                override fun onAdFailed(error: AdErrorInfo) {
                    container.visibility = View.GONE
                    binding.tvStatus.text = "Native show failed: ${error.code} - ${error.message}"
                }

                override fun onAdClosed() {
                    binding.tvStatus.text = "Native closed"
                }

                override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {
                    binding.tvStatus.text = "Native paid: $adUnit / $mediationNetwork"
                }
            }
        )
    }

    private fun <T : ViewBinding> loadAndShowNativeAd(
        container: android.view.ViewGroup,
        renderer: NativeAdRenderer<T>,
    ) {
        AdmobManager.loadAndShowNativeAd(
            activity = this,
            idAd = "",
            viewNativeAd = container,
            renderer = renderer,
            adCallBack = object : AdmobManager.LoadAndShowAdCallBack {
                override fun onAdLoaded() {
                    binding.tvStatus.text = "Native loaded"
                }

                override fun onAdShowed() {
                    container.visibility = View.VISIBLE
                    binding.tvStatus.text = "Native showed"
                }

                override fun onAdFailed(error: AdErrorInfo) {
                    container.visibility = View.GONE
                    binding.tvStatus.text = "Native failed: ${error.code} - ${error.message}"
                }

                override fun onAdClosed() {
                    binding.tvStatus.text = "Native closed"
                }

                override fun onAdClicked() {
                    binding.tvStatus.text = "Native clicked"
                }

                override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {
                    binding.tvStatus.text = "Native paid: $adUnit / $mediationNetwork"
                }
            }
        )
    }

    private fun <T : ViewBinding> showNativeFullScreen(
        container: android.view.ViewGroup,
        renderer: NativeAdRenderer<T>,
    ) {
        AdmobManager.showNativeAdFullScreen(
            activity = this,
            idAd = "",
            viewNativeAd = container,
            renderer = renderer,
            adCallBack = object : AdmobManager.ShowAdCallBack {
                override fun onAdShowed() {
                    container.visibility = View.VISIBLE
                    binding.tvStatus.text = "Native fullscreen showed"
                }

                override fun onAdFailed(error: AdErrorInfo) {
                    container.visibility = View.GONE
                    binding.tvStatus.text = "Native fullscreen failed: ${error.code} - ${error.message}"
                }

                override fun onAdClosed() {
                    binding.tvStatus.text = "Native fullscreen closed"
                }

                override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {
                    binding.tvStatus.text = "Native fullscreen paid: $adUnit / $mediationNetwork"
                }
            }
        )
    }

    private fun <T : ViewBinding> loadAndShowNativeFullScreen(
        container: android.view.ViewGroup,
        renderer: NativeAdRenderer<T>,
    ) {
        AdmobManager.loadAndShowNativeAdFullScreen(
            activity = this,
            idNativeAd = "",
            viewNativeAd = container,
            renderer = renderer,
            mediaAspectRatio = MediaAspectRatio.PORTRAIT,
            adCallBack = object : AdmobManager.LoadAndShowAdCallBack {
                override fun onAdLoaded() {
                    binding.tvStatus.text = "Native fullscreen loaded"
                }

                override fun onAdShowed() {
                    container.visibility = View.VISIBLE
                    binding.tvStatus.text = "Native fullscreen showed"
                }

                override fun onAdFailed(error: AdErrorInfo) {
                    container.visibility = View.GONE
                    binding.tvStatus.text = "Native fullscreen failed: ${error.code} - ${error.message}"
                }

                override fun onAdClosed() {
                    binding.tvStatus.text = "Native fullscreen closed"
                }

                override fun onAdClicked() {
                    binding.tvStatus.text = "Native fullscreen clicked"
                }

                override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {
                    binding.tvStatus.text = "Native fullscreen paid: $adUnit / $mediationNetwork"
                }
            }
        )
    }
}
