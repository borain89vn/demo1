/**
 * File: TNPreferenceManager.java
 * Creator: Timon.Trinh (timon@gkxim.com)
 * Date: 04-12-2012
 * 
 */
package com.gkxim.android.thanhniennews.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.View;
import android.view.View.MeasureSpec;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.gkim.thanhniennews.R;
import com.gkxim.android.cache.UrlImageViewCallback;
import com.gkxim.android.thanhniennews.models.BoxElement;
import com.gkxim.android.thanhniennews.models.BoxStory;
import com.gkxim.android.thanhniennews.models.GeneralPage;
import com.gkxim.android.thanhniennews.models.SectionTemplate;
import com.gkxim.android.thanhniennews.models.StoryDetail;
import com.gkxim.android.thanhniennews.models.TNTemplate;
import com.gkxim.android.thanhniennews.models.UserAccount;
import com.gkxim.android.thanhniennews.networking.RequestDataFactory;
import com.gkxim.android.thanhniennews.networking.XTifyController;
import com.gkxim.android.utils.ContentCache;
import com.gkxim.android.utils.GKIMLog;
import com.gkxim.android.utils.UIUtils;
import com.gkxim.android.utils.Utils;
import com.gkxim.android.utils.UtilsPreference;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

/**
 *
 */
public class TNPreferenceManager {

	private static final String TAG = "TNPreferenceManager";
	private static final int[] KEY_STATE_NORMAL = {};
	private static final int[] KEY_STATE_PRESSED = { android.R.attr.state_pressed };
	private static final int[] KEY_STATE_SELECTED = { android.R.attr.state_selected };

	public static final String EVENT_START = "STARTED";
	public static final String EVENT_END = "ENDED";
	public static final String EVENT_SECTION_VIEW = "section_view";
	public static final String EVENT_STORY_COMMENT = "story_comment";
	public static final String EVENT_STORY_SHARED = "story_shared";
	public static final String EVENT_STORY_VIEW = "story_view";
	public static final String EVENT_USER_REGISTERED = "user_registered";
	public static final String EVENT_USER_FBLOGIN = "user_fblogin";
	public static final String EVENT_KEY_SECTION_ID = "section_id";
	public static final String EVENT_KEY_SECTION_TITLE = "section_title";
	public static final String EVENT_KEY_STORY_ID = "story_id";
	public static final String EVENT_KEY_UID = "uid";
	public static final String EVENT_KEY_STORY_NAME = "story_name";
	public static final String EVENT_KEY_SHARED_NETWORK = "shared_network";
	public static final String EVENT_RATING = "rating";
	public static final String EVENT_RATING_SELECTED = "rating_selected";
	public static final String EVENT_XTIFY_REGISTERED = "xtify_registered";
	public static final String EVENT_XTIFY_REGISTERED_ID = "xtify_registered_id";
	public static final String EVENT_XTIFY_RECEIVED = "xtify_received";
	public static final String EVENT_XTIFY_RECEIVED_SHOW = "xtify_received_show";
	public static final String EVENT_XTIFY_PNS_TOUCHED = "xtify_pns_touched";
	public static final String EVENT_WORLDCUP_HOME_OPENED = "worldcup_home_opened";
	public static final String EVENT_WORLDCUP_MENU_OPENED = "worldcup_menu_opened";
	public static final String EVENT_WORLDCUP_PUSH_OPENED = "event_worldcup_push_opened";

	// Extra keys for intent
	public static final String EXTRAKEY_IS_STORY = "extrakey_isStory";
	public static final String EXTRAKEY_IS_STORY_CHECKED = "extrakey_is_story_checked";
	public static final String EXTRAKEY_STORYID = "extrakey_storyid";
	public static final String EXTRAKEY_STORY_FROM_VIDEO_SECTION = "extrakey_storyid_type";
	public static final String EXTRAKEY_BACK_TO_HOME = "extrakey_back_to_home";
	public static final String EXTRAKEY_BACK_TO_SECTION = "extrakey_back_to_section";
	public static final String EXTRAKEY_BACK_HAS_BACK = "extrakey_back_from_story";
	public static final String EXTRAKEY_BACK_HAS_FAVORITED_CHANGED = "extrakey_back_to_has_favorited";
	public static final String EXTRAKEY_BACK_HAS_STORY_ID = "extrakey_back_has_story_id";
	public static final String EXTRAKEY_OPEN_STORY_FROM_SECTION = "extrakey_open_story_from_section";
	public static final String EXTRAKEY_COMMENT_COUNT = "extrakey_comment_count";
	public static final String EXTRAKEY_STORYIDS_FROM_SECTION = "extrakey_storyids_from_section";
	public static final String EXTRAKEY_STORY_IMAGEREVIEW_COUNT = "extrakey_story_imagereview_count";
	public static final String EXTRAKEY_STORY_IMAGEREVIEW_URLS = "extrakey_story_imagereview_urls";
	public static final String EXTRAKEY_STORY_IMAGEREVIEW_CAPTIONS = "extrakey_story_imagereview_captions";
	public static final String EXTRAKEY_STORY_NIGHTMODE = "extrakey_story_nightmode";
	public static final String EXTRAKEY_STORY_TEXTZOOMSIZE = "extrakey_story_textzoomsize";
	public static final String EXTRAKEY_ROTATE_SECTION = "extrakey_rotate_section";
	public static final String EXTRAKEY_ROTATE_ISSUE_ID = "extrakey_rotate_issue_id";
	public static final String EXTRAKEY_OPEN_STORY_FROM_PNS = "extrakey_open_story_from_pns";

	public static final String EXTRAKEY_EVENTWEBVIEW_URL = "extrakey_eventwebview_url";
	public static final String EXTRAKEY_WORLDCUP_URL_HOME = "extrakey_worldcup_url_home";
	public static final String EXTRAKEY_WORLDCUP_URL_DETAIL = "extrakey_worldcup_url_detail";

	// list of preference keys
	public static final String SHAREDPREF_SPRING = "sharedpref_spring";
	public static final String PREF_KEYNAME_BOXCELLSIZE = "pref_keyname_boxcellsize";
	public static final String PREF_KEYNAME_RATING = "pref_keyname_rating";
	public static final String PREF_KEYNAME_BOXCELLSIZE_LAND = "pref_keyname_boxcellsize_land";
	public static final String PREF_KEYNAME_BOXCELLSIZE_POTRAIL = "pref_keyname_boxcellsize_potrail";
	public static final String PREF_KEYNAME_TEXT_SIZE_BOXVIEW = "pref_keyname_text_size_boxview";
	public static final String PREF_KEYNAME_SECTION_STANDINGID = "pref_keyname_section_standingid";
	private static final String PREF_KEYNAME_ACCOUNT_USERID = "pref_keyname_account_userid";
	private static final String PREF_KEYNAME_ACCOUNT_USERINFO = "pref_keyname_account_userinfo";
	private static final String PREF_KEYNAME_PNS_ENABLED = "pref_keyname_pns_enabled";
	private static final String PREF_KEYNAME_PNS_SECTIONS = "pref_keyname_pns_sections";
	private static final String PREF_KEYNAME_PNS_CHECK_ALL = "pref_keyname_pns_check_all";
	private static final String PREF_KEYNAME_SAVE_BOXSTORY = "pref_keyname_save_boxstory";
	private static final String PREF_KEYNAME_SAVE_ID_BOXSTORY = "pref_keyname_save_id_boxstory";
	private static final String PREF_KEYNAME_GPS_SHOW = "pref_keyname_gps_show";

	private static final String PREF_KEYNAME_INSTALL_SHORTCUT = "pref_keyname_install_shortcut";
	public static final String PREF_KEYNAME_BOXLAYOUT_GAPWIDTH = "pref_keyname_boxlayout_gapwidth";
	public static final String PREF_KEYNAME_BOXLAYOUT_GAPWIDTH_LAND = "pref_keyname_boxlayout_gapwidth_land";
	public static final String PREF_KEYNAME_BOXLAYOUT_GAPWIDTH_POTRAIL = "pref_keyname_boxlayout_gapwidth_potrail";
	private static final String CONST_REPLACE_TARGET_ID = "_#ID#_";
	private static final String PREF_KEYNAME_STORY_SMILEY_ICONS = "pref_keyname_keyname_story_smiley_icons";
	private static final String PREF_KEYNAME_SECTIONTEMPLATE_PREFIX = SectionTemplate.CONST_JSON_SECTIONTEMPLATE_ID
			+ "@" + CONST_REPLACE_TARGET_ID;
	private static final String PREF_KEYNAME_SEARCHCONTENT = "pref_keyname_searchcontent";
	private static final String PREF_BASIC_DATA_CREATED = "pref_basic_data_created";
	private static final String PREF_KEYNAME_HOST_DOMAIN = "pref_keyname_host_domain";
	private static final String PREF_KEYNAME_STORIES_READ = "pref_keyname_stories_read";
	private static final String PREF_KEYNAME_STORIES_SAVE = "pref_keyname_stories_save";
	private static final String PREF_KEYNAME_VIDEOHOME_CELLWIDTH = "pref_keyname_videohome_cellwidth";

	public static final String CONST_SEPARATOR = "_##_";
	private static final int PREF_INDEX_ID = 0;
	private static final int PREF_INDEX_TITLE = 1;
	private static final int PREF_INDEX_COLORSTRING = 2;
	private static final int PREF_INDEX_BGCOLOR1 = 3;
	private static final int PREF_INDEX_BGCOLOR2 = 4;
	private static final int PREF_INDEX_BGCOLOR3 = 5;
	private static final int PREF_INDEX_BGCOLOR4 = 6;

	private static final boolean ENABLE_SCROLLING_SECTION = false;
	private static final boolean IGNORE_TEXT_GRAVITY = true;
	public static final boolean BOX_HAS_TOUCH_HIGHLIGHT = true;
	public static final boolean BOX_HAS_TOUCH_ANIMATION = true;
	public static final long BOX_HAS_TOUCH_ANIMATION_DURATION = 200;
	public static final int REQ_CODE_SECTION_2_STORY = 0x0F;
	public static final int REQ_CODE_USER_POST = 0x0D;
	public static final int REQ_CODE_STORYDETAIL_COMMENT = 0x0E;
	public static final int REQ_CODE_SECTION_2_TETOFYOU = 0x0C;
	public static final int REQ_CODE_SECTION_2_GREETING = 0x0B;
	public static final int REQ_CODE_TETOFYOU_2_VIEWIMAGES = 0x0A;
	public static final int ACTIVITY_RESULT_BACK_FROM_LOGO = 0xF1;
	public static final int ACTIVITY_RESULT_BACK_FROM_LOGO_DETAIL_IMAGE = 0xF2;
	public static final int WEBVIEW_TEXTZOOM_STEP = 20;
	public static final int WEBVIEW_TEXTZOOM_NUMBERSTEPS = 2;
	public static final int WEBVIEW_TEXTZOOM_MIN = 100 - WEBVIEW_TEXTZOOM_STEP
			* WEBVIEW_TEXTZOOM_NUMBERSTEPS;
	public static final int WEBVIEW_TEXTZOOM_MAX = 100 + WEBVIEW_TEXTZOOM_STEP
			* WEBVIEW_TEXTZOOM_NUMBERSTEPS;

	// these are constant cheating for Xuan 2014, on DEV2
	public static final boolean SECTION_SPRING = false;
	public static final String EXTRAVALUE_SECTION_SPRING = "37226";
	public static final String EXTRAVALUE_SECTION_SPRING_TETOFYOU = "37227";
	public static final String EXTRAVALUE_SECTION_SPRING_WISHES_STORY = "699724";
	// these are constant cheating for Xuan 2014, on LIVE
	public static final String EXTRAVALUE_SECTION_SPRING_LIVE = "37226";
	public static final String EXTRAVALUE_SECTION_SPRING_TETOFYOU_LIVE = "37227";
	// public static final String EXTRAVALUE_SECTION_SPRING_WISHES_STORY_LIVE
	// ="699724"; // DEV
	public static final String EXTRAVALUE_SECTION_SPRING_WISHES_STORY_LIVE = "705690"; // LIVE;

	// these are constants setting for World Cup 2014 campaign
	public static final String EXTRAVALUE_ID_WORLDCUP_BANNER = Settings.Secure.ANDROID_ID
			+ "01";

	// these are constants setting for Media section
	public static String EXTRAVALUE_SECTION_MEDIA_LIVE = "62598";
	public static String EXTRAVALUE_SECTION_MEDIA_DEV = "52120";
	public static String EXTRAVALUE_SECTION_MEDIA = EXTRAVALUE_SECTION_MEDIA_DEV;
	public static String VIDEO_OF_YOU_ID_DEV = "52131";
	public static String VIDEO_OF_YOU_ID_LIVE = "62604";
	public static String VIDEO_OF_YOU_ID = VIDEO_OF_YOU_ID_DEV;

	// constant values for application
	public static final String EXTRAVALUE_SECTION_HOME = "1";
	// public static final String EXTRAVALUE_SECTION_HOME_VIDEO = "2";
	public static final String EXTRAVALUE_SECTION_USER_PAGE = "user_page";
	public static final String EXTRAVALUE_SECTION_USER_PAGE_VIDEO = "user_page_video";
	public static String EXTRAVALUE_SECTION_USER_PAGE_NAME = "Trang cua ban";
	public static final String EXTRAVALUE_SECTION_USER_PAGE_SAVED = "user_page_saved";
	public static String EXTRAVALUE_SECTION_USER_PAGE_SAVED_NAME = "Tin da luu";
	public static final String EXTRAVALUE_SECTION_SEARCH_PAGE = "search_page";
	public static final String EXTRAKEY_SPRING_WISH_LIKE = "liked_spring_wishes";
	public static final String EXTRAKEY_SPRING_WISH_LIKECOUNT = "liked_spring_wishes_count";
	public static final String EXTRAKEY_SPRING_YOURTET_LIKE = "liked_spring_yourtet";
	public static final String EXTRAKEY_SPRING_YOURTET_LIKECOUNT = "liked_spring_yourtet_count";
	public static final int HANDLER_MSG_HAS_LOGGIN_CHANGED = 1;
	public static final int HANDLER_MSG_HAS_STORYDETAIL_LOAD_COMPLETED = 2;
	public static final int HANDLER_MSG_HAS_SHOW_RATING = 99;
	private static final int MEGABYTE = 1024 * 1024;
	private static final int MAX_MEGABYTE = 8;
	private static final int STORIES_READ_MAXLENGTH = 1000;
	public static final int EXTRAVALUE_SECTION_COLUMN_TABLET = 4;
	public static final int EXTRAVALUE_SECTION_COLUMN_PHONE = 3;
	public static final int CACH_TIMEOUT_ONEMIN = 60 * 1000;
	public static final int MIN_COUNT_BOX_SECTION_HOME = 60;
	public static final int MIN_COUNT_BOX_SECTION_OTHER = 20;
	public static final int CACH_TIMEOUT_ONEHOUR = 60 * CACH_TIMEOUT_ONEMIN;
	public static final int CACHE_TIMEOUT_ONEDAY = 24 * CACH_TIMEOUT_ONEHOUR;
	public static final String VN_DATEFORMAT = "EEEE, dd.MM.yyyy";

	private static final int MAX_FILEPOST_PHOTO = 3;
	private static final int MAX_FILEPOST_VIDEO = 1;

	public static final String PREF_KEYNAME_LOGIN_FB = "pref_keyname_login_fb";
	// SplashActivity.mLocation is key checking
	public static final boolean GPS_GETTINGS = false;

	// EVENT name for tracking
	/**
	 * Tracking event on Story view, comming with map data: Map<String,String> =
	 * {uid=["1",uid]}
	 */
	public static String TRACKEVT_STORY_VIEW = "story_view";
	public static String TRACKKEY_STORY_VIEW_UID = "uid";

	// private static Context applicationContext = null;
	private static String[] mSectionIds = null;
	// Section's background drawables
	private static HashMap<String, Drawable> sectionDrawables = new HashMap<String, Drawable>();
	// Section's icons
	private static HashMap<String, Drawable> sectionIcons = new HashMap<String, Drawable>();
	private static HashMap<String, Drawable> sectionIconsNormal = new HashMap<String, Drawable>();
	private static HashMap<String, Drawable> sectionIconsOver = new HashMap<String, Drawable>();
	// Smiley icons on share comment
	private static HashMap<Long, Drawable> smileyIcons = new HashMap<Long, Drawable>();

	// Section Media's icons
	private static boolean mHasMediaFeature = true;
	private static HashMap<String, Drawable> secMediaIcons = new HashMap<String, Drawable>();
	private static HashMap<String, Drawable> secMediaIconsNormal = new HashMap<String, Drawable>();
	private static HashMap<String, Drawable> secMediaIconsOver = new HashMap<String, Drawable>();
	private static String[] mSecMediaIds = null;
	private static HashMap<String, Drawable> secMediaDrawables = new HashMap<String, Drawable>();

	//
	private static GeneralPage mSavedPage = null;
	private static ArrayList<JSONObject> mListJsonBoxStory = new ArrayList<JSONObject>();
	private static String mIDBoxStorys = "";
	@SuppressWarnings("unused")
	// reserved for next use
	private static HashMap<String, ColorStateList> sectionTextColors = new HashMap<String, ColorStateList>();
	private static Drawable secondDrawable = null;
	private static Utils mUtils = null;
	private static ColorStateList mMenuItemDefaultColorState;
	private static String mFlurryKey = "";
	private static String mGAKey = "";
	private static String mMyGAKey = "";
	private static boolean mTextMode = false;
	private static boolean mPNSEnabled = false;
	public static boolean mSectionModeFlip = false;
	public static int CAPTION_VISIBLE_DEFAULT_STATE = View.VISIBLE;
	public static ContentCache mContentCacher = null;
	private static Context mContext;
	private final static int GAPS_COUNT = 5;

	private TNPreferenceManager() {

	}

	public static void setContext(Context context) {
		// applicationContext = context.getApplicationContext();
		mUtils = new Utils(context.getApplicationContext());
		mContext = context;
		mSavedPage = null;
		// Nam.Nguyen
		// mSavedPage = new GeneralPage();
		// mSavedPage.setSectionId(EXTRAVALUE_SECTION_USER_PAGE_SAVED);

		EXTRAVALUE_SECTION_USER_PAGE_NAME = context.getResources().getString(
				R.string.menu_head_user_page);
		EXTRAVALUE_SECTION_USER_PAGE_SAVED_NAME = context.getResources()
				.getString(R.string.menu_head_user_page_saved);
		RequestDataFactory.initDeviceType();
		int hostid = mUtils.getIntPref(PREF_KEYNAME_HOST_DOMAIN);
		if (hostid == -1) {
			hostid = 666; // For LIVE
//			 hostid = 4; // For Debug on dev2
			mUtils.setIntPref(PREF_KEYNAME_HOST_DOMAIN, hostid);
		}
		RequestDataFactory.changeDomain(hostid);
		mTextMode = mUtils.getBooleanPref(EXTRAKEY_STORY_NIGHTMODE);
		if (mContentCacher == null) {
			mContentCacher = new ContentCache(context);
		}
		mContentCacher.getIntance();

		// Initialize for ImageLoader
		@SuppressWarnings("deprecation")
		DisplayImageOptions dio = new DisplayImageOptions.Builder()
				.delayBeforeLoading(200).cacheInMemory(true).cacheOnDisc(true)
				.imageScaleType(ImageScaleType.IN_SAMPLE_INT) // default
				.bitmapConfig(Bitmap.Config.RGB_565) // default
				.displayer(new SimpleBitmapDisplayer()) // default
				.build();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				context).threadPriority(Thread.NORM_PRIORITY - 1)
				.denyCacheImageMultipleSizesInMemory().discCacheFileCount(100)
				.discCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.FIFO) // default
				// .enableLogging() // Not necessary in common
				.discCacheFileNameGenerator(new HashCodeFileNameGenerator()) // default
				.imageDownloader(new BaseImageDownloader(context)) // default
				.imageDecoder(new BaseImageDecoder(true)) // default
				.defaultDisplayImageOptions(dio) // default
				.build();
		ImageLoader.getInstance().init(config);

		getCalculaterBoxWidth();
	}

	public static void getCalculaterBoxWidth() {
		GKIMLog.l(1, TAG + " getCalculaterBoxWidth");
		int deviceWidth = mContext.getResources().getDisplayMetrics().widthPixels;
		boolean tabletVersionget = mContext.getResources().getBoolean(
				R.bool.istablet);
		if (tabletVersionget) {
			Configuration config = mContext.getResources().getConfiguration();
			float realGapWidth = 2 * deviceWidth / 100;
			int gapWidth = (int) realGapWidth;
			int boxWidth = (int) ((deviceWidth - (realGapWidth * GAPS_COUNT)) / (GAPS_COUNT - 1));
			if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
				mUtils.setIntPref(PREF_KEYNAME_BOXCELLSIZE_LAND, boxWidth);
				mUtils.setIntPref(PREF_KEYNAME_BOXLAYOUT_GAPWIDTH_LAND,
						gapWidth);
			} else if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
				mUtils.setIntPref(PREF_KEYNAME_BOXCELLSIZE_POTRAIL, boxWidth);
				mUtils.setIntPref(PREF_KEYNAME_BOXLAYOUT_GAPWIDTH_POTRAIL,
						gapWidth);
			}
			refreshTextSizeBox();
		}

	}

	public static int getBoxSize() {
		return mUtils.getIntPref(PREF_KEYNAME_BOXCELLSIZE);
	}

	public static int getGapWidth() {
		if (mUtils == null) {
			return 2;
		}
		return mUtils.getIntPref(PREF_KEYNAME_BOXLAYOUT_GAPWIDTH);
	}

	public static void setBoxSize(int size) {

	}

	public static void refreshBoxSizeAndGap() {
		GKIMLog.l(1, TAG + " refreshBoxSizeAndGap");
		int boxWidth, gapwidth;
		boxWidth = mUtils.getIntPref(PREF_KEYNAME_BOXCELLSIZE);
		gapwidth = mUtils.getIntPref(PREF_KEYNAME_BOXLAYOUT_GAPWIDTH);
		Configuration config = mContext.getResources().getConfiguration();
		boolean tabletVersionget = mContext.getResources().getBoolean(
				R.bool.istablet);
		if (tabletVersionget) {
			if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
				boxWidth = mUtils.getIntPref(PREF_KEYNAME_BOXCELLSIZE_LAND);
				gapwidth = mUtils
						.getIntPref(PREF_KEYNAME_BOXLAYOUT_GAPWIDTH_LAND);
			} else if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
				boxWidth = mUtils.getIntPref(PREF_KEYNAME_BOXCELLSIZE_POTRAIL);
				gapwidth = mUtils
						.getIntPref(PREF_KEYNAME_BOXLAYOUT_GAPWIDTH_POTRAIL);
			}
			mUtils.setIntPref(PREF_KEYNAME_BOXCELLSIZE, boxWidth);
			mUtils.setIntPref(PREF_KEYNAME_BOXLAYOUT_GAPWIDTH, gapwidth);
		}
		refreshTextSizeBox();
	}

	public static void setBoxSizeAndGap(int boxWidth, int gapwidth) {
		GKIMLog.l(1, TAG + " setBoxSizeAndGap");
		if (mUtils == null) {
			return;
		}
		mUtils.setIntPref(PREF_KEYNAME_BOXCELLSIZE, boxWidth);
		mUtils.setIntPref(PREF_KEYNAME_BOXLAYOUT_GAPWIDTH, gapwidth);
		refreshTextSizeBox();
	}

	// Nam.Nguyen
	public static void refreshTextSizeBox() {
		int boxWidth, gapwidth;
		boxWidth = mUtils.getIntPref(PREF_KEYNAME_BOXCELLSIZE);
		gapwidth = mUtils.getIntPref(PREF_KEYNAME_BOXLAYOUT_GAPWIDTH);

		TextView textView = new TextView(mContext);
		String text = "nghi??ng nghi??ng nghi??ng nghi??ng nghi??ng nghi??ng nghi??ng nghi??ng nghi??ng";
		Typeface mDefaultTF = TNPreferenceManager.getTNTypeface();
		textView.setTypeface(mDefaultTF, Typeface.BOLD);
		textView.setTextSize(
				TypedValue.COMPLEX_UNIT_PX,
				mContext.getResources().getDimensionPixelSize(
						R.dimen.section_box_title_textsize));
		textView.setText(text);
		textView.setMaxLines(5);
		// Force the text to wrap. In principle this is not necessary since the
		// dummy TextView
		// already does this for us but in rare cases adding this line can
		// prevent flickering
		textView.setMaxWidth(boxWidth);
		float textSize = textView.getTextSize();
		// Padding should not be an issue since we never define it
		// programmatically in this app
		// but just to to be sure we cut it off here

		int targetFieldWidth = (boxWidth - gapwidth * 2);
		int targetFieldHeight = (boxWidth - gapwidth * 2);
		float mThreshold = 0.5f;
		float lowerTextSize = textSize / 2;
		float upperTextSize = 100;
		// Initialize the dummy with some params (that are largely ignored
		// anyway, but this is
		// mandatory to not get a NullPointerException)
		textView.setLayoutParams(new LayoutParams(targetFieldWidth,
				targetFieldHeight));

		// maxWidth is crucial! Otherwise the text would never line wrap but
		// blow up the width
		textView.setMaxWidth(targetFieldWidth);
		/*********************** Converging algorithm 2 ***************************************/
		// Upper and lower size converge over time. As soon as they're close
		// enough the loop
		// stops
		// TODO probe the algorithm for cost (ATM possibly O(n^2)) and optimize
		// if possible

		for (float testSize; (upperTextSize - lowerTextSize) > mThreshold;) {

			// Go to the mean value...
			testSize = (upperTextSize + lowerTextSize) / 2;

			// ... inflate the dummy TextView by setting a scaled textSize and
			// the text...
			textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, testSize);
			textView.setText(text);

			// ... call measure to find the current values that the text WANTS
			// to occupy
			textView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
			int tempHeight = textView.getMeasuredHeight();
			// int tempWidth = textView.getMeasuredWidth();

			// LOG.debug("Measured: " + tempWidth + "x" + tempHeight);
			// LOG.debug("TextSize: " + testSize / mScaledDensityFactor);

			// ... decide whether those values are appropriate.
			if (tempHeight > targetFieldHeight) {
				upperTextSize = testSize; // Font is too big, decrease upperSize
			} else {
				lowerTextSize = testSize; // Font is too small, increase
											// lowerSize
			}
		}
		setTextSizeBoxView(textView.getTextSize());
	}

	public static boolean isConnectionAvailable() {
		return mUtils.isConnectionAvailable();
	}

	public static String getTestJSONString(String testFilename) {
		return mUtils.getJSONStringFromAssetFile(testFilename);
	}

	public static void updateTNSmileyIcons(JsonArray ja) {
		if (ja == null || ja.size() <= 0) {
			return;
		}
		UtilsPreference pref = mUtils.getUtilPreference();
		UrlImageViewCallback callback = new UrlImageViewCallback() {
			Drawable selectedDrawable = getApplicationContext().getResources()
					.getDrawable(R.drawable.bg_storycomment_smiley_selected);

			@Override
			public void onLoaded(ImageView imageView, Drawable loadedDrawable,
					String url, boolean loadedFromCache, String strid) {
				// GKIMLog.lf(null, 0, TAG + "=>completed load (" + strid +
				// "): "
				// + url);
				// NOTE: imageView is alway null since we just load drawable
				if (loadedDrawable != null) {
					long id = Long.parseLong(strid);
					LayerDrawable ldraw = new LayerDrawable(new Drawable[] {
							selectedDrawable, loadedDrawable });
					StateListDrawable sldrawable = new StateListDrawable();
					sldrawable.addState(KEY_STATE_SELECTED, ldraw);
					sldrawable.addState(KEY_STATE_NORMAL, loadedDrawable);
					if (!smileyIcons.containsKey(id)) {
						smileyIcons.put(id, sldrawable);
					}
				}
			}
		};

		StringBuilder sb = new StringBuilder();
		for (JsonElement je : ja) {
			if (je != null) {
				try {
					JsonObject jo = je.getAsJsonObject();
					long id = jo.getAsJsonPrimitive("icon_id").getAsLong();
					String icon_url = jo.getAsJsonPrimitive("icon_url")
							.getAsString();
					UIUtils.loadToCache(getApplicationContext(), icon_url,
							callback, String.valueOf(id));
					sb.append(id + CONST_SEPARATOR + icon_url).append(
							CONST_SEPARATOR);
				} catch (Exception e) {
					GKIMLog.lf(
							null,
							0,
							TAG + "=>updateSmilley icons, exception: "
									+ e.getMessage());
				}
			}
		}
		pref.setStringPref(PREF_KEYNAME_STORY_SMILEY_ICONS, sb.toString());
	}

	public static void updateTNTemplate(TNTemplate template) {
		if (template == null || template.getSectionCount() < 1) {
			return;
		}
		Context context = mUtils.getApplicationContext();
		// Resources res = mUtils.getResource();
		UtilsPreference pref = mUtils.getUtilPreference();
		SectionTemplate[] sections = template.getSections();
		if (mSectionIds != null) {
			mSectionIds = null;
		}
		mSectionIds = new String[sections.length];
		int sectioncount = 0;
		long bg[] = new long[2];
		final StringBuilder id = new StringBuilder();
		UrlImageViewCallback callback = new UrlImageViewCallback() {
			@Override
			public void onLoaded(ImageView imageView, Drawable loadedDrawable,
					String url, boolean loadedFromCache, String secId) {
				// GKIMLog.lf(null, 0, TAG + "=>completed load (" + secId +
				// "): "
				// + url);
				if (loadedDrawable == null) {
					GKIMLog.lf(null, 5, TAG + "=>false (" + secId + "): " + url);
					return;
				}
				if (url.contains("_over")) {
					sectionIconsOver.put(secId, loadedDrawable);
					// GKIMLog.lf(null, 0, TAG +
					// "=>completed load , Over size: "
					// + sectionIconsOver.size() + " (" + secId + ")");
				} else {
					sectionIconsNormal.put(secId, loadedDrawable);
					// GKIMLog.lf(null, 0, TAG
					// + "=>completed load , Normal size: "
					// + sectionIconsNormal.size() + " (" + secId + ")");
				}
				// NOTE: imageView is alway null since we just load drawable
				if (!sectionIcons.containsKey(secId)) {
					// put first icon
					sectionIcons.put(secId, loadedDrawable);
				} else {
					// generate the statelistDrawable and replace the first one.
					StateListDrawable stateDrw = new StateListDrawable();
					Drawable oldState = sectionIcons.remove(secId);
					if (url.contains("_over")) {
						stateDrw.addState(KEY_STATE_PRESSED, loadedDrawable);
						stateDrw.addState(KEY_STATE_NORMAL, oldState);
					} else {
						stateDrw.addState(KEY_STATE_PRESSED, oldState);
						stateDrw.addState(KEY_STATE_NORMAL, loadedDrawable);
					}
					sectionIcons.put(secId, stateDrw);
					// GKIMLog.lf(null, 0, TAG + "=>section menu icon added: "
					// + secId + "(" + sectionIcons.size() + ").");

				}
			}
		};

		// int sectionIconWidth = res
		// .getInteger(R.integer.menu_list_item_section_icon_size);
		for (SectionTemplate as : sections) {
			id.setLength(0);
			id.append(as.getSectionId());
			mSectionIds[sectioncount++] = id.toString();
			String key = compileSectionKeyToSP(id.toString());
			String value = compileSectionValueToSP(as);
			// XXX: if the key has existed, then update it (again). Generate
			// GradientDrawable as a time.
			pref.setStringPref(key, value);
			UIUtils.loadToCache(context, as.getSectionIconLink(), callback,
					id.toString());
			UIUtils.loadToCache(context, as.getSectionIconHoverLink(),
					callback, id.toString());
			bg = as.getSectionColors1();
			Drawable sectionDbl = UIUtils.buildGadientDrawableFromColor(
					(int) bg[0], (int) bg[1]);
			sectionDrawables.put(id.toString(), sectionDbl);
			// if (secondDrawable == null) {
			bg = as.getSectionColors2();
			secondDrawable = UIUtils.buildGadientDrawableFromColor((int) bg[0],
					(int) bg[1]);
			// }
		}
		GKIMLog.lf(null, 0, TAG + "=>updateTNTemplate done with: "
				+ sections.length + ", " + sectionDrawables.size() + ", have "
				+ sectioncount + " ids.");

	}

	public static SectionTemplate getSectionTemplateFromPref(String sectionID) {
		if (sectionID == null || sectionID.length() == 0) {
			return null;
		}
		String key = compileSectionKeyToSP(sectionID);
		String value = mUtils.getStringPref(key);
		return decompileSectionValue(value);
	}

	public static String getSectionTitleFromPref(String sectionId) {
		if (sectionId == null || sectionId.length() == 0) {
			return null;
		}
		String key = compileSectionKeyToSP(sectionId);
		String value = mUtils.getStringPref(key);
		if (value == null || value.length() == 0) {
			return "";
		}
		String[] parsed = value.split(CONST_SEPARATOR);
		if (parsed == null || parsed.length < 3) {
			return "";
		}
		return parsed[PREF_INDEX_TITLE];
	}

	public static int getSectionTextColorFromPref(String sectionId) {
		if (sectionId == null || sectionId.length() == 0) {
			return Color.WHITE;
		}
		String key = compileSectionKeyToSP(sectionId);
		String value = mUtils.getStringPref(key);
		if (value == null || value.length() == 0) {
			return Color.WHITE;
		}
		String[] parsed = value.split(CONST_SEPARATOR);
		if (parsed == null || parsed.length < 3) {
			return Color.WHITE;
		}
		return Color.parseColor(new String(parsed[PREF_INDEX_COLORSTRING]));
	}

	public static int getCategoryColorFromPref(String sectionId) {
		if (sectionId == null || sectionId.length() == 0) {
			return mUtils.getResource().getColor(
					R.color.storydetail_header_title);
		}
		String key = compileSectionKeyToSP(sectionId);
		String value = mUtils.getStringPref(key);
		if (value == null || value.length() == 0) {
			return mUtils.getResource().getColor(
					R.color.storydetail_header_title);
		}
		String[] parsed = value.split(CONST_SEPARATOR);
		if (parsed == null || parsed.length < 4) {
			return mUtils.getResource().getColor(
					R.color.storydetail_header_title);
		}
		try {
			Color.parseColor(parsed[PREF_INDEX_BGCOLOR1]);
			return Color.parseColor(parsed[PREF_INDEX_BGCOLOR1]);
		} catch (IllegalArgumentException exception) {
			return Color.BLACK;
		}

	}

	public static ColorStateList getSectionColorStateList(String sectionId) {
		// NOTE: not really implement yet.
		if (mMenuItemDefaultColorState == null) {
			mMenuItemDefaultColorState = mUtils.getResource()
					.getColorStateList(R.color.menu_list_item_text);
		}
		return mMenuItemDefaultColorState;
	}

	public static Drawable getSectionIcon(String sectionId) {
		return sectionIcons.get(sectionId);
	}

	public static HashMap<String, Drawable> getSectionIcons() {
		return sectionIcons;
	}

	public static HashMap<String, Drawable> getSectionIconsNormal() {
		return sectionIconsNormal;
	}

	public static HashMap<String, Drawable> getSectionIconsOver() {
		return sectionIconsOver;
	}

	public static HashMap<Long, Drawable> getSmileyIcons() {
		return smileyIcons;
	}

	public static Drawable getSmileyIcon(String strid) {
		long id = Long.parseLong(strid);
		return smileyIcons.get(id);
	}

	/**
	 * @Description: Compile sharedpreference key for a section template item.
	 *               <p>
	 *               The key should be in format:
	 *               </p>
	 *               <b>"sectionid"</b> + @ + <b>section's id number</b>
	 *               <p>
	 *               Example: "sectionid@1234 </b>
	 * @param SectionTemplate
	 *            section
	 * @return section template key string.
	 */
	private static String compileSectionKeyToSP(String sectionID) {
		return PREF_KEYNAME_SECTIONTEMPLATE_PREFIX.replace(
				CONST_REPLACE_TARGET_ID, sectionID);
	}

	/**
	 * @Description: Compile sharedpreference value for a section template item.
	 * @param SectionTemplate
	 *            section
	 * @return section template value string
	 */
	private static String compileSectionValueToSP(SectionTemplate section) {
		String result = "";
		String bgColor1[] = section.getSectionColors1String();
		String bgColor2[] = section.getSectionColors2String();
		result += section.getSectionId() + CONST_SEPARATOR
				+ section.getSectionTitle() + CONST_SEPARATOR
				+ section.getSectionColorString() + CONST_SEPARATOR
				+ bgColor1[0] + CONST_SEPARATOR + bgColor1[1] + CONST_SEPARATOR
				+ bgColor2[0] + CONST_SEPARATOR + bgColor2[1];
		return result;
	}

	public static SectionTemplate decompileSectionValue(String value) {
		if (value == null || value.length() == 0) {
			return null;
		}
		String[] parsed = value.split(CONST_SEPARATOR);
		if (parsed == null || parsed.length < 3) {
			return null;
		}
		SectionTemplate result = new SectionTemplate(parsed[PREF_INDEX_ID],
				parsed[PREF_INDEX_TITLE], parsed[PREF_INDEX_COLORSTRING]);
		result.setSectionColors1(parsed[PREF_INDEX_BGCOLOR1],
				parsed[PREF_INDEX_BGCOLOR2]);
		result.setSectionColors2(parsed[PREF_INDEX_BGCOLOR3],
				parsed[PREF_INDEX_BGCOLOR4]);
		return result;
	}

	public static Drawable getBackgroundDrawable1(String sectionId) {
		return sectionDrawables.get(sectionId);
	}

	public static Drawable getBackgroundDrawable2(String sectionId) {
		return secondDrawable;
	}

	public static Context getApplicationContext() {
		if (mUtils != null) {
			return mUtils.getApplicationContext();
		}
		return null;
	}

	public static int getMinTitleLengthForNewStory() {
		// TODO Update to get data from SharedPreference mUtil
		return 8;
	}

	public static int getMaxContentLengthForNewStory() {
		// TODO Update to get data from SharedPreference mUtil
		return 500;
	}

	public static String[] getSectionIDs() {
		return mSectionIds;
	}

	public static boolean hasSectionScroll() {
		return ENABLE_SCROLLING_SECTION;
	}

	public static boolean isIgnoreTextGravity() {
		return IGNORE_TEXT_GRAVITY;
	}

	public static Typeface getTNTypeface() {
		return UIUtils.getDefaultTypeFace(mUtils.getApplicationContext(),
				Typeface.NORMAL);
	}

	public static Typeface getTNTypefaceBOLD() {
		return UIUtils.getDefaultTypeFace(mUtils.getApplicationContext(),
				Typeface.BOLD);
	}

	public static Typeface getTNTypefaceItalic() {
		return UIUtils.getDefaultTypeFace(mUtils.getApplicationContext(),
				Typeface.ITALIC);
	}

	public static String getDeviceType() {
		if (UIUtils.isTablet(mUtils.getApplicationContext())) {
			return RequestDataFactory.DEFAULT_COMMON_DEVICE_TABLET;
		}
		return RequestDataFactory.DEFAULT_COMMON_DEVICE_PHONE;
	}

	public static boolean checkLoggedIn() {
		String uid = getUserId();
		if (uid == null || uid.length() <= 0) {
			return false;
		}
		return true;
	}

	public static void setUserId(String uid) {
		if (mUtils != null) {
			mUtils.setStringPref(PREF_KEYNAME_ACCOUNT_USERID, uid);
		}
	}

	public static void setGpsShow(boolean show) {
		if (mUtils != null) {
			mUtils.setBoolPref(PREF_KEYNAME_GPS_SHOW, show);
		}
	}

	public static void setUserInfo(String strInfo) {
		if (mUtils != null) {
			mUtils.setStringPref(PREF_KEYNAME_ACCOUNT_USERINFO, strInfo);
		}
	}

	public static void setUserAccount(UserAccount user) {
		setUserId(user.getUserId());
		String json = new Gson().toJson(user);
		setUserInfo(json);
	}

	public static String getUserId() {
		if (mUtils != null && mUtils.hasKey(PREF_KEYNAME_ACCOUNT_USERID)) {
			return mUtils.getStringPref(PREF_KEYNAME_ACCOUNT_USERID);
		}
		return null;
	}

	public static boolean getGpsShow() {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext.getApplicationContext());
		if (sp.contains(PREF_KEYNAME_GPS_SHOW)) {
			if (GKIMLog.DEBUG_ON)
				GKIMLog.lf(
						null,
						1,
						TAG
								+ "=> getBooleanPref("
								+ PREF_KEYNAME_GPS_SHOW
								+ "):="
								+ String.valueOf(sp.getBoolean(
										PREF_KEYNAME_GPS_SHOW, false)));
			return sp.getBoolean(PREF_KEYNAME_GPS_SHOW, true);
		}
		return true;
	}

	public static UserAccount getUserInfo() {
		if (mUtils != null && mUtils.hasKey(PREF_KEYNAME_ACCOUNT_USERINFO)) {
			String strJSON = mUtils
					.getStringPref(PREF_KEYNAME_ACCOUNT_USERINFO);
			return ((new Gson()).fromJson(strJSON, UserAccount.class));
		}
		return null;
	}

	public static boolean validatePostTitle(String title) {
		// TODO business logic validating
		return true;
	}

	public static boolean validatePostContent(String content) {
		// TODO business logic validating
		return true;
	}

	public static long getPostFilesMaxSize() {
		return MAX_MEGABYTE * MEGABYTE;
	}

	public static int getPostMaxFiles() {
		return MAX_FILEPOST_PHOTO;
	}

	public static int getPostMaxVideoFiles() {
		return MAX_FILEPOST_VIDEO;
	}

	public static String getFileType(File afile) {
		if (afile == null || afile.isDirectory()) {
			return "";
		}
		// String extension = MimeTypeMap.getFileExtensionFromUrl(afile
		// .getAbsolutePath());
		String fileName = afile.getName();
		String extension = fileName.substring(fileName.lastIndexOf(".") + 1)
				.toLowerCase();
		if (extension == null) {
			return "";
		}
		MimeTypeMap mime = MimeTypeMap.getSingleton();
		return mime.getMimeTypeFromExtension(extension);
	}

	public static String getXtifyId() {
		return XTifyController.getXID();
	}

	public static String getDeviceIMEI() {
		return mUtils.getDeviceIMEI();
	}

	public static void setContentToSearch(String strToSearch) {
		mUtils.getUtilPreference().setStringPref(PREF_KEYNAME_SEARCHCONTENT,
				strToSearch);
	}

	public static String getContentToSearch() {
		return mUtils.getStringPref(PREF_KEYNAME_SEARCHCONTENT);
	}

	public static String getErrorMessageFromCode(int resultCode) {
		Context context = getApplicationContext();
		if (context == null)
			return "";
		Resources res = context.getResources();
		switch (resultCode) {
		case 201:
			return res.getString(R.string.errcode_201);
		case 202:
			return res.getString(R.string.errcode_202);
		case 203:
			return res.getString(R.string.errcode_203);
		case 204:
			return res.getString(R.string.errcode_204);
		case 301:
			return res.getString(R.string.errcode_301);
		case 302:
			return res.getString(R.string.errcode_302);
		case 303:
			return res.getString(R.string.errcode_303);
		case 304:
			return res.getString(R.string.errcode_304);
		case 305:
			return res.getString(R.string.errcode_305);
		case 306:
			return res.getString(R.string.errcode_306);
		case 307:
			return res.getString(R.string.errcode_307);
		default:
			return "";
		}
	}

	public static void postDataCreated() {
		if (mUtils != null) {
			mUtils.setBoolPref(PREF_BASIC_DATA_CREATED, true);
		}
	}

	public static boolean getDataCreated() {
		if (mUtils != null) {
			return mUtils.getBooleanPref(PREF_BASIC_DATA_CREATED);
		}
		return false;
	}

	public static void changeDomain(int i) {
		mUtils.setIntPref(PREF_KEYNAME_HOST_DOMAIN, i);
		RequestDataFactory.changeDomain(i);
	}

	/**
	 * @param mStoryId
	 */
	public static void putReadStory(String storyid) {
		if (mUtils != null) {
			StringBuilder sb = new StringBuilder(
					mUtils.getStringPref(PREF_KEYNAME_STORIES_READ));
			if (sb.length() >= STORIES_READ_MAXLENGTH) {
				String strClean = sb
						.substring(sb.indexOf(",", sb.length() / 4) + 1);
				sb.setLength(0);
				sb.append(strClean);
			}
			if (sb.indexOf(storyid) < 0) {
				sb.append(storyid).append(",");
				mUtils.setStringPref(PREF_KEYNAME_STORIES_READ, sb.toString());
			}
		}
	}

	public static boolean hasReadStory(String storyid) {
		if (mUtils != null) {
			String sStories = mUtils.getStringPref(PREF_KEYNAME_STORIES_READ);
			if (sStories != null && sStories.contains(storyid + ",")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return
	 */
	public static String getReadStories() {
		if (mUtils != null) {
			return mUtils.getStringPref(PREF_KEYNAME_STORIES_READ);
		}
		return null;
	}

	/**
	 * @return
	 */
	public static String getIdSavedStories() {
		if (mIDBoxStorys != null) {
			return mIDBoxStorys;
		}
		return "";
	}

	/**
	 * @return
	 */
	public static String getFlurryAPIKey() {
		if (mFlurryKey.length() == 0) {
			mFlurryKey = mUtils.getResource().getString(
					R.string.api_key_tracking_flurry);
		}
		return mFlurryKey;
	}

	/**
	 * @return
	 */
	public static String getGAAPIKey() {
		if (mGAKey.length() == 0) {
			mGAKey = mUtils.getResource().getString(R.string.ga_trackingId);
		}
		return mGAKey;
	}

	public static String getAdModKey() {
		if (mUtils != null) {
			return mUtils.getResource().getString(R.string.api_key_admob);
		}
		return "";
	}

	public static String getMyGAAPIKey() {
		if (mMyGAKey.length() == 0) {
			mMyGAKey = mUtils.getResource().getString(
					R.string.ga_tracking_my_Id);
		}
		return mMyGAKey;
	}

	/**
	 * @param bChecked
	 */
	public static void setTextMode(boolean bChecked) {
		mTextMode = bChecked;
		if (mUtils != null) {
			mUtils.setBoolPref(EXTRAKEY_STORY_NIGHTMODE, mTextMode);
		}
	}

	public static boolean isNightMode() {
		return mTextMode;
	}

	/**
	 * @param bChecked
	 */
	public static void setTextSizeMode(int textZoomSize) {
		if (mUtils != null) {
			mUtils.setIntPref(EXTRAKEY_STORY_TEXTZOOMSIZE, textZoomSize);
		}
	}

	public static int getTextSizeMode() {
		if (mUtils != null) {
			return mUtils.getIntPref(EXTRAKEY_STORY_TEXTZOOMSIZE);
		}
		return 0;
	}

	public static boolean getPNSEnabled() {
		if (mUtils != null) {
			mPNSEnabled = mUtils.getBooleanPref(PREF_KEYNAME_PNS_ENABLED);
		}
		if (GKIMLog.DEBUG_ON) {
			mPNSEnabled = true;
			mUtils.setBoolPref(PREF_KEYNAME_PNS_ENABLED, true);
		}
		return mPNSEnabled;
	}

	public static void enablePNS() {
		mPNSEnabled = true;
		if (mUtils != null) {
			mUtils.setBoolPref(PREF_KEYNAME_PNS_ENABLED, true);
		}
	}

	public static void setListeningSections(String sectionIds) {
		if (mUtils != null) {
			mUtils.setStringPref(PREF_KEYNAME_PNS_SECTIONS, sectionIds);
		}
	}

	public static String getListeningSections() {
		String result = null;
		if (mUtils != null) {
			result = mUtils.getStringPref(PREF_KEYNAME_PNS_SECTIONS);
		}
		return result;
	}

	public static void setPNSCheckAll(boolean isCheckAll) {
		if (mUtils != null) {
			mUtils.setBoolPref(PREF_KEYNAME_PNS_CHECK_ALL, isCheckAll);
		}
	}

	public static boolean getPNSCheckAll() {
		boolean isCheckAll = false;
		if (mUtils != null) {
			isCheckAll = mUtils.getBooleanPref(PREF_KEYNAME_PNS_CHECK_ALL);
		}
		return isCheckAll;
	}

	public static void setInstallShorcut(boolean isInstallShorcut) {
		if (mUtils != null) {
			mUtils.setBoolPref(PREF_KEYNAME_INSTALL_SHORTCUT, isInstallShorcut);
		}
	}

	public static void installShorcut() {
		if (mUtils != null) {
			mUtils.addShortcut();
		}
	}

	public static boolean getInstallShorcut() {
		boolean isInstallShorcut = false;
		if (mUtils != null) {
			isInstallShorcut = mUtils
					.getBooleanPref(PREF_KEYNAME_INSTALL_SHORTCUT);
		}
		return isInstallShorcut;
	}

	public static void uninstallShorcut() {
		if (mUtils != null) {
			mUtils.removeShortcut();
		}
	}

	public static GeneralPage getSavedPage() {
		boolean bTablet = UIUtils.isTablet(mUtils.getApplicationContext());
		if (bTablet && mSavedPage != null) {
			refreshBoxSizeAndGap();
			mSavedPage.setGapwidth(mUtils
					.getIntPref(PREF_KEYNAME_BOXLAYOUT_GAPWIDTH));
			mSavedPage.setBoxWidth(mUtils.getIntPref(PREF_KEYNAME_BOXCELLSIZE));
			mSavedPage.setLayoutWidth(EXTRAVALUE_SECTION_COLUMN_TABLET);
		}
		// mSavedPage.setSectionTitle(EXTRAVALUE_SECTION_USER_PAGE_SAVED);
		return mSavedPage;
	}

	public static boolean checkHasSavedStory() {
		// Nam.Nguyen
		String str = mUtils.getStringPref(PREF_KEYNAME_STORIES_SAVE);
		if (str != null & str.length() > 0)
			return true;
		return false;

	}

	/*
	 * Save : is save or remove
	 */
	public static void putSavedStory(String storyid, int save) {

		StringBuilder sb = new StringBuilder(
				mUtils.getStringPref(PREF_KEYNAME_STORIES_SAVE));
		GKIMLog.l(1, TAG + " putSavedStory 111:" + sb.toString());
		if (save == 1) {
			if (sb == null || sb.length() <= 0) {
				sb.append(storyid).append(",");
			} else {
				if (sb.indexOf(storyid) < 0) {
					sb.append(storyid).append(",");
					mUtils.setStringPref(PREF_KEYNAME_STORIES_SAVE,
							sb.toString());
				}
			}
		} else if (save == 0) {
			if (sb != null && sb.length() > 0) {
				int index = sb.indexOf(storyid);
				sb = sb.delete(index, index + storyid.length() + 1);
			}
		}
		mUtils.setStringPref(PREF_KEYNAME_STORIES_SAVE, sb.toString());
		GKIMLog.l(
				1,
				TAG + " putSavedStory 222:"
						+ mUtils.getStringPref(PREF_KEYNAME_STORIES_SAVE));
	}

	public static boolean hasSavedStory(String storyid) {

		if (storyid == null || storyid.length() <= 0 || mSavedPage == null
				|| mSavedPage.getBoxStoryCount() == 0) {
			return false;
		}
		return (mSavedPage.getBoxStorybyId(storyid) != null ? true : false);

	}

	public static boolean checkHasSaved() {
		// Nam.Nguyen news code

		if (mUtils != null) {
			String sStories = mUtils.getStringPref(PREF_KEYNAME_STORIES_SAVE);
			if (sStories != null && sStories.length() > 0) {
				GKIMLog.l(1, TAG + " hasSavedStory:" + sStories);
				return true;
			}
		}
		return false;

	}

	/**
	 * @param sd
	 * @return
	 */
	public static int saveStory(StoryDetail sd) {
		int result = 0;
		boolean bTablet = UIUtils.isTablet(mUtils.getApplicationContext());
		if (mSavedPage == null) {
			mSavedPage = new GeneralPage();
			mSavedPage.setSectionId(EXTRAVALUE_SECTION_USER_PAGE_SAVED);
			// mSavedPage.setSectionTitle(EXTRAVALUE_SECTION_USER_PAGE_SAVED);
		}
		mSavedPage.setGapwidth(mUtils
				.getIntPref(PREF_KEYNAME_BOXLAYOUT_GAPWIDTH));
		mSavedPage.setBoxWidth(mUtils.getIntPref(PREF_KEYNAME_BOXCELLSIZE));
		mSavedPage.setLayoutWidth((bTablet ? EXTRAVALUE_SECTION_COLUMN_TABLET
				: EXTRAVALUE_SECTION_COLUMN_PHONE));
		if (sd == null) {
			return -1;
		}
		// generate box 1x1 from single detail
		// generate box 1x2 if tablet?? next version
		BoxStory box = new BoxStory(sd);
		if (bTablet) {
			box.setLayout(0x12);
			box.addBoxElement(new BoxElement(3, sd.getTopImageUrl(), 7));
		}
		try {
			parserBoxStoryToJson(box);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			GKIMLog.lf(null, 0, TAG + "=>ParserBoxStoryToJson , exception: "
					+ e.getMessage());
			e.printStackTrace();
		}
		GKIMLog.l(1,
				TAG + " saveStory new save page : " + mSavedPage.getBoxWidth()
						+ " " + mSavedPage.getGapwidth());
		mSavedPage.addBoxStory(box);
		result = mSavedPage.getBoxStoryCount();

		return result;
	}

	public static void loadBoxStoryFormJson() {
		if (mSavedPage != null) {
			return;
		}
		try {
			boolean bTablet = UIUtils.isTablet(mUtils.getApplicationContext());
			if (mSavedPage == null) {
				mSavedPage = new GeneralPage();
				mSavedPage.setSectionId(EXTRAVALUE_SECTION_USER_PAGE_SAVED);
				mSavedPage.setGapwidth(mUtils
						.getIntPref(PREF_KEYNAME_BOXLAYOUT_GAPWIDTH));
				mSavedPage.setBoxWidth(mUtils
						.getIntPref(PREF_KEYNAME_BOXCELLSIZE));
				mSavedPage
						.setLayoutWidth((bTablet ? EXTRAVALUE_SECTION_COLUMN_TABLET
								: EXTRAVALUE_SECTION_COLUMN_PHONE));
			}
			String strJson = mUtils.getStringPref(PREF_KEYNAME_SAVE_BOXSTORY);
			mIDBoxStorys = mUtils.getStringPref(PREF_KEYNAME_SAVE_ID_BOXSTORY);
			if (strJson.trim().length() > 0) {
				mListJsonBoxStory.clear();
				JSONArray jsArr = new JSONArray(strJson);
				for (int i = 0; i < jsArr.length(); i++) {
					JSONObject json = jsArr.getJSONObject(i);
					BoxStory box = new Gson().fromJson(json.toString(),
							BoxStory.class);
					mListJsonBoxStory.add(json);
					mSavedPage.addBoxStory(box);
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			GKIMLog.lf(null, 0, TAG + "=>loadBoxStoryFormJson , exception: "
					+ e.getMessage());
		}
	}

	public static void parserBoxStoryToJson(BoxStory box) throws JSONException {
		if (mListJsonBoxStory != null) {
			String strJson = new Gson().toJson(box);
			mIDBoxStorys += box.getStoryId() + ";";
			mListJsonBoxStory.add(new JSONObject(strJson));
			mUtils.setStringPref(PREF_KEYNAME_SAVE_BOXSTORY,
					mListJsonBoxStory.toString());
			mUtils.setStringPref(PREF_KEYNAME_SAVE_ID_BOXSTORY, mIDBoxStorys);
		}
	}

	public static JSONObject getBoxStoryJsonFromArrayList(String storyid) {
		if (mListJsonBoxStory != null) {
			try {
				for (JSONObject jsonBox : mListJsonBoxStory) {
					if (jsonBox.has("storyId")) {
						if (jsonBox.getString("storyId").equalsIgnoreCase(
								storyid)) {
							return jsonBox;
						}
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				GKIMLog.lf(null, 0,
						TAG + "=>getBoxStoryJsonFromArrayList , exception: "
								+ e.getMessage());
			}
		}
		return null;
	}

	public static void updateFavoriteBoxStory(BoxStory[] boxes) {
		GeneralPage page = TNPreferenceManager.getSavedPage();
		if (page == null) {
			return;
		}
		for (int i = 0; i < page.getBoxStoryCount(); i++) {
			BoxStory box = page.getBoxStory(i);
			for (int j = 0; j < boxes.length; j++) {
				BoxStory gbox = boxes[j];
				if (box.getStoryId().equalsIgnoreCase(gbox.getStoryId())) {
					gbox.setFavorite(true);
					break;
				}
			}
		}

		// Nam.Nguyen
		/*
		 * for (BoxStory boxStory : boxes) { if
		 * (hasSavedStory(boxStory.getStoryId())) { boxStory.setFavorite(true);
		 * } }
		 */
	}

	public static BoxStory deleteStory(BoxStory box) {
		if (box != null) {
			return deleteStory(box.getStoryId());
		}
		return null;
	}

	public static BoxStory deleteStory(String storyid) {
		if (storyid == null || storyid.length() <= 0 || mSavedPage == null
				|| mSavedPage.getBoxStoryCount() == 0
				|| mListJsonBoxStory == null) {
			return null;
		}
		JSONObject json = getBoxStoryJsonFromArrayList(storyid);
		if (json != null) {
			mListJsonBoxStory.remove(json);
			mUtils.setStringPref(PREF_KEYNAME_SAVE_BOXSTORY,
					mListJsonBoxStory.toString());
			mIDBoxStorys = mIDBoxStorys.replace(storyid + ";", "");
			mUtils.setStringPref(PREF_KEYNAME_SAVE_ID_BOXSTORY, mIDBoxStorys);
		}
		return mSavedPage.removeBoxStory(storyid);
	}

	/**
	 * @param strURL
	 * @return true if has cached or not expired yet.
	 */
	public static boolean checkCache(String keyCacher) {
		boolean bExisted = mContentCacher.isDBKeyCacherExist(keyCacher);
		boolean bNotExpired = mContentCacher.hasExpiredLong(keyCacher) < 0 ? true
				: false;
		GKIMLog.l(1, TAG + " checkCache strURL : " + keyCacher + " bExisted:"
				+ bExisted + " bNotExpired:" + bNotExpired);
		return (bExisted & bNotExpired);
	}

	public static void clearCacheToDay() {
		// TODO Auto-generated method stub
		if (mContentCacher != null) {
			mContentCacher.clearCacheToDay();
		}
	}

	public static void updateSaved(String storyid, int saved) {
		GKIMLog.l(1, TAG + " updateSaved :" + storyid + " " + saved);
		putSavedStory(storyid, saved);
		String keyCacher = RequestDataFactory.STORY_ID_CACHER + storyid;
		if (mContentCacher != null) {
			mContentCacher.updateSaved(keyCacher, saved);
		}
	}

	public static void addOrUpdateCache(String strURL, String strContent,
			long timeout, String keyCacher) {
		if (keyCacher == null || keyCacher == "") {
			return;
		}
		boolean bExisted = mContentCacher.isDBKeyCacherExist(keyCacher);
		boolean bExpired = mContentCacher.hasExpiredLong(keyCacher) >= 0 ? true
				: false;
		try {
			if (bExisted) {
				if (bExpired) {
					GKIMLog.l(1, TAG + " addOrUpdateCache updateDBURL : "
							+ strURL);
					mContentCacher.updateDBURL(strURL, strContent, timeout,
							keyCacher);
				}
			} else {
				GKIMLog.l(1, TAG + " addOrUpdateCache addDBURL : " + strURL);
				mContentCacher.addDBURL(strURL, strContent, timeout, keyCacher);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}

	}

	public static void addOrUpdateCache(String strUrl, String strContent,
			String keyCacher) {
		GKIMLog.l(1, TAG + "2222 addOrUpdateCache: keyCacher ==>" + keyCacher
				+ " strUrl:" + strUrl);
		if (!strContent.contains("Empty data query based on the key refs"))
			addOrUpdateCache(strUrl, strContent, CACHE_TIMEOUT_ONEDAY,
					keyCacher);// /*CACHE_TIMEOUT_ONEDAY
	}

	public static String getContentFromCache(String keyCacher) {
		synchronized (mContentCacher) {
			if (mContentCacher.isDBKeyCacherExist(keyCacher)) {
				return mContentCacher.getContentFromKeyCacher(keyCacher);
			}
		}
		return null;
	}

	public static boolean isSectionAddBoxes() {
		return mSectionModeFlip;
	}

	public static void setTextSizeBoxView(float size) {
		if (mUtils != null) {
			mUtils.setFloatPref(PREF_KEYNAME_TEXT_SIZE_BOXVIEW, size);
		}
	}

	public static float getTextSizeBoxView() {
		return mUtils.getFloatPref(PREF_KEYNAME_TEXT_SIZE_BOXVIEW);
	}

	// get sate app has login by using facebook account
	public static int getLoginFBState() {
		if (mUtils != null) {
			return mUtils.getIntPref(PREF_KEYNAME_LOGIN_FB);
		}
		return -1;
	}

	public static void setLoginFBState(int state) {
		if (mUtils != null) {
			mUtils.setIntPref(PREF_KEYNAME_LOGIN_FB, state);
		}
	}

	public static String getCurrentStandingSectionId() {
		if (mUtils != null) {
			return mUtils.getStringPref(PREF_KEYNAME_SECTION_STANDINGID);
		}
		return null;
	}

	public static void updateCurrentStandingSectionId(String sectionid) {
		GKIMLog.l(1, TAG + " updateCurrentStandingSectionId:" + sectionid);
		if (mUtils != null) {
			mUtils.setStringPref(PREF_KEYNAME_SECTION_STANDINGID, sectionid);
		}
	}

	public static String getXuanSectionId() {
		if (GKIMLog.DEBUG_ON) {
			return EXTRAVALUE_SECTION_SPRING;
		} else {
			return EXTRAVALUE_SECTION_SPRING_LIVE;
		}
	}

	public static String getTetOfYouSectionId() {
		if (GKIMLog.DEBUG_ON) {
			return EXTRAVALUE_SECTION_SPRING_TETOFYOU;
		} else {
			return EXTRAVALUE_SECTION_SPRING_TETOFYOU_LIVE;
		}
	}

	public static String getXuanWishesStoryId() {
		if (GKIMLog.DEBUG_ON) {
			return EXTRAVALUE_SECTION_SPRING_WISHES_STORY;
		} else {
			return EXTRAVALUE_SECTION_SPRING_WISHES_STORY_LIVE;
		}
	}

	public static boolean isXuanSection(String sectionId) {
		if (getXuanSectionId().equalsIgnoreCase(sectionId)) {
			return true;
		}
		return false;
	}

	public static boolean isTetOfYouSection(String sectionId) {
		if (getTetOfYouSectionId().equalsIgnoreCase(sectionId)) {
			return true;
		}
		return false;
	}

	public static boolean isWishesSpringStory(String storyid) {
		if (getXuanWishesStoryId().equalsIgnoreCase(storyid)) {
			return true;
		}
		return false;
	}

	public static boolean isStandingOnSpringStory(Context context) {
		if (context != null) {
			return context.getSharedPreferences(SHAREDPREF_SPRING,
					Context.MODE_PRIVATE).getBoolean(getXuanWishesStoryId(),
					false);
		}
		return false;
	}

	/**
	 * Get Application version name from application context
	 * 
	 * @param act
	 * @return
	 */
	public static String getAppVersionName(Context context) {
		String result = "1.00";
		if (context != null) {
			mContext = context.getApplicationContext();
		}
		if (mContext != null) {
			try {
				PackageInfo pi = mContext.getPackageManager().getPackageInfo(
						mContext.getPackageName(), 0);
				result = pi.versionName;
			} catch (NameNotFoundException e) {
				GKIMLog.l(
						4,
						TAG + "=>getAppVersionName NameNotFoundException: "
								+ e.getMessage());
			}

		}
		return result;
	}

	public static String getMediaSectionId() {
		return EXTRAVALUE_SECTION_MEDIA;
	}

	public static boolean isMediaSection(String sectionId) {
		if (getMediaSectionId().equalsIgnoreCase(sectionId)) {
			return true;
		}
		return false;
	}

	public static boolean hasMediaFeature() {
		return mHasMediaFeature;
	}

	public static String[] getSecMediaIDs() {
		return mSecMediaIds;
	}

	public static Drawable getSecMediaBackgroundDrawable(String sectionId) {
		return secMediaDrawables.get(sectionId);
	}

	public static Drawable getSecMediaIcon(String sectionId) {
		return secMediaIcons.get(sectionId);
	}

	public static HashMap<String, Drawable> getSecMediaIcons() {
		return secMediaIcons;
	}

	public static HashMap<String, Drawable> getSecMediaIconsNormal() {
		return secMediaIconsNormal;
	}

	public static HashMap<String, Drawable> getSecMediaIconsOver() {
		return secMediaIconsOver;
	}

	/**
	 * Update for section Video's menu list.
	 * 
	 * @param videoMenuList
	 */
	public static void updateVideoMenuList(TNTemplate videoMenuList) {
		if (videoMenuList == null || videoMenuList.getSectionCount() < 1) {
			return;
		}
		Context context = mUtils.getApplicationContext();
		// Resources res = mUtils.getResource();
		UtilsPreference pref = mUtils.getUtilPreference();
		SectionTemplate[] videoSections = videoMenuList.getSections();
		if (mSecMediaIds != null) {
			mSecMediaIds = null;
		}
		mSecMediaIds = new String[videoSections.length];
		int sectioncount = 0;
		long bg[] = new long[2];
		final StringBuilder id = new StringBuilder();
		UrlImageViewCallback callback = new UrlImageViewCallback() {
			@Override
			public void onLoaded(ImageView imageView, Drawable loadedDrawable,
					String url, boolean loadedFromCache, String secId) {
				if (loadedDrawable == null) {
					GKIMLog.lf(null, 5, TAG + "=>false (" + secId + "): " + url);
					return;
				}
				if (url.contains("_over")) {
					secMediaIconsOver.put(secId, loadedDrawable);
				} else {
					secMediaIconsNormal.put(secId, loadedDrawable);
				}
				// NOTE: imageView is alway null since we just load drawable
				if (!secMediaIcons.containsKey(secId)) {
					// put first icon
					secMediaIcons.put(secId, loadedDrawable);
				} else {
					// generate the statelistDrawable and replace the first one.
					StateListDrawable stateDrw = new StateListDrawable();
					Drawable oldState = secMediaIcons.remove(secId);
					if (url.contains("_over")) {
						stateDrw.addState(KEY_STATE_PRESSED, loadedDrawable);
						stateDrw.addState(KEY_STATE_NORMAL, oldState);
					} else {
						stateDrw.addState(KEY_STATE_PRESSED, oldState);
						stateDrw.addState(KEY_STATE_NORMAL, loadedDrawable);
					}
					secMediaIcons.put(secId, stateDrw);
					// GKIMLog.lf(null, 0, TAG + "=>section menu icon added: "
					// + secId + "(" + sectionIcons.size() + ").");

				}
			}
		};

		for (SectionTemplate as : videoSections) {
			id.setLength(0);
			id.append(as.getSectionId());
			mSecMediaIds[sectioncount++] = id.toString();
			String key = compileSectionKeyToSP(id.toString());
			String value = compileSectionValueToSP(as);
			// XXX: if the key has existed, then update it (again). Generate
			// GradientDrawable as a time.
			pref.setStringPref(key, value);
			UIUtils.loadToCache(context, as.getSectionIconLink(), callback,
					id.toString());
			UIUtils.loadToCache(context, as.getSectionIconHoverLink(),
					callback, id.toString());
			bg = as.getSectionColors1();
			Drawable sectionDbl = UIUtils.buildGadientDrawableFromColor(
					(int) bg[0], (int) bg[1]);
			secMediaDrawables.put(id.toString(), sectionDbl);
		}
		GKIMLog.lf(null, 0, TAG + "=>updateVideoMediaList done with: "
				+ videoSections.length + ", " + secMediaDrawables.size()
				+ ", have " + sectioncount + " ids.");
	}

	/**
	 * @return
	 */
	public static int getCellWidthVideoHome() {
		if (mUtils == null) {
			return 0;
		}
		int result = mUtils.getIntPref(PREF_KEYNAME_VIDEOHOME_CELLWIDTH);
		int gap = getGapWidth();
		int icol = mContext.getResources().getInteger(
				R.integer.section_video_home_max_cols);
		if (icol == 0) {
			icol = 2;
		}
		int sW = mContext.getResources().getDisplayMetrics().widthPixels;
		result = (int) ((sW - ((icol + 1) * gap)) / icol);
		mUtils.setIntPref(PREF_KEYNAME_VIDEOHOME_CELLWIDTH, result);
		return result;
	}
}
