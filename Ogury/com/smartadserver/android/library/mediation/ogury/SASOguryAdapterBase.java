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
import com.ogury.core.OguryError;
import com.ogury.ed.OguryAdListener;
import com.ogury.sdk.Ogury;
import com.ogury.sdk.OguryConfiguration;
import com.smartadserver.android.coresdk.util.SCSConstants;
import com.smartadserver.android.library.mediation.SASMediationAdapterListener;

/**
 * Base class for Ogury formats adapters
 */
public abstract class SASOguryAdapterBase implements OguryAdListener {

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
        OguryConfiguration.Builder oguryConfigurationBuilder =
                new OguryConfiguration.Builder(context, getAssetKey(serverParametersString));
        Ogury.start(oguryConfigurationBuilder.build());

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
    public void onAdDisplayed() {
        Log.d(TAG, "Ogury listener onAdDisplayed");
    }

    @Override
    public void onAdClicked() {
        Log.d(TAG, "Ogury listener onAdClicked");
        if (mediationAdapterListener != null) {
            mediationAdapterListener.onAdClicked();
        }
    }

    @Override
    public void onAdClosed() {
        Log.d(TAG, "Ogury listener onAdClosed");
        if (mediationAdapterListener != null) {
            mediationAdapterListener.onAdClosed();
        }
    }

    @Override
    public void onAdError(OguryError error) {
        Log.d(TAG, "Ogury listener onAdError: " + error);

        /**
         * From Ogury documentation :
         *
         * NO_INTERNET_CONNECTION = 0;
         * LOAD_FAILED = 2000;
         * AD_DISABLED = 2001;
         * PROFIG_NOT_SYNCED = 2002;
         * AD_EXPIRED = 2003;
         * SDK_INIT_NOT_CALLED = 2004;
         * ANOTHER_AD_ALREADY_DISPLAYED = 2005;
         * SDK_INIT_FAILED = 2006;
         * ACTIVITY_IN_BACKGROUND = 2007;
         * AD_NOT_AVAILABLE = 2008;
         * AD_NOT_LOADED = 2009;
         * SHOW_FAILED = 2010;
         **/

        boolean isNoFill = error.getErrorCode() == 2001 || error.getErrorCode() == 2008;
        String errorMessage = "Ogury SASOguryAdapterBase failed with error " + error;

        if (mediationAdapterListener != null) {
            mediationAdapterListener.adRequestFailed(errorMessage, isNoFill);
        }
    }
}
