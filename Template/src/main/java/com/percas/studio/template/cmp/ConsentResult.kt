package com.percas.studio.template.cmp

import com.google.android.ump.FormError

data class ConsentResult(
    val canRequestAds: Boolean,
    val privacyOptionsRequired: Boolean,
    val formError: FormError? = null,
)
