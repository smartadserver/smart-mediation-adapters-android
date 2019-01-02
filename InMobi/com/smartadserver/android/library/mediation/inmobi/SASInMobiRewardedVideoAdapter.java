package com.smartadserver.android.library.mediation.inmobi;

import android.content.Context;
import android.support.annotation.NonNull;

import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiInterstitial;
import com.inmobi.ads.listeners.InterstitialAdEventListener;
import com.smartadserver.android.library.exception.SASAdDisplayException;
import com.smartadserver.android.library.mediation.SASMediationInterstitialAdapter;
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

    private static final String TAG = "SASInMobiRewardedVideoAdapter";

    private InMobiInterstitial inMobiInterstitial;

    @Override
    public void requestRewardedVideoAd(@NonNull Context context, @NonNull String serverParametersString, @NonNull Map<String, String> clientParameters,
                                       @NonNull final SASMediationRewardedVideoAdapterListener rewardedVideoAdapterListener) {

        configureAdRequest(context, serverParametersString, clientParameters);

        long placementID = getPlacementId(serverParametersString);

        // create InMobi interstitial listener
        InterstitialAdEventListener interstitialAdEventListener = new InterstitialAdEventListener() {

            // an array of rewards
            ArrayList<SASReward> rewards = new ArrayList<>();

            @Override
            public void onAdLoadSucceeded(InMobiInterstitial inMobiInterstitial) {
                super.onAdLoadSucceeded(inMobiInterstitial);
                SASUtil.logDebug(TAG, "InMobi onAdLoadSucceeded for rewarded video");
                rewardedVideoAdapterListener.onRewardedVideoLoaded();
            }

            @Override
            public void onAdLoadFailed(InMobiInterstitial inMobiInterstitial, InMobiAdRequestStatus inMobiAdRequestStatus) {
                super.onAdLoadFailed(inMobiInterstitial, inMobiAdRequestStatus);
                SASUtil.logDebug(TAG, "InMobi onAdLoadFailed for rewarded video");

                boolean isNoFill = inMobiAdRequestStatus.getStatusCode() == InMobiAdRequestStatus.StatusCode.NO_FILL;
                rewardedVideoAdapterListener.adRequestFailed(inMobiAdRequestStatus.getMessage(), isNoFill);
            }

            @Override
            public void onAdReceived(InMobiInterstitial inMobiInterstitial) {
                super.onAdReceived(inMobiInterstitial);
                SASUtil.logDebug(TAG, "InMobi onAdReceived for rewarded video");
            }

            @Override
            public void onAdClicked(InMobiInterstitial inMobiInterstitial, Map<Object, Object> map) {
                super.onAdClicked(inMobiInterstitial, map);
                SASUtil.logDebug(TAG, "InMobi onAdClicked for rewarded video");
                rewardedVideoAdapterListener.onAdClicked();
            }

            @Override
            public void onAdWillDisplay(InMobiInterstitial inMobiInterstitial) {
                super.onAdWillDisplay(inMobiInterstitial);
                SASUtil.logDebug(TAG, "InMobi onAdWillDisplay for rewarded video");
            }

            @Override
            public void onAdDisplayed(InMobiInterstitial inMobiInterstitial) {
                super.onAdDisplayed(inMobiInterstitial);
                SASUtil.logDebug(TAG, "InMobi onAdDisplayed for rewarded video");
                rewardedVideoAdapterListener.onRewardedVideoShown();
            }

            @Override
            public void onAdDisplayFailed(InMobiInterstitial inMobiInterstitial) {
                super.onAdDisplayFailed(inMobiInterstitial);
                SASUtil.logDebug(TAG, "InMobi onAdDisplayFailed for rewarded video");
                rewardedVideoAdapterListener.onRewardedVideoFailedToShow("no reason available");
            }

            @Override
            public void onAdDismissed(InMobiInterstitial inMobiInterstitial) {
                super.onAdDismissed(inMobiInterstitial);
                SASUtil.logDebug(TAG, "InMobi onAdDismissed for rewarded video");

                // fire rewards
                for (SASReward reward : rewards) {
                    rewardedVideoAdapterListener.onReward(reward);
                }
                rewards.clear();

                rewardedVideoAdapterListener.onAdClosed();
            }

            @Override
            public void onUserLeftApplication(InMobiInterstitial inMobiInterstitial) {
                super.onUserLeftApplication(inMobiInterstitial);
                SASUtil.logDebug(TAG, "InMobi onUserLeftApplication for rewarded video");
            }

            @Override
            public void onRewardsUnlocked(InMobiInterstitial inMobiInterstitial, Map<Object, Object> map) {
                super.onRewardsUnlocked(inMobiInterstitial, map);
                SASUtil.logDebug(TAG, "InMobi onRewardsUnlocked for rewarded video");

                rewards = new ArrayList<>();
                for (Object key : map.keySet()) {
                    String value = map.get(key).toString();
                    try {
                        rewards.add(new SASReward(key.toString(), Double.parseDouble(value)));
                    } catch (Exception e) {
                        SASUtil.logDebug(TAG, "Unparsable reward for key: " + key + " and value: " + value);
                    }
                }
            }

            @Override
            public void onRequestPayloadCreated(byte[] bytes) {
                super.onRequestPayloadCreated(bytes);
                SASUtil.logDebug(TAG, "InMobi onRequestPayloadCreated for rewarded video");
            }

            @Override
            public void onRequestPayloadCreationFailed(InMobiAdRequestStatus inMobiAdRequestStatus) {
                super.onRequestPayloadCreationFailed(inMobiAdRequestStatus);
                SASUtil.logDebug(TAG, "InMobi onRequestPayloadCreated for rewarded video");
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
                    if (inMobiInterstitial.isReady()) {
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
