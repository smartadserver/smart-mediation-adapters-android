package com.smartadserver.android.library.mediation.inmobi;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;

import com.inmobi.sdk.InMobiSdk;
import com.smartadserver.android.library.util.SASLibraryInfo;
import com.smartadserver.android.library.util.location.SASLocationManager;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for InMobi adapter containing utility methods
 */
public class SASInMobiAdapterBase {

    private static final String TAG = "SASInMobiAdapterBase";

    private static final String GDPR_APPLIES_KEY = "gdprapplies";

    private static boolean initInMobiDone = false;

    // HashMap containing Smart AdServer custom parameters
    static HashMap<String, String> inMobiParametersMap;

    /**
     * init InMobi SDK once
     */
    protected static void initInMobiIfNecessary(Context context, String accountID, JSONObject JSONConsent) {
        if (!initInMobiDone) {
            InMobiSdk.setLogLevel(InMobiSdk.LogLevel.DEBUG);
            InMobiSdk.init(context, accountID, JSONConsent);

            inMobiParametersMap = new HashMap<String, String>();
            inMobiParametersMap.put("tp", "c_smartadserver");
            inMobiParametersMap.put("tp-ver", SASLibraryInfo.getSharedInstance().getVersion());

            initInMobiDone = true;
        }
    }

    /**
     * Returns a JSON object containing GDPR info as expected by InMobi
     */
    protected static JSONObject getJSONConsent(Context context, Map<String, String> clientParameters) {
        JSONObject JSONConsent = new JSONObject();

        try {
            // find if gdpr applies
            String GDPRApplies = ""; // unknown value by default
            String value = clientParameters.get(GDPR_APPLIES_KEY);
            if (value != null) {
                if ("true".equalsIgnoreCase(value)) {
                    GDPRApplies = "1";
                } else if ("false".equalsIgnoreCase(value)) {
                    GDPRApplies = "0";
                }
            }

            // store value in JSON
            JSONConsent.put("gdpr", GDPRApplies);

            // Due to the fact that InMobi is not IAB compliant, it does not accept IAB Consent String, but only a
            // binary consent status.
            // Smart advises app developers to store the binary consent in the 'Smart_advertisingConsentStatus' key
            // in NSUserDefault, therefore this adapter will retrieve it from this key.
            // Adapt the code below if your app don't follow this convention.
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            final String smartConsentStatus = sharedPreferences.getString("Smart_advertisingConsentStatus", null);

            if (smartConsentStatus != null) {
                // we have an info
                JSONConsent.put(InMobiSdk.IM_GDPR_CONSENT_AVAILABLE, smartConsentStatus);
            } else {
                // store empty string for "not available"
                JSONConsent.put(InMobiSdk.IM_GDPR_CONSENT_AVAILABLE, "");
            }
        } catch (JSONException ignored) {
        }

        return JSONConsent;
    }

    /**
     * Performs common InMobi ad request configuration
     */
    protected void configureAdRequest(Context context, String serverParameterString, Map<String, String> clientParameters) {

        // get GDPR consent tailored for inMobi
        JSONObject JSONConsent = getJSONConsent(context, clientParameters);

        initInMobiIfNecessary(context, getAccountId(serverParameterString), JSONConsent);

        InMobiSdk.updateGDPRConsent(JSONConsent);

        // pass geolocation if available
        Location location = null;
        location = SASLocationManager.getSharedInstance().getLocation();
        if (location != null) {
            InMobiSdk.setLocation(location);
        }
    }

    /**
     * Returns InMobi account ID from server parameters String
     */
    protected String getAccountId(String serverParameters) {
        return serverParameters.split("/")[0];
    }

    /**
     * Returns InMobi placement ID from server parameters String
     */
    protected long getPlacementId(String serverParameters) {

        // extract inMobi placement ID from server parameters
        String[] inMobiParams = serverParameters.split("/");

        long placementID = -1;
        try {
            placementID = Long.parseLong(inMobiParams[1]);
        } catch (Exception e) {
            Log.d(TAG, "InMobi Invalid Placement format");
        }

        return placementID;
    }
}
