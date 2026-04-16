@file:Suppress("DEPRECATION")

package com.percas.studio.template.admob

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import com.google.android.gms.ads.AdRequest

internal object AdmobCore {
    enum class ConsentStatus {
        UNKNOWN,
        PENDING,
        GRANTED,
        DENIED,
        ERROR,
    }

    var isEnableAd = false
    var isOverlayAdShowing = false
    var isTestAd = true
    var adRequest: AdRequest? = null

    @Volatile
    var consentStatus: ConsentStatus = ConsentStatus.UNKNOWN
        private set

    @Volatile
    var consentMessage: String = "Consent has not been initialized"
        private set

    private var timeOut = 10000

    fun updateConfig(config: AdmobConfig) {
        timeOut = if (config.requestTimeoutMillis > 0) {
            config.requestTimeoutMillis
        } else {
            10000
        }
        isTestAd = config.isTestAd
        isEnableAd = config.isEnableAd
    }

    fun getTimeout(): Int = timeOut

    fun markConsentPending() {
        consentStatus = ConsentStatus.PENDING
        consentMessage = "Consent is being collected"
    }

    fun markConsentGranted() {
        consentStatus = ConsentStatus.GRANTED
        consentMessage = "Consent granted"
    }

    fun markConsentDenied(message: String) {
        consentStatus = ConsentStatus.DENIED
        consentMessage = message
    }

    fun markConsentError(message: String) {
        consentStatus = ConsentStatus.ERROR
        consentMessage = message
    }

    fun canRequestAdsByConsent(): Boolean = consentStatus == ConsentStatus.GRANTED

    fun initAdRequest(timeOut: Int) {
        adRequest = AdRequest.Builder()
            .setHttpTimeoutMillis(timeOut)
            .build()
    }

    fun Context.isNetworkConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        return cm?.activeNetworkInfo?.isConnected == true
    }
}
