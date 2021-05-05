package com.smartadserver.android.library.mediation.ogury;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ogury.ed.OguryThumbnailAd;
import com.ogury.ed.OguryThumbnailAdListener;
import com.smartadserver.android.library.mediation.SASMediationBannerAdapter;
import com.smartadserver.android.library.mediation.SASMediationBannerAdapterListener;

import java.util.Map;

/**
 * Mediation adapter class for Ogury Thumbnail format
 */
public class SASOguryThumbnailAdapter extends SASOguryAdapterBase implements SASMediationBannerAdapter, OguryThumbnailAdListener {

    static private final String TAG = SASOguryThumbnailAdapter.class.getSimpleName();

    @Nullable
    OguryThumbnailAd thumbnailAd;
    @Nullable
    FrameLayout dummyBanner;

    @Override
    public void requestBannerAd(@NonNull Context context,
                                @NonNull String serverParametersString,
                                @NonNull Map<String, Object> clientParameters,
                                @NonNull SASMediationBannerAdapterListener bannerAdapterListener) {

        Log.d(TAG, "SASOguryThumbnailAdapter adRequest");

        // common configuration
        configureAdRequest(context, serverParametersString, bannerAdapterListener);

        thumbnailAd = new OguryThumbnailAd(context,getAdUnitID(serverParametersString));


        final int[] thumbnailSizeParameters = getThumbnailSizeParameters(serverParametersString);


        // dummy view that needs to be sent to Smart SDK as mediated banner
        dummyBanner = new FrameLayout(context) {
            @Override
            protected void onAttachedToWindow() {
                super.onAttachedToWindow();
                if (thumbnailAd != null && thumbnailAd.isLoaded()) {
                    thumbnailAd.show((Activity)context, thumbnailSizeParameters[2],thumbnailSizeParameters[3]);
                }
            }
        };

        // set callback on thumbnail ad
        thumbnailAd.setListener(this);

        // load thumbnail ad
        thumbnailAd.load(thumbnailSizeParameters[0],thumbnailSizeParameters[1]);
    }

    @NonNull
    protected int[] getThumbnailSizeParameters(@NonNull String serverParametersString) {

        int[] params = new int[4];

        String[] parameters = serverParametersString.split("\\|");
        if (parameters.length >= 6) {
            // Extracting thumbnail size parameters
            params[0] = Integer.parseInt(parameters[2]); // thumbnail max width
            params[1] = Integer.parseInt(parameters[3]); // thumbnail max height
            params[2] = Integer.parseInt(parameters[4]); // thumbnail top margin
            params[3] = Integer.parseInt(parameters[5]); // thumbnail left margin
        }

        return params;
    }

    @Override
    public void onAdLoaded() {
        Log.d(TAG, "Ogury thumbnail onAdLoaded");
        if (mediationAdapterListener != null && dummyBanner != null) {
            ((SASMediationBannerAdapterListener) mediationAdapterListener).onBannerLoaded(dummyBanner);
        }
    }

    @Override
    public void onDestroy() {
        if (thumbnailAd != null) {
            thumbnailAd = null;
        }
    }
}
