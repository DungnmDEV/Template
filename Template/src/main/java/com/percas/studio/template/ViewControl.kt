package com.percas.studio.template

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.transition.TransitionManager

object ViewControl {
    fun View.visible(){
        visibility = View.VISIBLE
    }

    fun View.gone(){
        visibility = View.GONE
    }
    fun View.invisible(){
        visibility = View.INVISIBLE
    }

    fun ViewGroup.actionAnimation(){
        TransitionManager.beginDelayedTransition(this)
    }


}