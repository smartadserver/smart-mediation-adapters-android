package com.smartadserver.android.library.mediation.google;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.MobileAds;

import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;

import com.smartadserver.android.library.mediation.SASMediationAdapter;

import java.util.Map;

/**
 * Mediation adapter base class that will handle initialization and GDPR for all Google Mobile ads adapters
 */
class SASGoogleMobileAdsAdapterBase {

    private static final String GMA_AD_MANAGER_KEY = "admanager";

    public enum GoogleMobileAds {
        NOT_INITIALIZED, ADMOB, AD_MANAGER
    }

    // static flag for Google mobile ads SDK initialization
    private static GoogleMobileAds GoogleMobileAdsInitStatus = GoogleMobileAds.NOT_INITIALIZED;

    /**
     * Init method for Google Mobile Ads to decide from which canal (Google AdMob or Ad Manager) ads should be requested
     */
    protected GoogleMobileAds initGoogleMobileAds(Context context, String serverParametersString) {
        // reason behind the '|' separator is because Google mobile ads placement already use '/'
        String appID = getAppID(serverParametersString);

        if (!GMA_AD_MANAGER_KEY.equals(appID)) { // check if the template corresponds to Google AdMob or Ad Manager
            if (GoogleMobileAds.NOT_INITIALIZED == GoogleMobileAdsInitStatus) {
                // appID = "ca-app-pub-3940256099942544~3347511713"; // USE FOR TESTING ONLY (AdMob sample ID)
                MobileAds.initialize(context, appID);
            }
            GoogleMobileAdsInitStatus = GoogleMobileAds.ADMOB;
        } else {
            GoogleMobileAdsInitStatus = GoogleMobileAds.AD_MANAGER;
        }

        return GoogleMobileAdsInitStatus;
    }

    /**
     * Utility method to get AppID from serverParametersString
     */
    protected String getAppID(String serverParametersString) {
        return serverParametersString.split("\\|")[0];
    }

    /**
     * Utility method to get AppUnitID from serverParametersString
     */
    protected String getAdUnitID(String serverParametersString) {
        String[] parameters = serverParametersString.split("\\|");
        if (parameters.length > 1) {
            return parameters[1];
        }
        return "";
    }

    /**
     * Common Google AdMob request configuration for all formats
     */
    protected AdRequest configureAdRequest(Context context, String serverParametersString, Map<String, String> clientParameters) {
        // create Google ad request builder
        AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
        // Uncomment this line to see Google mobile test ads in your device simulator
        // adRequestBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);

        Bundle extras = createExtrasBundleWithNPAIfNeeded(context, clientParameters);
        if (extras != null) {
            adRequestBuilder.addNetworkExtrasBundle(AdMobAdapter.class, extras);
        }

        return adRequestBuilder.build();
    }

    /**
     * Common Google Ad Manager publisher request configuration for all formats
     */
    protected PublisherAdRequest configurePublisherAdRequest(Context context, String serverParametersString, Map<String, String> clientParameters) {
        // create Google publisher ad request builder
        PublisherAdRequest.Builder publisherAdRequestBuilder = new PublisherAdRequest.Builder();
        // Uncomment this line to see Google mobile test ads in your device simulator
        // adRequestBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);

        Bundle extras = createExtrasBundleWithNPAIfNeeded(context, clientParameters);
        if (extras != null) {
            publisherAdRequestBuilder.addNetworkExtrasBundle(AdMobAdapter.class, extras);
        }

        return publisherAdRequestBuilder.build();
    }

    /**
     * Create an extras bundle with non-personalized ads flag if needed
     */
    private Bundle createExtrasBundleWithNPAIfNeeded(Context context, Map<String, String> clientParameters) {
        // check if GDPR applies
        final String GDPRApplies = clientParameters.get(SASMediationAdapter.GDPR_APPLIES_KEY);

        // Due to the fact that Google Mobile Ads is not IAB compliant, it does not accept IAB Consent String, but only a
        // binary consent status.
        // Smart advises app developers to store the binary consent in the 'Smart_advertisingConsentStatus' key
        // in NSUserDefault, therefore this adapter will retrieve it from this key.
        // Adapt the code below if your app don't follow this convention.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final String smartConsentStatus = sharedPreferences.getString("Smart_advertisingConsentStatus", null);

        // check if GDPR does apply
        if ("true".equalsIgnoreCase(GDPRApplies) && !("1".equals(smartConsentStatus))) {
            Bundle extras = new Bundle();
            extras.putString("npa", "1");
            return extras;
        }
        return null;
    }


}
