package com.percas.studio.template.admob

data class AdmobConfig(
    val requestTimeoutMillis: Int = 10_000,
    val isTestAd: Boolean = true,
    val isEnableAd: Boolean = true,
)
