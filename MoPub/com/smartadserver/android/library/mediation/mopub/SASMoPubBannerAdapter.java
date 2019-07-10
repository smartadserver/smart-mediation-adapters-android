package com.smartadserver.android.library.mediation.mopub;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.ViewGroup;

import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.mopub.common.privacy.ConsentDialogListener;
import com.mopub.common.privacy.PersonalInfoManager;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubView;
import com.smartadserver.android.library.mediation.SASMediationBannerAdapter;
import com.smartadserver.android.library.mediation.SASMediationBannerAdapterListener;
import com.smartadserver.android.library.util.SASConfiguration;


import java.util.ArrayList;
import java.util.Map;

/**
 * Mediation adapter class for MoPub banner format
 */
public class SASMoPubBannerAdapter implements SASMediationBannerAdapter {

    static private final String TAG = SASMoPubBannerAdapter.class.getSimpleName();

    private static boolean initMoPubDone = false;

    private MoPubView bannerAdView;

    /**
     * @param context                the {@link android.content.Context} needed by the mediation SDK to make the ad request
     * @param serverParametersString a String containing all needed parameters (as returned by Smart ad delivery)
     *                               to make the mediation ad call
     * @param clientParameters       additional client-side parameters (user specific, like location)
     * @param bannerAdapterListener  the {@link SASMediationBannerAdapterListener} provided to
     *                               this {@link com.smartadserver.android.library.mediation.SASMediationAdapter} to notify Smart SDK of events occurring
     */
    @Override
    public void requestBannerAd(@NonNull final Context context, @NonNull final String serverParametersString, @NonNull final Map<String, String> clientParameters,
                                @NonNull final SASMediationBannerAdapterListener bannerAdapterListener) {

        Log.d(TAG, "SASMoPubBannerAdapter requestAd");

        // Init MoPub SDK -- Here serverParametersString is the MoPub Ad unit id
        if (!initMoPubDone) {
            SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder(serverParametersString)
                    .build();

            SdkInitializationListener initializationListener = new SdkInitializationListener() {
                @Override
                public void onInitializationFinished() {
                    Log.d(TAG, "MoPub onInitializationFinished");
                    initMoPubDone = true;
                    // call requestBannerAd again, with SDK initialized
                    requestBannerAd(context, serverParametersString, clientParameters, bannerAdapterListener);
                }
            };

            MoPub.initializeSdk(context, sdkConfiguration, initializationListener);
        } else {
            // Pass geolocation if available
            MoPub.setLocationAwareness(SASConfiguration.getSharedInstance().isAutomaticLocationDetectionAllowed() ?
                    MoPub.LocationAwareness.NORMAL :
                    MoPub.LocationAwareness.DISABLED);


            final PersonalInfoManager personalInfoManager = MoPub.getPersonalInformationManager();
            if (personalInfoManager != null && personalInfoManager.shouldShowConsentDialog()) {
                personalInfoManager.loadConsentDialog(new ConsentDialogListener() {
                    @Override
                    public void onConsentDialogLoaded() {
                        personalInfoManager.showConsentDialog();
                    }

                    @Override
                    public void onConsentDialogLoadFailed(@NonNull MoPubErrorCode moPubErrorCode) {
                        Log.d(TAG, "MoPub onConsentDialogLoadFailed : " + moPubErrorCode.toString());
                    }
                });
            }

            // Instantiate Banner Ad Listener
            MoPubView.BannerAdListener bannerAdListener = new MoPubView.BannerAdListener() {
                @Override
                public void onBannerLoaded(MoPubView banner) {
                    Log.d(TAG, "BannerAdListener onBannerLoaded");
                    bannerAdapterListener.onBannerLoaded(banner);
                }

                @Override
                public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
                    Log.d(TAG, "BannerAdListener onBannerFailed");

                    // check if this is due to a no ad
                    boolean isNoAd = errorCode == MoPubErrorCode.NO_FILL || errorCode == MoPubErrorCode.NETWORK_NO_FILL;
                    bannerAdapterListener.adRequestFailed(errorCode.toString(), isNoAd);
                }

                @Override
                public void onBannerClicked(MoPubView banner) {
                    Log.d(TAG, "BannerAdListener onBannerClicked");
                    bannerAdapterListener.onAdClicked();
                }

                @Override
                public void onBannerExpanded(MoPubView banner) {
                    Log.d(TAG, "BannerAdListener onBannerExpanded");
                    bannerAdapterListener.onAdFullScreen();
                }

                @Override
                public void onBannerCollapsed(MoPubView banner) {
                    Log.d(TAG, "BannerAdListener onBannerCollapse");
                    bannerAdapterListener.onAdClosed();
                }
            };


            // Instantiate MoPub banner view
            bannerAdView = new MoPubView(context);
            bannerAdView.setAdUnitId(serverParametersString);

            // retrieve ad view height and width through clientParameters
            int viewWidth = Integer.parseInt(clientParameters.get("adViewWidth"));
            int viewHeight = Integer.parseInt(clientParameters.get("adViewHeight"));
            ViewGroup.LayoutParams lParams = new ViewGroup.LayoutParams(viewWidth, viewHeight);
            bannerAdView.setLayoutParams(lParams);

            // set banner listener
            bannerAdView.setBannerAdListener(bannerAdListener);

            // disable auto refresh to avoid discrepancy
            bannerAdView.setAutorefreshEnabled(false);

            // Load the ad
            bannerAdView.loadAd();
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "MoPub onDestroy for banner");
        if (bannerAdView != null) {
            bannerAdView.destroy();
            bannerAdView = null;
        }
    }
}
