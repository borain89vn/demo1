/**
 * File: ApplicationRating.java
 * Creator: Timon.Trinh (timon@gkxim.com)
 * Date: 17-07-2013
 * A static class that help for application's rating on Google Play
 */
package com.gkxim.android.utils;

import java.util.HashMap;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.tracking.Tracking;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;

public class ApplicationRating {

	private static final String PREFKEY_DONSHOWAGAIN = "rate_dont";
	private static final String PREFKEY_COUNT = "rate_count";
	private static final String PREFKEY_DATELAUNCH = "rate_launchdate";
	private static final String PREFKEY_RATED = "rate_did";

	private static final int DAYS_UNTIL_PROMPT = 2;
	private static final int LAUNCHES_UNTIL_PROMPT = 1;
	private static boolean isShowingDialogRotate = false;
	private static Dialog mDialog = null;

	public static void checkForRating(Context context) {
		GKIMLog.lf(context, 0, "ApplicationRating=>check for rating");
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		if (prefs.getBoolean(PREFKEY_RATED, false)) {
			return;
		}
		if (prefs.getBoolean(PREFKEY_DONSHOWAGAIN, false)) {
			return;
		}

		SharedPreferences.Editor editor = prefs.edit();

		// Increment launch counter
		long launchCount = prefs.getLong(PREFKEY_COUNT, 0) + 1;
		editor.putLong(PREFKEY_COUNT, launchCount);

		// Get date of first launch
		Long dateFirstLaunch = prefs.getLong(PREFKEY_DATELAUNCH, 0);
		if (dateFirstLaunch == 0) {
			dateFirstLaunch = System.currentTimeMillis();
			editor.putLong(PREFKEY_DATELAUNCH, dateFirstLaunch);
		}

		// boolean bMod = (launchCount%LAUNCHES_UNTIL_PROMPT)==0?true:false;
		if (launchCount >= LAUNCHES_UNTIL_PROMPT) {
			// Wait at least n days before opening
			if (System.currentTimeMillis() >= dateFirstLaunch
					+ (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
				showRatingDialog(context, editor);
			}
		}
		editor.commit();
	}

	public static boolean isShowing() {
		return isShowingDialogRotate;
	}

	public static void CloseDialog() {
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
			mDialog = null;
		}
		isShowingDialogRotate = false;
	}

	static void showRatingDialog(final Context context, final Editor editor) {
		// generating dialog layout
		if (mDialog == null) {
			mDialog = new Dialog(context);
			mDialog.setCanceledOnTouchOutside(false);
			mDialog.setTitle(R.string.app_name);
			mDialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
			mDialog.setContentView(R.layout.dlg_section_rating);
			mDialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
					R.drawable.ic_launcher);
		}

		Button btn = (Button) mDialog
				.findViewById(R.id.btn_section_dlg_rating_confirmyes);
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				context.startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse("market://details?id="
								+ context.getPackageName())));
				mDialog.dismiss();

				HashMap<String, String> map = new HashMap<String, String>();
				map.put(TNPreferenceManager.EVENT_RATING_SELECTED, "1");
				// FlurryAgent.onEvent(TNPreferenceManager.EVENT_RATING, map);
				Tracking.sendEvent(TNPreferenceManager.EVENT_RATING, map);

				// Save into SharedPreferences rated for the user's
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(context);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putBoolean(PREFKEY_RATED, true);
				editor.commit();
			}
		});
		btn = (Button) mDialog
				.findViewById(R.id.btn_section_dlg_rating_confirmno);
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (editor != null) {
					editor.putBoolean(PREFKEY_DONSHOWAGAIN, true);
					editor.commit();
				}
				mDialog.dismiss();
				HashMap<String, String> map = new HashMap<String, String>();
				map.put(TNPreferenceManager.EVENT_RATING_SELECTED, "2");
				// FlurryAgent.onEvent(TNPreferenceManager.EVENT_RATING, map);
				Tracking.sendEvent(TNPreferenceManager.EVENT_RATING, map);
			}
		});
		btn = (Button) mDialog
				.findViewById(R.id.btn_section_dlg_rating_confirmnotnow);
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (editor != null) {
					editor.putLong(PREFKEY_DATELAUNCH,
							System.currentTimeMillis());
					editor.commit();
				}

				mDialog.dismiss();
				HashMap<String, String> map = new HashMap<String, String>();
				map.put(TNPreferenceManager.EVENT_RATING_SELECTED, "3");
				// FlurryAgent.onEvent(TNPreferenceManager.EVENT_RATING, map);
				Tracking.sendEvent(TNPreferenceManager.EVENT_RATING, map);
			}
		});
		mDialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
				isShowingDialogRotate = false;
			}
		});

		mDialog.show();
		isShowingDialogRotate = true;

	}
}
