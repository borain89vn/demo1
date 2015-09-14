/**
 * 
 */
package com.gkxim.android.thanhniennews.spring;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.StoryDetailImageReviewActivity;
import com.gkxim.android.thanhniennews.layout.GUIHeader;
import com.gkxim.android.thanhniennews.layout.GUIListMenuAdapter;
import com.gkxim.android.thanhniennews.layout.GUIListMenuListView;
import com.gkxim.android.thanhniennews.networking.RequestDataFactory;
import com.gkxim.android.thanhniennews.social.SocialHelper;
import com.gkxim.android.thanhniennews.social.SocialShare;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.DataDownloader;
import com.gkxim.android.utils.DataDownloader.OnDownloadCompletedListener;
import com.gkxim.android.utils.GKIMLog;
import com.gkxim.android.utils.RequestData;
import com.gkxim.android.utils.UIUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;

import java.util.ArrayList;

/**
 * @author Timon
 * 
 */
public class StoryDetailGalleryActivity extends FragmentActivity {
	private static final String TAG = StoryDetailGalleryActivity.class
			.getSimpleName();
	private CGallery mGallery;
	private ArrayList<CBoxes> lstDataImages = new ArrayList<CBoxes>();
	private SpringGalleryAdapter mGalleryAdapter;
	// private GridView mListView;
	private HFGridView mListView;
	private OnRefreshListener<ListView> mOnRefreshListener = getOnRefreshListener();

	// private AlertDialog mProcessingDialog;
	private GUIHeader mGuiHeader = null;
	private GUIStoryFooterSpring mGuiFooter = null;
	private GUIListMenuListView mGuiMenu = null;
	private GUIStoryShareDialogSpring mShareDialog;
	private GUIStoryTextModeDialogSpring mTextModeDialog = null;

	private boolean mMenuShown = false;
	private boolean mTabletVersion = false;
	private Animation mOutAnimation2Left;
	private Animation mInAnimationFromRight;
	private Animation mInAnimationFromLeft;
	private Animation mOutAnimation2Right;

	private Intent mFinishData = null;
	private OnClickListener mOnClickListener = getOnClickListener();
	private UiLifecycleHelper uiHelper;
	private int checksharelikefb = 0;
	private int mStepIndex = 0;
	private DataDownloader mTNDownloader = getDataDownloader();

	private ProgressBar mProgressbar;
	private Typeface mDefaultTF;
	private TextView mTvCategory;
	private TextView mTvTitle;
	private RelativeLayout mRootLayout;
	private TextView mCountLike;
	private ViewGroup mHeaderView;

	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	private void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		GKIMLog.lf(this, 1, TAG + "=>onSessionStateChange.Actity");
		GKIMLog.l(4, "onSessionStateChange");
		if (session != null && session.isOpened()) {
			if (checksharelikefb == 1) {
				checksharelikefb = 0;
				socialShare(1, 0);
			} else if (checksharelikefb == 2) {
				checksharelikefb = 0;
				socialLike();
			}
		}

	}

	@Override
	protected void onActivityResult(int reqCode, int resCode, Intent data) {
		GKIMLog.lf(this, 1, TAG + "=>onActivityResult :" + reqCode);
		uiHelper.onActivityResult(reqCode, resCode, data);
		if (TNPreferenceManager.REQ_CODE_USER_POST == reqCode
				&& TNPreferenceManager.ACTIVITY_RESULT_BACK_FROM_LOGO == resCode
				|| TNPreferenceManager.ACTIVITY_RESULT_BACK_FROM_LOGO_DETAIL_IMAGE == resCode) {
			setResult(resCode, data);
			mFinishData = null;
			// FIXME: this should return to HOME section instead of closing the
			// TetOfYou->Xuan2014
			StoryDetailGalleryActivity.this.finish();
		} else if (TNPreferenceManager.ACTIVITY_RESULT_BACK_FROM_LOGO_DETAIL_IMAGE == resCode) {
			backToSection(TNPreferenceManager.EXTRAVALUE_SECTION_HOME, RESULT_OK);
		} else {
			SocialShare provider = SocialHelper.getLastInstance()
					.getSNSInstance();
			if (provider != null) {
				if (!provider.handlingActivityForResult(reqCode, resCode, data)) {
					super.onActivityResult(reqCode, resCode, data);
				}
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GKIMLog.lf(this, 1, TAG + "=>onCreate");
		setContentView(R.layout.activity_gallery);
		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);

		mTabletVersion = UIUtils.isTablet(this);
		String params = "";
		if (!mTabletVersion) {
			params = RequestDataFactory.DEFAULT_TESTING_PARAM_DEVICE_PHONE;
		} else {
			params = RequestDataFactory.DEFAULT_TESTING_PARAM_DEVICE_TABLET;
		}

		if (getResources().getBoolean(R.bool.portrait_only)) {
			GKIMLog.lf(this, 0, TAG + "=>Not support for rotation");
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		mGalleryAdapter = new SpringGalleryAdapter(this, lstDataImages,
				mOnClickListener);
		initLayout();
		mTNDownloader.addDownload(
				false,
				RequestDataFactory.makeGetSpringGallery(params,
						String.valueOf(UIUtils.getDeviceWidth())));

	}

	private void initLayout() {
		GKIMLog.l(1, TAG + " initLayout");
		mRootLayout = (RelativeLayout) findViewById(R.id.storydetail_relayout_root);
		mGuiHeader = (GUIHeader) findViewById(R.id.guiheader);

		mGuiFooter = (GUIStoryFooterSpring) findViewById(R.id.guifooter);
		// mGuiFooter.setGoneTextSize();
		if (mTabletVersion) {
			mGuiFooter.setVisibility(View.GONE);
		}
		// mGuiFooter.setVisibility(View.INVISIBLE);
		mGuiMenu = (GUIListMenuListView) findViewById(R.id.guimenu);

		// paper grid
		mListView = (HFGridView) findViewById(R.id.pager);

		mProgressbar = (ProgressBar) findViewById(R.id.pb_storydetail_processing);
		// Instantiate a listview.
		// old view is inflated from layout
		// View mheaderView = LinearLayout.inflate(getApplicationContext(),
		// R.layout.spring_header, null);
		// new view is find from xml layout
		mHeaderView = (ViewGroup) findViewById(R.id.springgallery_header);

		mDefaultTF = TNPreferenceManager.getTNTypefaceBOLD();
		if (mTabletVersion) {
			ImageButton btn = (ImageButton) findViewById(R.id.imb_storyfooter_share);
			btn.setOnClickListener(getOnClickListener());
			mCountLike = (TextView) findViewById(R.id.tv_storydetail_fblike_count);
			mCountLike.setOnClickListener(getOnClickListener());
			btn = (ImageButton) findViewById(R.id.imb_storyfooter_back);
			btn.setOnClickListener(getOnClickListener());
		} else {
			// if (mListView != null) {
			// mListView.addHeaderView(mheaderView);
			// }
		}
		mTvCategory = (TextView) findViewById(R.id.tv_storydetail_category);
		mTvCategory.setOnClickListener(mOnClickListener);
		mTvCategory.setTypeface(mDefaultTF);
		mTvTitle = (TextView) findViewById(R.id.tv_storydetail_title);
		mTvTitle.setTypeface(mDefaultTF);
		mListView.setListener(new HFGridView.HFGridViewListener() {
			@Override
			public void readyToDisposeItems() {
				mListView.setAdapter(mGalleryAdapter);
			}
		});
		// mListView.addHeaderView(mheaderView);
		// mListView.setAdapter(mGalleryAdapter);

		mOutAnimation2Left = AnimationUtils.loadAnimation(this,
				R.anim.push_left_out);
		mOutAnimation2Left.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				mMenuShown = false;

			}
		});
		mInAnimationFromLeft = AnimationUtils.loadAnimation(this,
				R.anim.push_right_in);
		mOutAnimation2Right = AnimationUtils.loadAnimation(this,
				R.anim.push_right_out);
		mInAnimationFromRight = AnimationUtils.loadAnimation(this,
				R.anim.push_left_in);
		boolean bChecked = TNPreferenceManager.isNightMode();
		int idColorbg = R.color.storydetail_background_white;
		if (bChecked) {
			idColorbg = R.color.storydetail_background_black;
		}
		mRootLayout.setBackgroundColor(getResources().getColor(idColorbg));
		mListView.setBackgroundColor(getResources().getColor(idColorbg));
	}

	private void updateLayout() {
		if (mGallery != null) {
			mTvCategory.setText(mGallery.getData().getSectiontitle());
//			mTvTitle.setText(mGallery.getData().getTitle());
            mTvTitle.setText(getResources().getString(R.string.tet_cua_ban));

            if (TNPreferenceManager.isNightMode()) {
				mTvTitle.setTextColor(Color.WHITE);
			} else {
				mTvTitle.setTextColor(Color.RED);
			}
			mProgressbar.setVisibility(View.GONE);
			mListView.setColumnWidth(mGallery.getData().getBoxwidth());
			mListView.setNumColumns(mGallery.getData().getLayoutwidth());
			mListView.setHorizontalSpacing(mGallery.getData().getGapwidth());
			mListView.setVerticalSpacing(mGallery.getData().getGapwidth());
			mGalleryAdapter.setBoxGapWidth(mGallery.getData().getBoxwidth(),
					mGallery.getData().getGapwidth());
			mGalleryAdapter.notifyDataSetChanged();
			int fblikecount = mGallery.getFblike_count();
			getSharedPreferences(TNPreferenceManager.SHAREDPREF_SPRING,
					Context.MODE_PRIVATE)
					.edit()
					.putInt(TNPreferenceManager.EXTRAKEY_SPRING_YOURTET_LIKECOUNT,
							fblikecount).commit();
			updateFBLikeCount();
			updateGui();
		}
	}

	@Override
	protected void onStart() {
		GKIMLog.lf(this, 1, TAG + "=>onStart");
		super.onStart();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		uiHelper.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		GKIMLog.lf(this, 1, TAG + "=>onResume");
		if (mFinishData != null) {
			mFinishData = null;
		}
		mFinishData = new Intent();

		if (mGuiHeader != null) {
			mGuiHeader.setOnClickListener(mOnClickListener);
		}
		if (mGuiFooter != null && !mTabletVersion) {
			mGuiFooter.setOnClickListener(mOnClickListener);
		}
		if (mGuiMenu != null) {
			mGuiMenu.setOnClickListener(mOnClickListener);
		}
		if (mListView != null) {
			mListView.setOnRefreshListener(mOnRefreshListener);
		}
		uiHelper.onResume();
		updateLayout();
		super.onResume();
	}

	@Override
	protected void onStop() {
		StoryDetailImageReviewActivity.mVideoFirst = false;
		if (mGuiHeader != null) {
			mGuiHeader.setOnClickListener(null);
		}
		if (mGuiFooter != null && !mTabletVersion) {
			mGuiFooter.setOnClickListener(null);
		}
		if (mGuiMenu != null) {
			hideGUIListMenu();
			mGuiMenu.setOnClickListener(null);
		}
		super.onStop();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		mOnClickListener = null;
		mListView = null;
		uiHelper.onDestroy();
		super.onDestroy();
	}

	private DataDownloader getDataDownloader() {
		return (new DataDownloader(new OnDownloadCompletedListener() {

			@Override
			public void onCompleted(Object key, String result) {
				GKIMLog.lf(null, 0, TAG + "=>onCompleted: " + key.toString());
				if (result == null || result.length() <= 0) {
					UIUtils.showToast(
							null,
							StoryDetailGalleryActivity.this
									.getResources()
									.getString(
											R.string.close_application_no_connection));
					return;
				}
				int type = ((RequestData) key).type;
				if (type == RequestDataFactory.DATA_JSON_DEF_REQUESTTYPE_SPRING_GALLERY) {
					GKIMLog.lf(null, 0, TAG
							+ "=> process for get spring gallery.:" + result);
					Gson gson = new Gson();
					CGallery data = gson.fromJson(result, CGallery.class);
					if (data.getData() != null
							&& data.getData().getBoxes() != null
							&& data.getData().getBoxes().size() > 0) {
						GKIMLog.l(1, TAG + " data.getData().size():"
								+ data.getData().getBoxes().size());
						mGallery = data;
						if (lstDataImages != null && mListView != null) {
							lstDataImages.clear();
							lstDataImages.addAll(data.getData().getBoxes());
							mListView.onRefreshComplete();
						}
						updateLayout();
					}
				} else if (type == RequestDataFactory.DATA_JSON_DEF_REQUESTTYPE_DATA_COUNT_FB) {
					GKIMLog.l(4, TAG + "=> like count on your tet : " + result);
					JsonParser jp = new JsonParser();
					JsonElement je = jp.parse(result);
					int likecount = 0;
					if (je != null && je.isJsonArray()) {
						JsonArray ja = je.getAsJsonArray();
						int length = ja.size();
						if (length > 0) {
							JsonElement jse = ja.get(0);
							likecount = jse.getAsJsonObject().get("like_count")
									.getAsInt();
						}
					} else if (je != null && je.isJsonObject()) {
						likecount = je.getAsJsonObject().get("like_count")
								.getAsInt();
					}
					updateFBLikeCount(likecount);
				}
			}

			@Override
			public String doInBackgroundDebug(Object... params) {
				// TODO Auto-generated method stub
				return null;
			}
		}));
	}

	private View.OnClickListener getOnClickListener() {
		return (new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				GKIMLog.lf(null, 1, TAG + "=>onClick: " + v);
				switch (v.getId()) {
				case R.id.header_iv_logo:
				case R.id.menu_list_header_ivhome:
					backToSection(TNPreferenceManager.EXTRAVALUE_SECTION_HOME,
							TNPreferenceManager.ACTIVITY_RESULT_BACK_FROM_LOGO_DETAIL_IMAGE);
					break;
				case R.id.menu_in_list:
					if (v instanceof LinearLayout) {
						GUIListMenuAdapter.ViewHolder menuItem = (com.gkxim.android.thanhniennews.layout.GUIListMenuAdapter.ViewHolder) v
								.getTag();
						backToSection(menuItem.id, TNPreferenceManager.ACTIVITY_RESULT_BACK_FROM_LOGO);
					}
					break;

				case R.id.menu_list_header_ivmyhome:
					backToSection(
							TNPreferenceManager.EXTRAVALUE_SECTION_USER_PAGE,
							TNPreferenceManager.ACTIVITY_RESULT_BACK_FROM_LOGO);
					break;
				case R.id.menu_list_header_ivstored:
					backToSection(
							TNPreferenceManager.EXTRAVALUE_SECTION_USER_PAGE_SAVED,
							TNPreferenceManager.ACTIVITY_RESULT_BACK_FROM_LOGO);
					break;
				case R.id.menu_list_header_ivsearch:
					backToSection(
							TNPreferenceManager.EXTRAVALUE_SECTION_SEARCH_PAGE,
							TNPreferenceManager.ACTIVITY_RESULT_BACK_FROM_LOGO);
					break;
				case R.id.imb_storyfooter_back:
					onBackPressed();
					break;
				case R.id.imb_storydetail_back:

					break;
				case R.id.imb_storyfooter_textsize:
					showTextModeDialog();
					break;
				case R.id.imb_storyfooter_fontsmaller:
					// changeTextZoom(false);
					break;
				case R.id.imb_storyfooter_fontbigger:
					// changeTextZoom(true);
					break;
				case R.id.tbtn_storyfooter_textmode:
					if (v instanceof ToggleButton) {
						boolean bChecked = ((ToggleButton) v).isChecked();
						GKIMLog.lf(null, 0, TAG + "=> toggle is checked: "
								+ bChecked);
						changeTextMode(bChecked);
					}
					break;
				case R.id.imb_storyfooter_check:
					break;
				case R.id.imb_storyfooter_share:
					showShareDialog();
					break;
				case R.id.imv_storydetail_dlg_shareby_comment:
					// if (!TNPreferenceManager.checkLoggedIn()) {
					// UIUtils.showToast(
					// StoryDetailFragmentActivity.this,
					// getResources().getString(
					// R.string.request_for_login));
					// } else {
					// showCommentDialog();
					// }
					break;
				case R.id.imb_storyfooter_addcomment:
				case R.id.imv_storydetail_shareby_comment:
					// FragmentManager fm = getSupportFragmentManager();
					// StoryDetailFragment frag = (StoryDetailFragment) fm
					// .findFragmentByTag(getFragmentTag(mPager
					// .getCurrentItem()));
					// if (frag != null) {
					// if (frag.getStoryDetail() != null) {
					// if (!mTabletVersion) {
					// startCommentActivity(mStoryId);
					// } else {
					//
					// showCommentDialog();
					//
					// }
					// }
					// }

					break;
				case R.id.imv_storydetail_shareby_email:
					socialShare(0, 0);
					break;
				case R.id.imv_storydetail_shareby_facebook:
					checksharelikefb = 1;
					socialShare(1, 0);
					break;
				case R.id.imv_storydetail_shareby_twitter:
					socialShare(2, 1);
					break;
				case R.id.imv_menulist_bg:
					if (mMenuShown) {
						hideGUIListMenu();
					}
					return;
				case R.id.header_ib_menu:
					if (mMenuShown) {
						hideGUIListMenu();
					} else {
						showGUIListMenu();
					}
					return;
				case R.id.imv_storydetail_topimage:
					// show horizontal scroll images dialog
					// if (mHSVImages != null && !mHSVImages.isShowing()) {
					// mHSVImages.show();
					// }
					break;
				case R.id.tv_storydetail_fblike_count:
					checksharelikefb = 2;
					socialLike();
					break;
				case R.id.picture:
					GKIMLog.l(1, TAG
							+ " album_iconalbum_iconalbum_iconalbum_icon");
					CBoxes box = (CBoxes) v.getTag();
					if (box != null) {
						Intent mImageReviewIntent = new Intent(
								getApplicationContext(),
								StoryDetailImageSpringReviewActivity.class);
						mImageReviewIntent.putExtra("story_id",
								box.getStoryid() + "");
						startActivityForResult(mImageReviewIntent, TNPreferenceManager.REQ_CODE_TETOFYOU_2_VIEWIMAGES);
					}
					break;
				case R.id.tv_storydetail_category:
					StoryDetailGalleryActivity.this.finish();
					break;
				default:
					break;
				}
			}
		});
	}

	protected void showShareDialog() {
		if (mShareDialog == null) {
			mShareDialog = new GUIStoryShareDialogSpring(this);
			mShareDialog.setOnShareClickListener(mOnClickListener);
		}
		mShareDialog.show();
	}

	protected void socialShare(int networkId, int isstatus) {
		if (mShareDialog != null && mShareDialog.isShowing()) {
			mShareDialog.dismiss();
		}

		// Collect story's information
		String[] data = new String[4];
		data[0] = mGallery.getData().getSectiontitle();
		data[1] = mGallery.getData().getTitle();
		data[2] = "";
		data[3] = mGallery.getWap_story_url();
		if (data != null && data.length >= 3) {
			// Progress SNS's strategy
			SocialHelper helper = SocialHelper.getInstance(this, networkId);
			// FIXME: add callback for listening result.
			helper.post(data, isstatus);
		}
	}

	private void showGUIListMenu() {
		if (mGuiMenu != null && mGuiMenu.getVisibility() != View.VISIBLE) {
			mGuiMenu.setVisibility(View.VISIBLE);
			mGuiMenu.startAnimation(mInAnimationFromLeft);
			if (!mTabletVersion) {
				mGuiFooter.setVisibility(View.GONE);
				mListView.setVisibility(View.GONE);
				mListView.startAnimation(mOutAnimation2Right);
				mHeaderView.setVisibility(View.GONE);
				mHeaderView.startAnimation(mOutAnimation2Right);
			}
			mMenuShown = true;
		}
	}

	protected void hideGUIListMenu() {
		if (mGuiMenu != null && mGuiMenu.getVisibility() == View.VISIBLE) {
			mGuiMenu.setVisibility(View.GONE);
			mGuiMenu.startAnimation(mOutAnimation2Left);
			if (!mTabletVersion) {
				mGuiFooter.setVisibility(View.VISIBLE);
				mListView.setVisibility(View.VISIBLE);
				mListView.startAnimation(mInAnimationFromRight);
				mHeaderView.setVisibility(View.VISIBLE);
				mHeaderView.startAnimation(mInAnimationFromRight);
			}
			mMenuShown = false;

			InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			if (im != null && im.isActive()) {
				im.hideSoftInputFromWindow(mGuiMenu.getWindowToken(), 0);
			}
		}
	}

	protected void socialLike() {
		// Collect story's information
		String[] data = new String[4];
		data[0] = mGallery.getData().getSectiontitle();
		data[1] = mGallery.getData().getTitle();
		data[2] = "";
		data[3] = mGallery.getWap_story_url();
		if (data != null && data.length >= 4) {
			SocialHelper helper = SocialHelper.getInstance(this, 1);
			helper.like(data[3], data);
		}
	}

	protected void showTextModeDialog() {
		if (mTextModeDialog == null) {
			mTextModeDialog = new GUIStoryTextModeDialogSpring(this);
			mTextModeDialog.setOnShareClickListener(mOnClickListener);
		}
		mTextModeDialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
			}
		});
		mTextModeDialog.setVisibleButtons(View.GONE, View.GONE, View.VISIBLE);
		mTextModeDialog.setToggleChecked(TNPreferenceManager.isNightMode());
		mTextModeDialog.show();
	}

	protected void changeTextMode(boolean bChecked) {
		TNPreferenceManager.setTextMode(bChecked);
		int idColorbg = getResources().getColor(
				R.color.storydetail_background_white);
		if (bChecked) {
			idColorbg = getResources().getColor(
					R.color.storydetail_background_black);
			mTvTitle.setTextColor(Color.WHITE);
		} else {
			mTvTitle.setTextColor(Color.RED);
		}
		mRootLayout.setBackgroundColor(idColorbg);
		mListView.setBackgroundColor(idColorbg);
	}

	protected void changeTextZoom(boolean bZoomIn) {
		float cursize = mTvTitle.getTextSize();
		float newsize = cursize;
		if (bZoomIn) {
			if ((mStepIndex + 1) <= TNPreferenceManager.WEBVIEW_TEXTZOOM_NUMBERSTEPS) {
				newsize += 0.2f;
				mStepIndex += 1;
			}
		} else {
			if ((mStepIndex - 1) >= (-1f * TNPreferenceManager.WEBVIEW_TEXTZOOM_NUMBERSTEPS)) {
				newsize -= 0.2f;
				mStepIndex -= 1;
			}
		}
		if (newsize != cursize) {
			mTvTitle.setTextSize(newsize);
		}
	}

	public void updateFBLikeCount(int likecount) {
		getSharedPreferences(TNPreferenceManager.SHAREDPREF_SPRING,
				Context.MODE_PRIVATE)
				.edit()
				.putInt(TNPreferenceManager.EXTRAKEY_SPRING_YOURTET_LIKECOUNT,
						likecount).commit();
		if (mTabletVersion) {
			mCountLike.setText(String.valueOf(likecount));
		} else {
			mGuiFooter.setFbLikeCountView(likecount);
		}

	}

	public void updateFBLikeCount() {
		int likecount = getSharedPreferences(
				TNPreferenceManager.SHAREDPREF_SPRING, Context.MODE_PRIVATE)
				.getInt(TNPreferenceManager.EXTRAKEY_SPRING_YOURTET_LIKECOUNT,
						mGallery.getFblike_count());
		if (mTabletVersion) {
			mCountLike.setText(String.valueOf(likecount));
		} else {
			mGuiFooter.setFbLikeCountView(likecount);
		}
	}

	public void updateGui() {
		boolean ischeck = this.getSharedPreferences(
				TNPreferenceManager.SHAREDPREF_SPRING, Context.MODE_PRIVATE)
				.getBoolean(TNPreferenceManager.EXTRAKEY_SPRING_YOURTET_LIKE,
						false);
		if (ischeck) {
			if (mTabletVersion) {
				if (mCountLike != null) {
					mCountLike
							.setBackgroundResource(R.drawable.ic_storydetail_fb_like_over);
				}
			} else {
				mGuiFooter.setFbLiked(ischeck);
			}
		} else {
			if (mTabletVersion) {
				if (mCountLike != null) {
					mCountLike
							.setBackgroundResource(R.drawable.ic_storydetail_fb_like);
				}
			} else {
				mGuiFooter.setFbLiked(ischeck);
			}
		}
	}

	private OnRefreshListener getOnRefreshListener() {
		return (new OnRefreshListener<ListView>() {
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				GKIMLog.lf(StoryDetailGalleryActivity.this, 1, TAG
						+ "=>onRefresh");
				if (mTNDownloader != null) {
					mTNDownloader.setExitTasksEarly(true);
					mTNDownloader = null;
					mTNDownloader = getDataDownloader();
				}
				mTabletVersion = UIUtils
						.isTablet(StoryDetailGalleryActivity.this);
				String params = "";
				if (!mTabletVersion) {
					params = RequestDataFactory.DEFAULT_TESTING_PARAM_DEVICE_PHONE;
				} else {
					params = RequestDataFactory.DEFAULT_TESTING_PARAM_DEVICE_TABLET;
				}

				mTNDownloader.addDownload(
						true,
						RequestDataFactory.makeGetSpringGallery(params,
								String.valueOf(UIUtils.getDeviceWidth())));
			}
		});
	}

	private void backToSection(String sectionId, int resultCode) {
		mFinishData.putExtra(TNPreferenceManager.EXTRAKEY_BACK_HAS_BACK, true);
		mFinishData.putExtra(TNPreferenceManager.EXTRAKEY_BACK_TO_SECTION,
				sectionId);
		setResult(resultCode, mFinishData);
		finish();
	}
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onConfigurationChanged(android.content.res.Configuration)
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}
}
