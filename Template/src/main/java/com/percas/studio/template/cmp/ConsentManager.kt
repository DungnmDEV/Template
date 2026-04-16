package com.percas.studio.template.cmp

import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform

internal class ConsentManager(
    private val activity: Activity,
    private val config: ConsentConfig = ConsentConfig(),
) {
    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(activity)

    val canRequestAds: Boolean
        get() = consentInformation.canRequestAds()

    val isPrivacyOptionsRequired: Boolean
        get() = consentInformation.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    fun interface OnConsentResultListener {
        fun onConsentResult(result: ConsentResult)
    }

    fun gatherConsent(listener: OnConsentResultListener) {
        consentInformation.requestConsentInfoUpdate(
            activity,
            buildRequestParameters(),
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.e(TAG, "gatherConsent failed: ${formError.message}")
                    } else {
                        Log.d(TAG, "gatherConsent completed")
                    }
                    listener.onConsentResult(currentResult(formError))
                }
            },
            { requestConsentError ->
                Log.e(TAG, "requestConsentInfoUpdate failed: ${requestConsentError.message}")
                listener.onConsentResult(currentResult(requestConsentError))
            }
        )
    }

    fun showPrivacyOptionsForm(listener: OnConsentResultListener) {
        consentInformation.requestConsentInfoUpdate(
            activity,
            buildRequestParameters(),
            {
                UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
                    if (formError != null) {
                        Log.e(TAG, "showPrivacyOptionsForm failed: ${formError.message}")
                    }
                    listener.onConsentResult(currentResult(formError))
                }
            },
            { requestConsentError ->
                Log.e(TAG, "requestConsentInfoUpdate failed: ${requestConsentError.message}")
                listener.onConsentResult(currentResult(requestConsentError))
            }
        )
    }

    fun canShowConsentForm(onFinish: (result: Boolean) -> Unit) {
        consentInformation.requestConsentInfoUpdate(
            activity,
            buildRequestParameters(),
            {
                UserMessagingPlatform.loadConsentForm(
                    activity,
                    {
                        Log.d(TAG, "canShowConsentForm: true")
                        onFinish(true)
                    },
                    {
                        Log.d(TAG, "canShowConsentForm: false")
                        onFinish(false)
                    }
                )
            },
            {
                Log.e(TAG, "canShowConsentForm request failed: ${it.message}")
                onFinish(false)
            }
        )
    }

    private fun buildRequestParameters(): ConsentRequestParameters {
        val debugBuilder = ConsentDebugSettings.Builder(activity)
        config.applyTo(debugBuilder)
        return ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(config.tagForUnderAgeOfConsent)
            .setConsentDebugSettings(debugBuilder.build())
            .build()
    }

    private fun currentResult(formError: FormError?): ConsentResult = ConsentResult(
        canRequestAds = canRequestAds,
        privacyOptionsRequired = isPrivacyOptionsRequired,
        formError = formError,
    )

    companion object {
        private const val TAG = "ConsentManager"
    }
}
