package com.smartadserver.android.library.mediation.applovin;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;

import com.applovin.adview.AppLovinIncentivizedInterstitial;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdClickListener;
import com.applovin.sdk.AppLovinAdDisplayListener;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdRewardListener;
import com.applovin.sdk.AppLovinAdVideoPlaybackListener;
import com.applovin.sdk.AppLovinErrorCodes;
import com.smartadserver.android.library.mediation.SASMediationRewardedVideoAdapter;
import com.smartadserver.android.library.mediation.SASMediationRewardedVideoAdapterListener;
import com.smartadserver.android.library.model.SASReward;


import java.util.Map;

/**
 * Mediation adapter class for AppLovin rewarded video format
 */
public class SASAppLovinRewardedVideoAdapter extends SASAppLovinAdapterBase implements SASMediationRewardedVideoAdapter {

    // tag for logging purposes
    private static final String TAG = SASAppLovinRewardedVideoAdapter.class.getSimpleName();

    @Nullable
    private AppLovinIncentivizedInterstitial incentivizedInterstitial;

    /**
     * All-in-one listener class, implemented in requestRewardedVideoAd
     */
    abstract class RewardedVideoListener implements AppLovinAdClickListener, AppLovinAdDisplayListener, AppLovinAdVideoPlaybackListener, AppLovinAdRewardListener {
    }

    // RewardedVideoListener instance
    @Nullable
    RewardedVideoListener rewardedVideoListener;

    @Nullable
    Context context;

    /**
     * Requests a mediated rewarded video ad asynchronously.
     *
     * @param context                      The {@link Context} needed by the mediation SDK to make the ad request.
     * @param serverParametersString       a String containing all needed parameters (as returned by Smart ad delivery) to make the mediation ad call.
     * @param clientParameters             additional client-side parameters (user specific, like location).
     * @param rewardedVideoAdapterListener the {@link SASMediationRewardedVideoAdapterListener} provided to this {@link com.smartadserver.android.library.mediation.SASMediationAdapter} to notify the Smart SDK of events
     */
    @Override
    public void requestRewardedVideoAd(@NonNull Context context,
                                       @NonNull String serverParametersString,
                                       @NonNull Map<String, Object> clientParameters,
                                       @NonNull final SASMediationRewardedVideoAdapterListener rewardedVideoAdapterListener) {

        configureAdRequest(context, serverParametersString, clientParameters);

        // need to store context object for later show()
        this.context = context;

        // Create AppLovin rewarded video manager object
        incentivizedInterstitial = AppLovinIncentivizedInterstitial.create(context.getApplicationContext());

        // instantiate RewardedVideoListener to be used in show
        rewardedVideoListener = new RewardedVideoListener() {

            @Override
            public void adClicked(AppLovinAd appLovinAd) {
                Log.d(TAG, "AppLovin adClicked for rewarded video");
                rewardedVideoAdapterListener.onAdClicked();
            }

            @Override
            public void adDisplayed(AppLovinAd appLovinAd) {
                Log.d(TAG, "AppLovin adDisplayed for rewarded video");
                rewardedVideoAdapterListener.onRewardedVideoShown();
            }

            @Override
            public void adHidden(AppLovinAd appLovinAd) {
                Log.d(TAG, "AppLovin adHidden for rewarded video");
                rewardedVideoAdapterListener.onAdClosed();
            }

            @Override
            public void videoPlaybackBegan(AppLovinAd appLovinAd) {
                Log.d(TAG, "AppLovin videoPlaybackBegan for rewarded video");
            }

            @Override
            public void videoPlaybackEnded(AppLovinAd appLovinAd, double percentage, boolean fullyWatched) {
                Log.d(TAG, "AppLovin videoPlaybackEnded for rewarded video. Percentage:" + percentage + " completed:" + fullyWatched);
            }

            @Override
            public void userRewardVerified(AppLovinAd appLovinAd, Map map) {
                Log.d(TAG, "AppLovin userRewardVerified for rewarded interstitial");

                String currencyName = (String) map.get("currency");
                // For example, "5" or "5.00" if you've specified an amount in the UI.
                double amount = 0;
                try {
                    amount = Double.parseDouble((String) map.get("amount"));
                } catch (Exception ignored) {
                }

                // legit reward, fire appropriate callback
                if (amount > 0) {
                    rewardedVideoAdapterListener.onReward(new SASReward(currencyName, amount));
                }
            }

            @Override
            public void userOverQuota(AppLovinAd appLovinAd, Map map) {
                Log.d(TAG, "AppLovin userOverQuota for rewarded interstitial");
            }

            @Override
            public void userRewardRejected(AppLovinAd appLovinAd, Map map) {
                Log.d(TAG, "AppLovin userRewardRejected for rewarded interstitial");
            }

            @Override
            public void validationRequestFailed(AppLovinAd appLovinAd, int responseCode) {
                Log.d(TAG, "AppLovin validationRequestFailed for rewarded interstitial");
                if (responseCode == AppLovinErrorCodes.INCENTIVIZED_USER_CLOSED_VIDEO) {
                    // Your user exited the video prematurely. It's up to you if you'd still like to grant
                    // a reward in this case. Most developers choose not to. Note that this case can occur
                    // after a reward was initially granted (since reward validation happens as soon as a
                    // video is launched).
                } else if (responseCode == AppLovinErrorCodes.INCENTIVIZED_SERVER_TIMEOUT || responseCode == AppLovinErrorCodes.INCENTIVIZED_UNKNOWN_SERVER_ERROR) {
                    // Some server issue happened here. Don't grant a reward. By default we'll show the user
                    // a alert telling them to try again later, but you can change this in the
                    // AppLovin dashboard.
                } else if (responseCode == AppLovinErrorCodes.INCENTIVIZED_NO_AD_PRELOADED) {
                    // Indicates that the developer called for a rewarded video before one was available.
                    // Note: This code is only possible when working with rewarded videos.
                }
            }

            @Override
            public void userDeclinedToViewAd(AppLovinAd appLovinAd) {
                Log.d(TAG, "AppLovin userDeclinedToViewAd for rewarded interstitial");
                rewardedVideoAdapterListener.onAdClosed();
            }
        };


        incentivizedInterstitial.preload(new AppLovinAdLoadListener() {
            @Override
            public void adReceived(AppLovinAd ad) {
                Log.d(TAG, "AppLovin adReceived for rewarded interstitial");
                rewardedVideoAdapterListener.onRewardedVideoLoaded();

            }

            @Override
            public void failedToReceiveAd(int errorCode) {
                Log.d(TAG, "AppLovin failedToReceiveAd for rewarded interstitial (error:" + errorCode + ")");
                boolean isNoAd = errorCode == AppLovinErrorCodes.NO_FILL;
                rewardedVideoAdapterListener.adRequestFailed("errorCode:" + errorCode, isNoAd);
            }
        });

    }

    @Override
    public void showRewardedVideoAd() throws Exception {
        if (incentivizedInterstitial.isAdReadyToDisplay()) {
            incentivizedInterstitial.show(context, rewardedVideoListener,
                    rewardedVideoListener, rewardedVideoListener, rewardedVideoListener);
        } else {
            throw new Exception("No AppLovin rewarded video ready to display !");
        }
    }

    @Override
    public void onDestroy() {
        // nothing to do
    }
}
