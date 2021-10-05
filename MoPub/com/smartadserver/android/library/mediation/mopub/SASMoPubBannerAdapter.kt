package com.smartadserver.android.library.mediation.mopub

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import com.mopub.common.MoPub
import com.mopub.common.SdkConfiguration
import com.mopub.common.SdkInitializationListener
import com.mopub.common.privacy.ConsentDialogListener
import com.mopub.mobileads.MoPubErrorCode
import com.mopub.mobileads.MoPubView
import com.mopub.mobileads.MoPubView.BannerAdListener
import com.smartadserver.android.library.mediation.SASMediationAdapter
import com.smartadserver.android.library.mediation.SASMediationBannerAdapter
import com.smartadserver.android.library.mediation.SASMediationBannerAdapterListener
import com.smartadserver.android.library.util.SASConfiguration

/**
 * Mediation adapter class for MoPub banner format
 */
class SASMoPubBannerAdapter : SASMediationBannerAdapter {
    private var bannerAdView: MoPubView? = null

    /**
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
        Log.d(TAG, "SASMoPubBannerAdapter requestAd")

        // Init MoPub SDK -- Here serverParametersString is the MoPub Ad unit id
        if (!initMoPubDone) {
            val sdkConfiguration = SdkConfiguration.Builder(serverParametersString)
                    .build()
            val initializationListener = SdkInitializationListener {
                Log.d(TAG, "MoPub onInitializationFinished")
                initMoPubDone = true
                // call requestBannerAd again, with SDK initialized
                requestBannerAd(context, serverParametersString, clientParameters, bannerAdapterListener)
            }
            MoPub.initializeSdk(context, sdkConfiguration!!, initializationListener)
        } else {
            // Pass geolocation if available
            MoPub.setLocationAwareness(if (SASConfiguration.getSharedInstance().isAutomaticLocationDetectionAllowed)
                MoPub.LocationAwareness.NORMAL else MoPub.LocationAwareness.DISABLED)
            val personalInfoManager = MoPub.getPersonalInformationManager()
            if (personalInfoManager?.shouldShowConsentDialog() == true) {
                personalInfoManager.loadConsentDialog(object : ConsentDialogListener {
                    override fun onConsentDialogLoaded() {
                        personalInfoManager.showConsentDialog()
                    }

                    override fun onConsentDialogLoadFailed(moPubErrorCode: MoPubErrorCode) {
                        Log.d(TAG, "MoPub onConsentDialogLoadFailed : $moPubErrorCode")
                    }
                })
            }

            // Instantiate Banner Ad Listener
            val bannerAdListenerImpl: BannerAdListener = object : BannerAdListener {
                override fun onBannerLoaded(banner: MoPubView) {
                    Log.d(TAG, "BannerAdListener onBannerLoaded")
                    bannerAdapterListener.onBannerLoaded(banner)
                }

                override fun onBannerFailed(banner: MoPubView, errorCode: MoPubErrorCode) {
                    Log.d(TAG, "BannerAdListener onBannerFailed")

                    // check if this is due to a no ad
                    val isNoAd = errorCode == MoPubErrorCode.NO_FILL || errorCode == MoPubErrorCode.NETWORK_NO_FILL
                    bannerAdapterListener.adRequestFailed(errorCode.toString(), isNoAd)
                }

                override fun onBannerClicked(banner: MoPubView) {
                    Log.d(TAG, "BannerAdListener onBannerClicked")
                    bannerAdapterListener.onAdClicked()
                }

                override fun onBannerExpanded(banner: MoPubView) {
                    Log.d(TAG, "BannerAdListener onBannerExpanded")
                    bannerAdapterListener.onAdFullScreen()
                }

                override fun onBannerCollapsed(banner: MoPubView) {
                    Log.d(TAG, "BannerAdListener onBannerCollapse")
                    bannerAdapterListener.onAdClosed()
                }
            }


            // Instantiate MoPub banner view
            bannerAdView = MoPubView(context)

            // retrieve ad view width and height from clientParameters
            val width = (clientParameters[SASMediationAdapter.AD_VIEW_WIDTH_KEY] as? String)?.toIntOrNull() ?: 0
            val height = (clientParameters[SASMediationAdapter.AD_VIEW_HEIGHT_KEY] as? String)?.toIntOrNull() ?: 0
            val lParams = ViewGroup.LayoutParams(width, height)

            bannerAdView!!.run {
                setAdUnitId(serverParametersString)
                layoutParams = lParams

                // set banner listener
                bannerAdListener = bannerAdListenerImpl

                // disable auto refresh to avoid discrepancy
                autorefreshEnabled = false

                // Load the ad
                loadAd()
            }
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "MoPub onDestroy for banner")
        bannerAdView?.destroy()
        bannerAdView = null
    }

    companion object {
        private val TAG = SASMoPubBannerAdapter::class.java.simpleName
        private var initMoPubDone = false
    }
}