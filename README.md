# Admob Library
[![](https://jitpack.io/v/dungvnhh98/AdmobLib.svg)](https://jitpack.io/#dungvnhh98/AdmobLib)
<h3 align="center">From Percas Studio by Dungvnhh98</h3>

<p align="left"> <a href="https://developer.android.com" target="_blank" rel="noreferrer"> <img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/android/android-original-wordmark.svg" alt="android" width="40" height="40"/> </a> <a href="https://www.java.com" target="_blank" rel="noreferrer"> <img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/java/java-original.svg" alt="java" width="40" height="40"/> </a> <a href="https://kotlinlang.org" target="_blank" rel="noreferrer"> <img src="https://www.vectorlogo.zone/logos/kotlinlang/kotlinlang-icon.svg" alt="kotlin" width="40" height="40"/> </a> </p>

Step1: Add it in your root build.gradle at the end of repositories:
```bash
dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url 'https://jitpack.io' }
		}
	}
```

Step 2: Add the dependency
```bash
          implementation 'com.google.android.gms:play-services-ads:{version}'
	        implementation 'com.github.dungvnhh98:AdmobLib:{version}'
```


Step 3: Add to AndroidManifest.xml
```bash
<!--    ID Ads Test:    ca-app-pub-3940256099942544~3347511713-->

<meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="{YourAdsID}" />
```

Step 4: Create MyApplication extend Application
```kotlin
class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()

    }
}
```
Step 5: Use MyApplication in AndroidManifest.xml
```bash
<application
        android:name=".MyApplication"
	........
</application>
```
Step 6: Init Admob 
```kotlin
class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        AdmobManager.initAdmob(this, timeOut = 10000, isAdsTest = true, isEnableAds = true) \\change isAdsTest = false when you use live Ads ID
    }
}
```


<h1>Now you can use Admob Library</h1>
<h1>Note: If your use test Ad ID, you can leave the your Ad ID blank</h1>

- AppResumeAdsManager:
```kotlin
class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        AdmobManager.initAdmob(this, timeOut = 10000, isAdsTest = true, isEnableAds = true)
        AppResumeAdsManager.getInstance().init(this, appOnresmeAdsId})
    }
}
```
- AppOpenAdsManager:
```kotlin
val appOpenAdsManager = AppOpenAdsManager(this,appOpenID,
            timeOut = 10000, object : AppOpenAdsManager.AppOpenAdListener {
            override fun onAdClose() {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            }

            override fun onAdFail(error: String) {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            }

            override fun onAdPaid(adValue: AdValue, adUnitAds: String) {
            }
        })
        
        appOpenAdsManager.loadAndShowAoA()
```

- Banner Ads:
```kotlin
//Load and show Banner Ad normal
    fun loadAndShowBanner(
        activity: Activity,
        idBannerAd: String,
        viewBannerAd: ViewGroup,
        viewLine: View
    ) {
        AdmobManager.loadAndShowBannerAd(
            activity,
            idBannerAd,
            viewBannerAd,
            object : AdmobManager.LoadAndShowAdCallBack {
                override fun onAdLoaded() {}

                override fun onAdShowed() {
                    viewBannerAd.visible()
                    viewLine.visible()
                }

                override fun onAdFailed(error: String) {
                    viewBannerAd.gone()
                    viewLine.gone()
                }

                override fun onAdClosed() {}

                override fun onAdClicked() {}

                override fun onAdPaid(adValue: AdValue, adUnit: String) {}

            })
    }

//Load and show Banner Collapsible Ad
    fun loadAndShowBannerCollapsibleAd(activity: Activity, idBannerCollapsible: String, isBottomCollapsible:Boolean, viewBannerCollapsibleAd: ViewGroup, viewLine: View) {
        AdmobManager.loadAndShowBannerCollapsibleAd(
            activity,
            idBannerCollapsible,
            isBottomCollapsible,
            viewBannerCollapsibleAd,
            object : AdmobManager.LoadAndShowAdCallBack {
                override fun onAdLoaded() {
                }

                override fun onAdShowed() {
                    
                }

                override fun onAdFailed(error: String) {
                    viewBannerCollapsibleAd.gone()
                    viewLine.gone()
                }

                override fun onAdClosed() {
                    
                }

                override fun onAdClicked() {
                    
                }

                override fun onAdPaid(adValue: AdValue, adUnit: String) {
                    
                }

            })
    }

```
- Native Ad:
```kotlin
// Load Native Ads before show it or use Load and Show Native Ads Function
// Use NativeAdHolder to hold Native ads id

    val nativeAdHolder = NativeAdHolder(idNativeAd)

    fun loadNativeAd(context: Context, nativeAdHolder: NativeAdHolder){
        AdmobManager.loadNativeAd(context, nativeAdHolder, object : AdmobManager.LoadAdCallBack{
            override fun onAdLoaded() {
                
            }

            override fun onAdFailed(error: String) {
                
            }

            override fun onAdClicked() {
                
            }

            override fun onAdPaid(adValue: AdValue, adUnit: String) {
                
            }

        })
    }

// After load Native Ad, you can show it on a ViewGroup
fun showNativeAd(activity: Activity, nativeAdHolder: NativeAdHolder, viewNativeAd: ViewGroup, layoutNativeFormat: Int, isNativeAdMedium: Boolean) {
        AdmobManager.showNativeAd(activity, nativeAdHolder, viewNativeAd, layoutNativeFormat, isNativeAdMedium, object : AdmobManager.ShowAdCallBack{
            override fun onAdShowed() {
            }

            override fun onAdFailed(error: String) {
                viewNativeAd.gone()
            }

            override fun onAdClosed() {
                
            }

            override fun onAdPaid(adValue: AdValue, adUnit: String) {
                
            }

        })
    }

//Use load and show Native ad Function
 fun loadAndShowNativeAds(activity: Activity, nativeAdHolder: NativeAdHolder, viewNativeAd: ViewGroup, layoutNativeAdFormat: Int, isNativeAdMedium: Boolean) {
        AdmobManager.loadAndShowNativeAd(activity, nativeAdHolder, viewNativeAd, layoutNativeAdFormat, isNativeAdMedium,
            object : AdmobManager.LoadAndShowAdCallBack {
                override fun onAdLoaded() {
                    
                }

                override fun onAdShowed() {
                    
                }

                override fun onAdFailed(error: String) {
                    viewNativeAd.gone()
                }

                override fun onAdClosed() {
                    
                }

                override fun onAdClicked() {
                    
                }

                override fun onAdPaid(adValue: AdValue, adUnit: String) {
                    
                }

            })
    }


//For Native Ad Full Screen
//Load native ad full screen
fun loadNativeAdFullScreen(context: Context, nativeAdHolder: NativeAdHolder, mediaAspectRatio: Int){
        AdmobManager.loadNativeAdFullScreen(context, nativeAdHolder, mediaAspectRatio, object : AdmobManager.LoadAdCallBack{
            override fun onAdLoaded() {
                
            }

            override fun onAdFailed(error: String) {
                
            }

            override fun onAdClicked() {
                
            }

            override fun onAdPaid(adValue: AdValue, adUnit: String) {
                
            }

        })
    }

//Show native ad full screen
 fun showNativeAdFullScreen(activity: Activity, nativeAdHolder: NativeAdHolder, viewNativeAd: ViewGroup, layoutNativeAdFormat: Int){
        AdmobManager.showNativeAdFullScreen(activity, nativeAdHolder, viewNativeAd, layoutNativeAdFormat, object : AdmobManager.ShowAdCallBack{
            override fun onAdShowed() {
                
            }

            override fun onAdFailed(error: String) {
                viewNativeAd.gone()
            }

            override fun onAdClosed() {
                
            }

            override fun onAdPaid(adValue: AdValue, adUnit: String) {
                
            }

        })
    }

//Load and show Native Ad full screen
    fun loadAndShowNativeFullScreen(activity: Activity, idNativeAd: String, viewNativeAd: ViewGroup, layoutNativeFormat: Int, mediaAspectRatio: Int){
        AdmobManager.loadAndShowNativeAdFullScreen(activity, idNativeAd, viewNativeAd, layoutNativeFormat, mediaAspectRatio, object :AdmobManager.LoadAndShowAdCallBack{
            override fun onAdLoaded() {
                
            }

            override fun onAdShowed() {
                
            }

            override fun onAdFailed(error: String) {
                viewNativeAd.gone()
            }

            override fun onAdClosed() {
                
            }

            override fun onAdClicked() {
                
            }

            override fun onAdPaid(adValue: AdValue, adUnit: String) {
                
            }

        })
    }
```
- Interstitial Ad:
```kotlin
//Load interstitial ad
    fun loadInterstitialAd(context: Context, interAdHolder: InterAdHolder){
        AdmobManager.loadInterstitialAd(context, interAdHolder, object : AdmobManager.LoadAdCallBack{
            override fun onAdLoaded() {
            }

            override fun onAdFailed(error: String) {
            }

            override fun onAdClicked() {
            }

            override fun onAdPaid(adValue: AdValue, adUnit: String) {
            }
        })
    }

//Show interstitial ad
    fun showInterstitialAd(activity: Activity, interAdHolder: InterAdHolder){
        AdmobManager.showInterstitialAd(activity, interAdHolder, object : AdmobManager.ShowAdCallBack{
            override fun onAdShowed() {
                
            }

            override fun onAdFailed(error: String) {
                startActivity(Intent(this, TargetActivity::java.class))
            }

            override fun onAdClosed() {
                startActivity(Intent(this, TargetActivity::java.class))
            }

            override fun onAdPaid(adValue: AdValue, adUnit: String) {
                
            }

        })
    }

//Load and show interstitial ad
    fun loadAndShowInterstitialAd(activity: Activity, interAdHolder: InterAdHolder){
        AdmobManager.loadAndShowInterstitialAd(activity, interAdHolder, object :AdmobManager.LoadAndShowAdCallBack{
            override fun onAdLoaded() {
                
            }

            override fun onAdShowed() {
                
            }

            override fun onAdFailed(error: String) {
                startActivity(Intent(this, TargetActivity::java.class))
            }

            override fun onAdClosed() {
                startActivity(Intent(this, TargetActivity::java.class))
            }

            override fun onAdClicked() {
                
            }

            override fun onAdPaid(adValue: AdValue, adUnit: String) {
                
            }

        })
    }

```
- Reward Ad:
```kotlin
//Load and show Reward Ad
   fun loadAndShowRewardAd(activity: Activity, idRewardAd: String){
        AdmobManager.loadAndShowRewardAd(activity, idRewardAd, object : AdmobManager.LoadAndShowRewardAdCallBack{
            override fun onAdLoaded() {
            }

            override fun onAdShowed() {
            }

            override fun onAdFailed(error: String) {
            }

            override fun onAdClosed() {
            }

            override fun onAdEarned() {
                Log.d(TAG, "onAdEarned: Collected reward!")
            }

            override fun onAdPaid(adValue: AdValue, adUnit: String) {
            }

        })
    }

//Load Reward Interstitial Ad
fun loadInterRewardAd(context: Context, rewardInterAdHolder: RewardInterAdHolder){
        AdmobManager.loadInterReward(context, rewardInterAdHolder, object : AdmobManager.LoadAdCallBack{
            override fun onAdLoaded() {
            }

            override fun onAdFailed(error: String) {
            }

            override fun onAdClicked() {
            }

            override fun onAdPaid(adValue: AdValue, adUnit: String) {
                
            }

        })
    }

//Show Reward Interstitial Ad
fun showRewardInterAd(activity: Activity, rewardInterAdHolder: RewardInterAdHolder){
        AdmobManager.showInterReward(activity, rewardInterAdHolder, object : AdmobManager.ShowRewardAdCallBack{
            override fun onAdShowed() {
                
            }

            override fun onAdClosed() {
            }

            override fun onAdEarned() {
                Log.d(TAG, "onAdEarned: Collected reward!")
            }

            override fun onAdFailed(error: String) {
            }

            override fun onAdPaid(adValue: AdValue, adUnit: String) {
            }

        })
    }
```

# Adjust Manager:
## Step 1: Add Adjust to your project
 
 Visit the following link and download the latest version of the 2 files ARR and JAR: https://github.com/adjust/android_sdk/releases
 <br>
 In the directory {your_project_name}/app, create a libs directory and add the 2 downloaded files to this directory
<br>
 Open your app-level build.gradle file and add the following, in their respective sections:
 ```bash
android {
	defaultConfig {
	ndk.abiFilters 'armeabi-v7a','arm64-v8a','x86','x86_64'
	...
	}

	...
}

dependencies {
	...
	implementation files('libs/adjust-lib.aar')
}
```
 Add to the app's proguard-rules.pro file:
 ```bash
-keep class com.adjust.sdk.** { *; }
-keep class com.google.android.gms.common.ConnectionResult {
   int SUCCESS;
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient {
   com.google.android.gms.ads.identifier.AdvertisingIdClient$Info getAdvertisingIdInfo(android.content.Context);
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {
   java.lang.String getId();
   boolean isLimitAdTrackingEnabled();
}
-keep public class com.android.installreferrer.** { *; }
```
 Add to the AndroidManifest.xml:
 ```bash
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission  android:name="com.google.android.gms.permission.AD_ID"/>

<application>
	...
	<receiver
	   android:name="com.adjust.sdk.AdjustReferrerReceiver"
   	   android:exported="true"
 	   android:permission="android.permission.INSTALL_PACKAGES">
	   <intent-filter>
  	     <action android:name="com.android.vending.INSTALL_REFERRER" />
	   </intent-filter>
	</receiver>
</application>
```
## Step 2: Init Adjust
- In MyApplication:
```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ...
	
        AdjustManager.initAdjust(this, {your_app_token}, isTestAdjust) 
    }
}
//In your release version, change isTestAdjust to falsefalse
```
## Step 3: Post revenue to Adjust

In the `onAdPaid` event:

```kotlin
override fun onAdPaid(adValue: AdValue, adUnit: String) {
    AdjustManager.postRevenue(adValue, adUnit)
}
```
# Firebase Instruction
## Step 1: Add Firebase to your project
Download and save file google-services.json to file **app**
In Android Studio: **Tools** > **Firebase** > click and add SDK of **Analytics**, **Crashlytics**, **Cloud Messaging**, **Remote Config**
## Step 2: Remote config
You need a remote_config_defaults.xml file to store keys and default values, you can create new entries to store new keys
```xml
<?xml version="1.0" encoding="utf-8"?>
<defaultsMap>
    <entry>
        <key>test_ad</key>
        <value>true</value>
    </entry>
</defaultsMap>

```

```kotlin
fun initRemoteConfig(key: String, onCompleteListener: OnCompleteListener<Boolean>) {
        val mFirebaseRemoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings: FirebaseRemoteConfigSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600)
            .build()

        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings)
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val value = mFirebaseRemoteConfig.getBoolean(key)
                onCompleteListener.onComplete(task, value)
            } else {
                val defaultValue = mFirebaseRemoteConfig.getBoolean(key, R.xml.remote_config_defaults)
                onCompleteListener.onComplete(task, defaultValue)
            }
        }
    }

//Use in your activity:

initRemoteConfig(key, OnCompleteListener<Boolean> { task, value ->
            Log,d(TAG, value)
        })
```
## Step 3: Message Service
Create MessageService class:
```kotlin
class MessageService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.notification?.let {
            showNotification(it.title ?: "", it.body ?: "")
        }
    }

    private fun showNotification(title: String, message: String) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(this, "default_channel_id")
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.logo_app)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("default_channel_id", "Default Channel", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }
}
```
Add in your AndroidManifest.xml:
```xml
<application
	...
	<service
		android:name=".MessageService"
		android:exported="false">
		<intent-filter>
			<action android:name="com.google.firebase.MESSAGING_EVENT" />
		</intent-filter>
	</service>
</application>
```
# Rate App
```kotlin
 fun showRate(activity: Activity){
        val rateDialog = RateDialog(activity, object : RateDialog.RateDialogCallback{
            override fun onShowRateDialog() {

            }

            override fun onDismissRateDialog() {
            }

            override fun onRateButtonClicked(numberStart: Int) {
            }

            override fun onMaybeLaterClicked() {
            }

            override fun onError(error: String) {

            }

        })
        rateDialog.setTitle("Rate App")
        rateDialog.setContent("We need your review to improve the application")
        rateDialog.setTextButtonRate("Rate")
        rateDialog.setTextButtonMaybeLater("Maybe Later!")
        rateDialog.setPackageName(packageName)

        rateDialog.showDialog()
    }
```
# Other extention:
```kotlin
\\ Visible view
	View.visible()

\\ Invisible view
	View.invisible()

\\ Gone view
	View.gone()

\\ Add animation when change layout
	ViewGroup.actionAnimation()

\\ For example:
	viewContainer.actionAnimation() // viewContainer is the view containing the textview
	textView.gone()
```
