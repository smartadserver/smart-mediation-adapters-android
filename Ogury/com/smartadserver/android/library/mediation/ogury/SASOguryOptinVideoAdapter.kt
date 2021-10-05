package com.smartadserver.android.library.mediation.ogury

import android.content.Context
import android.util.Log
import com.ogury.ed.OguryOptinVideoAd
import com.ogury.ed.OguryOptinVideoAdListener
import com.ogury.ed.OguryReward
import com.smartadserver.android.library.mediation.SASMediationRewardedVideoAdapter
import com.smartadserver.android.library.mediation.SASMediationRewardedVideoAdapterListener
import com.smartadserver.android.library.model.SASReward

class SASOguryOptinVideoAdapter : SASOguryAdapterBase(), SASMediationRewardedVideoAdapter,
    OguryOptinVideoAdListener {

    companion object {
        private val TAG = SASOguryOptinVideoAdapter::class.java.simpleName
    }

    private var optinVideoAd: OguryOptinVideoAd? = null

    /**
     * @param context                     the {@link android.content.Context} needed by the mediation SDK to make the ad request
     * @param serverParametersString      a String containing all needed parameters (as returned by Smart ad delivery)
     *                                    to make the mediation ad call
     * @param clientParameters            additional client-side parameters (user specific, like location)
     * @param rewardedVideoAdapterListener the {@link SASMediationRewardedVideoAdapterListener} provided to
     *                                    this {@link com.smartadserver.android.library.mediation.SASMediationAdapter} to notify Smart SDK of events occurring
     */
    override fun requestRewardedVideoAd(
        context: Context,
        serverParametersString: String,
        clientParameters: MutableMap<String, Any>,
        rewardedVideoAdapterListener: SASMediationRewardedVideoAdapterListener
    ) {
        Log.d(TAG, "SASOguryOptinVideoAdapter adRequest")

        // Common configuration
        configureAdRequest(context, serverParametersString, rewardedVideoAdapterListener)

        // Instantiate the optin interstitial
        optinVideoAd = OguryOptinVideoAd(context, getAdUnitID(serverParametersString)).apply {
            setListener(this@SASOguryOptinVideoAdapter)
            load()
        }
    }

    override fun showRewardedVideoAd() {
        optinVideoAd?.run {
            if (isLoaded) {
                show()
            }
        }
    }

    override fun onAdLoaded() {
        Log.d(TAG, "Ogury optin video onAdLoaded")
        (mediationAdapterListener as? SASMediationRewardedVideoAdapterListener)?.onRewardedVideoLoaded()
    }

    override fun onAdDisplayed() {
        super.onAdDisplayed()
        (mediationAdapterListener as? SASMediationRewardedVideoAdapterListener)?.onRewardedVideoShown()
    }

    override fun onAdRewarded(reward: OguryReward?) {
        Log.d(TAG, "OguryOptinVideoAdListener onAdRewarded")

        // Notify Smart SDK of earned reward, if reward is numerical
        reward?.let { oguryReward ->
            oguryReward.value.toDoubleOrNull()?.let { rewardValue ->
                (mediationAdapterListener as? SASMediationRewardedVideoAdapterListener)?.onReward(
                    SASReward(oguryReward.name, rewardValue)
                )
            }
        }
    }

    override fun onDestroy() {
        optinVideoAd = null
    }

}