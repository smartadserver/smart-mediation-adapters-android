package com.smartadserver.android.library.mediation.google

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.smartadserver.android.library.mediation.SASMediationInterstitialAdapter
import com.smartadserver.android.library.mediation.SASMediationInterstitialAdapterListener
import com.smartadserver.android.library.util.SASUtil
import java.lang.ref.WeakReference

/**
 * Mediation adapter class for Google mobile ads interstitial format
 */
class SASGoogleMobileAdsInterstitialAdapter : SASGoogleMobileAdsAdapterBase(), SASMediationInterstitialAdapter {
    // Google mobile ads interstitial ad
    var mInterstitialAd: InterstitialAd? = null

    // WeakReference on Activity at loading time for future display
    private lateinit var activityWeakReference: WeakReference<Activity>

    /**
     * Requests a mediated interstitial ad asynchronously
     *
     * @param context                     the [android.content.Context] needed by the mediation SDK to make the ad request
     * @param serverParametersString      a String containing all needed parameters (as returned by Smart ad delivery)
     * to make the mediation ad call
     * @param clientParameters            additional client-side parameters (user specific, like location)
     * @param interstitialAdapterListener the [SASMediationInterstitialAdapterListener] provided to
     * this [com.smartadserver.android.library.mediation.SASMediationAdapter] to notify Smart SDK of events occurring
     */
    override fun requestInterstitialAd(context: Context,
                                       serverParametersString: String,
                                       clientParameters: Map<String, Any>,
                                       interstitialAdapterListener: SASMediationInterstitialAdapterListener) {

        // reset any previous leftover (?) interstitial
        mInterstitialAd = null
        if (context !is Activity) {
            interstitialAdapterListener.adRequestFailed("Google interstitial requires the Context to be an Activity for display", false)
            return
        }
        activityWeakReference = WeakReference(context)
        val gma = initGoogleMobileAds(context, serverParametersString)
        val adUnitID = getAdUnitID(serverParametersString)
        if (GoogleMobileAds.ADMOB == gma) {
            // create Google mobile ad request
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(context, adUnitID, adRequest, createInterstitialAdLoadCallback(interstitialAdapterListener))
        } else if (GoogleMobileAds.AD_MANAGER == gma) {
            // create Google mobile ad request
            val publisherAdRequest = AdManagerAdRequest.Builder().build()

            // create Google mobile ads interstitial ad object
            AdManagerInterstitialAd.load(context, adUnitID, publisherAdRequest, createInterstitialAdLoadCallback(interstitialAdapterListener))
        }
    }

    private fun createInterstitialAdLoadCallback(interstitialAdapterListener: SASMediationInterstitialAdapterListener): InterstitialAdLoadCallback {
        return object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "Google mobile ads ad onAdLoaded for interstitial")
                mInterstitialAd = interstitialAd

                // Create fullscreen callback
                interstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        // Called when fullscreen content failed to show.
                        Log.d("TAG", "Google mobile ads onAdFailedToShowFullScreenContent : " + adError.message)
                        interstitialAdapterListener.onInterstitialFailedToShow(adError.message)
                    }

                    override fun onAdShowedFullScreenContent() {
                        Log.d(TAG, "Google mobile ads onAdShowedFullScreenContent for interstitial")
                        interstitialAdapterListener.onInterstitialShown()
                        onDestroy()
                    }

                    override fun onAdDismissedFullScreenContent() {
                        Log.d(TAG, "Google mobile ads onAdDismissedFullScreenContent for interstitial")
                        interstitialAdapterListener.onAdClosed()
                    }

                    override fun onAdImpression() {
                        Log.d(TAG, "Google mobile ads onAdImpression for interstitial")
                    }
                }

                // notify Smart SDK of successful interstitial loading
                interstitialAdapterListener.onInterstitialLoaded()
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.d(TAG, "Google mobile ads onAdFailedToLoad for interstitial (error:$loadAdError)")
                val isNoAd = loadAdError.code == AdRequest.ERROR_CODE_NO_FILL
                interstitialAdapterListener.adRequestFailed("Google mobile ads interstitial ad loading error $loadAdError", isNoAd)
            }
        }
    }

    /**
     * Shows the previously loaded interstitial if any (or throws an exception if error)
     *
     * @throws Exception
     */
    @Throws(Exception::class)
    override fun showInterstitial() {
        mInterstitialAd?.let { interstitialAd ->
            activityWeakReference.get()?.let { activity ->
                SASUtil.getMainLooperHandler().post { interstitialAd.show(activity) }
            } ?: throw Exception("Activity to display Google interstitial is null")
        } ?: throw Exception("No Google mobile ads interstitial ad loaded !")
    }

    override fun onDestroy() {
        mInterstitialAd = null
    }

    companion object {
        // tag for logging purposes
        private val TAG = SASGoogleMobileAdsInterstitialAdapter::class.java.simpleName
    }
}