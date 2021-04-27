package com.smartadserver.android.library.mediation.facebook;

import android.content.Context;

import com.facebook.ads.AdSettings;

import java.util.Map;

/**
 * Mediation adapter base class that will handle SDK initialization for all Facebook adapters
 */
public class SASFacebookAdapterBase {


    // Tag for logging purposes
    static private final String TAG = SASFacebookAdapterBase.class.getSimpleName();

    // Smart Ad Server identification key for facebook mediation
    private static final String FB_SAS_MEDIATION_SERVICE_NAME = "Smart AdServer";

    // one-time init flag
    private static boolean initFacebookDone = false;

    /**
     * Common configuration code for all formats
     */
    void configureAdRequest(Context context, String serverParametersString, Map<String, Object> clientParameters) {

        // one time init
        if (!initFacebookDone) {
            initFacebookDone = true;

            //Set mediation service
            AdSettings.setMediationService(FB_SAS_MEDIATION_SERVICE_NAME);
        }
    }
}
