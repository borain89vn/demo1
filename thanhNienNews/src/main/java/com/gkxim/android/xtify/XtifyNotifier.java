package com.gkxim.android.xtify;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.gkxim.android.utils.GKIMLog;
import com.xtify.sdk.api.XtifyBroadcastReceiver;
import com.xtify.sdk.api.XtifySDK;

public class XtifyNotifier extends XtifyBroadcastReceiver {

	private static final String TAG = XtifyNotifier.class.getName();
	private static final String ACTION_INTENT_XTIFY_NOTIFIER = "com.xtify.sdk.NOTIFIER";

	// build handler message for handling specified message to XTifyController.
	private OnXTifyListener mOnXTifyListener = null;

	public interface OnXTifyListener {
		boolean onReceive(Context context, Intent intent);

		void onMessageReceived(Context context, Bundle msgExtras);

		void onDeviceRegistered(Context context, String xid, String regid);
	}

	@Override
	public void onMessage(Context context, Bundle msgExtras) {
		GKIMLog.lf(context, 0, TAG + "=>onMessage");
		GKIMLog.lf(context, 1, TAG + ", Extra: " + msgExtras.toString());
		if (mOnXTifyListener != null) {
			mOnXTifyListener.onMessageReceived(context, msgExtras);
		}
	}

	@Override
	public void onRegistered(Context context) {
		GKIMLog.lf(context, 0,
				TAG + "=>onRegistered: " + XtifySDK.getXidKey(context));
		if (mOnXTifyListener != null) {
			mOnXTifyListener.onDeviceRegistered(context,
					XtifySDK.getXidKey(context),
					XtifySDK.getRegistrationId(context));
		}
	}

	@Override
	public void onC2dmError(Context context, String errorId) {
		GKIMLog.lf(context, 0, TAG + "=>onC2dmError: " + errorId);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		GKIMLog.lf(context, 0, TAG + "=>onReceive");
		boolean hasProceed = false;
		if (mOnXTifyListener != null) {
			hasProceed = mOnXTifyListener.onReceive(context, intent);
		}
		if (!hasProceed) {
			super.onReceive(context, intent);
		}
	}

	public static IntentFilter getXtifyNotifierReceiverIntentFilter() {
		return new IntentFilter(ACTION_INTENT_XTIFY_NOTIFIER);
	}

	public void setOnXTifyListener(OnXTifyListener listener) {
		mOnXTifyListener = listener;
	}
}
