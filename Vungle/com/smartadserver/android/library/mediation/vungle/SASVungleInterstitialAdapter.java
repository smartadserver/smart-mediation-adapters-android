package com.smartadserver.android.library.mediation.vungle;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.smartadserver.android.library.mediation.SASMediationInterstitialAdapter;
import com.smartadserver.android.library.mediation.SASMediationInterstitialAdapterListener;
import com.vungle.warren.AdConfig;
import com.vungle.warren.Vungle;
import com.vungle.warren.error.VungleException;

import java.util.Map;

/**
 * Mediation adapter class for Vungle Interstitial format
 */
public class SASVungleInterstitialAdapter extends SASVungleAdapterBase implements SASMediationInterstitialAdapter {

    static private final String TAG = SASVungleInterstitialAdapter.class.getSimpleName();

    private boolean interstitialShown = false;


    /**
     * Requests and process a Vungle Interstitial ad.
     *
     * @param context                     the {@link android.content.Context} needed by the mediation SDK to make the ad request
     * @param serverParametersString      a String containing all needed parameters (as returned by Smart ad delivery) to make the mediation ad call
     * @param clientParameters            additional client-side parameters (user specific, like location)
     * @param interstitialAdapterListener the {@link SASMediationInterstitialAdapterListener} provided to this {@link com.smartadserver.android.library.mediation.SASMediationAdapter} to notify Smart SDK of events occurring
     */
    @Override
    public void requestInterstitialAd(@NonNull Context context,
                                      @NonNull String serverParametersString,
                                      @NonNull Map<String, String> clientParameters,
                                      @NonNull final SASMediationInterstitialAdapterListener interstitialAdapterListener) {
        Log.d(TAG, "SASVungleInterstitialAdapter requestAd");

        // reset interstitial show status to false
        interstitialShown = false;

        configureAdapter(context, serverParametersString, clientParameters, interstitialAdapterListener);

    }

    @Override
    public void onError(String s, VungleException exception) {
        super.onError(s, exception);

        // check if this is a display error
        if (!interstitialShown && adLoaded) {
            String message = "";
            if (exception != null && exception.getLocalizedMessage() != null) {
                message = exception.getLocalizedMessage();
            }
            ((SASMediationInterstitialAdapterListener) mediationAdapterListener).onInterstitialFailedToShow(message);
        }
    }

    /**
     * Overriden for interstitial specific needs
     */
    @Override
    public void onSuccess() {
        super.onSuccess();
        Vungle.loadAd(placementID, this);
    }

    @Override
    public void onAdLoad(String id) {
        super.onAdLoad(id);
        ((SASMediationInterstitialAdapterListener) mediationAdapterListener).onInterstitialLoaded();
    }

    @Override
    public void onAdStart(String s) {
        super.onAdStart(s);
        interstitialShown = true;
        ((SASMediationInterstitialAdapterListener) mediationAdapterListener).onInterstitialShown();
    }

    @Override
    public void showInterstitial() throws Exception {
        if (Vungle.canPlayAd(placementID)) {
            Vungle.playAd(placementID, new AdConfig(), this);
        }
    }

    @Override
    public void onDestroy() {
    }
}
