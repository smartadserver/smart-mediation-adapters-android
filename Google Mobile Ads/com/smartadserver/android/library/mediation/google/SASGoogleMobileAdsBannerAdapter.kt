package com.smartadserver.android.library.mediation.google

import android.content.Context
import android.util.Log
import android.view.View
import com.google.android.gms.ads.*
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView
import com.smartadserver.android.library.mediation.SASMediationBannerAdapter
import com.smartadserver.android.library.mediation.SASMediationBannerAdapterListener

/**
 * Mediation adapter class for Google mobile ads banner format
 */
class SASGoogleMobileAdsBannerAdapter : SASGoogleMobileAdsAdapterBase(), SASMediationBannerAdapter {
    // Google mobile ads banner view instance
    var adView: View? = null

    /**
     * Requests a mediated banner ad asynchronously
     *
     * @param context                the [android.content.Context] needed by the mediation SDK to make the ad request
     * @param serverParametersString a String containing all needed parameters (as returned by Smart ad delivery)
     * to make the mediation ad call
     * @param clientParameters       additional client-side parameters (user specific, like location)
     * @param bannerAdapterListener  the [SASMediationBannerAdapterListener] provided to
     * this [com.smartadserver.android.library.mediation.SASMediationAdapter] to notify Smart SDK of events occurring
     */
    override fun requestBannerAd(context: Context,
                                 serverParametersString: String,
                                 clientParameters: Map<String, Any>,
                                 bannerAdapterListener: SASMediationBannerAdapterListener) {
        val adUnitID = getAdUnitID(serverParametersString)
        val gma = initGoogleMobileAds(context, serverParametersString)
        val adSize = getAdSize(serverParametersString)
        if (GoogleMobileAds.ADMOB == gma) {
            // create google ad request
            val adRequest = AdRequest.Builder().build()

            // Create Google AdView and configure it.
            val adMobView = AdView(context)
            adMobView.adUnitId = adUnitID
            adMobView.setAdSize(adSize)
            val adListener = createAdListener(bannerAdapterListener, adMobView)

            // set listener on banner
            adMobView.adListener = adListener

            // perform ad request
            adMobView.loadAd(adRequest)
            adView = adMobView
        } else if (GoogleMobileAds.AD_MANAGER == gma) {
            // create google publisher ad request
            val publisherAdRequest = AdManagerAdRequest.Builder().build()
            val adManagerView = AdManagerAdView(context)
            adManagerView.adUnitId = adUnitID
            adManagerView.setAdSizes(adSize)
            val adListener = createAdListener(bannerAdapterListener, adManagerView)

            // set listener on banner
            adManagerView.adListener = adListener

            // perform ad request
            adManagerView.loadAd(publisherAdRequest)
            adView = adManagerView
        }
    }

    private fun createAdListener(bannerAdapterListener: SASMediationBannerAdapterListener, adView: View): AdListener {
        // create Google banner listener that will intercept ad mob banner events and call appropriate SASMediationBannerAdapterListener counterpart methods
        return object : AdListener() {
            override fun onAdClosed() {
                Log.d(TAG, "Google mobile ads onAdClosed for banner")
                bannerAdapterListener.onAdClosed()
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.d(TAG, "Google mobile ads onAdFailedToLoad for banner (error : $loadAdError)")
                val isNoAd = loadAdError.code == AdRequest.ERROR_CODE_NO_FILL
                bannerAdapterListener.adRequestFailed("Google mobile ads banner ad loading error : $loadAdError", isNoAd)
            }

            override fun onAdClicked() {
                Log.d(TAG, "Google mobile ads onAdClicked for banner")
            }

            override fun onAdImpression() {
                Log.d(TAG, "Google mobile ads onAdImpression for banner")
            }

            override fun onAdOpened() {
                Log.d(TAG, "Google mobile ads onAdOpened for banner")
                bannerAdapterListener.onAdClicked()
            }

            override fun onAdLoaded() {
                Log.d(TAG, "Google mobile ads onAdLoaded for banner")
                bannerAdapterListener.onBannerLoaded(adView)
            }
        }
    }

    /**
     * Utility method to get Banner Size from serverParametersString
     */
    private fun getAdSize(serverParametersString: String)= when (serverParametersString.split("|").getOrElse(2){"0"}) {
            "1" -> AdSize.MEDIUM_RECTANGLE
            "2" -> AdSize.LEADERBOARD
            "3" -> AdSize.LARGE_BANNER
            else -> AdSize.BANNER
        }

    override fun onDestroy() {
        Log.d(TAG, "Google mobile ads onDestroy for banner")
        (adView as? AdView)?.destroy() ?: (adView as? AdManagerAdView)?.destroy()
    }

    companion object {
        // tag for logging purposes
        private val TAG = SASGoogleMobileAdsBannerAdapter::class.java.simpleName
    }
}