package com.smartadserver.android.library.mediation.adcolony;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import com.adcolony.sdk.AdColony;
import com.adcolony.sdk.AdColonyInterstitial;
import com.adcolony.sdk.AdColonyInterstitialListener;
import com.adcolony.sdk.AdColonyReward;
import com.adcolony.sdk.AdColonyRewardListener;
import com.adcolony.sdk.AdColonyZone;
import com.smartadserver.android.library.mediation.SASMediationRewardedVideoAdapter;
import com.smartadserver.android.library.mediation.SASMediationRewardedVideoAdapterListener;
import com.smartadserver.android.library.model.SASReward;
import com.smartadserver.android.library.util.SASUtil;

import java.util.Map;

/**
 * Mediation adapter class for MoPub interstitial format
 */
public class SASAdColonyRewardedVideoAdapter extends SASAdColonyAdapterBase implements SASMediationRewardedVideoAdapter {


    // tag for logging purposes
    static private final String TAG = SASAdColonyInterstitialAdapter.class.getSimpleName();

    // AdColony interstitial instance
    AdColonyInterstitial adColonyInterstitial;


    /**
     * Requests a mediated interstitial ad asynchronously
     *
     * @param context                      The {@link Context} needed by the mediation SDK to make the ad request.
     * @param serverParametersString       a String containing all needed parameters (as returned by Smart ad delivery) to make the mediation ad call.
     * @param clientParameters             additional client-side parameters (user specific, like location).
     * @param rewardedVideoAdapterListener the {@link SASMediationRewardedVideoAdapterListener} provided to this
     *                                     {@link com.smartadserver.android.library.mediation.SASMediationAdapter} to notify the Smart SDK of events
     */
    @Override
    public void requestRewardedVideoAd(@NonNull Context context, @NonNull String serverParametersString, @NonNull Map<String, String> clientParameters,
                                       @NonNull final SASMediationRewardedVideoAdapterListener rewardedVideoAdapterListener) {
        if (!(context instanceof Activity)) {
            rewardedVideoAdapterListener.adRequestFailed("AdColony ad mediation requires that the Smart AdServer SASAdview " +
                    " be created with an Activity as context parameter", false);
            return;
        }

        // extract AdColony specific parameters
        final String zoneID = serverParametersString.split("/")[1];

        // prepare ad request
        configureAdRequest((Activity) context, serverParametersString, clientParameters);

        // instantiate adcolony listener
        AdColonyInterstitialListener listener = new AdColonyInterstitialListener() {
            @Override
            public void onRequestFilled(AdColonyInterstitial ad) {
                SASUtil.logDebug(TAG, "AdColony onRequestFilled for rewarded video");

                adColonyInterstitial = ad;
                rewardedVideoAdapterListener.onRewardedVideoLoaded();
            }

            @Override
            public void onRequestNotFilled(AdColonyZone zone) {
                SASUtil.logDebug(TAG, "AdColony onRequestNotFilled for rewarded video. Zone :" + zone);
                rewardedVideoAdapterListener.adRequestFailed("Cannot load rewarded video from AdColony!", true);
            }

            @Override
            public void onExpiring(AdColonyInterstitial ad) {
                SASUtil.logDebug(TAG, "AdColony onExpiring for rewarded video");

                // If the interstitial is expiring, we need to get a new one.
                // This can be problematic if the developer uses several appID for the same app since
                // this value is static and defined only once during the 'requestAd' call.
//                AdColony.requestInterstitial(ad.getZoneID(), this, null);
                onDestroy();
                adColonyInterstitial = null;
            }

            @Override
            public void onOpened(AdColonyInterstitial ad) {
                SASUtil.logDebug(TAG, "AdColony onOpened for rewarded video");
                rewardedVideoAdapterListener.onRewardedVideoShown();
            }

            @Override
            public void onClosed(AdColonyInterstitial ad) {
                SASUtil.logDebug(TAG, "AdColony onClosed for rewarded video");
                rewardedVideoAdapterListener.onAdClosed();
            }

            @Override
            public void onIAPEvent(AdColonyInterstitial ad, String product_id, int engagement_type) {
                SASUtil.logDebug(TAG, "AdColony onIAPEvent for rewarded video");
            }

            @Override
            public void onLeftApplication(AdColonyInterstitial ad) {
                SASUtil.logDebug(TAG, "AdColony onLeftApplication for rewarded video");
            }

            @Override
            public void onClicked(AdColonyInterstitial ad) {
                SASUtil.logDebug(TAG, "AdColony onClicked for rewarded video");
                rewardedVideoAdapterListener.onAdClicked();
            }
        };

        AdColony.setRewardListener(new AdColonyRewardListener() {
            @Override
            public void onReward(AdColonyReward adColonyReward) {

                String currency = adColonyReward.getRewardName();
                int amount = adColonyReward.getRewardAmount();

                SASUtil.logDebug(TAG, "AdColony onReward for rewarded video: label:" + currency + " amount:" + amount);

                if (amount > 0) {
                    rewardedVideoAdapterListener.onReward(new SASReward(currency, amount));
                }
            }
        });

        // perform ad request
        AdColony.requestInterstitial(zoneID, listener, null);

    }

    @Override
    public void showRewardedVideoAd() throws Exception {
        if (adColonyInterstitial != null) {
            adColonyInterstitial.show();
        } else {
            throw new Exception("No AdColony rewarded video available to show (might have expired). Please make another ad call");
        }
    }

    @Override
    public void onDestroy() {
        SASUtil.logDebug(TAG, "AdColony onDestroy() for rewarded video");
        if (adColonyInterstitial != null) {
            adColonyInterstitial.destroy();
            adColonyInterstitial = null;
        }
    }
}
