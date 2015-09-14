/**
 * 
 */
package com.gkxim.android.thanhniennews.social;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Message;

import com.gkxim.android.utils.GKIMLog;

/**
 * Not directly us this abstract class
 * 
 * @author Timon
 * 
 */
public class SocialShare implements ISocialShare {
	private static final String TAG = SocialShare.class.getSimpleName();
	public static final int ACTIVITY_REQUESTCODE_LOGIN = 129;
	public static final int ACTIVITY_REQUESTCODE_POST = 130;

	protected Context mApplicationContext = null;
	protected Activity mActivityContext = null;

	private int mState = 0; // 0: created, 1: initialized, 2: user's data
	private Message mCompletedMessage;

	// requested, 3:expired, ..
	
	protected boolean mLoginApp = false;
	
	public Message getmCompletedMessage() {
		return mCompletedMessage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gkxim.android.thanhniennews.social.ISocialShare#getId()
	 */
	@Override
	public int getId() {
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gkxim.android.thanhniennews.social.ISocialShare#initialize()
	 */
	@Override
	public void initialize(Context context) {
		if (context instanceof Activity) {
			mActivityContext = (Activity) context;
		}
		mApplicationContext = context.getApplicationContext();
	}

	public void initialize(Activity activity) {
		mActivityContext = activity;
		mApplicationContext = activity.getApplicationContext();
	}

	public boolean isReadyForShare() {
		return false;
	}

	public boolean hasInit() {
		return (mState >= 1);
	}

	protected void setInitialized() {
		if (mState < 1) {
			mState += 1;
		}
	}

	public void setCompletedMessage(Message msg) {
		mCompletedMessage = msg;
	}

	protected void postCompletedMessage(String... addContent) {
		if (mCompletedMessage != null) {
			GKIMLog.lf(null, 0, TAG + "=>postCompletedMessage");
			if (addContent != null) {
				String[] olddata = (String[]) mCompletedMessage.obj;
				int length = addContent.length;
				int oldlen = 0;
				if (olddata != null) {
					oldlen = olddata.length;
					length += oldlen;
				}
				String[] newdata = new String[length];
				System.arraycopy(olddata, 0, newdata, 0, oldlen);
				if (length > oldlen) {
					System.arraycopy(addContent, 0, newdata, oldlen, length
							- oldlen);
				}
				GKIMLog.lf(null, 0,
						TAG + "=>content to post: " + newdata.toString());
				mCompletedMessage.obj = newdata;
			}
			mCompletedMessage.sendToTarget();
			mCompletedMessage = null;
		}
	}

	@Override
	public void login() {
		GKIMLog.lf(null, 1, TAG + "=>login is not implemeted here.");
	}

	@Override
	public void post(String[] data) {
		GKIMLog.lf(null, 1, TAG + "=>post is not implemeted here.");
	}

	@Override
	public String getAccessToken() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gkxim.android.thanhniennews.social.ISocialShare#handlingActivityForResult
	 * (int, int, com.gkxim.android.thanhniennews.social.Intent)
	 */
	@Override
	public boolean handlingActivityForResult(int requestCode, int resultCode,
			Intent data) {
		GKIMLog.lf(null, 1, TAG + "=>handlingActivityForResult :" + requestCode);
		return false;
	}

	@Override
	public void loginApp() {
		// TODO Auto-generated method stub
		GKIMLog.lf(null, 1, TAG + "=>loginApp is not implemeted here.");
	}

	@Override
	public void logout() {
		// TODO Auto-generated method stub
		GKIMLog.lf(null, 1, TAG + "=>logout.");
	}
}
