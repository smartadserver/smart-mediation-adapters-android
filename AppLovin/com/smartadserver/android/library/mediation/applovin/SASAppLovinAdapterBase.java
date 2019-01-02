package com.smartadserver.android.library.mediation.applovin;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.applovin.sdk.AppLovinPrivacySettings;
import com.applovin.sdk.AppLovinSdk;
import com.smartadserver.android.library.mediation.SASMediationAdapter;

import java.util.Map;

/**
 * Mediation adapter base class that will handle initialization and GDPR for all AppLovin adapters
 */
public class SASAppLovinAdapterBase {

    // static flag for AdMob SDK initialization
    protected static boolean initAppLovinDone = false;

    // AppLovin SDK instance
    static AppLovinSdk sdk;

    /**
     * Common AdMob ad request configuration for all formats
     */
    protected void configureAdRequest(Context context, String serverParametersString, Map<String, String> clientParameters) {

        // execute one time initialization code
        if (!initAppLovinDone) {
            AppLovinSdk.initializeSdk(context);
            // init AppLovin instances
            sdk = AppLovinSdk.getInstance(context.getApplicationContext());
            initAppLovinDone = true;
        }


        // GDPR consent
        boolean userConsent = false;

        // check if GDPR applies
        final String GDPRApplies = clientParameters.get(SASMediationAdapter.GDPR_APPLIES_KEY);
        // check if GDPR does NOT apply
        if ("false".equalsIgnoreCase(GDPRApplies)) {
            userConsent = true;
        } else {
            // Due to the fact that AppLovin is not IAB compliant, it does not accept IAB Consent String, but only a
            // binary consent status.
            // Smart advises app developers to store the binary consent in the 'Smart_advertisingConsentStatus' key
            // in NSUserDefault, therefore this adapter will retrieve it from this key.
            // Adapt the code below if your app don't follow this convention.
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            final String smartConsentStatus = sharedPreferences.getString("Smart_advertisingConsentStatus", null);
            userConsent = "1".equals(smartConsentStatus);
        }

        // pass consent info to AppLovin
        AppLovinPrivacySettings.setHasUserConsent(userConsent, context);

        // apply mute param
        sdk.getSettings().setMuted(true);

    }
}
