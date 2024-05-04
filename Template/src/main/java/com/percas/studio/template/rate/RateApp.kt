package com.percas.studio.template.rate

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.ViewGroup
import com.percas.studio.template.databinding.DialogRateAppBinding

class RateDialog(private val activity: Activity, private val callBack: RateDialogCallback) {

    private var title = ""
    private var content = ""
    private var packageName = ""
    private var maybeLater = ""
    private var rate = ""

    fun setPackageName(packageName: String) {
        this.packageName = packageName
    }

    fun setTitle(title: String) {
        this.title = title
    }

    fun setContent(content: String) {
        this.content = content
    }

    fun setTextButtonRate(rate: String) {
        this.rate = rate
    }

    fun setTextButtonMaybeLater(maybeLater: String) {
        this.maybeLater = maybeLater
    }

    fun showDialog() {
        if (packageName.isBlank()) {
            callBack.onError("Package Name is Null!")
            return
        }
        val dialog = Dialog(activity)
        val binding = DialogRateAppBinding.inflate(activity.layoutInflater)
        dialog.setContentView(binding.root)

        val window = dialog.window
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        if (title.isNotBlank()) {
            binding.tvTitle.text = title
        }
        if (content.isNotBlank()) {
            binding.tvContent.text = content
        }
        if (maybeLater.isNotBlank()) {
            binding.btnMaybeLater.text = maybeLater
        }
        if (rate.isNotBlank()) {
            binding.btnRate.text = rate
        }

        binding.btnMaybeLater.setOnClickListener {
            callBack.onMaybeLaterClicked()
            dialog.dismiss()
        }
        binding.btnRate.setOnClickListener {
            val numberRate = Math.round(binding.ratingBar.rating)
            callBack.onRateButtonClicked(numberRate)
            if (numberRate == 5) {
                openStoreForRating()
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

    interface RateDialogCallback {
        fun onShowRateDialog()
        fun onDismissRateDialog()
        fun onRateButtonClicked(numberStart: Int)
        fun onMaybeLaterClicked()
        fun onError(error: String)
    }
}