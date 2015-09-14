package com.gkxim.android.thanhniennews;

import android.app.Application;
import android.content.SharedPreferences;

import com.gkim.thanhniennews.R;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

public class ThanhNienNewsApp extends Application {
	public static SharedPreferences preferences;
	public static String KEY_CATCH_LOW_MEMORY="CatchMemory";
	public static String KEY_START_SERVICE="StartService";
	@Override
	public void onCreate() {
		super.onCreate();
		preferences = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
	}

    /**
	 * Enum used to identify the tracker that needs to be used for tracking.
	 * 
	 * A single tracker is usually enough for most purposes. In case you do need
	 * multiple trackers, storing them all in Application object helps ensure
	 * that they are created only once per application instance.
	 */
	public enum TrackerName {
		GOOGLE_ANALYTIC_TRACKER, THANHNIENNEWS_TRACKER, ECOMMERCE_TRACKER
	}

	HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

	public synchronized Tracker getTracker(TrackerName trackerId) {
		if (!mTrackers.containsKey(trackerId)) {

			GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
			Tracker t = (trackerId == TrackerName.GOOGLE_ANALYTIC_TRACKER) ? analytics
					.newTracker(R.xml.global_tracker) : analytics
					.newTracker(R.xml.thanhniennews_tracker);

			mTrackers.put(trackerId, t);
		}
		return mTrackers.get(trackerId);
	}

}
