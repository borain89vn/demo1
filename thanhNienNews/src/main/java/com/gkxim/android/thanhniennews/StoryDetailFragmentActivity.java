/**
 * 
 */
package com.gkxim.android.thanhniennews;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.layout.GUIHeader;
import com.gkxim.android.thanhniennews.layout.GUIListMenuAdapter;
import com.gkxim.android.thanhniennews.layout.GUIListMenuListView;
import com.gkxim.android.thanhniennews.layout.GUIStoryCommentDialog;
import com.gkxim.android.thanhniennews.layout.GUIStoryFooter;
import com.gkxim.android.thanhniennews.layout.GUIStoryShareDialog;
import com.gkxim.android.thanhniennews.layout.GUIStoryTextModeDialog;
import com.gkxim.android.thanhniennews.layout.StoryDetailFragment;
import com.gkxim.android.thanhniennews.models.StoryDetail;
import com.gkxim.android.thanhniennews.social.SocialHelper;
import com.gkxim.android.thanhniennews.social.SocialShare;
import com.gkxim.android.thanhniennews.tracking.Tracking;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.GKIMLog;
import com.gkxim.android.utils.UIUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Hashtable;

/**
 * @author Timon
 * 
 */
public class StoryDetailFragmentActivity extends FragmentActivity {

	private static final String TAG = "StoryDetailFragmentActivity";
	private static final String ACTION_STORY_VIEW = "com.gkxim.android.thanhniennews.ACTION_VIEW_STORY";
	private static final String DATA_KEY_STORY_JSON = "data.data";
	private static final String DATA_KEY_STORY_ID = "story_id";
	public static final String EXTRAKEY_STORY_DEEP_LINK = "sid-";
	
	
	protected static final boolean DEBUG = GKIMLog.DEBUG_ON;

	private ViewPager mPager;

	private StoryDetailPagerAdapter mPagerAdapter;
	// List of Stories from section.
	private String[] mListStoryIds;
	private Hashtable<String, Integer> mHashStoryCommentCount;
	private Hashtable<String, Integer> mHashStoryFBLikeCount;

	// private int mNumberOfStories;

	// private AlertDialog mProcessingDialog;
	private GUIHeader mGuiHeader = null;
	private GUIStoryFooter mGuiFooter = null;
	private GUIListMenuListView mGuiMenu = null;
	private GUIStoryShareDialog mShareDialog;
	private GUIStoryTextModeDialog mTextModeDialog = null;
	private GUIStoryCommentDialog mCommentDialog;

	private Intent mFinishData = null;
	private String mStoryId;
	private String mOpenedFromSectionId;
	private boolean mMenuShown = false;
	private boolean mTabletVersion = false;

	private Animation mOutAnimation2Left;
	private Animation mInAnimationFromRight;
	private Animation mInAnimationFromLeft;
	private Animation mOutAnimation2Right;

	private OnClickListener mOnClickListener = getOnClickListener();
	private Intent mIntentComment;
	private OnDismissListener mOnDialogDismissListener = getOnCommentDialogDissmisListener();
	private boolean mHasFromPNS;
	private UiLifecycleHelper uiHelper;
	private int checksharelikefb = 0;
	private HashMap<Integer,StoryDetailFragment>  storyDetailFragmentArrayList;

    //Nam.Nguyen add InterstitialAd
//    private InterstitialAd interstitial;

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
				if (!mTabletVersion) {
					socialLike();
				} else {
					FragmentManager fm = getSupportFragmentManager();
					StoryDetailFragment frag = (StoryDetailFragment) fm
							.findFragmentByTag(getFragmentTag(mPager
									.getCurrentItem()));
					if (frag != null) {
						frag.socialLike();
					}
				}
			}
		}

	}

	@Override
	protected void onCreate(Bundle arg0) {
		GKIMLog.lf(this, 1, TAG + "=>onCreate.");
		super.onCreate(arg0);
		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(arg0);
		if (TNPreferenceManager.getApplicationContext() == null) {
			TNPreferenceManager.setContext(this);
		}
		mTabletVersion = (getResources().getBoolean(R.bool.istablet));
		if (getResources().getBoolean(R.bool.portrait_only)) {
			GKIMLog.lf(this, 0, TAG + "=>Not support for rotation");
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		Intent intent = this.getIntent();
		if (intent != null) {
			// should pass to StoryDetailFragment.
			String action = intent.getAction();
			if (ACTION_STORY_VIEW.equalsIgnoreCase(action)) {
				// start story from PNS
				Bundle extra = intent.getExtras();
				if (extra != null && extra.containsKey(DATA_KEY_STORY_JSON)) {
					String strjson = extra.getString(DATA_KEY_STORY_JSON);
					try {
						JSONObject jo = new JSONObject(strjson);
						if (jo.has(DATA_KEY_STORY_ID)) {
							mStoryId = jo.getString(DATA_KEY_STORY_ID);
							// mStoryChecked = false;
							mOpenedFromSectionId = TNPreferenceManager.EXTRAVALUE_SECTION_HOME;
						}
					} catch (Exception e) {
						GKIMLog.lf(null, 4, TAG
								+ "=>Exception from parsing json string: "
								+ strjson + ": " + e.getMessage());
					}
				}
			} else if (intent.hasExtra(TNPreferenceManager.EXTRAKEY_IS_STORY)
					&& intent.hasExtra(TNPreferenceManager.EXTRAKEY_STORYID)) {
				mStoryId = intent
						.getStringExtra(TNPreferenceManager.EXTRAKEY_STORYID);
				// mStoryChecked = intent.getBooleanExtra(
				// TNPreferenceManager.EXTRAKEY_IS_STORY_CHECKED, false);
				mOpenedFromSectionId = intent
						.getStringExtra(TNPreferenceManager.EXTRAKEY_OPEN_STORY_FROM_SECTION);
				mHasFromPNS = intent
						.getBooleanExtra(
								TNPreferenceManager.EXTRAKEY_OPEN_STORY_FROM_PNS,
								false);
				String storyIds = intent
						.getStringExtra(TNPreferenceManager.EXTRAKEY_STORYIDS_FROM_SECTION);
				if (storyIds != null && storyIds.length() > 0) {
					mListStoryIds = storyIds.split(",");
					// mNumberOfStories = mListStoryIds.length;
				}
			}
		}
		
		initLayout();
		
		GKIMLog.lf(this, 0, TAG + "=>onCreate: " + mStoryId);
		mHashStoryCommentCount = new Hashtable<String, Integer>();
		mHashStoryFBLikeCount = new Hashtable<String, Integer>();
		if (DEBUG) {
			UIUtils.showToast(this, "starting Story: " + mStoryId);
		}
		if (mStoryId == null || mStoryId.length() <= 0) {
			this.finish();
		}
		if (mListStoryIds == null || mListStoryIds.length <= 0) {
			mListStoryIds = new String[] { mStoryId };
		}
        //Nam.Nguyen adding InterstitialAd
//        GKIMLog.l(1, TAG + " interstitialAd ");
//        // Create the interstitial.
//        interstitial = new InterstitialAd(this);
//        interstitial.setAdUnitId(getResources().getString(R.string.api_key_interstitial_ad));
//
//        // Create ad request.
//        AdRequest adRequest = new AdRequest.Builder().build();
//
//        // Load ads into Interstitial Ads
//        interstitial.loadAd(adRequest);


	}
    // Invoke displayInterstitial() when you are ready to display an interstitial.
    public void displayInterstitial() {
        // Prepare an Interstitial Ad Listener
//        interstitial.setAdListener(new AdListener() {
//            public void onAdLoaded() {
//                // Call displayInterstitial() function
//                if (interstitial.isLoaded()) {
//                    interstitial.show();
//                }
//            }
//        });
    }

    @Override
	protected void onStart() {
		GKIMLog.lf(this, 0, TAG + "=>onStart.");
		if (mStoryId == null || mStoryId.length() <= 0) {
			this.finish();
		}
		mPagerAdapter.setStories(mListStoryIds);
		mPagerAdapter.notifyDataSetChanged();
		int index = mPagerAdapter.getStoryIndex(mStoryId);
		mPager.setCurrentItem(index, true);
		if (index == 0) {
			myOnPageSelectedLogic(index);
		}
		mHashStoryCommentCount.clear();
		mHashStoryFBLikeCount.clear();
		super.onStart();
		Tracking.startSession(this);
        displayInterstitial();
	}

	@Override
	protected void onResume() {
		GKIMLog.lf(this, 0, TAG + "=>onResume.");
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
		if (mGuiFooter != null && !mTabletVersion) {
			mGuiFooter.setOnClickListener(mOnClickListener);
		}
		if (mGuiMenu != null) {
			mGuiMenu.setOnClickListener(mOnClickListener);
		}
		uiHelper.onResume();
		super.onResume();
	}

	@Override
	protected void onStop() {
		GKIMLog.lf(this, 0, TAG + "=>onStop.");
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
		// FlurryAgent.onEndSession(this);
		Tracking.endSeesion(this);
		super.onStop();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		uiHelper.onPause();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		GKIMLog.lf(this, 0, TAG + "=>onDestroy.");
		uiHelper.onDestroy();
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		GKIMLog.lf(this, 0, TAG + "=>onBackPressed.");
		if (mGuiMenu.getVisibility() == View.VISIBLE) {
			hideGUIListMenu();
			GKIMLog.lf(this, 0, TAG + "=>onBackPressed. 1");
		} else if (mHasFromPNS) {
			// FIXME: fix pns
			GKIMLog.lf(this, 0, TAG + "=>onBackPressed. 2");
			mFinishData.putExtra(TNPreferenceManager.EXTRAKEY_BACK_HAS_BACK,
					true);
			mFinishData.putExtra(TNPreferenceManager.EXTRAKEY_BACK_TO_SECTION,
					TNPreferenceManager.EXTRAVALUE_SECTION_HOME);
			StoryDetailFragmentActivity.this.finish();
			// mFinishData.setClass(this, SectionActivity.class);
			// startActivityFromChild(this, mFinishData, -1);
		} else {
			GKIMLog.lf(this, 0, TAG + "=>onBackPressed. 2");
			super.onBackPressed();
		}
	}

	@Override
	public void finish() {
		GKIMLog.lf(this, 0, TAG + "=>finish");
		if (mFinishData != null) {
			setResult(RESULT_OK, mFinishData);
		}
		super.finish();
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
			StoryDetailFragmentActivity.this.finish();
		} else {
			if (resCode == RESULT_OK) {
				if (mGuiFooter != null) {
					mHashStoryCommentCount.put(mStoryId, data.getIntExtra(
							TNPreferenceManager.EXTRAKEY_COMMENT_COUNT, 0));
					// mGuiFooter.setCommentCountView();
				}
			}
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
	protected void onNewIntent(Intent intent) {
		GKIMLog.lf(this, 1, TAG + "=>onNewIntent");
		super.onNewIntent(intent);
	}

	private void myOnPageSelectedLogic(int position) {
		// if(TNPreferenceManager.isConnectionAvailable()) {
		mStoryId = mPagerAdapter.getPageStoryId(position);
		GKIMLog.lf(null, 1, TAG + "=>onPageSelected: " + position + " story: "
				+ mStoryId);
		TNPreferenceManager.putReadStory(mStoryId);
		
		String storyName = "";
		try {
			FragmentManager fm = getSupportFragmentManager();
			StoryDetailFragment frag = (StoryDetailFragment) fm
					.findFragmentByTag(getFragmentTag(mPager.getCurrentItem()));
			if (frag != null) {
				StoryDetail sd = frag.getStoryDetail();
				String sectionid = "";
				if (sd != null) {
					sectionid = sd.getSectionid();
					storyName = sd.getStorytitle();
				}
				GKIMLog.lf(this, 0, TAG
						+ "=>myOnPageSelectedLogic, saving section code: "
						+ sectionid);
				TNPreferenceManager.updateCurrentStandingSectionId(sectionid);
			}
		} catch (Exception e) {
			GKIMLog.lf(this, 0, TAG
					+ "=> failed to get current section code from story: "
					+ mStoryId + ", " + e.getMessage());
		}
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(TNPreferenceManager.EVENT_KEY_STORY_NAME, storyName);
		map.put(TNPreferenceManager.EVENT_KEY_STORY_ID, mStoryId);
		Tracking.sendEvent(TNPreferenceManager.EVENT_STORY_VIEW, map);

		updateFragmentTextmode(TNPreferenceManager.isNightMode());
		updateFragmentTextZoommode();
		if (!mTabletVersion) {
			int count = 0;
			if (mHashStoryCommentCount != null
					&& mHashStoryCommentCount.containsKey(mStoryId)
					&& mGuiFooter != null) {
				count = mHashStoryCommentCount.get(mStoryId);
			}
			mGuiFooter.setCommentCountView(count);
			count = 0;
			if (mHashStoryFBLikeCount != null
					&& mHashStoryFBLikeCount.containsKey(mStoryId)) {
				count = mHashStoryFBLikeCount.get(mStoryId);
			}
			mGuiFooter.setFbLikeCountView(count);
		}
		// boolean bLoggedIn = TNPreferenceManager.checkLoggedIn();
		// if (bLoggedIn) {
		GKIMLog.l(1, TAG + " myOnPageSelectedLogic call setSavedStory");
		setSavedStory(TNPreferenceManager.hasSavedStory(mStoryId));
		// } else {
		// setSavedStory(false);
		// }

		if (mGuiFooter != null && !mTabletVersion) {
			int mFbLikeCount = getSharedPreferences(mStoryId + "_",
					Context.MODE_PRIVATE).getInt(mStoryId + "_", 0);
			boolean is = getSharedPreferences(mStoryId, Context.MODE_PRIVATE)
					.getBoolean(mStoryId, false);
			TextView tvlikefb = (TextView) mGuiFooter
					.findViewById(R.id.tv_storydetail_fblike_count);
			tvlikefb.setText(mFbLikeCount + "");
			if (is) {
				tvlikefb.setBackgroundResource(R.drawable.ic_storyfooter_fblike_over);
			} else {
				tvlikefb.setBackgroundResource(R.drawable.ic_storyfooter_fblike);
			}
		}
	}

	private void initLayout() {
		setContentView(R.layout.activity_storydetail);
		mGuiHeader = (GUIHeader) findViewById(R.id.guiheader);
		mGuiFooter = (GUIStoryFooter) findViewById(R.id.guifooter);
		if (mTabletVersion) {
			mGuiFooter.setVisibility(View.GONE);
		}
		// mGuiFooter.setVisibility(View.INVISIBLE);
		mGuiMenu = (GUIListMenuListView) findViewById(R.id.guimenu);

		// Instantiate a ViewPager and a PagerAdapter.
		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setOffscreenPageLimit(getResources().getInteger(
				R.integer.storydetail_preload_limit));
		// if (UIUtils.hasHoneycomb()) {
		// mPagerAdapter = new StoryDetailPagerAdapter(getFragmentManager());
		// }
		mPagerAdapter = new StoryDetailPagerAdapter(getSupportFragmentManager());
		mPager.setAdapter(mPagerAdapter);
		mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				myOnPageSelectedLogic(position);
			}

		});

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
	}

	/**
	 * true = zoom in (bigger), else zoom out (smaller)
	 * 
	 * @param bZoomIn
	 */
	protected void changeTextZoom(boolean bZoomIn) {
		FragmentManager fm = getSupportFragmentManager();
		StoryDetailFragment frag = (StoryDetailFragment) fm
				.findFragmentByTag(getFragmentTag(mPager.getCurrentItem()));
		if (frag != null) {
			frag.changeTextZoom(bZoomIn);
		}
	}

	protected void changeTextMode(boolean bChecked) {
		TNPreferenceManager.setTextMode(bChecked);
		updateFragmentTextmode(bChecked);
	}

	private void updateFragmentTextmode(boolean bChecked) {
		FragmentManager fm = getSupportFragmentManager();
		StoryDetailFragment frag = (StoryDetailFragment) fm
				.findFragmentByTag(getFragmentTag(mPager.getCurrentItem()));
		if (frag != null) {
			GKIMLog.lf(null, 0,
					TAG + "=> update text mode: " + bChecked
							+ " for fragment: " + frag.getStoryId() + " =>: "
							+ frag.getStoryTitle());
			frag.changeTextMode(bChecked);
		}
	}

	private void updateFragmentTextZoommode() {
		FragmentManager fm = getSupportFragmentManager();
		StoryDetailFragment frag = (StoryDetailFragment) fm
				.findFragmentByTag(getFragmentTag(mPager.getCurrentItem()));
		if (frag != null) {
			frag.resizeFontWebView();
		}
	}

	protected void updateStoryComment(StoryDetailFragment frag, String storyid) {
		if (frag != null) {
			frag.updateStoryComment();
		}
	}

	public void addStoryDetailCommentCount(String storyId, int count) {
		if (mHashStoryCommentCount != null) {
			// && !mHashStoryCommentCount.containsKey(storyId)) {
			mHashStoryCommentCount.put(storyId, count);
			if (storyId.equals(mStoryId) && mGuiFooter != null) {
				mGuiFooter.setCommentCountView(count);
			}
		}
	}

	public void addStoryFbLikeCount(String storyId, int count) {
		if (mHashStoryFBLikeCount != null
				&& !mHashStoryFBLikeCount.containsKey(storyId)) {
			mHashStoryFBLikeCount.put(storyId, count);
			if (storyId.equals(mStoryId) && mGuiFooter != null) {
				mGuiFooter.setFbLikeCountView(count);
			}
		}
	}

	public void setHeaderDate(String date) {
		if (mGuiHeader != null) {
			mGuiHeader.setDate(date);
		}
	}

	private void showGUIListMenu() {
		if (mGuiMenu != null && mGuiMenu.getVisibility() != View.VISIBLE) {
			mGuiMenu.setVisibility(View.VISIBLE);
			mGuiMenu.startAnimation(mInAnimationFromLeft);
			if (!mTabletVersion) {
				mGuiFooter.setVisibility(View.GONE);
				mPager.setVisibility(View.GONE);
				mPager.startAnimation(mOutAnimation2Right);
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
				mPager.setVisibility(View.VISIBLE);
				mPager.startAnimation(mInAnimationFromRight);
			}
			mMenuShown = false;

			InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			if (im != null && im.isActive()) {
				im.hideSoftInputFromWindow(mGuiMenu.getWindowToken(), 0);
			}
		}
	}

	public Handler getHandler() {
		return mHandler;
	}

	private Handler mHandler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
				case TNPreferenceManager.HANDLER_MSG_HAS_LOGGIN_CHANGED:
					GKIMLog.lf(null, 0, TAG + "=>login has changed.");
					// XXX: not correct on the UID from logout to login state.
					// boolean bLoggedIn = TNPreferenceManager.checkLoggedIn();
					if (mTabletVersion) {
						FragmentManager fm = getSupportFragmentManager();
						StoryDetailFragment frag = (StoryDetailFragment) fm
								.findFragmentByTag(getFragmentTag(mPager
										.getCurrentItem()));
						// if (bLoggedIn) {
						frag.setCheckSave(TNPreferenceManager
								.hasSavedStory(mStoryId));
						// } else {
						// frag.setCheckSave(false);
						// }
					} else {
						// if (bLoggedIn) {
						GKIMLog.l(1, TAG + " handleMessage call setSavedStory");
						setSavedStory(TNPreferenceManager
								.hasSavedStory(mStoryId));
						// } else {
						// setSavedStory(false);
						// }
					}
					hideGUIListMenu();
					return true;
				case TNPreferenceManager.HANDLER_MSG_HAS_STORYDETAIL_LOAD_COMPLETED:
					// Story activity will receive this event when a
					// StoryDetail is completely loaded in fragment.
					StoryDetail sd = (StoryDetail) msg.obj;
					if (sd == null || TextUtils.isEmpty(sd.getSectionid())) {
						return false;
					}
					String sectionid = sd.getSectionid();
					String storyid = String.valueOf(sd.getStoryid());
					try {
						if (mStoryId == null || storyid.equals(mStoryId)) {
							GKIMLog.lf(
									StoryDetailFragmentActivity.this,
									0,
									TAG
											+ "=>myOnPageSelectedLogic, update section code from current story: "
											+ storyid + ", section: "
											+ sectionid);
							TNPreferenceManager
									.updateCurrentStandingSectionId(sectionid);
						}
					} catch (Exception e) {
						GKIMLog.lf(
								StoryDetailFragmentActivity.this,
								0,
								TAG
										+ "=> failed to get current section code from story: "
										+ sectionid + ", " + e.getMessage());
					}
					break;
				default:
					break;
			}
			return false;
		}
	});

	protected void showShareDialog() {
		if (mShareDialog == null) {
			mShareDialog = new GUIStoryShareDialog(this);
			mShareDialog.setOnShareClickListener(mOnClickListener);
		}
		mShareDialog.show();
	}

	protected void showTextModeDialog() {
		if (mTextModeDialog == null) {
			mTextModeDialog = new GUIStoryTextModeDialog(this);
			mTextModeDialog.setOnShareClickListener(mOnClickListener);
		}
		mTextModeDialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
				FragmentManager fm = getSupportFragmentManager();
				StoryDetailFragment frag = (StoryDetailFragment) fm
						.findFragmentByTag(getFragmentTag(mPager
								.getCurrentItem()));
				if (frag != null) {
					frag.reloadContentWebView();
				}
			}
		});
		mTextModeDialog.setToggleChecked(TNPreferenceManager.isNightMode());
		mTextModeDialog.show();
	}

	protected void showCommentDialog() {
		if (mCommentDialog != null) {
			mCommentDialog = null;
		}
		if (mShareDialog != null && mShareDialog.isShowing()) {
			mShareDialog.dismiss();
		}
		FragmentManager fm = getSupportFragmentManager();
		StoryDetailFragment frag = (StoryDetailFragment) fm
				.findFragmentByTag(getFragmentTag(mPager.getCurrentItem()));

		mCommentDialog = new GUIStoryCommentDialog(this);
		mCommentDialog.setStoryId(mStoryId);
		mCommentDialog.setStoryTitle(frag.getStoryTitle());
		// FIXME: refresh story comment on Dialog's dismiss.
		mCommentDialog.setOnDismissListener(mOnDialogDismissListener);
		mCommentDialog.show();
	}

	protected void socialShare(int networkId, int isstatus) {
		if (mShareDialog != null && mShareDialog.isShowing()) {
			mShareDialog.dismiss();
		}

		// Collect story's information
		String[] data = null;
		FragmentManager fm = getSupportFragmentManager();
		StoryDetailFragment frag = (StoryDetailFragment) fm
				.findFragmentByTag(getFragmentTag(mPager.getCurrentItem()));
		if (frag != null) {
			data = frag.getShareContent().split("%2C");
			GKIMLog.lf(null, 0, TAG + "=>socialShare: " + data.length);
		}

		if (data != null && data.length >= 3) {
			// Progress SNS's strategy
			SocialHelper helper = SocialHelper.getInstance(this, networkId);
			// FIXME: add callback for listening result.
			helper.post(data, isstatus);
		}
	}

	protected void socialLike() {
		// Collect story's information
		String[] data = null;
		FragmentManager fm = getSupportFragmentManager();
		StoryDetailFragment frag = (StoryDetailFragment) fm
				.findFragmentByTag(getFragmentTag(mPager.getCurrentItem()));
		if (frag != null) {
			data = frag.getShareContent().split("%2C");
			GKIMLog.lf(null, 0, TAG + "=>socialLike: " + data.length);
		}

		if (data != null && data.length >= 4) {
			SocialHelper helper = SocialHelper.getInstance(this, 1);
			helper.like(data[3], data);
		}
	}

	protected void startCommentActivity(String storyId) {
		if (mIntentComment == null) {
			mIntentComment = new Intent(this, StoryDetailCommentActivity.class);
			mIntentComment.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
					| Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		}
		FragmentManager fm = getSupportFragmentManager();
		StoryDetailFragment frag = (StoryDetailFragment) fm
				.findFragmentByTag(getFragmentTag(mPager.getCurrentItem()));

		mIntentComment.putExtra(StoryDetailCommentActivity.EXTRA_STORY_TITLE,
				frag.getStoryTitle());
		mIntentComment.putExtra(StoryDetailCommentActivity.EXTRA_STORYID,
				storyId);
		int count = 0;
		if (mHashStoryCommentCount.containsKey(storyId)) {
			count = mHashStoryCommentCount.get(storyId);
		}
		mIntentComment.putExtra(StoryDetailCommentActivity.EXTRA_COMMENT_COUNT,
				count);
		startActivityForResult(mIntentComment,
				TNPreferenceManager.REQ_CODE_STORYDETAIL_COMMENT);
	}

	private String getFragmentTag(int pos) {
		// return mStoryId;
		return ("android:switcher:" + R.id.pager + ":" + pos);
	}

	/**
	 * @param extrakeyBackToSection
	 * @param sectionid
	 */
	public void putFinishExtra(String extrakey, String sectionid) {
		mFinishData.putExtra(extrakey, sectionid);
	}

	/**
	 * @param extrakey
	 * @param bValue
	 */
	public void putFinishExtra(String extrakey, boolean bValue) {
		mFinishData.putExtra(extrakey, bValue);
	}

	public void performOnClickListener(View v) {
		mOnClickListener.onClick(v);
	}

	private void addOrRemoveSaveBoxStory(View fromView, StoryDetail storyDetail) {
		GKIMLog.lf(this, 0, TAG + "=>addOrRemoveSaveBoxStory");
		if (!mStoryId.equals(String.valueOf(storyDetail.getStoryid()))) {
			GKIMLog.lf(this, 0, TAG
					+ "=> not match current story, won't save. (" + mStoryId
					+ ", " + storyDetail.getStoryid() + ").");
			return;
		}
		String currentStoryId = mStoryId;
		if (TNPreferenceManager.hasSavedStory(currentStoryId)) {
			// if (!TNPreferenceManager.checkLoggedIn()) {
			// UIUtils.showToast(this,
			// getResources().getString(R.string.request_for_login));
			// } else {
			TNPreferenceManager.deleteStory(currentStoryId);
			fromView.setSelected(false);
			GKIMLog.lf(this, 0, TAG + "=>removed story: " + currentStoryId);
			TNPreferenceManager.updateSaved(currentStoryId, 0);

			UIUtils.showToast(
					this,
					getResources().getString(
							R.string.storydetail_story_has_removed));

			// }
		} else {
			// if (!TNPreferenceManager.checkLoggedIn()) {
			// UIUtils.showToast(this,
			// getResources().getString(R.string.request_for_login));
			// } else {
			TNPreferenceManager.saveStory(storyDetail);
			fromView.setSelected(true);
			GKIMLog.lf(this, 0, TAG + "=>added story: " + currentStoryId);
			TNPreferenceManager.updateSaved(currentStoryId, 1);
			UIUtils.showToast(
					this,
					getResources().getString(
							R.string.storydetail_story_has_saved));
			// }
		}
	}

	public void setSavedStory(boolean hasShaved) {
		GKIMLog.l(1, TAG + " setSavedStory :" + hasShaved);
		if (mGuiFooter != null) {
			mGuiFooter.setSavedStory(hasShaved);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		if (mGuiMenu != null) {
			mGuiMenu.CloseDialog();
		}
		super.onSaveInstanceState(outState);
	}

	private View.OnClickListener getOnClickListener() {
		return (new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				GKIMLog.lf(null, 1, TAG + "=>onClick: " + v);
				switch (v.getId()) {
					case R.id.header_iv_logo:
					case R.id.menu_list_header_ivhome:
						mFinishData.putExtra(
								TNPreferenceManager.EXTRAKEY_BACK_HAS_BACK,
								true);
						mFinishData.putExtra(
								TNPreferenceManager.EXTRAKEY_BACK_TO_SECTION,
								TNPreferenceManager.EXTRAVALUE_SECTION_HOME);
						StoryDetailFragmentActivity.this.finish();

						break;
					case R.id.menu_in_list:
						if (v instanceof LinearLayout) {
							GUIListMenuAdapter.ViewHolder menuItem = (com.gkxim.android.thanhniennews.layout.GUIListMenuAdapter.ViewHolder) v
									.getTag();
                            mFinishData.putExtra(
                                    TNPreferenceManager.EXTRAKEY_BACK_HAS_BACK,
                                    true);
                            mFinishData
                                    .putExtra(
                                            TNPreferenceManager.EXTRAKEY_BACK_TO_SECTION,
                                            menuItem.id);
                            StoryDetailFragmentActivity.this.finish();
						}
						break;
					case R.id.tv_storydetail_category:
						break;
					case R.id.menu_list_header_ivmyhome:
						mFinishData.putExtra(
								TNPreferenceManager.EXTRAKEY_BACK_HAS_BACK,
								true);
						mFinishData
								.putExtra(
										TNPreferenceManager.EXTRAKEY_BACK_TO_SECTION,
										TNPreferenceManager.EXTRAVALUE_SECTION_USER_PAGE);
						StoryDetailFragmentActivity.this.finish();
						break;
					case R.id.menu_list_header_ivstored:
						// Note process as same as user's page
						// GKIMLog.lf(null, 0, TAG +
						// "=>Stored page, not handle in BETA");
						// hideGUIListMenu();
						mFinishData.putExtra(
								TNPreferenceManager.EXTRAKEY_BACK_HAS_BACK,
								true);
						mFinishData
								.putExtra(
										TNPreferenceManager.EXTRAKEY_BACK_TO_SECTION,
										TNPreferenceManager.EXTRAVALUE_SECTION_USER_PAGE_SAVED);
						StoryDetailFragmentActivity.this.finish();
						break;
					case R.id.menu_list_header_ivsearch:
						mFinishData.putExtra(
								TNPreferenceManager.EXTRAKEY_BACK_HAS_BACK,
								true);
						mFinishData
								.putExtra(
										TNPreferenceManager.EXTRAKEY_BACK_TO_SECTION,
										TNPreferenceManager.EXTRAVALUE_SECTION_SEARCH_PAGE);
						StoryDetailFragmentActivity.this.finish();
						break;
					case R.id.imb_storyfooter_back:
						onBackPressed();
						break;
					case R.id.imb_storydetail_back:
						// if (mHSVImages != null && mHSVImages.isShowing()) {
						// mHSVImages.dismiss();
						// }
						break;
					case R.id.imb_storyfooter_textsize:
						showTextModeDialog();
						break;
					case R.id.imb_storyfooter_fontsmaller:
						changeTextZoom(false);
						break;
					case R.id.imb_storyfooter_fontbigger:
						changeTextZoom(true);
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
						Object o = v.getTag();
						if (o == null) {
							FragmentManager fm = getSupportFragmentManager();
							StoryDetailFragment frag = (StoryDetailFragment) fm
									.findFragmentByTag(getFragmentTag(mPager
											.getCurrentItem()));
							if (frag != null) {
								o = frag.getStoryDetail();
							}
						}
						if (o != null) {
							addOrRemoveSaveBoxStory(v, (StoryDetail) o);
						}
						break;
					case R.id.imb_storyfooter_share:
						showShareDialog();
						break;
					case R.id.imv_storydetail_dlg_shareby_comment:
						if (!TNPreferenceManager.checkLoggedIn()) {
							UIUtils.showToast(
									StoryDetailFragmentActivity.this,
									getResources().getString(
											R.string.request_for_login));
						} else {
							showCommentDialog();
						}
						break;
					case R.id.imb_storyfooter_addcomment:
					case R.id.imv_storydetail_shareby_comment:
						FragmentManager fm = getSupportFragmentManager();
						StoryDetailFragment frag = (StoryDetailFragment) fm
								.findFragmentByTag(getFragmentTag(mPager
										.getCurrentItem()));
						if (frag != null) {
							if (frag.getStoryDetail() != null) {
								if (!mTabletVersion) {
									startCommentActivity(mStoryId);
								} else {
									// if (!TNPreferenceManager.checkLoggedIn())
									// {
									// UIUtils.showToast(
									// StoryDetailFragmentActivity.this,
									// getResources().getString(
									// R.string.request_for_login));
									// } else {
									showCommentDialog();
									// }
								}
							}
						}

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
						if (!mTabletVersion) {
							checksharelikefb = 2;
							socialLike();
						}
						break;
					default:
						break;
				}
			}
		});
	}

	private OnDismissListener getOnCommentDialogDissmisListener() {
		return (new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				GKIMLog.lf(null, 1, TAG + "=>onDismiss: " + dialog + ": "
						+ mStoryId);
				if (mTabletVersion) {
					FragmentManager fm = getSupportFragmentManager();
					StoryDetailFragment frag = (StoryDetailFragment) fm
							.findFragmentByTag(getFragmentTag(mPager
									.getCurrentItem()));
					updateStoryComment(frag, mStoryId);
				}
			}
		});
	}

	public class StoryDetailPagerAdapter extends FragmentPagerAdapter {

		private String[] mArrayStories;
		private int mStoriesCount = 0;
		FragmentManager fm;

		public StoryDetailPagerAdapter(FragmentManager fm) {
			super(fm);
			this.fm=fm;
			mStoriesCount = 0;
			storyDetailFragmentArrayList = new HashMap<>();
		}

		/**
		 * @param position
		 * @return
		 */
		public String getPageStoryId(int position) {
			if (mArrayStories != null && mArrayStories.length > position) {
				return mArrayStories[position];
			}
			return "empty";
		}

		public void setStories(String[] listStories) {
			if (listStories == null || listStories.length == 0) {
				return;
			}
			mArrayStories = null;
			mStoriesCount = listStories.length;
			mArrayStories = new String[mStoriesCount];
			System.arraycopy(listStories, 0, mArrayStories, 0, mStoriesCount);
			GKIMLog.lf(null, 0, TAG + "=>setStories, " + mStoriesCount
					+ " story's ids had been added.");
		}

		public int getStoryIndex(String storyId) {
			if (mArrayStories == null || mArrayStories.length == 0) {
				return -1;
			}
			int i = 0;
			int len = mArrayStories.length;
			while (i < len && !storyId.equals(mArrayStories[i])) {
				i++;
			}
			if (i == mArrayStories.length) {
				return 0;
			}
			return i;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.support.v4.app.FragmentPagerAdapter#getItem(int)
		 */
		@Override
		public Fragment getItem(int position) {
			GKIMLog.lf(null, 1, TAG + "=>getItem: " + position + ": "
					+ mArrayStories[position]);
			StoryDetailFragment storyDetailFragment = StoryDetailFragment.create(mArrayStories[position]);
			//storyDetailFragmentArrayList.put(position,storyDetailFragment);
			return storyDetailFragment;

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.support.v4.view.PagerAdapter#getCount()
		 */
		@Override
		public int getCount() {
			return mStoriesCount;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			GKIMLog.lf(null, 1, TAG + "=>destroyItem: " + position + ": "
					+ mArrayStories[position] + ": "
					+ ((StoryDetailFragment) object).getStoryTitle());
			((StoryDetailFragment) object).MyDestroy();
		FragmentManager manager;
			manager	= ((Fragment) object).getFragmentManager();
			String b="asda";
			b="asa";
//			FragmentTransaction trans = manager.beginTransaction();
//			trans.remove((Fragment) object);
//			trans.commit();
//
		String s="sas";


		}
	}
	public ViewPager getmPager() {
		return mPager;
	}
	public GUIStoryFooter getmGuiFooter() {
		return mGuiFooter;
	}

	public void setChecksharelikefb(int checksharelikefb) {
		this.checksharelikefb = checksharelikefb;
	}

	public String getmStoryId() {
		return mStoryId;
	}

}
