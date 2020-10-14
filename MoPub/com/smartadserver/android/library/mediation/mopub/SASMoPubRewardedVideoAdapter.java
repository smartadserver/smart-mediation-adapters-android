package com.smartadserver.android.library.mediation.mopub;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;

import com.mopub.common.MediationSettings;
import com.mopub.common.MoPub;
import com.mopub.common.MoPubReward;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.mopub.common.privacy.ConsentDialogListener;
import com.mopub.common.privacy.PersonalInfoManager;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubRewardedVideoListener;
import com.mopub.mobileads.MoPubRewardedVideos;
import com.smartadserver.android.library.mediation.SASMediationRewardedVideoAdapter;
import com.smartadserver.android.library.mediation.SASMediationRewardedVideoAdapterListener;
import com.smartadserver.android.library.model.SASReward;


import java.util.Map;
import java.util.Set;

/**
 * Mediation adapter class for MoPub rewarded video format
 */
public class SASMoPubRewardedVideoAdapter implements SASMediationRewardedVideoAdapter {

    static private final String TAG = SASMoPubRewardedVideoAdapter.class.getSimpleName();

    private static boolean initMoPubDone = false;

    private String adUnitID = "";

    // GDPR related
    private boolean needToShowConsentDialog = false;

    /**
     * @param context                      The {@link Context} needed by the mediation SDK to make the ad request.
     * @param serverParametersString       a String containing all needed parameters (as returned by Smart ad delivery) to make the mediation ad call.
     * @param clientParameters             additional client-side parameters (user specific, like location).
     * @param rewardedVideoAdapterListener the {@link SASMediationRewardedVideoAdapterListener} provided to this {@link com.smartadserver.android.library.mediation.SASMediationAdapter} to notify the Smart SDK of events
     */
    @Override
    public void requestRewardedVideoAd(@NonNull final Context context, @NonNull final String serverParametersString, @NonNull final Map clientParameters,
                                       @NonNull final SASMediationRewardedVideoAdapterListener rewardedVideoAdapterListener) {
        Log.d(TAG, "SASMoPubRewardedVideoAdapter adRequest");

        // To request an interstitial using MoPub, the context have to be an Activity.
        if (!(context instanceof Activity)) {
            rewardedVideoAdapterListener.adRequestFailed("Can not get a MoPub rewarded video because its creation context is not an Activity", false);
            return;
        }

        // Here serverParameterString is the MoPub Ad unit id
        adUnitID = serverParametersString;

        // Init MoPub SDK
        if (!initMoPubDone) {
            SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder(serverParametersString)
                    .build();

            SdkInitializationListener initializationListener = new SdkInitializationListener() {
                @Override
                public void onInitializationFinished() {
                    Log.d(TAG, "MoPub onInitializationFinished");
                    initMoPubDone = true;
                    // call requestBannerAd again, with SDK initialized
                    requestRewardedVideoAd(context,serverParametersString,clientParameters,rewardedVideoAdapterListener);
                }
            };

            MoPub.initializeSdk(context, sdkConfiguration, initializationListener);
        } else {
            final PersonalInfoManager personalInfoManager = MoPub.getPersonalInformationManager();
            // store that the consent dialog need to be shown. We will show it AFTER the interstitial display to avoid having the interstitial above the consent dialog.
            needToShowConsentDialog = personalInfoManager != null && personalInfoManager.shouldShowConsentDialog();
            if (needToShowConsentDialog) {
                personalInfoManager.loadConsentDialog(new ConsentDialogListener() {
                    @Override
                    public void onConsentDialogLoaded() {
                        // ok
                    }

                    @Override
                    public void onConsentDialogLoadFailed(@NonNull MoPubErrorCode moPubErrorCode) {
                        Log.d(TAG, "MoPub onConsentDialogLoadFailed");
                    }
                });
            }

            // Instantiate Rewarded Video Listener
            MoPubRewardedVideoListener rewardedVideoListener = new MoPubRewardedVideoListener() {

                @Override
                public void onRewardedVideoLoadSuccess(@NonNull String adUnitId) {
                    Log.d(TAG, "RewardedVideoListener onRewardedVideoLoadSuccess");
                    rewardedVideoAdapterListener.onRewardedVideoLoaded();
                }

                @Override
                public void onRewardedVideoLoadFailure(@NonNull String adUnitId, @NonNull MoPubErrorCode errorCode) {
                    Log.d(TAG, "RewardedVideoListener onRewardedVideoLoadFailure");

                    // check if this is due to a No Ad
                    boolean isNoAd = errorCode == MoPubErrorCode.NO_FILL || errorCode == MoPubErrorCode.NETWORK_NO_FILL;
                    rewardedVideoAdapterListener.adRequestFailed(errorCode.toString(), isNoAd);
                }

                @Override
                public void onRewardedVideoStarted(@NonNull String adUnitId) {
                    Log.d(TAG, "RewardedVideoListener onRewardedVideoStarted");
                    rewardedVideoAdapterListener.onRewardedVideoShown();

                    if (needToShowConsentDialog && personalInfoManager != null) {
                        personalInfoManager.showConsentDialog();
                    }
                }

                @Override
                public void onRewardedVideoPlaybackError(@NonNull String adUnitId, @NonNull MoPubErrorCode errorCode) {
                    Log.d(TAG, "RewardedVideoListener onRewardedVideoPlaybackError");
                    rewardedVideoAdapterListener.onRewardedVideoFailedToShow(errorCode.toString());
                }

                @Override
                public void onRewardedVideoClicked(@NonNull String adUnitId) {
                    Log.d(TAG, "RewardedVideoListener onRewardedVideoClicked");
                    rewardedVideoAdapterListener.onAdClicked();
                }

                @Override
                public void onRewardedVideoClosed(@NonNull String adUnitId) {
                    Log.d(TAG, "RewardedVideoListener onRewardedVideoClosed");
                    rewardedVideoAdapterListener.onAdClosed();
                }

                @Override
                public void onRewardedVideoCompleted(@NonNull Set<String> adUnitIds, @NonNull MoPubReward reward) {
                    Log.d(TAG, "RewardedVideoListener onRewardedVideoCompleted");

                    // notify Smart SDK of earned reward
                    rewardedVideoAdapterListener.onReward(new SASReward(reward.getLabel(), reward.getAmount()));
                }
            };

            // Instantiate MoPub Rewarded Video
            MoPubRewardedVideos.setRewardedVideoListener(rewardedVideoListener);
            MoPubRewardedVideos.loadRewardedVideo(adUnitID, null, new MediationSettings[0]);
        }
    }

    @Override
    public void showRewardedVideoAd() throws Exception {
        if (MoPubRewardedVideos.hasRewardedVideo(adUnitID)) {
            MoPubRewardedVideos.showRewardedVideo(adUnitID);
        }
    }

    @Override
    public void onDestroy() {
        // nothing to do
    }
}
