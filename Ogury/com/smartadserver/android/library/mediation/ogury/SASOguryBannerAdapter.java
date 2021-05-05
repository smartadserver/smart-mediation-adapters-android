package com.smartadserver.android.library.mediation.ogury;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ogury.ed.OguryBannerAdListener;
import com.ogury.ed.OguryBannerAdSize;
import com.ogury.ed.OguryBannerAdView;
import com.smartadserver.android.library.mediation.SASMediationBannerAdapter;
import com.smartadserver.android.library.mediation.SASMediationBannerAdapterListener;

import java.util.Map;

/**
 *  Mediation adapter class for Ogury banner ad format
 */
public class SASOguryBannerAdapter extends SASOguryAdapterBase implements SASMediationBannerAdapter, OguryBannerAdListener {

    static private final String TAG = SASOguryBannerAdapter.class.getSimpleName();

    // Ogury banner view
    @Nullable
    OguryBannerAdView bannerAdView;

    @Override
    public void requestBannerAd(@NonNull Context context,
                                @NonNull String serverParametersString,
                                @NonNull Map<String, Object> clientParameters,
                                @NonNull SASMediationBannerAdapterListener bannerAdapterListener) {
        Log.d(TAG, "SASOguryBannerAdapter adRequest");

        // common configuration
        configureAdRequest(context, serverParametersString, bannerAdapterListener);

        bannerAdView = new OguryBannerAdView(context);
        bannerAdView.setListener(this);
        bannerAdView.setAdUnit(getAdUnitID(serverParametersString));
        bannerAdView.setAdSize(getBannerAdSize(serverParametersString));
        bannerAdView.loadAd();
    }

    protected OguryBannerAdSize getBannerAdSize(@NonNull String serverParametersString) {

        String[] parameters = serverParametersString.split("\\|");
        int bannerSizeIndex = 0;
        if (parameters.length > 2) {
            // Extracting banner size
            bannerSizeIndex = Integer.parseInt(parameters[2]);
        }
        switch (bannerSizeIndex) {
            case 1:
                return OguryBannerAdSize.MPU_300x250;
            default:
                return OguryBannerAdSize.SMALL_BANNER_320x50;
        }
    }

    @Override
    public void onAdLoaded() {
        Log.d(TAG, "Ogury banner listener onAdLoaded");
        if (mediationAdapterListener != null && bannerAdView != null) {
            ((SASMediationBannerAdapterListener) mediationAdapterListener).onBannerLoaded(bannerAdView);
        }
    }

    @Override
    public void onDestroy() {
        if (bannerAdView != null) {
            bannerAdView.destroy();
        }
        bannerAdView = null;
    }
}
