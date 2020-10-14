package com.smartadserver.android.library.mediation.adincube;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;

import com.adincube.sdk.AdinCube;
import com.adincube.sdk.AdinCubeInterstitialEventListener;
import com.smartadserver.android.library.mediation.SASMediationInterstitialAdapter;
import com.smartadserver.android.library.mediation.SASMediationInterstitialAdapterListener;


import java.util.Map;

/**
 * Mediation adapter class for AdinCube banner format
 */
public class SASAdinCubeInterstitialAdapter extends SASAdinCubeAdapterBase implements SASMediationInterstitialAdapter {

    // internal TAG string for console output
    private static final String TAG = SASAdinCubeInterstitialAdapter.class.getSimpleName();

    // store if the request has succeed or not, to differentiate ad request error and ad show error.
    private boolean adRequestSucceed = false;

    // the Activity needed by the AdInCube interstitial
    private Activity activity;

    /**
     * Requests a mediated interstitial ad asynchronously
     *
     * @param context                     the {@link android.content.Context} needed by the mediation SDK to make the bannr ad request
     * @param serverParametersString      a String containing all needed parameters (as returned by Smart ad delivery)
     *                                    to make the mediation ad call
     * @param clientParameters            additional client-side parameters (user specific, like location)
     * @param interstitialAdapterListener the {@link SASMediationInterstitialAdapterListener} provided to
     *                                    this {@link com.smartadserver.android.library.mediation.SASMediationAdapter} to notify Smart SDK of events occurring
     */
    @Override
    public void requestInterstitialAd(@NonNull Context context, @NonNull String serverParametersString, @NonNull Map<String, String> clientParameters,
                                      @NonNull final SASMediationInterstitialAdapterListener interstitialAdapterListener) {
        Log.d(TAG, "SASAdinCubeInterstitialAdapter requestAd");


        // AdInCube interstitial require that context be an Activity
        if (!(context instanceof Activity)) {
            interstitialAdapterListener.adRequestFailed("The AdinCube SDK requires that the passed Context" +
                    "to load interstitial ads be an Activity ", false);
            return;
        }

        activity = (Activity) context;

        configureAdRequest(context, serverParametersString);

        // create AdInCube interstitial listener to catch events
        AdinCubeInterstitialEventListener interstitialEventListener = new AdinCubeInterstitialEventListener() {
            @Override
            public void onAdCached() {
                Log.d(TAG, "AdinCube onAdCached");
                interstitialAdapterListener.onInterstitialLoaded();
                adRequestSucceed = true;
            }

            @Override
            public void onAdShown() {
                Log.d(TAG, "AdinCube ad onAdShown");
                interstitialAdapterListener.onInterstitialShown();
            }

            @Override
            public void onError(String s) {
                Log.d(TAG, "AdinCube interstitial onError");
                if (adRequestSucceed) {
                    interstitialAdapterListener.onInterstitialFailedToShow(s);
                } else {
                    interstitialAdapterListener.adRequestFailed(s, true);
                }

            }

            @Override
            public void onAdClicked() {
                Log.d(TAG, "AdinCube interstitial onAdClicked");
                interstitialAdapterListener.onAdClicked();
            }

            @Override
            public void onAdHidden() {
                Log.d(TAG, "AdinCube onAdHidden");
                interstitialAdapterListener.onAdClosed();
            }
        };

        // create AdInCubeBanner instance, set the listener and load ad
        AdinCube.Interstitial.setEventListener(interstitialEventListener);
        AdinCube.Interstitial.init(activity);

    }

    @Override
    public void showInterstitial() throws Exception {
        showInterstitialIfReady();
    }

    private void showInterstitialIfReady() {
        if (AdinCube.Interstitial.isReady(activity)) {
            AdinCube.Interstitial.show(activity);
        }
    }

    @Override
    public void onDestroy() {
        activity = null;
    }

}
