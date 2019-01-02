package com.smartadserver.android.library.mediation.adincube;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import com.adincube.sdk.AdinCube;
import com.adincube.sdk.AdinCubeRewardedEventListener;
import com.adincube.sdk.AdinCubeUserConsentEventListener;
import com.smartadserver.android.library.mediation.SASMediationRewardedVideoAdapter;
import com.smartadserver.android.library.mediation.SASMediationRewardedVideoAdapterListener;
import com.smartadserver.android.library.util.SASUtil;

import java.util.Map;

public class SASAdinCubeRewardedVideoAdapter extends SASAdinCubeAdapterBase implements SASMediationRewardedVideoAdapter {

    // internal TAG string for console output
    private static final String TAG = SASAdinCubeRewardedVideoAdapter.class.getSimpleName();

    private Activity activity;

    @Override
    public void requestRewardedVideoAd(@NonNull Context context, @NonNull String serverParametersString, @NonNull Map<String, String> clientParameters,
                                       @NonNull final SASMediationRewardedVideoAdapterListener rewardedVideoAdapterListener) {
        SASUtil.logDebug(TAG, "SASAdinCubeRewardedVideoAdapter requestAd");

        // AdInCube interstitial require that context be an Activity
        if (!(context instanceof Activity)) {
            rewardedVideoAdapterListener.adRequestFailed("The AdinCube SDK requires that the passed Context" +
                    "to load rewarded video ads be an Activity ", false);
            return;
        }

        activity = (Activity) context;

        configureAdRequest(context, serverParametersString, clientParameters);

        // create AdInCube rewarded video listener to catch events
        AdinCubeRewardedEventListener rewardedEventListener = new AdinCubeRewardedEventListener() {

            @Override
            public void onAdFetched() {
                SASUtil.logDebug(TAG, "AdinCube onAdFetched");
                rewardedVideoAdapterListener.onRewardedVideoLoaded();
            }

            @Override
            public void onFetchError(String s) {
                SASUtil.logDebug(TAG, "AdinCube onFetchError");
                rewardedVideoAdapterListener.adRequestFailed(s, true);
            }

            @Override
            public void onAdShown() {
                SASUtil.logDebug(TAG, "AdinCube onAdShown");
                rewardedVideoAdapterListener.onRewardedVideoShown();
            }

            @Override
            public void onError(String s) {
                SASUtil.logDebug(TAG, "AdinCube onError");
                rewardedVideoAdapterListener.onRewardedVideoFailedToShow(s);
            }

            @Override
            public void onAdCompleted() {
                SASUtil.logDebug(TAG, "AdinCube onAdCompleted");

                // notify Smart SDK of earned reward
                rewardedVideoAdapterListener.onReward(null);
            }

            @Override
            public void onAdClicked() {
                SASUtil.logDebug(TAG, "AdinCube onAdClicked");
                rewardedVideoAdapterListener.onAdClicked();
            }

            @Override
            public void onAdHidden() {
                SASUtil.logDebug(TAG, "AdinCube onAdHidden");
                rewardedVideoAdapterListener.onAdClosed();
            }
        };

        AdinCube.Rewarded.setEventListener(rewardedEventListener);
        AdinCube.Rewarded.fetch(activity);
    }

    @Override
    public void showRewardedVideoAd() throws Exception {
        if (needToShowConsentDialog && !consentWasShown) {
            AdinCube.UserConsent.setEventListener(new AdinCubeUserConsentEventListener() {
                @Override
                public void onAccepted() {
                    SASUtil.logDebug(TAG, "AdinCube rewarded GDPR onAccepted");
                    showRewardedIfReady();
                }

                @Override
                public void onDeclined() {
                    SASUtil.logDebug(TAG, "AdinCube rewarded GDPR onDeclined");
                    showRewardedIfReady();
                }

                @Override
                public void onError(String s) {
                    SASUtil.logDebug(TAG, "AdinCube rewarded GDPR onError: " + s);
                    showRewardedIfReady();
                }
            });

            showConsentDialogIfNeeded(activity);
        } else {
            showRewardedIfReady();
        }
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
