package com.smartadserver.android.library.mediation.mopub

import android.app.Activity
import android.content.Context
import android.util.Log
import com.mopub.common.MoPub
import com.mopub.common.SdkConfiguration
import com.mopub.common.SdkInitializationListener
import com.mopub.common.privacy.ConsentDialogListener
import com.mopub.mobileads.MoPubErrorCode
import com.mopub.mobileads.MoPubInterstitial
import com.mopub.mobileads.MoPubInterstitial.InterstitialAdListener
import com.smartadserver.android.library.mediation.SASMediationInterstitialAdapter
import com.smartadserver.android.library.mediation.SASMediationInterstitialAdapterListener

/**
 * Mediation adapter class for MoPub interstitial format
 */
class SASMoPubInterstitialAdapter : SASMediationInterstitialAdapter {
    // MoPub interstitial instance
    private var moPubInterstitial: MoPubInterstitial? = null

    // GDPR related
    private var needToShowConsentDialog = false

    /**
     * @param context                     the [android.content.Context] needed by the mediation SDK to make the ad request
     * @param serverParametersString      a String containing all needed parameters (as returned by Smart ad delivery)
     * to make the mediation ad call
     * @param clientParameters            additional client-side parameters (user specific, like location)
     * @param interstitialAdapterListener the [SASMediationInterstitialAdapterListener] provided to
     * this [com.smartadserver.android.library.mediation.SASMediationAdapter] to notify Smart SDK of events occurring
     */
    override fun requestInterstitialAd(context: Context, serverParametersString: String,
                                       clientParameters: MutableMap<String, Any>,
                                       interstitialAdapterListener: SASMediationInterstitialAdapterListener) {
        Log.d(TAG, "SASMoPubInterstitialAdapter adRequest")

        // To request an interstitial using MoPub, the context have to be an Activity.
        if (context !is Activity) {
            interstitialAdapterListener.adRequestFailed("Can not get a MoPub interstitial because its creation context is not an Activity", false)
            return
        }

        // Init MoPub SDK -- Here serverParameterString is the MoPub Ad unit id
        if (!initMoPubDone) {
            val sdkConfiguration = SdkConfiguration.Builder(serverParametersString)
                    .build()
            val initializationListener = SdkInitializationListener {
                Log.d(TAG, "MoPub onInitializationFinished")
                initMoPubDone = true
                // call requestInterstitialAd again, with SDK initialized
                requestInterstitialAd(context, serverParametersString, clientParameters, interstitialAdapterListener)
            }
            MoPub.initializeSdk(context, sdkConfiguration!!, initializationListener)
        } else {
            val personalInfoManager = MoPub.getPersonalInformationManager()
            // store that the consent dialog need to be shown. We will show it AFTER the interstitial display to avoid having the interstitial above the consent dialog.
            needToShowConsentDialog = personalInfoManager?.shouldShowConsentDialog() ?: false
            if (needToShowConsentDialog) {
                personalInfoManager!!.loadConsentDialog(object : ConsentDialogListener {
                    override fun onConsentDialogLoaded() {
                        // to be displayed once the interstitial is being shown
                    }

                    override fun onConsentDialogLoadFailed(moPubErrorCode: MoPubErrorCode) {
                        Log.d(TAG, "MoPub onConsentDialogLoadFailed")
                    }
                })
            }

            // Instantiate Interstitial Ad Listener
            val interstitialAdListenerImpl: InterstitialAdListener = object : InterstitialAdListener {
                override fun onInterstitialLoaded(interstitial: MoPubInterstitial?) {
                    Log.d(TAG, "InterstitialAdListener onInterstitialLoaded")
                    interstitialAdapterListener.onInterstitialLoaded()
                }

                override fun onInterstitialFailed(interstitial: MoPubInterstitial?, errorCode: MoPubErrorCode?) {
                    Log.d(TAG, "InterstitialAdListener onInterstitialFailed")

                    // check if this is due to a No Ad
                    val isNoAd = errorCode == MoPubErrorCode.NO_FILL || errorCode == MoPubErrorCode.NETWORK_NO_FILL
                    interstitialAdapterListener.adRequestFailed(errorCode.toString(), isNoAd)
                }

                override fun onInterstitialShown(interstitial: MoPubInterstitial?) {
                    Log.d(TAG, "InterstitialAdListener onInterstitialShown")
                    interstitialAdapterListener.onInterstitialShown()
                    if (needToShowConsentDialog) {
                        personalInfoManager?.showConsentDialog()
                    }
                }

                override fun onInterstitialClicked(interstitial: MoPubInterstitial?) {
                    Log.d(TAG, "InterstitialAdListener onInterstitialClicked")
                    interstitialAdapterListener.onAdClicked()
                }

                override fun onInterstitialDismissed(interstitial: MoPubInterstitial?) {
                    Log.d(TAG, "InterstitialAdListener onInterstitialDismissed")
                    interstitialAdapterListener.onAdClosed()
                    onDestroy()
                }
            }

            // Instantiate the MoPub Interstitial
            moPubInterstitial = MoPubInterstitial(context, serverParametersString)

            // and set the listener & load the ad
            moPubInterstitial!!.run {
                interstitialAdListener = interstitialAdListenerImpl;
                load()
            }
        }
    }

    @kotlin.Throws(Exception::class)
    override fun showInterstitial() {
        moPubInterstitial?.run {
            if (isReady) {
                show()
            }
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "MoPub onDestroy for interstitial")
        moPubInterstitial?.destroy()
        moPubInterstitial = null;
    }

    companion object {
        // tag for logging purposes
        private val TAG = SASMoPubInterstitialAdapter::class.java.simpleName
        private var initMoPubDone = false
    }
}