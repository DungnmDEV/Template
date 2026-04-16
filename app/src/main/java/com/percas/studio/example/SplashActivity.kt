package com.percas.studio.example

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdValue
import com.percas.studio.template.admob.AdErrorInfo
import com.percas.studio.template.admob.AdmobManager

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        AdmobManager.loadAndShowInterstitialAdOnSplash(
            activity = this,
            idAd = "",
            timeoutMillis = 10_000L,
            adCallback = object : AdmobManager.LoadAndShowAdCallBack {
                override fun onAdLoaded() = Unit

                override fun onAdShowed() = Unit

                override fun onAdFailed(error: AdErrorInfo) {
                    openMain()
                }

                override fun onAdClosed() {
                    openMain()
                }

                override fun onAdClicked() = Unit

                override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) = Unit
            },
        )
    }

    private fun openMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
