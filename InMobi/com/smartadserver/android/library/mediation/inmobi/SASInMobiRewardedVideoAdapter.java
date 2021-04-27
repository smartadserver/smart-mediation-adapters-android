package com.smartadserver.android.library.mediation.inmobi;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;

import com.inmobi.ads.AdMetaInfo;
import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiInterstitial;
import com.inmobi.ads.listeners.InterstitialAdEventListener;
import com.smartadserver.android.library.exception.SASAdDisplayException;
import com.smartadserver.android.library.mediation.SASMediationRewardedVideoAdapter;
import com.smartadserver.android.library.mediation.SASMediationRewardedVideoAdapterListener;
import com.smartadserver.android.library.model.SASReward;
import com.smartadserver.android.library.util.SASUtil;


import java.util.ArrayList;
import java.util.Map;

/**
 * Mediation adapter class for InMobi rewarded video format
 * NOTE: As InMobi interstitial format already handles rewards, an InMobiInterstitial instance is also used.
 */
public class SASInMobiRewardedVideoAdapter extends SASInMobiAdapterBase implements SASMediationRewardedVideoAdapter {

    private static final String TAG = SASInMobiRewardedVideoAdapter.class.getSimpleName();

    @Nullable
    private InMobiInterstitial inMobiInterstitial;

    @Override
    public void requestRewardedVideoAd(@NonNull Context context,
                                       @NonNull String serverParametersString,
                                       @NonNull Map<String, Object> clientParameters,
                                       @NonNull final SASMediationRewardedVideoAdapterListener rewardedVideoAdapterListener) {

        configureAdRequest(context, serverParametersString, clientParameters);

        long placementID = getPlacementId(serverParametersString);

        // create InMobi interstitial listener
        InterstitialAdEventListener interstitialAdEventListener = new InterstitialAdEventListener() {

            // an array of rewards
            ArrayList<SASReward> rewards = new ArrayList<>();

            @Override
            public void onAdLoadSucceeded(@NonNull InMobiInterstitial inMobiInterstitial, @NonNull AdMetaInfo adMetaInfo) {
                super.onAdLoadSucceeded(inMobiInterstitial, adMetaInfo);
                Log.d(TAG, "InMobi onAdLoadSucceeded for rewarded video");
                rewardedVideoAdapterListener.onRewardedVideoLoaded();
            }

            @Override
            public void onAdLoadFailed(@NonNull InMobiInterstitial inMobiInterstitial, @NonNull InMobiAdRequestStatus inMobiAdRequestStatus) {
                super.onAdLoadFailed(inMobiInterstitial, inMobiAdRequestStatus);
                Log.d(TAG, "InMobi onAdLoadFailed for rewarded video");

                boolean isNoFill = inMobiAdRequestStatus.getStatusCode() == InMobiAdRequestStatus.StatusCode.NO_FILL;
                rewardedVideoAdapterListener.adRequestFailed(inMobiAdRequestStatus.getMessage(), isNoFill);
            }

            @Override
            public void onAdFetchSuccessful(@NonNull InMobiInterstitial inMobiInterstitial, @NonNull AdMetaInfo adMetaInfo) {
                super.onAdFetchSuccessful(inMobiInterstitial, adMetaInfo);
                Log.d(TAG, "InMobi onAdFetchSuccessful for rewarded video");
            }

            @Override
            public void onAdFetchFailed(@NonNull InMobiInterstitial inMobiInterstitial, @NonNull InMobiAdRequestStatus inMobiAdRequestStatus) {
                super.onAdFetchFailed(inMobiInterstitial, inMobiAdRequestStatus);
                Log.d(TAG, "InMobi onAdFetchFailed for rewarded video");
            }

            @Override
            public void onAdWillDisplay(@NonNull InMobiInterstitial inMobiInterstitial) {
                super.onAdWillDisplay(inMobiInterstitial);
                Log.d(TAG, "InMobi onAdWillDisplay for rewarded video");
            }

            @Override
            public void onAdDisplayed(@NonNull InMobiInterstitial inMobiInterstitial, @NonNull AdMetaInfo adMetaInfo) {
                super.onAdDisplayed(inMobiInterstitial, adMetaInfo);
                Log.d(TAG, "InMobi onAdDisplayed for rewarded video");
                rewardedVideoAdapterListener.onRewardedVideoShown();
            }

            @Override
            public void onAdDisplayFailed(@NonNull InMobiInterstitial inMobiInterstitial) {
                super.onAdDisplayFailed(inMobiInterstitial);
                Log.d(TAG, "InMobi onAdDisplayFailed for rewarded video");
                rewardedVideoAdapterListener.onRewardedVideoFailedToShow("no reason available");
            }

            @Override
            public void onAdDismissed(@NonNull InMobiInterstitial inMobiInterstitial) {
                super.onAdDismissed(inMobiInterstitial);
                Log.d(TAG, "InMobi onAdDismissed for rewarded video");

                // fire rewards
                for (SASReward reward : rewards) {
                    rewardedVideoAdapterListener.onReward(reward);
                }
                rewards.clear();

                rewardedVideoAdapterListener.onAdClosed();
            }

            @Override
            public void onUserLeftApplication(@NonNull InMobiInterstitial inMobiInterstitial) {
                super.onUserLeftApplication(inMobiInterstitial);
                Log.d(TAG, "InMobi onUserLeftApplication for rewarded video");
            }

            @Override
            public void onRewardsUnlocked(@NonNull InMobiInterstitial inMobiInterstitial, Map<Object, Object> map) {
                super.onRewardsUnlocked(inMobiInterstitial, map);
                Log.d(TAG, "InMobi onRewardsUnlocked for rewarded video");

                rewards = new ArrayList<>();
                for (Object key : map.keySet()) {
                    String value = map.get(key).toString();
                    try {
                        rewards.add(new SASReward(key.toString(), Double.parseDouble(value)));
                    } catch (Exception e) {
                        Log.d(TAG, "Unparsable reward for key: " + key + " and value: " + value);
                    }
                }
            }

            @Override
            public void onAdClicked(@NonNull InMobiInterstitial inMobiInterstitial, Map<Object, Object> map) {
                super.onAdClicked(inMobiInterstitial, map);
                Log.d(TAG, "InMobi onAdClicked for rewarded video");
                rewardedVideoAdapterListener.onAdClicked();
            }

            @Override
            public void onRequestPayloadCreated(byte[] bytes) {
                super.onRequestPayloadCreated(bytes);
                Log.d(TAG, "InMobi onRequestPayloadCreated for rewarded video");
            }

            @Override
            public void onRequestPayloadCreationFailed(@NonNull InMobiAdRequestStatus inMobiAdRequestStatus) {
                super.onRequestPayloadCreationFailed(inMobiAdRequestStatus);
                Log.d(TAG, "InMobi onRequestPayloadCreationFailed for rewarded video");
            }
        };

        inMobiInterstitial = new InMobiInterstitial(context, placementID, interstitialAdEventListener);

        // set request params
        inMobiInterstitial.setExtras(inMobiParametersMap);

        // load interstitial
        inMobiInterstitial.load();

    }

    @Override
    public void showRewardedVideoAd() throws Exception {
        /*
         * Methods of the InMobi's InMobiInterstitial
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
                    if (inMobiInterstitial != null && inMobiInterstitial.isReady()) {
                        // regular interstitial case
                        inMobiInterstitial.show();
                    } else {
                        throw new Exception("No InMobi interstitial ad loaded !");
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
        // Nothing to do here
    }
}
