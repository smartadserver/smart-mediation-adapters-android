package com.smartadserver.android.library.mediation.google;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.smartadserver.android.library.mediation.SASMediationBannerAdapter;
import com.smartadserver.android.library.mediation.SASMediationBannerAdapterListener;
import com.smartadserver.android.library.util.SASUtil;

import java.util.Map;

/**
 * Mediation adapter class for Google mobile ads banner format
 */
public class SASGoogleMobileAdsBannerAdapter extends SASGoogleMobileAdsAdapterBase implements SASMediationBannerAdapter {

    // tag for logging purposes
    private static final String TAG = "SASGoogleMobileAdsBannerAdapter";

    // Google mobile ads banner view instance
    AdView googleBanner;

    /**
     * Requests a mediated banner ad asynchronously
     *
     * @param context                the {@link android.content.Context} needed by the mediation SDK to make the ad request
     * @param serverParametersString a String containing all needed parameters (as returned by Smart ad delivery)
     *                               to make the mediation ad call
     * @param clientParameters       additional client-side parameters (user specific, like location)
     * @param bannerAdapterListener  the {@link SASMediationBannerAdapterListener} provided to
     *                               this {@link com.smartadserver.android.library.mediation.SASMediationAdapter} to notify Smart SDK of events occurring
     */
    @Override
    public void requestBannerAd(@NonNull Context context, @NonNull String serverParametersString, @NonNull Map<String, String> clientParameters,
                                @NonNull final SASMediationBannerAdapterListener bannerAdapterListener) {

        // create google ad request
        AdRequest adRequest = configureAdRequest(context, serverParametersString, clientParameters);

        // Create Google AdView and configure it.
        googleBanner = new AdView(context);
        googleBanner.setAdUnitId(serverParametersString.split("\\|")[1]);
        googleBanner.setAdSize(getAppropriateAdSizeFromVisualSize(context, clientParameters));

        // create Google banner listener that will intercept ad mob banner events and call appropriate SASMediationBannerAdapterListener counterpart methods
        AdListener bannerAdListener = new AdListener() {

            public void onAdClosed() {
                SASUtil.logDebug(TAG, "Google mobile ads onAdClosed for banner");
                bannerAdapterListener.onAdClosed();
            }

            public void onAdFailedToLoad(int errorCode) {
                SASUtil.logDebug(TAG, "Google mobile ads onAdFailedToLoad for banner (error code:" + errorCode + ")");
                boolean isNoAd = errorCode == AdRequest.ERROR_CODE_NO_FILL;
                bannerAdapterListener.adRequestFailed("Google mobile ads ad loading error code " + errorCode, isNoAd);
            }

            public void onAdLeftApplication() {
                SASUtil.logDebug(TAG, "Google mobile ads onAdLeftApplication for banner");
                bannerAdapterListener.onAdClicked();
                bannerAdapterListener.onAdLeftApplication();
            }

            public void onAdOpened() {
                SASUtil.logDebug(TAG, "Google mobile ads onAdOpened for banner");
            }

            public void onAdLoaded() {
                SASUtil.logDebug(TAG, "Google mobile ads onAdLoaded for banner");
                bannerAdapterListener.onBannerLoaded(googleBanner);
            }
        };

        // set listener on banner
        googleBanner.setAdListener(bannerAdListener);

        // perform ad request
        googleBanner.loadAd(adRequest);
    }

    @Override
    public void onDestroy() {
        SASUtil.logDebug(TAG, "Google mobile ads onDestroy for banner");
        if (googleBanner != null) {
            googleBanner.destroy();
        }
    }
}
