package com.smartadserver.android.library.mediation.vungle;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.smartadserver.android.library.mediation.SASMediationRewardedVideoAdapter;
import com.smartadserver.android.library.mediation.SASMediationRewardedVideoAdapterListener;
import com.vungle.warren.AdConfig;
import com.vungle.warren.Vungle;
import com.vungle.warren.error.VungleException;

import java.util.Map;

/**
 * Mediation Adapter class for Vungle rewarded video format
 */
public class SASVungleRewardedVideoAdapter extends SASVungleAdapterBase implements SASMediationRewardedVideoAdapter {

    static private final String TAG = SASVungleRewardedVideoAdapter.class.getSimpleName();

    private boolean rewardedVideoShown = false;

    /**
     * Requests and process a Vungle Rewarded video ad.s
     *
     * @param context                      The {@link Context} needed by the mediation SDK to make the ad request.
     * @param serverParametersString       a String containing all needed parameters (as returned by Smart ad delivery)
     * @param clientParameters             additional client-side parameters (user specific, like location).
     * @param rewardedVideoAdapterListener the {@link SASMediationRewardedVideoAdapterListener} provided to this {@link com.smartadserver.android.library.mediation.SASMediationAdapter} to notify the Smart SDK of events
     */
    @Override
    public void requestRewardedVideoAd(@NonNull Context context,
                                       @NonNull String serverParametersString,
                                       @NonNull Map<String, String> clientParameters,
                                       @NonNull final SASMediationRewardedVideoAdapterListener rewardedVideoAdapterListener) {
        Log.d(TAG, "SASVungleRewardedVideoAdapter requestAd");

        // reset rewarded video show status to false
        rewardedVideoShown = false;

        configureAdapter(context, serverParametersString, clientParameters, rewardedVideoAdapterListener);
    }


    @Override
    public void onError(String s, VungleException exception) {
        super.onError(s, exception);

        // check if this is a display error
        if (!rewardedVideoShown && adLoaded) {
            String message = "";
            if (exception != null && exception.getLocalizedMessage() != null) {
                message = exception.getLocalizedMessage();
            }
            ((SASMediationRewardedVideoAdapterListener) mediationAdapterListener).onRewardedVideoFailedToShow(message);
        }
    }

    /**
     * Overriden for interstitial specific needs
     */
    @Override
    public void onSuccess() {
        super.onSuccess();
        Vungle.loadAd(placementID, this);
    }

    @Override
    public void onAdLoad(String id) {
        super.onAdLoad(id);
        ((SASMediationRewardedVideoAdapterListener) mediationAdapterListener).onRewardedVideoLoaded();
    }

    @Override
    public void onAdRewarded(String id) {
        super.onAdRewarded(id);
        // reward is provided at the template level, and managed by SDK directly
        ((SASMediationRewardedVideoAdapterListener) mediationAdapterListener).onReward(null);
    }

    @Override
    public void onAdStart(String s) {
        super.onAdStart(s);
        rewardedVideoShown = true;
        ((SASMediationRewardedVideoAdapterListener) mediationAdapterListener).onRewardedVideoShown();
    }

    @Override
    public void showRewardedVideoAd() throws Exception {
        if (Vungle.canPlayAd(placementID)) {
            Vungle.playAd(placementID, new AdConfig(), this);
        }
    }

    @Override
    public void onDestroy() {
    }
}
