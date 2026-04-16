@file:Suppress("DEPRECATION")

package com.percas.studio.template.admob

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.view.Window
import android.widget.LinearLayout
import com.airbnb.lottie.LottieAnimationView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdRequest
import com.percas.studio.template.R

internal object AdmobCore {
    var isEnableAd = false
    var isOverlayAdShowing = false
    var isTestAd = true
    var shimmerFrameLayout: ShimmerFrameLayout? = null
    var adRequest: AdRequest? = null

    private var timeOut = 10000
    private var dialogFullScreen: Dialog? = null

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

    fun dismissAdDialog() {
        try {
            if (dialogFullScreen?.isShowing == true) {
                dialogFullScreen?.dismiss()
            }
        } catch (_: Exception) {
        }
    }

    fun dialogLoading(context: Activity) {
        dialogFullScreen = Dialog(context)
        dialogFullScreen?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogFullScreen?.setContentView(R.layout.dialog_full_screen)
        dialogFullScreen?.setCancelable(false)
        dialogFullScreen?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogFullScreen?.window!!.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        val img = dialogFullScreen?.findViewById<LottieAnimationView>(R.id.imageView3)
        img?.setAnimation(R.raw.gifloading)
        try {
            if (!context.isFinishing && dialogFullScreen?.isShowing == false) {
                dialogFullScreen?.show()
            }
        } catch (_: Exception) {
        }
    }
}
