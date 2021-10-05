package com.smartadserver.android.library.mediation.ogury

import android.content.Context
import android.util.Log
import com.ogury.ed.OguryInterstitialAd
import com.ogury.ed.OguryInterstitialAdListener
import com.smartadserver.android.library.mediation.SASMediationInterstitialAdapter
import com.smartadserver.android.library.mediation.SASMediationInterstitialAdapterListener

class SASOguryInterstitialAdapter : SASOguryAdapterBase(), SASMediationInterstitialAdapter,
    OguryInterstitialAdListener {

    companion object {
        private val TAG = SASOguryInterstitialAdapter::class.java.simpleName
    }

    private var oguryInterstitial: OguryInterstitialAd? = null

    /**
     * @param context                     the {@link android.content.Context} needed by the mediation SDK to make the ad request
     * @param serverParametersString      a String containing all needed parameters (as returned by Smart ad delivery)
     *                                    to make the mediation ad call
     * @param clientParameters            additional client-side parameters (user specific, like location)
     * @param interstitialAdapterListener the {@link SASMediationInterstitialAdapterListener} provided to
     *                                    this {@link com.smartadserver.android.library.mediation.SASMediationAdapter} to notify Smart SDK of events occurring
     */
    override fun requestInterstitialAd(
        context: Context,
        serverParametersString: String,
        clientParameters: MutableMap<String, Any>,
        interstitialAdapterListener: SASMediationInterstitialAdapterListener
    ) {
        Log.d(TAG, "SASOguryInterstitialAdapter adRequest")

        // Common configuration
        configureAdRequest(context, serverParametersString, interstitialAdapterListener)

        // Instantiate the Presage interstitial
        oguryInterstitial = OguryInterstitialAd(context, getAdUnitID(serverParametersString)).apply {
            setListener(this@SASOguryInterstitialAdapter)
            load()
        }
    }

    override fun onAdLoaded() {
        Log.d(TAG, "Ogury interstitial onAdLoaded")
        (mediationAdapterListener as? SASMediationInterstitialAdapterListener)?.onInterstitialLoaded()
    }

    override fun onAdDisplayed() {
        super.onAdDisplayed()
        (mediationAdapterListener as? SASMediationInterstitialAdapterListener)?.onInterstitialShown()
    }

    override fun showInterstitial() {
        oguryInterstitial?.run {
            if (isLoaded) {
                show()
            }
        }
    }

    override fun onDestroy() {
        oguryInterstitial = null
    }

}