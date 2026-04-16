# Template Library
[![](https://jitpack.io/v/DungnmPercas/Template.svg)](https://jitpack.io/#DungnmPercas/Template)

Android AdMob library for app teams that need:

1. AdMob initialization
2. Banner ads
3. Native ads
4. Interstitial ads
5. Reward and rewarded interstitial ads
6. App open ads
7. App resume ads
8. Google UMP consent flow

This repository contains:

1. `Template`: the reusable ads library
2. `app`: a sample app that demonstrates integration

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

## AndroidManifest setup

Add your AdMob application ID in the host app manifest:

```xml
<application>
    <meta-data
        android:name="com.google.android.gms.ads.APPLICATION_ID"
        android:value="ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy" />
</application>
```

For testing, Google provides this sample app ID:

```xml
ca-app-pub-3940256099942544~3347511713
```

## Initialize the library

Create an `Application` class and initialize AdMob once at app startup.

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

Then register it in the manifest:

```xml
<application
    android:name=".MyApplication"
    ... />
```

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

Notes:

1. In test mode, the library will use Google's test app-open unit.
2. In production, pass your real app resume ad unit ID.
3. Use `timeWaitToShow` to avoid showing resume ads too frequently.

## App Open Ads

Use `AppOpenAdsManager` when you want to show an app open ad explicitly, for example on splash.

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

        override fun onAdPaid(adValue: AdValue, adUnitAds: String, mediationNetwork: String) {
        }
    }
)

appOpenAdsManager.loadAndShowAoA()
```

## Consent With UMP

Use `CMP_Manager` to request consent before showing ads in regions where consent is required.

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

## Banner Ads

### Normal banner

```kotlin
AdmobManager.loadAndShowBannerAd(
    activity = this,
    idBannerAd = "ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy",
    viewBannerAd = binding.bannerContainer,
    adCallBack = object : AdmobManager.LoadAndShowAdCallBack {
        override fun onAdLoaded() {}

        override fun onAdShowed() {
            binding.bannerContainer.visibility = View.VISIBLE
        }

        override fun onAdFailed(error: String) {
            binding.bannerContainer.visibility = View.GONE
        }

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

Native ads now use a `Renderer + ViewBinding` contract.

The library is responsible for:

1. loading native ads
2. caching native ads
3. showing loading placeholders
4. lifecycle and callbacks

The host app is responsible for:

1. defining the native ad layout
2. creating a `NativeAdRenderer`
3. binding views with ViewBinding

### 1. Create a holder

```kotlin
val nativeHolder = NativeAdHolder("ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy")
```

### 2. Load native ad first

```kotlin
AdmobManager.loadNativeAd(
    context = this,
    nativeHolder = nativeHolder,
    adCallBack = object : AdmobManager.LoadAdCallBack {
        override fun onAdLoaded() {}
        override fun onAdFailed(error: String) {}
        override fun onAdClicked() {}
        override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {}
    }
)
```

### 3. Create a renderer

Example renderer:

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

### 4. Show preloaded native ad

```kotlin
AdmobManager.showNativeAd(
    activity = this,
    nativeHolder = nativeHolder,
    viewNativeAd = binding.nativeContainer,
    renderer = MediumNativeAdRenderer(),
    adCallBack = object : AdmobManager.ShowAdCallBack {
        override fun onAdShowed() {
            binding.nativeContainer.visibility = View.VISIBLE
        }

        override fun onAdFailed(error: String) {
            binding.nativeContainer.visibility = View.GONE
        }

        override fun onAdClosed() {}

        override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {}
    }
)
```

### 5. Load and show native ad directly

```kotlin
AdmobManager.loadAndShowNativeAd(
    activity = this,
    nativeHolder = nativeHolder,
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
    nativeHolder = nativeHolder,
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
    nativeHolder = nativeHolder,
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
val interHolder = InterAdHolder("ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy")

AdmobManager.loadInterstitialAd(
    activity = this,
    interHolder = interHolder,
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
    interHolder = interHolder,
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
    interHolder = interHolder,
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
val rewardHolder = RewardInterAdHolder("ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy")

AdmobManager.loadInterReward(
    context = this,
    rewardInterAdHolder = rewardHolder,
    adCallBack = object : AdmobManager.LoadAdCallBack {
        override fun onAdLoaded() {}
        override fun onAdFailed(error: String) {}
        override fun onAdClicked() {}
        override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {}
    }
)

AdmobManager.showInterReward(
    activity = this,
    rewardInterAdHolder = rewardHolder,
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

## Test Mode And Production Mode

When calling `AdmobManager.initAdmob(...)`:

1. `isTestAd = true`
   - library uses Google test units where supported
2. `isTestAd = false`
   - you must pass real ad unit IDs

Example:

```kotlin
AdmobManager.initAdmob(
    context = this,
    timeOut = 10_000,
    isTestAd = false,
    isEnableAd = true,
)
```

## Current Native Ad API Summary

Native ads no longer use:

1. `layoutNativeFormat: Int`
2. `isNativeMedium: Boolean`
3. hardcoded `findViewById(...)` contract inside the library

They now use:

1. `NativeAdRenderer<T : ViewBinding>`
2. host-owned layout and binding logic
3. library-owned loading and ad lifecycle logic

## Sample App

The `app` module in this repository contains working examples for:

1. banner ads
2. native ads with custom renderers
3. interstitial ads
4. rewarded ads
5. app open ads
6. app resume ads
7. consent flow

Use it as the reference implementation for integration.
