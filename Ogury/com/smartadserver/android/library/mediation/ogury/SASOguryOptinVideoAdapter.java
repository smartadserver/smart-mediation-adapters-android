package com.smartadserver.android.library.mediation.ogury;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ogury.ed.OguryOptinVideoAd;
import com.ogury.ed.OguryOptinVideoAdListener;
import com.ogury.ed.OguryReward;
import com.smartadserver.android.library.mediation.SASMediationRewardedVideoAdapter;
import com.smartadserver.android.library.mediation.SASMediationRewardedVideoAdapterListener;
import com.smartadserver.android.library.model.SASReward;

import java.util.Map;

/**
 * Mediation adapter class for Ogury "Optin video" ad format
 */
public class SASOguryOptinVideoAdapter extends SASOguryAdapterBase implements SASMediationRewardedVideoAdapter, OguryOptinVideoAdListener {

    static private final String TAG = SASOguryOptinVideoAdapter.class.getSimpleName();

    // Ogury optin video manager instance
    @Nullable
    private OguryOptinVideoAd optinVideoAd;

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
                                       @NonNull Map<String, Object> clientParameters,
                                       @NonNull SASMediationRewardedVideoAdapterListener rewardedVideoAdapterListener) {

        Log.d(TAG, "SASOguryOptinVideoAdapter adRequest");

        // common configuration
        configureAdRequest(context, serverParametersString, rewardedVideoAdapterListener);

        // Instantiate the Presage Optin video manager
        optinVideoAd = new OguryOptinVideoAd(context, getAdUnitID(serverParametersString));
        optinVideoAd.setListener(this);
        optinVideoAd.load();
    }

    @Override
    public void showRewardedVideoAd() throws Exception {
        if (optinVideoAd != null && optinVideoAd.isLoaded()) {
            optinVideoAd.show();
        }
    }

    @Override
    public void onAdLoaded() {
        Log.d(TAG, "Ogury optin video onAdLoaded");
        if (mediationAdapterListener != null) {
            ((SASMediationRewardedVideoAdapterListener) mediationAdapterListener).onRewardedVideoLoaded();
        }
    }

    @Override
    public void onAdDisplayed() {
        super.onAdDisplayed();
        if (mediationAdapterListener != null) {
            ((SASMediationRewardedVideoAdapterListener) mediationAdapterListener).onRewardedVideoShown();
        }
    }

    @Override
    public void onAdRewarded(OguryReward oguryReward) {
        Log.d(TAG, "OguryOptinVideoAdListener onAdRewarded");

        // notify Smart SDK of earned reward, if reward is numerical
        try {
            if (mediationAdapterListener != null) {
                ((SASMediationRewardedVideoAdapterListener) mediationAdapterListener).onReward(
                        new SASReward(oguryReward.getName(), Double.parseDouble(oguryReward.getValue())));
            }
        } catch (NumberFormatException ignored) {}
    }

    @Override
    public void onDestroy() {
        if (optinVideoAd != null) {
            // workaround for Ogury not supporting nullification of callback
            //optinVideo.setOptinVideoCallback(null);
            optinVideoAd = null;
        }
    }
}
