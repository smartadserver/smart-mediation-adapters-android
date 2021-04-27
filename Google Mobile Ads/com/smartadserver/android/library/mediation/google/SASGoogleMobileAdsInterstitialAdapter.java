package com.smartadserver.android.library.mediation.google;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.smartadserver.android.library.mediation.SASMediationInterstitialAdapter;
import com.smartadserver.android.library.mediation.SASMediationInterstitialAdapterListener;
import com.smartadserver.android.library.util.SASUtil;


import java.lang.ref.WeakReference;
import java.util.Map;

/**
 * Mediation adapter class for Google mobile ads interstitial format
 */
public class SASGoogleMobileAdsInterstitialAdapter extends SASGoogleMobileAdsAdapterBase implements SASMediationInterstitialAdapter {

    // tag for logging purposes
    private static final String TAG = SASGoogleMobileAdsInterstitialAdapter.class.getSimpleName();

    // Google mobile ads interstitial ad
    InterstitialAd mInterstitialAd = null;

    // WeakReference on Activity at loading time for future display
    WeakReference<Activity> activityWeakReference = null;

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

        // reset any previous leftover (?) interstitial
        mInterstitialAd = null;

        if (!(context instanceof Activity)) {
            interstitialAdapterListener.adRequestFailed("Google interstitial requires the Context to be an Activity for display", false);
            return;
        }

        activityWeakReference = new WeakReference<>((Activity)context);

        GoogleMobileAds gma = initGoogleMobileAds(context, serverParametersString);

        String adUnitID = getAdUnitID(serverParametersString);

        if (GoogleMobileAds.ADMOB == gma) {
            // create Google mobile ad request
            AdRequest adRequest = configureAdRequest(context, serverParametersString, clientParameters);

            InterstitialAd.load(context, adUnitID, adRequest, createInterstitialAdLoadCallback(interstitialAdapterListener));

        } else if (GoogleMobileAds.AD_MANAGER == gma) {
            // create Google mobile ad request
            AdManagerAdRequest publisherAdRequest = configureAdManagerAdRequest(context, serverParametersString, clientParameters);

            // create Google mobile ads interstitial ad object
            AdManagerInterstitialAd.load(context, adUnitID, publisherAdRequest, createInterstitialAdLoadCallback(interstitialAdapterListener));
        }
    }


    private InterstitialAdLoadCallback createInterstitialAdLoadCallback(SASMediationInterstitialAdapterListener interstitialAdapterListener) {
        return new InterstitialAdLoadCallback() {

            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                Log.d(TAG, "Google mobile ads ad onAdLoaded for interstitial");
                mInterstitialAd = interstitialAd;

                // Create fullscreen callback
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {

                        String errorMessage = adError.getMessage();
                        // Called when fullscreen content failed to show.
                        Log.d("TAG", "Google mobile ads onAdFailedToShowFullScreenContent : " + adError.getMessage());
                        interstitialAdapterListener.onInterstitialFailedToShow(adError.getMessage());
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        Log.d(TAG, "Google mobile ads onAdShowedFullScreenContent for interstitial");
                        interstitialAdapterListener.onInterstitialShown();
                        mInterstitialAd = null;
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        Log.d(TAG, "Google mobile ads onAdDismissedFullScreenContent for interstitial");
                        interstitialAdapterListener.onAdClosed();
                    }

                    @Override
                    public void onAdImpression() {
                        Log.d(TAG, "Google mobile ads onAdImpression for interstitial");
                    }
                });

                interstitialAdapterListener.onInterstitialLoaded();
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.d(TAG, "Google mobile ads onAdFailedToLoad for interstitial (error:" + loadAdError + ")");
                boolean isNoAd = loadAdError.getCode() == AdRequest.ERROR_CODE_NO_FILL;
                interstitialAdapterListener.adRequestFailed("Google mobile ads interstitial ad loading error " + loadAdError, isNoAd);
            }
        };
    }

    /**
     * Shows the previously loaded interstitial if any (or throws an exception if error)
     *
     * @throws Exception
     */
    @Override
    public void showInterstitial() throws Exception {

        if (mInterstitialAd == null) {
            throw new Exception("No Google mobile ads interstitial ad loaded !");
        }

        final Activity activity = activityWeakReference != null ? activityWeakReference.get() : null;

        if (activity == null) {
            throw new Exception("Activity to display Google interstitial is null");
        }

        // launch interstitial display
        SASUtil.getMainLooperHandler().post(new Runnable() {
            @Override
            public void run() {
                mInterstitialAd.show(activity);
            }
        });
    }

    @Override
    public void onDestroy() {
        mInterstitialAd = null;
    }
}
