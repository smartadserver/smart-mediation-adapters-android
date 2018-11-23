package com.smartadserver.android.library.mediation.adincube;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.adincube.sdk.AdChoicesView;
import com.adincube.sdk.AdinCube;
import com.adincube.sdk.AdinCubeNativeEventListener;
import com.adincube.sdk.NativeAd;
import com.smartadserver.android.library.mediation.SASMediationNativeAdAdapter;
import com.smartadserver.android.library.mediation.SASMediationNativeAdContent;
import com.smartadserver.android.library.mediation.SASMediationNativeAdAdapterListener;
import com.smartadserver.android.library.model.SASNativeVideoAdElement;
import com.smartadserver.android.library.ui.SASAdChoicesView;
import com.smartadserver.android.library.util.SASUtil;

import java.util.List;
import java.util.Map;

/**
 * Mediation adapter class for AdinCube native ad format
 */
public class SASAdinCubeNativeAdAdapter extends SASAdinCubeAdapterBase implements SASMediationNativeAdAdapter {

    private static final String TAG = SASAdinCubeNativeAdAdapter.class.getSimpleName();

    private View.OnClickListener onClickListener;
    private View[] registeredClickableViews;
    private ViewGroup proxyView;
    private AdChoicesView adincubeAdChoicesView;

    /**
     * @param context                the {@link android.content.Context} needed by the mediation SDK to make the ad request
     * @param serverParametersString a String containing all needed parameters (as returned by Smart ad delivery)
     *                               to make the mediation call
     * @param clientParameters       additional client-side parameters (user specific, like location)
     * @param nativeAdAdapterListener  the {@link SASMediationNativeAdAdapterListener} provided to this {@link com.smartadserver.android.library.mediation.SASMediationAdapter}
     */
    @Override
    public void requestNativeAd(@NonNull Context context, @NonNull String serverParametersString, @NonNull Map<String,String> clientParameters,
                                @NonNull final SASMediationNativeAdAdapterListener nativeAdAdapterListener) {
        SASUtil.logDebug(TAG, "SASAdinCubeNativeAdAdapter requestNativeAd");

        configureAdRequest(context, serverParametersString, clientParameters);

        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nativeAdAdapterListener.onAdClicked();
                proxyView.performClick();
            }
        };

        AdinCubeNativeEventListener nativeEventListener = new AdinCubeNativeEventListener() {
            @Override
            public void onAdLoaded(List<NativeAd> list) {
                SASUtil.logDebug(TAG, "onAdLoaded");

                final NativeAd nativeAd = list.get(0);

                SASMediationNativeAdContent nativeAdContent = new SASMediationNativeAdContent() {
                    @NonNull
                    @Override
                    public String getTitle() {
                        return nativeAd.getTitle();
                    }

                    @NonNull
                    @Override
                    public String getSubTitle() {
                        return nativeAd.getDescription();
                    }

                    @NonNull
                    @Override
                    public String getBody() {
                        return "";
                    }

                    @NonNull
                    @Override
                    public String getIconUrl() {
                        return nativeAd.getIcon() != null ? nativeAd.getIcon().getUrl() : "";
                    }

                    @Override
                    public int getIconWidth() {
                        if (nativeAd.getIcon() != null) {
                            Integer widthInteger = nativeAd.getIcon().getWidth();
                            if (widthInteger != null) {
                                return widthInteger;
                            }
                        }

                        return -1;
                    }

                    @Override
                    public int getIconHeight() {
                        if (nativeAd.getIcon() != null) {
                            Integer heightInteger = nativeAd.getIcon().getHeight();
                            if (heightInteger != null) {
                                return heightInteger;
                            }
                        }

                        return -1;
                    }

                    @NonNull
                    @Override
                    public String getCoverImageUrl() {
                        return nativeAd.getCover() != null ? nativeAd.getCover().getUrl() : "";
                    }

                    @Override
                    public int getCoverImageWidth() {
                        if (nativeAd.getIcon() != null) {
                            Integer widthInteger = nativeAd.getCover().getWidth();
                            if (widthInteger != null) {
                                return widthInteger;
                            }
                        }

                        return -1;
                    }

                    @Override
                    public int getCoverImageHeight() {
                        if (nativeAd.getIcon() != null) {
                            Integer heightInteger = nativeAd.getCover().getHeight();
                            if (heightInteger != null) {
                                return heightInteger;
                            }
                        }

                        return -1;
                    }

                    @Override
                    public float getRating() {
                        return nativeAd.getRating();
                    }

                    @NonNull
                    @Override
                    public String getCallToAction() {
                        return nativeAd.getCallToAction();
                    }

                    @NonNull
                    @Override
                    public String getSponsoredMessage() {
                        return "";
                    }

                    @Nullable
                    @Override
                    public SASNativeVideoAdElement getMediaElement() {
                        return null;
                    }

                    @Nullable
                    @Override
                    public View getMediaView(@NonNull Context context) {
                        return null;
                    }

                    @Override
                    public void unregisterView(@NonNull View v) {
                        if (registeredClickableViews != null) {
                            // clean all installed listeners on clickable views
                            for (View clickableView : registeredClickableViews) {
                                clickableView.setOnClickListener(null);
                                // this is MANDATORY as the view will continue to intercept clicks although its clicklistener is null
                                clickableView.setClickable(false);
                            }
                        }

                        // remove adChoicesView from parent
                        ViewParent adChoicesParent = adincubeAdChoicesView.getParent();
                        if (adChoicesParent instanceof ViewGroup) {
                            ((ViewGroup) adChoicesParent).removeView(adincubeAdChoicesView);
                        }
                    }

                    @Override
                    public void registerView(@NonNull View v, @Nullable View[] clickableViews) {
                        // add click listeners on clickable views
                        if (clickableViews != null) {
                            registeredClickableViews = clickableViews;
                            for (View clickableView : registeredClickableViews) {
                                clickableView.setOnClickListener(onClickListener);
                            }
                        }

                        // setup proxyView to receive forwarded clicks
                        if (proxyView == null) {
                            proxyView = new RelativeLayout(v.getContext());
                            AdinCube.Native.link(proxyView, nativeAd);
                        }

                        SASAdChoicesView adChoicesView = (SASAdChoicesView) SASUtil.findSubViewOfClass(v, SASAdChoicesView.class);
                        if (adChoicesView != null) {
                            // configure adChoicesView
                            adincubeAdChoicesView = new AdChoicesView(v.getContext()) {
                                @Override
                                public void setVisibility(int i) {
                                    super.setVisibility(i);
                                    SASUtil.logDebug(TAG, "AdInCube AdChoicesView setVisibility:" + i);
                                }
                            };

                            adincubeAdChoicesView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                            adChoicesView.setDelegateAdChoiceView(adincubeAdChoicesView);
                            adincubeAdChoicesView.setNativeAd(nativeAd);
                        }
                    }

                    @NonNull
                    @Override
                    public String getAdChoicesUrl() {
                        return "";
                    }
                };

                nativeAdAdapterListener.onNativeAdLoaded(nativeAdContent);
            }

            @Override
            public void onLoadError(String s) {
                SASUtil.logDebug(TAG, "onLoadError");
                nativeAdAdapterListener.adRequestFailed(s, true);
            }

            @Override
            public void onAdClicked(NativeAd nativeAd) {
                SASUtil.logDebug(TAG, "onAdClicked");
                nativeAdAdapterListener.onAdClicked();
            }
        };

        // Disable caching for both cover and icon
        AdinCube.Native.disableImageCaching(NativeAd.Image.Type.COVER);
        AdinCube.Native.disableImageCaching(NativeAd.Image.Type.ICON);

        AdinCube.Native.load(context, nativeEventListener);
    }

    @Override
    public void onDestroy() {
        onClickListener = null;
        registeredClickableViews = null;
        proxyView = null;
        adincubeAdChoicesView = null;
    }
}
