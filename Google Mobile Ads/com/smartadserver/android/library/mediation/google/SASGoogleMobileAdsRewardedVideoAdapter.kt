package com.smartadserver.android.library.mediation.google

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.smartadserver.android.library.mediation.SASMediationRewardedVideoAdapter
import com.smartadserver.android.library.mediation.SASMediationRewardedVideoAdapterListener
import com.smartadserver.android.library.model.SASReward
import com.smartadserver.android.library.util.SASUtil
import java.lang.ref.WeakReference

/**
 * Mediation adapter class for AdMob rewarded video format
 */
class SASGoogleMobileAdsRewardedVideoAdapter : SASGoogleMobileAdsAdapterBase(), SASMediationRewardedVideoAdapter {
    // Google mobile ads rewarded ad
    private var mRewardedAd: RewardedAd? = null

    // WeakReference on Activity at loading time for future display
    private lateinit var activityWeakReference: WeakReference<Activity>
    private lateinit var rewardedVideoAdapterListener: SASMediationRewardedVideoAdapterListener

    /**
     * Requests a mediated rewarded video ad asynchronously.
     *
     * @param context                      The [Context] needed by the mediation SDK to make the ad request.
     * @param serverParametersString       a String containing all needed parameters (as returned by Smart ad delivery) to make the mediation ad call.
     * @param clientParameters             additional client-side parameters (user specific, like location).
     * @param rewardedVideoAdapterListener the [SASMediationRewardedVideoAdapterListener] provided to this [com.smartadserver.android.library.mediation.SASMediationAdapter] to notify the Smart SDK of events
     */
    override fun requestRewardedVideoAd(context: Context,
                                        serverParametersString: String,
                                        clientParameters: Map<String, Any>,
                                        rewardedVideoAdapterListener: SASMediationRewardedVideoAdapterListener) {

        // reset any previous leftover (?) rewarded ad
        mRewardedAd = null
        if (context !is Activity) {
            rewardedVideoAdapterListener.adRequestFailed("Google rewarded requires the Context to be an Activity for display", false)
            return
        }
        activityWeakReference = WeakReference(context)
        this.rewardedVideoAdapterListener = rewardedVideoAdapterListener
        val gma = initGoogleMobileAds(context, serverParametersString)
        val adUnitID = getAdUnitID(serverParametersString)
        if (GoogleMobileAds.ADMOB == gma) {
            // create rewarded ad request
            val adRequest = AdRequest.Builder().build()
            // execute request
            RewardedAd.load(context, adUnitID, adRequest, createRewardedAdLoadCallback(rewardedVideoAdapterListener))
        } else if (GoogleMobileAds.AD_MANAGER == gma) {
            // create rewarded publisher ad request
            val adManagerAdRequest = AdManagerAdRequest.Builder().build()
            // execute request
            RewardedAd.load(context, adUnitID, adManagerAdRequest, createRewardedAdLoadCallback(rewardedVideoAdapterListener))
        }
    }

    private fun createRewardedAdLoadCallback(rewardedVideoAdapterListener: SASMediationRewardedVideoAdapterListener): RewardedAdLoadCallback {
        return object : RewardedAdLoadCallback() {
            override fun onAdLoaded(rewardedAd: RewardedAd) {
                Log.d(TAG, "Google mobile ads onRewardedVideoAdLoaded for rewarded video")
                mRewardedAd = rewardedAd
                rewardedAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        super.onAdFailedToShowFullScreenContent(adError)
                    }

                    override fun onAdShowedFullScreenContent() {
                        Log.d(TAG, "Google mobile ads onAdShowedFullScreenContent for rewarded")
                        rewardedVideoAdapterListener.onRewardedVideoShown()
                        onDestroy()
                    }

                    override fun onAdDismissedFullScreenContent() {
                        Log.d(TAG, "Google mobile ads onAdDismissedFullScreenContent for rewarded")
                        rewardedVideoAdapterListener.onAdClosed()
                    }

                    override fun onAdImpression() {
                        Log.d(TAG, "Google mobile ads onAdImpression for rewarded")
                    }

                    override fun onAdClicked() {
                        Log.d(TAG, "Google mobile ads onAdClicked for rewarded")
                        rewardedVideoAdapterListener.onAdClicked()
                    }
                }
                rewardedVideoAdapterListener.onRewardedVideoLoaded()
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.d(TAG, "Google mobile ads onAdFailedToLoad for rewarded (error:$loadAdError)")
                val isNoAd = loadAdError.code == AdRequest.ERROR_CODE_NO_FILL
                rewardedVideoAdapterListener.adRequestFailed("Google mobile ads rewarded sad loading error $loadAdError", isNoAd)
            }
        }
    }

    /**
     * Shows the previously loaded rewarded video if any (or throws an exception if error).
     *
     * @throws Exception
     */
    @Throws(Exception::class)
    override fun showRewardedVideoAd() {
        mRewardedAd?.let { rewardedAd ->
            activityWeakReference.get()?.let { activity ->
                SASUtil.getMainLooperHandler().post {
                    rewardedAd.show(activity) { rewardItem ->
                        Log.d(TAG, "Google mobile ads onUserEarnedReward for rewarded ad : label:" + rewardItem.type + " amount:" + rewardItem.amount)

                        // notify Smart SDK of earned reward
                        rewardedVideoAdapterListener.onReward(SASReward(rewardItem.type, rewardItem.amount.toDouble()))
                    }
                }
            } ?: throw Exception("Activity to display Google rewarded is null")
        } ?: throw Exception("No Google mobile ads rewarded ad loaded !")
    }

    override fun onDestroy() {
        mRewardedAd = null
    }

    companion object {
        // tag for logging purposes
        private val TAG = SASGoogleMobileAdsRewardedVideoAdapter::class.java.simpleName
    }
}