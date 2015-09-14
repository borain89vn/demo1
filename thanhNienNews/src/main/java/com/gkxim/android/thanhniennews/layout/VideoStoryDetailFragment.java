/**
 * 
 */
package com.gkxim.android.thanhniennews.layout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.TextSize;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsListView.LayoutParams;
import android.widget.*;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.StoryDetailImageReviewActivity;
import com.gkxim.android.thanhniennews.VideoSectionActivity;
import com.gkxim.android.thanhniennews.VideoStoryDetailFragmentActivity;
import com.gkxim.android.thanhniennews.models.*;
import com.gkxim.android.thanhniennews.networking.RequestDataFactory;
import com.gkxim.android.thanhniennews.social.SocialHelper;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.DataDownloader;
import com.gkxim.android.utils.DataDownloader.OnDownloadCompletedListener;
import com.gkxim.android.utils.GKIMLog;
import com.gkxim.android.utils.RequestData;
import com.gkxim.android.utils.UIUtils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Timon Replace for StoryDetailActivity.
 */
@SuppressWarnings("deprecation")
public class VideoStoryDetailFragment extends Fragment {

	private static final String TAG = "StoryDetailFragment";
	protected static final boolean DEBUG = GKIMLog.DEBUG_ON;
	private static final String ARG_STORY_ID = "arg_story_id";
	private static final String CONST_STR_HTML_WRAP = "<html><head><meta content=\"text/html; charset=UTF-8\" /><meta name=\"viewport\" content=\"width=device-width,user-scalable=yes\"/><link href=\"style.css\" rel=\"stylesheet\" type=\"text/css\" /><script type=\"text/javascript\" src=\"script.js\"></script></head><body>%1s</body></html>";
	private static final String CONST_STR_HTML_UL_WRAP = "<ul class=\"nonstyle\">%1s</ul>";
	private static final String CONST_STR_HTML_LI_WRAP = "<li class=\"pdfline\"><a href=\"%1s\"><img class=\"pdftext\" src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAABTJJREFUeNq0V2tMXEUU/ubOvfsGGltUahCwKhhDICgKqIml1jYkxCDVEI0/jP5utWlif2p8xUQrvhq1QWtiQ6VQWiA+fvkgsSRoxVgTbNKEkCZQlmeVbQu7O54z9+6yu91dWEInmb1353HON+fxnblCKYVYE0KYfX19x+nZig1o4XC4u6WlpY1eI+nmtW7+iXVqef39/Wqj2sTEhOru7j5BcmUmAGbKmDv2cnbkTxiGsfoxhWBJ8b/RaBQ11VXo6elBa2srGhoa9tD7D+3t7U2Dg4NLqdtTAcQ1WpYLUhpZFSvThLFwGSrgI802iEgkqp9SSszNzaOoqAj19fU7TNP8hwDcTVPLaRWmNoOUswUydeGyUPBRB/K+6oIwrZW5BNAerwfB4LQGUVtbW9rb23uez7YmACYJk9k6nVBYJsKNDyetNR23sX+9Hg8CeYG4Jerq6kopyC8kgjAzW0BmjQGl5yV8R77G5c/fg4jFAccEtbKyMtCJObMc10R0TND/YvpLPsNCVgDSMWmWIEC0tBjm0FlIk6yxbK81SOH0zAyqq6t1z9Dca7DAKgDY12W3Q9DDIFeIpZW1i6EQ/ltcTFoeJQvdUVKC1bIgQX52F4ACzzzzG+S5UViTQaibNpGdoxmXc3qmPWhGC5BZtRXSdZ7zumGd+h7LX7bDfeA1GJSKq+3JCYBJFpCZussNs/MUVNU9wK5GKHKFdfAtyPx8SK8X0sPdA6m5xNR7WF5aPRldQKhjEXwd8/m9EK8fgvrlNCSb9uO3Ie7bCaPpGajtDU4hIPp/6AGgrka/J9acNQHQZJMOABEQjp2EuqsM4uQARHCGEupf4LmngG0lEEe/gfr5V+DJZuDDDohHHtQsmTMANlsSAH4n5WpyCtH3P4N84yBE3f1AfiBWWajTc/cOUApA/fEXRx7xg+CUWocFEl3A2eBxI/Lqu1B/j0K+8CyZmxRdW+Kam7xxiUzvdgENtbHw1ymbCYCRjYji3e1G+Pl9MKrvhZqehbX3RcjlMCQBTEvTPE4KdU9Yk5sLTDMxIMik5xAZuwj/d51QkbBtlQ1oWak44eYA/8AxyOKt9Bq1CedGAYj6vPaEmTJFyu1gNJgk1q0wJj8jgPJmug7W1mJ0eJh0ZVHEQZUuTTMtp2CsILnlJDcrAOWjSun3w1NYaNcCKqPq6lUtQGeFZUHwXYAYLzI762QoreOC5HLpqE9aT0woKCt0JSC5Wn5oYZUYoNO5KPIZgFoMoejSxfhU6HgX5ve+jK3jF5K2hLq6sXDgFb03cX1kfBxT2x/X1zekScWMNmYAFp3IcmJh6ubbMLe7Gb62p+GtrNRj83vaECy5EzOVNbDoArL5iyMwnao3dWsxgtsqMPdYE1x0apaXUxbwBjahWrbvkJ5AHsSlSXvulkL7WUDFZ8sWTbVLnxyG/2gHXNIW6aGCJKggscu4q1zT0M2I2YcOAN87b8J8gvidbjvy9xF7M1c6x7TCdNn7WCm1/LHzdk060YPIoQ+4uuUAgARajqAY1coff4I6MwQ1NAzXpgL76l5RTr61dD0Q+18CBr6FK2DXhmgVUTGZ3qDTGxx4Kd8Pa6qGDiHYkT5CxYVyWAT8wJUr9tj+fYgnYmcXcPhTuqjYeW7k5wFe7zqYkJEm5j+jb9xFH22BlXEee3SnzYjC2cNAKc30KXk9r0nlEf6fwh3XAaD6FiQSKoRS2PBGyll+0lDK1zE7t5T6ZgBi4xHoZKAbDMb4u4B1pwKwnI8GN25cu8a8xd+IrPt/AQYAntHxL2P1ZVwAAAAASUVORK5CYII=\"/></a><a class=\"pdflink\" href=\"%2s\" target=\"_blank\">%3s</a></li>";
	private static final String CONST_STR_JSCRIP_COLOR = "javascript:changetextmode('%1s', '%2s')";

	private String mTAG;
	public String mStoryId;
	private String mStoryType;
	private StoryDetail mStoryDetail = null;
	protected AlertDialog mProcessingDialog;
	private ImageButton imgBtCheck;
	// view components
	private Typeface mDefaultTF;
	private ViewGroup mRootLayout;
	// private OverrideScrollView mRootLayout;
	private TextView mTvCategory;
	private TextView mTvTitle;
	private TextView mTvAuthor;
	private TextView mTvDate;
	private TextView mTvNumber;
	public ImageView mIvTopImage;
	private TextView mTvImageCount;
	private TextView mTvVideoCount;
	private FrameLayout mFlImage;
	private FrameLayout mFlVideo;
	private Button mBtImage;
	private Button mBtVideo;
	private LinearLayout mLlTopImageCount;
	private TextView mTvFbLike;
	private WebView mWvContent;
	private ExpandableListView mELVComments;
	private CommentExpandableListAdapter mELVAdapter;
	private PullToRefreshScrollView mPullRefreshScrollView;
	private ScrollView mScrollView;

	public int mCommentCount;
	protected int mSectionColor;
	private String mStoryTitle;
	protected boolean mStoryChecked;
	protected boolean mFavoriteChaned;
	private int mTextZoomSize = 100; // the fixed value is 100 = 100% zoom size
	private StringBuilder mShareContentBuilder = new StringBuilder();

	private OnClickListener mDefaultOnClickListener = getDefaultOnClickListener();
	private DataDownloader mTNDownloader = getDataloader();
	private ExpandableListView.OnGroupExpandListener mCommentExpandListener = getOnGroupExpandListener();
	private ExpandableListView.OnGroupCollapseListener mCommentCollapseListener = getOnGroupCollapseListener();
	private Intent mImageReviewIntent;
	private boolean mTabletVersion = false;
	private CustomWebViewClient mWebViewClient = null;
	private int mFbLikeCount = 0;
	private ProgressBar mProgressbar;
	private int mTextZoomForPhone = 0;
	private int mColorTitle, mColorAuthur, mColorComment, mColorBackground;

	private Context mContext;
	private String mNoConnection;
	private String mTopImageCount;
	private String mTopVideoCount;

	private AdView adView;

	/**
	 * Factory method for this fragment class. Constructs a new fragment for the
	 * given story id.
	 */
	public static VideoStoryDetailFragment create(String storyId) {
		VideoStoryDetailFragment fragment = new VideoStoryDetailFragment();
		Bundle args = new Bundle();
		args.putString(ARG_STORY_ID, storyId);
		fragment.setArguments(args);
		return fragment;
	}

	public VideoStoryDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mColorTitle = getResources().getColor(
				R.color.story_comment_title_mode_night);
		mColorComment = getResources().getColor(
				R.color.story_comment_content_mode_night);
		mColorAuthur = getResources().getColor(
				R.color.story_comment_name_mode_night);
		mColorBackground = getResources().getColor(
				R.color.story_comment_background_mode_night);
		mStoryId = getArguments().getString(ARG_STORY_ID);
		mTAG = TAG + "(" + mStoryId + ")";
		GKIMLog.lf(null, 1, mTAG + "=>onCreate: " + mStoryId + ".");
		mProcessingDialog = new GUISimpleLoadingDialog(getActivity());
		mProcessingDialog.setMessage(getResources().getString(
				R.string.please_wait));
		mProcessingDialog.setCancelable(false);
		mTabletVersion = (getResources().getBoolean(R.bool.istablet));

		mContext = getActivity().getApplicationContext();
		mNoConnection = getResources().getString(
				R.string.storydetail_no_connection);
		mTopImageCount = getResources().getString(
				R.string.storydetail_topimage_image_count);
		mTopVideoCount = getResources().getString(
				R.string.storydetail_topimage_video_count);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout
		GKIMLog.lf(null, 1, mTAG + "=>onCreateView");
		View result = initializeView(inflater, mStoryId);

		return result;
	}

	@Override
	public void onAttach(Activity activity) {
		GKIMLog.lf(null, 1, mTAG + "=>onAttach: " + mStoryId + ".");
		super.onAttach(activity);
	}

	@Override
	public void onResume() {
		if (mTNDownloader == null) {
			mTNDownloader = getDataloader();
		}
		GKIMLog.lf(null, 1, mTAG + "=>onResume: " + mStoryId + ".");
		GKIMLog.l(4, "mStoryId : " + mStoryId + " pageid : " + getStoryId());
		if (mStoryDetail == null) {
			loadStory(mStoryId);
			setLoadingStory(true);
		} else {
			setLoadingStory(false);
			setStoryHeaderTextViews(mStoryDetail.getSectiontitle(),
					mStoryDetail.getStorytitle(), mStoryDetail.getAuthor(),
					mStoryDetail.getStorydatetext());
			setStoryImagesHSViews(mStoryDetail.getJaImages(),
					mStoryDetail.getJaVideos());
			// ADD: PDF resources
			setStoryContentWebView(mStoryDetail);

			updateStoryComment();
			updateGui();
			updateCountGui();

		}
		if (mTabletVersion && mELVComments != null) {
			// // mELVComments
			// // .setOnItemSelectedListener(mOnCommentItemSelectedListener);
			mELVComments.setOnGroupExpandListener(mCommentExpandListener);
			mELVComments.setOnGroupCollapseListener(mCommentCollapseListener);
			if (mELVAdapter != null) {
				mELVAdapter.notifyDataSetChanged();
			}
			if (mTabletVersion) {
				if (!mELVComments.isGroupExpanded(0)) {
					GKIMLog.lf(null, 0, mTAG + "=>expanding on resume.");
					mELVComments.expandGroup(0);
				}
			}
		}
		changeTextMode(TNPreferenceManager.isNightMode());
		super.onResume();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onStop() {
		GKIMLog.lf(null, 1, mTAG + "=>onStop: " + mStoryId + ".");
		if (mELVComments != null) {
			mELVComments.setOnItemSelectedListener(null);
			mELVComments.setOnGroupExpandListener(null);
			mELVComments.setOnGroupCollapseListener(null);
		}
		if (mTNDownloader != null) {
			mTNDownloader.setExitTasksEarly(true);
			mTNDownloader = null;
		}
		super.onStop();
	}

	@Override
	public void onDestroy() {
		GKIMLog.lf(null, 1, mTAG + "=>onDestroy: " + mStoryId + ".");
		super.onDestroy();
	}

	/**
	 * @param bloading
	 *            true if loading
	 */
	private void setLoadingStory(boolean bloading) {
		GKIMLog.lf(null, 0, mTAG + "=>setLoadingStory: " + bloading);
		if (mRootLayout != null) {
			if (bloading) {
				mProgressbar.setVisibility(View.VISIBLE);
			} else {
				mProgressbar.setVisibility(View.GONE);
			}
			boolean bNightmode = TNPreferenceManager.isNightMode();
			// changeTextMode(bNightmode);
			if (mWvContent != null) {
				int id = bNightmode ? R.color.storydetail_background_black
						: R.color.storydetail_background_white;
				mWvContent.setBackgroundColor(getResources().getColor(id));
			}
		}
	}

	private void loadStory(String storyId) {
		mStoryDetail = null;
		if (mTNDownloader != null) {
			// mTNDownloader.setExitTasksEarly(true);
			// mTNDownloader.addDownload(
			// true,
			// RequestDataFactory.makeStoryRequest(
			// TNPreferenceManager.getUserId(), storyId));

			String storyType = "";
			VideoStoryDetailFragmentActivity storyDetailFragmentActivity = (VideoStoryDetailFragmentActivity) getActivity();
			if (storyDetailFragmentActivity != null) {
				storyType = storyDetailFragmentActivity.getmStoryType();
			}
			if (storyType != null && storyType.length() > 0) {
				mTNDownloader.addDownload(TNPreferenceManager
						.isConnectionAvailable(), RequestDataFactory
						.makeStoryRequestType(TNPreferenceManager.getUserId(),
								storyId, storyType));
			} else {
				mTNDownloader.addDownload(
						TNPreferenceManager.isConnectionAvailable(),
						RequestDataFactory.makeStoryRequest(
								TNPreferenceManager.getUserId(), storyId));
			}

		}
	}

	public void loadcountfacebook(String url) {
		if (mTNDownloader != null) {
			// mTNDownloader.setExitTasksEarly(true);
			mTNDownloader.addDownload(RequestDataFactory.makecount(url));
		}
	}

	/**
	 * Returns the story id represented by this fragment object.
	 */
	public String getStoryId() {
		return mStoryId;
	}

	/**
	 * Returns the section id of current story.
	 */
	public String getSectionId() {
		if (mStoryDetail != null) {
			return mStoryDetail.getSectionid();
		}
		return "";
	}

	private View initializeView(LayoutInflater inflater, String storyId) {
		mDefaultTF = TNPreferenceManager.getTNTypefaceBOLD();
		// NOTE: if this is tablet, then it was LinearLayout, otherwise is
		// Scrollview
		mRootLayout = (ViewGroup) inflater.inflate(
				R.layout.storydetail_storycontent, null);
		// OverrideScrollView rootLayout = (OverrideScrollView)
		// findViewById(R.id.storydetail_sv_content);

		mProgressbar = (ProgressBar) mRootLayout
				.findViewById(R.id.pb_storydetail_processing);

		mTvCategory = (TextView) mRootLayout
				.findViewById(R.id.tv_storydetail_category);
		mTvCategory.setTypeface(mDefaultTF);
		mTvCategory.setOnClickListener(mDefaultOnClickListener);
		mTvTitle = (TextView) mRootLayout
				.findViewById(R.id.tv_storydetail_title);
		mTvTitle.setTypeface(mDefaultTF);

		mDefaultTF = TNPreferenceManager.getTNTypeface();
		mTvAuthor = (TextView) mRootLayout
				.findViewById(R.id.tv_storydetail_author);
		mTvAuthor.setTypeface(mDefaultTF, Typeface.ITALIC);
		mTvDate = (TextView) mRootLayout.findViewById(R.id.tv_storydetail_date);
		mTvDate.setTypeface(mDefaultTF, Typeface.ITALIC);
		// mTvCommentCount = (TextView)
		// findViewById(R.id.tv_storydetail_comment_header_count);
		mIvTopImage = (ImageView) mRootLayout
				.findViewById(R.id.imv_storydetail_topimage);

		mTvImageCount = (TextView) mRootLayout
				.findViewById(R.id.tv_storydetail_topimage_count);
		mTvImageCount.setTypeface(mDefaultTF);

		mLlTopImageCount = (LinearLayout) mRootLayout
				.findViewById(R.id.ll_storydetail_topimage_count);
		mTvVideoCount = (TextView) mRootLayout
				.findViewById(R.id.tv_storydetail_topvideo_count);
		mTvVideoCount.setTypeface(mDefaultTF);
		mFlImage = (FrameLayout) mRootLayout
				.findViewById(R.id.fl_storydetail_image);
		mFlVideo = (FrameLayout) mRootLayout
				.findViewById(R.id.fl_storydetail_video);
		mBtVideo = (Button) mRootLayout.findViewById(R.id.bt_storydetail_video);
		mBtVideo.setOnClickListener(mDefaultOnClickListener);
		mBtImage = (Button) mRootLayout.findViewById(R.id.bt_storydetail_image);
		mBtImage.setOnClickListener(mDefaultOnClickListener);

		mWvContent = (WebView) mRootLayout
				.findViewById(R.id.wv_storydetail_content);
		mWvContent.setFocusable(false);
		configWebViewSetting(mWvContent);
		if (mTabletVersion) {
			mELVComments = (ExpandableListView) mRootLayout
					.findViewById(R.id.elv_storydetail_comments);
			if (mELVAdapter == null) {
				mELVAdapter = new CommentExpandableListAdapter();
			}
			mELVComments.setAdapter(mELVAdapter);

			ImageButton ibtn = (ImageButton) mRootLayout
					.findViewById(R.id.imb_storyfooter_back);
			if (ibtn != null) {
				ibtn.setOnClickListener(mDefaultOnClickListener);
			}
			ImageView imagev = (ImageView) mRootLayout
					.findViewById(R.id.imv_storydetail_shareby_comment);
			if (imagev != null) {
				imagev.setOnClickListener(mDefaultOnClickListener);
			}
			ibtn = (ImageButton) mRootLayout
					.findViewById(R.id.imb_storyfooter_textsize);
			if (ibtn != null) {
				ibtn.setOnClickListener(mDefaultOnClickListener);
			}
			imgBtCheck = (ImageButton) mRootLayout
					.findViewById(R.id.imb_storyfooter_check);
			if (imgBtCheck != null) {
				imgBtCheck.setOnClickListener(mDefaultOnClickListener);
				imgBtCheck.setTag(null);
				imgBtCheck.setSelected(/*
										 * TNPreferenceManager.checkLoggedIn()
										 * &&
										 */TNPreferenceManager
						.hasSavedStory(storyId));
			}
			ibtn = (ImageButton) mRootLayout
					.findViewById(R.id.imb_storyfooter_share);
			if (ibtn != null) {
				ibtn.setOnClickListener(mDefaultOnClickListener);
			}
			// ImageView fblike = (ImageView) mRootLayout
			// .findViewById(R.id.imv_storydetail_fblike);
			// if (fblike != null) {
			// fblike.setOnClickListener(mDefaultOnClickListener);
			// }
			mTvFbLike = (TextView) mRootLayout
					.findViewById(R.id.tv_storydetail_fblike_count);
			if (mTvFbLike != null) {
				mTvFbLike.setOnClickListener(mDefaultOnClickListener);
			}
		}

		mPullRefreshScrollView = (PullToRefreshScrollView) mRootLayout
				.findViewById(R.id.storydetail_sv_pulltorefresh);
		mPullRefreshScrollView
				.setOnRefreshListener(new OnRefreshListener<ScrollView>() {

					@Override
					public void onRefresh(
							PullToRefreshBase<ScrollView> refreshView) {
						loadStory(mStoryId);
					}
				});

		mScrollView = mPullRefreshScrollView.getRefreshableView();

		// Nam.nguyen add
		adView = (AdView) mRootLayout.findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		adView.loadAd(adRequest);

		return mRootLayout;
	}

	public void setCheckSave(boolean flag) {
		imgBtCheck.setSelected(flag);
	}

	@SuppressLint("NewApi")
	private void configWebViewSetting(WebView wvContent) {
		if (wvContent != null) {
			WebSettings ws = wvContent.getSettings();
			ws.setDefaultFontSize(getResources().getDimensionPixelSize(
					R.dimen.storydetail_content_textsize));
			ws.setJavaScriptEnabled(true);
			ws.setSupportZoom(false);
			// FIXME: give a general value for web text size.
			// mWvContent.getSettings().setTextZoom(mTextZoomSize);
			if (!mTabletVersion) {
				mTextZoomForPhone = 20;
				if (!resizeFontWebView()) {
					if (!UIUtils.hasICS()) {
						TextSize textsize = TextSize.NORMAL;
						mWvContent.getSettings().setTextSize(textsize);
					} else {
						// mTextZoomSize +=
						// TNPreferenceManager.WEBVIEW_TEXTZOOM_STEP;
						mTextZoomSize = 120;
						mWvContent.getSettings().setTextZoom(mTextZoomSize);
					}
				}
			} else {
				mTextZoomForPhone = 0;
				resizeFontWebView();
			}
			ws.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
			if (mWebViewClient == null) {
				mWebViewClient = new CustomWebViewClient();
			}
			wvContent.setWebViewClient(mWebViewClient);
		}
	}

	private void setStoryHeaderTextViews(String category, String title,
			String author, String date) {
		// mShareContentBuilder.setLength(0);
		// mShareContentBuilder.append(mStoryId).append(",");
		if (mTvCategory != null) {
			// NOTE: changed section title's color to WHITE since April
			// 17, but keep this for "unstable design and requirement"
			// mTvCategory.setTextColor(mSectionColor);
			mTvCategory.setText(category);
		}

		GKIMLog.l(1, TAG + " title:" + title);
		if (mTvTitle != null) {
			mStoryTitle = title;
			mTvTitle.setText(mStoryTitle);

			// mShareContentBuilder.append(mStoryTitle).append(",");
		}
		GKIMLog.l(1,
				TAG + " mStoryTitle:" + mStoryTitle + " " + mTvTitle.getText());
		if (mTvAuthor != null) {
			mTvAuthor.setText(author);
		}
		if (mTvDate != null) {
			mTvDate.setText(date + " ");
		}
	}

	protected void setStoryContentWebView(StoryDetail sd) {
		String strHTMLContent = generatePDFIntoHTMLContent(
				mStoryDetail.getHtmlcontent(), mStoryDetail.getJaPDFs());
		setStoryContentWebView(strHTMLContent);
	}

	protected void setStoryContentWebView(String htmlcontent) {
		if (mWvContent != null) {
			int color = (TNPreferenceManager.isNightMode() ? R.color.storydetail_background_black
					: R.color.storydetail_background_white);
			mWvContent.setBackgroundColor(getResources().getColor(color));
			String htmlful = String.format(CONST_STR_HTML_WRAP, htmlcontent);
			mWvContent.loadDataWithBaseURL("file:///android_asset/", htmlful,
					"text/html", "utf-8", null);
		}
	}

	protected void setHeaderTextViewDate(long storydate) {
		// if (mTabletVersion) {
		// ((VideoStoryDetailFragmentActivity) getActivity())
		// .setHeaderDate((String) DateFormat.format("dd.MM.yyyy",
		// storydate));
		// }
	}

	protected void addStoryComments(StoryComment[] arrComments) {
		if (arrComments == null || arrComments.length == 0
				|| mELVAdapter == null) {
			return;
		}
		if (mTabletVersion) {
			mELVAdapter.clear();
			mELVAdapter.addComments(arrComments);
			mELVComments.expandGroup(0);
		}
	}

	protected void setCommentCountTextView(int count) {
		if (mCommentCount != count) {
			mCommentCount = count;
		}
		GKIMLog.lf(null, 0, mTAG + "=>setCommentCountTextView: "
				+ mCommentCount);
		((VideoStoryDetailFragmentActivity) getActivity())
				.addStoryDetailCommentCount(mStoryId, mCommentCount);
		if (mTvNumber != null && mTabletVersion) {
			mTvNumber.setText(String.valueOf(mCommentCount));
		}
	}

	protected void setStoryImagesHSViews(JsonArray jaImages, JsonArray jaVideos) {
		if (mIvTopImage != null) {
			mIvTopImage.setVisibility(View.GONE);
		}
		if (mLlTopImageCount != null) {
			mLlTopImageCount.setVisibility(View.GONE);

		}

		if ((jaImages == null || jaImages.size() == 0)
				&& (jaVideos == null || jaVideos.size() == 0)) {
			return;
		}
		if (jaImages != null)
			GKIMLog.l(1,
					TAG + " setStoryImagesHSViews jaImages:" + jaImages.size());
		if (jaVideos != null)
			GKIMLog.l(1,
					TAG + " setStoryImagesHSViews jaVideos:" + jaVideos.size());

		ArrayList<ImageThumb> listImageThumbs = new ArrayList<ImageThumb>();
		ArrayList<VideoThumb> listVideoThumbs = new ArrayList<VideoThumb>();
		Gson gson = new Gson();
		Type listTypeImage = new TypeToken<List<ImageThumb>>() {
		}.getType();
		Type listTypeVideo = new TypeToken<List<VideoThumb>>() {
		}.getType();
		listImageThumbs = gson.fromJson(jaImages, listTypeImage);
		listVideoThumbs = gson.fromJson(jaVideos, listTypeVideo);

		generateImageReviewIntent(listImageThumbs, listVideoThumbs,
				mStoryDetail.isImageFirst());

		int images = 0;
		if (listImageThumbs != null) {
			images = listImageThumbs.size();
		}
		final int countImages = images;
		int videos = 0;
		if (listVideoThumbs != null) {
			videos = listVideoThumbs.size();
		}
		final int countVideos = videos;
		String url = mStoryDetail.getFeatured_image();
		if (url == null || url == "" || url.length() <= 0) {
			if (listImageThumbs != null && listImageThumbs.size() > 0) {
				url = listImageThumbs.get(0).getUrl();
			} else if (listVideoThumbs != null && listVideoThumbs.size() > 0) {
				url = listVideoThumbs.get(0).getVideoThumb();
			}
		}
		if (url != null && url.length() > 0) {
			if (url.endsWith(".png") || url.endsWith(".jpg")
					|| url.endsWith(".gif")) {
				mStoryDetail.setTopImageUrl(url);

				if (mIvTopImage != null && ImageLoader.getInstance() != null) {
//					mIvTopImage.setTag(R.id.abs__action_bar, listImageThumbs);
//					mIvTopImage.setTag(R.id.abs__action_bar_container,
//							listVideoThumbs);
					ImageLoader.getInstance().displayImage(url, mIvTopImage,
							new ImageLoadingListener() {
								@Override
								public void onLoadingStarted(String imageUri,
										View view) {
								}

								@Override
								public void onLoadingFailed(String imageUri,
										View view, FailReason failReason) {
								}

								@Override
								public void onLoadingComplete(String imageUri,
										View view, Bitmap loadedImage) {
									view.setVisibility(View.VISIBLE);
									view.post(new Runnable() {
										@Override
										public void run() {
											GKIMLog.l(1, TAG
													+ " download completed :"
													+ mStoryId + " "
													+ countImages + " "
													+ countVideos);
											resizeImage(countImages,
													countVideos);
										}
									});
								}

								@Override
								public void onLoadingCancelled(String imageUri,
										View view) {

								}
							});
					mIvTopImage.setOnClickListener(mDefaultOnClickListener);
				}
			}
		} else {
			String storyType = "";
			Activity activity = getActivity();
			if (activity instanceof VideoStoryDetailFragmentActivity) {
				VideoStoryDetailFragmentActivity storyDetailFragmentActivity = (VideoStoryDetailFragmentActivity) activity;
				if (storyDetailFragmentActivity != null) {
					storyType = storyDetailFragmentActivity.getmStoryType();
				}
				if (storyType != null
						&& storyType
								.equalsIgnoreCase(VideoStoryDetailFragmentActivity.EXTRAKEY_STORY_TYPE_VIDEO)) {
					if (mIvTopImage != null) {
						mIvTopImage.setVisibility(View.VISIBLE);
						mIvTopImage.setImageDrawable(null);
						mIvTopImage
								.setBackgroundColor(VideoSectionActivity.BACKGROUND_COLOR_DETAIL_BOX);
						resizeImage(0, countVideos);
					}
				}
			}
		}

	}

	private void generateImageReviewIntent(
			ArrayList<ImageThumb> listImageThumbs,
			ArrayList<VideoThumb> listVideoThumbs, boolean imagesFirst) {
		if (mImageReviewIntent != null) {
			mImageReviewIntent = null;
		}

		mImageReviewIntent = new Intent(getActivity(),
				StoryDetailImageReviewActivity.class);
		mImageReviewIntent.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		if (listImageThumbs != null) {
			mImageReviewIntent.putExtra("dataImageThumbs", listImageThumbs);
		}
		if (listVideoThumbs != null) {
			mImageReviewIntent.putExtra("dataVideoThumbs", listVideoThumbs);
			mBtVideo.setTag(listVideoThumbs.get(0));
		}
		mImageReviewIntent.putExtra("dataImagesFirst", imagesFirst);

		// int len = jaImages.size();
		// JsonObject jo = null;
		// String[] urls = new String[len];
		// String[] captions = new String[len];
		// mImageReviewIntent.putExtra(
		// TNPreferenceManager.EXTRAKEY_STORY_IMAGEREVIEW_COUNT, len);
		// for (int i = 0; i < len; i++) {
		// jo = jaImages.get(i).getAsJsonObject();
		// JsonPrimitive jp = jo.getAsJsonPrimitive("url");
		// if (jp != null) {
		// urls[i] = jp.getAsString();
		// }
		// if (jo.has("caption") && jo.getAsJsonPrimitive("caption") != null) {
		// captions[i] = jo.getAsJsonPrimitive("caption").getAsString();
		// }
		// }
		// mImageReviewIntent.putExtra(
		// TNPreferenceManager.EXTRAKEY_STORY_IMAGEREVIEW_URLS, urls);
		// mImageReviewIntent.putExtra(
		// TNPreferenceManager.EXTRAKEY_STORY_IMAGEREVIEW_CAPTIONS,
		// captions);
	}

	public StoryDetail getStoryDetail() {
		return mStoryDetail;
	}

	public String getShareContent() {
		mShareContentBuilder.setLength(0);
		mShareContentBuilder.append(mStoryId).append("%2C");
		mShareContentBuilder.append(mStoryTitle).append("%2C");
		if (mStoryDetail != null) {
			mShareContentBuilder.append(mStoryDetail.getTopImageUrl()).append(
					"%2C");
			mShareContentBuilder.append(mStoryDetail.getWapurl());
		}
		return mShareContentBuilder.toString();
	}

	/**
	 * @param bChecked
	 */
	public void changeTextMode(boolean bChecked) {
		if (mWvContent != null && this.isAdded()) {
			GKIMLog.lf(null, 0, mTAG + "=>changeTextMode (" + mStoryId
					+ ")=>: " + bChecked);
			final String idbg;
			final String idtxt;
			int idColorbg = R.color.storydetail_background_white;

			int idColortxt = R.color.storydetail_text_black;
			if (bChecked) {
				idColorbg = R.color.storydetail_background_black;
				idColortxt = R.color.storydetail_text_white;

				idbg = getResources().getString(
						R.string.storydetail_background_black);
				idtxt = getResources().getString(
						R.string.storydetail_text_white);
			} else {
				idColorbg = R.color.storydetail_background_white;
				idColortxt = R.color.storydetail_text_black;
				idbg = getResources().getString(
						R.string.storydetail_background_white);
				idtxt = getResources().getString(
						R.string.storydetail_text_black);
			}
			mTvTitle.setTextColor(getResources().getColor(idColortxt));
			mTvAuthor.setTextColor(getResources().getColor(idColortxt));
			mTvDate.setTextColor(getResources().getColor(idColortxt));
			mRootLayout.setBackgroundColor(getResources().getColor(idColorbg));

			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// mWvContent.setBackgroundColor(idColorbg);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
						mWvContent.evaluateJavascript(String.format(
								CONST_STR_JSCRIP_COLOR, idbg, idtxt), null);
					} else {
						mWvContent.loadUrl(String.format(
								CONST_STR_JSCRIP_COLOR, idbg, idtxt));
					}

				}
			});

			if (mTabletVersion) {
				LinearLayout ln = (LinearLayout) mRootLayout
						.findViewById(R.id.ln_storydetail_share_comments);
				if (ln != null) {
					ln.setBackgroundColor(getResources().getColor(idColorbg));
				}
				mELVAdapter.changeCommentColors(
						getResources().getColor(idColorbg), getResources()
								.getColor(idColortxt));
				mELVAdapter.notifyDataSetInvalidated();
			}
			updateGui();
			updateCountGui();
		}
	}

	@SuppressLint("NewApi")
	public void changeTextZoom(final boolean bZoomIn) {
		int TextZoomMax = TNPreferenceManager.WEBVIEW_TEXTZOOM_MAX
				+ mTextZoomForPhone;
		int TextZoomMin = TNPreferenceManager.WEBVIEW_TEXTZOOM_MIN
				- mTextZoomForPhone;
		if (mWvContent != null) {
			if (bZoomIn) {
				mTextZoomSize += TNPreferenceManager.WEBVIEW_TEXTZOOM_STEP;
				if (mTextZoomSize > TextZoomMax) {
					mTextZoomSize = TextZoomMax;
				}
			} else {
				mTextZoomSize -= TNPreferenceManager.WEBVIEW_TEXTZOOM_STEP;
				if (mTextZoomSize < TextZoomMin) {
					mTextZoomSize = TextZoomMin;
				}
			}
			if (UIUtils.hasICS()) {
				mWvContent.getSettings().setTextZoom(mTextZoomSize);
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
				mWvContent.getSettings().setTextSize(textsize);
			}
			TNPreferenceManager.setTextSizeMode(mTextZoomSize);
			Drawable wvbg = mWvContent.getBackground();
			if (wvbg != null) {
				mWvContent.invalidate(wvbg.getBounds());
			} else {
				mWvContent.invalidate();
			}
		}
	}

	@SuppressLint("NewApi")
	public boolean resizeFontWebView() {
		int textZoom = TNPreferenceManager.getTextSizeMode();
		if (textZoom >= 0 && mWvContent != null) {
			mTextZoomSize = textZoom;
			int TextZoomMax = TNPreferenceManager.WEBVIEW_TEXTZOOM_MAX
					+ mTextZoomForPhone;
			int TextZoomMin = TNPreferenceManager.WEBVIEW_TEXTZOOM_MIN
					- mTextZoomForPhone;
			if (UIUtils.hasICS()) {
				mWvContent.getSettings().setTextZoom(mTextZoomSize);
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
				mWvContent.getSettings().setTextSize(textsize);
			}
			return true;
		}
		return false;

	}

	private View.OnClickListener getDefaultOnClickListener() {
		return (new View.OnClickListener() {
			public void onClick(View v) {
				GKIMLog.lf(null, 1, mTAG + "=>onClick: " + v);
				switch (v.getId()) {
				case R.id.imv_storydetail_topimage:
				case R.id.bt_storydetail_image:
					if (mImageReviewIntent != null) {
						if (TNPreferenceManager.isConnectionAvailable()) {
							StoryDetailImageReviewActivity.mVideoFirst = false;
							startActivityForResult(mImageReviewIntent, 0);
						} else {
							UIUtils.showToast(
									getActivity().getApplicationContext(),
									getResources()
											.getString(
													R.string.close_application_no_connection));
						}
					}
					break;
				case R.id.bt_storydetail_video:
					StoryDetailImageReviewActivity.mVideoFirst = true;
					startActivityForResult(mImageReviewIntent, 0);

					break;
				case R.id.tv_storydetail_category:
					if (mStoryDetail != null
							&& mStoryDetail.getSectionid() != null) {
						VideoStoryDetailFragmentActivity act = (VideoStoryDetailFragmentActivity) getActivity();
						act.putFinishExtra(
								TNPreferenceManager.EXTRAKEY_BACK_HAS_BACK,
								true);
						act.putFinishExtra(
								TNPreferenceManager.EXTRAKEY_BACK_TO_SECTION,
								mStoryDetail.getSectionid());

						act.finish();

					}
					break;
				case R.id.imb_storyfooter_back:
					getActivity().onBackPressed();
					break;
				case R.id.imb_storyfooter_check:
					v.setTag((Object) mStoryDetail);
				case R.id.imb_storyfooter_share:
				case R.id.imb_storyfooter_textsize:
				case R.id.imv_storydetail_shareby_comment:
					((VideoStoryDetailFragmentActivity) getActivity())
							.performOnClickListener(v);
					break;
				case R.id.tv_storydetail_fblike_count:
					((VideoStoryDetailFragmentActivity) getActivity())
							.setChecksharelikefb(2);
					socialLike(1, getShareContent());
					break;
				default:
					break;
				}
			}
		});
	}

	private DataDownloader getDataloader() {
		return (new DataDownloader(new OnDownloadCompletedListener() {

			@Override
			public void onCompleted(Object key, String result) {
				RequestData contentKey = (RequestData) key;
				GKIMLog.lf(null, 1, mTAG + "=>onCompleted: " + key.toString());

				if (mProcessingDialog != null && mProcessingDialog.isShowing()) {
					mProcessingDialog.dismiss();
				}
				int type = contentKey.type;
				if (result == null || result.length() <= 0) {
					if (type == RequestDataFactory.DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_DETAIL) {
						if (mContext != null && mNoConnection != null
								&& !TNPreferenceManager.isConnectionAvailable()) {
							UIUtils.showToast(mContext, mNoConnection);
						}
					}
					return;
				}
				String theUrl = contentKey.getURLString();
				String keyCacher = contentKey.getKeyCacher();
				boolean bCheckCache = false;
				if (keyCacher != null && keyCacher != "") {
					bCheckCache = TNPreferenceManager.checkCache(keyCacher);
				}

				if (!bCheckCache || contentKey.forceUpdate) {
					TNPreferenceManager.addOrUpdateCache(theUrl, result,
							keyCacher);
				}
				if (type == RequestDataFactory.DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_DETAIL) {
					GKIMLog.lf(null, 0, mTAG + "=> process for story's detail.");
					// FIXME: check for PNS issue here
					// dismiss the dialog.
					if (mPullRefreshScrollView != null) {
						mPullRefreshScrollView.onRefreshComplete();
					}
					Gson gson = new GsonBuilder().registerTypeAdapter(
							GenericResponse.class,
							new GenericResponse.GenericResponseConverter())
							.create();
					GenericResponse gres = gson.fromJson(result,
							GenericResponse.class);
					boolean hasImages = false;
					boolean hasVideos = false;
					boolean hasPDFs = false;
					if (gres != null && gres.isHasData()) {
						StoryDetail sd = gson.fromJson(gres.getData(),
								StoryDetail.class);
						if (sd != null) {
							JsonArray jsImages = sd.getJaImages();
							if (jsImages != null && jsImages.size() > 0) {
								hasImages = true;
								GKIMLog.lf(
										null,
										0,
										mTAG + "=>story id: " + sd.getStoryid()
												+ ", has images: "
												+ jsImages.size());
							}
							JsonArray jsVideos = sd.getJaVideos();
							if (jsVideos != null && jsVideos.size() > 0) {
								hasVideos = true;
								GKIMLog.lf(
										null,
										0,
										mTAG + "=>story id: " + sd.getStoryid()
												+ ", has videos: "
												+ jsVideos.size());
							}
							JsonArray jsPDFs = sd.getJaPDFs();
							if (jsPDFs != null && jsPDFs.size() > 0) {
								hasPDFs = true;
								GKIMLog.lf(null, 0,
										mTAG + "=>story id: " + sd.getStoryid()
												+ ", has pdf: " + jsPDFs.size());
							}
							if (mStoryId.equalsIgnoreCase(String.valueOf(sd
									.getStoryid())) || DEBUG) {

								mStoryDetail = sd;
							}
						}
					}
					if (mStoryDetail != null) {
						// StoryDetail load completed
						sendStoryDetailCompleteMessage();

						loadcountfacebook(mStoryDetail.getWapurl());
						GKIMLog.l(4, "getShareContent : " + getShareContent());
						socialcheckLike(1, getShareContent());
						updateStoryComment();

						setCommentCountTextView(mStoryDetail.getTotalcomments());
						/*
						 * NOTE: changed section title's color to WHITE since
						 * April 17, but keep this for
						 * "unstable design and requirement"
						 */

						if (hasImages || hasVideos) {
							setStoryImagesHSViews(mStoryDetail.getJaImages(),
									mStoryDetail.getJaVideos());
						} else {
							mIvTopImage.setVisibility(View.GONE);
							mTvImageCount.setVisibility(View.GONE);
						}
						setLoadingStory(false);
						// Adding PDFs resource into HTML content;
						if (hasPDFs) {
							setStoryContentWebView(mStoryDetail);
						} else {
							setStoryContentWebView(mStoryDetail
									.getHtmlcontent());
						}

						setStoryHeaderTextViews(mStoryDetail.getSectiontitle(),
								mStoryDetail.getStorytitle(),
								mStoryDetail.getAuthor(),
								mStoryDetail.getStorydatetext());
						setHeaderTextViewDate((mStoryDetail.getStorydate()) * 1000);
						// setBookMarkState(mStoryChecked);
					}
				} else if (type == RequestDataFactory.DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_COMMENTS) {
					// the comment would be lazy loaded in later.
					Gson gson = new GsonBuilder().registerTypeAdapter(
							GenericResponse.class,
							new GenericResponse.GenericResponseConverter())
							.create();
					GenericResponse gres = gson.fromJson(result,
							GenericResponse.class);
					if (gres != null && gres.isHasData()) {
						JsonElement jAE = new JsonParser().parse(gres.getData());
						if (jAE != null && jAE.isJsonArray()) {

							JsonArray ja = jAE.getAsJsonArray();
							int length = ja.size();
							if (length > 0) {
								JsonElement je = ja.get(0);
								if (je.isJsonPrimitive()) {
									return;
								}
								StoryComment[] arrComments = new StoryComment[length];
								StoryComment aComment = null;
								for (int i = 0; i < length; i++) {
									aComment = gson.fromJson(ja.get(i),
											StoryComment.class);
									arrComments[i] = aComment;
								}
								GKIMLog.lf(null, 0, mTAG + "=> loaded: "
										+ length);
								setCommentCountTextView(length);
								addStoryComments(arrComments);
							}
						}
					}
				} else if (type == RequestDataFactory.DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_BOOKMARK) {
					Gson gson = new GsonBuilder().registerTypeAdapter(
							GenericResponse.class,
							new GenericResponse.GenericResponseConverter())
							.create();
					GenericResponse gres = gson.fromJson(result,
							GenericResponse.class);
					if (gres != null && gres.isSucceed()) {
						mStoryChecked = true;
						mFavoriteChaned = true;
						// setBookMarkState(mStoryChecked);
					} else {
						UIUtils.showToast(
								getActivity(),
								getResources()
										.getString(
												R.string.storydetail_cannot_savebookmark));
					}
				} else if (type == RequestDataFactory.DATA_JSON_DEF_REQUESTTYPE_DATA_COUNT_FB) {
					GKIMLog.l(4, "COUNT FB : " + result);
					JsonParser jp = new JsonParser();
					JsonElement je = jp.parse(result);
					if (je != null && je.isJsonArray()) {
						JsonArray ja = je.getAsJsonArray();
						int length = ja.size();
						if (length > 0) {
							JsonElement jse = ja.get(0);
							mFbLikeCount = jse.getAsJsonObject()
									.get("like_count").getAsInt();
							getActivity()
									.getSharedPreferences(getStoryId() + "_",
											Context.MODE_PRIVATE).edit()
									.putInt(getStoryId() + "_", mFbLikeCount)
									.commit();
							updateCountGui();
						}
					} else if (je != null && je.isJsonObject()) {
						mFbLikeCount = je.getAsJsonObject().get("like_count")
								.getAsInt();
						getActivity()
								.getSharedPreferences(getStoryId() + "_",
										Context.MODE_PRIVATE).edit()
								.putInt(getStoryId() + "_", mFbLikeCount)
								.commit();
						updateCountGui();
					}
				}
			}

			@Override
			public String doInBackgroundDebug(Object... params) {
				return null;
			}
		}));
	}

	/**
	 * @author: Timon Trinh
	 * @Description: Send out to StoryFragmentActivity a "2" message, notify
	 *               that the story content has been successfully loaded.
	 */
	protected void sendStoryDetailCompleteMessage() {
		GKIMLog.lf(getActivity(), 0, TAG + "=>sendStoryDetailCompleteMessage: "
				+ mStoryId);
		Message msg = new Message();
		msg.what = TNPreferenceManager.HANDLER_MSG_HAS_STORYDETAIL_LOAD_COMPLETED;
		msg.obj = mStoryDetail;
		msg.setTarget(((VideoStoryDetailFragmentActivity) getActivity())
				.getHandler());
		msg.sendToTarget();

		// same but clear implement
		// VideoStoryDetailFragmentActivity act =
		// (VideoStoryDetailFragmentActivity)
		// getActivity();
		// Handler handler = act.getHandler();
		// if (handler != null) {
		// handler.sendMessage(msg);
		// }
	}

	public void updateStoryComment() {
		if (mTNDownloader != null) {
			mTNDownloader.addDownload(RequestDataFactory
					.makeStoryCommentsRequest(TNPreferenceManager.getUserId(),
							mStoryId));
		}
	}

	// protected void updateStoryFbLikeCount(String wapurl) {
	// mFbLikeCount = mStoryDetail.getFbLikeCount();
	// if (mTabletVersion) {
	// mTvFbLike.setText(String.valueOf(mFbLikeCount));
	// } else {
	// ((VideoStoryDetailFragmentActivity) getActivity()).addStoryFbLikeCount(
	// mStoryId, mFbLikeCount);
	// }
	// }

	private OnGroupExpandListener getOnGroupExpandListener() {
		return (new OnGroupExpandListener() {

			@Override
			public void onGroupExpand(int groupPosition) {
				GKIMLog.lf(null, 0, mTAG + "=>onGroupExpand: " + groupPosition);
				if (mTabletVersion && mELVAdapter != null) {
					ViewGroup group = (ViewGroup) mELVAdapter.getGroupView(
							groupPosition, true, null, mELVComments);
					int count = mELVAdapter.getChildrenCount(groupPosition);
					int totalHeight = 0;
					setCommentCountTextView(count);
					for (int i = 0; i < count; i++) {
						View listItem = mELVAdapter.getChildView(groupPosition,
								i, (i == (count - 1) ? true : false), null,
								group);
						if (listItem != null) {
							listItem.measure(0, 0);
							totalHeight += listItem.getMeasuredHeight();
						}
					}
					final ViewGroup.LayoutParams params = mELVComments
							.getLayoutParams();
					params.height = totalHeight
							+ (mELVComments.getDividerHeight() * (mELVAdapter
									.getChildrenCount(groupPosition) - 1))
							+ (int) getResources().getDimension(
									R.dimen.menu_header_height);
					GKIMLog.lf(null, 0, mTAG + "=>onGroupExpand, height to: "
							+ params.height);
					mELVComments.setLayoutParams(params);
					mELVComments.requestLayout();
					// NOTE: do not remove this
					// mRootLayout.postDelayed(new Runnable() {
					// @Override
					// public void run() {
					// GKIMLog.lf(null, 0, mTAG
					// + "=>onGroupExpand => FOCUS_DOWN "
					// + mRootLayout.toString());
					// ((ScrollView) mRootLayout)
					// .fullScroll(ScrollView.FOCUS_DOWN);
					// }
					// }, 250);
				}
			}
		});
	}

	private OnGroupCollapseListener getOnGroupCollapseListener() {
		return (new OnGroupCollapseListener() {
			@Override
			public void onGroupCollapse(int groupPosition) {
				GKIMLog.lf(null, 0, mTAG + "=>onGroupCollapse: "
						+ groupPosition);
				if (mELVComments != null) {
					if (mTabletVersion) {
						mELVComments.expandGroup(groupPosition);
					}
					// else {
					// ViewGroup.LayoutParams params = mELVComments
					// .getLayoutParams();
					// params.height = (mELVComments.getDividerHeight())
					// + (int) getResources().getDimension(
					// R.dimen.menu_header_height);
					// mELVComments.setLayoutParams(params);
					// mELVComments.requestLayout();
					// }
				}
			}
		});
	}

	public String getStoryTitle() {
		return mStoryTitle;
	}

	public String getStoryDate() {
		String date = (String) DateFormat.format("dd.MM.yyyy", new Date());
		if (mStoryDetail != null) {
			date = (String) DateFormat.format("dd.MM.yyyy",
					(mStoryDetail.getStorydate()) * 1000);
		}
		return date;
	}

	public void socialLike() {
		socialLike(1, getShareContent());
	}

	protected void socialLike(int netId, String shareContent) {
		if (shareContent == null) {
			return;
		}
		String[] data = shareContent.split("%2C");
		if (data == null || data.length < 4) {
			return;
		}
		SocialHelper helper = SocialHelper.getInstance(getActivity(), netId);
		helper.like(data[3], data);
	}

	protected void socialcheckLike(int netId, String shareContent) {
		if (shareContent == null) {
			return;
		}
		String[] data = shareContent.split("%2C");
		if (data == null || data.length < 4) {
			return;
		}
		SocialHelper helper = SocialHelper.getInstance(getActivity(), netId);
		helper.checklike(data[3], data);
	}

	/**
	 *
	 */
	public class CommentExpandableListAdapter extends BaseExpandableListAdapter {

		private ArrayList<StoryComment> mArrComments;
		// private Context mContext;
		private int mMenuHeaderHeight;

		// private String mFormatAuthorCount;
		// private int backgroundColor;
		// private int textColor;

		public class ViewHolder {
			public TextView title;
			public TextView author;
			public TextView date;
			public TextView comment;
			public ImageView imageicon;
		}

		public void setMenuHeaderHeight(int height) {
			mMenuHeaderHeight = height;
		}

		public void setFormatAuthorCount(String format) {
			// mFormatAuthorCount = format;
		}

		@Override
		public int getGroupCount() {
			return 1;
		}

		public void changeCommentColors(int background, int text) {

		}

		@Override
		public int getChildrenCount(int groupPosition) {
			if (mArrComments == null) {
				return 0;
			}
			return mArrComments.size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return null;
		}

		@Override
		public StoryComment getChild(int groupPosition, int childPosition) {
			if (childPosition < 0 || mArrComments == null
					|| childPosition >= mArrComments.size()) {
				return null;
			}
			return mArrComments.get(childPosition);
		}

		@Override
		public long getGroupId(int groupPosition) {
			return 0;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return 0;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = View.inflate(parent.getContext(),
						R.layout.storydetail_comment_header, null);
				LayoutParams lp = (LayoutParams) convertView.getLayoutParams();
				if (convertView.getLayoutParams() == null) {
					lp = new LayoutParams(LayoutParams.MATCH_PARENT,
							mMenuHeaderHeight);
				} else {
					lp.height = mMenuHeaderHeight;
				}
				convertView.setLayoutParams(lp);
				if (mTabletVersion) {
					mTvNumber = (TextView) convertView
							.findViewById(R.id.tv_storydetail_comment_header_count);
					setCommentCountTextView(mCommentCount);
					convertView.setTag(mTvNumber);
				}
			} else {
				mTvNumber = (TextView) convertView.getTag();
			}
			return convertView;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = View.inflate(parent.getContext(),
						R.layout.storydetail_comment_content, null);
				holder = new ViewHolder();
				holder.imageicon = (ImageView) convertView
						.findViewById(R.id.iv_storycomment_icon);
				holder.title = (TextView) convertView
						.findViewById(R.id.tv_storycomment_title);
				holder.title.setTypeface(mDefaultTF, Typeface.BOLD);
				holder.author = (TextView) convertView
						.findViewById(R.id.tv_storycomment_author);
				holder.author.setTypeface(mDefaultTF, Typeface.ITALIC);
				holder.date = (TextView) convertView
						.findViewById(R.id.tv_storycomment_date);
				holder.date.setTypeface(mDefaultTF, Typeface.ITALIC);
				holder.comment = (TextView) convertView
						.findViewById(R.id.tv_storycomment_content);
				holder.comment.setTypeface(mDefaultTF, Typeface.NORMAL);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			final TextView title = holder.title;
			StoryComment sc = mArrComments.get(childPosition);
			String sctitle = sc.getCommentTitle();
			if (sctitle != null && sctitle.length() > 0) {
				title.setText(sctitle);
			}
			String urlIcon = sc.getUrlRatingIcon();
			if (urlIcon != null && urlIcon.length() > 0) {
				UIUtils.loadToImageView(urlIcon, holder.imageicon);
			}
			String authorNcount = String.format(
					getResources().getString(R.string.comment_author_n_count),
					sc.getName(), sc.getCommentCount());
			holder.author.setText(authorNcount);
			holder.date.setText(sc.getCommentTimeText());
			// holder.comment.setTextColor(textColor);
			holder.comment.setText(sc.getComment());
			changeTextMode(TNPreferenceManager.isNightMode(), holder);
			return convertView;
		}

		public void changeTextMode(boolean mode, ViewHolder holder) {
			if (mode && holder != null) {
				holder.title.setTextColor(mColorTitle);
				holder.comment.setTextColor(mColorComment);
				holder.author.setTextColor(mColorAuthur);
			}
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

		public void clear() {
			if (mArrComments != null && mArrComments.size() > 0) {
				mArrComments.clear();
			}
		}

		public void addComments(StoryComment[] comments) {
			if (mArrComments == null) {
				mArrComments = new ArrayList<StoryComment>();
			}
			if (comments == null || comments.length == 0) {
				return;
			}
			int oldlen = mArrComments.size();
			for (StoryComment storyComment : comments) {
				mArrComments.add(storyComment);
			}
			int newlen = mArrComments.size();
			if (newlen != oldlen) {
				GKIMLog.lf(null, 0, mTAG + "=> notifyDataSetChanged: " + newlen
						+ " from: " + oldlen);
				notifyDataSetChanged();
			}
		}

		@Deprecated
		public void setNumberOfComment(int numberComment) {
			// if (mTvNumber != null) {
			// mTvNumber.setText(String.valueOf(numberComment));
			// }
		}
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
					GKIMLog.lf(null, 0, mTAG
							+ "=>shouldOverrideUrlLoading, open by browser: "
							+ url);
					// Pass it to the system and open it by default browser,
					// doesn't match your domain
					// Intent intent = new Intent(Intent.ACTION_VIEW);
					// intent.setData(Uri.parse(url));
					//
					// startActivity(intent);
					if (url != null
							&& url.contains(VideoStoryDetailFragmentActivity.EXTRAKEY_STORY_DEEP_LINK)) {
						String strs[] = url
								.split(VideoStoryDetailFragmentActivity.EXTRAKEY_STORY_DEEP_LINK);
						if (strs != null && strs.length > 1) {
							String storyId = strs[1];
							if (storyId != null && storyId.length() > 0) {
								// loadStory(storyId);
								startStoryDetailActivity(storyId);
							}
						}
					}

					return true;
				}
			} catch (Exception e) {
				GKIMLog.lf(
						null,
						0,
						mTAG + "=>shouldOverrideUrlLoading exception: "
								+ e.getMessage());
			}
			return super.shouldOverrideUrlLoading(view, url);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			GKIMLog.lf(null, 0, mTAG + "=>onPageFinished: " + url);
			changeTextMode(TNPreferenceManager.isNightMode());
			super.onPageFinished(view, url);
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			GKIMLog.lf(null, 0, mTAG + "=>onReceivedError: " + errorCode
					+ " from: " + failingUrl);
			super.onReceivedError(view, errorCode, description, failingUrl);
		}

	}

	protected void startStoryDetailActivity(String storyId) {
		if (storyId == null || storyId.length() == 0) {
			return;
		}
		Intent storyIntent = new Intent();
		storyIntent.setClass(getActivity(),
				VideoStoryDetailFragmentActivity.class);
		storyIntent.putExtra(TNPreferenceManager.EXTRAKEY_IS_STORY, true);
		storyIntent.putExtra(TNPreferenceManager.EXTRAKEY_STORYID, storyId);
		storyIntent.putExtra(
				TNPreferenceManager.EXTRAKEY_STORY_FROM_VIDEO_SECTION,
				VideoStoryDetailFragmentActivity.EXTRAKEY_STORY_TYPE_VIDEO);
		this.startActivityForResult(storyIntent, 0);
	}

	public void updateGui() {
		boolean ischeck = getActivity().getSharedPreferences(getStoryId(),
				Context.MODE_PRIVATE).getBoolean(getStoryId(), false);
		if (ischeck) {
			if (mTabletVersion) {
				if (mTvFbLike != null) {
					mTvFbLike
							.setBackgroundResource(R.drawable.ic_storydetail_fb_like_over);
				}
			} else {
				if (getStoryId().equals(
						((VideoStoryDetailFragmentActivity) getActivity())
								.getmStoryId())) {
					TextView tvlikefb = (TextView) ((VideoStoryDetailFragmentActivity) getActivity())
							.getmGuiFooter().findViewById(
									R.id.tv_storydetail_fblike_count);
					tvlikefb.setBackgroundResource(R.drawable.ic_storyfooter_fblike_over);
				}
			}
		} else {
			if (mTabletVersion) {
				if (mTvFbLike != null) {
					mTvFbLike
							.setBackgroundResource(R.drawable.ic_storydetail_fb_like);
				}
			} else {
				if (getStoryId().equals(
						((VideoStoryDetailFragmentActivity) getActivity())
								.getmStoryId())) {
					TextView tvlikefb = (TextView) ((VideoStoryDetailFragmentActivity) getActivity())
							.getmGuiFooter().findViewById(
									R.id.tv_storydetail_fblike_count);
					tvlikefb.setBackgroundResource(R.drawable.ic_storyfooter_fblike);
				}

			}
		}

	}

	public void updateCountGui() {
		GKIMLog.l(4, "getStoryId : " + getStoryId());
		mFbLikeCount = getActivity().getSharedPreferences(getStoryId() + "_",
				Context.MODE_PRIVATE).getInt(getStoryId() + "_", 0);
		if (mTabletVersion) {
			if (mTvFbLike != null) {
				mTvFbLike.setText(mFbLikeCount + "");
			}
		} else {
			if (getStoryId().equals(
					((VideoStoryDetailFragmentActivity) getActivity())
							.getmStoryId())) {
				TextView tvlikefb = (TextView) ((VideoStoryDetailFragmentActivity) getActivity())
						.getmGuiFooter().findViewById(
								R.id.tv_storydetail_fblike_count);
				tvlikefb.setText(mFbLikeCount + "");
			}

		}
	}

	public void reloadContentWebView() {
		if (mStoryDetail != null) {
			// ADD: PDF resources
			setStoryContentWebView(mStoryDetail);
		}
	}

	private String generatePDFIntoHTMLContent(String htmlcontent,
			JsonArray jaPDFs) {
		if (jaPDFs != null && jaPDFs.size() > 0) {
			StringBuilder sb = new StringBuilder();
			// GET PDF's JSON into array list of PDFThumb objects
			ArrayList<PDFThumb> listPDFThumbs = new ArrayList<PDFThumb>();
			Gson gson = new Gson();
			Type listTypePDF = new TypeToken<List<PDFThumb>>() {
			}.getType();
			listPDFThumbs = gson.fromJson(jaPDFs, listTypePDF);
			for (PDFThumb pdfThumb : listPDFThumbs) {
				sb.append(String.format(CONST_STR_HTML_LI_WRAP,
						pdfThumb.getUrl(), pdfThumb.getUrl(),
						pdfThumb.getCaption()));
				GKIMLog.l(1, TAG + " href PDF: " + pdfThumb.getCaption()
						+ " at: " + pdfThumb.getUrl());
			}
			return (String.format("%1s %2s", htmlcontent,
					(String.format(CONST_STR_HTML_UL_WRAP, sb.toString()))));
		}
		// if there don't have PDF then just return nature htmlcontent from the
		// story.
		return htmlcontent;
	}

	public void resizeImage(int countImages, int countVideos) {
		int ivh = mIvTopImage.getMeasuredHeight();
		int ivw = mIvTopImage.getMeasuredWidth();
		GKIMLog.l(4, TAG + " resizeImage storyid:" + getStoryId()
				+ " countImages:" + countImages);
		GKIMLog.l(4, TAG + " resizeImage storyid:" + getStoryId()
				+ " countVideos:" + countVideos);
		// if (mIvTopImage.getDrawable() != null && ivh != 0) {
		// GKIMLog.l(1, TAG + " resizeImage countImages 1111111:"
		// + countImages);
		// int insw = mIvTopImage.getDrawable().getIntrinsicWidth();
		// int insh = mIvTopImage.getDrawable().getIntrinsicHeight();
		//
		// FrameLayout.LayoutParams mlpImg = (FrameLayout.LayoutParams)
		// mIvTopImage
		// .getLayoutParams();
		//
		// if (insw < insh) {
		// int per = insh * 100 / ivh;
		// mlpImg.width = insw * 100 / per;
		// mlpImg.height = ivh;
		// mlpImg.gravity = Gravity.CENTER_HORIZONTAL;
		// mIvTopImage.setLayoutParams(mlpImg);
		// } else {
		// if (ivw != 0) {
		// int per = insw * 100 / ivw;
		// mlpImg.width = ivw;
		// if (per != 0) {
		// mlpImg.height = insh * 100 / per;
		// }
		// mlpImg.gravity = Gravity.CENTER_HORIZONTAL;
		// mIvTopImage.setLayoutParams(mlpImg);
		// }
		// }

		GKIMLog.l(1, TAG + " resizeImage :" + mIvTopImage.getWidth() + " "
				+ mIvTopImage.getHeight());

		if (countImages > 0 && countVideos > 0) {
			mLlTopImageCount.setVisibility(View.VISIBLE);
			mTvImageCount.setText(String.format(mTopImageCount, countImages));
			mFlImage.setVisibility(View.VISIBLE);
			mTvVideoCount.setText(String.format(mTopVideoCount, countVideos));
			mFlVideo.setVisibility(View.VISIBLE);

		} else if (countImages <= 0 && countVideos > 0) {
			mLlTopImageCount.setVisibility(View.VISIBLE);
			mFlImage.setVisibility(View.GONE);
			mTvVideoCount.setText(String.format(mTopVideoCount, countVideos));
			mFlVideo.setVisibility(View.VISIBLE);
		} else if (countImages > 1) {
			mLlTopImageCount.setVisibility(View.VISIBLE);
			mTvImageCount.setText(String.format(mTopImageCount, countImages));
			mFlVideo.setVisibility(View.GONE);
			mFlImage.setVisibility(View.VISIBLE);

		} else {
			mLlTopImageCount.setVisibility(View.GONE);
		}
		// FrameLayout.LayoutParams mlp = (FrameLayout.LayoutParams)
		// mLlTopImageCount
		// .getLayoutParams();
		// mlp.width = mlpImg.width;
		// mLlTopImageCount.setLayoutParams(mlp);
		// }
	}

	public void MyDestroy() {
		if (mWvContent != null) {
			mWvContent.removeAllViews();
			mWvContent.clearFormData();
			mWvContent.clearHistory();
			mWvContent = null;
		}

		mWebViewClient = null;

		if (imgBtCheck != null) {
			imgBtCheck.destroyDrawingCache();
			imgBtCheck = null;
		}

		// private OverrideScrollView mRootLayout;
		if (mTvCategory != null) {
			mTvCategory.destroyDrawingCache();
			mTvCategory = null;
		}

		if (mTvTitle != null) {
			mTvTitle.destroyDrawingCache();
			mTvTitle = null;
		}

		if (mTvAuthor != null) {
			mTvAuthor.destroyDrawingCache();
			mTvAuthor = null;
		}

		if (mTvDate != null) {
			mTvDate.destroyDrawingCache();
			mTvDate = null;
		}

		if (mTvNumber != null) {
			mTvNumber.destroyDrawingCache();
			mTvNumber = null;
		}

		if (mTNDownloader != null) {
			mTNDownloader.setExitTasksEarly(true);
			mTNDownloader = null;
		}

		if (mIvTopImage != null) {
			ImageLoader.getInstance().cancelDisplayTask(mIvTopImage);
			mIvTopImage.destroyDrawingCache();
		}

		if (mStoryDetail != null) {
			mStoryDetail = null;
		}

		if (mELVComments != null) {
			mELVComments.setOnItemSelectedListener(null);
			mELVComments.setOnGroupExpandListener(null);
			mELVComments.setOnGroupCollapseListener(null);
		}

		if (mTNDownloader != null) {
			mTNDownloader.setExitTasksEarly(true);
			mTNDownloader.ExitTask();
			mTNDownloader = null;
		}

		if (mRootLayout != null) {
			mRootLayout.destroyDrawingCache();
			mRootLayout.removeAllViews();
			mRootLayout = null;
		}
	}

}
