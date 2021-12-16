package com.smartadserver.android.library.mediation.vungle;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.smartadserver.android.library.mediation.SASMediationAdapterListener;
import com.vungle.warren.InitCallback;
import com.vungle.warren.LoadAdCallback;
import com.vungle.warren.PlayAdCallback;
import com.vungle.warren.Vungle;
import com.vungle.warren.error.VungleException;

import java.util.Map;

import static com.smartadserver.android.library.mediation.SASMediationAdapter.GDPR_APPLIES_KEY;

/**
 * Mediation adapter base class for Vungle adapters
 */
public class SASVungleAdapterBase implements LoadAdCallback, PlayAdCallback, InitCallback {

    static private final String TAG = SASVungleAdapterBase.class.getSimpleName();

    // common parameters for most of the formats
    @NonNull
    protected String applicationID = "";

    @NonNull
    protected String placementID = "";

    @Nullable
    private String GDPRApplies = null;

    @Nullable
    private String consentStatus = null;

    @Nullable
    protected SASMediationAdapterListener mediationAdapterListener;

    protected boolean adLoaded = false;

    // only for banners
    protected int bannerSizeIndex = 0;

    /**
     * Perform some initializations common to all formats.
     *  @param context                   The {@link Context} needed by the mediation SDK to make the ad request.
     * @param serverParametersString    a String containing all needed parameters (as returned by Smart ad delivery)
     * @param clientParameters          additional client-side parameters (user specific, like location).
     * @param mediationAdapterListener  the {@link SASMediationAdapterListener} provided to this {@link com.smartadserver.android.library.mediation.SASMediationAdapter} to notify the Smart SDK of events
     */
    protected void configureAdapter(@NonNull Context context,
                                    @NonNull String serverParametersString,
                                    @NonNull Map<String, Object> clientParameters,
                                    @NonNull SASMediationAdapterListener mediationAdapterListener) {

        // reset ad loaded status, if need be
        adLoaded = false;

        // Retrieve placement info -- Here the serverParametersString is composed as "applicationID/placementID"
        String[] placementInfo = serverParametersString.split("/");

        // Check all placement info are correctly set
        if (placementInfo.length < 2) {
            mediationAdapterListener.adRequestFailed("The Vungle applicationID and/or placementID is not correctly set.", false);
        }

        this.mediationAdapterListener = mediationAdapterListener;

        // extract IDs
        applicationID = placementInfo[0];
        placementID = placementInfo[1];

        // Try to extract Banner size index from template
        try {
            if (placementInfo.length >= 3) {
                bannerSizeIndex = Integer.parseInt(placementInfo[2]);
            }
        } catch (NumberFormatException ignored) {}


        // GDPR related
        GDPRApplies = (String) clientParameters.get(GDPR_APPLIES_KEY);

        // Due to the fact that Vungle is not IAB compliant, it does not accept IAB Consent String, but only a
        // binary consent status.
        // Smart advises app developers to store the binary consent in the 'Smart_advertisingConsentStatus' key
        // in NSUserDefault, therefore this adapter will retrieve it from this key.
        // Adapt the code below if your app don't follow this convention.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        consentStatus = sharedPreferences.getString("Smart_advertisingConsentStatus", null);

        //Need to init Vungle at every requestAd call, as the placement id can be different
        Vungle.init(applicationID, context.getApplicationContext(), this);

    }

    /***** LoadAdCallback interface **********/

    @Override
    public void onAdLoad(@Nullable String id) {
        Log.d(TAG, "Vungle LoadAdCallback onAdLoad");
        this.adLoaded = true;
    }

    /***** PlayAdCallback interface **********/

    @Override
    public void creativeId(String creativeId) {
        Log.d(TAG, "Vungle PlayAdCallback creativeId: " + creativeId);
    }

    @Override
    public void onAdStart(@Nullable String s) {
        Log.d(TAG, "Vungle PlayAdCallback onAdStart");
    }

    @Override
    public void onAdEnd(@Nullable String s, boolean completed, boolean isClicked) {
        Log.d(TAG, "Vungle PlayAdCallback deprecated onAdEnd");
    }

    @Override
    public void onAdEnd(@Nullable String id) {
        Log.d(TAG, "Vungle PlayAdCallback onAdEnd id:" + id);
        if (mediationAdapterListener != null) {
            mediationAdapterListener.onAdClosed();
        }
    }

    @Override
    public void onAdClick(@Nullable String id) {
        Log.d(TAG, "Vungle PlayAdCallback onAdClick id:" + id);
        if (mediationAdapterListener != null) {
            mediationAdapterListener.onAdClicked();
        }
    }

    @Override
    public void onAdRewarded(@Nullable String id) {
        Log.d(TAG, "Vungle PlayAdCallback onAdRewarded called id:" + id);
    }

    @Override
    public void onAdLeftApplication(@Nullable String id) {
        Log.d(TAG, "Vungle PlayAdCallback onAdLeftApplication id:" + id);
        if (mediationAdapterListener != null) {
            mediationAdapterListener.onAdLeftApplication();
        }
    }

    @Override
    public void onError(@Nullable String s, @Nullable VungleException exception) {
        if (!adLoaded) {
            Log.d(TAG, "Vungle LoadAdCallback onError");
            // check if the error is due to a no ad.
            boolean isNoAd = (exception != null && exception.getExceptionCode() == VungleException.NO_SERVE);
            String message = "";
            if (exception != null && exception.getLocalizedMessage() != null) {
                message = exception.getLocalizedMessage();
            }
            if (mediationAdapterListener != null) {
                mediationAdapterListener.adRequestFailed(message, isNoAd);
            }
        } else {
            Log.d(TAG, "Vungle PlayAdCallback onError");
        }
    }

    @Override
    public void onAdViewed(String placementId) {
        Log.d(TAG, "Vungle onAdViewed");
    }

    /***** InitCallback interface **********/
    @Override
    public void onSuccess() {
        Log.d(TAG, "Vungle InitCallback onSuccess");

        // handle GDPR
        if (GDPRApplies != null) {
            // Smart determined GDPR applies or not
            if (!("false".equalsIgnoreCase(GDPRApplies))) {
                // get GDPR consent status
                if (consentStatus != null) {
                    if (consentStatus.equals("1")) {
                        Vungle.updateConsentStatus(Vungle.Consent.OPTED_IN, "REPLACE_WITH_YOUR_CONSENT_POLICY_VERSION");
                    } else {
                        Vungle.updateConsentStatus(Vungle.Consent.OPTED_OUT, "REPLACE_WITH_YOUR_CONSENT_POLICY_VERSION");
                    }
                }
            }
        }
    }

    @Override
    public void onError(@Nullable VungleException exception) {
        Log.d(TAG, "Vungle InitCallback onError");
        String message = "";
        if (exception != null && exception.getLocalizedMessage() != null) {
            message = exception.getLocalizedMessage();
        }
        if (mediationAdapterListener != null) {
            mediationAdapterListener.adRequestFailed(message, false);
        }
    }

    @Override
    public void onAutoCacheAdAvailable(@Nullable String placementId) {
        Log.d(TAG, "Vungle InitCallback onAutoCacheAdAvailable: placementId:" + placementId );
    }

}
