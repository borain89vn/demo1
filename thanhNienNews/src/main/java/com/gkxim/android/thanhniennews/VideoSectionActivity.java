package com.gkxim.android.thanhniennews;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.layout.AsyncVideoHomeAdapter;
import com.gkxim.android.thanhniennews.layout.AsyncVideoSectionAdapter;
import com.gkxim.android.thanhniennews.layout.BoxLayout;
import com.gkxim.android.thanhniennews.layout.EggingListSectionColor;
import com.gkxim.android.thanhniennews.layout.GUIHeaderVideo;
import com.gkxim.android.thanhniennews.layout.GUIListMenuVideoAdapter;
import com.gkxim.android.thanhniennews.layout.GUIListMenuVideoListView;
import com.gkxim.android.thanhniennews.layout.GUISimpleLoadingDialog;
import com.gkxim.android.thanhniennews.layout.VideoHomeBoxLayout;
import com.gkxim.android.thanhniennews.layout.VideoSectionBoxLayout;
import com.gkxim.android.thanhniennews.models.BoxStory;
import com.gkxim.android.thanhniennews.models.GeneralPage;
import com.gkxim.android.thanhniennews.models.GenericResponse;
import com.gkxim.android.thanhniennews.models.IGenericPage;
import com.gkxim.android.thanhniennews.models.ImageThumb;
import com.gkxim.android.thanhniennews.models.Issue;
import com.gkxim.android.thanhniennews.models.SectionPage;
import com.gkxim.android.thanhniennews.models.StoryDetail;
import com.gkxim.android.thanhniennews.models.VideoThumb;
import com.gkxim.android.thanhniennews.networking.RequestDataFactory;
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
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class VideoSectionActivity extends FragmentActivity {

	private static final String TAG = "VideoSectionActivity";
	public static final String USER_STORIES_PAGE = TNPreferenceManager.EXTRAVALUE_SECTION_USER_PAGE;
	public static final String SEARCH_STORIES_PAGE = TNPreferenceManager.EXTRAVALUE_SECTION_SEARCH_PAGE;
	public static int BACKGROUND_COLOR_DETAIL_BOX = 0;
	private static Hashtable<String, String> mListNewIssue = new Hashtable<String, String>();
	private PullToRefreshListView mViewFlow = null;
	private AsyncVideoHomeAdapter mVideoHomeAdapter = null;
	private AsyncVideoSectionAdapter mVideoSectionAdapter = null;
	private GUIHeaderVideo mGuiHeader = null;
	private GUIListMenuVideoListView mGuiMenu = null;
	private String mIssueId = "";
	private Issue mIssue = null;
	private boolean mIssueChanged = false;
	private String mCurrentSectionId = "";
	protected BoxLayout mCurrentBoxLayout = null;
	protected String mStoryIds = null;
	private boolean mHasChangedSection = false;
	protected boolean mMenuShown = false;
	private int mBoxCountDownloaded = 0;
	private boolean mRequestDownload = false;
	private TextView mTextViewBottom;
	private boolean isPullUp;
	// Intens for next activities

	// Nam.nguyen
	private long mTimeTouch = 0;
	private UiLifecycleHelper uiHelper;

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
			case R.id.header_iv_logo:
				hideGUIListMenu();
				// skip user from stressing on logo
				if (mTimeTouch == 0
						|| System.currentTimeMillis() - mTimeTouch > 2 * 1000) {
					mTimeTouch = System.currentTimeMillis();
					mCurrentSectionId = TNPreferenceManager.getMediaSectionId();
					reloadData();
				}
				break;
			case R.id.video_header_ib_home:
			case R.id.menu_list_header_ivhome:
				finishVideo();
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
			case R.id.menu_video_upload:
				hideGUIListMenu();
				break;
			case R.id.menu_in_list:
				if (v instanceof LinearLayout) {

					GUIListMenuVideoAdapter.ViewHolder menuItem = (com.gkxim.android.thanhniennews.layout.GUIListMenuVideoAdapter.ViewHolder) v
							.getTag();
					if (menuItem != null) {
						v.setSelected(true);
						if (menuItem.id
								.equalsIgnoreCase(TNPreferenceManager.VIDEO_OF_YOU_ID)) {
							processVideoOfYou();
						} else {
							startingToVideoSection(menuItem.id);
							hideGUIListMenu();
						}
					} else {
						hideGUIListMenu();
					}
				} else {
					hideGUIListMenu();
				}
				break;
			case R.id.imgv_spring_greetings:
				GKIMLog.lf(null, 0, TAG + "starting imgv_spring_greetings");
				Intent springIntent = new Intent(VideoSectionActivity.this,
						StoryDetailSpringFragmentActivity.class);
				startActivityForResult(springIntent,
						TNPreferenceManager.REQ_CODE_SECTION_2_GREETING);
				break;
			case R.id.imgv_tet_of_you:
				GKIMLog.lf(null, 0, TAG + "starting imgv_tet_of_you");
				Intent tetOfYou = new Intent(VideoSectionActivity.this,
						StoryDetailGalleryActivity.class);
				startActivityForResult(tetOfYou,
						TNPreferenceManager.REQ_CODE_SECTION_2_TETOFYOU);
				break;
			default:
				if (v instanceof VideoHomeBoxLayout) {
					// Nam.nguyen cheat code
					if (mTimeTouch == 0
							|| System.currentTimeMillis() - mTimeTouch > 1 * 1000) {
						mTimeTouch = System.currentTimeMillis();
						VideoHomeBoxLayout bvf = ((VideoHomeBoxLayout) v);
						if (bvf != null) {
							startingToVideoSection(bvf.getSectionId());
						}
					}
				} else if (v instanceof VideoSectionBoxLayout) {
					if (mTimeTouch == 0
							|| System.currentTimeMillis() - mTimeTouch > 1 * 1000) {
						mTimeTouch = System.currentTimeMillis();
						VideoSectionBoxLayout bvf = ((VideoSectionBoxLayout) v);
						if (bvf != null) {
							if (bvf.getBoxStory() != null) {
								VideoSectionActivity.BACKGROUND_COLOR_DETAIL_BOX = bvf
										.getBoxStory().getBackground1Color();
							}

							startStoryDetailActivity(bvf.getStoryId(),
									bvf.getStoryChecked());
						}
					}
				}
				break;
			}
		}
	};

	public void processVideoOfYou() {
		if (TNPreferenceManager.getUserId() == null
				|| TNPreferenceManager.getUserId().length() <= 0) {
			// showDialogRequire();
			if (mGuiMenu != null) {
				mGuiMenu.showLoginDialog();
			} else {
				GKIMLog.l(1, TAG + " View is not init now!");
			}
		} else {
			startingToVideoSection(TNPreferenceManager.VIDEO_OF_YOU_ID);
			hideGUIListMenu();
		}
	}

	public void showDialogRequire() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setMessage(R.string.request_message_dialog);
		alertDialogBuilder.setPositiveButton("Ok",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						if (mGuiMenu != null) {
							mGuiMenu.showLoginDialog();
						} else {
							GKIMLog.l(1, TAG + " View is not init now!");
						}
						arg0.dismiss();
					}
				});
		alertDialogBuilder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}

	private OnRefreshListener2<ListView> mOnPullToRefreshListener = new OnRefreshListener2<ListView>() {
		@Override
		public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
			GKIMLog.lf(null, 0, TAG + "=>onPullDownToRefresh.");
			if (TNPreferenceManager.getMediaSectionId().equalsIgnoreCase(
					mCurrentSectionId)) {
				// mVideoHomeAdapter.clear();
			} else {
				// mVideoSectionAdapter.clear();
			}
			reloadData();
		}

		@Override
		public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
			GKIMLog.lf(null, 0, TAG + "=>onPullUpToRefresh.");
			isPullUp = true;
			String direction = "-1";
			String lastIssueId = null;
			IGenericPage lastpage = null;
			if (mVideoSectionAdapter != null) {
				lastpage = mVideoSectionAdapter.getItem(mVideoSectionAdapter
						.getCount() - 1);
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
				if (TNPreferenceManager.getMediaSectionId().equalsIgnoreCase(
						mCurrentSectionId)) {
					// mVideoHomeAdapter.clear();
					reloadData();
					return;
				}
				if (mCurrentSectionId
						.equalsIgnoreCase(TNPreferenceManager.VIDEO_OF_YOU_ID)) {
					isPullUp = false;
					startingToVideoSection(mCurrentSectionId);
					return;
				}

				if (!mCurrentSectionId
						.equalsIgnoreCase(TNPreferenceManager.EXTRAVALUE_SECTION_HOME)) {
					if (mProgressDialog != null && !mProgressDialog.isShowing()) {
						mProgressDialog.show();
					}
					mTNDownloader.addDownload(false, RequestDataFactory
							.makeSectionRequest(
									TNPreferenceManager.getUserId(), "",
									String.valueOf(UIUtils.getDeviceWidth()),
									lastIssueId, mCurrentSectionId, direction));
				} else {
					if (mTNDownloader != null) {
						if (mProgressDialog != null
								&& !mProgressDialog.isShowing()) {
							mProgressDialog.show();
						}
						mUIRefresh = true;
						mTNDownloader.addDownload(false, RequestDataFactory
								.makeIssueRequest(TNPreferenceManager
										.getUserId(), "", String
										.valueOf(UIUtils.getDeviceWidth()),
										direction, lastIssueId));
					}
				}

				// if
				// (!TNPreferenceManager.getMediaSectionId().equals(sectionId))
				// {
				// GKIMLog.l(1, TAG + " startingTo Video's section: " +
				// sectionId);
				// mVideoSectionAdapter.clear();
				// mViewFlow.setAdapter(mVideoSectionAdapter);
				// mTNDownloader.addDownload(true, RequestDataFactory
				// .makeSectionRequest(TNPreferenceManager.getUserId(), "",
				// String.valueOf(UIUtils.getDeviceWidth()), null,
				// sectionId, null));
				// } else {
				// // un-normal use to return Video's home
				// mVideoHomeAdapter.clear();
				// mViewFlow.setAdapter(mVideoHomeAdapter);
				// mTNDownloader.addDownload(RequestDataFactory
				// .makeVideoHomeSectionRequest(
				// TNPreferenceManager.getUserId(), null, null));
				// }
				//

			}
		}
	};

	private void checkCurrentOver15Story() {
		GKIMLog.lf(null, 0, TAG + "=>onPullUpToRefresh.");
		isPullUp = true;
		String direction = "-1";
		String lastIssueId = null;
		IGenericPage lastpage = null;
		if (mVideoSectionAdapter != null) {
			lastpage = mVideoSectionAdapter.getItem(mVideoSectionAdapter
					.getCount() - 1);
		}

		if (lastpage != null && lastpage instanceof SectionPage) {
			lastIssueId = ((SectionPage) lastpage).getIssueId();
		}

		if (lastIssueId == null) {
			lastIssueId = mIssueId;
		}
		if (mCurrentSectionId != null) {
			if (TNPreferenceManager.getMediaSectionId().equalsIgnoreCase(
					mCurrentSectionId)) {
				reloadData();
				return;
			}
			if (mCurrentSectionId
					.equalsIgnoreCase(TNPreferenceManager.VIDEO_OF_YOU_ID)) {
				isPullUp = false;
				startingToVideoSection(mCurrentSectionId);
				return;
			}

			if (!mCurrentSectionId
					.equalsIgnoreCase(TNPreferenceManager.EXTRAVALUE_SECTION_HOME)) {
				if (mProgressDialog != null && !mProgressDialog.isShowing()) {
					mProgressDialog.show();
				}
				mTNDownloader.addDownload(false, RequestDataFactory
						.makeSectionRequest(TNPreferenceManager.getUserId(),
								"", String.valueOf(UIUtils.getDeviceWidth()),
								lastIssueId, mCurrentSectionId, direction));
			} else {
				if (mTNDownloader != null) {
					if (mProgressDialog != null && !mProgressDialog.isShowing()) {
						mProgressDialog.show();
					}
					mUIRefresh = true;
					mTNDownloader.addDownload(false, RequestDataFactory
							.makeIssueRequest(TNPreferenceManager.getUserId(),
									"",
									String.valueOf(UIUtils.getDeviceWidth()),
									direction, lastIssueId));
				}
			}

		}
	}

	private DataDownloader mTNDownloader = new DataDownloader(
			new OnDownloadCompletedListener() {
				@Override
				public void onCompleted(Object key, String result) {
					RequestData contentKey = (RequestData) key;
					GKIMLog.lf(null, 0, TAG + "=>onCompleted: " + key);
					if (result == null || result.length() <= 0) {
						if (!TNPreferenceManager.isConnectionAvailable()) {
							UIUtils.showToast(
									null,
									VideoSectionActivity.this
											.getResources()
											.getString(
													R.string.close_application_no_connection));
						}

						if (mVideoHomeAdapter != null) {
							mVideoHomeAdapter.clear();
							mVideoHomeAdapter.notifyDataSetInvalidated();
						}

						if (mVideoSectionAdapter != null) {
							mVideoSectionAdapter.clear();
							mVideoSectionAdapter.notifyDataSetInvalidated();

							UIUtils.showToast(
									VideoSectionActivity.this,
									getResources().getString(
											R.string.section_is_no_data));
						}
						mRequestDownload = false;
						// Nam.Nguyen
						if (mViewFlow != null) {
							mViewFlow.onRefreshComplete();
						}

						if (mProgressDialog != null
								&& mProgressDialog.isShowing()) {
							mProgressDialog.dismiss();
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
					if (type == RequestDataFactory.DATA_JSON_DEF_REQUESTTYPE_DATA_VIDEO_HOME) {
						if (mProgressDialog != null
								&& mProgressDialog.isShowing()) {
							mProgressDialog.dismiss();
						}

						mVideoHomeAdapter.clear();
						if (mViewFlow != null) {
							mViewFlow.setMode(Mode.PULL_FROM_START);
						}
						SectionPage.SECTIONPAGE_VIDEO = true;
						Gson gson = new GsonBuilder().registerTypeAdapter(
								Issue.class, new Issue.IssueConverter())
								.create();
						Issue anIssue = gson.fromJson(result, Issue.class);
						SectionPage.SECTIONPAGE_VIDEO = false;
						if (anIssue != null) {
							addIssue(anIssue, true);
							switchViewFlowBySectionId(TNPreferenceManager
									.getMediaSectionId());
							if (mVideoHomeAdapter.getCount() == 0) {
								mIssueId = anIssue.getPage(0).getIssueId();
							}
							if (!bCheckCache || contentKey.forceUpdate) {
								TNPreferenceManager.addOrUpdateCache(theUrl,
										result, keyCacher);
							}
						} else {
							mRequestDownload = false;
							UIUtils.showToast(
									VideoSectionActivity.this,
									getResources().getString(
											R.string.section_is_not_available));
							GKIMLog.lf(null, 4, TAG
									+ "=> failed to load section from url: "
									+ key.toString());
						}
						mVideoHomeAdapter.notifyDataSetChanged();
					} else if (type == RequestDataFactory.DATA_JSON_DEF_REQUESTTYPE_DATA_BOXES_SECTION) {
						GKIMLog.l(
								1,
								TAG
										+ " DATA_JSON_DEF_REQUESTTYPE_DATA_BOXES_SECTION:"
										+ result);
						if (mVideoSectionAdapter != null) {
							if (!isPullUp) {
								mVideoSectionAdapter.clear();
							} else {
								isPullUp = false;
							}
						}
						if (mViewFlow != null) {
							mViewFlow.setMode(Mode.BOTH);
						}
						Gson gson = new GsonBuilder().registerTypeAdapter(
								Issue.class, new Issue.IssueConverter())
								.create();
						Issue anIssue = gson.fromJson(result, Issue.class);
						if (anIssue != null) {
							if (!bCheckCache || contentKey.forceUpdate) {
								TNPreferenceManager.addOrUpdateCache(theUrl,
										result, keyCacher);
							}
							String secId = RequestDataFactory
									.getSectionId((RequestData) key);
							if (secId == null) {
								if (mCurrentSectionId != null
										&& mCurrentSectionId
												.equalsIgnoreCase(TNPreferenceManager.VIDEO_OF_YOU_ID)) {
									GKIMLog.lf(
											null,
											0,
											TAG
													+ "=> current section is video cua ban.");
									if (anIssue.getPage(0) != null) {
										anIssue.getPage(0).setSectionId(
												mCurrentSectionId);
										anIssue.getPage(0).setSectionTitle(
												getResources().getString(
														R.string.video_of_you));
									}
									secId = mCurrentSectionId;
								} else {
									GKIMLog.lf(null, 0, TAG
											+ "=> section Null to refresh.");
									return;
								}
							}
							if (mVideoSectionAdapter.getCount() == 0) {
								mIssueId = anIssue.getPage(0).getIssueId();
							}
							if (!bCheckCache || contentKey.forceUpdate) {
								TNPreferenceManager.addOrUpdateCache(theUrl,
										result, keyCacher);
							}
							addIssue(anIssue, false);
							switchViewFlowBySectionId(secId);

							mVideoSectionAdapter.notifyDataSetChanged();

							if (!mVideoSectionAdapter.isOver()) {
								checkCurrentOver15Story();
							} else {
								if (mProgressDialog != null
										&& mProgressDialog.isShowing()) {
									mProgressDialog.dismiss();
								}
							}
						} else {
							mRequestDownload = false;
							UIUtils.showToast(
									VideoSectionActivity.this,
									getResources().getString(
											R.string.section_is_not_available));
							GKIMLog.lf(null, 4, TAG
									+ "=> failed to load section from url: "
									+ key.toString());
							mVideoSectionAdapter.notifyDataSetChanged();
							if (mProgressDialog != null
									&& mProgressDialog.isShowing()) {
								mProgressDialog.dismiss();
							}
						}

					} else if (type == RequestDataFactory.DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_DETAIL) {
						if (mProgressDialog != null
								&& mProgressDialog.isShowing()) {
							mProgressDialog.dismiss();
						}
						GKIMLog.lf(null, 0, TAG
								+ "=> process for story's detail.");
						Gson gson = new GsonBuilder().registerTypeAdapter(
								GenericResponse.class,
								new GenericResponse.GenericResponseConverter())
								.create();
						GenericResponse gres = gson.fromJson(result,
								GenericResponse.class);
						boolean hasImages = false;
						boolean hasVideos = false;
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
									hasVideos = true;
									GKIMLog.lf(
											null,
											0,
											TAG + "=>story id: "
													+ sd.getStoryid()
													+ ", has videos: "
													+ jsVideos.size());
								}
								if (mStoryId.equalsIgnoreCase(String.valueOf(sd
										.getStoryid()))) {
									mStoryDetail = sd;
								}
							}
						}
						if (mStoryDetail != null) {
							if (hasImages || hasVideos) {
								setStoryImagesHSViews(
										mStoryDetail.getJaImages(),
										mStoryDetail.getJaVideos());
							}
						}
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
				} else if (mVideoHomeAdapter != null) {
					mVideoHomeAdapter.notifyDataSetChanged();
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
	private boolean mTabletVersion = false;

	private Intent mImageReviewIntent;
	public String mStoryId;
	private StoryDetail mStoryDetail = null;

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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
		setContentView(R.layout.activity_section_video);
		mTextViewBottom = (TextView) findViewById(R.id.txt_bottom);

		// DragImageView ivAr = (DragImageView)
		// findViewById(R.id.imv_section_ar);
		// if (ivAr != null) {
		// if (ARiseHelpMethods
		// .checkARSupportedForDevice(getApplicationContext())) {
		// GKIMLog.lf(this, 0, TAG
		// + "=>AR SDK has support for this device");
		// DisplayMetrics dm = new DisplayMetrics();
		// getWindowManager().getDefaultDisplay().getMetrics(dm);
		// RelativeLayout.LayoutParams lp =
		// (android.widget.RelativeLayout.LayoutParams) ivAr
		// .getLayoutParams();
		// float xfrac = 0.75f;
		// float yfrac = 0.28f;
		// try {
		// xfrac = getResources().getFraction(
		// R.dimen.section_ar_pos_xfractor, 1, 1);
		// yfrac = getResources().getFraction(
		// R.dimen.section_ar_pos_yfractor, 1, 1);
		// GKIMLog.lf(this, 0, TAG + "=> fractors: " + xfrac + ", "
		// + yfrac);
		// } catch (Exception e) {
		// GKIMLog.lf(this, 4, TAG + "=>failed to load AR fractions");
		// }
		// lp.leftMargin = (int) (dm.widthPixels * xfrac);
		// lp.topMargin = (int) (dm.heightPixels * yfrac);
		// ivAr.setLayoutParams(lp);
		// ivAr.setOnTouchListener(ivAr);
		// ivAr.setOnClickListener(mOnClickListener);
		// GKIMLog.lf(this, 0, TAG + "=>size: [" + ivAr.getWidth() + ", "
		// + ivAr.getHeight() + "], position: [" + lp.leftMargin
		// + ", " + lp.topMargin + "]");
		// } else {
		// ivAr.setVisibility(View.GONE);
		// GKIMLog.lf(this, 0, TAG
		// + "=>AR SDK is not support for this device");
		// }
		// }

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

		// load data
		Intent intent = getIntent();
		boolean bBackFromChild = false;
		String strBackToSection = TNPreferenceManager.getMediaSectionId();
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
			// XXX: review for Rotate
			// if (savedInstanceState
			// .containsKey(TNPreferenceManager.EXTRAKEY_ROTATE_ISSUE_ID)) {
			// savedIssueId = savedInstanceState
			// .getString(TNPreferenceManager.EXTRAKEY_ROTATE_ISSUE_ID);
			// }
			// if (bBackFromChild && savedSectionId == null) {
			// savedSectionId = strBackToSection;
			// }

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
				// TNPreferenceManager
				// .updateCurrentStandingSectionId(mCurrentSectionId);

				if (TNPreferenceManager.getMediaSectionId().equalsIgnoreCase(
						savedSectionId)) {
					startingToVideoSection(savedSectionId);

				}
				// else if (TNPreferenceManager.EXTRAVALUE_SECTION_SEARCH_PAGE
				// .equalsIgnoreCase(savedSectionId)) {
				// String strToSearch = TNPreferenceManager
				// .getContentToSearch();
				// if (validateSearchString(strToSearch)) {
				// if (!mProgressDialog.isShowing()) {
				// mProgressDialog.show();
				// }
				// mTNDownloader.addDownload(RequestDataFactory
				// .makeSearchStoriesRequest(TNPreferenceManager
				// .getUserId(), null, String
				// .valueOf(UIUtils.getDeviceWidth()),
				// strToSearch));
				// }
				// } else if
				// (TNPreferenceManager.EXTRAVALUE_SECTION_USER_PAGE_SAVED
				// .equalsIgnoreCase(savedSectionId)) {
				// mCurrentSectionId = savedSectionId;
				// TNPreferenceManager
				// .updateCurrentStandingSectionId(mCurrentSectionId);
				// }
				else if (TNPreferenceManager.EXTRAVALUE_SECTION_USER_PAGE_VIDEO
						.equalsIgnoreCase(savedSectionId)) {
					// TODO: request for User's video
					// mTNDownloader.addDownload(RequestDataFactory
					// .makeUserPostedStoriesRequest(
					// TNPreferenceManager.getUserId(), null,
					// String.valueOf(UIUtils.getDeviceWidth())));
				} else {
					startingToVideoSection(savedSectionId);
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
					// Intent intentStoryDetail = new Intent(this,
					// StoryDetailFragmentActivity.class);
					// intentStoryDetail.putExtra(
					// TNPreferenceManager.EXTRAKEY_IS_STORY, true);
					// intentStoryDetail.putExtra(
					// TNPreferenceManager.EXTRAKEY_STORYID, storiId);
					// intentStoryDetail.putExtra(
					// TNPreferenceManager.EXTRAKEY_IS_STORY_CHECKED,
					// false);
					// intentStoryDetail
					// .putExtra(
					// TNPreferenceManager.EXTRAKEY_OPEN_STORY_FROM_SECTION,
					// TNPreferenceManager.EXTRAVALUE_SECTION_HOME);
					// intentStoryDetail.putExtra(
					// TNPreferenceManager.EXTRAKEY_OPEN_STORY_FROM_PNS,
					// true);
					// this.startActivityForResult(intentStoryDetail,
					// TNPreferenceManager.REQ_CODE_SECTION_2_STORY);
					// // FlurryAgent
					// // .onEvent(TNPreferenceManager.EVENT_XTIFY_PNS_TOUCHED);
					// Tracking.sendEvent(
					// TNPreferenceManager.EVENT_XTIFY_PNS_TOUCHED, null);
				}
			} else {
				if (mTNDownloader != null) {
					if (mProgressDialog != null && !mProgressDialog.isShowing()) {
						mProgressDialog.show();
					}
					GKIMLog.lf(this, 0, TAG + "=> requesting for new layout.");
					mUIRefresh = true;
					mIssueChanged = true;
					mCurrentSectionId = TNPreferenceManager.getMediaSectionId();
					// TNPreferenceManager
					// .updateCurrentStandingSectionId(mCurrentSectionId);
					startingToVideoSection(mCurrentSectionId);
				}
			}
		}
	}

	public void reloadData() {
		// Clear Adapter
		if (mCurrentSectionId != null) {
			GKIMLog.lf(this, 1, TAG + "=>onCreate, but may be from rotation");
			// Or back from StoryDetailFragmentActivity child through PNS
			if (mTNDownloader != null) {
				GKIMLog.lf(this, 0, TAG + "=> requesting for new layout.");
				mUIRefresh = true;
				mIssueChanged = true;
				startingToVideoSection(mCurrentSectionId);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		GKIMLog.lf(this, 1, TAG + "=>onConfigurationChanged.");

		if (mTNDownloader != null) {
			mTNDownloader.setExitTasksEarly(true);
			mTNDownloader = null;
		}

		if (mVideoHomeAdapter != null) {
			mVideoHomeAdapter.clear();
			mVideoHomeAdapter = null;
		}
		if (mVideoSectionAdapter != null) {
			mVideoSectionAdapter.clear();
			mVideoSectionAdapter = null;
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
			if (mVideoHomeAdapter != null) {
				if (mVideoHomeAdapter != null
						&& mVideoHomeAdapter.getCount() > 0) {
					mVideoHomeAdapter.clear();
				}
			}
			if (mVideoSectionAdapter != null) {
				if (mVideoSectionAdapter != null
						&& mVideoSectionAdapter.getCount() > 0) {
					mVideoSectionAdapter.clear();
				}
			}
			if (mIssue != null) {
				addIssue(mIssue, true);
			}
		}
		// if (!ApplicationRating.isShowing()) {
		// ApplicationRating.checkForRating(this);
		// }
	}

	@Override
	protected void onResume() {
		GKIMLog.lf(this, 1, TAG + "=>onResume: " + mCurrentSectionId);
		TNPreferenceManager.updateCurrentStandingSectionId(mCurrentSectionId);
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
			mViewFlow.requestFocus();
		}
		if (mHasChangedSection) {
			if (TNPreferenceManager.getMediaSectionId().equalsIgnoreCase(
					mCurrentSectionId)) {
				if (mVideoSectionAdapter != null) {
					mVideoSectionAdapter.clear();
				}
				if (mVideoHomeAdapter == null
						|| mVideoHomeAdapter.getCount() <= 0) {
					startingToVideoSection(TNPreferenceManager
							.getMediaSectionId());
				} else {
					changeToHomeWithoutDownload(TNPreferenceManager
							.getMediaSectionId());
				}
			} else {
				startingToVideoSection(mCurrentSectionId);
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
			if (mVideoSectionAdapter != null) {
				mVideoSectionAdapter.notifyDataSetChanged();
			} else {
				GKIMLog.l(1, "Call reloadData");
				reloadData();
			}
		}
		uiHelper.onResume();
		super.onResume();
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
		Tracking.endSeesion(this);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		GKIMLog.lf(this, 0, TAG + "=>onDestroy.");
		uiHelper.onDestroy();
		super.onDestroy();

	}

	private void initLayout() {
		GKIMLog.lf(this, 0, TAG + "=>initLayout.");
		getWindow().setFormat(PixelFormat.RGBA_8888);
		mGuiHeader = (GUIHeaderVideo) findViewById(R.id.guiheader);
		mGuiMenu = (GUIListMenuVideoListView) findViewById(R.id.guimenu);

		mViewFlow = (PullToRefreshListView) findViewById(R.id.viewflow);
		mViewFlow.getRefreshableView().setSmoothScrollbarEnabled(true);
		if (mViewFlow != null) {
			mVideoHomeAdapter = new AsyncVideoHomeAdapter(this);
			mViewFlow.setAdapter(mVideoHomeAdapter);

			// also prepare for the section's adapter.
			mVideoSectionAdapter = new AsyncVideoSectionAdapter(this);
		} else {
			GKIMLog.lf(this, 0, TAG + "=>initializeSection has been NULL.");
		}
		mOutAnimation2Left = AnimationUtils.loadAnimation(this,
				R.anim.push_left_out);
		mOutAnimation2Left.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
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
					if (mVideoSectionAdapter != null) {
						int sectionIndex = mVideoSectionAdapter
								.getPageIndexById(sectionId);
						if (sectionIndex >= 0) {
							IGenericPage object = mVideoSectionAdapter
									.getItem(sectionIndex);
							if (object instanceof SectionPage) {
								SectionPage page = (SectionPage) object;
								BoxStory bs = page.getBoxStorybyId(storyId);
								if (page != null && bs != null) {
									bs.setFavorite(bFav);
									page.setFavoriteChanged();
									mVideoSectionAdapter.notifyDataSetChanged();
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
			}
		} else if (TNPreferenceManager.ACTIVITY_RESULT_BACK_FROM_LOGO == resultCode
				|| TNPreferenceManager.ACTIVITY_RESULT_BACK_FROM_LOGO_DETAIL_IMAGE == resultCode) {
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
	}

	public Handler getHandler() {
		return mHandler;
	}

	private void showGUIListMenu() {
		if (mGuiMenu != null && mGuiMenu.getVisibility() != View.VISIBLE
				&& !mMenuShown) {
			if (!mTabletVersion) {
				if (mTextViewBottom != null) {
					mTextViewBottom.setVisibility(View.GONE);
				}
			}
			mMenuShown = true;
			mGuiMenu.setVisibility(View.VISIBLE);
			mGuiMenu.startAnimation(mInAnimationFromLeft);
			if (!mTabletVersion) {
				mViewFlow.setVisibility(View.GONE);
				mViewFlow.startAnimation(mOutAnimation2Right);

			}

		}
	}

	protected void hideGUIListMenu() {
		if (mGuiMenu != null && mGuiMenu.getVisibility() == View.VISIBLE) {
			if (!mTabletVersion
					&& mCurrentSectionId.equalsIgnoreCase(TNPreferenceManager
							.getMediaSectionId())) {
				if (mTextViewBottom != null) {
					mTextViewBottom.setVisibility(View.VISIBLE);
				}
			}
			// String issueId = mListNewIssue.get(mCurrentSectionId);
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

	protected void startingToUserStoryPage() {
		// TODO: implementation for user's video page.
		// if (mProgressDialog != null && !mProgressDialog.isShowing()) {
		// mProgressDialog.show();
		// }
		// mSectionAdapter.clear();
		// RequestData contentData = RequestDataFactory
		// .makeUserPostedStoriesRequest(TNPreferenceManager.getUserId(),
		// null, String.valueOf(UIUtils.getDeviceWidth()));
		// mTNDownloader.addDownload(true, contentData);
	}

	protected void startingToSearchPage() {
		// TODO: implementation for Video's search page
		// String strToSearch = TNPreferenceManager.getContentToSearch();
		// if (validateSearchString(strToSearch)) {
		// mSectionAdapter.clear();
		// if (mProgressDialog != null && !mProgressDialog.isShowing()) {
		// mProgressDialog.show();
		// }
		// mTNDownloader.addDownload(RequestDataFactory
		// .makeSearchStoriesRequest(TNPreferenceManager.getUserId(),
		// null, String.valueOf(UIUtils.getDeviceWidth()),
		// strToSearch));
		// }
	}

	@Deprecated
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
		GKIMLog.l(1, TAG + " startingToStoredPage");
		// GeneralPage page = TNPreferenceManager.getSavedPage();
		// if (page == null) {
		// UIUtils.showToast(this,
		// getResources().getString(R.string.mystored_page_emptystory));
		// GKIMLog.l(1, TAG + " startingToStoredPage : page is nulllllllll");
		// } else {
		// if (page.getBoxStoryCount() <= 0) {
		// UIUtils.showToast(
		// this,
		// getResources().getString(
		// R.string.mystored_page_emptystory));
		// GKIMLog.l(
		// 1,
		// TAG
		// + " startingToStoredPage : page.getBoxStoryCount() <= 0");
		// } else {
		// mCurrentSectionId =
		// TNPreferenceManager.EXTRAVALUE_SECTION_USER_PAGE_SAVED;
		// TNPreferenceManager
		// .updateCurrentStandingSectionId(mCurrentSectionId);
		// mVideoHomeAdapter.clear();
		// setGeneralPage(page);
		// }
		// }
	}

	@Deprecated
	private void startingToSectionPage(String id, boolean needRefresh) {

		if (!mProgressDialog.isShowing() && !mUIRefresh) {
			mProgressDialog.show();
		}
		if (mTNDownloader != null) {
			mTNDownloader.ExitTask();
		}

		mCurrentSectionId = id;

		if (TNPreferenceManager.getMediaSectionId().equals(id)) {
			GKIMLog.l(1, TAG + " startingToSectionPage video's home");
			mViewFlow.setAdapter(mVideoHomeAdapter);
			mTNDownloader.addDownload(needRefresh, RequestDataFactory
					.makeVideoHomeSectionRequest(
							TNPreferenceManager.getUserId(), "",
							String.valueOf(UIUtils.getDeviceWidth())));
		} else {
			GKIMLog.l(1, TAG + " startingToSectionPage - section: " + id);
			mViewFlow.setAdapter(mVideoSectionAdapter);
			mTNDownloader.addDownload(needRefresh, RequestDataFactory
					.makeSectionRequest(TNPreferenceManager.getUserId(), "",
							String.valueOf(UIUtils.getDeviceWidth()), null, id,
							null));
		}

	}

	private void switchViewFlowBySectionId(String secId) {
		int len = mVideoHomeAdapter.getCount();
		if (mGuiMenu.getVisibility() == View.VISIBLE && len == 1) {
			mGuiMenu.setVisibility(View.GONE);
			mGuiMenu.startAnimation(mOutAnimation2Left);
			mViewFlow.setVisibility(View.VISIBLE);
			mViewFlow.startAnimation(mInAnimationFromRight);
			mMenuShown = false;
		}
		mCurrentSectionId = secId;
		TNPreferenceManager.updateCurrentStandingSectionId(mCurrentSectionId);
		BaseAdapter ba = null;
		if (TNPreferenceManager.getMediaSectionId().equalsIgnoreCase(
				mCurrentSectionId)) {
			ba = (BaseAdapter) mVideoHomeAdapter;
		} else {
			ba = (BaseAdapter) mVideoSectionAdapter;
		}

		IGenericPage page = null;
		if (ba != null) {
			len = ba.getCount();
		}
		if (len >= 1) {
			page = (IGenericPage) ba.getItem(0);
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
				Tracking.sendEvent(TNPreferenceManager.EVENT_SECTION_VIEW, map);
			}
			mStoryIds = page.getBoxStoryIds();
		}
		if (len > 1) {
			for (int i = 1; i < len; i++) {
				page = (IGenericPage) ba.getItem(i);
				mStoryIds += "," + page.getBoxStoryIds();
			}
		}

		// Auto load previous issues.
		// if (mRequestDownload) {
		// autoLoadIssue();
		// } else {
		// mViewFlow.setMode(Mode.PULL_FROM_START);
		// }
	}

	protected void startStoryDetailActivity(String storyId, boolean checked) {
		mIssueChanged = false;
		if (storyId == null || storyId.length() == 0) {
			return;
		}
		Intent storyIntent = new Intent();
		storyIntent.setClass(this, VideoStoryDetailFragmentActivity.class);
		storyIntent.putExtra(TNPreferenceManager.EXTRAKEY_IS_STORY, true);
		storyIntent.putExtra(TNPreferenceManager.EXTRAKEY_STORYID, storyId);
		storyIntent.putExtra(
				TNPreferenceManager.EXTRAKEY_STORY_FROM_VIDEO_SECTION,
				VideoStoryDetailFragmentActivity.EXTRAKEY_STORY_TYPE_VIDEO);
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
		// TODO get section Id when "adding"
		String pageSecId = mCurrentSectionId;
		if (anIssue != null && anIssue.getPageCount() > 0) {
			SectionPage page0 = anIssue.getPage(0);
			pageSecId = page0.getSectionId();
			// //FIXME: note to review, all will be 0 or null in Video Home
			mStoryIds = page0.getBoxStoryIds();
		} else {
			mRequestDownload = false;
		}
		if (TNPreferenceManager.getMediaSectionId().equalsIgnoreCase(pageSecId)) {
			if (mVideoHomeAdapter != null && anIssue != null) {
				mVideoHomeAdapter.addPages(anIssue.getPages());
				int len = mVideoHomeAdapter.getCount();
				if (bInitHome && len > 0) {
					// change current section id to first (0) section array
					// item.
					mCurrentSectionId = ((SectionPage) mVideoHomeAdapter
							.getItem(0)).getSectionId();

				}
			}
		} else {
			if (mVideoSectionAdapter != null && anIssue != null) {
				mVideoSectionAdapter.addPages(anIssue.getPages());
				int len = mVideoSectionAdapter.getCount();
				if (bInitHome && len > 0) {
					// change current section id to first (0) section array
					// item.
					mCurrentSectionId = ((SectionPage) mVideoSectionAdapter
							.getItem(0)).getSectionId();

				}
			}
		}
		TNPreferenceManager.updateCurrentStandingSectionId(mCurrentSectionId);
	}

	@Deprecated
	private void autoLoadIssue() {

		BaseAdapter ba = null;
		if (TNPreferenceManager.getMediaSectionId().equals(mCurrentSectionId)) {
			ba = mVideoHomeAdapter;
		} else {
			ba = mVideoSectionAdapter;
		}

		mRequestDownload = false;
		int len = ba.getCount();
		mBoxCountDownloaded = 0;
		if (!TNPreferenceManager.EXTRAVALUE_SECTION_SEARCH_PAGE
				.equals(mCurrentSectionId)
				&& !TNPreferenceManager.EXTRAVALUE_SECTION_USER_PAGE
						.equals(mCurrentSectionId)
				&& !TNPreferenceManager.EXTRAVALUE_SECTION_USER_PAGE_SAVED
						.equals(mCurrentSectionId)) {
			for (int i = 0; i < len; i++) {
				mBoxCountDownloaded += ((SectionPage) ba.getItem(i))
						.getBoxStoryCount();
			}
			String direction = "-1";
			String lastIssueId = null;
			IGenericPage lastpage = (IGenericPage) ba.getItem(len - 1);
			if (lastpage != null && lastpage instanceof SectionPage) {
				lastIssueId = ((SectionPage) lastpage).getIssueId();
			}

			if (lastIssueId == null) {
				lastIssueId = mIssueId;
			}
			if (TNPreferenceManager.getMediaSectionId().equals(
					mCurrentSectionId)) {
				if (mBoxCountDownloaded <= TNPreferenceManager.MIN_COUNT_BOX_SECTION_HOME) {
					mUIRefresh = true;
					mViewFlow.setAdapter(mVideoHomeAdapter);
					mTNDownloader.addDownload(false, RequestDataFactory
							.makeIssueRequest(TNPreferenceManager.getUserId(),
									"",
									String.valueOf(UIUtils.getDeviceWidth()),
									direction, lastIssueId));
					mRequestDownload = true;
				}
			} else {
				if (mBoxCountDownloaded <= TNPreferenceManager.MIN_COUNT_BOX_SECTION_OTHER) {
					if (mTNDownloader != null) {
						mViewFlow.setAdapter(mVideoSectionAdapter);
						mTNDownloader.addDownload(false, RequestDataFactory
								.makeSectionRequest(TNPreferenceManager
										.getUserId(), "", String
										.valueOf(UIUtils.getDeviceWidth()),
										lastIssueId, mCurrentSectionId,
										direction));
					}
					mRequestDownload = true;
				}
			}
		}
		if (!mRequestDownload) {
			ba.notifyDataSetInvalidated();
		} else {
			// Nam.nguyen
			// Fixed bug :#4224 App crashed when click title of section
			ba.notifyDataSetChanged();
		}
	}

	private void setGeneralPage(GeneralPage gpage) {
		if (mVideoHomeAdapter != null) {
			if (gpage != null && gpage.getBoxStoryCount() > 0) {
				TNPreferenceManager.setBoxSizeAndGap(gpage.getBoxWidth(),
						gpage.getGapwidth());
				mVideoHomeAdapter.addPage(gpage);
				mVideoHomeAdapter.notifyDataSetChanged();
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
		if (mGuiMenu.getVisibility() == View.VISIBLE) {
			GKIMLog.lf(this, 0, TAG + "=>onBackPressed. 1");
			mGuiMenu.setVisibility(View.GONE);
			mGuiMenu.startAnimation(mOutAnimation2Left);
			mViewFlow.setVisibility(View.VISIBLE);
			mViewFlow.startAnimation(mInAnimationFromRight);
			mMenuShown = false;
			GKIMLog.lf(null, 0, TAG + "=> " + mGuiMenu.getVisibility() + ", "
					+ mViewFlow.getVisibility());
		} else if (!TNPreferenceManager.getMediaSectionId().equalsIgnoreCase(
				mCurrentSectionId)) {
			if (mVideoHomeAdapter == null || mVideoHomeAdapter.getCount() <= 0) {
				startingToVideoSection(TNPreferenceManager.getMediaSectionId());
			} else {
				changeToHomeWithoutDownload(TNPreferenceManager
						.getMediaSectionId());
			}
		} else {
			finishVideo();
		}
	}

	public void finishVideo() {
		hideGUIListMenu();
		finish();
	}

	/**
	 * @param id
	 */
	protected void startingToVideoSection(String sectionId) {
		// TODO: Reviewing
		if (!mProgressDialog.isShowing() && !mUIRefresh) {
			mProgressDialog.show();
		}
		if (mTNDownloader != null) {
			mTNDownloader.ExitTask();
		}
		mCurrentSectionId = sectionId;

		if (!TNPreferenceManager.getMediaSectionId().equals(sectionId)) {
			GKIMLog.l(1, TAG + " startingTo Video's section: " + sectionId);
			mViewFlow.setAdapter(mVideoSectionAdapter);
			if (sectionId.equalsIgnoreCase(TNPreferenceManager.VIDEO_OF_YOU_ID)) {
				mTNDownloader.addDownload(true, RequestDataFactory
						.makeVideoOfYouRequest(TNPreferenceManager.getUserId(),
								"", String.valueOf(UIUtils.getDeviceWidth()),
								null, sectionId, null));
			} else {
				mTNDownloader.addDownload(true, RequestDataFactory
						.makeSectionRequest(TNPreferenceManager.getUserId(),
								"", String.valueOf(UIUtils.getDeviceWidth()),
								null, sectionId, null));
			}
			if (mTextViewBottom != null) {
				mTextViewBottom.setVisibility(View.GONE);
			}
		} else {
			// un-normal use to return Video's home

			mViewFlow.setAdapter(mVideoHomeAdapter);
			mTNDownloader.addDownload(RequestDataFactory
					.makeVideoHomeSectionRequest(
							TNPreferenceManager.getUserId(), null, null));
			if (mTextViewBottom != null) {
				mTextViewBottom.setVisibility(View.VISIBLE);
			}
		}
	}

	private void changeToHomeWithoutDownload(String sectionId) {
		mCurrentSectionId = sectionId;
		mViewFlow.setAdapter(mVideoHomeAdapter);
		mViewFlow.setMode(Mode.PULL_FROM_START);
		if (mTextViewBottom != null) {
			mTextViewBottom.setVisibility(View.VISIBLE);
		}
		mVideoHomeAdapter.notifyDataSetChanged();
	}

	public int getGuiheaderheight() {
		if (mGuiMenu != null) {
			return mGuiHeader.getIBMenuHeight();
		}
		return 0;
	}

	public void loadStory(String storyId) {
		mStoryId = storyId;
		mStoryDetail = null;
		if (mTNDownloader != null) {
			if (mProgressDialog != null) {
				mProgressDialog.show();
			}
			mTNDownloader.addDownload(
					TNPreferenceManager.isConnectionAvailable(),
					RequestDataFactory.makeStoryRequest(
							TNPreferenceManager.getUserId(), storyId));
		}
	}

	private void generateImageReviewIntent(
			ArrayList<ImageThumb> listImageThumbs,
			ArrayList<VideoThumb> listVideoThumbs, boolean imagesFirst) {
		mIssueChanged = false;
		if (mImageReviewIntent != null) {
			mImageReviewIntent = null;
		}
		mImageReviewIntent = new Intent(this,
				StoryDetailImageReviewActivity.class);
		mImageReviewIntent.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		if (listImageThumbs != null) {
			mImageReviewIntent.putExtra("dataImageThumbs", listImageThumbs);
		}
		if (listVideoThumbs != null) {
			mImageReviewIntent.putExtra("dataVideoThumbs", listVideoThumbs);
		}
		// mImageReviewIntent.putExtra("dataImagesFirst", imagesFirst);
		// StoryDetailImageReviewActivity.mVideoFirst = true;
		// startActivityForResult(mImageReviewIntent, 0);

		if (listVideoThumbs == null
				|| (listVideoThumbs != null && listVideoThumbs.size() <= 0)) {
			UIUtils.showToast(this, 2, "have no video to play");
			return;
		}
		VideoThumb videoThumb = listVideoThumbs.get(0);
		if (videoThumb != null) {
			Intent mIntentVideoThumb = new Intent(this,
					StoryDetailVideoPlayActivity.class);
			mIntentVideoThumb.putExtra("objvideo", videoThumb);
			startActivityForResult(mIntentVideoThumb, 0);
		}
	}

	protected void setStoryImagesHSViews(JsonArray jaImages, JsonArray jaVideos) {
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

	}

}
