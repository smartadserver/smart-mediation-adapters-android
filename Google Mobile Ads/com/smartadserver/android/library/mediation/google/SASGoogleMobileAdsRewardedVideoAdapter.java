package com.smartadserver.android.library.mediation.google;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.smartadserver.android.library.exception.SASAdDisplayException;
import com.smartadserver.android.library.mediation.SASMediationRewardedVideoAdapter;
import com.smartadserver.android.library.mediation.SASMediationRewardedVideoAdapterListener;
import com.smartadserver.android.library.model.SASReward;
import com.smartadserver.android.library.util.SASUtil;

import java.util.Map;

/**
 * Mediation adapter class for AdMob rewarded video format
 */
public class SASGoogleMobileAdsRewardedVideoAdapter extends SASGoogleMobileAdsAdapterBase implements SASMediationRewardedVideoAdapter {

    // tag for logging purposes
    private static final String TAG = "SASGoogleMobileAdsRewardedVideoAdapter";

    // Google mobile ads rewarded video manager singleton instance
    RewardedVideoAd rewardedVideoAd;

    /**
     * Requests a mediated rewarded video ad asynchronously.
     *
     * @param context                      The {@link Context} needed by the mediation SDK to make the ad request.
     * @param serverParametersString       a String containing all needed parameters (as returned by Smart ad delivery) to make the mediation ad call.
     * @param clientParameters             additional client-side parameters (user specific, like location).
     * @param rewardedVideoAdapterListener the {@link SASMediationRewardedVideoAdapterListener} provided to this {@link com.smartadserver.android.library.mediation.SASMediationAdapter} to notify the Smart SDK of events
     */
    @Override
    public void requestRewardedVideoAd(@NonNull Context context, @NonNull String serverParametersString,
                                       @NonNull Map<String, String> clientParameters, final @NonNull SASMediationRewardedVideoAdapterListener rewardedVideoAdapterListener) {


        // create rewarded ad request
        AdRequest adRequest = configureAdRequest(context, serverParametersString, clientParameters);

        // Get Google mobile ads rewarded video manager instance
        rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(context);

        // create an Google mobile ads rewarded video ad listener that will intercept ad mob interstitial events and call
        // appropriate SASMediationRewardedVideoAdapterListener counterpart methods
        RewardedVideoAdListener rewardedVideoListener = new RewardedVideoAdListener() {

            @Override
            public void onRewardedVideoAdLoaded() {
                SASUtil.logDebug(TAG, "Google mobile ads onRewardedVideoAdLoaded for rewarded video");
                rewardedVideoAdapterListener.onRewardedVideoLoaded();
            }

            @Override
            public void onRewardedVideoAdOpened() {
                SASUtil.logDebug(TAG, "Google mobile ads onRewardedVideoAdOpened for rewarded video");
                rewardedVideoAdapterListener.onRewardedVideoShown();
            }

            @Override
            public void onRewardedVideoStarted() {
                SASUtil.logDebug(TAG, "Google mobile ads onRewardedVideoStarted for interstitial");
            }

            @Override
            public void onRewardedVideoAdClosed() {
                SASUtil.logDebug(TAG, "Google mobile ads onRewardedVideoAdClosed for interstitial");
                rewardedVideoAdapterListener.onAdClosed();
            }

            @Override
            public void onRewarded(RewardItem rewardItem) {
                SASUtil.logDebug(TAG, "Google mobile ads onRewarded for rewarded video : label:" + rewardItem.getType() + " amount:" + rewardItem.getAmount());

                // notify Smart SDK of earned reward
                rewardedVideoAdapterListener.onReward(new SASReward(rewardItem.getType(), rewardItem.getAmount()));
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {
                SASUtil.logDebug(TAG, "Google mobile ads onRewardedVideoAdLeftApplication for interstitial");
                rewardedVideoAdapterListener.onAdClicked();
            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int errorCode) {
                SASUtil.logDebug(TAG, "Google mobile ads rewarded video ad onRewardedVideoAdFailedToLoad (error code:" + errorCode + ")");
                boolean isNoAd = errorCode == AdRequest.ERROR_CODE_NO_FILL;
                rewardedVideoAdapterListener.adRequestFailed("Error code:" + errorCode, isNoAd);
            }

            @Override
            public void onRewardedVideoCompleted() {
                SASUtil.logDebug(TAG, "Google mobile ads onRewardedVideoCompleted for rewarded video");
            }

        };

        // set the rewarded video listener on the singleton
        rewardedVideoAd.setRewardedVideoAdListener(rewardedVideoListener);

        // make ad call
        String adUnitID = serverParametersString.split("\\|")[1];
//        adUnitID = "ca-app-pub-3940256099942544/5224354917"; // USE FOR TESTING ONLY (Google mobile ads sample ID)
        rewardedVideoAd.loadAd(adUnitID, adRequest);

    }

    /**
     * Shows the previously loaded rewarded video if any (or throws an exception if error).
     *
     * @throws Exception
     */
    @Override
    public void showRewardedVideoAd() throws Exception {

        /**
         * Methods of the Google mobile ads's InterstitialAd or RewardedVideoAd like isLoaded(), show()
         * must be called on the Main Thread or they throw an exception.
         *
         * So execute them from the Main thread, but wait for the outcome, should they throw
         * an exception (which will be stored in exceptions array)
         */
        final SASAdDisplayException[] exceptions = new SASAdDisplayException[1];

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    if (rewardedVideoAd.isLoaded()) {
                        rewardedVideoAd.show();
                    } else {
                        throw new Exception("No Google mobile ads rewarded video loaded !");
                    }
                } catch (Exception e) {
                    // catch potential Exception and create a SASAdDisplayException containing the message
                    exceptions[0] = new SASAdDisplayException(e.getMessage());
                }

                synchronized (this) {
                    this.notify();
                }
            }
        };

        // synchronized block to wait runnable execution outcome
        synchronized (runnable) {
            SASUtil.getMainLooperHandler().post(runnable);
            runnable.wait();
        }

        // if an exception was thrown, re-throw the exception
        if (exceptions[0] != null) {
            throw exceptions[0];
        }
    }

    @Override
    public void onDestroy() {
        // nothing to do
    }
}
