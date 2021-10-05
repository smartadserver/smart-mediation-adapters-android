package com.smartadserver.android.library.mediation.ogury

import android.content.Context
import android.util.Log
import com.ogury.ed.OguryBannerAdListener
import com.ogury.ed.OguryBannerAdSize
import com.ogury.ed.OguryBannerAdView
import com.smartadserver.android.library.mediation.SASMediationBannerAdapter
import com.smartadserver.android.library.mediation.SASMediationBannerAdapterListener

/**
 *  Mediation adapter class for Ogury banner ad format
 */
class SASOguryBannerAdapter : SASOguryAdapterBase(), SASMediationBannerAdapter,
    OguryBannerAdListener {

    companion object {
        private val TAG = SASOguryBannerAdapter::class.java.simpleName
    }

    private var bannerAdView: OguryBannerAdView? = null

    override fun requestBannerAd(
        context: Context,
        serverParametersString: String,
        clientParameters: MutableMap<String, Any>,
        bannerAdapterListener: SASMediationBannerAdapterListener
    ) {
        Log.d(TAG, "SASOguryBannerAdapter adRequest")

        // Common configuration
        configureAdRequest(context, serverParametersString, bannerAdapterListener)

        bannerAdView = OguryBannerAdView(context).apply {
            setListener(this@SASOguryBannerAdapter)
            setAdUnit(getAdUnitID(serverParametersString))
            setAdSize(getBannerAdSize(serverParametersString))
        }

        bannerAdView?.loadAd()
    }

    private fun getBannerAdSize(serverParametersString: String) =
        when (serverParametersString.split("|").getOrElse(2){"0"}) {
            "1" -> OguryBannerAdSize.MPU_300x250
            else -> OguryBannerAdSize.SMALL_BANNER_320x50
        }

    override fun onAdLoaded() {
        Log.d(TAG, "Ogury banner listener onAdLoaded")

        bannerAdView?.let {
            (mediationAdapterListener as? SASMediationBannerAdapterListener)?.onBannerLoaded(it)
        }
    }

    override fun onDestroy() {
        bannerAdView?.destroy()
        bannerAdView = null
    }

}