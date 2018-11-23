package com.smartadserver.android.library.mediation.facebook;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.smartadserver.android.library.mediation.SASMediationAdapter;
import com.smartadserver.android.library.mediation.SASMediationBannerAdapter;
import com.smartadserver.android.library.mediation.SASMediationBannerAdapterListener;
import com.smartadserver.android.library.util.SASUtil;

import java.util.Map;

/**
 * Mediation adapter class for Facebook banner format
 */
public class SASFacebookBannerAdapter extends SASFacebookAdapterBase implements SASMediationBannerAdapter {

    // Tag for logging purposes
    static private final String TAG = SASFacebookBannerAdapter.class.getSimpleName();

    // Facebook banner reference for later clean-up
    private AdView bannerView;

    @Override
    public void requestBannerAd(@NonNull Context context, @NonNull String serverParametersString, @NonNull Map<String, String> clientParameters,
                                @NonNull final SASMediationBannerAdapterListener bannerAdapterListener) {
        configureAdRequest(context,serverParametersString,clientParameters);

        String placementID = serverParametersString;

        // retrieve ad view width and height from clientParameters
        int width = Integer.parseInt(clientParameters.get(SASMediationAdapter.AD_VIEW_WIDTH_KEY));
        int height = Integer.parseInt(clientParameters.get(SASMediationAdapter.AD_VIEW_HEIGHT_KEY));

        // get Android metrics
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);

        // compute ad view size in dp
        int adViewWidthDp = (int)(width / metrics.density);
        int adViewHeightDp = (int)(height / metrics.density);

        AdSize adSize = getAppropriateBannerSize(adViewWidthDp,adViewHeightDp);

        // instantiate Facebook banner view
        bannerView = new AdView(context,placementID, adSize);

        // instantiate Facebook banner listener
        AdListener bannerListener = new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                SASUtil.logDebug(TAG, "Facebook onError for banner");
                boolean isNoAd = adError.getErrorCode() == AdError.NO_FILL_ERROR_CODE;
                bannerAdapterListener.adRequestFailed(adError.getErrorMessage(), isNoAd);
            }

            @Override
            public void onAdLoaded(Ad ad) {
                SASUtil.logDebug(TAG, "Facebook onAdLoaded for banner");
                bannerAdapterListener.onBannerLoaded(bannerView);
            }

            @Override
            public void onAdClicked(Ad ad) {
                SASUtil.logDebug(TAG, "Facebook onAdClicked for banner");
                bannerAdapterListener.onAdClicked();
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                SASUtil.logDebug(TAG, "Facebook onLoggingImpression for banner");
            }
        };

        // set listener on banner
        bannerView.setAdListener(bannerListener);

        // perform ad request
        bannerView.loadAd();

    }


    private AdSize getAppropriateBannerSize(int width, int height) {

        AdSize bannerSize = AdSize.BANNER_HEIGHT_50;

        // refine size according to availbale estate in SASBannerView
        if (height >= AdSize.BANNER_HEIGHT_90.getHeight()) {
            bannerSize = AdSize.BANNER_HEIGHT_90;
        }

        return  bannerSize;

    }


    @Override
    public void onDestroy() {
        SASUtil.logDebug(TAG, "Facebook onDestroy for banner ");
        if (bannerView != null) {
            bannerView.destroy();
        }
    }
}
