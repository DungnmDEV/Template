package com.percas.studio.template.rate

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.tv.TvContract.Channels.Logo
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import com.percas.studio.template.R
import com.percas.studio.template.databinding.DialogFeedbackBinding
import com.percas.studio.template.databinding.DialogRateAppBinding

class RateApp(private val activity: Activity, private val callBack: RateDialogCallback) {
    private var packageName = ""
    private var logoap = 0
    private var appName = ""

    fun setPackageName(packageName: String) {
        this.packageName = packageName
    }
    fun setLogoApp(logo: Int) {
        this.logoap = logo
    }

    fun setAppName(name : String){
        this.appName = name
    }
    fun showDialog() {
        if (packageName.isBlank()) {
            callBack.onError("Package Name is Null!")
            return
        }
        val dialog = Dialog(activity)
        val binding = DialogRateAppBinding.inflate(activity.layoutInflater)
        dialog.setContentView(binding.root)
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        binding.ratingBar.rating = 5f

        binding.btnMaybeLater.setOnClickListener {
            callBack.onMaybeLaterClicked()
            dialog.dismiss()
        }
        binding.btnRate.setOnClickListener {
            val numberRate = Math.round(binding.ratingBar.rating)
            callBack.onRateButtonClicked(numberRate)
            if (numberRate == 5) {
                openStoreForRating()
            }else{
                openDialogFeedBack(binding.ratingBar.rating)
            }
            dialog.dismiss()
        }

        dialog.setOnShowListener {
            callBack.onShowRateDialog()
        }
        dialog.setOnDismissListener {
            callBack.onDismissRateDialog()
        }

        dialog.show()
    }

    private fun openStoreForRating() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activity.startActivity(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activity.startActivity(intent)
        }
    }
    private fun openDialogFeedBack(rate: Float) {
        val dialog = Dialog(activity)
        val bindingDialog = DialogFeedbackBinding.inflate(activity.layoutInflater)
        dialog.setContentView(bindingDialog.root)

        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.BOTTOM)
        }

        if(logoap!=0){
            bindingDialog.logoAp.setImageResource(logoap)
            bindingDialog.logoAp.background = null
        }
        if(appName.isNotBlank()){
            bindingDialog.appname.setText(appName)
        }
        bindingDialog.dialogRatingRatingBar.rating = rate

        bindingDialog.body.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val newText = s.toString()
                bindingDialog.count.text = "${newText.length}/500"
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        bindingDialog.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        bindingDialog.btnSubmit.setOnClickListener {
            Toast.makeText(activity, "Your feedback has been sent!", Toast.LENGTH_LONG)
                .show()
            dialog.dismiss()
        }

        dialog.show()
    }
    interface RateDialogCallback {
        fun onShowRateDialog()
        fun onDismissRateDialog()
        fun onRateButtonClicked(numberStart: Int)
        fun onMaybeLaterClicked()
        fun onError(error: String)
    }
}