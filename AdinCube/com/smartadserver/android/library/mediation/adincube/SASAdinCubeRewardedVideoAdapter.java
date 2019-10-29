package com.smartadserver.android.library.mediation.adincube;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.adincube.sdk.AdinCube;
import com.adincube.sdk.AdinCubeRewardedEventListener;
import com.adincube.sdk.AdinCubeUserConsentEventListener;
import com.smartadserver.android.library.mediation.SASMediationRewardedVideoAdapter;
import com.smartadserver.android.library.mediation.SASMediationRewardedVideoAdapterListener;


import java.util.Map;

public class SASAdinCubeRewardedVideoAdapter extends SASAdinCubeAdapterBase implements SASMediationRewardedVideoAdapter {

    // internal TAG string for console output
    private static final String TAG = SASAdinCubeRewardedVideoAdapter.class.getSimpleName();

    private Activity activity;

    @Override
    public void requestRewardedVideoAd(@NonNull Context context, @NonNull String serverParametersString, @NonNull Map<String, String> clientParameters,
                                       @NonNull final SASMediationRewardedVideoAdapterListener rewardedVideoAdapterListener) {
        Log.d(TAG, "SASAdinCubeRewardedVideoAdapter requestAd");

        // AdInCube interstitial require that context be an Activity
        if (!(context instanceof Activity)) {
            rewardedVideoAdapterListener.adRequestFailed("The AdinCube SDK requires that the passed Context" +
                    "to load rewarded video ads be an Activity ", false);
            return;
        }

        activity = (Activity) context;

        configureAdRequest(context, serverParametersString);

        // create AdInCube rewarded video listener to catch events
        AdinCubeRewardedEventListener rewardedEventListener = new AdinCubeRewardedEventListener() {

            @Override
            public void onAdFetched() {
                Log.d(TAG, "AdinCube onAdFetched");
                rewardedVideoAdapterListener.onRewardedVideoLoaded();
            }

            @Override
            public void onFetchError(String s) {
                Log.d(TAG, "AdinCube onFetchError");
                rewardedVideoAdapterListener.adRequestFailed(s, true);
            }

            @Override
            public void onAdShown() {
                Log.d(TAG, "AdinCube onAdShown");
                rewardedVideoAdapterListener.onRewardedVideoShown();
            }

            @Override
            public void onError(String s) {
                Log.d(TAG, "AdinCube onError");
                rewardedVideoAdapterListener.onRewardedVideoFailedToShow(s);
            }

            @Override
            public void onAdCompleted() {
                Log.d(TAG, "AdinCube onAdCompleted");

                // notify Smart SDK of earned reward
                rewardedVideoAdapterListener.onReward(null);
            }

            @Override
            public void onAdClicked() {
                Log.d(TAG, "AdinCube onAdClicked");
                rewardedVideoAdapterListener.onAdClicked();
            }

            @Override
            public void onAdHidden() {
                Log.d(TAG, "AdinCube onAdHidden");
                rewardedVideoAdapterListener.onAdClosed();
            }
        };

        AdinCube.Rewarded.setEventListener(rewardedEventListener);
        AdinCube.Rewarded.fetch(activity);
    }

    @Override
    public void showRewardedVideoAd() throws Exception {
        showRewardedIfReady();
    }

    private void showRewardedIfReady() {
        if (AdinCube.Rewarded.isReady(activity)) {
            AdinCube.Rewarded.show(activity);
        }
    }

    @Override
    public void onDestroy() {
        activity = null;
    }
}
