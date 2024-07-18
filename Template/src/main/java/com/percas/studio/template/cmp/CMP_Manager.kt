package com.percas.studio.template.cmp

import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform

/**
 * The Google Mobile Ads SDK provides the User Messaging Platform (Google's IAB Certified consent
 * management platform) as one solution to capture consent for users in GDPR impacted countries.
 * This is an example and you can choose another consent management platform to capture consent.
 */
class CMP_Manager(private val activity: Activity) {
    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(activity)

    /** Interface definition for a callback to be invoked when consent gathering is complete. */
    fun interface OnConsentGatheringCompleteListener {
        fun consentGatheringComplete(error: FormError?)
    }

    /** Helper variable to determine if the app can request ads. */
    val canRequestAds: Boolean
        get() = consentInformation.canRequestAds()

    /** Helper variable to determine if the privacy options form is required. */
    val isPrivacyOptionsRequired: Boolean
        get() =
            consentInformation.privacyOptionsRequirementStatus ==
                    ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    /**
     * Helper method to call the UMP SDK methods to request consent information and load/show a
     * consent form if necessary.
     */
    fun gatherConsent(onConsentGatheringCompleteListener: OnConsentGatheringCompleteListener) {
        // For testing purposes, you can force a DebugGeography of EEA or NOT_EEA.
        val debugSettings =
            ConsentDebugSettings.Builder(activity)
                // .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                // Check your logcat output for the hashed device ID e.g.
                // "Use new ConsentDebugSettings.Builder().addTestDeviceHashedId("ABCDEF012345")" to use
                // the debug functionality.
                .addTestDeviceHashedId("TEST-DEVICE-HASHED-ID")
                .build()

        val params = ConsentRequestParameters.Builder().setTagForUnderAgeOfConsent(false)
            .setConsentDebugSettings(debugSettings).build()

        // Requesting an update to consent information should be called on every app launch.
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    onConsentGatheringCompleteListener.consentGatheringComplete(formError)
                    if (formError != null) {
                        Log.d("TAG123", "gatherConsent: 1")
                    } else {
                        Log.d("TAG123", "gatherConsent: 2")
                    }
                }
            },
            { requestConsentError ->
                Log.d("TAG123", "gatherConsent: 3")
                onConsentGatheringCompleteListener.consentGatheringComplete(requestConsentError)
            }
        )
    }

    fun loadAndShowConsent(onConsentGatheringCompleteListener: OnConsentGatheringCompleteListener) {
        // For testing purposes, you can force a DebugGeography of EEA or NOT_EEA.
        val debugSettings =
            ConsentDebugSettings.Builder(activity)
                // .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                // Check your logcat output for the hashed device ID e.g.
                // "Use new ConsentDebugSettings.Builder().addTestDeviceHashedId("ABCDEF012345")" to use
                // the debug functionality.
                .addTestDeviceHashedId("TEST-DEVICE-HASHED-ID")
                .build()

        val params = ConsentRequestParameters.Builder().setTagForUnderAgeOfConsent(false)
            .setConsentDebugSettings(debugSettings).build()

        // Requesting an update to consent information should be called on every app launch.
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadConsentForm(activity, {
                    UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
                        onConsentGatheringCompleteListener.consentGatheringComplete(formError)
                    }
                }, { formError ->
                    onConsentGatheringCompleteListener.consentGatheringComplete(formError)
                })
            },
            { requestConsentError ->
                onConsentGatheringCompleteListener.consentGatheringComplete(requestConsentError)
            }
        )
    }

    fun checkEnableShowCMP(onFinish: (result: Boolean) -> Unit) {
        val debugSettings =
            ConsentDebugSettings.Builder(activity)
                .addTestDeviceHashedId("TEST-DEVICE-HASHED-ID")
                .build()
        val params = ConsentRequestParameters.Builder().setTagForUnderAgeOfConsent(false)
            .setConsentDebugSettings(debugSettings).build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadConsentForm(activity, {
                    Log.d("TAG123", "checkLocaShowConsent: true")
                    onFinish.invoke(true)
                }, {
                    Log.d("TAG123", "checkLocaShowConsent: false")
                    onFinish.invoke(false)
                })
            },
            {
                Log.d("TAG123", "checkLocaShowConsent: 3")
            }
        )
    }
}