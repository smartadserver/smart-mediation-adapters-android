package com.smartadserver.android.library.mediation.applovin;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;

import com.applovin.adview.AppLovinInterstitialAd;
import com.applovin.adview.AppLovinInterstitialAdDialog;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdClickListener;
import com.applovin.sdk.AppLovinAdDisplayListener;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdSize;
import com.applovin.sdk.AppLovinAdVideoPlaybackListener;
import com.applovin.sdk.AppLovinErrorCodes;
import com.smartadserver.android.library.mediation.SASMediationInterstitialAdapter;
import com.smartadserver.android.library.mediation.SASMediationInterstitialAdapterListener;


import java.util.Map;

/**
 * Mediation adapter class for AppLovin interstitial format
 */
public class SASAppLovinInterstitialAdapter extends SASAppLovinAdapterBase implements SASMediationInterstitialAdapter {

    // tag for logging purposes
    private static final String TAG = SASAppLovinInterstitialAdapter.class.getSimpleName();

    // AppLovin needed Objects
    AppLovinAd appLovinAd = null;
    AppLovinInterstitialAdDialog interstitialAdDialog = null;

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
    public void requestInterstitialAd(@NonNull Context context, @NonNull String serverParametersString, @NonNull Map<String, String> clientParameters,
                                      @NonNull final SASMediationInterstitialAdapterListener interstitialAdapterListener) {

        configureAdRequest(context, serverParametersString, clientParameters);

        interstitialAdDialog = AppLovinInterstitialAd.create(sdk, context);

        // create AppLovin click listener
        AppLovinAdClickListener adClickListener = new AppLovinAdClickListener() {
            @Override
            public void adClicked(AppLovinAd appLovinAd) {
                Log.d(TAG, "AppLovin adClicked for Interstitial");
                interstitialAdapterListener.onAdClicked();
            }
        };

        // create AppLovin display listener
        AppLovinAdDisplayListener adDisplayListener = new AppLovinAdDisplayListener() {
            @Override
            public void adDisplayed(AppLovinAd appLovinAd) {
                Log.d(TAG, "AppLovin adDisplayed for Interstitial");
                interstitialAdapterListener.onInterstitialShown();
            }

            @Override
            public void adHidden(AppLovinAd appLovinAd) {
                Log.d(TAG, "AppLovin adHidden for Interstitial");
                interstitialAdapterListener.onAdClosed();
            }
        };

        // create AppLovin video listener
        AppLovinAdVideoPlaybackListener adVideoPlaybackListener = new AppLovinAdVideoPlaybackListener() {
            @Override
            public void videoPlaybackBegan(AppLovinAd appLovinAd) {
                Log.d(TAG, "AppLovin videoPlaybackBegan for Interstitial");
            }

            @Override
            public void videoPlaybackEnded(AppLovinAd appLovinAd, double percentage, boolean fullyWatched) {
                Log.d(TAG, "AppLovin videoPlaybackEnded for Interstitial. Percentage:" + percentage + " completed:" + fullyWatched);
            }
        };

        // set AppLovin ad listeners
        interstitialAdDialog.setAdClickListener(adClickListener);
        interstitialAdDialog.setAdDisplayListener(adDisplayListener);
        interstitialAdDialog.setAdVideoPlaybackListener(adVideoPlaybackListener);

        // request interstitial ad
        SASAppLovinAdapterBase.sdk.getAdService().loadNextAd(AppLovinAdSize.INTERSTITIAL, new AppLovinAdLoadListener() {
            @Override
            public void adReceived(AppLovinAd ad) {
                appLovinAd = ad;
                Log.d(TAG, "AppLovin adReceived for interstitial");
                interstitialAdapterListener.onInterstitialLoaded();

            }

            @Override
            public void failedToReceiveAd(int errorCode) {
                // Look at AppLovinErrorCodes.java for list of error codes
                Log.d(TAG, "AppLovin failedToReceiveAd for interstitial (error:" + errorCode + ")");
                boolean isNoAd = errorCode == AppLovinErrorCodes.NO_FILL;
                interstitialAdapterListener.adRequestFailed("errorCode:" + errorCode, isNoAd);
            }
        });
    }

    @Override
    public void showInterstitial() throws Exception {
        if (interstitialAdDialog.isAdReadyToDisplay()) {
            interstitialAdDialog.showAndRender(appLovinAd);
        } else {
            throw new Exception("No AppLovin insterstitial ad ready to be displayed !");
        }
    }

    @Override
    public void onDestroy() {
        // nothing to do
    }
}
