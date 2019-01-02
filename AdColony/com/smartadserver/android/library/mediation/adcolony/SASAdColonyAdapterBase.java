package com.smartadserver.android.library.mediation.adcolony;

import android.app.Activity;

import com.adcolony.sdk.AdColony;
import com.adcolony.sdk.AdColonyAppOptions;
import com.smartadserver.android.library.mediation.SASMediationAdapter;
import com.smartadserver.android.library.util.SASConfiguration;

import java.util.Map;

/**
 * Mediation adapter base class that will handle initialization and GDPR for all AdColony adapters
 */
public class SASAdColonyAdapterBase {

    /**
     * Common configuration code for all formats
     */
    void configureAdRequest(Activity activity, String serverParametersString, Map<String, String> clientParameters) {

        // extract AdColony specific parameters
        String[] params = serverParametersString.split("/");
        String appID = params[0];
        String zoneID = params[1];

        AdColony.configure(activity, appID, zoneID);

        // GDPR consent
        boolean GDPRRequired = true; // applies by default

        // check Smart value
        final String GDPRApplies = clientParameters.get(SASMediationAdapter.GDPR_APPLIES_KEY);

        // Due to the fact that AdColony is not IAB compliant, it does not accept IAB Consent String, but only a
        // binary consent status. The Smart Display SDK will retrieve it from the SharedPreferences with the
        // key "Smart_advertisingConsentStatus". Note that this is not an IAB requirement, so you have to set it by yourself.
        final String smartConsentStatus = SASConfiguration.getSharedInstance().getGDPRConsentStatus();
        // check if GDPR does NOT apply
        if ("false".equalsIgnoreCase(GDPRApplies)) {
            GDPRRequired = false;
        }

        // apply AdColony app options with GDPR flags
        AdColonyAppOptions appOptions = new AdColonyAppOptions();
        appOptions.setGDPRRequired(GDPRRequired);
        appOptions.setGDPRConsentString(smartConsentStatus);
        AdColony.setAppOptions(appOptions);
    }
}
