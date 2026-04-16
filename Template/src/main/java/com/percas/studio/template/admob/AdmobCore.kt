@file:Suppress("DEPRECATION")

package com.percas.studio.template.admob

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import com.google.android.gms.ads.AdRequest

internal object AdmobCore {
    var isEnableAd = false
    var isOverlayAdShowing = false
    var isTestAd = true
    var adRequest: AdRequest? = null

    private var timeOut = 10000

    fun updateConfig(timeOut: Int, isTestAd: Boolean, isEnableAd: Boolean) {
        this.timeOut = if (timeOut > 0) timeOut else 10000
        this.isTestAd = isTestAd
        this.isEnableAd = isEnableAd
    }

    fun getTimeout(): Int = timeOut

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
