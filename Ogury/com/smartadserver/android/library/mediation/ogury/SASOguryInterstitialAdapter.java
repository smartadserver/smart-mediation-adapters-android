package com.smartadserver.android.library.mediation.ogury;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.smartadserver.android.library.mediation.SASMediationInterstitialAdapter;
import com.smartadserver.android.library.mediation.SASMediationInterstitialAdapterListener;


import java.util.Map;

import io.presage.Presage;
import io.presage.interstitial.PresageInterstitial;
import io.presage.interstitial.PresageInterstitialCallback;

/**
 * Mediation adapter class for Ogury interstitial ad format
 */
public class SASOguryInterstitialAdapter implements SASMediationInterstitialAdapter {

    static private final String TAG = SASOguryInterstitialAdapter.class.getSimpleName();

    private PresageInterstitial presageInterstitial;

    /**
     * @param context                     the {@link android.content.Context} needed by the mediation SDK to make the ad request
     * @param serverParametersString      a String containing all needed parameters (as returned by Smart ad delivery)
     *                                    to make the mediation ad call
     * @param clientParameters            additional client-side parameters (user specific, like location)
     * @param interstitialAdapterListener the {@link SASMediationInterstitialAdapterListener} provided to
     *                                    this {@link com.smartadserver.android.library.mediation.SASMediationAdapter} to notify Smart SDK of events occurring
     */
    @Override
    public void requestInterstitialAd(@NonNull Context context, @NonNull String serverParametersString, @NonNull Map<String, String> clientParameters, @NonNull final SASMediationInterstitialAdapterListener interstitialAdapterListener) {
        Log.d(TAG, "SASOguryInterstitialAdapter adRequest");

        if (!(context instanceof Activity)) {
            interstitialAdapterListener.adRequestFailed("Ogury ad mediation requires the context to be an Activity", false);
            return;
        }

        // Init the Ogury SDK ad each call, the API KEY can be different at each call
        Presage presageInstance = Presage.getInstance();
        presageInstance.start(serverParametersString, context); // Here serverParametersString is the apiKey

        // GDPR is handle by the Ogury SDK. No need to do anything.

        // Instantiate the Presage interstitial callback
        PresageInterstitialCallback presageInterstitialCallback = new PresageInterstitialCallback() {
            @Override
            public void onAdAvailable() {
                Log.d(TAG, "presageInterstitialCallback onAdAvailable");
            }

            @Override
            public void onAdNotAvailable() {
                Log.d(TAG, "presageInterstitialCallback onAdNotAvailable");
                interstitialAdapterListener.adRequestFailed("Ogury ad not available", true);
            }

            @Override
            public void onAdLoaded() {
                Log.d(TAG, "presageInterstitialCallback onAdLoaded");
                interstitialAdapterListener.onInterstitialLoaded();
            }

            @Override
            public void onAdNotLoaded() {
                Log.d(TAG, "presageInterstitialCallback onAdNotLoaded");
                interstitialAdapterListener.adRequestFailed("Ogury ad not loaded", false);
            }

            @Override
            public void onAdDisplayed() {
                Log.d(TAG, "presageInterstitialCallback onAdDisplayed");
                interstitialAdapterListener.onInterstitialShown();
            }

            @Override
            public void onAdClosed() {
                Log.d(TAG, "presageInterstitialCallback onAdClosed");
                interstitialAdapterListener.onAdClosed();
            }

            @Override
            public void onAdError(int i) {
                Log.d(TAG, "presageInterstitialCallback onAdError: " + i);

                /**
                 * From Ogury documentation
                 *
                 * code 0: load failed
                 * code 1: phone not connected to internet
                 * code 2: ad disabled
                 * code 3: various error (configuration file not synced)
                 * code 4: ad expires in 4 hours if it was not shown
                 * code 5: start method not called
                 **/

                switch (i) {
                    case 0:
                        interstitialAdapterListener.adRequestFailed("Ogury adapter failed with error code 0: load failed", true);
                        break;

                    case 1:
                        interstitialAdapterListener.adRequestFailed("Ogury adapter failed with error code 1: phone not connected to internet", false);
                        break;

                    case 2:
                        interstitialAdapterListener.adRequestFailed("Ogury adapter failed with error code 2: ad disabled", true);
                        break;

                    case 3:
                        interstitialAdapterListener.adRequestFailed("Ogury adapter failed with error code 3: various error", true);
                        break;

                    case 4:
                        interstitialAdapterListener.onInterstitialFailedToShow("Ogury adapter failed with error code 4: ad expires in 4 hours if it was not shown");
                        break;

                    case 5:
                        interstitialAdapterListener.onInterstitialFailedToShow("Ogury adapter failed with error code 5: start method not called");
                        break;
                }
            }
        };

        // Instantiate the Presage interstitial
        presageInterstitial = new PresageInterstitial((Activity) context);
        presageInterstitial.setInterstitialCallback(presageInterstitialCallback);

        presageInterstitial.load();
    }

    @Override
    public void showInterstitial() throws Exception {
        if (presageInterstitial != null && presageInterstitial.isLoaded()) {
            presageInterstitial.show();
        }
    }

    @Override
    public void onDestroy() {
        presageInterstitial = null;
    }
}
