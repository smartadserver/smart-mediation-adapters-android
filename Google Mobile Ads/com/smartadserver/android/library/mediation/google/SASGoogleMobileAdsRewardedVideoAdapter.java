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
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;

import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.smartadserver.android.library.mediation.SASMediationRewardedVideoAdapter;
import com.smartadserver.android.library.mediation.SASMediationRewardedVideoAdapterListener;
import com.smartadserver.android.library.model.SASReward;
import com.smartadserver.android.library.util.SASUtil;


import java.lang.ref.WeakReference;
import java.util.Map;

/**
 * Mediation adapter class for AdMob rewarded video format
 */
public class SASGoogleMobileAdsRewardedVideoAdapter extends SASGoogleMobileAdsAdapterBase implements SASMediationRewardedVideoAdapter {

    // tag for logging purposes
    private static final String TAG = SASGoogleMobileAdsRewardedVideoAdapter.class.getSimpleName();

    // Google mobile ads rewarded ad
    private RewardedAd mRewardedAd = null;

    // WeakReference on Activity at loading time for future display
    WeakReference<Activity> activityWeakReference = null;

    SASMediationRewardedVideoAdapterListener rewardedVideoAdapterListener = null;


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
                                       final @NonNull SASMediationRewardedVideoAdapterListener rewardedVideoAdapterListener) {

        // reset any previous leftover (?) rewarded ad
        mRewardedAd = null;

        if (!(context instanceof Activity)) {
            rewardedVideoAdapterListener.adRequestFailed("Google rewarded requires the Context to be an Activity for display", false);
            return;
        }

        activityWeakReference = new WeakReference<>((Activity)context);

        this.rewardedVideoAdapterListener = rewardedVideoAdapterListener;

        GoogleMobileAds gma = initGoogleMobileAds(context, serverParametersString);

        String adUnitID = getAdUnitID(serverParametersString);

        if (GoogleMobileAds.ADMOB == gma) {
            // create rewarded ad request
            AdRequest adRequest = configureAdRequest(context, serverParametersString, clientParameters);
            // execute request
            RewardedAd.load(context, adUnitID, adRequest, createRewardedAdLoadCallback(rewardedVideoAdapterListener));
        } else if (GoogleMobileAds.AD_MANAGER == gma) {
            // create rewarded publisher ad request
            AdManagerAdRequest adManagerAdRequest = configureAdManagerAdRequest(context, serverParametersString, clientParameters);
            // execute request
            RewardedAd.load(context, adUnitID, adManagerAdRequest, createRewardedAdLoadCallback(rewardedVideoAdapterListener));
        }
    }

    private RewardedAdLoadCallback createRewardedAdLoadCallback(SASMediationRewardedVideoAdapterListener rewardedVideoAdapterListener) {
        return new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                Log.d(TAG, "Google mobile ads onRewardedVideoAdLoaded for rewarded video");

                mRewardedAd = rewardedAd;

                mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        super.onAdFailedToShowFullScreenContent(adError);
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        Log.d(TAG, "Google mobile ads onAdShowedFullScreenContent for rewarded");
                        rewardedVideoAdapterListener.onRewardedVideoShown();
                        mRewardedAd = null;
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        Log.d(TAG, "Google mobile ads onAdDismissedFullScreenContent for rewarded");
                        rewardedVideoAdapterListener.onAdClosed();
                    }

                    @Override
                    public void onAdImpression() {
                        Log.d(TAG, "Google mobile ads onAdImpression for rewarded");
                    }
                });

                rewardedVideoAdapterListener.onRewardedVideoLoaded();
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.d(TAG, "Google mobile ads onAdFailedToLoad for rewarded (error:" + loadAdError + ")");
                boolean isNoAd = loadAdError.getCode() == AdRequest.ERROR_CODE_NO_FILL;
                rewardedVideoAdapterListener.adRequestFailed("Google mobile ads rewarded sad loading error " + loadAdError, isNoAd);
            }
        };
    }

    /**
     * Shows the previously loaded rewarded video if any (or throws an exception if error).
     *
     * @throws Exception
     */
    @Override
    public void showRewardedVideoAd() throws Exception {

        if (mRewardedAd == null) {
            throw new Exception("No Google mobile ads rewarded ad loaded !");
        }

        final Activity activity = activityWeakReference != null ? activityWeakReference.get() : null;

        if (activity == null) {
            throw new Exception("Activity to display Google rewarded is null");
        }

        // launch rewarded ad display
        SASUtil.getMainLooperHandler().post(new Runnable() {
            @Override
            public void run() {
                mRewardedAd.show(activity, new OnUserEarnedRewardListener() {
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                        Log.d(TAG, "Google mobile ads onUserEarnedReward for rewarded ad : label:" + rewardItem.getType() + " amount:" + rewardItem.getAmount());

                        // notify Smart SDK of earned reward
                        rewardedVideoAdapterListener.onReward(new SASReward(rewardItem.getType(), rewardItem.getAmount()));
                    }
                });
            }
        });
    }

    @Override
    public void onDestroy() {
        mRewardedAd = null;
    }
}
