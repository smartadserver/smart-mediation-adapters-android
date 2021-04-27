package com.smartadserver.android.library.mediation.adcolony;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;


import com.adcolony.sdk.AdColony;
import com.adcolony.sdk.AdColonyInterstitial;
import com.adcolony.sdk.AdColonyInterstitialListener;
import com.adcolony.sdk.AdColonyZone;
import com.smartadserver.android.library.mediation.SASMediationInterstitialAdapter;
import com.smartadserver.android.library.mediation.SASMediationInterstitialAdapterListener;


import java.util.Map;

/**
 * Mediation adapter class for MoPub interstitial format
 */
public class SASAdColonyInterstitialAdapter extends SASAdColonyAdapterBase implements SASMediationInterstitialAdapter {

    // tag for logging purposes
    static private final String TAG = SASAdColonyInterstitialAdapter.class.getSimpleName();

    // AdColony interstitial instance
    @Nullable
    AdColonyInterstitial adColonyInterstitial;

    /**
     * Requests a mediated interstitial ad asynchronously
     *
     * @param context                     the {@link android.content.Context} needed by the mediation SDK to make the ad request
     * @param serverParametersString      a String containing all needed parameters (as returned by Smart ad delivery)
     *                                    to make the mediation ad call
     * @param clientParameters            additional client-side parameters (user specific, like location)
     * @param interstitialAdapterListener the {@link SASMediationInterstitialAdapterListener} provided to
     *                                    this {@link com.smartadserver.android.library.mediation.SASMediationAdapter} to notify Smart SDK of events occurring
     */
    @Override
    public void requestInterstitialAd(@NonNull Context context,
                                      @NonNull String serverParametersString,
                                      @NonNull Map<String, Object> clientParameters,
                                      @NonNull final SASMediationInterstitialAdapterListener interstitialAdapterListener) {

        if (!(context instanceof Activity)) {
            interstitialAdapterListener.adRequestFailed("AdColony ad mediation requires that the Smart AdServer SASAdview " +
                    " be created with an Activity as context parameter", false);
            return;
        }

        // extract AdColony specific parameters
        final String zoneID = serverParametersString.split("/")[1];

        // prepare ad request
        configureAdRequest((Activity) context, serverParametersString, clientParameters);

        // instantiate adcolony listener
        AdColonyInterstitialListener listener = new AdColonyInterstitialListener() {
            @Override
            public void onRequestFilled(AdColonyInterstitial ad) {
                Log.d(TAG, "AdColony onRequestFilled for interstitial");

                adColonyInterstitial = ad;

                interstitialAdapterListener.onInterstitialLoaded();
            }

            @Override
            public void onRequestNotFilled(AdColonyZone zone) {
                Log.d(TAG, "AdColony onRequestNotFilled for interstitial. Zone :" + zone);
                interstitialAdapterListener.adRequestFailed("Cannot load interstitial from AdColony!", true);
            }

            @Override
            public void onExpiring(AdColonyInterstitial ad) {
                Log.d(TAG, "AdColony onExpiring for interstitial");

                // If the interstitial is expiring, we need to get a new one.
                // This can be problematic if the developer uses several appID for the same app since
                // this value is static and defined only once during the 'requestAd' call.
//                AdColony.requestInterstitial(ad.getZoneID(), this, null);
                onDestroy();
                adColonyInterstitial = null;
            }

            @Override
            public void onOpened(AdColonyInterstitial ad) {
                Log.d(TAG, "AdColony onOpened for interstitial");
                interstitialAdapterListener.onInterstitialShown();
            }

            @Override
            public void onClosed(AdColonyInterstitial ad) {
                Log.d(TAG, "AdColony onClosed for interstitial");
                interstitialAdapterListener.onAdClosed();
            }

            @Override
            public void onIAPEvent(AdColonyInterstitial ad, String product_id, int engagement_type) {
                Log.d(TAG, "AdColony onIAPEvent for interstitial");
            }

            @Override
            public void onLeftApplication(AdColonyInterstitial ad) {
                Log.d(TAG, "AdColony onLeftApplication for interstitial");
            }

            @Override
            public void onClicked(AdColonyInterstitial ad) {
                Log.d(TAG, "AdColony onClicked for interstitial");
                interstitialAdapterListener.onAdClicked();
            }
        };

        // perform ad request
        AdColony.requestInterstitial(zoneID, listener, null);
    }


    @Override
    public void showInterstitial() throws Exception {
        if (adColonyInterstitial != null) {
            adColonyInterstitial.show();
        } else {
            throw new Exception("No AdColony interstitial available to show (might have expired). Please make another ad call");
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "AdColony onDestroy() for interstitial");
        if (adColonyInterstitial != null) {
            adColonyInterstitial.destroy();
            adColonyInterstitial = null;
        }
    }
}
