package com.smartadserver.android.library.mediation.mopub;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.mopub.common.privacy.ConsentDialogListener;
import com.mopub.common.privacy.PersonalInfoManager;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.smartadserver.android.library.mediation.SASMediationInterstitialAdapter;
import com.smartadserver.android.library.mediation.SASMediationInterstitialAdapterListener;
import com.smartadserver.android.library.util.SASUtil;

import java.util.ArrayList;
import java.util.Map;

/**
 * Mediation adapter class for MoPub interstitial format
 */
public class SASMoPubInterstitialAdapter implements SASMediationInterstitialAdapter {

    // tag for logging purposes
    static private final String TAG = SASMoPubInterstitialAdapter.class.getSimpleName();

    private static boolean initMoPubDone = false;

    // MoPub interstitial instance
    private MoPubInterstitial moPubInterstitial;

    // GDPR related
    private boolean needToShowConsentDialog = false;

    /**
     * @param context                     the {@link android.content.Context} needed by the mediation SDK to make the ad request
     * @param serverParametersString      a String containing all needed parameters (as returned by Smart ad delivery)
     *                                    to make the mediation ad call
     * @param clientParameters            additional client-side parameters (user specific, like location)
     * @param interstitialAdapterListener the {@link SASMediationInterstitialAdapterListener} provided to
     *                                    this {@link com.smartadserver.android.library.mediation.SASMediationAdapter} to notify Smart SDK of events occurring
     */
    @Override
    public void requestInterstitialAd(@NonNull final Context context, @NonNull final String serverParametersString, @NonNull final Map clientParameters, @NonNull final SASMediationInterstitialAdapterListener interstitialAdapterListener) {
        SASUtil.logDebug(TAG, "SASMoPubInterstitialAdapter adRequest");

        // To request an interstitial using MoPub, the context have to be an Activity.
        if (!(context instanceof Activity)) {
            interstitialAdapterListener.adRequestFailed("Can not get a MoPub interstitial because its creation context is not an Activity", false);
            return;
        }

        // Init MoPub SDK -- Here serverParameterString is the MoPub Ad unit id
        if (!initMoPubDone) {
            SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder(serverParametersString)
                    .build();

            SdkInitializationListener initializationListener = new SdkInitializationListener() {
                @Override
                public void onInitializationFinished() {
                    SASUtil.logDebug(TAG, "MoPub onInitializationFinished");
                    initMoPubDone = true;
                    // call requestBannerAd again, with SDK initialized
                    requestInterstitialAd(context,serverParametersString,clientParameters,interstitialAdapterListener);
                }
            };

            MoPub.initializeSdk(context, sdkConfiguration, initializationListener);
        } else {
            final PersonalInfoManager personalInfoManager = MoPub.getPersonalInformationManager();
            // store that the consent dialog need to be shown. We will show it AFTER the interstitial display to avoid having the interstitial above the consent dialog.
            needToShowConsentDialog = personalInfoManager != null && personalInfoManager.shouldShowConsentDialog();
            if (needToShowConsentDialog) {
                personalInfoManager.loadConsentDialog(new ConsentDialogListener() {
                    @Override
                    public void onConsentDialogLoaded() {
                        // ok
                    }

                    @Override
                    public void onConsentDialogLoadFailed(@NonNull MoPubErrorCode moPubErrorCode) {
                        SASUtil.logDebug(TAG, "MoPub onConsentDialogLoadFailed");
                    }
                });
            }

            // Instantiate Interstitial Ad Listener
            MoPubInterstitial.InterstitialAdListener interstitialAdListener = new MoPubInterstitial.InterstitialAdListener() {
                @Override
                public void onInterstitialLoaded(MoPubInterstitial interstitial) {
                    SASUtil.logDebug(TAG, "InterstitialAdListener onInterstitialLoaded");
                    interstitialAdapterListener.onInterstitialLoaded();
                }

                @Override
                public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
                    SASUtil.logDebug(TAG, "InterstitialAdListener onInterstitialFailed");

                    // check if this is due to a No Ad
                    boolean isNoAd = errorCode == MoPubErrorCode.NO_FILL || errorCode == MoPubErrorCode.NETWORK_NO_FILL;
                    interstitialAdapterListener.adRequestFailed(errorCode.toString(), isNoAd);
                }

                @Override
                public void onInterstitialShown(MoPubInterstitial interstitial) {
                    SASUtil.logDebug(TAG, "InterstitialAdListener onInterstitialShown");
                    interstitialAdapterListener.onInterstitialShown();

                    if (needToShowConsentDialog && personalInfoManager != null) {
                        personalInfoManager.showConsentDialog();
                    }
                }

                @Override
                public void onInterstitialClicked(MoPubInterstitial interstitial) {
                    SASUtil.logDebug(TAG, "InterstitialAdListener onInterstitialClicked");
                    interstitialAdapterListener.onAdClicked();
                }

                @Override
                public void onInterstitialDismissed(MoPubInterstitial interstitial) {
                    SASUtil.logDebug(TAG, "InterstitialAdListener onInterstitialDismissed");
                    interstitialAdapterListener.onAdClosed();
                    moPubInterstitial.destroy();
                }
            };

            // Instantiate the MoPub Interstitial
            moPubInterstitial = new MoPubInterstitial((Activity) context, serverParametersString);
            moPubInterstitial.setInterstitialAdListener(interstitialAdListener);

            moPubInterstitial.load();
        }
    }

    @Override
    public void showInterstitial() throws Exception {
        if (moPubInterstitial != null && moPubInterstitial.isReady()) {
            moPubInterstitial.show();
        }
    }

    @Override
    public void onDestroy() {
        SASUtil.logDebug(TAG, "MoPub onDestroy for interstitial");
        if (moPubInterstitial != null) {
            moPubInterstitial.destroy();
            moPubInterstitial = null;
        }
    }
}
