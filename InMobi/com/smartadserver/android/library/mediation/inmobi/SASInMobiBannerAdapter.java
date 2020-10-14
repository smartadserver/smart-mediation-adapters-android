package com.smartadserver.android.library.mediation.inmobi;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.ViewGroup;

import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiBanner;
import com.inmobi.ads.listeners.BannerAdEventListener;
import com.smartadserver.android.library.mediation.SASMediationBannerAdapter;
import com.smartadserver.android.library.mediation.SASMediationBannerAdapterListener;
import com.smartadserver.android.library.util.SASUtil;


import java.util.Map;

/**
 * Mediation adapter class for InMobi banner format
 */
public class SASInMobiBannerAdapter extends SASInMobiAdapterBase implements SASMediationBannerAdapter {


    private static final String TAG = SASInMobiBannerAdapter.class.getSimpleName();

    private BannerAdEventListener bannerAdEventListener;

    @Override
    public void requestBannerAd(@NonNull Context context, @NonNull String serverParameterString,
                                @NonNull Map<String, String> clientParameters, @NonNull final SASMediationBannerAdapterListener bannerAdapterListener) {


        configureAdRequest(context, serverParameterString, clientParameters);

        long placementID = getPlacementId(serverParameterString);

        // Create inMobi banner view with appropriate size.
        InMobiBanner bannerAdView = new InMobiBanner(context, placementID);
        bannerAdView.setAnimationType(InMobiBanner.AnimationType.ANIMATION_OFF);

        int width = Integer.parseInt(clientParameters.get(AD_VIEW_WIDTH_KEY));
        int height = Integer.parseInt(clientParameters.get(AD_VIEW_HEIGHT_KEY));

        ViewGroup.LayoutParams lParams = new ViewGroup.LayoutParams(width, height);
        bannerAdView.setLayoutParams(lParams);

        // debug color
        if (SASUtil.debugModeEnabled) {
            bannerAdView.setBackgroundColor(Color.CYAN);
        }

        // create inmobi banner listener
        bannerAdEventListener = new BannerAdEventListener() {
            @Override
            public void onAdLoadSucceeded(InMobiBanner inMobiBanner) {
                Log.d(TAG, "InMobi onAdLoadSucceeded for banner");
                bannerAdapterListener.onBannerLoaded(inMobiBanner);
            }

            @Override
            public void onAdLoadFailed(InMobiBanner inMobiBanner, InMobiAdRequestStatus inMobiAdRequestStatus) {
                Log.d(TAG, "InMobi  onAdLoadFailed for banner");
                boolean isNoAd = inMobiAdRequestStatus.getStatusCode() == InMobiAdRequestStatus.StatusCode.NO_FILL;
                bannerAdapterListener.adRequestFailed(inMobiAdRequestStatus.getMessage() + "(" + inMobiAdRequestStatus.getStatusCode() + ")", isNoAd);
            }

            @Override
            public void onAdClicked(InMobiBanner inMobiBanner, Map<Object, Object> map) {
                Log.d(TAG, "InMobi onAdClicked for banner");
                bannerAdapterListener.onAdClicked();
            }

            @Override
            public void onAdDisplayed(InMobiBanner inMobiBanner) {
                Log.d(TAG, "InMobi onAdDisplayed for banner");
                bannerAdapterListener.onAdFullScreen();
            }

            @Override
            public void onAdDismissed(InMobiBanner inMobiBanner) {
                Log.d(TAG, "InMobi onAdDismissed for banner");
                bannerAdapterListener.onAdClosed();
            }

            @Override
            public void onUserLeftApplication(InMobiBanner inMobiBanner) {
                Log.d(TAG, "InMobi onUserLeftApplication for banner");
            }

            @Override
            public void onRewardsUnlocked(InMobiBanner inMobiBanner, Map<Object, Object> map) {
                Log.d(TAG, "InMobi onRewardsUnlocked for banner");
            }

            @Override
            public void onRequestPayloadCreated(byte[] bytes) {
                Log.d(TAG, "InMobi onRequestPayloadCreated for banner");
            }

            @Override
            public void onRequestPayloadCreationFailed(InMobiAdRequestStatus inMobiAdRequestStatus) {
                Log.d(TAG, "InMobi onRequestPayloadCreationFailed for banner");
            }
        };

        // configure inmobi banner view
        bannerAdView.setListener(bannerAdEventListener);
        bannerAdView.setEnableAutoRefresh(false);

        // set request params
        bannerAdView.setExtras(inMobiParametersMap);

        // perform ad request
        bannerAdView.load();
    }

    @Override
    public void onDestroy() {
        // nothing to do here
    }

}
