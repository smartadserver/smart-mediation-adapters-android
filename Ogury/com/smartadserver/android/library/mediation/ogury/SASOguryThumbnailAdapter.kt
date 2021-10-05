package com.smartadserver.android.library.mediation.ogury

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.FrameLayout
import com.ogury.ed.OguryThumbnailAd
import com.ogury.ed.OguryThumbnailAdListener
import com.smartadserver.android.library.mediation.SASMediationBannerAdapter
import com.smartadserver.android.library.mediation.SASMediationBannerAdapterListener

/**
 * Mediation adapter class for Ogury Thumbnail format
 */
class SASOguryThumbnailAdapter : SASOguryAdapterBase(), SASMediationBannerAdapter,
    OguryThumbnailAdListener {

    companion object {
        private val TAG = SASOguryThumbnailAdapter::class.java.simpleName
    }

    private var thumbnailAd: OguryThumbnailAd? = null

    private var dummyBanner: FrameLayout? = null

    override fun requestBannerAd(
        context: Context,
        serverParametersString: String,
        clientParameters: MutableMap<String, Any>,
        bannerAdapterListener: SASMediationBannerAdapterListener
    ) {
        Log.d(TAG, "SASOguryThumbnailAdapter adRequest")

        // Common configuration
        configureAdRequest(context, serverParametersString, bannerAdapterListener)

        // Loading the thumbnail
        thumbnailAd = OguryThumbnailAd(context, getAdUnitID(serverParametersString)).apply {
            setListener(this@SASOguryThumbnailAdapter)
        }

        // Preparing the dummy layout that will be returned to the SDK
        val thumbnailSizeParameters = getThumbnailAdSize(serverParametersString)
        dummyBanner = object : FrameLayout(context) {
            override fun onAttachedToWindow() {
                super.onAttachedToWindow()
                thumbnailAd?.run {
                    if (isLoaded) {
                        show(
                            context as Activity,
                            thumbnailSizeParameters[2],
                            thumbnailSizeParameters[3]
                        )
                    }
                }
            }
        }

        // Load thumbnail ad
        thumbnailAd?.load(thumbnailSizeParameters[0], thumbnailSizeParameters[1])
    }

    private fun getThumbnailAdSize(serverParametersString: String) =
        serverParametersString.split("|")
            .let { parameters ->
                if (parameters.count() >= 6) {
                    arrayOf(
                        parameters[2].toInt(),
                        parameters[3].toInt(),
                        parameters[4].toInt(),
                        parameters[5].toInt()
                    )
                } else {
                    arrayOf(0, 0, 0, 0)
                }
            }

    override fun onAdLoaded() {
        Log.d(TAG, "Ogury thumbnail onAdLoaded")
        dummyBanner?.let {
            (mediationAdapterListener as? SASMediationBannerAdapterListener)?.onBannerLoaded(it)
        }
    }

    override fun onDestroy() {
        thumbnailAd = null
    }

}