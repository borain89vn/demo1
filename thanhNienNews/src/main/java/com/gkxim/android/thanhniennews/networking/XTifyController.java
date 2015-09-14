/**
 * File: TNxTifyController.java
 * Creator: Timon.Trinh (timon@gkxim.com)
 * Date: 10-12-2012
 * Working with Xtify SDK v2.2.2.4
 */
package com.gkxim.android.thanhniennews.networking;

import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.models.GenericResponse;
import com.gkxim.android.thanhniennews.tracking.Tracking;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.DataDownloader;
import com.gkxim.android.utils.DataDownloader.OnDownloadCompletedListener;
import com.gkxim.android.utils.GKIMLog;
import com.gkxim.android.utils.RequestData;
import com.gkxim.android.utils.Utils;
import com.gkxim.android.xtify.XtifyNotifier;
import com.gkxim.android.xtify.XtifyNotifier.OnXTifyListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xtify.sdk.api.NotificationsPreference;
import com.xtify.sdk.api.XtifySDK;

/**
 * xTify controller for calling and storing xTify configuration
 */
public class XTifyController {
	// This is xTify APP key.
	static final String XTIFY_APP_KEY =  "2deb34a9-db0e-4713-b234-8653b9a6d43d";//"650f11fc-2186-4b91-a5d0-55a0a3d05b61";
	// This is the Google Project ID
	static final String SENDER_ID = "827711607488";
//	// This is xTify APP key for DEV
//	static final String XTIFY_APP_KEY = "ce446ec6-c96d-4ee3-ba7d-e1520375536a";
//	// This is the Google Project ID  for DEV
//	static final String SENDER_ID = "1093553867562";
	
	private static final String TAG = "XTifyController";

	private static final int XTIFY_CONST_FEATURE_ALL = 0;
	private static final int XTIFY_CONST_FEATURE_SOUND = 1;
	private static final int XTIFY_CONST_FEATURE_VIBRATE = 2;
	private static final int XTIFY_CONST_FEATURE_LIGHT = 3;

	private static XtifyNotifier receiver = null;
	private static boolean mRegistered = false;
	private static String mXid = null;
	private static OnXTifyListener mOnXTifyListener = getDefaultOnXtifyListener();

	public static void start() {
		Context context = TNPreferenceManager.getApplicationContext();
		GKIMLog.lf(context, 0, TAG + "=>start xtify service.");
		XtifySDK.start(context, XTIFY_APP_KEY, SENDER_ID);
		NotificationsPreference.setIcon(context, R.drawable.ic_launcher);
		// NotificationsPreference.setSound(context);
		NotificationsPreference.setVibrationPattern(context, new long[] { 0,
				100, 200, 300 });
		NotificationsPreference.setLights(context, new int[] { 0x00a2ff, 300,
				1000 });

		NotificationsPreference.setSoundEnabled(context, false);
		NotificationsPreference.setVibrateEnabled(context, true);
		NotificationsPreference.setLightsEnabled(context, true);
	}

	public static void registerOnXtifyListenPNS(Context context,
			XtifyNotifier.OnXTifyListener listener) {
		if (receiver == null) {
			receiver = new XtifyNotifier();
			receiver.setOnXTifyListener(mOnXTifyListener);
			context.registerReceiver(receiver,
					XtifyNotifier.getXtifyNotifierReceiverIntentFilter());
		} else {
			GKIMLog.lf(context, 0, TAG
					+ "=>OnXTifyListener should have 1 instance only ("
					+ getXID() + ").");
		}
	}

	public static void unregisterOnXtifyListenerPNS() {
		Context context = TNPreferenceManager.getApplicationContext();
		if (receiver != null) {
			receiver.setOnXTifyListener(null);
			context.unregisterReceiver(receiver);
			receiver = null;
		} else {
			GKIMLog.lf(context, 0, TAG
					+ "=>OnXTifyListener didn't have an instance to stop.");
		}
	}

	/**
	 * @Description: The XID is not available until the device is registered. If
	 *               you want to obtain the XID and send it to your server you
	 *               should do it in XtifyNotifier onRegistered method.
	 * 
	 * @return
	 */
	public static String getXID() {
		String xid = XtifySDK.getXidKey(TNPreferenceManager
				.getApplicationContext());
		if (xid != null) {
			return xid;
		} else {
			GKIMLog.lf(TNPreferenceManager.getApplicationContext(), 0, TAG
					+ "=> Device is not yet registered with Xtify.");
			return "";
		}
	}

	public static void setEnableFeature(int feature, boolean state) {
		Context context = TNPreferenceManager.getApplicationContext();
		switch (feature) {
		case XTIFY_CONST_FEATURE_ALL:
			NotificationsPreference.setSoundEnabled(context, state);
			NotificationsPreference.setVibrateEnabled(context, state);
			NotificationsPreference.setLightsEnabled(context, state);
			break;
		case XTIFY_CONST_FEATURE_SOUND:
			NotificationsPreference.setSoundEnabled(context, state);
			break;
		case XTIFY_CONST_FEATURE_VIBRATE:
			NotificationsPreference.setVibrateEnabled(context, state);
			break;
		case XTIFY_CONST_FEATURE_LIGHT:
			NotificationsPreference.setLightsEnabled(context, state);
			break;
		default:
			break;
		}
	}

	private static OnXTifyListener getDefaultOnXtifyListener() {

		return (new OnXTifyListener() {
			@Override
			public void onMessageReceived(Context context, Bundle msgExtras) {
				GKIMLog.lf(context, 1, TAG + "=>onMessageReceived");
			}

			@Override
			public void onDeviceRegistered(Context context, String xid,
					String regid) {
				GKIMLog.lf(context, 1, TAG + "=>onDeviceRegistered " + xid + "  "+regid);
				// Invoke server registration
				mXid = xid;
				boolean hasEnabled = TNPreferenceManager.getPNSEnabled();
				updatePNSStatus(context, (hasEnabled ? 1 : 0));
				HashMap<String, String> map = new HashMap<String, String>();
				map.put(TNPreferenceManager.EVENT_XTIFY_REGISTERED_ID, xid);
//				FlurryAgent.onEvent(TNPreferenceManager.EVENT_XTIFY_REGISTERED,
//						map);
				Tracking.sendEvent(TNPreferenceManager.EVENT_XTIFY_REGISTERED,
						map);
			}

			@Override
			public boolean onReceive(Context context, Intent intent) {
				boolean hasEnabled = TNPreferenceManager.getPNSEnabled();
				GKIMLog.lf(context, 1, TAG + "=>onReceive: " + hasEnabled
						+ ", registered: " + mRegistered);
				boolean showPNS = false;
				HashMap<String, String> map = new HashMap<String, String>();
				if (mRegistered && !hasEnabled) {
					showPNS = true;
				}
				map.put(TNPreferenceManager.EVENT_XTIFY_RECEIVED_SHOW,
						(showPNS ? "true" : "false"));
//				FlurryAgent.onEvent(TNPreferenceManager.EVENT_XTIFY_RECEIVED,
//						map);
				Tracking.sendEvent(TNPreferenceManager.EVENT_XTIFY_RECEIVED,
						map);
				return showPNS;
			}
		});
	}

	/**
	 * @param xid
	 * @param i
	 */
	protected static void updatePNSStatus(final Context context, int iActive) {
		DataDownloader mTask = new DataDownloader(
				new OnDownloadCompletedListener() {

					@Override
					public void onCompleted(Object key, String result) {
						GKIMLog.lf(null, 1, TAG + "=>onCompleted: " + key);
						if (result == null || result.length() <= 0) {
							return;
						}
						int type = ((RequestData) key).type;
						if (type == RequestDataFactory.DATA_JSON_DEF_REQUESTTYPE_PNS_REGISTER) {
							Gson gson = new GsonBuilder()
									.registerTypeAdapter(
											GenericResponse.class,
											new GenericResponse.GenericResponseConverter())
									.create();
							GenericResponse gres = gson.fromJson(result,
									GenericResponse.class);
							if (gres != null && gres.isSucceed()) {
								GKIMLog.lf(null, 0, TAG
										+ "Register PNS is succeed." + key);
								mRegistered = true;
								if (GKIMLog.DEBUG_ON) {
									String[] sectionIds = TNPreferenceManager
											.getSectionIDs();
									if (sectionIds != null
											&& sectionIds.length > 0) {
										String strSectionIds = Utils
												.arrayToString(sectionIds, ", ");
										listenToSections(context, " "
												+ strSectionIds);
									}
								}
							}
						}
					}

					@Override
					public String doInBackgroundDebug(Object... params) {
						return null;
					}
				});
		String uid = TNPreferenceManager.getUserId();
		mTask.addDownload(RequestDataFactory.makePNSRegisterRequest(uid, mXid,
				iActive));
	}

	public static void listenToSections(Context context, String sectionIds) {
		if (!mRegistered || mXid == null || sectionIds == null
				|| sectionIds.length() <= 0) {
			return;
		}
		GKIMLog.lf(null, 0, TAG + "=>listenToSections: " + sectionIds);
		String uid = TNPreferenceManager.getUserId();
		DataDownloader mTask = new DataDownloader(
				new OnDownloadCompletedListener() {

					@Override
					public void onCompleted(Object key, String result) {
						GKIMLog.lf(null, 1, TAG + "=>onCompleted: " + key);
						if (result == null || result.length() <= 0) {
							return;
						}
						int type = ((RequestData) key).type;
						if (type == RequestDataFactory.DATA_JSON_DEF_REQUESTTYPE_PNS_REGISTER_SECTION) {
							Gson gson = new GsonBuilder()
									.registerTypeAdapter(
											GenericResponse.class,
											new GenericResponse.GenericResponseConverter())
									.create();
							GenericResponse gres = gson.fromJson(result,
									GenericResponse.class);
							if (gres != null && gres.isSucceed()) {
								GKIMLog.lf(null, 0, TAG
										+ "Listening to sections.");
							}
						}
					}

					@Override
					public String doInBackgroundDebug(Object... params) {
						return null;
					}
				});
		mTask.addDownload(RequestDataFactory.makePNSRegisterSectionRequest(uid,
				mXid, sectionIds));
		TNPreferenceManager.setListeningSections(sectionIds);
	}
}
