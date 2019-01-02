package com.smartadserver.android.library.mediation.applovin;

import android.content.Context;

import com.applovin.sdk.AppLovinPrivacySettings;
import com.applovin.sdk.AppLovinSdk;
import com.smartadserver.android.library.mediation.SASMediationAdapter;
import com.smartadserver.android.library.util.SASConfiguration;

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
            // binary consent status. The Smart Display SDK will retrieve it from the SharedPreferences with the
            // key "Smart_advertisingConsentStatus". Note that this is not an IAB requirement, so you have to set it by yourself.
            final String smartConsentStatus = SASConfiguration.getSharedInstance().getGDPRConsentStatus();
            userConsent = "1".equals(smartConsentStatus);
        }

        // pass consent info to AppLovin
        AppLovinPrivacySettings.setHasUserConsent(userConsent, context);

        // apply mute param
        sdk.getSettings().setMuted(true);

    }
}
