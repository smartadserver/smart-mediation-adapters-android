package com.smartadserver.android.library.mediation.ogury;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ogury.cm.OguryChoiceManager;
import com.ogury.cm.OguryChoiceManagerExternal;
import com.ogury.cm.OguryCmConfig;
import com.smartadserver.android.coresdk.util.SCSConstants;
import com.smartadserver.android.library.mediation.SASMediationAdapterListener;

import io.presage.Presage;
import io.presage.interstitial.PresageInterstitialCallback;

public class SASOguryAdapterBase implements PresageInterstitialCallback {

    static private final String TAG = SASOguryAdapterBase.class.getSimpleName();

    @Nullable
    protected SASMediationAdapterListener mediationAdapterListener;

    /**
     * Common configuration code for all formats
     */
    protected void configureAdRequest(@NonNull Context context,
                                      @NonNull String serverParametersString,
                                      @NonNull SASMediationAdapterListener mediationAdapterListener) {

        this.mediationAdapterListener = mediationAdapterListener;

        String assetKey = getAssetKey(serverParametersString);

        // extract TCF V2 string...
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String tcfString = sharedPreferences.getString(SCSConstants.GDPR.TCF_V2_KEY, null);

        // ... and pass it to Ogury SDK, if any.
        if (tcfString != null) {
            OguryChoiceManager.initialize(context, assetKey, new OguryCmConfig());
            OguryChoiceManagerExternal.TcfV2.setConsent(context, assetKey, tcfString, new Integer[0]);
        }

        // Init the Ogury SDK ad each call, the API KEY can be different at each call
        Presage.getInstance().start(getAssetKey(serverParametersString), context);

    }

    /**
     * Utility method to get Ogury Asset Key from serverParametersString
     */
    @NonNull
    protected String getAssetKey(@NonNull String serverParametersString) {
        return serverParametersString.split("\\|")[0];
    }

    /**
     * Utility method to get Ogury AdUnit ID from serverParametersString
     */
    @NonNull
    protected String getAdUnitID(@NonNull String serverParametersString) {
        String[] parameters = serverParametersString.split("\\|");
        if (parameters.length > 1) {
            return parameters[1];
        }
        return "";
    }

    @Override
    public void onAdAvailable() {
        Log.d(TAG, "Ogury callback onAdAvailable");
    }

    @Override
    public void onAdNotAvailable() {
        Log.d(TAG, "Ogury callback onAdNotAvailable");
        if (mediationAdapterListener != null) {
            mediationAdapterListener.adRequestFailed("Ogury ad not available", true);
        }
    }

    @Override
    public void onAdLoaded() {
        Log.d(TAG, "Ogury callback onAdLoaded");
    }

    @Override
    public void onAdNotLoaded() {
        Log.d(TAG, "Ogury callback onAdNotLoaded");
        if (mediationAdapterListener != null) {
            mediationAdapterListener.adRequestFailed("Ogury ad not loaded", false);
        }
    }

    @Override
    public void onAdDisplayed() {
        Log.d(TAG, "Ogury callback onAdDisplayed");
    }

    @Override
    public void onAdClosed() {
        Log.d(TAG, "Ogury callback onAdClosed");
        if (mediationAdapterListener != null) {
            mediationAdapterListener.onAdClosed();
        }
    }

    @Override
    public void onAdError(int i) {
        Log.d(TAG, "Ogury callback onAdError: " + i);

        boolean isNoFill = true;
        String errorMessage = "Ogury SASOguryAdapterBase failed with error code " + i;

        /**
         * From Ogury documentation
         *
         * code 0: load failed
         * code 1: phone not connected to internet
         * code 2: ad disabled fot this placement/application
         * code 3: internal error occurred
         * code 5: start method was not called before the ad call
         * code 6: an error occurred during the initialization of the SDK
         **/

        switch (i) {
            case 0:
                errorMessage += " : load failed";
                break;

            case 1:
                errorMessage += " : phone not connected to internet";
                isNoFill = false;
                break;

            case 2:
                errorMessage += " : ad disabled fot this placement/application";
                break;

            case 3:
                errorMessage += " : internal error occurred";
                break;

            case 5:
                errorMessage += " : start method was not called before the ad call";
                isNoFill = false;
                break;

            case 6:
                errorMessage += " : an error occurred during the initialization of the SDK";
                isNoFill = false;
                break;
        }

        if (mediationAdapterListener != null) {
            mediationAdapterListener.adRequestFailed(errorMessage, isNoFill);
        }
    }
}
