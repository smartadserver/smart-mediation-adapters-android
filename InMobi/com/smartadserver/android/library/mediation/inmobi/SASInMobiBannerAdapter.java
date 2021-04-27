package com.smartadserver.android.library.mediation.inmobi;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;
import android.view.ViewGroup;

import com.inmobi.ads.AdMetaInfo;
import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiBanner;
import com.inmobi.ads.listeners.BannerAdEventListener;
import com.smartadserver.android.library.mediation.SASMediationAdapter;
import com.smartadserver.android.library.mediation.SASMediationBannerAdapter;
import com.smartadserver.android.library.mediation.SASMediationBannerAdapterListener;
import com.smartadserver.android.library.util.SASUtil;


import java.util.Map;

/**
 * Mediation adapter class for InMobi banner format
 */
public class SASInMobiBannerAdapter extends SASInMobiAdapterBase implements SASMediationBannerAdapter {


    private static final String TAG = SASInMobiBannerAdapter.class.getSimpleName();

    @Nullable
    InMobiBanner bannerAdView;

    @Override
    public void requestBannerAd(@NonNull Context context,
                                @NonNull String serverParameterString,
                                @NonNull Map<String, Object> clientParameters,
                                @NonNull final SASMediationBannerAdapterListener bannerAdapterListener) {

        configureAdRequest(context, serverParameterString, clientParameters);

        long placementID = getPlacementId(serverParameterString);

        // Create inMobi banner view with appropriate size.
        bannerAdView = new InMobiBanner(context, placementID);
        bannerAdView.setAnimationType(InMobiBanner.AnimationType.ANIMATION_OFF);

        int width = 0;
        try {
            width = Integer.parseInt((String)clientParameters.get(SASMediationAdapter.AD_VIEW_WIDTH_KEY));
        } catch (NumberFormatException ignored) {}

        int height = 0;
        try {
            height = Integer.parseInt((String)clientParameters.get(SASMediationAdapter.AD_VIEW_HEIGHT_KEY));
        } catch (NumberFormatException ignored) {}

        ViewGroup.LayoutParams lParams = new ViewGroup.LayoutParams(width, height);
        bannerAdView.setLayoutParams(lParams);

        // debug color
        if (SASUtil.debugModeEnabled) {
            bannerAdView.setBackgroundColor(Color.CYAN);
        }

        // create inmobi banner listener
        BannerAdEventListener bannerAdEventListener = new BannerAdEventListener() {
            @Override
            public void onAdLoadSucceeded(@NonNull InMobiBanner inMobiBanner, @NonNull AdMetaInfo adMetaInfo) {
                super.onAdLoadSucceeded(inMobiBanner, adMetaInfo);
                Log.d(TAG, "InMobi onAdLoadSucceeded for banner");
                bannerAdapterListener.onBannerLoaded(inMobiBanner);
            }

            @Override
            public void onAdLoadFailed(@NonNull InMobiBanner inMobiBanner, @NonNull InMobiAdRequestStatus inMobiAdRequestStatus) {
                super.onAdLoadFailed(inMobiBanner, inMobiAdRequestStatus);
                Log.d(TAG, "InMobi  onAdLoadFailed for banner");
                boolean isNoAd = inMobiAdRequestStatus.getStatusCode() == InMobiAdRequestStatus.StatusCode.NO_FILL;
                bannerAdapterListener.adRequestFailed(inMobiAdRequestStatus.getMessage() + "(" + inMobiAdRequestStatus.getStatusCode() + ")", isNoAd);
            }

            @Override
            public void onAdFetchSuccessful(@NonNull InMobiBanner inMobiBanner, @NonNull AdMetaInfo adMetaInfo) {
                super.onAdFetchSuccessful(inMobiBanner, adMetaInfo);
                Log.d(TAG, "InMobi onAdFetchSuccessful for banner");
            }

            @Override
            public void onAdFetchFailed(@NonNull InMobiBanner inMobiBanner, @NonNull InMobiAdRequestStatus inMobiAdRequestStatus) {
                super.onAdFetchFailed(inMobiBanner, inMobiAdRequestStatus);
                Log.d(TAG, "InMobi onAdFetchFailed for banner");
            }

            @Override
            public void onAdDisplayed(@NonNull InMobiBanner inMobiBanner) {
                super.onAdDisplayed(inMobiBanner);
                Log.d(TAG, "InMobi onAdDisplayed for banner");
                bannerAdapterListener.onAdFullScreen();
            }

            @Override
            public void onAdDismissed(@NonNull InMobiBanner inMobiBanner) {
                super.onAdDismissed(inMobiBanner);
                Log.d(TAG, "InMobi onAdDismissed for banner");
                bannerAdapterListener.onAdClosed();
            }

            @Override
            public void onUserLeftApplication(@NonNull InMobiBanner inMobiBanner) {
                super.onUserLeftApplication(inMobiBanner);
                Log.d(TAG, "InMobi onUserLeftApplication for banner");
            }

            @Override
            public void onRewardsUnlocked(@NonNull InMobiBanner inMobiBanner, Map<Object, Object> map) {
                super.onRewardsUnlocked(inMobiBanner, map);
                Log.d(TAG, "InMobi onRewardsUnlocked for banner");
            }

            @Override
            public void onAdClicked(@NonNull InMobiBanner inMobiBanner, Map<Object, Object> map) {
                super.onAdClicked(inMobiBanner, map);
                Log.d(TAG, "InMobi onAdClicked for banner");
                bannerAdapterListener.onAdClicked();
            }

            @Override
            public void onRequestPayloadCreated(byte[] bytes) {
                super.onRequestPayloadCreated(bytes);
                Log.d(TAG, "InMobi onRequestPayloadCreated for banner");
            }

            @Override
            public void onRequestPayloadCreationFailed(@NonNull InMobiAdRequestStatus inMobiAdRequestStatus) {
                super.onRequestPayloadCreationFailed(inMobiAdRequestStatus);
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
        if (bannerAdView != null) {
            bannerAdView.destroy();
        }
    }

}
