package com.percas.studio.template.admob

enum class AdErrorCode {
    CONSENT_REQUIRED,
    CONSENT_FLOW_ERROR,
    ADS_DISABLED,
    NO_INTERNET,
    BLANK_AD_UNIT_ID,
    TIMEOUT,
    AD_NOT_READY,
    ALREADY_LOADED,
    ALREADY_SHOWING,
    NOT_INITIALIZED,
    LOAD_FAILED,
    SHOW_FAILED,
    BACKGROUND_STATE,
    INVALID_STATE,
    UNKNOWN,
}

data class AdErrorInfo(
    val code: AdErrorCode,
    val message: String,
)
