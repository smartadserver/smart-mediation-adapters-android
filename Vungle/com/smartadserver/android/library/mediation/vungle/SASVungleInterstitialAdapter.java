package com.smartadserver.android.library.mediation.vungle;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.smartadserver.android.library.mediation.SASMediationInterstitialAdapter;
import com.smartadserver.android.library.mediation.SASMediationInterstitialAdapterListener;

import com.vungle.warren.AdConfig;
import com.vungle.warren.InitCallback;
import com.vungle.warren.LoadAdCallback;
import com.vungle.warren.PlayAdCallback;
import com.vungle.warren.Vungle;
import com.vungle.warren.error.VungleException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Mediation adapter class for Vungle Interstitial format
 */
public class SASVungleInterstitialAdapter implements SASMediationInterstitialAdapter {

    static private final String TAG = SASVungleInterstitialAdapter.class.getSimpleName();

    private String placementID = "";

    private PlayAdCallback vunglePlayAdCallback;

    /**
     * @param context                     the {@link android.content.Context} needed by the mediation SDK to make the ad request
     * @param serverParameterString       a String containing all needed parameters (as returned by Smart ad delivery)
     *                                    to make the mediation ad call
     * @param clientParameters            additional client-side parameters (user specific, like location)
     * @param interstitialAdapterListener the {@link SASMediationInterstitialAdapterListener} provided to
     *                                    this {@link com.smartadserver.android.library.mediation.SASMediationAdapter} to notify Smart SDK of events occurring
     */
    @Override
    public void requestInterstitialAd(@NonNull Context context, @NonNull String serverParameterString, @NonNull Map clientParameters, @NonNull final SASMediationInterstitialAdapterListener interstitialAdapterListener) {
        Log.d(TAG, "SASVungleInterstitialAdapter requestAd");

        // Instantiate Vungle load ad callback
        final LoadAdCallback vungleLoadAdCallback = new LoadAdCallback() {
            @Override
            public void onAdLoad(String s) {
                Log.d(TAG, "Vungle LoadAdCallback onAdLoad");
                interstitialAdapterListener.onInterstitialLoaded();
            }

            @Override
            public void onError(String s, Throwable throwable) {
                Log.d(TAG, "Vungle LoadadCallback onError");

                // check if the error is due to a no ad.
                boolean isNoAd = false;
                if (throwable instanceof VungleException) {
                    VungleException exception = (VungleException) throwable;
                    isNoAd = exception.getExceptionCode() == 1;
                }

                interstitialAdapterListener.adRequestFailed(throwable.getMessage(), isNoAd);
            }
        };

        // Intantiate Vungle play ad callback
        vunglePlayAdCallback = new PlayAdCallback() {
            @Override
            public void onAdStart(String s) {
                Log.d(TAG, "Vungle PlayAdCallback onAdStart");
                interstitialAdapterListener.onInterstitialShown();
            }

            @Override
            public void onAdEnd(String s, boolean completed, boolean isClicked) {
                Log.d(TAG, "Vungle PlayAdCallback onAdEnd");

                if (isClicked) {
                    interstitialAdapterListener.onAdClicked();
                }

                interstitialAdapterListener.onAdClosed();
            }

            @Override
            public void onError(String s, Throwable throwable) {
                Log.d(TAG, "Vungle PlayAdCallback onError");
                interstitialAdapterListener.onInterstitialFailedToShow(throwable.getMessage());
            }
        };

        // Retrieve placement info -- Here the serverParameterString is composed as "applicationID/placementID"
        String[] placementInfo = serverParameterString.split("/");

        // Check all placement info are correcty set
        if (placementInfo.length != 2) {
            interstitialAdapterListener.adRequestFailed("The Vungle applicationID and/or placementID is not correclty set.", false);
        }

        String applicationID = placementInfo[0];
        placementID = placementInfo[1];

        // GDPR related
        final String GDPRApplies = (String) clientParameters.get(GDPR_APPLIES_KEY);

        // Due to the fact that Vungle is not IAB compliant, it does not accept IAB Consent String, but only a
        // binary consent status.
        // Smart advises app developers to store the binary consent in the 'Smart_advertisingConsentStatus' key
        // in NSUserDefault, therefore this adapter will retrieve it from this key.
        // Adapt the code below if your app don't follow this convention.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final String consentStatus = sharedPreferences.getString("Smart_advertisingConsentStatus", null);


        //Need to init Vungle at every requestAd call, as the placement id can be different
        final List<String> placementList = Arrays.asList(placementID);
        Vungle.init(placementList, applicationID, context.getApplicationContext(), new InitCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Vungle InitCallback onSuccess");

                // handle GDPR
                if (GDPRApplies != null) {
                    // Smart determined GDPR applies or not
                    if (!("false".equalsIgnoreCase(GDPRApplies))) {
                        // get GDPR consent status
                        if (consentStatus != null) {
                            if (consentStatus.equals("1")) {
                                Vungle.updateConsentStatus(Vungle.Consent.OPTED_IN, "REPLACE_WITH_YOUR_CONSENT_POLICY_VERSION");
                            } else {
                                Vungle.updateConsentStatus(Vungle.Consent.OPTED_OUT, "REPLACE_WITH_YOUR_CONSENT_POLICY_VERSION");
                            }
                        }
                    }
                }

                Vungle.loadAd(placementID, vungleLoadAdCallback);
            }

            @Override
            public void onError(Throwable throwable) {
                Log.d(TAG, "Vungle InitCallback onError");
                interstitialAdapterListener.adRequestFailed(throwable.getMessage(), false);
            }

            @Override
            public void onAutoCacheAdAvailable(String s) {
                Log.d(TAG, "Vungle InitCallback onAutoCacheAdAvailable");
            }
        });
    }

    @Override
    public void showInterstitial() throws Exception {
        if (Vungle.canPlayAd(placementID)) {
            Vungle.playAd(placementID, new AdConfig(), vunglePlayAdCallback);
        }
    }

    @Override
    public void onDestroy() {
        vunglePlayAdCallback = null;
    }
}
