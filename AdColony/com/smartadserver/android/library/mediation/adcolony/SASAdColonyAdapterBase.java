package com.smartadserver.android.library.mediation.adcolony;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

import com.adcolony.sdk.AdColony;
import com.adcolony.sdk.AdColonyAppOptions;
import com.smartadserver.android.library.mediation.SASMediationAdapter;

import java.util.Map;

/**
 * Mediation adapter base class that will handle initialization and GDPR for all AdColony adapters
 */
public class SASAdColonyAdapterBase {

    /**
     * Common configuration code for all formats
     */
    void configureAdRequest(@NonNull Activity activity, @NonNull String serverParametersString, @NonNull Map<String, Object> clientParameters) {

        // extract AdColony specific parameters
        String[] params = serverParametersString.split("/");
        String appID = params[0];
        String zoneID = params[1];

        AdColony.configure(activity, appID, zoneID);

        // GDPR consent
        boolean GDPRRequired = true; // applies by default

        // check Smart value
        final String GDPRApplies = (String)clientParameters.get(SASMediationAdapter.GDPR_APPLIES_KEY);

        // Due to the fact that AdColony is not IAB compliant, it does not accept IAB Consent String, but only a
        // binary consent status.
        // Smart advises app developers to store the binary consent in the 'Smart_advertisingConsentStatus' key
        // in NSUserDefault, therefore this adapter will retrieve it from this key.
        // Adapt the code below if your app don't follow this convention.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        final String smartConsentStatus = sharedPreferences.getString("Smart_advertisingConsentStatus", null);

        // check if GDPR does NOT apply
        if ("false".equalsIgnoreCase(GDPRApplies)) {
            GDPRRequired = false;
        }

        // apply AdColony app options with GDPR flags
        AdColonyAppOptions appOptions = new AdColonyAppOptions();
        appOptions.setGDPRRequired(GDPRRequired);
        appOptions.setGDPRConsentString(smartConsentStatus == null ? "0" : smartConsentStatus);
        AdColony.setAppOptions(appOptions);
    }
}
