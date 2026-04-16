package com.percas.studio.template.cmp

import com.google.android.ump.ConsentDebugSettings

internal data class ConsentConfig(
    val tagForUnderAgeOfConsent: Boolean = false,
    val testDeviceHashedIds: List<String> = emptyList(),
    val debugGeography: Int? = null,
) {
    internal fun applyTo(builder: ConsentDebugSettings.Builder): ConsentDebugSettings.Builder {
        testDeviceHashedIds.forEach(builder::addTestDeviceHashedId)
        debugGeography?.let(builder::setDebugGeography)
        return builder
    }
}
