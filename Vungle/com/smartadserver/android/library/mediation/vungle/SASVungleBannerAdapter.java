package com.smartadserver.android.library.mediation.vungle;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.smartadserver.android.library.mediation.SASMediationBannerAdapter;
import com.smartadserver.android.library.mediation.SASMediationBannerAdapterListener;
import com.vungle.warren.AdConfig;
import com.vungle.warren.Banners;
import com.vungle.warren.VungleBanner;

import java.util.Map;

/**
 * Mediation adapter class for Vungle Banner format
 */
public class SASVungleBannerAdapter extends SASVungleAdapterBase implements SASMediationBannerAdapter {

    static private final String TAG = SASVungleBannerAdapter.class.getSimpleName();

    @Nullable
    private VungleBanner vungleBanner = null;

    @NonNull
    private AdConfig.AdSize bannerAdSize = AdConfig.AdSize.BANNER;

    /**
     * Requests and process a Vungle banner ad.
     *
     * @param context                the {@link Context} needed by the mediation SDK to make the ad request
     * @param serverParametersString a String containing all needed parameters (as returned by Smart ad delivery) to make the mediation ad call
     * @param clientParameters       additional client-side parameters (user specific, like location)
     * @param bannerAdapterListener  the {@link SASMediationBannerAdapterListener} provided to this {@link com.smartadserver.android.library.mediation.SASMediationAdapter} to notify Smart SDK of events occurring
     */
    @Override
    public void requestBannerAd(@NonNull Context context,
                                @NonNull String serverParametersString,
                                @NonNull Map<String, String> clientParameters,
                                @NonNull final SASMediationBannerAdapterListener bannerAdapterListener) {
        Log.d(TAG, "SASVungleBannerAdapter requestAd");

        configureAdapter(context, serverParametersString, clientParameters, bannerAdapterListener);

        // init banner ad size
        switch (bannerSizeIndex) {
            case 1:
                bannerAdSize = AdConfig.AdSize.BANNER_SHORT;
                break;
            case 2:
                bannerAdSize = AdConfig.AdSize.BANNER_LEADERBOARD;
                break;
            default:
                bannerAdSize = AdConfig.AdSize.BANNER;
        }

    }

    @Override
    public void onAdLoad(String id) {
        super.onAdLoad(id);
        if (Banners.canPlayAd(placementID, bannerAdSize)) {
            vungleBanner = Banners.getBanner(placementID, bannerAdSize, this);
            if (vungleBanner != null) {
                ((SASMediationBannerAdapterListener) mediationAdapterListener).onBannerLoaded(vungleBanner);
            }
        }
    }

    /**
     * Overriden for banner specific needs
     */
    @Override
    public void onSuccess() {
        super.onSuccess();
        Banners.loadBanner(placementID, bannerAdSize, this);
    }

    @Override
    public void onDestroy() {
        if (vungleBanner != null) {
            vungleBanner.destroyAd();
        }
    }
}
