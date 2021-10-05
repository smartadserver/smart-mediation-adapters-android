package com.smartadserver.android.library.mediation.google

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.smartadserver.android.library.mediation.SASMediationAdapter

/**
 * Mediation adapter base class that will handle initialization and GDPR for all Google Mobile ads adapters
 */
open class SASGoogleMobileAdsAdapterBase {
    enum class GoogleMobileAds {
        NOT_INITIALIZED, ADMOB, AD_MANAGER
    }

    /**
     * Init method for Google Mobile Ads to decide from which canal (Google AdMob or Ad Manager) ads should be requested
     */
    protected fun initGoogleMobileAds(context: Context, serverParametersString: String): GoogleMobileAds {
        // reason behind the '|' separator is because Google mobile ads placement already use '/'
        val appID = getAppID(serverParametersString)
        if (GMA_AD_MANAGER_KEY != appID) { // check if the template corresponds to Google AdMob or Ad Manager
            if (GoogleMobileAds.NOT_INITIALIZED == GoogleMobileAdsInitStatus) {
                // appID = "ca-app-pub-3940256099942544~3347511713"; // USE FOR TESTING ONLY (AdMob sample ID)
                MobileAds.initialize(context) { initializationStatus ->
                    Log.d(TAG, "Google mobile ads onInitializationComplete : " +
                            initializationStatus.toString())
                }
            }
            GoogleMobileAdsInitStatus = GoogleMobileAds.ADMOB
        } else {
            GoogleMobileAdsInitStatus = GoogleMobileAds.AD_MANAGER
        }
        return GoogleMobileAdsInitStatus
    }

    /**
     * Utility method to get AppID from serverParametersString
     */
    protected fun getAppID(serverParametersString: String) = serverParametersString.split("\\|".toRegex())[0]

    /**
     * Utility method to get AppUnitID from serverParametersString
     */
    protected fun getAdUnitID(serverParametersString: String) =
            serverParametersString.split("\\|".toRegex()).getOrElse(1){""}


    companion object {
        private val TAG = SASGoogleMobileAdsAdapterBase::class.java.simpleName
        private const val GMA_AD_MANAGER_KEY = "admanager"

        // static flag for Google mobile ads SDK initialization
        private var GoogleMobileAdsInitStatus = GoogleMobileAds.NOT_INITIALIZED
    }
}