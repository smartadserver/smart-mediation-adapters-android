# Smart Mediation Adapters Android - Ogury

This repository contains all mediation adapters we officially support for Ogury Android SDK.

Please note that Ogury Optin video is the equivalent for Smart Rewarded Video Ads, and consequently the Optin video adapter will only fill ad requests for Smart rewarded video placements.

Besides, the Ogury Thumbnail format (floating video) having no counterpart in Smart Display SDK, we decided to associate it to a banner with a height of 0. This way, the banner (acting as the ad holder) will not be visible in the publisher's application, while the Ogury thumbnail will be displayed on the application's screen.
For that integration to work properly, it is mandatory that the SASBannerView component be resized according to the size of the banner ad received.

See https://documentation.smartadserver.com/displaySDK/android/integration/banner.html#adviewresize for more information on how to adapt banner size.
