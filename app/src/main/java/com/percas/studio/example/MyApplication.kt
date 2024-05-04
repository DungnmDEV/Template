package com.percas.studio.example

import android.app.Application
import com.percas.studio.template.admob.AdmobManager
import com.percas.studio.template.admob.AppResumeAdsManager


class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AdmobManager.initAdmob(this, timeOut = 10000, isTestAd = true, isEnableAd = true)
        AppResumeAdsManager.getInstance().init(/* application = */ this,/* appOnresmeAdsId = */ "")
    }
}