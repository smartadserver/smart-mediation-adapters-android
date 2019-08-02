# Smart Mediation Adapters Android

This repository contains all mediation adapters we officially support.

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

For instance you can import _InMobi_ and _Tapjoy_ like so:

    implementation('com.smartadserver.android.mediation:smart-display-sdk-with-inmobi:7.1.1.0')
    implementation('com.smartadserver.android.mediation:smart-display-sdk-with-vungle:7.1.1.0')

> **Note on version numbers:**
>
> The latest version is: **7.1.1.0**
>
> If you import several third party adapters using _Gradle_, you must use the **same version number for all of them**.
>
> The version number always correspond to the underlying _Smart Display SDK_ for its first three digits, then a technical version corresponding to the adapters.
> For instance, 7.0.3.1 will import the first technical release of the adapters with the _Smart Display SDK_ 7.0.3.

Available adapters are:

| Package name | Supported SDK version | Comments | Maven repository |
| ------------ | --------------------- | -------- | ---------------- |
| ```smart-display-sdk-with-adcolony``` | 3.3.8 | _n/a_ | ```maven { url 'https://adcolony.bintray.com/AdColony' }``` |
| ```smart-display-sdk-with-adincube``` | 2.6.3 | Cannot be installed alongside the other adapters | ```maven { url 'http://repository.adincube.com/maven'}``` |
| ```smart-display-sdk-with-applovin``` | 9.3.0 | _n/a_ | _n/a_ |
| ```smart-display-sdk-with-facebookaudiencenetwork``` | 4.99.1 | _n/a_ | _n/a_ |
| ```smart-display-sdk-with-googlemobileads``` | 17.1.3 | _n/a_ | _n/a_ |
| ```smart-display-sdk-with-inmobi``` | 7.2.0 | _n/a_ | _n/a_ |
| ```smart-display-sdk-with-mopub``` | 5.6.0 | _n/a_ | ```maven { url 'https://s3.amazonaws.com/moat-sdk-builds' }``` |
| ```smart-display-sdk-with-ogury``` | 3.0.13 | _Ogury_ AAR library **must be manually imported** | _n/a_ |
| ```smart-display-sdk-with-tapjoy``` | 12.0.0 | _n/a_ | _n/a_ |
| ```smart-display-sdk-with-vungle``` | 6.3.17 | _n/a_ | ```maven { url 'https://jitpack.io' }``` |

## Manual installation

You can still install the adapters manually if needed:

1. First make sure you have installed the __Smart Display SDK__. More information [here](http://documentation.smartadserver.com/DisplaySDK/android/gettingstarted.html).

2. Copy and paste the classes of the adapter(s) you need to your project sources. Note that some adapter classes have a base class, do not forget to copy it as well.

3. Make sure to integrate the SDK corresponding to the chosen adapter(s).

