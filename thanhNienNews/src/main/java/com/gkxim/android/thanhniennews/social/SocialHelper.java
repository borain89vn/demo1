/**
 * 
 */
package com.gkxim.android.thanhniennews.social;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.gkxim.android.thanhniennews.tracking.Tracking;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.GKIMLog;
import com.gkxim.android.utils.UIUtils;

/**
 * @author Timon
 * 
 */
public class SocialHelper {
	private static final String TAG = SocialHelper.class.getSimpleName();
	private static final String NETWORK_NAME_MAIL = "mail";
	private static final String NETWORK_NAME_FACEBOOK = "facebook";
	private static final String NETWORK_NAME_TWITTER = "twitter";
	public static final String SOCIAL_SHARE_TWITTER_SCHEMA_CALLBACK = TwitterShare.TWITTER_SCHEMA_CALL_BACK;

	private static SocialHelper _ins = new SocialHelper();

	private Context mApplicationContext = null;
	private Activity mActivity = null;
	private int mSelectedNetworkId = 1; // default is facebook
	private HashMap<Integer, SocialShare> mProviderMap = null;
	private Handler mHandler = getHandler();
	private boolean mLikeWaiting = false;

	private SocialHelper() {
		mProviderMap = new HashMap<Integer, SocialShare>(3);
	}

	public static SocialHelper getInstance(Context context, int netId) {
		if (_ins.mApplicationContext == null
				|| !context.equals(_ins.mApplicationContext)) {
			_ins.mApplicationContext = context;
		}
		if (context instanceof Activity) {
			_ins.mActivity = (Activity) context;
		}
		_ins.mSelectedNetworkId = netId;
		return _ins;
	}

	public static SocialHelper getLastInstance() {
		return _ins;
	}

	@SuppressWarnings("unused")
	private void selectNetworkName(String name) {
		if (name != null && name.length() > 0) {
			String lower = name.toLowerCase();
			if (NETWORK_NAME_MAIL.equals(lower)) {
				mSelectedNetworkId = 0;
			} else if (NETWORK_NAME_FACEBOOK.equals(lower)) {
				mSelectedNetworkId = 1;
			} else if (NETWORK_NAME_TWITTER.equals(lower)) {
				mSelectedNetworkId = 2;
			}
		}
	}

	public void setActivityContext(Activity act) {
		if (act != null) {
			_ins.mActivity = act;
		}
	}

	public void setSocialNetworkId(int netId) {
		synchronized (this) {
			mSelectedNetworkId = netId;
		}
	}

	/**
	 * @param data
	 */
	public void post(String[] data,int status) {
		if (data == null || data.length < 3) {
			return;
		}
		GKIMLog.lf(null, 1, TAG + "=>post");

		SocialShare provider = getSNSInstance();
		
		HashMap<String, String> map = new HashMap<String, String>();
		
		switch (provider.getId()) {
		case 0: // MAIL
			map.put(TNPreferenceManager.EVENT_KEY_SHARED_NETWORK, "Email");
			break;
		case 1: // Facebook
			map.put(TNPreferenceManager.EVENT_KEY_SHARED_NETWORK, "Facebook");
			break;
		case 2: // Twitter
			map.put(TNPreferenceManager.EVENT_KEY_SHARED_NETWORK, "Twitter");
			break;
		default:
			break;
		}
		
		map.put(TNPreferenceManager.EVENT_KEY_STORY_ID, data[0]);
		map.put(TNPreferenceManager.EVENT_KEY_STORY_NAME, data[1]);
		//FlurryAgent.onEvent(TNPreferenceManager.EVENT_STORY_SHARED, map);
		Tracking.sendEvent(TNPreferenceManager.EVENT_STORY_SHARED, map);
		if (!provider.hasInit()){
			provider.initialize(mActivity);
		}

		if (!provider.isReadyForShare()) {
			Message msgPost = new Message();
			msgPost.what = status;
			msgPost.obj = data;
			msgPost.setTarget(mHandler );
			provider.setCompletedMessage(msgPost);
			provider.login();
			
		} else {
			// FIXME: invoke post completed.
//			if (mLikeWaiting) {
//				Message msgLike = new Message();
//				msgLike.what = 2;
//				msgLike.obj = data;
//				msgLike.setTarget(mHandler );
//				provider.setCompletedMessage(msgLike);
//			}
			provider.post(data);
		}

	}
	
	//Nam.nguyen
	public void loginApp(Message msg) { 
		SocialShare provider = getSNSInstance();
		
		if (provider == null) {
			UIUtils.showToast(mApplicationContext, "There has no provider");
		}
		if (!provider.hasInit()) {
			provider.initialize(mActivity);
		}

		if (!provider.isReadyForShare()) {
			if (msg != null) {
				provider.setCompletedMessage(msg);
			}
			provider.loginApp();
		} else {
			GKIMLog.lf(null, 1, TAG + "=> login successful");
		}
	}

	public void logout() { 
		GKIMLog.log("Nam.nguyen:" + TAG +"logout ");
		SocialShare provider = getSNSInstance();
		if (provider == null) {
			UIUtils.showToast(mApplicationContext, "There has no provider");
		}
		if (!provider.hasInit()) {
			provider.initialize(mActivity);
		}
		provider.logout();
		
		GKIMLog.lf(null, 1, TAG + "=> facebook logout successful");
	}
	/**
	 * like is only available in Facebook
	 */
	public void like(String postId, String[] data) {
		if (data == null || data.length < 4) {
			return;
		}
		GKIMLog.lf(null, 1, TAG + "=>processing facebook like");
		
		FacebookShare provider = (FacebookShare) getSNSInstance();
		if (provider == null) {
			UIUtils.showToast(mApplicationContext, "There has no provider");
		}
		if (!provider.hasInit()){
			provider.initialize(mActivity);
		}

		if (!provider.isReadyForShare()) {
			provider.login();
			
		} else {
			
			provider.likeAnUrl(data[3],data[0]);
		}
	}
	
	public void checklike(String postId, String[] data) {
		if (data == null || data.length < 4) {
			return;
		}
		GKIMLog.lf(null, 1, TAG + "=>processing facebook like");
		
		FacebookShare provider = (FacebookShare) getSNSInstance();
		if (provider == null) {
			UIUtils.showToast(mApplicationContext, "There has no provider");
		}
		if (!provider.hasInit()){
			provider.initialize(mActivity);
		}

		if (provider.isReadyForShare()) {
			GKIMLog.l(4, "data[3] : " +data[0]);
			provider.checklikeAnUrl(data[3],data[0]);
		}
	}
	
	/**
	 * @return SocialShare
	 */
	public SocialShare getSNSInstance() {
		SocialShare result = null;
		if (mProviderMap.containsKey(mSelectedNetworkId)) {
			GKIMLog.lf(null, 0, TAG + "=>getSNSInstance found from providers:"+ mSelectedNetworkId);
			return mProviderMap.get(mSelectedNetworkId);
		}

		switch (mSelectedNetworkId) {
		case 0: // MAIL
			result = new MailShare();
			break;
		case 1: // Facebook
			result = new FacebookShare();
			break;
		case 2: // Twitter
			result = new TwitterShare();
			break;
		default:
			break;
		}
		if (result != null) {
			mProviderMap.put(mSelectedNetworkId, result);
		}
		return result;
	}

	private Handler getHandler() {
		return (new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 0: //Login
					
					break;
				case 1: //post
					SocialHelper.this.post((String[])msg.obj,1);
					break;
				case 2: //like
					String[] data = (String[])msg.obj;
					SocialHelper.this.like(data[3], data);
					break;
				default:
					break;
				}
				super.handleMessage(msg);
			}
		});
	}
}
