package com.smartadserver.android.library.mediation.ogury;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ogury.ed.OguryInterstitialAd;
import com.ogury.ed.OguryInterstitialAdListener;
import com.smartadserver.android.library.mediation.SASMediationInterstitialAdapter;
import com.smartadserver.android.library.mediation.SASMediationInterstitialAdapterListener;

import java.util.Map;

/**
 * Mediation adapter class for Ogury interstitial ad format
 */
public class SASOguryInterstitialAdapter extends SASOguryAdapterBase implements SASMediationInterstitialAdapter, OguryInterstitialAdListener {

    static private final String TAG = SASOguryInterstitialAdapter.class.getSimpleName();

    // Ogury Interstitial manager instance
    @Nullable
    private OguryInterstitialAd oguryInterstitial;

    /**
     * @param context                     the {@link android.content.Context} needed by the mediation SDK to make the ad request
     * @param serverParametersString      a String containing all needed parameters (as returned by Smart ad delivery)
     *                                    to make the mediation ad call
     * @param clientParameters            additional client-side parameters (user specific, like location)
     * @param interstitialAdapterListener the {@link SASMediationInterstitialAdapterListener} provided to
     *                                    this {@link com.smartadserver.android.library.mediation.SASMediationAdapter} to notify Smart SDK of events occurring
     */
    @Override
    public void requestInterstitialAd(@NonNull Context context,
                                      @NonNull String serverParametersString,
                                      @NonNull Map<String, Object> clientParameters,
                                      @NonNull final SASMediationInterstitialAdapterListener interstitialAdapterListener) {
        Log.d(TAG, "SASOguryInterstitialAdapter adRequest");

        // common configuration
        configureAdRequest(context, serverParametersString, interstitialAdapterListener);


        // Instantiate the Presage interstitial
        oguryInterstitial = new OguryInterstitialAd(context, getAdUnitID(serverParametersString));
        oguryInterstitial.setListener(this);
        oguryInterstitial.load();

    }

    @Override
    public void onAdLoaded() {
        Log.d(TAG, "Ogury interstitial onAdLoaded");
        if (mediationAdapterListener != null) {
            ((SASMediationInterstitialAdapterListener) mediationAdapterListener).onInterstitialLoaded();
        }
    }

    @Override
    public void onAdDisplayed() {
        super.onAdDisplayed();
        if (mediationAdapterListener != null) {
            ((SASMediationInterstitialAdapterListener) mediationAdapterListener).onInterstitialShown();
        }
    }

    @Override
    public void showInterstitial() throws Exception {
        if (oguryInterstitial != null && oguryInterstitial.isLoaded()) {
            oguryInterstitial.show();
        }
    }

    @Override
    public void onDestroy() {
        // workaround for Ogury not supporting nullification of callback
//        if (oguryInterstitial != null) {
//            oguryInterstitial.setListener(null);
//        }
        oguryInterstitial = null;
    }
}
