package com.smartadserver.android.library.mediation.facebook;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.RewardedVideoAd;
import com.facebook.ads.RewardedVideoAdListener;
import com.smartadserver.android.library.mediation.SASMediationRewardedVideoAdapter;
import com.smartadserver.android.library.mediation.SASMediationRewardedVideoAdapterListener;
import com.smartadserver.android.library.model.SASReward;


import java.util.Map;

/**
 * Mediation adapter class for Facebook rewarded video format
 */
public class SASFacebookRewardedVideoAdapter extends SASFacebookAdapterBase implements SASMediationRewardedVideoAdapter {

    // Tag for logging purposes
    static private final String TAG = SASFacebookRewardedVideoAdapter.class.getSimpleName();

    // Facebook interstitial object
    private RewardedVideoAd rewardedVideoAd;

    // SASReward object
    SASReward sasReward;

    @Override
    public void requestRewardedVideoAd(@NonNull Context context, @NonNull String serverParametersString, @NonNull Map<String, String> clientParameters,
                                       @NonNull final SASMediationRewardedVideoAdapterListener rewardedVideoAdapterListener) {

        configureAdRequest(context,serverParametersString,clientParameters);

        String placementID = serverParametersString;

        // instantiate Facebook interstitial object
        rewardedVideoAd = new RewardedVideoAd(context,placementID);

        RewardedVideoAdListener rewardedVideoAdListener = new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoCompleted() {
                Log.d(TAG, "Facebook onRewardedVideoCompleted for rewarded video");
                rewardedVideoAdapterListener.onReward(null);
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                Log.d(TAG, "Facebook onError for rewarded video");
                boolean isNoAd = adError.getErrorCode() == AdError.NO_FILL_ERROR_CODE;
                rewardedVideoAdapterListener.adRequestFailed(adError.getErrorMessage(), isNoAd);
            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.d(TAG, "Facebook ad onAdLoaded for rewarded video");
                rewardedVideoAdapterListener.onRewardedVideoLoaded();
            }

            @Override
            public void onAdClicked(Ad ad) {
                Log.d(TAG, "Facebook ad onAdClicked for rewarded video");
                rewardedVideoAdapterListener.onAdClicked();

            }

            @Override
            public void onLoggingImpression(Ad ad) {
                Log.d(TAG, "Facebook ad onLoggingImpression for rewarded video");
                rewardedVideoAdapterListener.onRewardedVideoShown();
            }

            @Override
            public void onRewardedVideoClosed() {
                Log.d(TAG, "Facebook onRewardedVideoClosed for rewarded video");
                rewardedVideoAdapterListener.onAdClosed();
            }
        };

        // set interstitial listener on interstitial
        rewardedVideoAd.setAdListener(rewardedVideoAdListener);

        // perform ad request
        rewardedVideoAd.loadAd();

    }

    @Override
    public void showRewardedVideoAd() throws Exception {
        if (rewardedVideoAd != null && rewardedVideoAd.isAdLoaded()) {
            rewardedVideoAd.show();
        } else {
            throw new Exception("No Facebook rewarded video ad loaded !");
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Facebook onDestroy for interstitial");
        if (rewardedVideoAd != null) {
            rewardedVideoAd.destroy();
        }
    }
}
