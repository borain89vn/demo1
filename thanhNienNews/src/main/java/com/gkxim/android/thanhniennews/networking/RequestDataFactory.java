/**
 * File: RequestDataFactory.java
 * Creator: Timon.Trinh (timon@gkxim.com)
 * Date: 04-12-2012
 * 
 */
package com.gkxim.android.thanhniennews.networking;

import com.gkxim.android.thanhniennews.models.UserAccount;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.GKIMLog;
import com.gkxim.android.utils.RequestData;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author Timon Trinh
 */
public final class RequestDataFactory {

	// Host constants
	protected static final String DEFAULT_DOMAIN_HOST = "http://tnmcms.dev2.gkxim.com/services";
	protected static final String DEFAULT_DOMAIN_SECONDHOST = "http://tnmcms1.dev2.gkxim.com/services";
	protected static final String DEFAULT_DOMAIN_RELEASEHOST = "http://tnmcms.gkxim.com/services"; // LIVE
	// protected static final String DEFAULT_DOMAIN_RELEASEHOST =
	// "http://tnmcms.dev2.gkxim.com/services"; //DEV
	protected static final String DEFAULT_URL_PARAM_CONNECT_CHAR = "&";

	// API constant names
	protected static final String DEFAULT_DOMAIN_SDATA_API_SECTIONLIST = "/sections_list";
	protected static final String DEFAULT_DOMAIN_SDATA_API_STORY_COMMENT_ICON = "/comment_icons_list";
	protected static final String DEFAULT_DOMAIN_SDATA_API_ISSUE = "/issue";
	protected static final String DEFAULT_DOMAIN_SDATA_API_SECTION = "/section";
	protected static final String DEFAULT_DOMAIN_SDATA_API_GETPOST_BYCITY = "/get_post_by_city";
	protected static final String DEFAULT_DOMAIN_SDATA_API_STORY_DETAIL = "/story";
	protected static final String DEFAULT_DOMAIN_SDATA_API_STORY_DETAIL_AI = "/ai_story";
	protected static final String DEFAULT_DOMAIN_SDATA_API_STORY_BOOKMARK = "/bookmark_story";
	protected static final String DEFAULT_DOMAIN_SDATA_API_STORY_COMMENTS = "/comments";
	protected static final String DEFAULT_DOMAIN_SDATA_API_STORY_POST_COMMENT = "/post_comment";
	protected static final String DEFAULT_DOMAIN_SDATA_API_STORY_POST_STORY = "/post_story";
	protected static final String DEFAULT_DOMAIN_SDATA_API_STORY_POST_STORY_ATTACHT = "/post_attachment";
	protected static final String DEFAULT_DOMAIN_SDATA_API_STORY_USER_STORY = "/user_story";
	protected static final String DEFAULT_DOMAIN_SDATA_API_STORY_SEARCH = "/search";
	protected static final String DEFAULT_DOMAIN_SDATA_API_STATIC_CONTENT = "/static_content";
	protected static final String DEFAULT_DOMAIN_SUSER_API_ACCOUNT_REGISTER = "/register";
	protected static final String DEFAULT_DOMAIN_SUSER_API_ACCOUNT_LOGIN = "/login";
	protected static final String DEFAULT_DOMAIN_SUSER_API_ACCOUNT_FORGOTPASSWORD = "/forgotpassword";
	protected static final String DEFAULT_DOMAIN_SUSER_API_ACCOUNT_UPDATE = "/update";
	protected static final String DEFAULT_DOMAIN_SUSER_API_USER_FEEDBACK = "/post_feedback";
	protected static final String DEFAULT_DOMAIN_SPNS_API_REGISTER = "/register";
	protected static final String DEFAULT_DOMAIN_SPNS_API_REGISTER_SECTIONS = "/register_sections";

	// API constant name for Video section
	protected static final String DEFAULT_DOMAIN_SDATA_API_SECTIONMEDIALIST = "/sections_media_list";
	protected static final String DEFAULT_DOMAIN_SDATA_API_VIDEO_HOME = "/sections_media_home";

	public static final int DATA_JSON_DEF_REQUESTTYPE_DATA_BOXES = 0; // 0x00
	public static final int DATA_JSON_DEF_REQUESTTYPE_DATA_BOXES_SECTION = 1; // 0x01
	public static final int DATA_JSON_DEF_REQUESTTYPE_DATA_VIDEO_HOME = 2; // 0x01
	public static final int DATA_JSON_DEF_REQUESTTYPE_TEMPLATES = 16; // 0xF0
	public static final int DATA_JSON_DEF_REQUESTTYPE_TEMPLATES_ICONS = 17; // 0xF1
	public static final int DATA_JSON_DEF_REQUESTTYPE_MEDIALIST = 18; // 0xF2
	public static final int DATA_JSON_DEF_REQUESTTYPE_DATA_STORY = 3840; // 0xF00
	public static final int DATA_JSON_DEF_REQUESTTYPE_DATA_ACCOUNT = 4080; // 0xFF0
	public static final int DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_DETAIL = 3841;
	public static final int DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_COMMENTS = 3842;
	public static final int DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_BOOKMARK = 3843;
	public static final int DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_POST_COMMENT = 3844;
	public static final int DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_POST_STORY = 3845;
	public static final int DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_USER_STORIES = 3846;
	public static final int DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_POST_STORY_ATTACHT = 3847;
	public static final int DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_SEARCH = 3848;
	public static final int DATA_JSON_DEF_REQUESTTYPE_DATA_STATIC_CONTENT = 3849;

	private static final int DATA_JSON_DEF_REQUESTTYPE_USER_ACCOUNT_REGISTER = 4081;
	private static final int DATA_JSON_DEF_REQUESTTYPE_USER_ACCOUNT_LOGIN = 4082;
	private static final int DATA_JSON_DEF_REQUESTTYPE_USER_ACCOUNT_FORGOTPASSWORD = 4087;
	public static final int DATA_JSON_DEF_REQUESTTYPE_USER_ACCOUNT_UPDATE = 4083;
	private static final int DATA_JSON_DEF_REQUESTTYPE_USER_FEEDBACK = 4084;
	public static final int DATA_JSON_DEF_REQUESTTYPE_PNS_REGISTER = 4085;
	public static final int DATA_JSON_DEF_REQUESTTYPE_PNS_REGISTER_SECTION = 4086;

	// param constant string
	private static final String DEFAULT_REQUEST_PARAM_API_ID = "api_id=";
	private static final String DEFAULT_REQUEST_PARAM_DEVICE = "device=";
	private static final String DEFAULT_REQUEST_PARAM_UID = "uid=";
	private static final String DEFAULT_REQUEST_PARAM_ISSUE_ID = "issue_id=";
	private static final String DEFAULT_REQUEST_PARAM_SECTION_ID = "section_id=";
	private static final String DEFAULT_REQUEST_PARAM_DEVICE_WIDTH = "width=";
	private static final String DEFAULT_REQUEST_PARAM_DIRECTION = "direction=";
	private static final String DEFAULT_REQUEST_PARAM_STORY_ID = "story_id=";
	private static final String DEFAULT_REQUEST_PARAM_LAT = "lat=";
	private static final String DEFAULT_REQUEST_PARAM_LNG = "lon=";
	private static final String DEFAULT_REQUEST_PARAM_STORY_POST_TITLE = "title=";
	private static final String DEFAULT_REQUEST_PARAM_STORY_POST_CONTENT = "content=";
	private static final String DEFAULT_REQUEST_PARAM_STORY_POST_SECTIONID = DEFAULT_REQUEST_PARAM_SECTION_ID;
	private static final String DEFAULT_REQUEST_PARAM_STORY_POST_COMMENT = "comment=";
	private static final String DEFAULT_REQUEST_PARAM_STORY_POST_RATING = "rating=";
	private static final String DEFAULT_REQUEST_PARAM_ACCOUNT_REGISTER_EMAIL = "email=";
	private static final String DEFAULT_REQUEST_PARAM_ACCOUNT_REGISTER_PASSWORD = "pwd=";
	private static final String DEFAULT_REQUEST_PARAM_ACCOUNT_UPDATE_PASSWORD_OLD = "pwdold=";
	private static final String DEFAULT_REQUEST_PARAM_ACCOUNT_UPDATE_PASSWORD_NEW = "pwdnew=";
	private static final String DEFAULT_REQUEST_PARAM_ACCOUNT_REGISTER_FNAME = "fname=";
	private static final String DEFAULT_REQUEST_PARAM_ACCOUNT_REGISTER_LNAME = "lname=";
	private static final String DEFAULT_REQUEST_PARAM_SEARCH_KEYWORD = "keyword=";
	private static final String DEFAULT_REQUEST_PARAM_USER_FEEDBACK_EMAIL = "email=";
	private static final String DEFAULT_REQUEST_PARAM_USER_FEEDBACK_CONTENT = "content=";
	private static final String DEFAULT_REQUEST_PARAM_USER_FEEDBACK_RATING = "rate=";
	private static final String DEFAULT_REQUEST_PARAM_PNS_REGISTER_XID = "pnsid=";
	private static final String DEFAULT_REQUEST_PARAM_PNS_REGISTER_ACTIVE = "active=";
	private static final String DEFAULT_REQUEST_PARAM_PNS_REGISTER_SECTIONS = "sections=";
	private static final String DEFAULT_REQUEST_PARAM_STATIC_CONTENT_NAME = "name=";

	// Define for Facebook account by using login app
	private static final String DEFAULT_REQUEST_PARAM_FB_ID = "fb_id=";
	private static final String DEFAULT_REQUEST_PARAM_FB_INFO = "fb_info=";
	// Define for some common usage
	public static final String DEFAULT_COMMON_DEVICE_PHONE = "phone";
	public static final String DEFAULT_COMMON_DEVICE_TABLET = "tablet";
	protected static final String DEFAULT_COMMON_PARAM_API_ID = DEFAULT_REQUEST_PARAM_API_ID
			+ "123456";
	protected static final String DEFAULT_COMMON_PARAM_VERSION = "version=3";
	private static final String DEFAULT_DATA_STATIC_CONTENT_SUPPORT = "ho-tro";
	private static final String DEFAULT_DATA_STATIC_CONTENT_POLICY = "dieu-khoan-bao-mat";

	// Define for testing
	private static final boolean DEBUG = GKIMLog.LOCAL_TEST_ON;

	public static final String DEFAULT_TESTING_PARAM_DEVICE_PHONE = DEFAULT_REQUEST_PARAM_DEVICE
			+ DEFAULT_COMMON_DEVICE_PHONE;
	public static final String DEFAULT_TESTING_PARAM_DEVICE_TABLET = DEFAULT_REQUEST_PARAM_DEVICE
			+ DEFAULT_COMMON_DEVICE_TABLET;
	private static final String DEFAULT_TESTING_PARAM_UID = DEFAULT_REQUEST_PARAM_UID
			+ "1";
	private static final String DEFAULT_TESTING_PARAM_ISSUE_ID = DEFAULT_REQUEST_PARAM_ISSUE_ID
			+ "8";
	private static final String DEFAULT_TESTING_PARAM_SECTION_ID = DEFAULT_REQUEST_PARAM_SECTION_ID
			+ "3";
	private static final String DEFAULT_TESTING_PARAM_DEVICE_WIDTH = DEFAULT_REQUEST_PARAM_DEVICE_WIDTH
			+ "480";
	private static final String DEFAULT_TESTING_PARAM_DIRECTION = DEFAULT_REQUEST_PARAM_DIRECTION
			+ "480";
	private static final String DEFAULT_TESTING_PARAM_STORY_ID = DEFAULT_REQUEST_PARAM_STORY_ID
			+ "508";
	private static final String DEFAULT_TESTING_PARAM_STORY_TITLE = DEFAULT_REQUEST_PARAM_STORY_POST_TITLE
			+ "Test";
	private static final String DEFAULT_TESTING_PARAM_STORY_CONTENT = DEFAULT_REQUEST_PARAM_STORY_POST_COMMENT
			+ "Test content";
	private static final String DEFAULT_TESTING_PARAM_STORY_RATING = DEFAULT_REQUEST_PARAM_STORY_POST_RATING
			+ "681";
	private static final String DEFAULT_TESTING_PARAM_SEARCH_CONTENT = DEFAULT_REQUEST_PARAM_SEARCH_KEYWORD
			+ "vietnam";

	protected static String mDevice;
	protected static String mUsingDomain = DEFAULT_DOMAIN_RELEASEHOST;
	protected static String DEFAULT_DOMAIN_SERVICE_DATA = mUsingDomain
			+ "/data";
	protected static String DEFAULT_DOMAIN_SERVICE_USER = mUsingDomain
			+ "/user";
	protected static String DEFAULT_DOMAIN_SERVICE_PNS = mUsingDomain + "/pns";
	protected static String DEFAULT_DOMAIN_SERVICE_SPRING = mUsingDomain;

	// Cuong
	protected static final String DOMAIN_HOST_COUNT_FB = "http://api.facebook.com";
	protected static final String DEFAULT_DOMAIN_SDATA_METHOD = "/method/fql.query";
	public static final int DATA_JSON_DEF_REQUESTTYPE_DATA_COUNT_FB = 5000;

	// end cuong

	public static final String DEFAULT_DOMAIN_DEVHOST_VIDEO = "http://tnmcms.dev2.gkxim.com/api_upload/index.php?action=upload_media_mobile";
	public static final String DEFAULT_DOMAIN_RELEASEHOST_VIDEO = "http://upload.thanhnien.com.vn/api_upload/index.php?action=upload_media_mobile";
	public static String DEFAULT_DOMAIN_HOST_VIDEO = DEFAULT_DOMAIN_DEVHOST_VIDEO;

	public static final String DEFAULT_DOMAIN_HOST_DEVVIDEO_VIDEO_OF_YOU = "http://tnmcms.dev2.gkxim.com/services/data/user_video";
	public static final String DEFAULT_DOMAIN_RELEASEHOST_VIDEO_OF_YOU = "http://upload.thanhnien.com.vn/services/data/user_video";
	public static String DEFAULT_DOMAIN_HOST_VIDEO_OF_YOU = DEFAULT_DOMAIN_HOST_DEVVIDEO_VIDEO_OF_YOU;

	public static final String STORY_ID_CACHER = DEFAULT_DOMAIN_SDATA_API_STORY_DETAIL
			+ DEFAULT_REQUEST_PARAM_STORY_ID;

	private RequestDataFactory() {
	}

	public static void initDeviceType() {
		mDevice = TNPreferenceManager.getDeviceType();
	}

	/**
	 * @author: Timon Trinh
	 * @Description: Change the domain that would be used for switching server's
	 *               location for development
	 * @param i
	 *            , 666 is suppose to be LIVE server, otherwise will be DEV
	 *            servers.
	 */
	public static void changeDomain(int i) {
		String newDomain = DEFAULT_DOMAIN_RELEASEHOST;
		if (i == 666) {
			newDomain = DEFAULT_DOMAIN_RELEASEHOST;
			RequestDataFactory.DEFAULT_DOMAIN_HOST_VIDEO = RequestDataFactory.DEFAULT_DOMAIN_RELEASEHOST_VIDEO;
			RequestDataFactory.DEFAULT_DOMAIN_HOST_VIDEO_OF_YOU = RequestDataFactory.DEFAULT_DOMAIN_RELEASEHOST_VIDEO_OF_YOU;
			TNPreferenceManager.EXTRAVALUE_SECTION_MEDIA = TNPreferenceManager.EXTRAVALUE_SECTION_MEDIA_LIVE;
			TNPreferenceManager.VIDEO_OF_YOU_ID = TNPreferenceManager.VIDEO_OF_YOU_ID_LIVE;
		} else if (i >= 3 && i < 666) {
			newDomain = DEFAULT_DOMAIN_HOST;
			RequestDataFactory.DEFAULT_DOMAIN_HOST_VIDEO = RequestDataFactory.DEFAULT_DOMAIN_DEVHOST_VIDEO;
			RequestDataFactory.DEFAULT_DOMAIN_HOST_VIDEO_OF_YOU = RequestDataFactory.DEFAULT_DOMAIN_HOST_DEVVIDEO_VIDEO_OF_YOU;
			TNPreferenceManager.EXTRAVALUE_SECTION_MEDIA = TNPreferenceManager.EXTRAVALUE_SECTION_MEDIA_DEV;
			TNPreferenceManager.VIDEO_OF_YOU_ID = TNPreferenceManager.VIDEO_OF_YOU_ID_DEV;
		}

		// TODO: Change to server dev2 from TNPreference.setContext()
		// and call changeDomain(3). DO NOT change here

		GKIMLog.lf(null, 5, "Domain has been changed from: " + mUsingDomain
				+ " to: " + newDomain);
		if (!mUsingDomain.equalsIgnoreCase(newDomain)) {
			mUsingDomain = newDomain;
			DEFAULT_DOMAIN_SERVICE_DATA = mUsingDomain + "/data";
			DEFAULT_DOMAIN_SERVICE_USER = mUsingDomain + "/user";
			DEFAULT_DOMAIN_SERVICE_PNS = mUsingDomain + "/pns";
			DEFAULT_DOMAIN_SERVICE_SPRING = mUsingDomain;
		}
	}

	/**
	 * @Description: Generate request for /data/sections_list api.
	 *               <p>
	 *               The definition for /data/section_list API is:
	 *               </p>
	 *               <p>
	 *               Input: <b>API_ID: String</b> is required.
	 *               </p>
	 *               <p>
	 *               URL in get: [HOST]/data/sections_list?api_id=#api_id
	 *               </p>
	 * @return RequestData object for requesting the sections definition
	 */
	public static RequestData makeSectionListRequest() {
		return new RequestData(DEFAULT_DOMAIN_SERVICE_DATA,
				DEFAULT_DOMAIN_SDATA_API_SECTIONLIST,
				DEFAULT_COMMON_PARAM_API_ID + DEFAULT_URL_PARAM_CONNECT_CHAR
						+ DEFAULT_COMMON_PARAM_VERSION,
				DATA_JSON_DEF_REQUESTTYPE_TEMPLATES,
				DEFAULT_DOMAIN_SDATA_API_SECTIONLIST);
	}

	/**
	 * @Description: Generate request for /data/sections_media_list api.
	 * @return RequestData object of requesting the video section's menu.
	 */
	public static RequestData makeSectionMediaListRequest() {
		return new RequestData(DEFAULT_DOMAIN_SERVICE_DATA,
				DEFAULT_DOMAIN_SDATA_API_SECTIONMEDIALIST,
				DEFAULT_COMMON_PARAM_API_ID,
				DATA_JSON_DEF_REQUESTTYPE_MEDIALIST,
				DEFAULT_DOMAIN_SDATA_API_SECTIONMEDIALIST);
	}

	public static RequestData makeStoryCommentIconsRequest() {
		return new RequestData(DEFAULT_DOMAIN_SERVICE_DATA,
				DEFAULT_DOMAIN_SDATA_API_STORY_COMMENT_ICON,
				DEFAULT_COMMON_PARAM_API_ID,
				DATA_JSON_DEF_REQUESTTYPE_TEMPLATES_ICONS,
				DEFAULT_DOMAIN_SDATA_API_STORY_COMMENT_ICON);
	}

	/**
	 * @Description: Generate request for /data/issue api
	 * @param uid
	 * @param devicetype
	 * @param deviceWidth
	 * @return
	 */
	public static RequestData makeIssueRequest(String uid, String devicetype,
			String deviceWidth, String direction, String issueId) {
		if (DEBUG) {
			String params = DEFAULT_COMMON_PARAM_API_ID
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_UID
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_DEVICE_PHONE
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_DEVICE_WIDTH
					+ DEFAULT_TESTING_PARAM_SECTION_ID
					+ DEFAULT_TESTING_PARAM_DIRECTION;

			return new RequestData(DEFAULT_DOMAIN_SERVICE_DATA,
					DEFAULT_DOMAIN_SDATA_API_ISSUE, params,
					DATA_JSON_DEF_REQUESTTYPE_DATA_BOXES,
					DEFAULT_DOMAIN_SDATA_API_ISSUE);
		} else {
			if (devicetype == null || devicetype.length() == 0) {
				devicetype = mDevice;
			}
			if (uid == null || uid.length() == 0) {
				uid = "1";
			}
			String keyCacher = DEFAULT_DOMAIN_SDATA_API_ISSUE;
			String params = DEFAULT_COMMON_PARAM_API_ID
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_UID + uid
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_DEVICE + devicetype
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_DEVICE_WIDTH + deviceWidth;

			if (direction != null) {
				params += DEFAULT_URL_PARAM_CONNECT_CHAR
						+ DEFAULT_REQUEST_PARAM_DIRECTION + direction;
			}
			if (issueId != null && issueId != "") {
				if (issueId
						.equalsIgnoreCase(TNPreferenceManager.EXTRAVALUE_SECTION_HOME)) {

				} else {
					params += DEFAULT_URL_PARAM_CONNECT_CHAR
							+ DEFAULT_REQUEST_PARAM_ISSUE_ID + issueId;
				}
				keyCacher += DEFAULT_REQUEST_PARAM_ISSUE_ID + issueId;
			}
			return new RequestData(DEFAULT_DOMAIN_SERVICE_DATA,
					DEFAULT_DOMAIN_SDATA_API_ISSUE, params,
					DATA_JSON_DEF_REQUESTTYPE_DATA_BOXES, keyCacher);
		}
	}

	/**
	 * @Description: Generate request for /data/issue api
	 * @param uid
	 * @param devicetype
	 * @param deviceWidth
	 * @return
	 */
	public static RequestData makeIssueHomeGPSWithLocationRequest(String uid,
			String devicetype, String deviceWidth, String direction,
			String issueId, double lat, double lng) {
		if (DEBUG) {
			String params = DEFAULT_COMMON_PARAM_API_ID
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_UID
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_DEVICE_PHONE
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_DEVICE_WIDTH
					+ DEFAULT_TESTING_PARAM_SECTION_ID
					+ DEFAULT_TESTING_PARAM_DIRECTION;

			return new RequestData(DEFAULT_DOMAIN_SERVICE_DATA,
					DEFAULT_DOMAIN_SDATA_API_ISSUE, params,
					DATA_JSON_DEF_REQUESTTYPE_DATA_BOXES,
					DEFAULT_DOMAIN_SDATA_API_ISSUE);
		} else {
			if (devicetype == null || devicetype.length() == 0) {
				devicetype = mDevice;
			}
			if (uid == null || uid.length() == 0) {
				uid = "1";
			}

			String keyCacher = DEFAULT_DOMAIN_SDATA_API_ISSUE;
			String params = DEFAULT_COMMON_PARAM_API_ID
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_UID + uid
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_DEVICE + devicetype
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_DEVICE_WIDTH + deviceWidth;

			if (lat != -1 && lng != -1) {
				params += DEFAULT_URL_PARAM_CONNECT_CHAR
						+ DEFAULT_REQUEST_PARAM_LAT + lat;

				params += DEFAULT_URL_PARAM_CONNECT_CHAR
						+ DEFAULT_REQUEST_PARAM_LNG + lng;
			} else {

				// 21.026629, 105.835744
				// singapore: 1.3759509,103.8042359
				// hanoi; 21.0333333, 105.850000

				params += DEFAULT_URL_PARAM_CONNECT_CHAR
						+ DEFAULT_REQUEST_PARAM_LAT + "21.0333333";

				params += DEFAULT_URL_PARAM_CONNECT_CHAR
						+ DEFAULT_REQUEST_PARAM_LNG + "105.850000";
			}
			if (direction != null) {
				params += DEFAULT_URL_PARAM_CONNECT_CHAR
						+ DEFAULT_REQUEST_PARAM_DIRECTION + direction;
			}
			if (issueId != null && issueId != "") {
				// params += DEFAULT_URL_PARAM_CONNECT_CHAR
				// + DEFAULT_REQUEST_PARAM_ISSUE_ID + issueId;
				keyCacher += DEFAULT_REQUEST_PARAM_ISSUE_ID + issueId;
			}
			return new RequestData(DEFAULT_DOMAIN_SERVICE_DATA,
					DEFAULT_DOMAIN_SDATA_API_ISSUE, params,
					DATA_JSON_DEF_REQUESTTYPE_DATA_BOXES, null);
		}
	}

	public static RequestData makeVideoHomeSectionRequest(String uid,
			String devicetype, String deviceWidth) {
		if (devicetype == null || devicetype.length() == 0) {
			devicetype = mDevice;
		}
		if (uid == null || uid.length() == 0) {
			uid = "1";
		}
		String keyCacher = DEFAULT_DOMAIN_SDATA_API_VIDEO_HOME;
		String params = DEFAULT_COMMON_PARAM_API_ID
				+ DEFAULT_URL_PARAM_CONNECT_CHAR + DEFAULT_REQUEST_PARAM_UID
				+ uid;

		if (devicetype != null && deviceWidth != null) {
			params += DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_DEVICE + devicetype;
			params += DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_DEVICE_WIDTH + deviceWidth;
		}
		return new RequestData(DEFAULT_DOMAIN_SERVICE_DATA,
				DEFAULT_DOMAIN_SDATA_API_VIDEO_HOME, params,
				DATA_JSON_DEF_REQUESTTYPE_DATA_VIDEO_HOME, keyCacher);
	}

	public static RequestData makeSectionRequest(String uid, String devicetype,
			String deviceWidth, String issueId, String sectionId,
			String direction) {
		if (DEBUG) {
			String params = DEFAULT_COMMON_PARAM_API_ID
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_UID
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_DEVICE_PHONE
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_DEVICE_WIDTH
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_ISSUE_ID
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_SECTION_ID
					+ DEFAULT_TESTING_PARAM_DIRECTION;
			return new RequestData(DEFAULT_DOMAIN_SERVICE_DATA,
					DEFAULT_DOMAIN_SDATA_API_SECTION, params,
					DATA_JSON_DEF_REQUESTTYPE_DATA_BOXES_SECTION,
					DEFAULT_DOMAIN_SDATA_API_SECTION);
		} else {
			if (devicetype == null || devicetype.length() == 0) {
				devicetype = mDevice;
			}
			if (uid == null || uid.length() == 0) {
				uid = "1";
			}

			String params = DEFAULT_COMMON_PARAM_API_ID
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_UID + uid
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_DEVICE + devicetype
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_DEVICE_WIDTH + deviceWidth
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_SECTION_ID + sectionId;
			String keyCacher = DEFAULT_DOMAIN_SDATA_API_SECTION
					+ DEFAULT_REQUEST_PARAM_SECTION_ID + sectionId;
			if (direction != null) {
				params += DEFAULT_URL_PARAM_CONNECT_CHAR
						+ DEFAULT_REQUEST_PARAM_DIRECTION + direction;
			}
			if (issueId != null && issueId != "") {
				params += DEFAULT_URL_PARAM_CONNECT_CHAR
						+ DEFAULT_REQUEST_PARAM_ISSUE_ID + issueId;
				keyCacher += (DEFAULT_REQUEST_PARAM_ISSUE_ID + issueId);
			}
			return new RequestData(DEFAULT_DOMAIN_SERVICE_DATA,
					DEFAULT_DOMAIN_SDATA_API_SECTION, params,
					DATA_JSON_DEF_REQUESTTYPE_DATA_BOXES_SECTION, keyCacher);
		}
	}

	public static RequestData makeSectionLocationGPSRequest(String uid,
			String devicetype, String deviceWidth, String story_id,
			String direction, String issueId, double lat, double lng) {
		if (DEBUG) {
			String params = DEFAULT_COMMON_PARAM_API_ID
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_UID
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_DEVICE_PHONE
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_DEVICE_WIDTH
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_ISSUE_ID
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_SECTION_ID
					+ DEFAULT_TESTING_PARAM_DIRECTION;
			return new RequestData(DEFAULT_DOMAIN_SERVICE_DATA,
					DEFAULT_DOMAIN_SDATA_API_SECTION, params,
					DATA_JSON_DEF_REQUESTTYPE_DATA_BOXES_SECTION,
					DEFAULT_DOMAIN_SDATA_API_SECTION);
		} else {
			if (devicetype == null || devicetype.length() == 0) {
				devicetype = mDevice;
			}
			if (uid == null || uid.length() == 0) {
				uid = "1";
			}

			String params = DEFAULT_COMMON_PARAM_API_ID
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_UID + uid
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_DEVICE + devicetype
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_DEVICE_WIDTH + deviceWidth;

			if (direction != null) {
				params += DEFAULT_URL_PARAM_CONNECT_CHAR
						+ DEFAULT_REQUEST_PARAM_DIRECTION + direction;
			}

			if (issueId != null && issueId != "") {
				params += DEFAULT_URL_PARAM_CONNECT_CHAR
						+ DEFAULT_REQUEST_PARAM_ISSUE_ID + issueId;
			}
			if (story_id != null && story_id != "") {
				params += DEFAULT_URL_PARAM_CONNECT_CHAR
						+ DEFAULT_REQUEST_PARAM_STORY_ID + story_id;
				params += DEFAULT_URL_PARAM_CONNECT_CHAR
						+ DEFAULT_REQUEST_PARAM_SECTION_ID + story_id;
			}

			// params += DEFAULT_URL_PARAM_CONNECT_CHAR
			// + DEFAULT_REQUEST_PARAM_LAT + lat;
			//
			// params += DEFAULT_URL_PARAM_CONNECT_CHAR
			// + DEFAULT_REQUEST_PARAM_LNG + lng;

			return new RequestData(DEFAULT_DOMAIN_SERVICE_DATA,
					DEFAULT_DOMAIN_SDATA_API_GETPOST_BYCITY, params,
					DATA_JSON_DEF_REQUESTTYPE_DATA_BOXES_SECTION, null);
		}
	}

	// http://tnmcms.dev2.gkxim.com/services/data/user_video?api_id=123456&device=tablet&uid=3820

	public static RequestData makeVideoOfYouRequest(String uid,
			String devicetype, String deviceWidth, String issueId,
			String sectionId, String pageIndex) {
		// if (DEBUG) {
		// String params = DEFAULT_COMMON_PARAM_API_ID
		// + DEFAULT_URL_PARAM_CONNECT_CHAR
		// + DEFAULT_TESTING_PARAM_UID
		// + DEFAULT_URL_PARAM_CONNECT_CHAR
		// + DEFAULT_TESTING_PARAM_DEVICE_PHONE
		// + DEFAULT_URL_PARAM_CONNECT_CHAR
		// + DEFAULT_TESTING_PARAM_DEVICE_WIDTH
		// + DEFAULT_URL_PARAM_CONNECT_CHAR
		// + DEFAULT_TESTING_PARAM_ISSUE_ID
		// + DEFAULT_URL_PARAM_CONNECT_CHAR
		// + DEFAULT_TESTING_PARAM_SECTION_ID
		// + DEFAULT_TESTING_PARAM_DIRECTION;
		// return new RequestData(DEFAULT_DOMAIN_SERVICE_DATA,
		// DEFAULT_DOMAIN_SDATA_API_SECTION, params,
		// DATA_JSON_DEF_REQUESTTYPE_DATA_BOXES_SECTION,
		// DEFAULT_DOMAIN_SDATA_API_SECTION);
		// } else {
		if (devicetype == null || devicetype.length() == 0) {
			devicetype = mDevice;
		}
		if (uid == null || uid.length() == 0) {
			uid = "1";
		}

		String params = DEFAULT_COMMON_PARAM_API_ID
				+ DEFAULT_URL_PARAM_CONNECT_CHAR + DEFAULT_REQUEST_PARAM_DEVICE
				+ devicetype + DEFAULT_URL_PARAM_CONNECT_CHAR
				+ DEFAULT_REQUEST_PARAM_UID + uid;
		// + DEFAULT_URL_PARAM_CONNECT_CHAR
		// + DEFAULT_REQUEST_PARAM_DEVICE_WIDTH + deviceWidth
		// + DEFAULT_URL_PARAM_CONNECT_CHAR
		// + DEFAULT_REQUEST_PARAM_SECTION_ID + sectionId;
		// String keyCacher = DEFAULT_DOMAIN_SDATA_API_SECTION
		// + DEFAULT_REQUEST_PARAM_SECTION_ID + sectionId;
		if (pageIndex != null) {
			params += DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_DIRECTION + pageIndex;
		}
		// if (issueId != null && issueId != "") {
		// params += DEFAULT_URL_PARAM_CONNECT_CHAR
		// + DEFAULT_REQUEST_PARAM_ISSUE_ID + issueId;
		// keyCacher += (DEFAULT_REQUEST_PARAM_ISSUE_ID + issueId);
		// }
		return new RequestData(DEFAULT_DOMAIN_HOST_VIDEO_OF_YOU, "", params,
				DATA_JSON_DEF_REQUESTTYPE_DATA_BOXES_SECTION, "");
		// }
	}

	public static RequestData makeStoryRequest(String uid, String storyid) {
		if (uid == null || uid.length() == 0) {
			uid = "1";
		}

		String params = DEFAULT_COMMON_PARAM_API_ID
				+ DEFAULT_URL_PARAM_CONNECT_CHAR + DEFAULT_REQUEST_PARAM_UID
				+ uid + DEFAULT_URL_PARAM_CONNECT_CHAR
				+ DEFAULT_REQUEST_PARAM_STORY_ID + storyid;
		String keyCacher = DEFAULT_DOMAIN_SDATA_API_STORY_DETAIL
				+ DEFAULT_REQUEST_PARAM_STORY_ID + storyid;
		return new RequestData(DEFAULT_DOMAIN_SERVICE_DATA,
				DEFAULT_DOMAIN_SDATA_API_STORY_DETAIL, params,
				DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_DETAIL, keyCacher);
	}

	public static RequestData makeStoryAppIndexRequest(String uid,
			String storyid) {
		if (uid == null || uid.length() == 0) {
			uid = "1";
		}

		String params = DEFAULT_COMMON_PARAM_API_ID
				+ DEFAULT_URL_PARAM_CONNECT_CHAR + DEFAULT_REQUEST_PARAM_UID
				+ uid + DEFAULT_URL_PARAM_CONNECT_CHAR
				+ DEFAULT_REQUEST_PARAM_STORY_ID + storyid;
		String keyCacher = DEFAULT_DOMAIN_SDATA_API_STORY_DETAIL
				+ DEFAULT_REQUEST_PARAM_STORY_ID + storyid;
		return new RequestData(DEFAULT_DOMAIN_SERVICE_DATA,
				DEFAULT_DOMAIN_SDATA_API_STORY_DETAIL_AI, params,
				DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_DETAIL, null);
	}

	public static RequestData makeStoryRequestType(String uid, String storyid,
			String type) {
		if (uid == null || uid.length() == 0) {
			uid = "1";
		}

		String params = DEFAULT_COMMON_PARAM_API_ID
				+ DEFAULT_URL_PARAM_CONNECT_CHAR + DEFAULT_REQUEST_PARAM_UID
				+ uid + DEFAULT_URL_PARAM_CONNECT_CHAR
				+ DEFAULT_REQUEST_PARAM_STORY_ID + storyid
				+ DEFAULT_URL_PARAM_CONNECT_CHAR + "type=" + type;
		String keyCacher = DEFAULT_DOMAIN_SDATA_API_STORY_DETAIL
				+ DEFAULT_REQUEST_PARAM_STORY_ID + storyid;
		return new RequestData(DEFAULT_DOMAIN_SERVICE_DATA,
				DEFAULT_DOMAIN_SDATA_API_STORY_DETAIL, params,
				DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_DETAIL, keyCacher);
	}

	public static RequestData makeStoryCommentsRequest(String uid,
			String storyid) {
		if (DEBUG) {
			String params = DEFAULT_COMMON_PARAM_API_ID
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_UID
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_STORY_ID;
			return new RequestData(DEFAULT_DOMAIN_SERVICE_DATA,
					DEFAULT_DOMAIN_SDATA_API_STORY_COMMENTS, params,
					DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_COMMENTS,
					DEFAULT_DOMAIN_SDATA_API_STORY_COMMENTS);
		} else {
			if (uid == null || uid.length() == 0) {
				uid = "1";
			}
			String params = DEFAULT_COMMON_PARAM_API_ID
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_UID + uid
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_STORY_ID + storyid;
			String keyCacher = DEFAULT_DOMAIN_SDATA_API_STORY_COMMENTS
					+ DEFAULT_REQUEST_PARAM_STORY_ID + storyid;
			return new RequestData(DEFAULT_DOMAIN_SERVICE_DATA,
					DEFAULT_DOMAIN_SDATA_API_STORY_COMMENTS, params,
					DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_COMMENTS, keyCacher);
		}
	}

	public static RequestData makeAccountRegisterRequest(String email,
			String password, String firstname, String lastname) {
		String params = DEFAULT_COMMON_PARAM_API_ID
				+ DEFAULT_URL_PARAM_CONNECT_CHAR
				+ DEFAULT_REQUEST_PARAM_ACCOUNT_REGISTER_EMAIL + email
				+ DEFAULT_URL_PARAM_CONNECT_CHAR
				+ DEFAULT_REQUEST_PARAM_ACCOUNT_REGISTER_PASSWORD + password
				+ DEFAULT_URL_PARAM_CONNECT_CHAR
				+ DEFAULT_REQUEST_PARAM_ACCOUNT_REGISTER_FNAME + firstname
				+ DEFAULT_URL_PARAM_CONNECT_CHAR
				+ DEFAULT_REQUEST_PARAM_ACCOUNT_REGISTER_LNAME + lastname;
		return new RequestData(DEFAULT_DOMAIN_SERVICE_USER,
				DEFAULT_DOMAIN_SUSER_API_ACCOUNT_REGISTER, params,
				DATA_JSON_DEF_REQUESTTYPE_USER_ACCOUNT_REGISTER,
				DEFAULT_DOMAIN_SUSER_API_ACCOUNT_REGISTER);
	}

	public static RequestData makeAccountLoginRequest(String email,
			String password) {
		String params = DEFAULT_COMMON_PARAM_API_ID
				+ DEFAULT_URL_PARAM_CONNECT_CHAR
				+ DEFAULT_REQUEST_PARAM_ACCOUNT_REGISTER_EMAIL + email
				+ DEFAULT_URL_PARAM_CONNECT_CHAR
				+ DEFAULT_REQUEST_PARAM_ACCOUNT_REGISTER_PASSWORD + password;
		return new RequestData(DEFAULT_DOMAIN_SERVICE_USER,
				DEFAULT_DOMAIN_SUSER_API_ACCOUNT_LOGIN, params,
				DATA_JSON_DEF_REQUESTTYPE_USER_ACCOUNT_LOGIN,
				DEFAULT_DOMAIN_SUSER_API_ACCOUNT_LOGIN);
	}

	public static RequestData makeAccountForgotPasswordRequest(String email) {
		String params = DEFAULT_COMMON_PARAM_API_ID
				+ DEFAULT_URL_PARAM_CONNECT_CHAR
				+ DEFAULT_REQUEST_PARAM_ACCOUNT_REGISTER_EMAIL + email;
		return new RequestData(DEFAULT_DOMAIN_SERVICE_USER,
				DEFAULT_DOMAIN_SUSER_API_ACCOUNT_FORGOTPASSWORD, params,
				DATA_JSON_DEF_REQUESTTYPE_USER_ACCOUNT_FORGOTPASSWORD,
				DEFAULT_DOMAIN_SUSER_API_ACCOUNT_FORGOTPASSWORD);
	}

	public static RequestData makeAccountUpdateRequest(String uid,
			String oldpassword, String firstname, String lastname,
			String newpass) {
		String params = DEFAULT_COMMON_PARAM_API_ID
				+ DEFAULT_URL_PARAM_CONNECT_CHAR + DEFAULT_REQUEST_PARAM_UID
				+ uid + DEFAULT_URL_PARAM_CONNECT_CHAR
				+ DEFAULT_REQUEST_PARAM_ACCOUNT_UPDATE_PASSWORD_OLD
				+ oldpassword + DEFAULT_URL_PARAM_CONNECT_CHAR
				+ DEFAULT_REQUEST_PARAM_ACCOUNT_REGISTER_FNAME + firstname
				+ DEFAULT_URL_PARAM_CONNECT_CHAR
				+ DEFAULT_REQUEST_PARAM_ACCOUNT_REGISTER_LNAME + lastname;
		if (newpass != null && newpass.length() > 0) {
			params += DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_ACCOUNT_UPDATE_PASSWORD_NEW
					+ newpass;
		}
		return new RequestData(DEFAULT_DOMAIN_SERVICE_USER,
				DEFAULT_DOMAIN_SUSER_API_ACCOUNT_UPDATE, params,
				DATA_JSON_DEF_REQUESTTYPE_USER_ACCOUNT_UPDATE,
				DEFAULT_DOMAIN_SUSER_API_ACCOUNT_UPDATE);
	}

	public static RequestData makeUserFeedbackRequest(String uid, String email,
			String content, String rating) {
		String params = DEFAULT_COMMON_PARAM_API_ID
				+ DEFAULT_URL_PARAM_CONNECT_CHAR
				// + DEFAULT_REQUEST_PARAM_UID + uid
				// + DEFAULT_URL_PARAM_CONNECT_CHAR
				+ DEFAULT_REQUEST_PARAM_USER_FEEDBACK_EMAIL + email
				+ DEFAULT_URL_PARAM_CONNECT_CHAR
				+ DEFAULT_REQUEST_PARAM_USER_FEEDBACK_CONTENT + content
				+ DEFAULT_URL_PARAM_CONNECT_CHAR
				+ DEFAULT_REQUEST_PARAM_USER_FEEDBACK_RATING
				+ String.valueOf(rating);
		return new RequestData(DEFAULT_DOMAIN_SERVICE_USER,
				DEFAULT_DOMAIN_SUSER_API_USER_FEEDBACK, params,
				DATA_JSON_DEF_REQUESTTYPE_USER_FEEDBACK,
				DEFAULT_DOMAIN_SUSER_API_USER_FEEDBACK);
	}

	public static RequestData makeBookmarkStoryRequest(String uid,
			String storyId) {
		if (DEBUG) {
			String params = DEFAULT_COMMON_PARAM_API_ID
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_UID
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_STORY_ID;
			return new RequestData(DEFAULT_DOMAIN_SERVICE_DATA,
					DEFAULT_DOMAIN_SDATA_API_STORY_BOOKMARK, params,
					DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_BOOKMARK, "");
		} else {
			if (uid == null || uid.length() == 0) {
				uid = "1";
			}
			// Nam.Nguyen

			String params = DEFAULT_COMMON_PARAM_API_ID
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_UID + uid
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_STORY_ID + storyId;
			return new RequestData(DEFAULT_DOMAIN_SERVICE_DATA,
					DEFAULT_DOMAIN_SDATA_API_STORY_BOOKMARK, params,
					DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_BOOKMARK, "");
		}
	}

	public static RequestData makeStoryPostCommentRequest(String uid,
			String storyId, String rating, String title, String content) {
		if (DEBUG) {
			String params = DEFAULT_COMMON_PARAM_API_ID
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_UID
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_STORY_ID
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_STORY_TITLE
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_STORY_CONTENT
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_STORY_RATING;
			return new RequestData(DEFAULT_DOMAIN_SERVICE_DATA,
					DEFAULT_DOMAIN_SDATA_API_STORY_POST_COMMENT, params,
					DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_POST_COMMENT,
					DEFAULT_DOMAIN_SDATA_API_STORY_POST_COMMENT);
		} else {
			if (uid == null || uid.length() == 0) {
				return null;
			}
			String params = DEFAULT_COMMON_PARAM_API_ID
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_UID + uid
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_STORY_ID + storyId
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_STORY_POST_TITLE + title
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_STORY_POST_COMMENT + content
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_STORY_POST_RATING + rating;
			return new RequestData(DEFAULT_DOMAIN_SERVICE_DATA,
					DEFAULT_DOMAIN_SDATA_API_STORY_POST_COMMENT, params,
					DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_POST_COMMENT,
					DEFAULT_DOMAIN_SDATA_API_STORY_POST_COMMENT);
		}
	}

	public static RequestData makeStoryPostStoryRequest(String uid,
			String title, String content) {
		return (makeStoryPostStoryRequest(uid, title, content, null));
	}

	public static RequestData makeStoryPostStoryRequest(String uid,
			String title, String content, String section_id) {
		if (DEBUG) {
			String params = DEFAULT_COMMON_PARAM_API_ID
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_UID
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_STORY_TITLE
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_STORY_CONTENT
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_SECTION_ID;
			return new RequestData(DEFAULT_DOMAIN_SERVICE_DATA,
					DEFAULT_DOMAIN_SDATA_API_STORY_POST_STORY, params,
					DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_POST_STORY, "");
		} else {
			if (uid == null || uid.length() == 0) {
				return null;
			}
			String params;
			try {
				params = DEFAULT_COMMON_PARAM_API_ID
						+ DEFAULT_URL_PARAM_CONNECT_CHAR
						+ DEFAULT_REQUEST_PARAM_UID + uid
						+ DEFAULT_URL_PARAM_CONNECT_CHAR
						+ DEFAULT_REQUEST_PARAM_STORY_POST_TITLE
						+ URLEncoder.encode(title, "utf-8")
						+ DEFAULT_URL_PARAM_CONNECT_CHAR
						+ DEFAULT_REQUEST_PARAM_STORY_POST_CONTENT
						+ URLEncoder.encode(content, "utf-8");
			} catch (UnsupportedEncodingException e) {
				GKIMLog.lf(null, 4,
						"RequestDataFactory=>makeStoryPostStoryRequest UnsupportedEncodingException: "
								+ e.getMessage());
				params = DEFAULT_COMMON_PARAM_API_ID
						+ DEFAULT_URL_PARAM_CONNECT_CHAR
						+ DEFAULT_REQUEST_PARAM_UID + uid
						+ DEFAULT_URL_PARAM_CONNECT_CHAR
						+ DEFAULT_REQUEST_PARAM_STORY_POST_TITLE + title
						+ DEFAULT_URL_PARAM_CONNECT_CHAR
						+ DEFAULT_REQUEST_PARAM_STORY_POST_CONTENT + content;
			}
			if (section_id != null && section_id.length() != 0) {
				params += DEFAULT_URL_PARAM_CONNECT_CHAR
						+ DEFAULT_REQUEST_PARAM_STORY_POST_SECTIONID
						+ section_id;
			}
			return new RequestData(DEFAULT_DOMAIN_SERVICE_DATA,
					DEFAULT_DOMAIN_SDATA_API_STORY_POST_STORY, params,
					DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_POST_STORY, "POST", "");
		}
	}

	public static RequestData makeStoryPostVideoRequest(String uid,
			String title, String content, String section_id) {
		// if (DEBUG) {
		// String params = DEFAULT_COMMON_PARAM_API_ID
		// + DEFAULT_URL_PARAM_CONNECT_CHAR
		// + DEFAULT_TESTING_PARAM_UID
		// + DEFAULT_URL_PARAM_CONNECT_CHAR
		// + DEFAULT_TESTING_PARAM_STORY_TITLE
		// + DEFAULT_URL_PARAM_CONNECT_CHAR
		// + DEFAULT_TESTING_PARAM_STORY_CONTENT
		// + DEFAULT_URL_PARAM_CONNECT_CHAR
		// + DEFAULT_TESTING_PARAM_SECTION_ID;
		// return new RequestData(DEFAULT_DOMAIN_HOST_VIDEO, "", params,
		// DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_POST_STORY, "");
		// } else {
		if (uid == null || uid.length() == 0) {
			return null;
		}
		String params;
		try {
			params = DEFAULT_COMMON_PARAM_API_ID
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_UID + uid
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_STORY_POST_TITLE
					+ URLEncoder.encode(title, "utf-8")
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_STORY_POST_CONTENT
					+ URLEncoder.encode(content, "utf-8");
		} catch (UnsupportedEncodingException e) {
			GKIMLog.lf(null, 4,
					"RequestDataFactory=>makeStoryPostStoryRequest UnsupportedEncodingException: "
							+ e.getMessage());
			params = DEFAULT_COMMON_PARAM_API_ID
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_UID + uid
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_STORY_POST_TITLE + title
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_STORY_POST_CONTENT + content;
		}
		if (section_id != null && section_id.length() != 0) {
			params += DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_STORY_POST_SECTIONID + section_id;
		}
		return new RequestData(DEFAULT_DOMAIN_RELEASEHOST_VIDEO, "", params,
				DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_POST_STORY, "POST", "");
		// }
	}

	public static RequestData makeStoryPostStoryAttachmentRequest(String uid,
			String storyid) {
		if (uid == null || uid.length() == 0 || storyid == null
				|| storyid.length() == 0) {
			return null;
		}
		String params = DEFAULT_COMMON_PARAM_API_ID
				+ DEFAULT_URL_PARAM_CONNECT_CHAR + DEFAULT_REQUEST_PARAM_UID
				+ uid + DEFAULT_URL_PARAM_CONNECT_CHAR
				+ DEFAULT_REQUEST_PARAM_STORY_ID + storyid;
		return new RequestData(DEFAULT_DOMAIN_SERVICE_DATA,
				DEFAULT_DOMAIN_SDATA_API_STORY_POST_STORY_ATTACHT, params,
				DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_POST_STORY_ATTACHT, "");
	}

	public static RequestData makeUserPostedStoriesRequest(String uid,
			String devicetype, String deviceWidth) {
		if (DEBUG) {
			String params = DEFAULT_COMMON_PARAM_API_ID
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_UID
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_STORY_ID
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_STORY_TITLE
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_STORY_CONTENT
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_STORY_RATING;
			return new RequestData(DEFAULT_DOMAIN_SERVICE_DATA,
					DEFAULT_DOMAIN_SDATA_API_STORY_USER_STORY, params,
					DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_USER_STORIES, "");
		} else {
			if (uid == null || uid.length() == 0) {
				return null;
			}
			if (devicetype == null || devicetype.length() == 0) {
				devicetype = mDevice;
			}
			String params = DEFAULT_COMMON_PARAM_API_ID
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_UID + uid
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_DEVICE + devicetype
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_DEVICE_WIDTH + deviceWidth;
			return new RequestData(DEFAULT_DOMAIN_SERVICE_DATA,
					DEFAULT_DOMAIN_SDATA_API_STORY_USER_STORY, params,
					DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_USER_STORIES, "");
		}
	}

	public static RequestData makeStaticContentRequest(int type) {
		String contentName = "";
		if (type == 0) {
			contentName = DEFAULT_DATA_STATIC_CONTENT_SUPPORT;
		} else {
			contentName = DEFAULT_DATA_STATIC_CONTENT_POLICY;
		}
		String params = DEFAULT_COMMON_PARAM_API_ID
				+ DEFAULT_URL_PARAM_CONNECT_CHAR
				+ DEFAULT_REQUEST_PARAM_STATIC_CONTENT_NAME + contentName;
		return new RequestData(DEFAULT_DOMAIN_SERVICE_DATA,
				DEFAULT_DOMAIN_SDATA_API_STATIC_CONTENT, params,
				DATA_JSON_DEF_REQUESTTYPE_DATA_STATIC_CONTENT, "");
	}

	public static RequestData makeSearchStoriesRequest(String uid,
			String devicetype, String deviceWidth, String strToSearch) {
		if (DEBUG) {
			String params = DEFAULT_COMMON_PARAM_API_ID
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_UID
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_DEVICE_PHONE
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_DEVICE_WIDTH
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_TESTING_PARAM_SEARCH_CONTENT;
			return new RequestData(DEFAULT_DOMAIN_SERVICE_DATA,
					DEFAULT_DOMAIN_SDATA_API_STORY_SEARCH, params,
					DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_SEARCH, "");
		} else {
			if (uid == null || uid.length() == 0) {
				uid = "1";
			}
			if (devicetype == null || devicetype.length() == 0) {
				devicetype = mDevice;
			}
			String params = DEFAULT_COMMON_PARAM_API_ID
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_UID + uid
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_DEVICE + devicetype
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_DEVICE_WIDTH + deviceWidth
					+ DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_SEARCH_KEYWORD + strToSearch;
			return new RequestData(DEFAULT_DOMAIN_SERVICE_DATA,
					DEFAULT_DOMAIN_SDATA_API_STORY_SEARCH, params,
					DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_SEARCH, "");
		}
	}

	public static RequestData makePNSRegisterRequest(String uid, String xid,
			int iActive) {
		String params = DEFAULT_COMMON_PARAM_API_ID
				+ DEFAULT_URL_PARAM_CONNECT_CHAR
				+ DEFAULT_REQUEST_PARAM_PNS_REGISTER_XID + xid
				+ DEFAULT_URL_PARAM_CONNECT_CHAR
				+ DEFAULT_REQUEST_PARAM_PNS_REGISTER_ACTIVE
				+ String.valueOf(iActive);
		if (uid != null && uid.length() > 0) {
			params += DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_UID + uid;
		}
		return new RequestData(DEFAULT_DOMAIN_SERVICE_PNS,
				DEFAULT_DOMAIN_SPNS_API_REGISTER, params,
				DATA_JSON_DEF_REQUESTTYPE_PNS_REGISTER, "");
	}

	public static RequestData makePNSRegisterSectionRequest(String uid,
			String xid, String sections) {
		String params = DEFAULT_COMMON_PARAM_API_ID
				+ DEFAULT_URL_PARAM_CONNECT_CHAR
				+ DEFAULT_REQUEST_PARAM_PNS_REGISTER_XID + xid
				+ DEFAULT_URL_PARAM_CONNECT_CHAR
				+ DEFAULT_REQUEST_PARAM_PNS_REGISTER_SECTIONS + sections;
		if (uid != null && uid.length() > 0) {
			params += DEFAULT_URL_PARAM_CONNECT_CHAR
					+ DEFAULT_REQUEST_PARAM_UID + uid;
		}
		return new RequestData(DEFAULT_DOMAIN_SERVICE_PNS,
				DEFAULT_DOMAIN_SPNS_API_REGISTER_SECTIONS, params,
				DATA_JSON_DEF_REQUESTTYPE_PNS_REGISTER_SECTION, "");
	}

	public static String getSectionId(RequestData req) {
		// the params would in form these cases:
		// 1: "section_id=2323"
		// 2: "section_id=2342&app_id=2342"
		// 3: "app_id=2342&section_id=2342
		// 4: "app_id=2342&section_id=2342&issue_id=1"
		if (req == null || req.params == null
				|| !req.params.contains(DEFAULT_REQUEST_PARAM_SECTION_ID)) {
			return null;
		}
		int start = req.params.indexOf(DEFAULT_REQUEST_PARAM_SECTION_ID) + 11;
		if (start <= 10) {
			return null;
		}
		int end = req.params.indexOf(DEFAULT_URL_PARAM_CONNECT_CHAR, start);
		if (end < 0) {
			return req.params.substring(start);
		}
		if (end <= start) {
			return null;
		}
		return req.params.substring(start, end);
	}

	public static boolean isIssueRequest(RequestData req) {
		return ((DATA_JSON_DEF_REQUESTTYPE_TEMPLATES > req.type) ? true : false);
	}

	public static boolean isTemplateRequest(RequestData req) {
		return (((DATA_JSON_DEF_REQUESTTYPE_TEMPLATES <= req.type) && (DATA_JSON_DEF_REQUESTTYPE_DATA_STORY > req.type)) ? true
				: false);
	}

	public static boolean isStoryRequest(RequestData req) {
		return (((DATA_JSON_DEF_REQUESTTYPE_DATA_STORY <= req.type) && (DATA_JSON_DEF_REQUESTTYPE_DATA_ACCOUNT > req.type)) ? true
				: false);
	}

	public static boolean isStoryDetailRequest(RequestData req) {
		return ((DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_DETAIL == req.type) ? true
				: false);
	}

	public static boolean isAccountManageRequest(RequestData req) {
		return ((DATA_JSON_DEF_REQUESTTYPE_DATA_ACCOUNT <= req.type) ? true
				: false);
	}

	public static boolean isLoginRequest(RequestData req) {
		return ((DATA_JSON_DEF_REQUESTTYPE_USER_ACCOUNT_LOGIN == req.type) ? true
				: false);
	}

	public static boolean isForgotPasswordRequest(RequestData req) {
		return ((DATA_JSON_DEF_REQUESTTYPE_USER_ACCOUNT_FORGOTPASSWORD == req.type) ? true
				: false);
	}

	public static boolean isRegisterRequest(RequestData req) {
		return ((DATA_JSON_DEF_REQUESTTYPE_USER_ACCOUNT_REGISTER == req.type) ? true
				: false);
	}

	public static RequestData makecount(String url) {
		String params = "query=select  like_count from link_stat where url in(\""
				+ url + "\")&format=json";
		return new RequestData(DOMAIN_HOST_COUNT_FB,
				DEFAULT_DOMAIN_SDATA_METHOD, params,
				DATA_JSON_DEF_REQUESTTYPE_DATA_COUNT_FB, "");
	}

	public static RequestData makeFBAccountLoginRequest(UserAccount user) {
		String params = DEFAULT_COMMON_PARAM_API_ID
				+ DEFAULT_URL_PARAM_CONNECT_CHAR + DEFAULT_REQUEST_PARAM_FB_ID
				+ user.getUserId() + DEFAULT_URL_PARAM_CONNECT_CHAR
				+ DEFAULT_REQUEST_PARAM_ACCOUNT_REGISTER_EMAIL
				+ user.getEmail() + DEFAULT_URL_PARAM_CONNECT_CHAR
				+ DEFAULT_REQUEST_PARAM_ACCOUNT_REGISTER_FNAME
				+ user.getFName() + DEFAULT_URL_PARAM_CONNECT_CHAR
				+ DEFAULT_REQUEST_PARAM_ACCOUNT_REGISTER_LNAME
				+ user.getLName() + DEFAULT_URL_PARAM_CONNECT_CHAR
				+ DEFAULT_REQUEST_PARAM_FB_INFO + user.getJsonInfo();
		return new RequestData(DEFAULT_DOMAIN_SERVICE_USER,
				DEFAULT_DOMAIN_SUSER_API_ACCOUNT_LOGIN, params,
				DATA_JSON_DEF_REQUESTTYPE_USER_ACCOUNT_LOGIN, "");
	}

	// http://tnmcms.dev2.gkxim.com/services/tet/getLoiChucXuan2014?api_id=123456&device=tablet
	public static final int DATA_JSON_DEF_REQUESTTYPE_SPRING_GREETINGS = 5001;
	public static final int DATA_JSON_DEF_REQUESTTYPE_SPRING_GALLERY = 5002;
	public static final int DATA_JSON_DEF_REQUESTTYPE_SPRING_GALLERY_STORY_ID = 5003;

	private static final String DEFAULT_DOMAIN_SUSER_API_SPRING_GREETINGS = "/tet/getLoiChucXuan2014";
	private static final String DEFAULT_DOMAIN_SUSER_API_SPRING_GALLERY = "/tet/getTetCuaBan";
	private static final String DEFAULT_DOMAIN_SUSER_API_SPRING_GALLERY_STORY_ID = "/tet/getStoryImages";

	public static RequestData makeGetSpringGreetings(String device) {
		String params = DEFAULT_COMMON_PARAM_API_ID
				+ DEFAULT_URL_PARAM_CONNECT_CHAR + device;
		return new RequestData(DEFAULT_DOMAIN_SERVICE_SPRING,
				DEFAULT_DOMAIN_SUSER_API_SPRING_GREETINGS, params,
				DATA_JSON_DEF_REQUESTTYPE_SPRING_GREETINGS, "");
	}

	// http://tnmcms.dev2.gkxim.com/services/tet/getTetCuaBan?api_id=123456&device=tablet&width=720
	public static RequestData makeGetSpringGallery(String device, String width) {
		String params = DEFAULT_COMMON_PARAM_API_ID
				+ DEFAULT_URL_PARAM_CONNECT_CHAR + device
				+ DEFAULT_URL_PARAM_CONNECT_CHAR
				+ DEFAULT_REQUEST_PARAM_DEVICE_WIDTH + width;
		return new RequestData(DEFAULT_DOMAIN_SERVICE_SPRING,
				DEFAULT_DOMAIN_SUSER_API_SPRING_GALLERY, params,
				DATA_JSON_DEF_REQUESTTYPE_SPRING_GALLERY, "");
	}

	// http://tnmcms.dev2.gkxim.com/services/tet/getStoryImages?api_id=123456&story_id=52922
	public static RequestData makeGetStoryImages(String storyId) {
		String params = DEFAULT_COMMON_PARAM_API_ID
				+ DEFAULT_URL_PARAM_CONNECT_CHAR + "story_id=" + storyId;
		return new RequestData(DEFAULT_DOMAIN_SERVICE_SPRING,
				DEFAULT_DOMAIN_SUSER_API_SPRING_GALLERY_STORY_ID, params,
				DATA_JSON_DEF_REQUESTTYPE_SPRING_GALLERY_STORY_ID, "");
	}
}
