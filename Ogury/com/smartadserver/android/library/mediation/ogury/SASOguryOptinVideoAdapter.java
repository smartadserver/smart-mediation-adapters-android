package com.smartadserver.android.library.mediation.ogury;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.smartadserver.android.library.mediation.SASMediationInterstitialAdapterListener;
import com.smartadserver.android.library.mediation.SASMediationRewardedVideoAdapter;
import com.smartadserver.android.library.mediation.SASMediationRewardedVideoAdapterListener;
import com.smartadserver.android.library.model.SASReward;

import java.util.Map;

import io.presage.common.AdConfig;
import io.presage.common.network.models.RewardItem;
import io.presage.interstitial.optinvideo.PresageOptinVideo;
import io.presage.interstitial.optinvideo.PresageOptinVideoCallback;

/**
 * Mediation adapter class for Ogury "Optin video" ad format
 */
public class SASOguryOptinVideoAdapter extends SASOguryAdapterBase implements SASMediationRewardedVideoAdapter, PresageOptinVideoCallback {

    static private final String TAG = SASOguryOptinVideoAdapter.class.getSimpleName();

    // Ogury optin video manager instance
    private PresageOptinVideo optinVideo;

    /**
     * @param context                     the {@link android.content.Context} needed by the mediation SDK to make the ad request
     * @param serverParametersString      a String containing all needed parameters (as returned by Smart ad delivery)
     *                                    to make the mediation ad call
     * @param clientParameters            additional client-side parameters (user specific, like location)
     * @param rewardedVideoAdapterListener the {@link SASMediationRewardedVideoAdapterListener} provided to
     *                                    this {@link com.smartadserver.android.library.mediation.SASMediationAdapter} to notify Smart SDK of events occurring
     */
    @Override
    public void requestRewardedVideoAd(@NonNull Context context,
                                       @NonNull String serverParametersString,
                                       @NonNull Map<String, String> clientParameters,
                                       @NonNull SASMediationRewardedVideoAdapterListener rewardedVideoAdapterListener) {

        Log.d(TAG, "SASOguryOptinVideoAdapter adRequest");

        if (!(context instanceof Activity)) {
            rewardedVideoAdapterListener.adRequestFailed("Ogury ad mediation requires the context to be an Activity for Optin Video format", false);
            return;
        }

        // common configuration
        configureAdRequest(context, serverParametersString, rewardedVideoAdapterListener);

        // Instantiate the Presage Optin video manager
        AdConfig adConfig = new AdConfig(getAdUnitID(serverParametersString));
        optinVideo = new PresageOptinVideo(context, adConfig);
        optinVideo.setOptinVideoCallback(this);
        optinVideo.load();
    }

    @Override
    public void showRewardedVideoAd() throws Exception {
        if (optinVideo != null && optinVideo.isLoaded()) {
            optinVideo.show();
        }
    }

    @Override
    public void onAdLoaded() {
        super.onAdLoaded();
        ((SASMediationRewardedVideoAdapterListener)mediationAdapterListener).onRewardedVideoLoaded();
    }

    @Override
    public void onAdDisplayed() {
        super.onAdDisplayed();
        ((SASMediationRewardedVideoAdapterListener)mediationAdapterListener).onRewardedVideoShown();
    }

    @Override
    public void onAdRewarded(RewardItem rewardItem) {
        Log.d(TAG, "optinVideoCallback onAdRewarded");

        // notify Smart SDK of earned reward, if reward is numerical
        try {
            ((SASMediationRewardedVideoAdapterListener)mediationAdapterListener).onReward(new SASReward(rewardItem.getName(), Double.parseDouble(rewardItem.getValue())));
        } catch (NumberFormatException ignored) {}
    }

    @Override
    public void onDestroy() {
        if (optinVideo != null) {
            // workaround for Ogury not supporting nullification of callback
            //optinVideo.setOptinVideoCallback(null);
            optinVideo = null;
        }
    }
}
