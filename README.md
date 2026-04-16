# Template Library
[![](https://jitpack.io/v/DungnmPercas/Template.svg)](https://jitpack.io/#DungnmPercas/Template)

Android AdMob library for company apps.

Current scope:

1. AdMob initialization
2. Banner ads
3. Native ads with `Renderer + ViewBinding`
4. Interstitial ads
5. Rewarded ads
6. Rewarded interstitial ads
7. App open ads
8. App resume ads
9. Google UMP consent flow

This repository contains:

1. `Template`: reusable ads library
2. `app`: sample integration app

## Phase Status

This phase is complete with these architectural changes in place:

1. Public ad APIs no longer expose ad holders
2. Internal ad state and cache are managed inside the library
3. Native ads use a `NativeAdRenderer<T : ViewBinding>` contract
4. The old monolithic `AdmobManager` has been split into focused internal managers
5. Shared runtime state has been moved to `AdmobCore`

Current internal structure:

1. `AdmobManager`: public facade and callback contracts
2. `AdmobCore`: shared state, request config, timeout, dialog helpers, network helper
3. `BannerAdManager`
4. `NativeAdManager`
5. `InterstitialAdManager`
6. `RewardAdManager`

## Requirements

1. Android `minSdk 27`
2. Kotlin Android project
3. Google Mobile Ads SDK in the host app

## Installation

### 1. Add JitPack repository

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}
```

### 2. Add dependencies

```kotlin
dependencies {
    implementation("com.google.android.gms:play-services-ads:24.4.0")
    implementation("com.github.DungnmPercas:Template:1.1.6")
}
```

Use the latest library version published on JitPack.

## AndroidManifest Setup

Add your AdMob application ID in the host app manifest:

```xml
<application>
    <meta-data
        android:name="com.google.android.gms.ads.APPLICATION_ID"
        android:value="ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy" />
</application>
```

Google test app ID:

```xml
ca-app-pub-3940256099942544~3347511713
```

## Initialize The Library

Create an `Application` class and initialize AdMob once.

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        AdmobManager.initAdmob(
            context = this,
            timeOut = 10_000,
            isTestAd = true,
            isEnableAd = true,
        )
    }
}
```

Register it in the manifest:

```xml
<application
    android:name=".MyApplication"
    ... />
```

## Test Mode And Production Mode

When calling `AdmobManager.initAdmob(...)`:

1. `isTestAd = true`
   The library uses Google test units where supported.
2. `isTestAd = false`
   You must pass real ad unit IDs.

Example:

```kotlin
AdmobManager.initAdmob(
    context = this,
    timeOut = 10_000,
    isTestAd = false,
    isEnableAd = true,
)
```

## Consent With UMP

Use `CMP_Manager` before requesting ads in regions where consent is required.

```kotlin
val cmpManager = CMP_Manager(this)

cmpManager.gatherConsent { error ->
    if (error == null && cmpManager.canRequestAds) {
        // Safe point to request ads.
    }
}
```

Useful helpers:

1. `canRequestAds`
2. `isPrivacyOptionsRequired`
3. `loadAndShowConsent(...)`
4. `checkEnableShowCMP(...)`

## App Resume Ads

If you want ads to show automatically when the app returns to foreground:

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        AdmobManager.initAdmob(this, 10_000, true, true)

        AppResumeAdsManager.getInstance().init(
            application = this,
            appOnresmeAdsId = "ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy"
        )

        AppResumeAdsManager.getInstance().timeWaitToShow = 10_000L
    }
}
```

## App Open Ads

Use `AppOpenAdsManager` when you want to show an app open ad explicitly, such as on splash.

```kotlin
val appOpenAdsManager = AppOpenAdsManager(
    activity = this,
    appOpenID = "ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy",
    timeOut = 10_000,
    appOpenAdsListener = object : AppOpenAdsManager.AppOpenAdListener {
        override fun onAdClose() {
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }

        override fun onAdFail(error: String) {
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }

        override fun onAdPaid(adValue: AdValue, adUnitAds: String, mediationNetwork: String) {}
    }
)

appOpenAdsManager.loadAndShowAoA()
```

## Banner Ads

### Normal banner

```kotlin
AdmobManager.loadAndShowBannerAd(
    activity = this,
    idBannerAd = "ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy",
    viewBannerAd = binding.bannerContainer,
    adCallBack = object : AdmobManager.LoadAndShowAdCallBack {
        override fun onAdLoaded() {}
        override fun onAdShowed() {}
        override fun onAdFailed(error: String) {}
        override fun onAdClosed() {}
        override fun onAdClicked() {}
        override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {}
    }
)
```

### Collapsible banner

```kotlin
AdmobManager.loadAndShowBannerCollapsibleAd(
    activity = this,
    idBannerCollapAd = "ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy",
    isBottomCollapsible = true,
    viewBanner = binding.bannerContainer,
    adCallBack = object : AdmobManager.LoadAndShowAdCallBack {
        override fun onAdLoaded() {}
        override fun onAdShowed() {}
        override fun onAdFailed(error: String) {}
        override fun onAdClosed() {}
        override fun onAdClicked() {}
        override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {}
    }
)
```

## Native Ads

Native ads use a `Renderer + ViewBinding` contract.

The library is responsible for:

1. loading native ads
2. caching native ads internally
3. showing loading placeholders
4. lifecycle and callbacks

The host app is responsible for:

1. defining the native ad layout
2. creating a `NativeAdRenderer`
3. binding views with ViewBinding

### 1. Load native ad first

```kotlin
AdmobManager.loadNativeAd(
    context = this,
    idAd = "ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy",
    adCallBack = object : AdmobManager.LoadAdCallBack {
        override fun onAdLoaded() {}
        override fun onAdFailed(error: String) {}
        override fun onAdClicked() {}
        override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {}
    }
)
```

### 2. Create a renderer

```kotlin
class MediumNativeAdRenderer : NativeAdRenderer<AdUnifiedMediumBinding> {
    override val loadingStyle = NativeAdLoadingStyle.MEDIUM

    override fun inflate(
        layoutInflater: LayoutInflater,
        parent: ViewGroup
    ): AdUnifiedMediumBinding {
        return AdUnifiedMediumBinding.inflate(layoutInflater, parent, false)
    }

    override fun root(binding: AdUnifiedMediumBinding): NativeAdView = binding.root

    override fun bind(binding: AdUnifiedMediumBinding, nativeAd: NativeAd) {
        binding.root.mediaView = binding.adMedia
        binding.root.headlineView = binding.adHeadline
        binding.root.bodyView = binding.adBody
        binding.root.callToActionView = binding.adCallToAction
        binding.root.iconView = binding.adAppIcon
        binding.root.starRatingView = binding.adStars

        binding.adHeadline.text = nativeAd.headline
        binding.adMedia.mediaContent = nativeAd.mediaContent

        binding.adBody.apply {
            text = nativeAd.body
            visibility = if (nativeAd.body.isNullOrBlank()) View.INVISIBLE else View.VISIBLE
        }

        binding.adCallToAction.apply {
            text = nativeAd.callToAction
            visibility = if (nativeAd.callToAction.isNullOrBlank()) View.INVISIBLE else View.VISIBLE
        }

        if (nativeAd.icon == null) {
            binding.adAppIcon.visibility = View.GONE
        } else {
            binding.adAppIcon.setImageDrawable(nativeAd.icon!!.drawable)
            binding.adAppIcon.visibility = View.VISIBLE
        }

        if (nativeAd.starRating == null) {
            binding.adStars.visibility = View.GONE
        } else {
            binding.adStars.rating = nativeAd.starRating!!.toFloat()
            binding.adStars.visibility = View.VISIBLE
        }

        binding.root.setNativeAd(nativeAd)
    }
}
```

### 3. Show preloaded native ad

```kotlin
AdmobManager.showNativeAd(
    activity = this,
    idAd = "ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy",
    viewNativeAd = binding.nativeContainer,
    renderer = MediumNativeAdRenderer(),
    adCallBack = object : AdmobManager.ShowAdCallBack {
        override fun onAdShowed() {}
        override fun onAdFailed(error: String) {}
        override fun onAdClosed() {}
        override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {}
    }
)
```

### 4. Load and show native ad directly

```kotlin
AdmobManager.loadAndShowNativeAd(
    activity = this,
    idAd = "ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy",
    viewNativeAd = binding.nativeContainer,
    renderer = MediumNativeAdRenderer(),
    adCallBack = object : AdmobManager.LoadAndShowAdCallBack {
        override fun onAdLoaded() {}
        override fun onAdShowed() {}
        override fun onAdFailed(error: String) {}
        override fun onAdClosed() {}
        override fun onAdClicked() {}
        override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {}
    }
)
```

### Fullscreen native ad

Load:

```kotlin
AdmobManager.loadNativeAdFullScreen(
    context = this,
    idAd = "ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy",
    mediaAspectRatio = MediaAspectRatio.PORTRAIT,
    adCallBack = object : AdmobManager.LoadAdCallBack {
        override fun onAdLoaded() {}
        override fun onAdFailed(error: String) {}
        override fun onAdClicked() {}
        override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {}
    }
)
```

Show:

```kotlin
AdmobManager.showNativeAdFullScreen(
    activity = this,
    idAd = "ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy",
    viewNativeAd = binding.fullscreenNativeContainer,
    renderer = FullscreenNativeAdRenderer(),
    adCallBack = object : AdmobManager.ShowAdCallBack {
        override fun onAdShowed() {}
        override fun onAdFailed(error: String) {}
        override fun onAdClosed() {}
        override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {}
    }
)
```

Or load and show directly:

```kotlin
AdmobManager.loadAndShowNativeAdFullScreen(
    activity = this,
    idNativeAd = "ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy",
    viewNativeAd = binding.fullscreenNativeContainer,
    renderer = FullscreenNativeAdRenderer(),
    mediaAspectRatio = MediaAspectRatio.PORTRAIT,
    adCallBack = object : AdmobManager.LoadAndShowAdCallBack {
        override fun onAdLoaded() {}
        override fun onAdShowed() {}
        override fun onAdFailed(error: String) {}
        override fun onAdClosed() {}
        override fun onAdClicked() {}
        override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {}
    }
)
```

## Interstitial Ads

### Load interstitial

```kotlin
AdmobManager.loadInterstitialAd(
    activity = this,
    idAd = "ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy",
    adLoadCallback = object : AdmobManager.LoadAdCallBack {
        override fun onAdLoaded() {}
        override fun onAdFailed(error: String) {}
        override fun onAdClicked() {}
        override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {}
    }
)
```

### Show interstitial

```kotlin
AdmobManager.showInterstitialAd(
    activity = this,
    idAd = "ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy",
    adCallback = object : AdmobManager.ShowAdCallBack {
        override fun onAdShowed() {}
        override fun onAdFailed(error: String) {}
        override fun onAdClosed() {}
        override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {}
    }
)
```

### Load and show interstitial directly

```kotlin
AdmobManager.loadAndShowInterstitialAd(
    activity = this,
    idAd = "ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy",
    adCallback = object : AdmobManager.LoadAndShowAdCallBack {
        override fun onAdLoaded() {}
        override fun onAdShowed() {}
        override fun onAdFailed(error: String) {}
        override fun onAdClosed() {}
        override fun onAdClicked() {}
        override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {}
    }
)
```

## Reward Ads

### Load and show rewarded ad

```kotlin
AdmobManager.loadAndShowRewardAd(
    activity = this,
    idRewardAd = "ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy",
    adCallback = object : AdmobManager.LoadAndShowRewardAdCallBack {
        override fun onAdLoaded() {}
        override fun onAdShowed() {}
        override fun onAdFailed(error: String) {}
        override fun onAdClosed() {}
        override fun onAdEarned() {}
        override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {}
    }
)
```

### Rewarded interstitial

```kotlin
AdmobManager.loadInterReward(
    context = this,
    idAd = "ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy",
    adCallBack = object : AdmobManager.LoadAdCallBack {
        override fun onAdLoaded() {}
        override fun onAdFailed(error: String) {}
        override fun onAdClicked() {}
        override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {}
    }
)

AdmobManager.showInterReward(
    activity = this,
    idAd = "ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy",
    adCallback = object : AdmobManager.ShowRewardAdCallBack {
        override fun onAdShowed() {}
        override fun onAdClosed() {}
        override fun onAdEarned() {}
        override fun onAdFailed(error: String) {}
        override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {}
    }
)
```

## Revenue Tracking

All major ad callbacks expose:

```kotlin
override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {
}
```

Use this callback to:

1. log revenue
2. forward data to analytics
3. post ad revenue to MMPs

## Native API Summary

Native ads no longer use:

1. holder objects in the public API
2. `layoutNativeFormat: Int`
3. `isNativeMedium: Boolean`
4. hardcoded `findViewById(...)` contracts inside the public integration surface

Native ads now use:

1. `idAd: String`
2. `NativeAdRenderer<T : ViewBinding>`
3. host-owned layout and binding logic
4. library-owned internal cache and lifecycle logic

## Sample App

The `app` module contains working examples for:

1. banner ads
2. native ads with custom renderers
3. interstitial ads
4. rewarded ads
5. app open ads
6. app resume ads
7. consent flow

Use it as the reference implementation for this phase.
