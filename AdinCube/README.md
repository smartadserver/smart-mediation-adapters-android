# Smart Mediation Adapters Android - AdinCube

## GDPR

_AdinCube_ requires you to forward user consent in order to be GDPR compliant. You have two solutions to do so:

1. forwarding your _IAB consent string_ & an _array of non IAB vendors consents_ to the adapters, so it can be forwarded automatically to _AdinCube_ SDK
2. using the _Ogury Choice Manager_ CMP

### Forwarding your IAB consent string

You can forward your _IAB consent string_ & an _array of non IAB vendors consents_ to the adapters by setting them on the _SASAdinCubeAdapterBase_ object.

For instance, you can call:

	SASAdinCubeAdapterBase.setIabString("BOeu-yuOk33yIAHABBENCb-AAAAo1rv___7__9_-____9uz7Ov_v_f__33e8779v_h_7_-___u_-3zd4u_1vf99yfm1-7ctr3tp_87uesm_Xur__59__3z3_9phP78k89r7337Ew4MA");
    SASAdinCubeAdapterBase.setNonIabVendorAcceptedArray(new String[] { "google-llc" });

Notes:
- You must update theses properties each time user consent is modified by your user (using your own CMP solution).
- **Your application must be whitelisted by Ogury!** Check the Ogury's documentation for more information.

### Using the Ogury Choice Manager CMP

If you are not whitelisted or if you don't have a CMP, you can use the _Ogury Choice Manager_ CMP. Check the documentation for more informations:

https://intelligentmonetization.ogury.co/dashboard/#/docs/android/objective-c?networks=26226be-26226be#ogury-choice-manager

_Ogury Choice Manager_ consent will be automatically forwarded to the _AdinCube_ SDK.
