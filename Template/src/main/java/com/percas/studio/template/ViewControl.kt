package com.percas.studio.template

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.transition.TransitionManager

object ViewControl {
    fun View.visible() {
        visibility = View.VISIBLE
    }

    fun View.gone() {
        visibility = View.GONE
    }

    fun View.invisible() {
        visibility = View.INVISIBLE
    }

    fun ViewGroup.actionAnimation() {
        TransitionManager.beginDelayedTransition(this)
    }

    fun View.setBackgroundColor(colorHex: String) {
        setBackgroundColor(Color.parseColor(colorHex))
    }

    fun View.setBackgroundColorFilter(colorHex: String) {
        backgroundTintList = if (colorHex.isBlank()) {
            null
        } else {
            ColorStateList.valueOf(Color.parseColor(colorHex))
        }
    }

    fun ImageView.setColorFilter(colorHex: String) {
        imageTintList = if (colorHex.isBlank()) {
            null
        } else {
            ColorStateList.valueOf(Color.parseColor(colorHex))
        }
    }


}