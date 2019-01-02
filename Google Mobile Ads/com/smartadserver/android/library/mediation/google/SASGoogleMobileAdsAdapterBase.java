package com.smartadserver.android.library.mediation.google;

import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.MobileAds;
import com.smartadserver.android.library.mediation.SASMediationAdapter;
import com.smartadserver.android.library.util.SASConfiguration;

import java.util.Map;

/**
 * Mediation adapter base class that will handle initialization and GDPR for all Google Mobile ads adapters
 */
class SASGoogleMobileAdsAdapterBase {

    // static flag for Google mobile ads SDK initialization
    protected static boolean initGoogleMobileAdsDone = false;

    /**
     * Common Google ad request configuration for all formats
     */
    protected AdRequest configureAdRequest(Context context, String serverParametersString, Map<String, String> clientParameters) {

        // check if GDPR applies
        final String GDPRApplies = clientParameters.get(SASMediationAdapter.GDPR_APPLIES_KEY);

        // Due to the fact that Google Mobile Ads is not IAB compliant, it does not accept IAB Consent String, but only a
        // binary consent status. The Smart Display SDK will retrieve it from the SharedPreferences with the
        // key "Smart_advertisingConsentStatus". Note that this is not an IAB requirement, so you have to set it by yourself.
        final String smartConsentStatus = SASConfiguration.getSharedInstance().getGDPRConsentStatus();

        boolean addNPAFlag = false;
        // check if GDPR does apply
        if ("true".equalsIgnoreCase(GDPRApplies)) {
            addNPAFlag = !("1".equals(smartConsentStatus));
        }

        // create Google ad request builder
        com.google.android.gms.ads.AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
        // Uncomment this line to see Google mobile test ads in your device simulator
        // adRequestBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);

        if (addNPAFlag) {
            // NO consent, enable google NPA (non personalized ads)
            Bundle extras = new Bundle();
            extras.putString("npa", "1");
            adRequestBuilder.addNetworkExtrasBundle(AdMobAdapter.class, extras);
        }

        // build ad request
        AdRequest adRequest = adRequestBuilder.build();

        // execute one time initialization code
        if (!initGoogleMobileAdsDone) {
            // reason behind the '|' separator is because Google mobile ads placement already use '/'
            String appID = serverParametersString.split("\\|")[0];
            // appID = "ca-app-pub-3940256099942544~3347511713"; // USE FOR TESTING ONLY (AdMob sample ID)
            MobileAds.initialize(context, appID);
            initGoogleMobileAdsDone = true;
        }

        return adRequest;

    }


    /**
     * Compute an ad size in DP given ad view size in pixels (in clientParameters)
     */
    protected AdSize getAppropriateAdSizeFromVisualSize(Context context, Map<String, String> clientParameters) {


        // retrieve ad view width and height from clientParameters
        int width = Integer.parseInt(clientParameters.get(SASMediationAdapter.AD_VIEW_WIDTH_KEY));
        int height = Integer.parseInt(clientParameters.get(SASMediationAdapter.AD_VIEW_HEIGHT_KEY));

        // get Android metrics
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);

        // compute ad view size in dp
        int adViewWidthDp = (int) (width / metrics.density);
        int adViewHeightDp = (int) (height / metrics.density);

        // return an google mobile ad size
        return new AdSize(adViewWidthDp, adViewHeightDp);
    }

}
