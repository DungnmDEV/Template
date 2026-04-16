package com.percas.studio.example

import android.app.Application
import com.percas.studio.template.admob.AdmobManager
import com.percas.studio.template.admob.AppOpenManager


class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AdmobManager.initAdmob(this, timeOut = 10000, true, isEnableAd = true)
        AppOpenManager.enableResumeMode(this, adUnitId = "", minIntervalMillis = 0L)
    }
}
