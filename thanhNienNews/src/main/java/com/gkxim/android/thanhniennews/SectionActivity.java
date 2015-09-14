package com.gkxim.android.thanhniennews;

import java.util.HashMap;
import java.util.Hashtable;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.layout.AsyncSectionAdapter;
import com.gkxim.android.thanhniennews.layout.BoxLayout;
import com.gkxim.android.thanhniennews.layout.BoxViewFrameLayout;
import com.gkxim.android.thanhniennews.layout.DragImageView;
import com.gkxim.android.thanhniennews.layout.EggingListSectionColor;
import com.gkxim.android.thanhniennews.layout.GUIExitDialog;
import com.gkxim.android.thanhniennews.layout.GUIHeader;
import com.gkxim.android.thanhniennews.layout.GUIListMenuAdapter;
import com.gkxim.android.thanhniennews.layout.GUIListMenuListView;
import com.gkxim.android.thanhniennews.layout.GUISimpleLoadingDialog;
import com.gkxim.android.thanhniennews.location.LocationHelper;
import com.gkxim.android.thanhniennews.models.BoxStory;
import com.gkxim.android.thanhniennews.models.GeneralPage;
import com.gkxim.android.thanhniennews.models.GenericResponse;
import com.gkxim.android.thanhniennews.models.IGenericPage;
import com.gkxim.android.thanhniennews.models.Issue;
import com.gkxim.android.thanhniennews.models.SectionPage;
import com.gkxim.android.thanhniennews.networking.RequestDataFactory;
import com.gkxim.android.thanhniennews.service.CatchLowMemoryService;
import com.gkxim.android.thanhniennews.social.SocialHelper;
import com.gkxim.android.thanhniennews.social.SocialShare;
import com.gkxim.android.thanhniennews.spring.StoryDetailGalleryActivity;
import com.gkxim.android.thanhniennews.spring.StoryDetailSpringFragmentActivity;
import com.gkxim.android.thanhniennews.tracking.Tracking;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.ApplicationRating;
import com.gkxim.android.utils.DataDownloader;
import com.gkxim.android.utils.DataDownloader.OnDownloadCompletedListener;
import com.gkxim.android.utils.GKIMLog;
import com.gkxim.android.utils.RequestData;
import com.gkxim.android.utils.UIUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.knx.framework.arise.ARiseHelpMethods;
import com.knx.framework.main.CameraActivity;

public class SectionActivity extends FragmentActivity {

	private static final String TAG = "SectionActivity";
	public static final String USER_STORIES_PAGE = TNPreferenceManager.EXTRAVALUE_SECTION_USER_PAGE;
	public static final String SEARCH_STORIES_PAGE = TNPreferenceManager.EXTRAVALUE_SECTION_SEARCH_PAGE;
	public static String LOCATION_SPECIAL_STORY_BOX_ID = "";
	public static BoxStory mBoxLocationStory = null;
	private static Hashtable<String, String> mListNewIssue = new Hashtable<String, String>();
	private PullToRefreshListView mViewFlow = null;
	private AsyncSectionAdapter mSectionAdapter = null;
	private GUIHeader mGuiHeader = null;
	private GUIListMenuListView mGuiMenu = null;
	private String mIssueId = "";
	private Issue mIssue = null;
	private boolean mIssueChanged = false;
	private String mCurrentSectionId = "";
	protected BoxLayout mCurrentBoxLayout = null;
	protected String mStoryIds = null;
	private boolean mHasChangedSection = false;
	protected boolean mMenuShown = false;
	private ImageView mBacktotop;
	private int mBoxCountDownloaded = 0;
	private boolean mRequestDownload = false;

	private String mLocationListStory = null;

	// Intens for next activities

	// Nam.nguyen
	private long mTimeTouch = 0;
	private UiLifecycleHelper uiHelper;

	// nam.nguyen config ARise SDK begin

	private static final String ARISE_SERVICE_BASE_URL = "https://arise.knorex.com/snap";// "http://thanhnien-snap.knorex.asia";
	private static final String ARISE_TRACKING_URL = "https://arise.knorex.com/tracking/api"; // "http://thanhnien-tracking.knorex.asia/tracking/api";
	private static final String ARISE_TRACKING_APP_ID = "thanhnien_android";
	// nam.nguyen config ARise SDK end

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
	}

	/**
	 * This function contains code to set up and start ARise Hybrid SDK. You may
	 * use these lines to start SDK from your own Activity.
	 */
	private void startARiseSDK() {
		Intent intent = new Intent(SectionActivity.this, CameraActivity.class);
		Bundle b = new Bundle();

		// compulsory, provided by us
		b.putString("SERVICE_BASE_URL", ARISE_SERVICE_BASE_URL);
		b.putString("TRACKING_URL", ARISE_TRACKING_URL);
		b.putString("TRACKING_APP_ID", ARISE_TRACKING_APP_ID);

		// Optional
		// b.putString("LOGO", LOGO);
		// b.putString("GUIDE_IMAGE", GUIDE_IMAGE); // no extension
		// b.putString("GUIDE_VIDEO_URL", GUIDE_VIDEO_URL);
		// b.putString("LANGUAGE", LANGUAGE);
		// b.putInt("THEME_COLOR", THEME_COLOR);
		b.putString("language", "vi");

		intent.putExtras(b);
		startActivity(intent);
	}

	private View.OnClickListener mOnClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			GKIMLog.lf(null, 1, TAG + "=>onClick: " + v);
			switch (v.getId()) {
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
			case R.id.btn_section_backtotop:
				mViewFlow.getRefreshableView().setSelection(0);
				break;
			case R.id.menu_list_header_ivhome:
				hideGUIListMenu();
				startingToSectionPage(
						TNPreferenceManager.EXTRAVALUE_SECTION_HOME, true);
				break;
			case R.id.header_iv_logo:
				hideGUIListMenu();
				// mViewFlow.setSelection(0);

				// Nam.nguyen cheat code
				if (mTimeTouch == 0
						|| System.currentTimeMillis() - mTimeTouch > 1 * 1000) {
					mTimeTouch = System.currentTimeMillis();
					startingToSectionPage(
							TNPreferenceManager.EXTRAVALUE_SECTION_HOME, true);
				}

				break;
			case R.id.menu_list_header_ivmyhome:
				hideGUIListMenu();
				startingToUserStoryPage();
				break;
			case R.id.menu_list_header_ivstored:
				hideGUIListMenu();
				startingToStoredPage();
				break;
			case R.id.menu_list_header_ivsearch:
				startingToSearchPage();
				break;
			case R.id.menu_list_footer_name:
				break;
			case R.id.menu_in_list:
				if (v instanceof LinearLayout) {
					GUIListMenuAdapter.ViewHolder menuItem = (com.gkxim.android.thanhniennews.layout.GUIListMenuAdapter.ViewHolder) v
							.getTag();
					if (menuItem != null) {
						startingToSectionPage(menuItem.id, false);
						v.setSelected(true);
					}
				}
				break;
			case R.id.imv_section_ar:
				// TODO: integrating Knorex-Arise library
				GKIMLog.lf(null, 0, TAG + "starting AriseFragment");
				startARiseSDK();
				break;
			case R.id.imgv_spring_greetings:
				GKIMLog.lf(null, 0, TAG + "starting imgv_spring_greetings");
				Intent springIntent = new Intent(SectionActivity.this,
						StoryDetailSpringFragmentActivity.class);
				startActivityForResult(springIntent,
						TNPreferenceManager.REQ_CODE_SECTION_2_GREETING);
				break;
			case R.id.imgv_tet_of_you:
				GKIMLog.lf(null, 0, TAG + "starting imgv_tet_of_you");
				Intent tetOfYou = new Intent(SectionActivity.this,
						StoryDetailGalleryActivity.class);
				startActivityForResult(tetOfYou,
						TNPreferenceManager.REQ_CODE_SECTION_2_TETOFYOU);
				TNPreferenceManager
						.updateCurrentStandingSectionId(TNPreferenceManager
								.getTetOfYouSectionId());
				break;
			default:
				if (v instanceof BoxViewFrameLayout) {
					// Nam.nguyen cheat code
					if (mTimeTouch == 0
							|| System.currentTimeMillis() - mTimeTouch > 1 * 1000) {
						mTimeTouch = System.currentTimeMillis();
						BoxViewFrameLayout bvf = ((BoxViewFrameLayout) v);
						if (bvf != null) {
							if (bvf.getBoxIndex().equalsIgnoreCase("3")
									&& TNPreferenceManager
											.getCurrentStandingSectionId()
											.equalsIgnoreCase(
													TNPreferenceManager.EXTRAVALUE_SECTION_HOME)
									&& TNPreferenceManager.GPS_GETTINGS) {
								if (bvf.isHasItems()) {
									// go to location section
									LOCATION_SPECIAL_STORY_BOX_ID = bvf
											.getStoryId();
									mBoxLocationStory = bvf.getBoxStory();
									startingToSectionLocationGPSPage(true,
											bvf.getStoryId(), true, null, null);
								}
							} else {
								startStoryDetailActivity(bvf.getStoryId(),
										bvf.getStoryChecked());
							}
						}
					}
				}
				break;
			}
		}
	};

	private OnScrollListener mOnScrollListiener = new OnScrollListener() {
		private int state = -1;
		private static final int TIME_DELAY = 3000;
		private Handler handler = new Handler();
		private Runnable run = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (state == OnScrollListener.SCROLL_STATE_IDLE) {
					setShowBackToTop(false);
				}
			}
		};

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// TODO Auto-generated method stub
			if (state == scrollState) {
				return;
			}
			if (OnScrollListener.SCROLL_STATE_IDLE == scrollState) {
				handler.postDelayed(run, TIME_DELAY);
			} else if (OnScrollListener.SCROLL_STATE_TOUCH_SCROLL == scrollState) {
				setShowBackToTop(true);
			}
			state = scrollState;
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			// TODO Auto-generated method stub
		}
	};

	private OnRefreshListener2<ListView> mOnPullToRefreshListener = new OnRefreshListener2<ListView>() {
		@Override
		public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
			GKIMLog.lf(null, 0, TAG + "=>onPullDownToRefresh.");
			if (mCurrentSectionId != null) {
				if (mCurrentSectionId
						.equalsIgnoreCase(TNPreferenceManager.EXTRAVALUE_SECTION_USER_PAGE_SAVED)) {
					refreshView.onRefreshComplete();
					return;
				}
				mSectionAdapter.clear();
				if (!mCurrentSectionId
						.equalsIgnoreCase(TNPreferenceManager.EXTRAVALUE_SECTION_HOME)) {
					if (mProgressDialog != null && !mProgressDialog.isShowing()) {
						mProgressDialog.show();
					}
					mUIRefresh = true;

					if (mLocationListStory != null) {
						startingToSectionLocationGPSPage(true,
								mLocationListStory, true, null, null);
					} else {
						mTNDownloader.addDownload(false, RequestDataFactory
								.makeSectionRequest(TNPreferenceManager
										.getUserId(), "", String
										.valueOf(UIUtils.getDeviceWidth()),
										null, mCurrentSectionId, null));
					}

					// mTNDownloader.addDownload(true, RequestDataFactory
					// .makeSectionRequest(
					// TNPreferenceManager.getUserId(), "",
					// String.valueOf(UIUtils.getDeviceWidth()),
					// null, mCurrentSectionId, null));
				} else {
					if (mTNDownloader != null) {
						if (mProgressDialog != null
								&& !mProgressDialog.isShowing()) {
							mProgressDialog.show();
						}
						mUIRefresh = true;

						if (!TNPreferenceManager.GPS_GETTINGS) {
							mTNDownloader.addDownload(true, RequestDataFactory
									.makeIssueRequest(TNPreferenceManager
											.getUserId(), "", String
											.valueOf(UIUtils.getDeviceWidth()),
											null, null));
						} else {
							if (SplashActivity.mLocation != null) {
								mTNDownloader
										.addDownload(RequestDataFactory
												.makeIssueHomeGPSWithLocationRequest(
														TNPreferenceManager
																.getUserId(),
														"",
														String.valueOf(UIUtils
																.getDeviceWidth()),
														null,
														null,
														SplashActivity.mLocation
																.getLatitude(),
														SplashActivity.mLocation
																.getLongitude()));
							} else {
								mTNDownloader
										.addDownload(RequestDataFactory
												.makeIssueHomeGPSWithLocationRequest(
														TNPreferenceManager
																.getUserId(),
														"",
														String.valueOf(UIUtils
																.getDeviceWidth()),
														null, null, -1, -1));

							}
						}

					}
				}
			}
		}

		@Override
		public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
			GKIMLog.lf(null, 0, TAG + "=>onPullUpToRefresh.");
			String direction = "-1";
			String lastIssueId = null;
			IGenericPage lastpage = null;
			if (mSectionAdapter != null) {
				lastpage = mSectionAdapter
						.getItem(mSectionAdapter.getCount() - 1);
			}

			if (lastpage != null && lastpage instanceof SectionPage) {
				lastIssueId = ((SectionPage) lastpage).getIssueId();
			}

			if (lastIssueId == null) {
				lastIssueId = mIssueId;
			}

			if (mCurrentSectionId != null) {
				if (mCurrentSectionId
						.equalsIgnoreCase(TNPreferenceManager.EXTRAVALUE_SECTION_USER_PAGE_SAVED)) {
					refreshView.onRefreshComplete();
					return;
				}

				if (!mCurrentSectionId
						.equalsIgnoreCase(TNPreferenceManager.EXTRAVALUE_SECTION_HOME)) {
					if (mProgressDialog != null && !mProgressDialog.isShowing()) {
						mProgressDialog.show();
					}
					if (mLocationListStory != null) {
						startingToSectionLocationGPSPage(false,
								mLocationListStory, true, "1", lastIssueId);
					} else {
						mTNDownloader.addDownload(false, RequestDataFactory
								.makeSectionRequest(TNPreferenceManager
										.getUserId(), "", String
										.valueOf(UIUtils.getDeviceWidth()),
										lastIssueId, mCurrentSectionId,
										direction));
					}

				} else {
					if (mTNDownloader != null) {
						if (mProgressDialog != null
								&& !mProgressDialog.isShowing()) {
							mProgressDialog.show();
						}
						mUIRefresh = true;

						if (!TNPreferenceManager.GPS_GETTINGS) {
							mTNDownloader.addDownload(false, RequestDataFactory
									.makeIssueRequest(TNPreferenceManager
											.getUserId(), "", String
											.valueOf(UIUtils.getDeviceWidth()),
											direction, lastIssueId));
						} else {
							if (SplashActivity.mLocation != null) {
								mTNDownloader
										.addDownload(RequestDataFactory
												.makeIssueHomeGPSWithLocationRequest(
														TNPreferenceManager
																.getUserId(),
														"",
														String.valueOf(UIUtils
																.getDeviceWidth()),
														direction,
														lastIssueId,
														SplashActivity.mLocation
																.getLatitude(),
														SplashActivity.mLocation
																.getLongitude()));
							} else {
								mTNDownloader
										.addDownload(RequestDataFactory
												.makeIssueHomeGPSWithLocationRequest(
														TNPreferenceManager
																.getUserId(),
														"",
														String.valueOf(UIUtils
																.getDeviceWidth()),
														direction, lastIssueId,
														-1, -1));

							}
						}

					}
				}
			}
		}
	};

	private DataDownloader mTNDownloader = new DataDownloader(
			new OnDownloadCompletedListener() {
				@Override
				public void onCompleted(Object key, String result) {
					RequestData contentKey = (RequestData) key;
					GKIMLog.lf(null, 0, TAG + "=>onCompleted: " + key);
					if (mProgressDialog != null && mProgressDialog.isShowing()) {
						mProgressDialog.dismiss();
					}

					if (result == null || result.length() <= 0) {
						if (!TNPreferenceManager.isConnectionAvailable()) {
							UIUtils.showToast(
									null,
									SectionActivity.this
											.getResources()
											.getString(
													R.string.close_application_no_connection));
						}
						mViewFlow.setMode(Mode.BOTH);
						if (mSectionAdapter != null) {
							mSectionAdapter.notifyDataSetInvalidated();
						}
						mRequestDownload = false;
						// Nam.Nguyen
						if (mViewFlow != null) {
							mViewFlow.onRefreshComplete();
						}
						return;
					}

					if (mUIRefresh) {
						mUIRefresh = false;
					}

					String theUrl = contentKey.getURLString();

					String keyCacher = contentKey.getKeyCacher();
					boolean bCheckCache = false;
					if (keyCacher != null && keyCacher != "") {
						bCheckCache = TNPreferenceManager.checkCache(keyCacher);
					}
					int type = contentKey.type;
					// GKIMLog.log("type:" + type);
					if (type == RequestDataFactory.DATA_JSON_DEF_REQUESTTYPE_DATA_BOXES_SECTION) {
						GKIMLog.l(
								1,
								TAG
										+ " DATA_JSON_DEF_REQUESTTYPE_DATA_BOXES_SECTION:"
										+ result);
						Gson gson = new GsonBuilder().registerTypeAdapter(
								Issue.class, new Issue.IssueConverter())
								.create();
						Issue anIssue = gson.fromJson(result, Issue.class);
						if (anIssue != null) {
							String secId = RequestDataFactory
									.getSectionId((RequestData) key);
							if (secId == null) {
								GKIMLog.lf(null, 0, TAG
										+ "=> section Null to refresh.");
								return;
							}
							if (mSectionAdapter.getCount() == 0) {
								mIssueId = anIssue.getPage(0).getIssueId();
							}
							if (!bCheckCache || contentKey.forceUpdate) {
								TNPreferenceManager.addOrUpdateCache(theUrl,
										result, keyCacher);
							}
							addIssue(anIssue, false);
							switchViewFlowBySectionId(secId);
						} else {
							mViewFlow.setMode(Mode.BOTH);

							mRequestDownload = false;
							UIUtils.showToast(
									SectionActivity.this,
									getResources().getString(
											R.string.section_is_not_available));
							GKIMLog.lf(null, 4, TAG
									+ "=> failed to load section from url: "
									+ key.toString());
						}
					} else if (type == RequestDataFactory.DATA_JSON_DEF_REQUESTTYPE_DATA_BOXES) {
						if (isNeedcleared && mSectionAdapter != null) {
							mSectionAdapter.clear();
							isNeedcleared = false;
						}
						Gson gson = new GsonBuilder().registerTypeAdapter(
								Issue.class, new Issue.IssueConverter())
								.create();

						Issue anIssue = gson.fromJson(result, Issue.class);
						if (anIssue != null) {
							addIssue(anIssue, true);
							switchViewFlowBySectionId(TNPreferenceManager.EXTRAVALUE_SECTION_HOME);
							if (mSectionAdapter.getCount() == 0) {
								mIssueId = anIssue.getPage(0).getIssueId();
							}
							if (!bCheckCache || contentKey.forceUpdate) {
								TNPreferenceManager.addOrUpdateCache(theUrl,
										result, keyCacher);
							}
						} else {
							mViewFlow.setMode(Mode.BOTH);

							mRequestDownload = false;
							UIUtils.showToast(
									SectionActivity.this,
									getResources().getString(
											R.string.section_is_not_available));
							GKIMLog.lf(null, 4, TAG
									+ "=> failed to load section from url: "
									+ key.toString());
						}

					} else if (type == RequestDataFactory.DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_USER_STORIES) {
						Gson gson = new GsonBuilder().registerTypeAdapter(
								GenericResponse.class,
								new GenericResponse.GenericResponseConverter())
								.create();
						GenericResponse gres = gson.fromJson(result,
								GenericResponse.class);
						if (gres != null) {
							if (!bCheckCache || contentKey.forceUpdate) {
								TNPreferenceManager.addOrUpdateCache(theUrl,
										result, keyCacher);
							}
							if (!gres.isSucceed() || !gres.isHasData()) {
								if (gres.resultCode == 202) {
									// NOTE: search not found
									(new AlertDialog.Builder(
											SectionActivity.this))
											.setTitle(
													R.string.menu_userpost_not_found)
											.setPositiveButton(R.string.close,
													null).create().show();

								}
								return;
							}

							String strPage = gres.getData();
							if (strPage == null) {
								GKIMLog.lf(null, 0, TAG
										+ "=>User's stories is empty.");
								return;
							}

							gson = new GsonBuilder().registerTypeAdapter(
									GeneralPage.class,
									new GeneralPage.GeneralPageConverter())
									.create();
							GeneralPage gpage = gson.fromJson(strPage,
									GeneralPage.class);
							if (gpage != null) {
								// add user's page and go
								gpage.setSectionId(USER_STORIES_PAGE);
								gpage.setSectionTitle(USER_STORIES_PAGE);
								setGeneralPage(gpage);
							}
						}
					} else if (type == RequestDataFactory.DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_SEARCH) {
						Gson gson = new GsonBuilder().registerTypeAdapter(
								GenericResponse.class,
								new GenericResponse.GenericResponseConverter())
								.create();
						GenericResponse gres = gson.fromJson(result,
								GenericResponse.class);
						if (gres != null) {
							if (!bCheckCache || contentKey.forceUpdate) {
								TNPreferenceManager.addOrUpdateCache(theUrl,
										result, keyCacher);
							}
							if (!gres.isSucceed()) {
								if (gres.resultCode == 202) {

									(new AlertDialog.Builder(
											SectionActivity.this))
											.setTitle(
													R.string.menu_search_not_found)
											.setPositiveButton(R.string.close,
													null).create().show();

								}
								return;
							}
							// NOTE: only hide the Menu list when search found
							hideGUIListMenu();
							String strPage = gres.getData();
							if (strPage == null) {
								GKIMLog.lf(null, 0, TAG
										+ "=>Search stories is empty.");
								return;
							}
							gson = new GsonBuilder().registerTypeAdapter(
									GeneralPage.class,
									new GeneralPage.GeneralPageConverter())
									.create();
							GeneralPage gpage = gson.fromJson(strPage,
									GeneralPage.class);
							if (gpage != null) {
								// add search page and go
								gpage.setSectionId(SEARCH_STORIES_PAGE);
								gpage.setSectionTitle(SEARCH_STORIES_PAGE);
								setGeneralPage(gpage);
							}
						}
					}

					if (mSectionAdapter != null) {
						mSectionAdapter.notifyDataSetChanged();
					}
					if (mViewFlow != null) {
						mViewFlow.onRefreshComplete();
					}

				}

				@Override
				public String doInBackgroundDebug(Object... params) {
					return null;
				}
			});

	private Handler mHandler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case TNPreferenceManager.HANDLER_MSG_HAS_LOGGIN_CHANGED:
				GKIMLog.lf(null, 0, TAG + "=>login has changed.");
				// XXX: not correct on the UID from logout to login state.
				if (USER_STORIES_PAGE.equalsIgnoreCase(mCurrentSectionId)
						|| TNPreferenceManager.EXTRAVALUE_SECTION_USER_PAGE_SAVED
								.equalsIgnoreCase(mCurrentSectionId)) {
					startingToSectionPage(
							TNPreferenceManager.EXTRAVALUE_SECTION_HOME, false);
				} else if (mSectionAdapter != null) {
					mSectionAdapter.notifyDataSetChanged();
				}
				hideGUIListMenu();
				return true;
			default:
				break;
			}
			return false;
		}
	});

	private Animation mOutAnimation2Left;
	private Animation mInAnimationFromRight;
	private Animation mInAnimationFromLeft;
	private Animation mOutAnimation2Right;
	private AlertDialog mProgressDialog;
	private boolean mUIRefresh = false;
	private boolean mIsReadyToExist = true;
	private boolean mTabletVersion = false;
	private GUIExitDialog mExitDialog = null;

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isGPSUpdate = false;

		if (TNPreferenceManager.GPS_GETTINGS) {
			if (SplashActivity.mLocationHelper == null) {
				SplashActivity.mLocationHelper = new LocationHelper(this);
				SplashActivity.mLocationHelper.setSectionActivity(this);
				startLocationHelper();
			} else {
				SplashActivity.mLocationHelper.setSectionActivity(this);
			}
		}
		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);

		GKIMLog.lf(this, 1, TAG + "=>onCreate: " + mCurrentSectionId);
		TNPreferenceManager.setContext(this);
		TNPreferenceManager.loadBoxStoryFormJson();
		if (!TNPreferenceManager.getDataCreated()) {
			GKIMLog.lf(this, 0, TAG
					+ "=>data might be cleared, stop and restart app.");
			(new AlertDialog.Builder(this))
					.setTitle(R.string.title_activity_splash)
					.setMessage(R.string.close_application_cleared_data)
					.setPositiveButton(R.string.close,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											finish();
										}
									});
								}

							}).setCancelable(false).create().show();
			return;
		}
		setContentView(R.layout.activity_section);


		DragImageView ivAr = (DragImageView) findViewById(R.id.imv_section_ar);
		if (ivAr != null) {
			if (ARiseHelpMethods
					.checkARSupportedForDevice(getApplicationContext())) {
				GKIMLog.lf(this, 0, TAG
						+ "=>AR SDK has support for this device");
				DisplayMetrics dm = new DisplayMetrics();
				getWindowManager().getDefaultDisplay().getMetrics(dm);
				RelativeLayout.LayoutParams lp = (android.widget.RelativeLayout.LayoutParams) ivAr
						.getLayoutParams();
				float xfrac = 0.75f;
				float yfrac = 0.28f;
				try {
					xfrac = getResources().getFraction(
							R.dimen.section_ar_pos_xfractor, 1, 1);
					yfrac = getResources().getFraction(
							R.dimen.section_ar_pos_yfractor, 1, 1);
					GKIMLog.lf(this, 0, TAG + "=> fractors: " + xfrac + ", "
							+ yfrac);
				} catch (Exception e) {
					GKIMLog.lf(this, 4, TAG + "=>failed to load AR fractions");
				}
				lp.leftMargin = (int) (dm.widthPixels * xfrac);
				lp.topMargin = (int) (dm.heightPixels * yfrac);
				ivAr.setLayoutParams(lp);
				ivAr.setOnTouchListener(ivAr);
				ivAr.setOnClickListener(mOnClickListener);
				GKIMLog.lf(this, 0, TAG + "=>size: [" + ivAr.getWidth() + ", "
						+ ivAr.getHeight() + "], position: [" + lp.leftMargin
						+ ", " + lp.topMargin + "]");
			} else {
				ivAr.setVisibility(View.GONE);
				GKIMLog.lf(this, 0, TAG
						+ "=>AR SDK is not support for this device");
			}
		}

		mTabletVersion = getResources().getBoolean(R.bool.istablet);
		boolean bCannotRotate = getResources().getBoolean(R.bool.portrait_only);
		if (bCannotRotate) {
			GKIMLog.lf(this, 0, TAG + "=>Not support for rotation");
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		if (UIUtils.hasHoneycomb()) {
			if (getActionBar() != null) {
				getActionBar().setDisplayHomeAsUpEnabled(false);
			}
		}
		initLayout();
		Intent intent = getIntent();
		boolean bBackFromChild = false;
		String strBackToSection = TNPreferenceManager.EXTRAVALUE_SECTION_HOME;
		if (intent != null
				&& intent.hasExtra(TNPreferenceManager.EXTRAKEY_BACK_HAS_BACK)) {
			bBackFromChild = intent.getBooleanExtra(
					TNPreferenceManager.EXTRAKEY_BACK_HAS_BACK, false);
			strBackToSection = intent
					.getStringExtra(TNPreferenceManager.EXTRAKEY_BACK_TO_SECTION);
		}
		String savedSectionId = null;
		String savedIssueId = null;
		if (savedInstanceState != null) {
			if (savedInstanceState
					.containsKey(TNPreferenceManager.EXTRAKEY_ROTATE_SECTION)) {
				savedSectionId = savedInstanceState
						.getString(TNPreferenceManager.EXTRAKEY_ROTATE_SECTION);
			}
			if (savedInstanceState
					.containsKey(TNPreferenceManager.EXTRAKEY_ROTATE_ISSUE_ID)) {
				savedIssueId = savedInstanceState
						.getString(TNPreferenceManager.EXTRAKEY_ROTATE_ISSUE_ID);
			}
			if (bBackFromChild && savedSectionId == null) {
				savedSectionId = strBackToSection;
			}

		}
		GKIMLog.lf(this, 0, TAG + " saved section id: " + savedSectionId
				+ ", back from child: " + bBackFromChild);
		if (savedSectionId != null) {
			GKIMLog.lf(this, 1, TAG + "=>onCreate, but may be from rotation");
			// Or back from StoryDetailFragmentActivity child through PNS
			if (mTNDownloader != null) {
				if (mProgressDialog != null && !mProgressDialog.isShowing()) {
					mProgressDialog.show();
				}
				GKIMLog.lf(this, 0, TAG + "=> requesting for new layout.");
				mUIRefresh = true;
				mIssueChanged = true;
				mCurrentSectionId = savedSectionId;
				TNPreferenceManager
						.updateCurrentStandingSectionId(mCurrentSectionId);

				if (TNPreferenceManager.EXTRAVALUE_SECTION_HOME
						.equalsIgnoreCase(savedSectionId)) {
					if (!TNPreferenceManager.GPS_GETTINGS) {
						mTNDownloader.addDownload(false, RequestDataFactory
								.makeIssueRequest(TNPreferenceManager
										.getUserId(), "", String
										.valueOf(UIUtils.getDeviceWidth()),
										null, savedIssueId));
					} else {
						if (SplashActivity.mLocation != null) {
							mTNDownloader.addDownload(RequestDataFactory
									.makeIssueHomeGPSWithLocationRequest(
											TNPreferenceManager.getUserId(),
											"", String.valueOf(UIUtils
													.getDeviceWidth()), null,
											savedIssueId,
											SplashActivity.mLocation
													.getLatitude(),
											SplashActivity.mLocation
													.getLongitude()));
						} else {

							mTNDownloader.addDownload(RequestDataFactory
									.makeIssueHomeGPSWithLocationRequest(
											TNPreferenceManager.getUserId(),
											"", String.valueOf(UIUtils
													.getDeviceWidth()), null,
											savedIssueId, -1, -1));
						}
					}

				} else if (TNPreferenceManager.EXTRAVALUE_SECTION_SEARCH_PAGE
						.equalsIgnoreCase(savedSectionId)) {
					String strToSearch = TNPreferenceManager
							.getContentToSearch();
					if (validateSearchString(strToSearch)) {
						if (!mProgressDialog.isShowing()) {
							mProgressDialog.show();
						}
						mTNDownloader.addDownload(RequestDataFactory
								.makeSearchStoriesRequest(TNPreferenceManager
										.getUserId(), null, String
										.valueOf(UIUtils.getDeviceWidth()),
										strToSearch));
					}
				} else if (TNPreferenceManager.EXTRAVALUE_SECTION_USER_PAGE_SAVED
						.equalsIgnoreCase(savedSectionId)) {
					mCurrentSectionId = savedSectionId;
					TNPreferenceManager
							.updateCurrentStandingSectionId(mCurrentSectionId);
				} else if (TNPreferenceManager.EXTRAVALUE_SECTION_USER_PAGE
						.equalsIgnoreCase(savedSectionId)) {
					mTNDownloader.addDownload(RequestDataFactory
							.makeUserPostedStoriesRequest(
									TNPreferenceManager.getUserId(), null,
									String.valueOf(UIUtils.getDeviceWidth())));
				} else if (LOCATION_SPECIAL_STORY_BOX_ID != null
						&& LOCATION_SPECIAL_STORY_BOX_ID.length() > 0
						&& LOCATION_SPECIAL_STORY_BOX_ID
								.equalsIgnoreCase(savedSectionId)) {

					startingToSectionLocationGPSPage(true, savedSectionId,
							true, null, null);
				}

				else {
					mTNDownloader.addDownload(RequestDataFactory
							.makeSectionRequest(
									TNPreferenceManager.getUserId(), "",
									String.valueOf(UIUtils.getDeviceWidth()),
									savedIssueId, savedSectionId, null));
				}
			}
		} else {
			// FIXME: fix pns
			if (intent
					.hasExtra(TNPreferenceManager.EXTRAKEY_OPEN_STORY_FROM_PNS)) {
				boolean hasPNS = intent
						.getBooleanExtra(
								TNPreferenceManager.EXTRAKEY_OPEN_STORY_FROM_PNS,
								false);
				String storiId = intent
						.getStringExtra(TNPreferenceManager.EXTRAKEY_STORYID);
				if (hasPNS) {
					Intent intentStoryDetail = new Intent(this,
							StoryDetailFragmentActivity.class);
					intentStoryDetail.putExtra(
							TNPreferenceManager.EXTRAKEY_IS_STORY, true);
					intentStoryDetail.putExtra(
							TNPreferenceManager.EXTRAKEY_STORYID, storiId);
					intentStoryDetail.putExtra(
							TNPreferenceManager.EXTRAKEY_IS_STORY_CHECKED,
							false);
					intentStoryDetail
							.putExtra(
									TNPreferenceManager.EXTRAKEY_OPEN_STORY_FROM_SECTION,
									TNPreferenceManager.EXTRAVALUE_SECTION_HOME);
					intentStoryDetail.putExtra(
							TNPreferenceManager.EXTRAKEY_OPEN_STORY_FROM_PNS,
							true);
					this.startActivityForResult(intentStoryDetail,
							TNPreferenceManager.REQ_CODE_SECTION_2_STORY);
					// FlurryAgent
					// .onEvent(TNPreferenceManager.EVENT_XTIFY_PNS_TOUCHED);
					Tracking.sendEvent(
							TNPreferenceManager.EVENT_XTIFY_PNS_TOUCHED, null);
				}
			} else {
				Intent myIntent = this.getIntent();
				if (myIntent.hasExtra(SplashActivity.EXTRA_SECTION_BOXES)) {
					Gson gson = new GsonBuilder().registerTypeAdapter(
							Issue.class, new Issue.IssueConverter()).create();
					Issue anIssue = gson
							.fromJson(
									myIntent.getStringExtra(SplashActivity.EXTRA_SECTION_BOXES),
									Issue.class);
					if (anIssue != null && anIssue.getPageCount() > 0) {
						if (mIssue == null
								|| anIssue.getRequesteddate() != mIssue
										.getRequesteddate()) {
							mIssue = anIssue;
							mIssueId = anIssue.getPage(0).getIssueId();
							mIssueChanged = true;
							// Nam.Nguyen close. I think be double message.
							// ApplicationRating.checkForRating(this);
						}
					}
				}
			}
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

	}

	private void startLocationHelper() {
		if (SplashActivity.mLocationHelper != null) {
			SplashActivity.mLocationHelper.checkAndStartLocationClientConnect();
		}
	}

	public void reloadData() {
		// Clear Adapter
		if (mCurrentSectionId != null) {
			GKIMLog.lf(this, 1, TAG + "=>onCreate, but may be from rotation");
			// Or back from StoryDetailFragmentActivity child through PNS
			if (mTNDownloader != null) {
				if (mProgressDialog != null && !mProgressDialog.isShowing()) {
					mProgressDialog.show();
				}
				GKIMLog.lf(this, 0, TAG + "=> requesting for new layout.");
				mUIRefresh = true;
				mIssueChanged = true;
				if (TNPreferenceManager.EXTRAVALUE_SECTION_HOME
						.equalsIgnoreCase(mCurrentSectionId)) {
					// if (SplashActivity.mLocation != null) {
					// mTNDownloader
					// .addDownload(RequestDataFactory.makeIssueHomeGPSWithLocationRequest(
					// TNPreferenceManager.getUserId(),
					// "",
					// String.valueOf(UIUtils.getDeviceWidth()),
					// null, mCurrentSectionId,
					// SplashActivity.mLocation.getLatitude(),
					// SplashActivity.mLocation.getLongitude()));
					// } else {
					// mTNDownloader.addDownload(RequestDataFactory
					// .makeIssueRequest(TNPreferenceManager
					// .getUserId(), "", String
					// .valueOf(UIUtils.getDeviceWidth()),
					// null, mIssueId));
					// }

					if (!TNPreferenceManager.GPS_GETTINGS) {
						mTNDownloader.addDownload(false, RequestDataFactory
								.makeIssueRequest(TNPreferenceManager
										.getUserId(), "", String
										.valueOf(UIUtils.getDeviceWidth()), "",
										mIssueId));
					} else {

						if (SplashActivity.mLocation != null) {
							mTNDownloader.addDownload(RequestDataFactory
									.makeIssueHomeGPSWithLocationRequest(
											TNPreferenceManager.getUserId(),
											"", String.valueOf(UIUtils
													.getDeviceWidth()), "",
											mIssueId, SplashActivity.mLocation
													.getLatitude(),
											SplashActivity.mLocation
													.getLongitude()));
						} else {
							mTNDownloader.addDownload(RequestDataFactory
									.makeIssueHomeGPSWithLocationRequest(
											TNPreferenceManager.getUserId(),
											"", String.valueOf(UIUtils
													.getDeviceWidth()), "",
											mIssueId, -1, -1));

						}
					}

				} else if (TNPreferenceManager.EXTRAVALUE_SECTION_SEARCH_PAGE
						.equalsIgnoreCase(mCurrentSectionId)) {
					String strToSearch = TNPreferenceManager
							.getContentToSearch();
					if (validateSearchString(strToSearch)) {
						if (!mProgressDialog.isShowing()) {
							mProgressDialog.show();
						}
						mTNDownloader.addDownload(RequestDataFactory
								.makeSearchStoriesRequest(TNPreferenceManager
										.getUserId(), null, String
										.valueOf(UIUtils.getDeviceWidth()),
										strToSearch));
					}
				} else if (TNPreferenceManager.EXTRAVALUE_SECTION_USER_PAGE
						.equalsIgnoreCase(mCurrentSectionId)) {
					mTNDownloader.addDownload(RequestDataFactory
							.makeUserPostedStoriesRequest(
									TNPreferenceManager.getUserId(), null,
									String.valueOf(UIUtils.getDeviceWidth())));
				} else {
					mTNDownloader.addDownload(RequestDataFactory
							.makeSectionRequest(
									TNPreferenceManager.getUserId(), "",
									String.valueOf(UIUtils.getDeviceWidth()),
									mIssueId, mCurrentSectionId, null));
				}
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getMenuInflater().inflate(R.menu.activity_section, menu);
		return true;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		GKIMLog.lf(this, 1, TAG + "=>onConfigurationChanged.");

		if (mTNDownloader != null) {
			mTNDownloader.setExitTasksEarly(true);
			mTNDownloader = null;
		}

		if (mSectionAdapter != null) {
			mSectionAdapter.clear();
			mSectionAdapter = null;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// if (mTNDownloader != null) {
		// mTNDownloader.setExitTasksEarly(true);
		// }
		GKIMLog.lf(this, 1, TAG + "=>onSaveInstanceState: " + outState);
		outState.putString(TNPreferenceManager.EXTRAKEY_ROTATE_SECTION,
				mCurrentSectionId);
		outState.putString(TNPreferenceManager.EXTRAKEY_ROTATE_ISSUE_ID,
				mIssueId);

		ApplicationRating.CloseDialog();
		if (mTNDownloader != null) {
			mTNDownloader.ExitTask();
		}
		if (mGuiMenu != null) {
			mGuiMenu.CloseDialog();
		}
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		GKIMLog.lf(this, 1, TAG + "=>onRestoreInstanceState: "
				+ savedInstanceState);
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onStart() {
		GKIMLog.lf(this, 1, TAG + "=>onStart. (" + mIssueChanged + ").");
		super.onStart();
		// FlurryAgent.onStartSession(this,
		// TNPreferenceManager.getFlurryAPIKey());
		Tracking.startSession(this);
		if (mIssueChanged) {
			if (mSectionAdapter != null && mSectionAdapter.getCount() > 0) {
				mSectionAdapter.clear();
			}
			if (mIssue != null) {
				addIssue(mIssue, true);
			}
		}
		if (!ApplicationRating.isShowing()) {
			ApplicationRating.checkForRating(this);
		}

	}

	@Override
	protected void onResume() {
		GKIMLog.lf(this, 1, TAG + "=>onResume: " + mCurrentSectionId);
		TNPreferenceManager.updateCurrentStandingSectionId(mCurrentSectionId);
		Log.d("FinishRessumSection","ResumSection");
		if (mGuiHeader != null) {
			mGuiHeader.setOnClickListener(mOnClickListener);
		}
		if (mGuiMenu != null) {
			mGuiMenu.setOnClickListener(mOnClickListener);
			if (mGuiMenu.getVisibility() == View.VISIBLE) {
				mGuiMenu.setVisibility(View.GONE);
				mViewFlow.setVisibility(View.VISIBLE);
				mMenuShown = false;
			}
		}
		if (mViewFlow != null) {
			mViewFlow.setOnRefreshListener(mOnPullToRefreshListener);
			mViewFlow.setOnScrollListener(mOnScrollListiener);
			setShowBackToTop(false);
			mViewFlow.requestFocus();
		}
		if (mHasChangedSection) {
			if (USER_STORIES_PAGE.equalsIgnoreCase(mCurrentSectionId)) {
				startingToUserStoryPage();
			} else if (SEARCH_STORIES_PAGE.equalsIgnoreCase(mCurrentSectionId)) {
				startingToSearchPage();
			} else if (TNPreferenceManager.EXTRAVALUE_SECTION_USER_PAGE_SAVED
					.equalsIgnoreCase(mCurrentSectionId)) {
				startingToStoredPage();
			} else {
				startingToSectionPage(mCurrentSectionId, false);
			}
		} else if (mIssueChanged) {
			if (TNPreferenceManager.EXTRAVALUE_SECTION_USER_PAGE_SAVED
					.equalsIgnoreCase(mCurrentSectionId)) {
				startingToStoredPage();
			} else {
				switchViewFlowBySectionId(mCurrentSectionId);
			}
			mIssueChanged = false;

		} else {
			if (mSectionAdapter != null) {
				GKIMLog.l(1, "Call mSectionAdapter.notifyDataSetChanged()");
				mSectionAdapter.notifyDataSetChanged();
			} else {
				GKIMLog.l(1, "Call reloadData");
				reloadData();
			}
		}
		uiHelper.onResume();
		super.onResume();
		if (SplashActivity.mLocation != null && !isGPSUpdate) {
			isGPSUpdate = true;
			requestUpdateHomeAgainGps();
		}
	}

	private boolean isGPSUpdate;
	private boolean isNeedcleared;

	public void requestUpdateHomeAgainGps() {
		if (TNPreferenceManager.EXTRAVALUE_SECTION_HOME
				.equals(mCurrentSectionId)) {
			if (mTNDownloader != null) {
				isNeedcleared = true;
				// mProgressDialog.show();
				mTNDownloader.addDownload(RequestDataFactory
						.makeIssueHomeGPSWithLocationRequest(
								TNPreferenceManager.getUserId(), "",
								String.valueOf(UIUtils.getDeviceWidth()), null,
								mCurrentSectionId,
								SplashActivity.mLocation.getLatitude(),
								SplashActivity.mLocation.getLongitude()));

				// SplashActivity.mLocationHelper.stopPeriodicUpdates();

			}
		}
	}

	public void gpsSessionUpdate() {
		if (!isGPSUpdate) {
			requestUpdateHomeAgainGps();
			isGPSUpdate = true;
		}
	}

	@Override
	protected void onStop() {
		GKIMLog.lf(this, 1, TAG + "=>onStop.");
		if (mGuiHeader != null) {
			mGuiHeader.setOnClickListener(null);
		}
		if (mGuiMenu != null) {
			mGuiMenu.setOnClickListener(null);
		}
		if (mViewFlow != null) {
			mViewFlow
					.setOnRefreshListener((PullToRefreshBase.OnRefreshListener2<ListView>) null);
		}
		// FlurryAgent.onEndSession(this);
		Tracking.endSeesion(this);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		GKIMLog.lf(this, 0, TAG + "=>onDestroy.");
		uiHelper.onDestroy();
		super.onDestroy();

		if (SplashActivity.mLocationHelper != null) {
			stopLocationHelper();
		}
		stopService();

	}

	private void stopLocationHelper() {
		SplashActivity.mLocationHelper.onPause();
		SplashActivity.mLocationHelper.onStop();
		SplashActivity.mLocationHelper = null;
		SplashActivity.mLocation = null;
	}

	private void initLayout() {
		GKIMLog.lf(this, 0, TAG + "=>initLayout.");
		getWindow().setFormat(PixelFormat.RGBA_8888);
		mGuiHeader = (GUIHeader) findViewById(R.id.guiheader);
		mGuiMenu = (GUIListMenuListView) findViewById(R.id.guimenu);
		mViewFlow = (PullToRefreshListView) findViewById(R.id.viewflow);
		mViewFlow.getRefreshableView().setSmoothScrollbarEnabled(true);
		mBacktotop = (ImageView) findViewById(R.id.btn_section_backtotop);
		mBacktotop.setOnClickListener(mOnClickListener);
		if (mViewFlow != null) {
			// this may cause error when the activity has left.
			// mSectionAdapter = new AsyncSectionAdapter(this,
			// mOnPullToRefreshListener);
			// mViewFlow.setAdapter(mSectionAdapter, 0);
			mSectionAdapter = new AsyncSectionAdapter(this);
			mViewFlow.setAdapter(mSectionAdapter);
		} else {
			GKIMLog.lf(this, 0, TAG + "=>initializeSection has been NULL.");
		}
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

		mProgressDialog = new GUISimpleLoadingDialog(this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		GKIMLog.lf(this, 0, TAG + "=>onActivityResult.");
		uiHelper.onActivityResult(requestCode, resultCode, data);
		mHasChangedSection = false;
		if (TNPreferenceManager.REQ_CODE_SECTION_2_STORY == requestCode
				&& resultCode == RESULT_OK) {
			if (data != null) {
				String sectionId = data
						.getStringExtra(TNPreferenceManager.EXTRAKEY_BACK_TO_SECTION);
				if (data.hasExtra(TNPreferenceManager.EXTRAKEY_BACK_HAS_BACK)) {
					if (sectionId != null && sectionId.length() > 0
							&& !sectionId.equalsIgnoreCase(mCurrentSectionId)) {
						mCurrentSectionId = sectionId;
						mHasChangedSection = true;
						TNPreferenceManager
								.updateCurrentStandingSectionId(mCurrentSectionId);
					}
				}
				if (data.hasExtra(TNPreferenceManager.EXTRAKEY_BACK_HAS_FAVORITED_CHANGED)) {
					sectionId = data
							.getStringExtra(TNPreferenceManager.EXTRAKEY_OPEN_STORY_FROM_SECTION);
					String storyId = data
							.getStringExtra(TNPreferenceManager.EXTRAKEY_BACK_HAS_STORY_ID);
					boolean bFav = data
							.getBooleanExtra(
									TNPreferenceManager.EXTRAKEY_BACK_HAS_FAVORITED_CHANGED,
									false);
					int sectionIndex = mSectionAdapter
							.getPageIndexById(sectionId);
					if (sectionIndex >= 0) {
						IGenericPage object = mSectionAdapter
								.getItem(sectionIndex);
						if (object instanceof SectionPage) {
							SectionPage page = (SectionPage) object;
							BoxStory bs = page.getBoxStorybyId(storyId);
							if (page != null && bs != null) {
								bs.setFavorite(bFav);
								page.setFavoriteChanged();
								mSectionAdapter.notifyDataSetChanged();
							}
						} else {
							GKIMLog.lf(
									null,
									0,
									TAG
											+ "=> can't favorit on your story page or search page.");
						}
					}
				}
			}
		} else if (TNPreferenceManager.ACTIVITY_RESULT_BACK_FROM_LOGO == resultCode
				|| TNPreferenceManager.ACTIVITY_RESULT_BACK_FROM_LOGO_DETAIL_IMAGE == resultCode) {
			// && (TNPreferenceManager.REQ_CODE_USER_POST == requestCode ||
			// TNPreferenceManager.REQ_CODE_SECTION_2_STORY == requestCode)
			if (data != null) {
				String sectionId = data
						.getStringExtra(TNPreferenceManager.EXTRAKEY_BACK_TO_SECTION);
				if (data.hasExtra(TNPreferenceManager.EXTRAKEY_BACK_HAS_BACK)) {
					if (sectionId != null && sectionId.length() > 0
							&& !sectionId.equalsIgnoreCase(mCurrentSectionId)) {
						mCurrentSectionId = sectionId;
						mHasChangedSection = true;
						TNPreferenceManager
								.updateCurrentStandingSectionId(mCurrentSectionId);
					}
				}
			}
		} else {
			SocialShare provider = SocialHelper.getLastInstance()
					.getSNSInstance();
			if (provider != null) {
				if (!provider.handlingActivityForResult(requestCode,
						resultCode, data)) {
					super.onActivityResult(requestCode, resultCode, data);
				}
			}
		}
		// else {
		// super.onActivityResult(requestCode, resultCode, data);
		// }
	}

	public Handler getHandler() {
		return mHandler;
	}

	private void showGUIListMenu() {
		if (mGuiMenu != null && mGuiMenu.getVisibility() != View.VISIBLE
				&& !mMenuShown) {
			mMenuShown = true;
			mGuiMenu.setVisibility(View.VISIBLE);
			mBacktotop.setVisibility(View.GONE);
			mGuiMenu.startAnimation(mInAnimationFromLeft);
			if (!mTabletVersion) {
				mViewFlow.setVisibility(View.GONE);
				mViewFlow.startAnimation(mOutAnimation2Right);
			}

		}
	}

	protected void hideGUIListMenu() {
		if (mGuiMenu != null && mGuiMenu.getVisibility() == View.VISIBLE) {
			String issueId = mListNewIssue.get(mCurrentSectionId);
			if (mCurrentSectionId
					.equalsIgnoreCase(TNPreferenceManager.EXTRAVALUE_SECTION_USER_PAGE)
					|| mCurrentSectionId
							.equalsIgnoreCase(TNPreferenceManager.EXTRAVALUE_SECTION_USER_PAGE_SAVED)
					|| mCurrentSectionId
							.equalsIgnoreCase(TNPreferenceManager.EXTRAVALUE_SECTION_SEARCH_PAGE)) {
				mBacktotop.setVisibility(View.GONE);
			} else {
				if (issueId != null && !mIssueId.equalsIgnoreCase(issueId)) {
					mBacktotop.setVisibility(View.VISIBLE);
				} else {
					mBacktotop.setVisibility(View.GONE);
				}
			}

			mGuiMenu.setVisibility(View.GONE);
			mGuiMenu.startAnimation(mOutAnimation2Left);
			if (!mTabletVersion) {
				mViewFlow.setVisibility(View.VISIBLE);
				mViewFlow.startAnimation(mInAnimationFromRight);
			}

			InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			if (im != null && im.isActive()) {
				im.hideSoftInputFromWindow(mGuiMenu.getWindowToken(), 0);
			}
		}
	}

	public void setShowBackToTop(boolean bShow) {
		if (bShow) {
			mBacktotop.setVisibility(View.VISIBLE);
		} else {
			mBacktotop.setVisibility(View.GONE);
		}
	}

	protected void startingToUserStoryPage() {
		if (mProgressDialog != null && !mProgressDialog.isShowing()) {
			mProgressDialog.show();
		}
		mSectionAdapter.clear();
		RequestData contentData = RequestDataFactory
				.makeUserPostedStoriesRequest(TNPreferenceManager.getUserId(),
						null, String.valueOf(UIUtils.getDeviceWidth()));
		mTNDownloader.addDownload(true, contentData);
	}

	protected void startingToSearchPage() {
		String strToSearch = TNPreferenceManager.getContentToSearch();
		if (validateSearchString(strToSearch)) {
			mSectionAdapter.clear();
			if (mProgressDialog != null && !mProgressDialog.isShowing()) {
				mProgressDialog.show();
			}
			mTNDownloader.addDownload(RequestDataFactory
					.makeSearchStoriesRequest(TNPreferenceManager.getUserId(),
							null, String.valueOf(UIUtils.getDeviceWidth()),
							strToSearch));
		}
	}

	private boolean validateSearchString(String strSearch) {
		if (strSearch == null || strSearch.length() <= 0) {
			return false;
		}
		if (strSearch.equalsIgnoreCase("eggsonchris2012")) {
			startActivity(new Intent(this, EggingListSectionColor.class));
			return false;
		}
		return true;
	}

	protected void startingToStoredPage() {

		// if (TNPreferenceManager.EXTRAVALUE_SECTION_USER_PAGE_SAVED
		// .equalsIgnoreCase(mCurrentSectionId)) {
		// return;
		// }
		GKIMLog.l(1, TAG + " startingToStoredPage");
		GeneralPage page = TNPreferenceManager.getSavedPage();
		if (page == null) {
			UIUtils.showToast(this,
					getResources().getString(R.string.mystored_page_emptystory));
			GKIMLog.l(1, TAG + " startingToStoredPage : page is nulllllllll");
		} else {
			if (page.getBoxStoryCount() <= 0) {
				UIUtils.showToast(
						this,
						getResources().getString(
								R.string.mystored_page_emptystory));
				GKIMLog.l(
						1,
						TAG
								+ " startingToStoredPage : page.getBoxStoryCount() <= 0");
			} else {
				mCurrentSectionId = TNPreferenceManager.EXTRAVALUE_SECTION_USER_PAGE_SAVED;
				TNPreferenceManager
						.updateCurrentStandingSectionId(mCurrentSectionId);
				mSectionAdapter.clear();
				setGeneralPage(page);
			}
		}
	}

	protected void startingToSectionPage(String id, boolean needRefresh) {

		if (!mProgressDialog.isShowing() && !mUIRefresh) {
			mProgressDialog.show();
		}
		mSectionAdapter.clear();
		if (mTNDownloader != null) {
			mTNDownloader.ExitTask();
		}
		if (TNPreferenceManager.EXTRAVALUE_SECTION_HOME.equals(id)) {
			GKIMLog.l(1, TAG + " startingToSectionPage:1");

			if (!TNPreferenceManager.GPS_GETTINGS) {
				mTNDownloader.addDownload(needRefresh, RequestDataFactory
						.makeIssueRequest(TNPreferenceManager.getUserId(), "",
								String.valueOf(UIUtils.getDeviceWidth()), null,
								null));
			} else {

				if (SplashActivity.mLocation != null) {
					mTNDownloader.addDownload(RequestDataFactory
							.makeIssueHomeGPSWithLocationRequest(
									TNPreferenceManager.getUserId(), "",
									String.valueOf(UIUtils.getDeviceWidth()),
									null, null,
									SplashActivity.mLocation.getLatitude(),
									SplashActivity.mLocation.getLongitude()));
				} else {
					mTNDownloader.addDownload(RequestDataFactory
							.makeIssueHomeGPSWithLocationRequest(
									TNPreferenceManager.getUserId(), "",
									String.valueOf(UIUtils.getDeviceWidth()),
									null, null, -1, -1));

				}
			}

		} else {
			GKIMLog.l(1, TAG + " startingToSectionPage:2");
			mTNDownloader.addDownload(needRefresh, RequestDataFactory
					.makeSectionRequest(TNPreferenceManager.getUserId(), "",
							String.valueOf(UIUtils.getDeviceWidth()), null, id,
							null));
		}

	}

	protected void startingToSectionLocationGPSPage(boolean isCleared,
			String storyId, boolean needRefresh, String direction,
			String lastIssueId) {
		mLocationListStory = storyId;
		if (!mProgressDialog.isShowing() && !mUIRefresh) {
			mProgressDialog.show();
		}
		if (isCleared) {
			mSectionAdapter.clear();
		}
		if (mTNDownloader != null) {
			mTNDownloader.ExitTask();
		}

		if (SplashActivity.mLocation != null) {
			mTNDownloader.addDownload(needRefresh, RequestDataFactory
					.makeSectionLocationGPSRequest(
							TNPreferenceManager.getUserId(), "",
							String.valueOf(UIUtils.getDeviceWidth()), storyId,
							direction, lastIssueId,
							SplashActivity.mLocation.getLatitude(),
							SplashActivity.mLocation.getLongitude()));
		} else {
			mTNDownloader.addDownload(needRefresh, RequestDataFactory
					.makeSectionLocationGPSRequest(
							TNPreferenceManager.getUserId(), "",
							String.valueOf(UIUtils.getDeviceWidth()), storyId,
							direction, lastIssueId, -1, -1));

		}

	}

	private void switchViewFlowBySectionId(String secId) {
		int len = mSectionAdapter.getCount();
		if (mGuiMenu.getVisibility() == View.VISIBLE && len == 1) {
			mGuiMenu.setVisibility(View.GONE);
			mGuiMenu.startAnimation(mOutAnimation2Left);
			mViewFlow.setVisibility(View.VISIBLE);
			mViewFlow.startAnimation(mInAnimationFromRight);
			mMenuShown = false;
		}
		mCurrentSectionId = secId;
		TNPreferenceManager.updateCurrentStandingSectionId(mCurrentSectionId);

		IGenericPage page = null;
		if (len >= 1) {
			page = mSectionAdapter.getItem(0);
			if (!(TNPreferenceManager.EXTRAVALUE_SECTION_HOME
					.equals(mCurrentSectionId)
					|| USER_STORIES_PAGE.equals(mCurrentSectionId)
					|| SEARCH_STORIES_PAGE.equals(mCurrentSectionId) || TNPreferenceManager.EXTRAVALUE_SECTION_USER_PAGE_SAVED
						.equals(mCurrentSectionId))) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put(TNPreferenceManager.EVENT_KEY_SECTION_ID,
						mCurrentSectionId);
				map.put(TNPreferenceManager.EVENT_KEY_SECTION_TITLE,
						page.getSectionTitle());
				// FlurryAgent
				// .onEvent(TNPreferenceManager.EVENT_SECTION_VIEW, map);
				Tracking.sendEvent(TNPreferenceManager.EVENT_SECTION_VIEW, map);
			}
			mStoryIds = page.getBoxStoryIds();

			if (TNPreferenceManager.EXTRAVALUE_SECTION_HOME
					.equals(mCurrentSectionId)) {
				if (TNPreferenceManager.GPS_GETTINGS) {
					if (page.getBoxes() != null && page.getBoxes().length >= 3) {
						String locationStoryId = page.getBoxes()[2]
								.getStoryId();
						if (locationStoryId == null) {
							locationStoryId = "null";
						}
						if (mStoryIds.contains(locationStoryId)) {
							locationStoryId = locationStoryId + ",";
							mStoryIds = mStoryIds.replace(locationStoryId, "");
						}
					}
				}
			}
		}
		if (len > 1) {
			for (int i = 1; i < len; i++) {
				page = mSectionAdapter.getItem(i);
				mStoryIds += "," + page.getBoxStoryIds();
			}
		}

		if (mRequestDownload) {
			autoLoadIssue();
		} else {
			mViewFlow.setMode(Mode.BOTH);
		}

		if (TNPreferenceManager.EXTRAVALUE_SECTION_HOME
				.equals(mCurrentSectionId)) {
			mIsReadyToExist = true;
		} else if (USER_STORIES_PAGE.equals(mCurrentSectionId)
				|| SEARCH_STORIES_PAGE.equals(mCurrentSectionId)
				|| TNPreferenceManager.EXTRAVALUE_SECTION_USER_PAGE_SAVED
						.equals(mCurrentSectionId)) {
			mViewFlow.setMode(Mode.DISABLED);
			mIsReadyToExist = false;
		} else {
			mIsReadyToExist = false;
		}
	}

	protected void startStoryDetailActivity(String storyId, boolean checked) {
		if (storyId == null || storyId.length() == 0) {
			return;
		}
		Intent storyIntent = new Intent();
		// Intent storyIntent = new Intent(this,
		// StoryDetailFragmentActivity.class);
		// Intent storyIntent = new Intent(this, StoryDetailActivity.class);
		// FIXME: start StoryDetail by FragmentActivity
		storyIntent.setClass(this, StoryDetailFragmentActivity.class);
		// if (UIUtils.hasHoneycomb()) {
		// storyIntent.setClass(this, StoryDetailFragmentActivity.class);
		// }else {
		// storyIntent.setClass(this, StoryDetailActivity.class);
		// }
		// storyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		storyIntent.putExtra(TNPreferenceManager.EXTRAKEY_IS_STORY, true);
		storyIntent.putExtra(TNPreferenceManager.EXTRAKEY_STORYID, storyId);
		storyIntent.putExtra(TNPreferenceManager.EXTRAKEY_IS_STORY_CHECKED,
				checked);
		storyIntent.putExtra(
				TNPreferenceManager.EXTRAKEY_OPEN_STORY_FROM_SECTION,
				mCurrentSectionId);
		if (mStoryIds != null) {
			storyIntent.putExtra(
					TNPreferenceManager.EXTRAKEY_STORYIDS_FROM_SECTION,
					mStoryIds);
		}
		this.startActivityForResult(storyIntent,
				TNPreferenceManager.REQ_CODE_SECTION_2_STORY);
	}

	private void addIssue(Issue anIssue, boolean bInitHome) {
		if (mSectionAdapter != null && anIssue != null) {
			GKIMLog.l(1, TAG + " nam nguyen  anIssue.getPageCount() :"
					+ anIssue.getPageCount());
			if (anIssue.getPageCount() > 0) {
				SectionPage page0 = anIssue.getPage(0);
				TNPreferenceManager.setBoxSizeAndGap(page0.getBoxWidth(),
						page0.getGapwidth());

				// //FIXME: note to review
				mStoryIds = page0.getBoxStoryIds();

				// to special location box
				// remove it to list story ids
				if (bInitHome) {
					if (TNPreferenceManager.GPS_GETTINGS) {
						if (page0.getBoxStory(2) != null) {
							String locationStoryId = page0.getBoxStory(2)
									.getStoryId();
							if (locationStoryId == null) {
								locationStoryId = "null";
							}
							if (mStoryIds.contains(locationStoryId)) {
								locationStoryId = locationStoryId + ",";
								mStoryIds = mStoryIds.replace(locationStoryId,
										"");
							}
						}
					}
				}

				mSectionAdapter.addPages(anIssue.getPages());
				// FIXME:Duy fix Scroll top of issue
				if (mSectionAdapter.getCount() == 1) {
					mSectionAdapter.notifyDataSetChanged();
					mViewFlow.setMode(Mode.DISABLED);
				}
				if (page0.getBoxStoryCount() > 0) {
					mRequestDownload = true;
				} else {
					mRequestDownload = false;
				}
			} else {
				mRequestDownload = false;
			}
			// mSectionAdapter.addPages(anIssue.getPages());
			// // FIXME:Duy fix Scroll top of issue
			// if (mSectionAdapter.getCount() == 1) {
			// mSectionAdapter.notifyDataSetChanged();
			// if (mStoryIds != null && mStoryIds.length() > 0) {
			// mRequestDownload = true;
			// } else {
			// mRequestDownload = false;
			// }
			// mViewFlow.setMode(Mode.DISABLED);
			// }

			int len = mSectionAdapter.getCount();
			if (bInitHome && len > 0) {
				// change current section id to first (0) section array item.
				mCurrentSectionId = ((SectionPage) mSectionAdapter.getItem(0))
						.getSectionId();
				TNPreferenceManager
						.updateCurrentStandingSectionId(mCurrentSectionId);
			}
		}
	}

	public void autoLoadIssue() {
		GKIMLog.l(1, TAG + " nam nguyen autoLoadIssue");
		mRequestDownload = false;
		int len = mSectionAdapter.getCount();
		mBoxCountDownloaded = 0;
		if (!TNPreferenceManager.EXTRAVALUE_SECTION_SEARCH_PAGE
				.equals(mCurrentSectionId)
				&& !TNPreferenceManager.EXTRAVALUE_SECTION_USER_PAGE
						.equals(mCurrentSectionId)
				&& !TNPreferenceManager.EXTRAVALUE_SECTION_USER_PAGE_SAVED
						.equals(mCurrentSectionId)) {
			for (int i = 0; i < len; i++) {
				mBoxCountDownloaded += mSectionAdapter.getItem(i)
						.getBoxStoryCount();
			}
			String direction = "-1";
			String lastIssueId = null;
			IGenericPage lastpage = null;
			if (mSectionAdapter != null) {
				lastpage = mSectionAdapter
						.getItem(mSectionAdapter.getCount() - 1);
			}

			if (lastpage != null && lastpage instanceof SectionPage) {
				lastIssueId = ((SectionPage) lastpage).getIssueId();
			}

			if (lastIssueId == null) {
				lastIssueId = mIssueId;
			}
			if (TNPreferenceManager.EXTRAVALUE_SECTION_HOME
					.equals(mCurrentSectionId)) {
				if (mBoxCountDownloaded <= TNPreferenceManager.MIN_COUNT_BOX_SECTION_HOME) {
					mUIRefresh = true;
					if (!TNPreferenceManager.GPS_GETTINGS) {
						mTNDownloader.addDownload(false, RequestDataFactory
								.makeIssueRequest(TNPreferenceManager
										.getUserId(), "", String
										.valueOf(UIUtils.getDeviceWidth()),
										direction, lastIssueId));
					} else {

						if (SplashActivity.mLocation != null) {
							mTNDownloader.addDownload(RequestDataFactory
									.makeIssueHomeGPSWithLocationRequest(
											TNPreferenceManager.getUserId(),
											"", String.valueOf(UIUtils
													.getDeviceWidth()),
											direction, lastIssueId,
											SplashActivity.mLocation
													.getLatitude(),
											SplashActivity.mLocation
													.getLongitude()));
						} else {
							mTNDownloader.addDownload(RequestDataFactory
									.makeIssueHomeGPSWithLocationRequest(
											TNPreferenceManager.getUserId(),
											"", String.valueOf(UIUtils
													.getDeviceWidth()),
											direction, lastIssueId, -1, -1));

						}
					}

					mRequestDownload = true;
				}
			} else {
				if (mBoxCountDownloaded <= TNPreferenceManager.MIN_COUNT_BOX_SECTION_OTHER) {

					if (mLocationListStory != null) {
						startingToSectionLocationGPSPage(false,
								mLocationListStory, true, "1", lastIssueId);
					} else {
						if (mTNDownloader != null) {
							mTNDownloader.addDownload(false, RequestDataFactory
									.makeSectionRequest(TNPreferenceManager
											.getUserId(), "", String
											.valueOf(UIUtils.getDeviceWidth()),
											lastIssueId, mCurrentSectionId,
											direction));
						}
					}

					mRequestDownload = true;
				}
			}
		}
		if (!mRequestDownload) {
			mViewFlow.setMode(Mode.BOTH);
			mSectionAdapter.notifyDataSetInvalidated();
		} else {
			// Nam.nguyen
			// Fixed bug :#4224 App crashed when click title of section
			mSectionAdapter.notifyDataSetChanged();
		}
	}

	private void setGeneralPage(GeneralPage gpage) {
		if (mSectionAdapter != null) {
			if (gpage != null && gpage.getBoxStoryCount() > 0) {
				TNPreferenceManager.setBoxSizeAndGap(gpage.getBoxWidth(),
						gpage.getGapwidth());
				mSectionAdapter.addPage(gpage);
				mSectionAdapter.notifyDataSetChanged();
				switchViewFlowBySectionId(gpage.getSectionId());
				if (mProgressDialog != null && mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
			}
		}
	}

	public void onClick(View v) {
		if (mOnClickListener != null) {
			mOnClickListener.onClick(v);
		}
	}

	public OnClickListener getOnClick() {
		return mOnClickListener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		GKIMLog.l(0, TAG + "=>onBackPressed.");
		mLocationListStory = null;
		if (mGuiMenu.getVisibility() == View.VISIBLE) {
			GKIMLog.lf(this, 0, TAG + "=>onBackPressed. 1");
			mGuiMenu.setVisibility(View.GONE);
			mGuiMenu.startAnimation(mOutAnimation2Left);
			mViewFlow.setVisibility(View.VISIBLE);
			mViewFlow.startAnimation(mInAnimationFromRight);
			mMenuShown = false;
			GKIMLog.lf(null, 0, TAG + "=> " + mGuiMenu.getVisibility() + ", "
					+ mViewFlow.getVisibility());
		} else if (!mIsReadyToExist) {
			GKIMLog.lf(this, 0, TAG + "=>onBackPressed. 2");
			startingToSectionPage(TNPreferenceManager.EXTRAVALUE_SECTION_HOME,
					true);
		} else {
			// super.onBackPressed();
			if (mExitDialog == null) {
				GKIMLog.lf(this, 0, TAG + "=>onBackPressed. 3");
				mExitDialog = new GUIExitDialog(this);
				mExitDialog
						.setConfirmedExit(new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// FlurryAgent.onEvent(TNPreferenceManager.EVENT_END);
								Tracking.sendEvent(
										TNPreferenceManager.EVENT_END, null);
								appExit();
							}
						});
				mExitDialog.show();
			} else {
				GKIMLog.lf(this, 0, TAG + "=>onBackPressed. 4");
				if (!mExitDialog.isShowing()) {
					mExitDialog.show();
				}
			}
		}
	}

	public void appExit() {
		finish();
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(0);
	}


	// Method to stop the service
	public void stopService() {
		stopService(new Intent(getBaseContext(), CatchLowMemoryService.class));
	}



}
