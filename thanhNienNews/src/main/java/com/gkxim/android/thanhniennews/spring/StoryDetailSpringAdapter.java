/**
 * @author Nam.Nguyen
 * @Date:Jan 14, 2014
 */
package com.gkxim.android.thanhniennews.spring;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebSettings.TextSize;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.GKIMLog;
import com.gkxim.android.utils.UIUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

import java.net.URL;
import java.util.ArrayList;

/**
 * @author Nam.Nguyen
 * 
 */
public class StoryDetailSpringAdapter extends BaseAdapter {
	private static final String CONST_STR_HTML_WRAP = "<html><head><meta content=\"text/html; charset=UTF-8\" /><meta name=\"viewport\" content=\"width=device-width,user-scalable=yes\"/><link href=\"style.css\" rel=\"stylesheet\" type=\"text/css\" /><script type=\"text/javascript\" src=\"script.js\"></script></head><body>%1s</body></html>";
	private static final String CONST_STR_JSCRIP_COLOR = "javascript:changetextmode('%1s', '%2s')";

	private static final String TAG = StoryDetailSpringAdapter.class
			.getSimpleName();

	public class ViewHolder {
		public ImageView icon;
		public WebView wv;
		public ImageView spimage;
		public TextView date;
		public TextView content;
	}

	private Context mContext;
	private ArrayList<CSpringData> mListData;
	private WebView mWvContent;
	private CustomWebViewClient mWebViewClient = new CustomWebViewClient();
	private int mTextZoomSize = 100; // the fixed value is 100 = 100% zoom size
	private int mTextZoomForPhone = 0;
	private boolean mTabletVersion;
	private Typeface mDefaultTF;

	public static final int TEXT_SIZE_LARGER = 24;
	public static final int TEXT_SIZE_LARGEST = 32;
	public static final int TEXT_SIZE_NORMAL = 16;
	public static final int TEXT_SIZE_SMALLER = 12;
	public static final int TEXT_SIZE_SMALLEST = 8;
	public static final int TEXT_SIZE_SUBSTRACT_DATE = 8;

	private ImageLoadingListener mImageLoadListener = new ImageLoadingListener() {

		@Override
		public void onLoadingStarted(String arg0, View arg1) {
			GKIMLog.lf(mContext, 1, TAG + "=>onLoadingStarted");
			arg1.setVisibility(View.INVISIBLE);
		}

		@Override
		public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
			GKIMLog.lf(mContext, 1, TAG + "=>onLoadingFailed");
		}

		@Override
		public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
			GKIMLog.lf(mContext, 1, TAG + "=>onLoadingComplete");
			arg1.setVisibility(View.VISIBLE);
		}

		@Override
		public void onLoadingCancelled(String arg0, View arg1) {
			GKIMLog.lf(mContext, 1, TAG + "=>onLoadingCancelled");
		}
	};

	@SuppressLint("NewApi")
	public StoryDetailSpringAdapter(Context cxt, ArrayList<CSpringData> lst) {
		this.mContext = cxt;
		this.mListData = lst;

		// int confLayout = cxt.getResources().getConfiguration().screenLayout
		// & Configuration.SCREENLAYOUT_SIZE_MASK;
		// && confLayout == Configuration.SCREENLAYOUT_SIZE_XLARGE
		// && density == DisplayMetrics.DENSITY_MEDIUM
		// int density = cxt.getResources().getDisplayMetrics().densityDpi;
		mTabletVersion = (mContext.getResources().getBoolean(R.bool.istablet));
		mDefaultTF = TNPreferenceManager.getTNTypeface();
	}

	@Override
	public int getCount() {
		return mListData.size();
	}

	@Override
	public CSpringData getItem(int position) {
		return mListData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		GKIMLog.l(1, TAG + "=> getView:" + position);
		return getCustomDropDownView(position, convertView, parent);
	}

	public View getCustomDropDownView(int position, View convertView,
			ViewGroup parent) {
		GKIMLog.l(1, TAG + " getCustomDropDownView:" + position);
		LayoutInflater inflater = LayoutInflater.from(mContext);

		ViewHolder vh = null;
		if (convertView == null) {
			vh = new ViewHolder();
			convertView = inflater.inflate(R.layout.item_spring, parent, false);
			vh.icon = (ImageView) convertView
					.findViewById(R.id.imgb_spring_icon);
			vh.wv = (WebView) convertView.findViewById(R.id.wv_spring_content);
			vh.content = (TextView) convertView
					.findViewById(R.id.txt_spring_content);
			if (!mTabletVersion) {
				vh.wv.setVisibility(View.VISIBLE);
				vh.content.setVisibility(View.GONE);
				vh.wv.setWebViewClient(mWebViewClient);
				vh.wv.getSettings().setJavaScriptEnabled(true);
			} else {
				vh.wv.setVisibility(View.GONE);
				vh.content.setVisibility(View.VISIBLE);
			}
			vh.spimage = (ImageView) convertView
					.findViewById(R.id.imgb_spring_bottom);
			vh.date = (TextView) convertView.findViewById(R.id.txt_spring_date);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}

		CSpringData item = getItem(position);

		GKIMLog.l(1, TAG + "=>item " + position + ":" + item.getName());
		if (TNPreferenceManager.isNightMode()) {
			vh.date.setTextColor(Color.WHITE);
			vh.content.setTextColor(Color.WHITE);
		} else {
			vh.date.setTextColor(mContext.getResources().getColor(R.color.spring_date));
			vh.content.setTextColor(Color.BLACK);
		}
		vh.date.setText(" " + item.getName() + " - " + item.getComment_date());
		setStoryContentWebView(vh, item.getComment());
		resizeFontWebView(vh);
		ImageLoader.getInstance().displayImage(item.getIcon_url(), vh.icon,
				mImageLoadListener);
		ImageLoader.getInstance().displayImage(item.getSep_url(), vh.spimage,
				mImageLoadListener);

		return convertView;

	}

	protected void setStoryContentWebView(ViewHolder vh, String htmlcontent) {
		if (vh != null) {
			int color = (TNPreferenceManager.isNightMode() ? R.color.storydetail_background_black
					: R.color.storydetail_background_white);
			if (!mTabletVersion && vh.wv != null) {
				vh.wv.setBackgroundColor(mContext.getResources()
						.getColor(color));
				vh.wv.getSettings().setPluginState(WebSettings.PluginState.ON);
				String htmlful = String
						.format(CONST_STR_HTML_WRAP, htmlcontent);
				vh.wv.loadDataWithBaseURL("file:///android_asset/", htmlful,
						"text/html", "utf-8", null);
				vh.wv.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
			} else if (vh.content != null) {
				vh.content.setBackgroundColor(mContext.getResources().getColor(
						color));
				vh.content.setText(Html.fromHtml(htmlcontent));
				vh.content.setTypeface(mDefaultTF, Typeface.NORMAL);
			}
		}
	}

	private int getTextSizeFromTextZoom(int textZoom) {

		int TextZoomMax = TNPreferenceManager.WEBVIEW_TEXTZOOM_MAX
				+ mTextZoomForPhone;
		int TextZoomMin = TNPreferenceManager.WEBVIEW_TEXTZOOM_MIN
				- mTextZoomForPhone;
		if (mTextZoomSize < 100) {
			if (mTextZoomSize <= TextZoomMin) {
				return TEXT_SIZE_SMALLEST;
			} else {
				return TEXT_SIZE_SMALLER;
			}
		} else if (textZoom == 100) {
			return TEXT_SIZE_NORMAL;
		} else {
			if (mTextZoomSize >= TextZoomMax) {
				return TEXT_SIZE_LARGEST;
			} else {
				return TEXT_SIZE_LARGER;
			}
		}
	}

	@SuppressLint("NewApi")
	public boolean resizeFontWebView(ViewHolder vh) {
		int textZoom = TNPreferenceManager.getTextSizeMode();
		if (textZoom >= 0) {
			mTextZoomSize = textZoom;
			int TextZoomMax = TNPreferenceManager.WEBVIEW_TEXTZOOM_MAX
					+ mTextZoomForPhone;
			int TextZoomMin = TNPreferenceManager.WEBVIEW_TEXTZOOM_MIN
					- mTextZoomForPhone;
			if (!mTabletVersion && vh != null) {
				vh.wv.getSettings().setJavaScriptEnabled(true);
				if (UIUtils.hasICS()) {
					vh.wv.getSettings().setTextZoom(mTextZoomSize);
				} else {
					TextSize textsize = TextSize.NORMAL;
					if (mTextZoomSize < 100) {
						if (mTextZoomSize <= TextZoomMin) {
							textsize = TextSize.SMALLEST;
						} else {
							textsize = TextSize.SMALLER;
						}
					} else {
						if (mTextZoomSize >= TextZoomMax) {
							textsize = TextSize.LARGEST;
						} else {
							textsize = TextSize.LARGER;
						}
					}
					vh.wv.getSettings().setTextSize(textsize);
                    vh.date.setTextSize(getTextSizeFromTextZoom(mTextZoomSize) - TEXT_SIZE_SUBSTRACT_DATE);
				}


			} else if (vh != null) {
				vh.content.setTextSize(getTextSizeFromTextZoom(mTextZoomSize));
				vh.date.setTextSize(getTextSizeFromTextZoom(mTextZoomSize));

			}

			return true;
		}
		return false;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.BaseAdapter#notifyDataSetChanged()
	 */
	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}

	private class CustomWebViewClient extends WebViewClient {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.webkit.WebViewClient#onPageFinished(android.webkit.WebView,
		 * java.lang.String)
		 */
		@Override
		public void onPageFinished(WebView view, String url) {
			GKIMLog.lf(null, 0, TAG + "=>onPageFinished: " + url);
			String idbg = "";
			String idtxt = "";
			if (TNPreferenceManager.isNightMode()) {
				idbg = mContext.getResources().getString(
						R.string.storydetail_background_black);
				idtxt = mContext.getResources().getString(
						R.string.storydetail_text_white);
			} else {
				idbg = mContext.getResources().getString(
						R.string.storydetail_background_white);
				idtxt = mContext.getResources().getString(
						R.string.storydetail_text_black);
			}
			view.loadUrl(String.format(CONST_STR_JSCRIP_COLOR, idbg, idtxt));
			super.onPageFinished(view, url);
		}

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
	}
}
