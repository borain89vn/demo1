/**
 * 
 */
package com.gkxim.android.thanhniennews;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;
import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.GKIMLog;
import com.gkxim.android.utils.UIUtils;

import java.net.URL;

/**
 * @author Timon
 * 
 */
public class EventFullWebViewActivity extends Activity {
	private static final String TAG = EventFullWebViewActivity.class
			.getSimpleName();
	public static final boolean DEBUG = GKIMLog.DEBUG_ON;
	private String mUrl = null;
	private ImageButton mImbBack;
	private WebView mWebView;
	private View.OnClickListener mDefaultOnClickListener = getDefaultOnClickListener();
	private CustomWebViewClient mWebViewClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GKIMLog.lf(this, 1, TAG + "=>onCreate");
		setContentView(R.layout.activity_event_webviewfull);
		Intent intent = getIntent();
		if (intent != null) {
			Bundle extra = intent.getExtras();
			if (extra != null && extra.containsKey(TNPreferenceManager.EXTRAKEY_EVENTWEBVIEW_URL)) {
				mUrl = extra.getString(TNPreferenceManager.EXTRAKEY_EVENTWEBVIEW_URL);
			}
		}
		if (mUrl == null) {
			GKIMLog.lf(this, 2, TAG + "=> there has no Event's url to open.");
			finish();
		}
		initLayout();
		boolean mTabletVersion = UIUtils.isTablet(this);
		Log.d("initGUIHeader", "mTabletVersion:" + mTabletVersion);
        //Spring 2015
        if(TNPreferenceManager.SECTION_SPRING) {
            if (!mTabletVersion) {
                ImageView imgv = (ImageView) findViewById(R.id.imgv_horse_phone);
                imgv.setVisibility(View.VISIBLE);
            } else {
                ImageView imgv1 = (ImageView) findViewById(R.id.imgv_horse_tablet);
                imgv1.setVisibility(View.VISIBLE);
            }
        }
	}

	/**
	 * 
	 */
	private void initLayout() {
		ImageView img_logo=(ImageView) findViewById(R.id.header_iv_logo);
        if (img_logo != null) {
            img_logo.setOnClickListener(mDefaultOnClickListener);
        }

		mImbBack = (ImageButton) findViewById(R.id.imb_storydetail_back);
		
		mWebView = (WebView) findViewById(R.id.wv_event_webviewfull);
		if (mWebView != null) {
			WebSettings ws = mWebView.getSettings();
//			ws.setDefaultFontSize(getResources().getDimensionPixelSize(
//					R.dimen.storydetail_content_textsize));
			ws.setJavaScriptEnabled(true);
//			ws.setSupportZoom(false);
			// FIXME: give a general value for web text size.
			// mWvContent.getSettings().setTextZoom(mTextZoomSize);
//			if (!mTabletVersion) {
//				mTextZoomForPhone = 20;
//				if (!resizeFontWebView()) {
//					if (!UIUtils.hasICS()) {
//						TextSize textsize = TextSize.NORMAL;
//						mWvContent.getSettings().setTextSize(textsize);
//					} else {
//						mTextZoomSize += TNPreferenceManager.WEBVIEW_TEXTZOOM_STEP;
//						mWvContent.getSettings().setTextZoom(mTextZoomSize);
//					}
//				}
//			} else {
//				mTextZoomForPhone = 0;
//				resizeFontWebView();
//			}
//			ws.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
			if (mWebViewClient == null) {
				mWebViewClient = new CustomWebViewClient();
			}
			mWebView.setWebViewClient(mWebViewClient);
		}

	}

	@Override
	protected void onStart() {
		GKIMLog.lf(this, 1, TAG + "=>onStart");
		super.onStart();
	}

	@Override
	protected void onResume() {
		GKIMLog.lf(this, 1, TAG + "=>onResume");
		if (mImbBack != null) {
			mImbBack.setOnClickListener(mDefaultOnClickListener);
		}
		if (mUrl != null && mWebView != null) {
			mWebView.loadUrl(mUrl);
		}
		super.onResume();
	}

	@Override
	protected void onStop() {
		GKIMLog.lf(this, 1, TAG + "=>onStop");
		if (mImbBack != null) {
			mImbBack.setOnClickListener(null);
		}
		super.onStop();
	}

	public void onClick(View view) {
		GKIMLog.lf(this, 1, TAG + "=>onClick: " + view);
	}

	private View.OnClickListener getDefaultOnClickListener() {
		return (new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				GKIMLog.lf(EventFullWebViewActivity.this, 1, TAG
						+ "=>onClick: " + v);
				switch (v.getId()) {
				case R.id.header_iv_logo:
					EventFullWebViewActivity.this.finish();
					break;
				case R.id.imb_storydetail_back:
					finish();
					break;
				case R.id.pager:
					// toggleCaption();
					break;
				default:
					break;
				}
			}
		});
	}
	
	private class CustomWebViewClient extends WebViewClient {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.webkit.WebViewClient#shouldOverrideUrlLoading(android.webkit
		 * .WebView, java.lang.String)
		 */
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			try {
				URL urlObj = new URL(url);
				if (!TextUtils.equals(urlObj.getHost(),
						"file:///android_asset/")) {
					GKIMLog.lf(null, 0, TAG
							+ "=>shouldOverrideUrlLoading, open by browser: "
							+ url);
					return true;
				}
			} catch (Exception e) {
				GKIMLog.lf(
						null,
						0,
						TAG + "=>shouldOverrideUrlLoading exception: "
								+ e.getMessage());
			}
			return super.shouldOverrideUrlLoading(view, url);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			GKIMLog.lf(null, 0, TAG + "=>onPageFinished: " + url);
			super.onPageFinished(view, url);
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			GKIMLog.lf(null, 0, TAG + "=>onReceivedError: " + errorCode
					+ " from: " + failingUrl);
			super.onReceivedError(view, errorCode, description, failingUrl);
		}

	}
}
