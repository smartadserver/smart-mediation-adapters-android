package com.smartadserver.android.library.mediation.ogury;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ogury.ed.OguryBannerAdSize;
import com.ogury.ed.OguryBannerAdView;
import com.ogury.ed.OguryBannerCallback;
import com.smartadserver.android.library.mediation.SASMediationBannerAdapter;
import com.smartadserver.android.library.mediation.SASMediationBannerAdapterListener;

import java.util.Map;

public class SASOguryBannerAdapter extends SASOguryAdapterBase implements SASMediationBannerAdapter, OguryBannerCallback {

    static private final String TAG = SASOguryBannerAdapter.class.getSimpleName();

    // Ogury banner view
    OguryBannerAdView bannerAdView;

    @Override
    public void requestBannerAd(@NonNull Context context,
                                @NonNull String serverParametersString,
                                @NonNull Map<String, String> clientParameters,
                                @NonNull SASMediationBannerAdapterListener bannerAdapterListener) {
        Log.d(TAG, "SASOguryBannerAdapter adRequest");

        // common configuration
        configureAdRequest(context, serverParametersString, bannerAdapterListener);

        bannerAdView = new OguryBannerAdView(context);
        bannerAdView.setCallback(this);
        bannerAdView.setAdUnit(getAdUnitID(serverParametersString));
        bannerAdView.setAdSize(getBannerAdSize(serverParametersString));
        bannerAdView.loadAd();
    }

    protected OguryBannerAdSize getBannerAdSize(String serverParametersString) {

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
        super.onAdLoaded();
        ((SASMediationBannerAdapterListener)mediationAdapterListener).onBannerLoaded(bannerAdView);
    }

    @Override
    public void onAdClicked() {
        Log.d(TAG, "oguryBannerCallback onAdClicked");
        mediationAdapterListener.onAdClicked();
    }

    @Override
    public void onDestroy() {
        if (bannerAdView != null) {
            bannerAdView.destroy();
        }
        bannerAdView = null;
    }


}
