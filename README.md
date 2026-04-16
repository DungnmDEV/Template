# Template Library (AdMob)

[![](https://jitpack.io/v/DungnmPercas/Template.svg)](https://jitpack.io/#DungnmPercas/Template)

Thư viện AdMob dùng chung cho các app Android trong công ty.

## Tính năng

- Khởi tạo AdMob tập trung qua `AdmobManager`
- Banner Ads (normal + collapsible)
- Native Ads (renderer + ViewBinding)
- Interstitial Ads
- Rewarded Ads
- Rewarded Interstitial Ads
- App Open Ads (splash)
- App Resume Ads (foreground)
- Consent (UMP) xử lý **nội bộ trong library**, app không cần tự gọi

## Yêu cầu

- `minSdk = 27`
- Kotlin Android project
- Đã khai báo App ID trong AndroidManifest

## Cài đặt

### 1) Thêm JitPack

`settings.gradle.kts`

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

### 2) Thêm dependency

```kotlin
dependencies {
    implementation("com.google.android.gms:play-services-ads:24.4.0")
    implementation("com.github.DungnmPercas:Template:<latest-version>")
}
```

## AndroidManifest

```xml
<application>
    <meta-data
        android:name="com.google.android.gms.ads.APPLICATION_ID"
        android:value="ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy" />
</application>
```

Google test App ID:

```xml
ca-app-pub-3940256099942544~3347511713
```

## Khởi tạo

Khởi tạo một lần trong `Application`:

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        AdmobManager.initAdmob(
            context = this,
            config = AdmobConfig(
                requestTimeoutMillis = 10_000,
                isTestAd = true,
                isEnableAd = true,
            )
        )
    }
}
```

Đăng ký trong manifest:

```xml
<application android:name=".MyApplication" ... />
```

## Consent (UMP)

- Consent được xử lý nội bộ trong library.
- App **không cần** import hay gọi `ConsentManager`.
- Khi chưa đủ điều kiện consent, library sẽ trả lỗi qua callback (`onAdFailed`).

## Sử dụng nhanh

### Banner

```kotlin
AdmobManager.loadAndShowBannerAd(
    activity = this,
    idBannerAd = "ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy",
    viewBannerAd = binding.bannerContainer,
    adCallBack = object : AdmobManager.LoadAndShowAdCallBack {
        override fun onAdLoaded() {}
        override fun onAdShowed() {}
        override fun onAdFailed(error: AdErrorInfo) {}
        override fun onAdClosed() {}
        override fun onAdClicked() {}
        override fun onAdPaid(adValue: AdValue, adUnit: String, mediationNetwork: String) {}
    }
)
```

### Native (load trước, show sau)

```kotlin
AdmobManager.loadNativeAd(this, "ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy", callback)
AdmobManager.showNativeAd(this, "ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy", binding.nativeContainer, renderer, showCallback)
```

#### Giải thích `renderer` cho Native Ad

`renderer` là nơi bạn định nghĩa cách map dữ liệu `NativeAd` vào layout của app. Library không tự biết layout của bạn, nên bắt buộc phải có `NativeAdRenderer<T : ViewBinding>`.

Vai trò từng hàm trong `NativeAdRenderer`:

- `inflate(...)`: inflate file layout native ad của bạn.
- `root(...)`: trả về `NativeAdView` gốc trong layout.
- `bind(...)`: gán dữ liệu (`headline`, `body`, `cta`, `icon`, `media`...) vào view.

Ví dụ renderer đầy đủ:

```kotlin
class MediumNativeAdRenderer : NativeAdRenderer<AdUnifiedMediumBinding> {
    override val loadingStyle = NativeAdLoadingStyle.MEDIUM

    override fun inflate(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
    ): AdUnifiedMediumBinding {
        return AdUnifiedMediumBinding.inflate(layoutInflater, parent, false)
    }

    override fun root(binding: AdUnifiedMediumBinding): NativeAdView = binding.root

    override fun bind(binding: AdUnifiedMediumBinding, nativeAd: NativeAd) {
        // 1) Map các asset view bắt buộc/khuyến nghị cho Google NativeAdView
        binding.root.mediaView = binding.adMedia
        binding.root.headlineView = binding.adHeadline
        binding.root.bodyView = binding.adBody
        binding.root.callToActionView = binding.adCallToAction
        binding.root.iconView = binding.adAppIcon

        // 2) Set dữ liệu
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

        // 3) Bắt buộc: báo cho SDK biết đã bind xong native ad
        binding.root.setNativeAd(nativeAd)
    }
}
```

Checklist để tránh lỗi Native:

- Layout root phải là `NativeAdView`.
- Trong `bind(...)`, luôn gọi `binding.root.setNativeAd(nativeAd)` ở cuối.
- Khi field null (`body`, `icon`, `cta`...), nhớ ẩn view tương ứng.
- `showNativeAd(...)` chỉ dùng khi ad đã được load trước; nếu chưa load dùng `loadAndShowNativeAd(...)`.
- Có thể tham khảo renderer mẫu trong app demo:
  - `app/src/main/java/com/percas/studio/example/nativead/MediumNativeAdRenderer.kt`
  - `app/src/main/java/com/percas/studio/example/nativead/SmallNativeAdRenderer.kt`
  - `app/src/main/java/com/percas/studio/example/nativead/FullscreenNativeAdRenderer.kt`

### Interstitial

```kotlin
AdmobManager.loadInterstitialAd(this, "ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy", loadCallback)
AdmobManager.showInterstitialAd(this, "ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy", showCallback)
```

### Reward

```kotlin
AdmobManager.loadAndShowRewardAd(this, "ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy", rewardCallback)
```

### App Open (splash)

```kotlin
AppOpenManager.showOnSlash(
    activity = this,
    adUnitId = "ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy",
    timeout = 10_000,
    listener = object : AppOpenManager.AppOpenAdListener {
        override fun onAdClose() {}
        override fun onAdFail(error: AdErrorInfo) {}
        override fun onAdPaid(adValue: AdValue, adUnitAds: String, mediationNetwork: String) {}
    }
)
```

### App Resume

```kotlin
AppOpenManager.enableResumeMode(
    application = this,
    adUnitId = "ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy",
    minIntervalMillis = 10_000L
)
```

## Callback contracts

- `AdmobManager.LoadAdCallBack`: load ad (chưa show)
- `AdmobManager.ShowAdCallBack`: show ad đã load
- `AdmobManager.LoadAndShowAdCallBack`: load + show
- `AdmobManager.ShowRewardAdCallBack`: show rewarded interstitial
- `AdmobManager.LoadAndShowRewardAdCallBack`: load + show rewarded

## Danh sách lỗi trả về

`AdErrorInfo` gồm:

- `code: AdErrorCode`
- `message: String`

`AdErrorCode` hiện có:

- `CONSENT_REQUIRED`: chưa đủ consent để request/show ads
- `CONSENT_FLOW_ERROR`: lỗi trong luồng consent UMP
- `ADS_DISABLED`: tắt ads bằng config (`isEnableAd = false`)
- `NO_INTERNET`: không có kết nối mạng
- `BLANK_AD_UNIT_ID`: ad unit id rỗng
- `TIMEOUT`: quá thời gian chờ load/show
- `AD_NOT_READY`: show khi ad chưa được load
- `ALREADY_LOADED`: ad đã có trong cache
- `ALREADY_SHOWING`: đang có ad khác hiển thị
- `NOT_INITIALIZED`: chưa init AdMob hợp lệ
- `LOAD_FAILED`: SDK load ad thất bại
- `SHOW_FAILED`: SDK show ad thất bại
- `BACKGROUND_STATE`: app không ở foreground hợp lệ để show
- `INVALID_STATE`: state không hợp lệ (ví dụ đang loading)
- `UNKNOWN`: lỗi chưa phân loại

## Test mode và production mode

Trong `AdmobConfig`:

- `isTestAd = true`: ưu tiên test ads
- `isTestAd = false`: dùng ad unit thật từ server/config

## Gợi ý tích hợp chuẩn

- Init trong `Application` một lần.
- Dùng callback `onAdFailed` để gom log/analytics theo `AdErrorCode`.
- Không gọi consent thủ công ở app; để library tự quản lý.
