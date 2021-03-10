package com.smartadserver.android.library.mediation.facebook;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.smartadserver.android.library.mediation.SASMediationInterstitialAdapter;
import com.smartadserver.android.library.mediation.SASMediationInterstitialAdapterListener;


import java.util.Map;

/**
 * Mediation adapter class for Facebook interstitial format
 */
public class SASFacebookInterstitialAdapter extends SASFacebookAdapterBase implements SASMediationInterstitialAdapter {

    // Tag for logging purposes
    static private final String TAG = SASFacebookInterstitialAdapter.class.getSimpleName();

    // Facebook interstitial object
    private InterstitialAd interstitialAdView;

    @Override
    public void requestInterstitialAd(@NonNull Context context, @NonNull String serverParametersString, @NonNull Map<String, String> clientParameters,
                                @NonNull final SASMediationInterstitialAdapterListener interstitialAdapterListener) {

        configureAdRequest(context, serverParametersString, clientParameters);

        String placementID = serverParametersString;

        // instantiate Facebook interstitial object
        interstitialAdView = new InterstitialAd(context,placementID);

        // instantiate Facebook interstitial listener object
        InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                Log.d(TAG, "Facebook onError for interstitial");
                boolean isNoAd = adError.getErrorCode() == AdError.NO_FILL_ERROR_CODE;
                interstitialAdapterListener.adRequestFailed(adError.getErrorMessage(), isNoAd);
            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.d(TAG, "Facebook onAdLoaded for interstitial");
                interstitialAdapterListener.onInterstitialLoaded();
            }

            @Override
            public void onAdClicked(Ad ad) {
                Log.d(TAG, "Facebook onAdClicked for interstitial");
                interstitialAdapterListener.onAdClicked();
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                Log.d(TAG, "Facebook onLoggingImpression for interstitial");
            }

            // interstitial only methods
            @Override
            public void onInterstitialDisplayed(Ad ad) {
                Log.d(TAG, "Facebook onInterstitialDisplayed for interstitial");
                interstitialAdapterListener.onInterstitialShown();
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                Log.d(TAG, "Facebook onInterstitialDismissed for interstitial");
                interstitialAdapterListener.onAdClosed();
            }
        };

        // perform ad request with listener
        interstitialAdView.loadAd(interstitialAdView.buildLoadAdConfig().withAdListener(interstitialAdListener).build());

    }

        @Override
    public void showInterstitial() throws Exception {
        if (interstitialAdView != null && interstitialAdView.isAdLoaded()) {
            interstitialAdView.show();
        } else {
            throw new Exception("No Facebook insterstitial ad loaded !");
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Facebook onDestroy for interstitial");
        if (interstitialAdView != null) {
            interstitialAdView.destroy();
        }
    }
}
