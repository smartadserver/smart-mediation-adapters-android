package com.smartadserver.android.library.mediation.tapjoy;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;

import com.smartadserver.android.library.mediation.SASMediationRewardedVideoAdapter;
import com.smartadserver.android.library.mediation.SASMediationRewardedVideoAdapterListener;

import com.tapjoy.TJActionRequest;
import com.tapjoy.TJConnectListener;
import com.tapjoy.TJError;
import com.tapjoy.TJPlacement;
import com.tapjoy.TJPlacementListener;
import com.tapjoy.TJPlacementManager;
import com.tapjoy.TJPlacementVideoListener;
import com.tapjoy.Tapjoy;

import java.util.Map;

/**
 * Mediation adapter class for Tapjoy rewarded video ad format
 */
public class SASTapjoyRewardedVideoAdapter implements SASMediationRewardedVideoAdapter {

    static private final String TAG = SASTapjoyRewardedVideoAdapter.class.getSimpleName();

    @Nullable
    private TJPlacement tjPlacement;

    private boolean needReward = false;

    /**
     * @param context                      The {@link Context} needed by the mediation SDK to make the ad request.
     * @param serverParametersString       a String containing all needed parameters (as returned by Smart ad delivery) to make the mediation ad call.
     * @param clientParameters             additional client-side parameters (user specific, like location).
     * @param rewardedVideoAdapterListener the {@link SASMediationRewardedVideoAdapterListener} provided to this {@link com.smartadserver.android.library.mediation.SASMediationAdapter} to notify the Smart SDK of events
     */
    @Override
    public void requestRewardedVideoAd(@NonNull final Context context,
                                       @NonNull String serverParametersString,
                                       @NonNull Map<String, Object> clientParameters,
                                       @NonNull final SASMediationRewardedVideoAdapterListener rewardedVideoAdapterListener) {
        Log.d(TAG, "SASTapjoyInterstitialAdapter adRequest");

        // Retrieve placement info -- Here serverParametersString is "SDKKey/placementName"
        String[] placementInfo = serverParametersString.split("/");

        // Check that the placement info are correctly set
        if (placementInfo.length != 2 || placementInfo[0].length() == 0 || placementInfo[1].length() == 0) {
            rewardedVideoAdapterListener.adRequestFailed("The Tapjoy SDKKey and/or placementName is not correctly set", false);
        }

        String SDKKey = placementInfo[0];
        final String placementName = placementInfo[1];

        // Instantiate the Tapjoy Placement Listener
        final TJPlacementListener placementListener = new TJPlacementListener() {
            @Override
            public void onRequestSuccess(TJPlacement tjPlacement) {
                Log.d(TAG, "placementListener onRequestSuccess");
                // check if the content is available. If not, we have a no ad.
                if (!tjPlacement.isContentAvailable()) {
                    rewardedVideoAdapterListener.adRequestFailed("Request succeed but content is not available (noad)", true);
                }
            }

            @Override
            public void onRequestFailure(TJPlacement tjPlacement, TJError tjError) {
                Log.d(TAG, "placementListener onRequestFailure");
                rewardedVideoAdapterListener.adRequestFailed(tjError.message, false);
            }

            @Override
            public void onContentReady(TJPlacement tjPlacement) {
                Log.d(TAG, "placementListener onContentReady");
                rewardedVideoAdapterListener.onRewardedVideoLoaded();
            }

            @Override
            public void onContentShow(TJPlacement tjPlacement) {
                Log.d(TAG, "placementListener onContentShow");
                // call the listener only when the video start to avoid counting pixel if the video have an error and does not start
            }

            @Override
            public void onContentDismiss(TJPlacement tjPlacement) {
                Log.d(TAG, "placementListener onContentDismiss");
                rewardedVideoAdapterListener.onAdClosed();

                if (needReward) {
                    rewardedVideoAdapterListener.onReward(null);
                }
            }

            @Override
            public void onPurchaseRequest(TJPlacement tjPlacement, TJActionRequest tjActionRequest, String s) {
                Log.d(TAG, "placementListener onPurchaseRequest");
            }

            @Override
            public void onRewardRequest(TJPlacement tjPlacement, TJActionRequest tjActionRequest, String s, int i) {
                Log.d(TAG, "placementListener onRewardRequest");
            }

            @Override
            public void onClick(TJPlacement tjPlacement) {
                Log.d(TAG, "placementListener onClick");
                rewardedVideoAdapterListener.onAdClicked();
            }
        };

        // Instantiate Tapjoy Placement Video Listener
        final TJPlacementVideoListener placementVideoListener = new TJPlacementVideoListener() {
            @Override
            public void onVideoStart(TJPlacement tjPlacement) {
                Log.d(TAG, "placementVideoListener onVideoStart");
                rewardedVideoAdapterListener.onRewardedVideoShown();
            }

            @Override
            public void onVideoError(TJPlacement tjPlacement, String s) {
                Log.d(TAG, "placementVideoListener onVideoError");
                rewardedVideoAdapterListener.onRewardedVideoFailedToShow(s);
            }

            @Override
            public void onVideoComplete(TJPlacement tjPlacement) {
                Log.d(TAG, "placementVideoListener onVideoComplete");

                // Store that the user needs a reward
                needReward = true;
            }
        };

        // Configure the Tapjoy SDK for this call
        Tapjoy.connect(context, SDKKey, null, new TJConnectListener() {
            @Override
            public void onConnectSuccess() {
                Log.d(TAG, "Tapjoy onConnectSuccess");
                tjPlacement = TJPlacementManager.createPlacement(context, placementName, false, placementListener);
                tjPlacement.setVideoListener(placementVideoListener);

                tjPlacement.requestContent();
            }

            @Override
            public void onConnectFailure() {
                Log.d(TAG, "Tapjoy onConnectFailure");
                rewardedVideoAdapterListener.adRequestFailed("The Tapjor SDK failed to connect", false);
            }
        });
    }

    @Override
    public void showRewardedVideoAd() throws Exception {
        if (tjPlacement != null && tjPlacement.isContentAvailable()) {
            tjPlacement.showContent();
        }
    }

    @Override
    public void onDestroy() {
        tjPlacement = null;
    }
}
