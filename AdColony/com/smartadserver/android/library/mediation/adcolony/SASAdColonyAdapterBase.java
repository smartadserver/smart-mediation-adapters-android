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
    }
}
