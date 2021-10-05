package com.smartadserver.android.library.mediation.mopub

import android.app.Activity
import android.content.Context
import android.util.Log
import com.mopub.common.*
import com.mopub.common.privacy.ConsentDialogListener
import com.mopub.mobileads.MoPubErrorCode
import com.mopub.mobileads.MoPubRewardedVideoListener
import com.mopub.mobileads.MoPubRewardedVideoManager
import com.mopub.mobileads.MoPubRewardedVideos
import com.smartadserver.android.library.mediation.SASMediationRewardedVideoAdapter
import com.smartadserver.android.library.mediation.SASMediationRewardedVideoAdapterListener
import com.smartadserver.android.library.model.SASReward

/**
 * Mediation adapter class for MoPub rewarded video format
 */
class SASMoPubRewardedVideoAdapter : SASMediationRewardedVideoAdapter {
    private var adUnitID: String = ""

    // GDPR related
    private var needToShowConsentDialog = false

    /**
     * @param context                      The [Context] needed by the mediation SDK to make the ad request.
     * @param serverParametersString       a String containing all needed parameters (as returned by Smart ad delivery) to make the mediation ad call.
     * @param clientParameters             additional client-side parameters (user specific, like location).
     * @param rewardedVideoAdapterListener the [SASMediationRewardedVideoAdapterListener] provided to this [com.smartadserver.android.library.mediation.SASMediationAdapter] to notify the Smart SDK of events
     */
    override fun requestRewardedVideoAd(context: Context, serverParametersString: String,
                                        clientParameters: MutableMap<String, Any>,
                                        rewardedVideoAdapterListener: SASMediationRewardedVideoAdapterListener) {
        Log.d(TAG, "SASMoPubRewardedVideoAdapter adRequest")

        // To request an interstitial using MoPub, the context have to be an Activity.
        if (context !is Activity) {
            rewardedVideoAdapterListener.adRequestFailed("Can not get a MoPub rewarded video because its creation context is not an Activity", false)
            return
        }

        // Here serverParameterString is the MoPub Ad unit id
        adUnitID = serverParametersString

        // Init MoPub SDK
        if (!initMoPubDone) {
            val sdkConfiguration = SdkConfiguration.Builder(serverParametersString)
                    .build()
            val initializationListener = SdkInitializationListener {
                Log.d(TAG, "MoPub onInitializationFinished")
                initMoPubDone = true
                // call requestRewardedVideoAd again, with SDK initialized
                requestRewardedVideoAd(context, serverParametersString, clientParameters, rewardedVideoAdapterListener)
            }
            MoPub.initializeSdk(context, sdkConfiguration!!, initializationListener)
        } else {
            val personalInfoManager = MoPub.getPersonalInformationManager()
            // store that the consent dialog need to be shown. We will show it AFTER the interstitial display to avoid having the rewarded video above the consent dialog.
            needToShowConsentDialog = personalInfoManager?.shouldShowConsentDialog() ?: false
            if (needToShowConsentDialog) {
                personalInfoManager!!.loadConsentDialog(object : ConsentDialogListener {
                    override fun onConsentDialogLoaded() {
                        // // to be displayed once the rewarded video is being shown
                    }

                    override fun onConsentDialogLoadFailed(moPubErrorCode: MoPubErrorCode) {
                        Log.d(TAG, "MoPub onConsentDialogLoadFailed")
                    }
                })
            }

            // Instantiate Rewarded Video Listener
            val rewardedVideoListenerImpl: MoPubRewardedVideoListener = object : MoPubRewardedVideoListener {
                override fun onRewardedVideoLoadSuccess(adUnitId: String) {
                    Log.d(TAG, "RewardedVideoListener onRewardedVideoLoadSuccess")
                    rewardedVideoAdapterListener.onRewardedVideoLoaded()
                }

                override fun onRewardedVideoLoadFailure(adUnitId: String, errorCode: MoPubErrorCode) {
                    Log.d(TAG, "RewardedVideoListener onRewardedVideoLoadFailure")

                    // check if this is due to a No Ad
                    val isNoAd = errorCode == MoPubErrorCode.NO_FILL || errorCode == MoPubErrorCode.NETWORK_NO_FILL
                    rewardedVideoAdapterListener.adRequestFailed(errorCode.toString(), isNoAd)
                }

                override fun onRewardedVideoStarted(adUnitId: String) {
                    Log.d(TAG, "RewardedVideoListener onRewardedVideoStarted")
                    rewardedVideoAdapterListener.onRewardedVideoShown()
                    if (needToShowConsentDialog) {
                        personalInfoManager?.shouldShowConsentDialog()
                    }
                }

                override fun onRewardedVideoPlaybackError(adUnitId: String, errorCode: MoPubErrorCode) {
                    Log.d(TAG, "RewardedVideoListener onRewardedVideoPlaybackError")
                    rewardedVideoAdapterListener.onRewardedVideoFailedToShow(errorCode.toString())
                }

                override fun onRewardedVideoClicked(adUnitId: String) {
                    Log.d(TAG, "RewardedVideoListener onRewardedVideoClicked")
                    rewardedVideoAdapterListener.onAdClicked()
                }

                override fun onRewardedVideoClosed(adUnitId: String) {
                    Log.d(TAG, "RewardedVideoListener onRewardedVideoClosed")
                    rewardedVideoAdapterListener.onAdClosed()
                }

                override fun onRewardedVideoCompleted(adUnitIds: Set<String?>, reward: MoPubReward) {
                    Log.d(TAG, "RewardedVideoListener onRewardedVideoCompleted")

                    // notify Smart SDK of earned reward
                    rewardedVideoAdapterListener.onReward(SASReward(reward.label, reward.amount.toDouble()))
                }
            }

            // Instantiate MoPub Rewarded Video
            MoPubRewardedVideos.setRewardedVideoListener(rewardedVideoListenerImpl)
            MoPubRewardedVideos.loadRewardedVideo(adUnitID,
                    null as MoPubRewardedVideoManager.RequestParameters?,
                    null as MediationSettings?)
        }
    }

    @kotlin.Throws(Exception::class)
    override fun showRewardedVideoAd() {
        if (MoPubRewardedVideos.hasRewardedVideo(adUnitID)) {
            MoPubRewardedVideos.showRewardedVideo(adUnitID)
        }
    }

    override fun onDestroy() {
        // nothing to do
    }

    companion object {
        private val TAG = SASMoPubRewardedVideoAdapter::class.java.simpleName
        private var initMoPubDone = false
    }
}