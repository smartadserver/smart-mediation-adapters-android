package com.smartadserver.android.library.mediation.adincube;

import android.content.Context;
import android.support.annotation.NonNull;

import com.adincube.sdk.AdinCube;
import com.adincube.sdk.AdinCubeBannerEventListener;
import com.adincube.sdk.BannerView;
import com.smartadserver.android.library.mediation.SASMediationBannerAdapter;
import com.smartadserver.android.library.mediation.SASMediationBannerAdapterListener;
import com.smartadserver.android.library.util.SASUtil;

import java.util.Map;

/**
 * Mediation adapter class for AdinCube banner format
 */
public class SASAdinCubeBannerAdapter extends SASAdinCubeAdapterBase implements SASMediationBannerAdapter {

    // internal TAG string for console output
    private static final String TAG = SASAdinCubeBannerAdapter.class.getSimpleName();

    /**
     * Loads an AdInCube banner ad
     *
     * @param context                the {@link android.content.Context} needed by the mediation SDK to make the bannr ad request
     * @param serverParametersString a String containing all needed parameters (as returned by Smart ad delivery)
     *                               to make the mediation ad call
     * @param clientParameters       additional client-side parameters (user specific, like location)
     * @param bannerAdapterListener  the {@link SASMediationBannerAdapterListener} provided to
     *                               this {@link com.smartadserver.android.library.mediation.SASMediationAdapter} to notify Smart SDK of events occurring
     */
    @Override
    public void requestBannerAd(@NonNull Context context, @NonNull String serverParametersString, @NonNull Map<String, String> clientParameters,
                                @NonNull final SASMediationBannerAdapterListener bannerAdapterListener) {
        SASUtil.logDebug(TAG, "SASAdinCubeBannerAdapter requestAd");

        configureAdRequest(context, serverParametersString, clientParameters);

        // create AdInCube banner listener to catch events
        AdinCubeBannerEventListener bannerEventListener = new AdinCubeBannerEventListener() {
            @Override
            public void onAdLoaded(BannerView bannerView) {
                SASUtil.logDebug(TAG, "AdinCube banner onAdLoaded");
                bannerAdapterListener.onBannerLoaded(bannerView);
            }

            @Override
            public void onLoadError(BannerView bannerView, String s) {
                SASUtil.logDebug(TAG, "AdinCube onLoadError for banner : " + s);
                bannerAdapterListener.adRequestFailed(s, true);
            }

            @Override
            public void onAdShown(BannerView bannerView) {
                SASUtil.logDebug(TAG, "AdinCube onAdShown for banner");
            }

            @Override
            public void onError(BannerView bannerView, String s) {
                SASUtil.logDebug(TAG, "AdinCube onError (while displaying) for banner");
            }

            @Override
            public void onAdClicked(BannerView bannerView) {
                SASUtil.logDebug(TAG, "AdinCube onAdClicked for banner");
                bannerAdapterListener.onAdClicked();
            }
        };

        // create AdInCubeBanner instance, set the listener and load ad
        BannerView bannerView = AdinCube.Banner.createView(context, AdinCube.Banner.Size.BANNER_AUTO);
        AdinCube.Banner.setEventListener(bannerView, bannerEventListener);
        bannerView.setAutoDestroyOnDetach(true);
        bannerView.load();

    }

    @Override
    public void onDestroy() {
        // banner will auto release itself
    }
}
