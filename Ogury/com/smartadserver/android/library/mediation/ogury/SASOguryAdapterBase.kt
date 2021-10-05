package com.smartadserver.android.library.mediation.ogury

import android.content.Context
import android.util.Log
import com.ogury.core.OguryError
import com.ogury.ed.OguryAdListener
import com.ogury.sdk.Ogury
import com.ogury.sdk.OguryConfiguration
import com.smartadserver.android.library.mediation.SASMediationAdapterListener

/**
 * Base class for Ogury formats adapters
 */
abstract class SASOguryAdapterBase : OguryAdListener {

    companion object {
        private val TAG = SASOguryAdapterBase::class.java.simpleName
    }

    protected var mediationAdapterListener: SASMediationAdapterListener? = null

    /**
     * Common configuration code for all formats
     */
    protected fun configureAdRequest(
        context: Context,
        serverParametersString: String,
        mediationAdapterListener: SASMediationAdapterListener
    ) {
        this.mediationAdapterListener = mediationAdapterListener

        // Init the Ogury SDK ad each call, the API KEY can be different at each call
        val builder = OguryConfiguration.Builder(context, getAssetKey(serverParametersString))
        Ogury.start(builder.build())
    }

    /**
     * Utility method to get Ogury Asset Key from serverParametersString
     */
    private fun getAssetKey(serverParametersString: String) = serverParametersString.split("|")[0]

    /**
     * Utility method to get Ogury AdUnit ID from serverParametersString
     */
    fun getAdUnitID(serverParametersString: String) = serverParametersString.split("|")
        .getOrElse(1) { "" }

    override fun onAdDisplayed() {
        Log.d(TAG, "Ogury listener onAdDisplayed")
    }

    override fun onAdClicked() {
        Log.d(TAG, "Ogury listener onAdClicked")
        mediationAdapterListener?.onAdClicked()
    }

    override fun onAdClosed() {
        Log.d(TAG, "Ogury listener onAdClosed")
        mediationAdapterListener?.onAdClosed()
    }

    override fun onAdError(error: OguryError?) {
        Log.d(TAG, "Ogury listener onAdError: $error")

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

        error?.let {
            val isNoFill = it.errorCode == 2001 || it.errorCode == 2008
            mediationAdapterListener?.adRequestFailed(
                "Ogury SASOguryAdapterBase failed with error: $error",
                isNoFill
            )
        } ?: run {
            mediationAdapterListener?.adRequestFailed(
                "Ogury SASOguryAdapterBase failed with unknown error",
                false
            )
        }
    }

}