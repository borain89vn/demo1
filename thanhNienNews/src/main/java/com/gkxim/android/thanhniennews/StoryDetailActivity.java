/**
 * File: StoryDetailActivity.java
 * Creator: Timon.Trinh (timon@gkxim.com)
 * Date: 24-12-2012
 * 
 */
package com.gkxim.android.thanhniennews;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;

import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.TextSize;
import android.webkit.WebView;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.gkim.thanhniennews.R;
import com.gkxim.android.cache.UrlImageViewCallback;
import com.gkxim.android.cache.UrlImageViewHelper;
import com.gkxim.android.thanhniennews.layout.GUIHeader;
import com.gkxim.android.thanhniennews.layout.GUIListMenuAdapter;
import com.gkxim.android.thanhniennews.layout.GUIListMenuListView;
import com.gkxim.android.thanhniennews.layout.GUIStoryCommentDialog;
import com.gkxim.android.thanhniennews.layout.GUIStoryFooter;
import com.gkxim.android.thanhniennews.layout.GUIStoryShareDialog;
import com.gkxim.android.thanhniennews.layout.OverrideScrollView;
import com.gkxim.android.thanhniennews.models.GenericResponse;
import com.gkxim.android.thanhniennews.models.StoryComment;
import com.gkxim.android.thanhniennews.models.StoryDetail;
import com.gkxim.android.thanhniennews.networking.RequestDataFactory;
import com.gkxim.android.thanhniennews.tracking.Tracking;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.DataDownloader;
import com.gkxim.android.utils.DataDownloader.OnDownloadCompletedListener;
import com.gkxim.android.utils.GKIMLog;
import com.gkxim.android.utils.RequestData;
import com.gkxim.android.utils.UIUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author Timon Trinh
 */
@TargetApi(14)
public class StoryDetailActivity extends Activity {

	private static final String TAG = "StoryDetailActivity";
	private static final String ACTION_STORY_VIEW = "com.gkxim.android.thanhniennews.ACTION_VIEW_STORY";
	private static final String DATA_KEY_STORY_JSON = "data.data";
	private static final String DATA_KEY_STORY_ID = "story_id";
	private static final String CONST_STR_HTML_WRAP = "<html><head><meta content=\"text/html; charset=UTF-8\" /><meta name=\"viewport\" content=\"width=device-width,user-scalable=no\"/><link href=\"style.css\" rel=\"stylesheet\" type=\"text/css\" /></head><body>%1s</body></html>";

	protected static final boolean DEBUG = GKIMLog.DEBUG_ON;

	private int mSectionColor = Color.BLACK;
	private int mTextZoomSize;
	private int mCommentCount = 0;
	private String mStoryId;
	private boolean mStoryChecked;
	private boolean mFavoriteChaned = false;
	private String mStoryTitle;
	private String mOpenedFromSectionId;
	private StoryDetail mStoryDetail = null;
	private AlertDialog mProcessingDialog;
	private GUIHeader mGuiHeader = null;
	private GUIStoryFooter mGuiFooter = null;
	private GUIListMenuListView mGuiMenu = null;
	private GUIStoryShareDialog mShareDialog;
	private GUIStoryCommentDialog mCommentDialog;
	private Dialog mHSVImages = null;
	private Intent mFinishData = null;

	// view components
	private Typeface mDefaultTF;
	private OverrideScrollView mRootLayout;
	private TextView mTvCategory;
	private TextView mTvTitle;
	private TextView mTvAuthor;
	private TextView mTvDate;
	private TextView mTvCommentCount;
	private TextView mTvNumber;
	private ImageView mIvTopImage;
	private WebView mWvContent;
	private ExpandableListView mELVComments;
	private CommentExpandableListAdapter mELVAdapter;
	private ListIterator<String> mListStoryIds = null;



	private DataDownloader mTNDownloader = new DataDownloader(
			new OnDownloadCompletedListener() {

				@Override
				public void onCompleted(Object key, String result) {
					GKIMLog.lf(null, 0,
							TAG + "=>onCompleted: " + key.toString());
					if (result == null || result.length() <= 0) {
						UIUtils.showToast(
								null,
								StoryDetailActivity.this
										.getResources()
										.getString(
												R.string.close_application_no_connection));
						return;
					}
					int type = ((RequestData) key).type;
					if (type == RequestDataFactory.DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_DETAIL) {
						GKIMLog.lf(null, 0, TAG
								+ "=> process for story's detail.");
						// dismiss the dialog.
						if (mProcessingDialog != null
								&& mProcessingDialog.isShowing()) {
							mProcessingDialog.dismiss();
						}
						Gson gson = new GsonBuilder().registerTypeAdapter(
								GenericResponse.class,
								new GenericResponse.GenericResponseConverter())
								.create();
						GenericResponse gres = gson.fromJson(result,
								GenericResponse.class);
						boolean hasImages = false;
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
											TAG + "=>story id: "
													+ sd.getStoryid()
													+ ", has images: "
													+ jsImages.size());

								}
								JsonArray jsVideos = sd.getJaVideos();
								if (jsVideos != null && jsVideos.size() > 0) {
									hasImages = true;
									GKIMLog.lf(
											null,
											0,
											TAG + "=>story id: "
													+ sd.getStoryid()
													+ ", has videos: "
													+ jsVideos.size());

								}
								if (mStoryId.equalsIgnoreCase(String.valueOf(sd
										.getStoryid())) || DEBUG) {
									mStoryDetail = sd;
								}
							}
						}
						if (mStoryDetail != null) {
							mTNDownloader.addDownload(TNPreferenceManager
									.isConnectionAvailable(),
									RequestDataFactory
											.makeStoryCommentsRequest(
													TNPreferenceManager
															.getUserId(),
													mStoryId));
							mSectionColor = TNPreferenceManager
									.getCategoryColorFromPref(mStoryDetail
											.getSectionid());
							setStoryHeaderTextViews(
									mStoryDetail.getSectiontitle(),
									mStoryDetail.getStorytitle(),
									mStoryDetail.getAuthor(),
									mStoryDetail.getStorydatetext());
							if (hasImages) {
								setStoryImagesHSViews(mStoryDetail
										.getJaImages());
							}
							setStoryContentWebView(mStoryDetail
									.getHtmlcontent());
							// setBookMarkState(mStoryChecked);
							updateListIterator(mStoryId);
						} else {
							onBackPressed();
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
							JsonElement jAE = new JsonParser().parse(gres
									.getData());
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
									GKIMLog.lf(null, 0, TAG + "=> loaded: "
											+ length);
									addStoryComments(arrComments);
									mCommentCount = length;
									if (mELVAdapter != null) {
										mELVAdapter.notifyDataSetChanged();
									}
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
									getApplicationContext(),
									getResources()
											.getString(
													R.string.storydetail_cannot_savebookmark));
						}
					}
				}

				@Override
				public String doInBackgroundDebug(Object... params) {
					return null;
				}
			});

	private OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			GKIMLog.lf(StoryDetailActivity.this, 0,
					TAG + "=>onClick: " + v.getId());
			switch (v.getId()) {
			case R.id.header_iv_logo:
			case R.id.menu_list_header_ivhome:
				mFinishData.putExtra(
						TNPreferenceManager.EXTRAKEY_BACK_HAS_BACK, true);
				mFinishData.putExtra(
						TNPreferenceManager.EXTRAKEY_BACK_TO_SECTION,
						TNPreferenceManager.EXTRAVALUE_SECTION_HOME);
				StoryDetailActivity.this.finish();
				break;
			case R.id.menu_in_list:
				if (v instanceof TextView) {
					GUIListMenuAdapter.DataHolder menuItem = (com.gkxim.android.thanhniennews.layout.GUIListMenuAdapter.DataHolder) v
							.getTag();
					mFinishData.putExtra(
							TNPreferenceManager.EXTRAKEY_BACK_HAS_BACK, true);
					mFinishData.putExtra(
							TNPreferenceManager.EXTRAKEY_BACK_TO_SECTION,
							menuItem.id);
					StoryDetailActivity.this.finish();
				}
				break;
			case R.id.tv_storydetail_category:
				if (mStoryDetail != null && mStoryDetail.getSectionid() != null) {
					mFinishData.putExtra(
							TNPreferenceManager.EXTRAKEY_BACK_HAS_BACK, true);
					mFinishData.putExtra(
							TNPreferenceManager.EXTRAKEY_BACK_TO_SECTION,
							mStoryDetail.getSectionid());
					StoryDetailActivity.this.finish();
				}
				break;
			case R.id.menu_list_header_ivmyhome:
				mFinishData.putExtra(
						TNPreferenceManager.EXTRAKEY_BACK_HAS_BACK, true);
				mFinishData.putExtra(
						TNPreferenceManager.EXTRAKEY_BACK_TO_SECTION,
						TNPreferenceManager.EXTRAVALUE_SECTION_USER_PAGE);
				StoryDetailActivity.this.finish();
				break;
			case R.id.menu_list_header_ivsearch:
				mFinishData.putExtra(
						TNPreferenceManager.EXTRAKEY_BACK_HAS_BACK, true);
				mFinishData.putExtra(
						TNPreferenceManager.EXTRAKEY_BACK_TO_SECTION,
						TNPreferenceManager.EXTRAVALUE_SECTION_SEARCH_PAGE);
				StoryDetailActivity.this.finish();
				break;
			case R.id.menu_list_header_ivstored:
				// Note process as same as user's page
				// GKIMLog.lf(null, 0, TAG +
				// "=>Stored page, not handle in BETA");
				// hideGUIListMenu();
				mFinishData.putExtra(
						TNPreferenceManager.EXTRAKEY_BACK_HAS_BACK, true);
				mFinishData.putExtra(
						TNPreferenceManager.EXTRAKEY_BACK_TO_SECTION,
						TNPreferenceManager.EXTRAVALUE_SECTION_USER_PAGE);
				StoryDetailActivity.this.finish();
				break;
			case R.id.imb_storyfooter_back:
				onBackPressed();
				break;
			case R.id.imb_storydetail_back:
				if (mHSVImages != null && mHSVImages.isShowing()) {
					mHSVImages.dismiss();
				}
				break;
			case R.id.imb_storyfooter_fontsmaller:
				changeTextZoom(false);
				break;
			case R.id.imb_storyfooter_fontbigger:
				changeTextZoom(true);
				break;
			case R.id.imb_storyfooter_check:
				if (!mStoryChecked) {
					setBookmarkStory(mStoryId);
				} else {
					UIUtils.showToast(getApplicationContext(), getResources()
							.getString(R.string.storydetail_story_has_saved));
				}
				break;
			case R.id.imb_storyfooter_share:
				showShareDialog();
				break;
			case R.id.imv_storydetail_shareby_comment:
				// if (!TNPreferenceManager.checkLoggedIn()) {
				// UIUtils.showToast(StoryDetailActivity.this, getResources()
				// .getString(R.string.request_for_login));
				// } else {
				showCommentDialog();
				// }
				break;
			case R.id.imv_storydetail_shareby_email:
				UIUtils.showToast(null, "Coming up../email");
				// if (mShareDialog.isShowing()) {
				// mShareDialog.dismiss();
				// }
				break;
			case R.id.imv_storydetail_shareby_facebook:
				UIUtils.showToast(null, "Coming up../facebook");
				// if (mShareDialog.isShowing()) {
				// mShareDialog.dismiss();
				// }
				break;
			case R.id.imv_storydetail_shareby_twitter:
				UIUtils.showToast(null, "Coming up../twitter");
				// if (mShareDialog.isShowing()) {
				// mShareDialog.dismiss();
				// }
				break;
			case R.id.header_ib_menu:
				showGUIListMenu();
				break;
			case R.id.imv_storydetail_topimage:
				// show horizontal scroll images dialog
				if (mHSVImages != null && !mHSVImages.isShowing()) {
					mHSVImages.show();
				}
				break;
			default:
				break;
			}
		}
	};

	private OnItemSelectedListener mOnCommentItemSelectedListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			GKIMLog.lf(StoryDetailActivity.this, 0, TAG + "=>onItemSelected: "
					+ view.getClass().getName() + ", " + position);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			GKIMLog.lf(StoryDetailActivity.this, 0, TAG
					+ "=>onNothingSelected: "
					+ parent.getClass().getSimpleName());
		}
	};

	private OnGroupExpandListener mOnGroupExpandListener = new OnGroupExpandListener() {

		@Override
		public void onGroupExpand(int groupPosition) {
			GKIMLog.lf(null, 0, TAG + "=>onGroupExpand: " + groupPosition);
			if (mELVAdapter != null) {
				ViewGroup group = (ViewGroup) mELVAdapter.getGroupView(
						groupPosition, true, null, mELVComments);
				int count = mELVAdapter.getChildrenCount(0);
				if (mCommentCount != count) {
					mCommentCount = count;
					mTvNumber.setText(String.valueOf(mCommentCount));
				}
				int totalHeight = 0;
				for (int i = 0; i < count; i++) {
					View listItem = mELVAdapter.getChildView(groupPosition, i,
							(i == (count - 1) ? true : false), null, group);
					if (listItem != null) {
						listItem.measure(0, 0);
						totalHeight += listItem.getMeasuredHeight();
					}
				}
				ViewGroup.LayoutParams params = mELVComments.getLayoutParams();
				params.height = totalHeight
						+ (mELVComments.getDividerHeight() * (mELVAdapter
								.getChildrenCount(groupPosition) - 1))
						+ (int) getResources().getDimension(
								R.dimen.menu_header_height);
				GKIMLog.lf(null, 0, TAG + "=>onGroupExpandj, height to: "
						+ params.height);
				mELVComments.setLayoutParams(params);
				mELVComments.requestLayout();
				if (mRootLayout != null) {
					mRootLayout.postDelayed(new Runnable() {
						@Override
						public void run() {
							mRootLayout.fullScroll(ScrollView.FOCUS_DOWN);
						}
					}, 250);
				}
			}
		}
	};
	private OnDismissListener mOnDialogDismissListener = new DialogInterface.OnDismissListener() {
		@Override
		public void onDismiss(DialogInterface dialog) {
			if (dialog instanceof GUIStoryCommentDialog) {
				if (((GUIStoryCommentDialog) dialog).isSucceed()) {
					UIUtils.showToast(
							null,
							getResources().getString(
									R.string.dlg_story_comment_post_succeed));
					mTNDownloader.addDownload(TNPreferenceManager
							.isConnectionAvailable(), RequestDataFactory
							.makeStoryCommentsRequest(
									TNPreferenceManager.getUserId(), mStoryId));
				}
			}
		}
	};

	private Handler mHandler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case TNPreferenceManager.HANDLER_MSG_HAS_LOGGIN_CHANGED:
				GKIMLog.lf(null, 0, TAG + "=>login has changed.");
				return false;
			default:
				break;
			}
			return false;
		}
	});

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (TNPreferenceManager.getApplicationContext() == null) {
			TNPreferenceManager.setContext(this);
		}
		initLayout();

		Intent intent = this.getIntent();
		if (intent != null) {
			String action = intent.getAction();
			if (intent.hasExtra(TNPreferenceManager.EXTRAKEY_IS_STORY)
					&& intent.hasExtra(TNPreferenceManager.EXTRAKEY_STORYID)) {
				mStoryId = intent
						.getStringExtra(TNPreferenceManager.EXTRAKEY_STORYID);
				mStoryChecked = intent.getBooleanExtra(
						TNPreferenceManager.EXTRAKEY_IS_STORY_CHECKED, false);
				mOpenedFromSectionId = intent
						.getStringExtra(TNPreferenceManager.EXTRAKEY_OPEN_STORY_FROM_SECTION);
				String storyIds = intent
						.getStringExtra(TNPreferenceManager.EXTRAKEY_STORYIDS_FROM_SECTION);
				if (storyIds != null && storyIds.length() > 0) {
					ArrayList<String> arr = new ArrayList<String>(
							Arrays.asList(storyIds.split(",")));
					mListStoryIds = arr.listIterator();
				}

			} else if (ACTION_STORY_VIEW.equalsIgnoreCase(action)) {
				// NOTE: from adding StoryDetailSplitActivity, this won't be
				// trigger any more.
				Bundle extra = intent.getExtras();
				if (extra != null && extra.containsKey(DATA_KEY_STORY_JSON)) {
					String strjson = extra.getString(DATA_KEY_STORY_JSON);
					try {
						JSONObject jo = new JSONObject(strjson);
						if (jo.has(DATA_KEY_STORY_ID)) {
							mStoryId = jo.getString(DATA_KEY_STORY_ID);
							mStoryChecked = false;
							mOpenedFromSectionId = TNPreferenceManager.EXTRAVALUE_SECTION_HOME;
						}
					} catch (Exception e) {
						GKIMLog.lf(null, 4, TAG
								+ "=>Exception from parsing json string: "
								+ strjson + ": " + e.getMessage());
					}
				}
			}
		}
		GKIMLog.lf(this, 0, TAG + "=>onCreate: " + mStoryId);
		if (mStoryId == null || mStoryId.length() <= 0) {
			this.finish();
		}
		// startloading
		loadStory(mStoryId);


	}

	protected void updateListIterator(String uptoStoryId) {
		if (uptoStoryId == null || mListStoryIds != null
				|| uptoStoryId.length() == 0)
			return;
		while (mListStoryIds.hasNext()
				&& !uptoStoryId.equals(mListStoryIds.next()))
			;
	}

	private void initLayout() {
		setContentView(R.layout.activity_storydetail);
		mRootLayout = (OverrideScrollView) findViewById(R.id.storydetail_sv_content);
		mGuiHeader = (GUIHeader) findViewById(R.id.guiheader);
		mGuiFooter = (GUIStoryFooter) findViewById(R.id.guifooter);
		// mGuiFooter.setVisibility(View.INVISIBLE);
		mGuiMenu = (GUIListMenuListView) findViewById(R.id.guimenu);
		mProcessingDialog = new AlertDialog.Builder(this)
				.setTitle(R.string.data_downloading)
				.setMessage(R.string.please_wait).setCancelable(true)
				.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						dialog.dismiss();
						mTNDownloader.setExitTasksEarly(true);
						onBackPressed();
					}
				}).create();
		mDefaultTF = TNPreferenceManager.getTNTypeface();
		mTvCategory = (TextView) findViewById(R.id.tv_storydetail_category);
		mTvCategory.setTypeface(mDefaultTF, Typeface.BOLD);
		mTvCategory.setOnClickListener(mOnClickListener);
		mTvTitle = (TextView) findViewById(R.id.tv_storydetail_title);
		mTvTitle.setTypeface(mDefaultTF, Typeface.BOLD);
		mTvAuthor = (TextView) findViewById(R.id.tv_storydetail_author);
		mTvAuthor.setTypeface(mDefaultTF, Typeface.ITALIC);
		mTvDate = (TextView) findViewById(R.id.tv_storydetail_date);
		mTvDate.setTypeface(mDefaultTF, Typeface.ITALIC);
		// mTvCommentCount = (TextView)
		// findViewById(R.id.tv_storydetail_comment_header_count);
		mIvTopImage = (ImageView) findViewById(R.id.imv_storydetail_topimage);
		mELVComments = (ExpandableListView) findViewById(R.id.elv_storydetail_comments);
		mELVAdapter = new CommentExpandableListAdapter();
		mELVComments.setAdapter(mELVAdapter);
		mWvContent = (WebView) findViewById(R.id.wv_storydetail_content);
		configWebViewSetting();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		GKIMLog.lf(this, 0, TAG + "=>onStart.");
		super.onStart();
		// FlurryAgent.onStartSession(this,
		// TNPreferenceManager.getFlurryAPIKey());
		Tracking.startSession(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		GKIMLog.lf(this, 0, TAG + "=>onResume: " + mStoryId);
		if (mFinishData != null) {
			mFinishData = null;
		}
		mFinishData = new Intent();
		mFinishData.putExtra(
				TNPreferenceManager.EXTRAKEY_OPEN_STORY_FROM_SECTION,
				mOpenedFromSectionId);
		if (mGuiHeader != null) {
			mGuiHeader.setOnClickListener(mOnClickListener);
		}
		if (mGuiFooter != null) {
			mGuiFooter.setOnClickListener(mOnClickListener);
		}
		if (mGuiMenu != null) {
			mGuiMenu.setOnClickListener(mOnClickListener);
		}
		if (mELVComments != null) {
			mELVComments
					.setOnItemSelectedListener(mOnCommentItemSelectedListener);
			mELVComments.setOnGroupExpandListener(mOnGroupExpandListener);
		}
		super.onResume();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		GKIMLog.lf(this, 0, TAG + "=>onStop: " + mStoryId);
		if (mGuiHeader != null) {
			mGuiHeader.setOnClickListener(null);
		}
		if (mGuiFooter != null) {
			mGuiFooter.setOnClickListener(null);
		}
		if (mGuiMenu != null) {
			hideGUIListMenu();
			mGuiMenu.setOnClickListener(null);
		}
		if (mELVComments != null) {
			mELVComments.setOnItemSelectedListener(null);
			mELVComments.setOnGroupExpandListener(null);
		}
		if (mTNDownloader != null) {
			mTNDownloader.setExitTasksEarly(true);
		}
		if (mIvTopImage != null && mIvTopImage.getVisibility() == View.VISIBLE) {
			mIvTopImage.setOnClickListener(null);
		}
		// FlurryAgent.onEndSession(this);
		Tracking.endSeesion(this);
		// mRootLayout = null;
		super.onStop();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		GKIMLog.lf(this, 0, TAG + "=>onDestroy.");
		super.onDestroy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDetachedFromWindow()
	 */
	@Override
	public void onDetachedFromWindow() {
		GKIMLog.lf(this, 0, TAG + "=>onDetachedFromWindow.");
		super.onDetachedFromWindow();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		GKIMLog.l(0, TAG + "=>onBackPressed.");
		if (mGuiMenu.getVisibility() == View.VISIBLE) {
			mGuiMenu.setVisibility(View.GONE);
			mGuiFooter.setVisibility(View.VISIBLE);
		} else {
			super.onBackPressed();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#finish()
	 */
	@Override
	public void finish() {
		GKIMLog.lf(this, 0, TAG + "=>finish");
		if (mFavoriteChaned) {
			mFinishData.putExtra(
					TNPreferenceManager.EXTRAKEY_BACK_HAS_FAVORITED_CHANGED,
					mStoryChecked);
		}
		setResult(RESULT_OK, mFinishData);
		super.finish();
	}

	private void configWebViewSetting() {
		if (mWvContent != null) {
			WebSettings ws = mWvContent.getSettings();
			mTextZoomSize = 100;
			ws.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		}
	}

	public Handler getHandler() {
		return mHandler;
	}

	/**
	 * 
	 */
	private void loadStory(String storyId) {
		mStoryDetail = null;
		mTNDownloader.addDownload(RequestDataFactory.makeStoryRequest(
				TNPreferenceManager.getUserId(), storyId));
		mProcessingDialog.show();
	}

	private void showGUIListMenu() {
		if (mGuiMenu != null && mGuiMenu.getVisibility() != View.VISIBLE) {
			mGuiMenu.setVisibility(View.VISIBLE);
			mGuiFooter.setVisibility(View.GONE);
		}
	}

	protected void hideGUIListMenu() {
		if (mGuiMenu != null && mGuiMenu.getVisibility() == View.VISIBLE) {
			mGuiMenu.setVisibility(View.GONE);
			mGuiFooter.setVisibility(View.VISIBLE);
		}
	}

	protected void showShareDialog() {
		if (mShareDialog == null) {
			mShareDialog = new GUIStoryShareDialog(this);
			mShareDialog.setOnShareClickListener(mOnClickListener);
		}
		mShareDialog.show();
	}

	protected void showCommentDialog() {
		if (mCommentDialog != null) {
			mCommentDialog = null;
		}
		if (mShareDialog.isShowing()) {
			mShareDialog.dismiss();
		}
		mCommentDialog = new GUIStoryCommentDialog(this);
		mCommentDialog.setStoryId(mStoryId);
		mCommentDialog.setOnDismissListener(mOnDialogDismissListener);
		mCommentDialog.show();
	}

	protected void setBookmarkStory(String storyId) {
		mFinishData.putExtra(TNPreferenceManager.EXTRAKEY_BACK_TO_SECTION,
				mOpenedFromSectionId);
		mFinishData.putExtra(TNPreferenceManager.EXTRAKEY_BACK_HAS_STORY_ID,
				String.valueOf(mStoryDetail.getStoryid()));
		mTNDownloader.addDownload(RequestDataFactory.makeBookmarkStoryRequest(
				TNPreferenceManager.getUserId(), mStoryId));
	}

	protected void addStoryComments(StoryComment[] arrComments) {
		if (arrComments == null || arrComments.length == 0
				|| mELVAdapter == null) {
			return;
		}
		int len = arrComments.length;
		setCommentCountTextView(len);
		mELVAdapter.clear();
		mELVAdapter.addComments(arrComments);
	}

	private void setStoryHeaderTextViews(String category, String title,
			String author, String date) {
		if (mTvCategory != null) {
			mTvCategory.setTextColor(mSectionColor);
			mTvCategory.setText(category);
		}
		if (mTvTitle != null) {
			mStoryTitle = title;
			mTvTitle.setText(mStoryTitle);
		}
		if (mTvAuthor != null) {
			mTvAuthor.setText(author);
		}
		if (mTvDate != null) {
			mTvDate.setText(date);
		}
	}

	protected void setStoryContentWebView(String htmlcontent) {
		if (mWvContent != null) {
			String htmlful = String.format(CONST_STR_HTML_WRAP, htmlcontent);
			mWvContent.loadDataWithBaseURL("file:///android_asset/", htmlful,
					"text/html", "utf-8", null);
		}
	}

	protected void setStoryImagesHSViews(JsonArray jaImages) {
		// TODO: catch story's images and show on screen. (not in BETA)
		if (jaImages == null || jaImages.size() == 0) {
			return;
		}

		DisplayMetrics dm = this.getResources().getDisplayMetrics();
		final int screenwidth = dm.widthPixels > dm.heightPixels ? dm.heightPixels
				: dm.widthPixels;
		Rect globalBound = new Rect();
		findViewById(android.R.id.content).getGlobalVisibleRect(globalBound);

		int jaLen = jaImages.size();
		FrameLayout fl = (FrameLayout) getLayoutInflater().inflate(
				R.layout.dlg_storydetail_imagereview, null);
		// hsv_storydetail_reviewimages
		// HorizontalScrollView hsv = new HorizontalScrollView(this);
		HorizontalScrollView hsv = (HorizontalScrollView) fl
				.findViewById(R.id.hsv_storydetail_reviewimages);
		ImageButton imbBack = (ImageButton) fl
				.findViewById(R.id.imb_storydetail_back);
		if (imbBack != null) {
			imbBack.setOnClickListener(mOnClickListener);
		}
		LinearLayout ll = (LinearLayout) fl
				.findViewById(R.id.ll_storydetail_reviewimages);
		// LinearLayout ll = new LinearLayout(hsv.getContext());
		// ll.setOrientation(LinearLayout.HORIZONTAL);
		// if (UIUtils.hasICS()) {
		// ll.setDividerPadding(10);
		// }
		// hsv.addView(ll);

		UrlImageViewCallback callback = new UrlImageViewCallback() {

			@Override
			public void onLoaded(ImageView imageView, Drawable loadedDrawable,
					String url, boolean loadedFromCache, String id) {
				GKIMLog.lf(null, 0, TAG + "=>onLoaded: " + url + " to screen: "
						+ screenwidth + " from: " + loadedDrawable);
				if (imageView != null && loadedDrawable != null) {
					UrlImageViewHelper.scaleImage(imageView, screenwidth,
							loadedDrawable);
				}
			}
		};

		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.gravity = Gravity.CENTER;

		String headerImageUrl = "";
		JsonObject jo = null;
		StringBuilder sbid = new StringBuilder();
		StringBuilder sburl = new StringBuilder();
		int loadLen = 0;
		for (JsonElement je : jaImages) {
			sbid.setLength(0);
			sburl.setLength(0);
			jo = je.getAsJsonObject();
			sburl.append(jo.getAsJsonPrimitive("url").getAsString());
			sbid.append(jo.getAsJsonPrimitive("id").toString());
			if (sburl.length() > 0) {
				ImageView img = new ImageView(hsv.getContext());
				img.setPadding(5, 5, 5, 5);
				img.setScaleType(ScaleType.MATRIX);
				img.setLayoutParams(lp);
				// UIUtils.loadToImageView(sburl.toString(), img);
				// UIUtils.loadToImageView(sburl.toString(), img, screenwidth);
				UIUtils.loadToImageView(sburl.toString(), img, callback,
						sbid.toString());
				ll.addView(img);
				loadLen++;

				// Storing first image to as is story's header image
				if (loadLen == 1) {
					headerImageUrl += sburl.toString();
					if (mIvTopImage != null) {
						// this image should be cached in previous loop.
						UIUtils.loadToImageView(headerImageUrl, mIvTopImage);
						mIvTopImage.setVisibility(View.VISIBLE);
						mIvTopImage.setOnClickListener(mOnClickListener);
					}
				}
			}
		}
		GKIMLog.lf(null, 0, TAG + "=>setStoryImagesHSViews loaded: " + loadLen
				+ "/" + jaLen);

		if (loadLen > 0) {
			// Note: show dialog on image's touch
			// initialize mHSVImages, but not show
			mHSVImages = new Dialog(this,
					android.R.style.Theme_Black_NoTitleBar_Fullscreen);
			mHSVImages.setContentView(fl);
		}
	}

	protected void setCommentCountTextView(int count) {
		mCommentCount = count;
		mELVAdapter.setNumberOfComment(mCommentCount);
	}

	protected void changeTextZoom(boolean bZoomIn) {
		if (mWvContent != null) {
			if (bZoomIn) {
				mTextZoomSize += TNPreferenceManager.WEBVIEW_TEXTZOOM_STEP;
				if (mTextZoomSize > TNPreferenceManager.WEBVIEW_TEXTZOOM_MAX) {
					mTextZoomSize = TNPreferenceManager.WEBVIEW_TEXTZOOM_MAX;
				}
			} else {
				mTextZoomSize -= TNPreferenceManager.WEBVIEW_TEXTZOOM_STEP;
				if (mTextZoomSize < TNPreferenceManager.WEBVIEW_TEXTZOOM_MIN) {
					mTextZoomSize = TNPreferenceManager.WEBVIEW_TEXTZOOM_MIN;
				}
			}
			if (UIUtils.hasICS()) {
				mWvContent.getSettings().setTextZoom(mTextZoomSize);
			} else {
				TextSize textsize = TextSize.NORMAL;
				if (mTextZoomSize < 100) {
					if (mTextZoomSize <= TNPreferenceManager.WEBVIEW_TEXTZOOM_MIN) {
						textsize = TextSize.SMALLEST;
					} else {
						textsize = TextSize.SMALLER;
					}
				} else {
					if (mTextZoomSize >= TNPreferenceManager.WEBVIEW_TEXTZOOM_MAX) {
						textsize = TextSize.LARGEST;
					} else {
						textsize = TextSize.LARGER;
					}
				}
				mWvContent.getSettings().setTextSize(textsize);
			}
		}
	}

	/**
	 *
	 */
	public class CommentExpandableListAdapter extends BaseExpandableListAdapter {

		private ArrayList<StoryComment> mArrComments;

		public class ViewHolder {
			public TextView title;
			public TextView author;
			public TextView date;
			public TextView comment;
			public ImageView imageicon;
		}

		@Override
		public int getGroupCount() {
			return 1;
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
							(int) getApplicationContext().getResources()
									.getDimension(R.dimen.menu_header_height));
				} else {
					lp.height = (int) getApplicationContext().getResources()
							.getDimension(R.dimen.menu_header_height);
				}
				convertView.setLayoutParams(lp);
			}
			mTvNumber = (TextView) convertView
					.findViewById(R.id.tv_storydetail_comment_header_count);
			if (mTvNumber != null) {
				mTvNumber.setText(String.valueOf(mCommentCount));
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
				holder.title.setTypeface(mDefaultTF, Typeface.ITALIC);
				holder.date = (TextView) convertView
						.findViewById(R.id.tv_storycomment_date);
				holder.title.setTypeface(mDefaultTF, Typeface.ITALIC);
				holder.comment = (TextView) convertView
						.findViewById(R.id.tv_storycomment_content);
				holder.title.setTypeface(mDefaultTF, Typeface.NORMAL);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			final TextView title = holder.title;
			StoryComment sc = mArrComments.get(childPosition);
			String sctitle = sc.getCommentTitle();
			if (sctitle == null || sctitle.length() <= 0) {
			} else {
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
			holder.comment.setText(sc.getComment());
			return convertView;
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
			for (StoryComment storyComment : comments) {
				mArrComments.add(storyComment);
			}
		}

		public void setNumberOfComment(int numberComment) {
			if (mTvNumber != null) {
				mTvNumber.setText(String.valueOf(numberComment));
			}
		}

	}

}
