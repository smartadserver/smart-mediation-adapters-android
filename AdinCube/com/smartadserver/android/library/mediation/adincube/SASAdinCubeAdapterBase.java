package com.smartadserver.android.library.mediation.adincube;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adincube.sdk.AdinCube;

/**
 * Mediation adapter base class that will handle initialization and GDPR for all AdinCube adapters
 */
public class SASAdinCubeAdapterBase {

    @Nullable
    private static String iabString = null;

    @Nullable
    private static String[] nonIabVendorAcceptedArray = null;

    // parameter key for required AdinCube application ID
    protected static final String APPLICATION_ID_KEY = "applicationID";

    // static flag for AdInCube SDK initialization
    protected static boolean initAdinCubeDone = false;

    @Nullable
    public static String getIabString() {
        return iabString;
    }

    @Nullable
    public static String[] getNonIabVendorAcceptedArray() {
        return nonIabVendorAcceptedArray;
    }

    public static void setIabString(@Nullable String iabString) {
        SASAdinCubeAdapterBase.iabString = iabString;
    }

    public static void setNonIabVendorAcceptedArray(@Nullable String[] nonIabVendorAcceptedArray) {
        SASAdinCubeAdapterBase.nonIabVendorAcceptedArray = nonIabVendorAcceptedArray;
    }

    /**
     * Common configuration code for all formats
     */
    protected void configureAdRequest(@NonNull Context context, @NonNull String serverParametersString) {

        // one time init
        if (!initAdinCubeDone) {
            initAdinCubeDone = true;
            // Now Adincube template could return appID or appID|bannerSizeID
            String appID = serverParametersString.split("\\|")[0];
            // init AdInCube
            AdinCube.setAppKey(appID);
        }

        // The adapter will automatically handles consent forwarding to AdinCube if:
        // - you have provided 'iabString' & 'nonIabVendorAcceptedArray' to SASAdinCubeAdapterBase (formatted as described in the Ogury's documentation)
        // - your application is whitelisted by Ogury.
        //
        // You can find more information in Ogury's documentation:
        // https://intelligentmonetization.ogury.co/dashboard/#/docs/android/gradle?networks=26226be#third-party-consent
        if (getIabString() != null && getNonIabVendorAcceptedArray() != null) {
            AdinCube.UserConsent.External.setConsent(context, getIabString(), getNonIabVendorAcceptedArray());
        }

        // Note:
        // If you don't provide 'iabString' & 'nonIabVendorAcceptedArray' or if you aren't whitelisted by Ogury, you
        // will need to use 'Ogury Choice Manager' and you will need to implement it by yourself.
        //
        // You can find more information about 'Ogury Choice Manager' in Ogury's documentation:
        // https://intelligentmonetization.ogury.co/dashboard/#/docs/android/gradle?networks=26226be#ogury-choice-manager
    }
}
