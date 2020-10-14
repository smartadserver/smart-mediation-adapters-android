package com.smartadserver.android.library.mediation.tapjoy;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;

import com.smartadserver.android.library.mediation.SASMediationInterstitialAdapter;
import com.smartadserver.android.library.mediation.SASMediationInterstitialAdapterListener;

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
 * Mediation adapter class for TapJoy interstitial ad format
 */
public class SASTapjoyInterstitialAdapter implements SASMediationInterstitialAdapter {

    static private final String TAG = SASTapjoyInterstitialAdapter.class.getSimpleName();

    private TJPlacement tjPlacement;

    /**
     * @param context                     the {@link android.content.Context} needed by the mediation SDK to make the ad request
     * @param serverParametersString      a String containing all needed parameters (as returned by Smart ad delivery)
     *                                    to make the mediation ad call
     * @param clientParameters            additional client-side parameters (user specific, like location)
     * @param interstitialAdapterListener the {@link SASMediationInterstitialAdapterListener} provided to
     *                                    this {@link com.smartadserver.android.library.mediation.SASMediationAdapter} to notify Smart SDK of events occurring
     */
    @Override
    public void requestInterstitialAd(@NonNull final Context context, @NonNull String serverParametersString,
                                      @NonNull Map<String, String> clientParameters, @NonNull final SASMediationInterstitialAdapterListener interstitialAdapterListener) {
        Log.d(TAG, "SASTapjoyInterstitialAdapter adRequest");

        // Retrieve placement info -- Here serverParametersString is "SDKKey/placementName"
        String[] placementInfo = serverParametersString.split("/");

        // Check that the placement info are correctly set
        if (placementInfo.length != 2 || placementInfo[0].length() == 0 || placementInfo[1].length() == 0) {
            interstitialAdapterListener.adRequestFailed("The Tapjoy SDKKey and/or placementName is not correctly set", false);
        }

        // Pass GDPR consent if applicable
        String value = clientParameters.get(GDPR_APPLIES_KEY);
        if (value != null) {
            // Smart determined GDPR applies or not
            Tapjoy.subjectToGDPR(!("false".equalsIgnoreCase(value)));
        } else {
            // leave Tapjoy make its choice on whether GDPR applies or not
        }

        // now find if we have the user consent for ad purpose, and pass it to TapJoy
        String smartConsent = clientParameters.get(GDPR_CONSENT_KEY);
        if (smartConsent != null) {
            Tapjoy.setUserConsent(smartConsent);
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
                    interstitialAdapterListener.adRequestFailed("Request succeed but content is not available (noad)", true);
                }
            }

            @Override
            public void onRequestFailure(TJPlacement tjPlacement, TJError tjError) {
                Log.d(TAG, "placementListener onRequestFailure");
                interstitialAdapterListener.adRequestFailed(tjError.message, false);
            }

            @Override
            public void onContentReady(TJPlacement tjPlacement) {
                Log.d(TAG, "placementListener onContentReady");
                interstitialAdapterListener.onInterstitialLoaded();
            }

            @Override
            public void onContentShow(TJPlacement tjPlacement) {
                Log.d(TAG, "placementListener onContentShow");
                // call the listener only when the video start to avoid counting pixel if the video have an error and does not start
            }

            @Override
            public void onContentDismiss(TJPlacement tjPlacement) {
                Log.d(TAG, "placementListener onContentDismiss");
                interstitialAdapterListener.onAdClosed();
            }

            @Override
            public void onPurchaseRequest(TJPlacement tjPlacement, TJActionRequest tjActionRequest, String s) {
                Log.d(TAG, "placementListener onPurchaseRequest");
            }

            @Override
            public void onRewardRequest(TJPlacement tjPlacement, TJActionRequest tjActionRequest, String s, int i) {
                Log.d(TAG, "placementListener onRewardRequest");
            }
        };

        // Instantiate Tapjoy Placement Video Listener
        final TJPlacementVideoListener placementVideoListener = new TJPlacementVideoListener() {
            @Override
            public void onVideoStart(TJPlacement tjPlacement) {
                Log.d(TAG, "placementVideoListener onVideoStart");
                interstitialAdapterListener.onInterstitialShown();
            }

            @Override
            public void onVideoError(TJPlacement tjPlacement, String s) {
                Log.d(TAG, "placementVideoListener onVideoError");
                interstitialAdapterListener.onInterstitialFailedToShow(s);
            }

            @Override
            public void onVideoComplete(TJPlacement tjPlacement) {
                Log.d(TAG, "placementVideoListener onVideoComplete");
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
                interstitialAdapterListener.adRequestFailed("The Tapjor SDK failed to connect", false);
            }
        });
    }

    @Override
    public void showInterstitial() throws Exception {
        if (tjPlacement != null && tjPlacement.isContentAvailable()) {
            tjPlacement.showContent();
        }
    }

    @Override
    public void onDestroy() {
        tjPlacement = null;
    }
}
