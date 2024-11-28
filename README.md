# Smart Mediation Adapters Android

This repository contains all mediation adapters we officially support for the legacy __Smart Display SDK__ version 7.
For the mediation adapters compatible with the new __Equativ Display SDK__ 8, please refer to [this repository](https://github.com/smartadserver/equativ-display-sdk-mediation-adapters-android) instead.


## Automatic Gradle installation

You can install the __Smart Display SDK__, one or several mediation adapters and their related third party SDKs using _Gradle_.

First declare the _Smart_ repository in the main _build.gradle_ file of your project. Also add any other _Maven_ repository that might be required by a third party SDK (see table below).

    allprojects {
      repositories {
        google()
        jcenter()

        // add the Smart repository
        maven { url 'https://packagecloud.io/smartadserver/android/maven2' }

        // add other third party repositories if necessary (see table below)
        // …
      }
    }

Then in the _build.gradle_ of to your application module, you can now import the adapters you need. Any dependency will be automatically fetched (_Smart Display SDK_, third party SDK, …).

For instance you can import _InMobi_ and _Tapjoy_ as follows:

    implementation('com.smartadserver.android.mediation:smart-display-sdk-with-inmobi:7.25.1.0')
    implementation('com.smartadserver.android.mediation:smart-display-sdk-with-vungle:7.25.1.0')

> **Note on version numbers:**
>
> The latest version is: **7.25.1.0**
>
> If you import several third party adapters using _Gradle_, you must use the **same version number for all of them**.
>
> The version number always correspond to the underlying _Smart Display SDK_ for its first three digits, then a technical version corresponding to the adapters.
> For instance, 7.24.0.1 will import the first technical release of the adapters with the _Smart Display SDK_ 7.24.0.

Available adapters are:

| Package name | Supported SDK version | Comments | Maven repository |
| ------------ | --------------------- | -------- | ---------------- |
| ```smart-display-sdk-with-adcolony``` | 4.6.5 | _n/a_ | _n/a_ |
| ```smart-display-sdk-with-applovin``` | 9.14.12 | _n/a_ | _n/a_ |
| ```smart-display-sdk-with-googlemobileads``` | 23.1.0 | _n/a_ | _n/a_ |
| ```smart-display-sdk-with-inmobi``` | 10.0.1 | _n/a_ | _n/a_ |
| ```smart-display-sdk-with-ogury``` | 5.6.2 | _n/a_ | ```maven {url 'https://maven.ogury.co'}``` |
| ```smart-display-sdk-with-tapjoy``` | 12.8.1 | _n/a_ | ```maven { url 'https://sdk.tapjoy.com/' }``` |
| ```smart-display-sdk-with-vungle``` | 6.10.3 | _n/a_ | ```mavenCentral()``` |

## Manual installation

You can still install the adapters manually if needed:

1. First make sure you have installed the __Smart Display SDK__. More information [here](http://documentation.smartadserver.com/DisplaySDK/android/gettingstarted.html).

2. Copy and paste the classes of the adapter(s) you need to your project sources. Note that some adapter classes have a base class, do not forget to copy it as well. __Beware__ of the fact that the adapter classes are located in a folder structure consistent with their declared package names __and__ the class name sent in the mediation ad sent by __Smart__ delivery. For the whole mediation flow to work properly, you __must__ leave this folder structure untouched when copying it in your project. Typically, you should copy the com/ root folder containing the classes directly in one of the source folders of your Android project, for instance src/main/Java. If that com/ folder already exists, simply merge it with the one containing the adapters. Failing to do so will prevent the SDK from properly instantiating the adapters when it receives a mediation ad, and the ad call will then fail.

3. Make sure to integrate the SDK corresponding to the chosen adapter(s).
