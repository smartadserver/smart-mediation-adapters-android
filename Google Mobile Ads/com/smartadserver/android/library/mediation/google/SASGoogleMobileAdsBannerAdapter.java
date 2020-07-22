package com.smartadserver.android.library.mediation.google;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.smartadserver.android.library.mediation.SASMediationBannerAdapter;
import com.smartadserver.android.library.mediation.SASMediationBannerAdapterListener;


import java.util.Map;

/**
 * Mediation adapter class for Google mobile ads banner format
 */
public class SASGoogleMobileAdsBannerAdapter extends SASGoogleMobileAdsAdapterBase implements SASMediationBannerAdapter {

    // tag for logging purposes
    private static final String TAG = SASGoogleMobileAdsBannerAdapter.class.getSimpleName();

    // Google mobile ads banner view instance
    View adView;


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

        String adUnitID = serverParametersString.split("\\|")[1];

        GoogleMobileAds gma = initGoogleMobileAds(context, serverParametersString);
        AdSize adSize = getAdSize(serverParametersString);

        if (GoogleMobileAds.ADMOB == gma) {
            // create google ad request
            AdRequest adRequest = configureAdRequest(context, serverParametersString, clientParameters);

            // Create Google AdView and configure it.
            AdView adMobView = new AdView(context);
            adMobView.setAdUnitId(adUnitID);
            adMobView.setAdSize(adSize);

            AdListener adListener =  createAdListener(bannerAdapterListener, adMobView);

            // set listener on banner
            adMobView.setAdListener(adListener);

            // perform ad request
            adMobView.loadAd(adRequest);

            adView = adMobView;

        } else if (GoogleMobileAds.AD_MANAGER == gma) {
            // create google publisher ad request
            PublisherAdRequest publisherAdRequest = new PublisherAdRequest.Builder().build();

            PublisherAdView adManagerView = new PublisherAdView(context);
            adManagerView.setAdUnitId(adUnitID);
            adManagerView.setAdSizes(adSize);

            AdListener adListener =  createAdListener(bannerAdapterListener, adManagerView);

            // set listener on banner
            adManagerView.setAdListener(adListener);

            // perform ad request
            adManagerView.loadAd(publisherAdRequest);

            adView = adManagerView;
        }
    }

    private AdListener createAdListener(SASMediationBannerAdapterListener bannerAdapterListener, View adView) {
        // create Google banner listener that will intercept ad mob banner events and call appropriate SASMediationBannerAdapterListener counterpart methods
        return new AdListener() {

            public void onAdClosed() {
                Log.d(TAG, "Google mobile ads onAdClosed for banner");
                bannerAdapterListener.onAdClosed();
            }

            public void onAdFailedToLoad(int errorCode) {
                Log.d(TAG, "Google mobile ads onAdFailedToLoad for banner (error code:" + errorCode + ")");
                boolean isNoAd = errorCode == AdRequest.ERROR_CODE_NO_FILL;
                bannerAdapterListener.adRequestFailed("Google mobile ads ad loading error code " + errorCode, isNoAd);
            }

            public void onAdLeftApplication() {
                Log.d(TAG, "Google mobile ads onAdLeftApplication for banner");
                bannerAdapterListener.onAdClicked();
                bannerAdapterListener.onAdLeftApplication();
            }

            public void onAdOpened() {
                Log.d(TAG, "Google mobile ads onAdOpened for banner");
            }

            public void onAdLoaded() {
                Log.d(TAG, "Google mobile ads onAdLoaded for banner");
                bannerAdapterListener.onBannerLoaded(adView);
            }
        };

    }


    /**
     * Utility method to get Banner Size from serverParametersString
     */
    protected AdSize getAdSize(String serverParametersString) {
        String[] parameters = serverParametersString.split("\\|");
        int bannerSizeIndex = 0;
        if (parameters.length > 2) {
            // Extracting banner size
            bannerSizeIndex = Integer.parseInt(parameters[2]);
        }
        switch (bannerSizeIndex) {
            case 1:
                return AdSize.MEDIUM_RECTANGLE;
            case 2:
                return AdSize.LEADERBOARD;
            case 3:
                return AdSize.LARGE_BANNER;
        }
        return AdSize.BANNER;
    }




    @Override
    public void onDestroy() {
        Log.d(TAG, "Google mobile ads onDestroy for banner");
        if (adView != null) {
            if (adView instanceof AdView) {
                ((AdView) adView).destroy();
            } else if (adView instanceof  PublisherAdView) {
                ((PublisherAdView)adView).destroy();
            }
        }
    }
}
