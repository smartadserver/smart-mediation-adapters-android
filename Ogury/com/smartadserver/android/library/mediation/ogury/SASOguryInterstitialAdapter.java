package com.smartadserver.android.library.mediation.ogury;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.smartadserver.android.library.mediation.SASMediationInterstitialAdapter;
import com.smartadserver.android.library.mediation.SASMediationInterstitialAdapterListener;

import java.util.Map;

import io.presage.common.AdConfig;
import io.presage.interstitial.PresageInterstitial;

/**
 * Mediation adapter class for Ogury interstitial ad format
 */
public class SASOguryInterstitialAdapter extends SASOguryAdapterBase implements SASMediationInterstitialAdapter {

    static private final String TAG = SASOguryInterstitialAdapter.class.getSimpleName();

    // Ogury Interstitial manager instance
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
    public void requestInterstitialAd(@NonNull Context context,
                                      @NonNull String serverParametersString,
                                      @NonNull Map<String, String> clientParameters,
                                      @NonNull final SASMediationInterstitialAdapterListener interstitialAdapterListener) {
        Log.d(TAG, "SASOguryInterstitialAdapter adRequest");

        if (!(context instanceof Activity)) {
            interstitialAdapterListener.adRequestFailed("Ogury ad mediation requires the context to be an Activity for Interstitial format", false);
            return;
        }

        // common configuration
        configureAdRequest(context, serverParametersString, interstitialAdapterListener);


        // Instantiate the Presage interstitial
        AdConfig adConfig = new AdConfig(getAdUnitID(serverParametersString));
        presageInterstitial = new PresageInterstitial(context, adConfig);
        presageInterstitial.setInterstitialCallback(this);
        presageInterstitial.load();

    }

    @Override
    public void onAdLoaded() {
        super.onAdLoaded();
        ((SASMediationInterstitialAdapterListener)mediationAdapterListener).onInterstitialLoaded();
    }

    @Override
    public void onAdDisplayed() {
        super.onAdDisplayed();
        ((SASMediationInterstitialAdapterListener)mediationAdapterListener).onInterstitialShown();
    }

    @Override
    public void showInterstitial() throws Exception {
        if (presageInterstitial != null && presageInterstitial.isLoaded()) {
            presageInterstitial.show();
        }
    }

    @Override
    public void onDestroy() {
        // workaround for Ogury not supporting nullification of callback
//        if (presageInterstitial != null) {
//            presageInterstitial.setInterstitialCallback(null);
//        }
        presageInterstitial = null;
    }
}
