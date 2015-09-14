package com.gkxim.android.thanhniennews.tracking;

import java.util.HashMap;
import java.util.Iterator;

import android.app.Activity;
import android.content.Context;

import com.gkxim.android.thanhniennews.ThanhNienNewsApp;
import com.gkxim.android.thanhniennews.ThanhNienNewsApp.TrackerName;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public final class Tracking {

	public static Tracker mTracker;
	public static Tracker mMyTracker;
	public static Activity mCurrentContext = null;

	private Tracking() {

	}

	public static void startSession(Activity act) {
		mCurrentContext = act;
		if (!TNPreferenceManager.isConnectionAvailable()) {
			return;
		}
		// Call start session for flurry
		// FlurryAgent.onStartSession(act,
		// TNPreferenceManager.getFlurryAPIKey());
		String appVer = TNPreferenceManager.getAppVersionName((Context) act);
		mTracker = // Get tracker.
		((ThanhNienNewsApp) mCurrentContext.getApplication())
				.getTracker(TrackerName.GOOGLE_ANALYTIC_TRACKER);
		mTracker.setAppVersion(appVer);
		mMyTracker = ((ThanhNienNewsApp) mCurrentContext.getApplication())
				.getTracker(TrackerName.THANHNIENNEWS_TRACKER);
		mMyTracker.setAppVersion(appVer);
		
		mTracker.enableAdvertisingIdCollection(true);
	}

	public static void endSeesion(Activity act) {
		if (!TNPreferenceManager.isConnectionAvailable()) {
			return;
		}
		// FlurryAgent.onEndSession(act);
	}

	public static void sendEvent(String eventId,
			HashMap<String, String> parameters) {
		if (!TNPreferenceManager.isConnectionAvailable()) {
			return;
		}
		if (parameters != null) {
			// FlurryAgent.onEvent(eventId, parameters);
			try {
				final Iterator<String> cursor = parameters.keySet().iterator();
				while (cursor.hasNext()) {
					String key = (String) cursor.next();
					String value = parameters.get(key);
					if (mTracker != null) {

						// Build and send an Event.
						mTracker.send(new HitBuilders.EventBuilder()
								.setCategory(eventId).setAction(key)
								.setLabel(value).build());
					}
					if (mMyTracker != null) {
						mMyTracker.send(new HitBuilders.EventBuilder()
								.setCategory(eventId).setAction(key)
								.setLabel(value).build());
					}
				}
			} catch (Exception e) {
				// cursor.next() might throw NoSuchElementException
			}
		} else {
			// FlurryAgent.onEvent(eventId);
			if (mTracker != null || mMyTracker != null) {
				// Set screen name.
				mTracker.setScreenName(eventId);
				// Send a screen view.
				mTracker.send(new HitBuilders.AppViewBuilder().build());
				// Set screen name.
				mMyTracker.setScreenName(eventId);
				// Send a screen view.
				mMyTracker.send(new HitBuilders.AppViewBuilder().build());
			}
		}

	}

	public static void trackDownload(Context context) {
		// if (context != null) {
		// (new AffleAppDownloadTracker()).trackDownload(
		// context.getApplicationContext(), null);
		// }
	}
}
