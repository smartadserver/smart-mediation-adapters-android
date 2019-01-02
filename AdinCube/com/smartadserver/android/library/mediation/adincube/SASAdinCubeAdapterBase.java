package com.smartadserver.android.library.mediation.adincube;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import com.adincube.sdk.AdinCube;
import com.smartadserver.android.library.mediation.SASMediationAdapter;
import com.smartadserver.android.library.util.SASConfiguration;

import java.util.Map;

/**
 * Mediation adapter base class that will handle initialization and GDPR for all AdinCube adapters
 */
class SASAdinCubeAdapterBase {

    // parameter key for required AdinCube application ID
    protected static final String APPLICATION_ID_KEY = "applicationID";

    // static flag for AdInCube SDK initialization
    protected static boolean initAdinCubeDone = false;

    // GDPR state variable
    boolean needToShowConsentDialog = false;
    static boolean consentWasShown = false;


    synchronized void showConsentDialogIfNeeded(@NonNull Context context) {
        if (context instanceof Activity && needToShowConsentDialog && !consentWasShown) {
            AdinCube.UserConsent.ask((Activity) context);
            consentWasShown = true;
        }
    }

    /**
     * Common configuration code for all formats
     */
    protected void configureAdRequest(Context context, String serverParametersString, Map<String, String> clientParameters) {

        // reset state variable
        needToShowConsentDialog = false;

        // handle GDPR consent
        handleGDPRConsent(context, clientParameters.get(SASMediationAdapter.GDPR_APPLIES_KEY));

        // one time init
        if (!initAdinCubeDone) {
            initAdinCubeDone = true;

            // init AdInCube -- Here the serverParametersString is the application ID
            AdinCube.setAppKey(serverParametersString);
        }
    }

    protected void handleGDPRConsent(@NonNull Context context, @NonNull String GDPRApplies) {
        // GDPR consent
        // Due to the fact that AdinCube is not IAB compliant, it does not accept IAB Consent String, but only a
        // binary consent status. The Smart Display SDK will retrieve it from the SharedPreferences with the
        // key "Smart_advertisingConsentStatus". Note that this is not an IAB requirement, so you have to set it by yourself.
        final String smartConsentStatus = SASConfiguration.getSharedInstance().getGDPRConsentStatus();

        if (smartConsentStatus != null) {
            // binary consent available

            // GDPR flag with false default value
            boolean GDPRAccepted = false;

            // check if GDPR does NOT apply
            if ("false".equalsIgnoreCase(GDPRApplies)) {
                GDPRAccepted = true;
            } else {
                // check if we have the consent if GDPR applies
                GDPRAccepted = "1".equals(smartConsentStatus);
            }

            // now pass "consent" to AdinCube
            if (GDPRAccepted) {
                AdinCube.UserConsent.setAccepted(context);
            } else {
                AdinCube.UserConsent.setDeclined(context);
            }

        } else {
            // AdinCube managed consent -> uncomment if you want to let AdinCube ask for the consent.
//                needToShowConsentDialog = true;
//                if (sasAdView instanceof SASInterstitialManager.InterstitialView) {
//                    // need to defer consent dialog just before interstitial or rewarded video show()
//                } else {
//                    // banner or native case, present immediately
//                    showConsentDialogIfNeeded(context);
//                }

            // fallback solution : do not present consent, assume declined (comment if you want Adincube to manage the consent)
            AdinCube.UserConsent.setDeclined(context);
        }
    }
}
