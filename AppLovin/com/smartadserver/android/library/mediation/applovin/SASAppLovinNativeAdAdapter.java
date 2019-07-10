package com.smartadserver.android.library.mediation.applovin;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.applovin.nativeAds.AppLovinNativeAd;
import com.applovin.nativeAds.AppLovinNativeAdLoadListener;
import com.applovin.sdk.AppLovinErrorCodes;
import com.smartadserver.android.library.mediation.SASMediationNativeAdAdapter;
import com.smartadserver.android.library.mediation.SASMediationNativeAdAdapterListener;
import com.smartadserver.android.library.mediation.SASMediationNativeAdContent;
import com.smartadserver.android.library.model.SASNativeVideoAdElement;


import java.util.List;
import java.util.Map;

/**
 * Mediation adapter class for AppLovin native ad format
 */
public class SASAppLovinNativeAdAdapter extends SASAppLovinAdapterBase implements SASMediationNativeAdAdapter {

    // tag for logging purposes
    private static final String TAG = SASAppLovinNativeAdAdapter.class.getSimpleName();

    /**
     * Implementation of {@link SASMediationNativeAdContent} AppLovin to Smart native ad wrapper
     */
    private class ApplovinNativeAdContent implements SASMediationNativeAdContent {

        AppLovinNativeAd appLovinNativeAd;
        View.OnClickListener onClickListener;
        View[] registerClickableViews;
        SASNativeVideoAdElement nativeVideoAdElement;

        // overriden when instantiated
        protected void onAdClicked() {
        }

        public ApplovinNativeAdContent(AppLovinNativeAd nativeAd) {
            this.appLovinNativeAd = nativeAd;

            onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onAdClicked();
                    if (registerClickableViews != null && registerClickableViews[0] != null) {
                        appLovinNativeAd.launchClickTarget(registerClickableViews[0].getContext());
                    }
                }
            };

            // init video element if video url present
            String videoUrl = appLovinNativeAd.getVideoUrl();
            if (videoUrl != null) {
                nativeVideoAdElement = new SASNativeVideoAdElement();
                nativeVideoAdElement.setVideoUrl(videoUrl);
                nativeVideoAdElement.setAutoplay(true);
                nativeVideoAdElement.setBackgroundColor(Color.BLACK);
                nativeVideoAdElement.setVideoVerticalPosition(SASNativeVideoAdElement.VIDEO_POSITION_CENTER);
                String videoStartPixel = appLovinNativeAd.getVideoStartTrackingUrl();
                if (videoStartPixel != null && videoStartPixel.length() > 0) {
                    nativeVideoAdElement.setEventTrackingURLs(SASNativeVideoAdElement.TRACKING_EVENT_NAME_START, new String[]{videoStartPixel});
                }

                String videoEndPixel = appLovinNativeAd.getVideoEndTrackingUrl(100, true);
                if (videoEndPixel != null && videoEndPixel.length() > 0) {
                    nativeVideoAdElement.setEventTrackingURLs(SASNativeVideoAdElement.TRACKING_EVENT_NAME_COMPLETE, new String[]{videoEndPixel});
                }
            }
        }

        @Override
        public String getTitle() {
            return appLovinNativeAd.getTitle();
        }

        @Override
        public String getSubTitle() {
            return appLovinNativeAd.getDescriptionText();
        }

        @Override
        public String getBody() {
            return "";
        }

        @Override
        public String getIconUrl() {
            return appLovinNativeAd.getIconUrl();
        }

        @Override
        public int getIconWidth() {
            return -1;
        }

        @Override
        public int getIconHeight() {
            return -1;
        }

        @Override
        public String getCoverImageUrl() {
            return appLovinNativeAd.getImageUrl();
        }

        @Override
        public int getCoverImageWidth() {
            return -1;
        }

        @Override
        public int getCoverImageHeight() {
            return -1;
        }

        @Override
        public float getRating() {
            return appLovinNativeAd.getStarRating();
        }

        @Override
        public String getCallToAction() {
            return appLovinNativeAd.getCtaText();
        }

        @Override
        public String getSponsoredMessage() {
            return "";
        }

        @Override
        public SASNativeVideoAdElement getMediaElement() {
            return nativeVideoAdElement;
        }

        @Override
        public View getMediaView(Context context) {
            return null;
        }

        @Override
        public void unregisterView(View v) {
            if (registerClickableViews != null) {
                // clean all installed listeners on clickable views
                for (View clickableView : registerClickableViews) {
                    clickableView.setOnClickListener(null);
                    // this is MANDATORY as the view will continue to intercept clicks although
                    // its clickListener is null
                    clickableView.setClickable(false);
                }
            }
        }

        @Override
        public void registerView(View v, View[] clickableViews) {
            if (clickableViews != null) {
                registerClickableViews = clickableViews;
                for (View clickableView : clickableViews) {
                    clickableView.setOnClickListener(onClickListener);
                }
            }

            // notifies applovin of impression
            String appLovinImpressionPixel = appLovinNativeAd.getImpressionTrackingUrl();
            sdk.getPostbackService().dispatchPostbackAsync(appLovinImpressionPixel, null);

        }

        @Override
        public String getAdChoicesUrl() {
            return "http://applovin.com/optoutmobile";
        }
    }


    /**
     * Requests a mediated native ad asynchronously
     *
     * @param context                 the {@link android.content.Context} needed by the mediation SDK to make the ad request
     * @param serverParametersString  a String containing all needed parameters (as returned by Smart ad delivery)
     *                                to make the mediation call
     * @param clientParameters        additional client-side parameters (user specific, like location)
     * @param nativeAdAdapterListener the {@link SASMediationNativeAdAdapterListener} provided to this {@link com.smartadserver.android.library.mediation.SASMediationAdapter}
     */
    @Override
    public void requestNativeAd(@NonNull Context context, @NonNull String serverParametersString, @NonNull Map<String, String> clientParameters,
                                @NonNull final SASMediationNativeAdAdapterListener nativeAdAdapterListener) {

        configureAdRequest(context, serverParametersString, clientParameters);

        sdk.getNativeAdService().loadNativeAds(1, new AppLovinNativeAdLoadListener() {
            @Override
            public void onNativeAdsLoaded(final List list) {
                Log.d(TAG, "Applovin onNativeAdsLoaded");

                nativeAdAdapterListener.onNativeAdLoaded(new ApplovinNativeAdContent((AppLovinNativeAd) list.get(0)) {
                    @Override
                    protected void onAdClicked() {
                        super.onAdClicked();
                        nativeAdAdapterListener.onAdClicked();
                    }
                });
            }

            @Override
            public void onNativeAdsFailedToLoad(final int errorCode) {
                Log.d(TAG, "Applovin onNativeAdsFailedToLoad (error:" + errorCode + ")");
                boolean isNoAd = errorCode == AppLovinErrorCodes.NO_FILL;
                nativeAdAdapterListener.adRequestFailed("errorCode:" + errorCode, isNoAd);
            }
        });
    }

    @Override
    public void onDestroy() {
        // nothing to do
    }
}
