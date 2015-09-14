/*
 * Copyright @ Timon Trinh
 * Website http://trinhquochungnotes.blogspot.com
 */

package com.gkxim.android.utils;

import java.util.Calendar;
import java.util.TimeZone;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gkim.thanhniennews.R;
import com.gkxim.android.cache.UrlImageViewCallback;
import com.gkxim.android.cache.UrlImageViewHelper;

/**
 * An assortment of UI helpers.
 */
public class UIUtils {
	/**
	 * Time zone to use when formatting all session times. To always use the
	 * phone local time, use {@link TimeZone#getDefault()}.
	 */
	public static final TimeZone VN_TIME_ZONE = TimeZone
			.getTimeZone("GMT+7:00");

	private static final int SECOND_MILLIS = 1000;
	private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
	private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
	private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

	public static final long TOAST_LONG = 3 * SECOND_MILLIS;
	public static final long TOAST_SHORT = SECOND_MILLIS;

	/** Flags used with {@link DateUtils#formatDateRange}. */
	private static final int TIME_FLAGS = DateUtils.FORMAT_SHOW_TIME
			| DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_WEEKDAY;

	private static StyleSpan sBoldSpan = new StyleSpan(Typeface.BOLD);
	// get default type face from context/assets.
	private static Typeface tfDefaultTypeFace = null;
	private static Typeface tfBoldTypeFace = null;
	private static Typeface tfItalicTypeFace = null;
	private static Typeface tfRegularTypeFace = null;
	private static final long sAppLoadTime = System.currentTimeMillis();
	private static final boolean DEBUG = GKIMLog.DEBUG_ON;

	private static final CharSequence HASHTAG = "UIUtils";
	private static Context applicationContext = null;
	private static Toast mToast;
	private static TextView mMessage;

	/**
	 * Format and return the given {@link Blocks} values using
	 * {@link #CONFERENCE_TIME_ZONE}.
	 */
	public static String formatBlockTimeString(long blockStart, long blockEnd,
			Context context) {
		TimeZone.setDefault(VN_TIME_ZONE);

		// NOTE: There is an efficient version of formatDateRange in Eclair and
		// beyond that allows you to recycle a StringBuilder.
		return DateUtils.formatDateRange(context, blockStart, blockEnd,
				TIME_FLAGS);
	}

	public static boolean isSameDay(long time1, long time2) {
		TimeZone.setDefault(VN_TIME_ZONE);

		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.setTimeInMillis(time1);
		cal2.setTimeInMillis(time2);
		return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
				&& cal1.get(Calendar.DAY_OF_YEAR) == cal2
						.get(Calendar.DAY_OF_YEAR);
	}

	public static String getTimeAgo(long time, Context ctx) {
		if (time < 1000000000000L) {
			// if timestamp given in seconds, convert to millis
			time *= 1000;
		}

		long now = getCurrentTime(ctx);
		if (time > now || time <= 0) {
			return null;
		}

		// localize
		final long diff = now - time;
		if (diff < MINUTE_MILLIS) {
			return "just now";
		} else if (diff < 2 * MINUTE_MILLIS) {
			return "a minute ago";
		} else if (diff < 50 * MINUTE_MILLIS) {
			return diff / MINUTE_MILLIS + " minutes ago";
		} else if (diff < 90 * MINUTE_MILLIS) {
			return "an hour ago";
		} else if (diff < 24 * HOUR_MILLIS) {
			return diff / HOUR_MILLIS + " hours ago";
		} else if (diff < 48 * HOUR_MILLIS) {
			return "yesterday";
		} else {
			return diff / DAY_MILLIS + " days ago";
		}
	}

	/**
	 * Populate the given {@link TextView} with the requested text, formatting
	 * through {@link Html#fromHtml(String)} when applicable. Also sets
	 * {@link TextView#setMovementMethod} so inline links are handled.
	 */
	public static void setTextMaybeHtml(TextView view, String text) {
		if (TextUtils.isEmpty(text)) {
			view.setText("");
			return;
		}
		if (text.contains("<") && text.contains(">")) {
			view.setText(Html.fromHtml(text));
			view.setMovementMethod(LinkMovementMethod.getInstance());
		} else {
			view.setText(text);
		}
	}

	/**
	 * Given a snippet string with matching segments surrounded by curly braces,
	 * turn those areas into bold spans, removing the curly braces.
	 */
	public static Spannable buildStyledSnippet(String snippet) {
		final SpannableStringBuilder builder = new SpannableStringBuilder(
				snippet);

		// Walk through string, inserting bold snippet spans
		int startIndex = -1, endIndex = -1, delta = 0;
		while ((startIndex = snippet.indexOf('{', endIndex)) != -1) {
			endIndex = snippet.indexOf('}', startIndex);

			// Remove braces from both sides
			builder.delete(startIndex - delta, startIndex - delta + 1);
			builder.delete(endIndex - delta - 1, endIndex - delta);

			// Insert bold style
			builder.setSpan(sBoldSpan, startIndex - delta,
					endIndex - delta - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

			delta += 2;
		}

		return builder;
	}

	public static void loadToImageView(String url, ImageView imageView) {
		if (url != null && imageView != null) {
			UrlImageViewHelper.setUrlDrawable(imageView, url, false, 1, 1);
		}
	}

	public static void loadToImageView(String url, ImageView imageView,
			int screenwidth) {
		if (url != null && imageView != null) {
			UrlImageViewHelper.setUrlDrawable(imageView, url, true,
					screenwidth, -1);
		}
	}

	public static void loadToImageView(String url, ImageView imageView,
			UrlImageViewCallback callback, String id) {
		if (url != null && imageView != null) {
			UrlImageViewHelper.setUrlDrawable(imageView, url, callback, false,
					-1, -1);
		}
	}

	public static void loadToCache(Context context, String url,
			UrlImageViewCallback callback, String id) {
		if (url != null && (url.length() > 0) && id != null
				&& (id.length() > 0) && callback != null) {
			UrlImageViewHelper.loadUrlDrawableWithId(context, url, callback,
					false, 1, 1, id);
		}
	}

	public static void loadToCache(Context context, String url,
			UrlImageViewCallback callback, int scaleWidth, int scaleHeight,
			String id) {
		if (url != null && (url.length() > 0) && id != null
				&& (id.length() > 0) && callback != null) {
			UrlImageViewHelper.loadUrlDrawableWithId(context, url, callback,
					true, scaleWidth, scaleHeight, id);
		}
	}

	public static String getSessionHashtagsString(String hashtags) {
		if (!TextUtils.isEmpty(hashtags)) {
			if (!hashtags.startsWith("#")) {
				hashtags = "#" + hashtags;
			}

			if (hashtags.contains(HASHTAG)) {
				return hashtags;
			}
			return HASHTAG + " " + hashtags;
		} else {
			return (String) HASHTAG;
		}
	}

	/**
	 * Calculate whether a color is light or dark, based on a commonly known
	 * brightness formula.
	 * 
	 * @see {@literal http://en.wikipedia.org/wiki/HSV_color_space%23Lightness}
	 */
	public static boolean isColorDark(int color) {
		return ((30 * Color.red(color) + 59 * Color.green(color) + 11 * Color
				.blue(color)) / 100) <= BRIGHTNESS_THRESHOLD;
	}

	private static final int BRIGHTNESS_THRESHOLD = 130;

	public static void preferPackageForIntent(Context context, Intent intent,
			String packageName) {
		PackageManager pm = context.getPackageManager();
		for (ResolveInfo resolveInfo : pm.queryIntentActivities(intent, 0)) {
			if (resolveInfo.activityInfo.packageName.equals(packageName)) {
				intent.setPackage(packageName);
				break;
			}
		}
	}

	// Shows whether a notification was fired for a particular session time
	// block. In the
	// event that notification has not been fired yet, return false and set the
	// bit.
	public static boolean isNotificationFiredForBlock(Context context,
			String blockId) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		final String key = String.format("notification_fired_%s", blockId);
		boolean fired = sp.getBoolean(key, false);
		sp.edit().putBoolean(key, true).commit();
		return fired;
	}

	public static long getCurrentTime(final Context context) {
		if (DEBUG) {
			return context.getSharedPreferences("mock_data",
					Context.MODE_PRIVATE).getLong("mock_current_time",
					System.currentTimeMillis())
					+ System.currentTimeMillis() - sAppLoadTime;
		} else {
			return System.currentTimeMillis();
		}
	}

	public static void safeOpenLink(Context context, Intent linkIntent) {
		try {
			context.startActivity(linkIntent);
		} catch (ActivityNotFoundException e) {
			showToast(context, "Couldn't open link");
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static void setActivatedCompat(View view, boolean activated) {
		if (hasHoneycomb()) {
			view.setActivated(activated);
		}
	}

	public static boolean isGoogleTV(Context context) {
		return context.getPackageManager().hasSystemFeature(
				"com.google.android.tv");
	}

	public static boolean hasFroyo() {
		// Can use static final constants like FROYO, declared in later versions
		// of the OS since they are inlined at compile time. This is guaranteed
		// behavior.
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
	}

	public static boolean hasGingerbread() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
	}

	public static boolean hasHoneycomb() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	public static boolean hasHoneycombMR1() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
	}

	public static boolean hasICS() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
	}

	public static boolean hasJellyBean() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
	}

	public static boolean isTablet(Context context) {
//		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
		 return (context.getResources().getBoolean(R.bool.istablet));
	}

	public static boolean isHoneycombTablet(Context context) {
		return hasHoneycomb() && isTablet(context);
	}

	public static void showToast(Context context, int aligment, String message) {
		if (applicationContext == null && context != null) {
			applicationContext = context.getApplicationContext();
		}
		if (applicationContext == null) {
			return;
		}
		if (mToast == null) {
			LayoutInflater inflater = (LayoutInflater) applicationContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if (inflater == null) {
				return;
			}
			ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.toast, null);
			if (vg == null) {
				return;
			}
			if (mMessage == null) {
				mMessage = (TextView) vg.findViewById(R.id.toast_message);
			}
			mToast = new Toast(applicationContext);
			mToast.setView(vg);
		}
		mToast.setGravity(aligment, 0, 0);
		if (mMessage != null) {
			mMessage.setText(message);
		}
		mToast.show();
	}

	public static void showToast(Context context, String message) {
		showToast(context, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, message);
	}

	public static Typeface getDefaultTypeFace(Context context, int type) {
		if (applicationContext == null && context != null) {
			applicationContext = context.getApplicationContext();
		}
		if (applicationContext != null) {

			switch (type) {
			case Typeface.BOLD:
				if (tfBoldTypeFace == null) {
					tfBoldTypeFace = Typeface.createFromAsset(
							applicationContext.getAssets(),
							"thanhnien_bold.ttf");
				}
				tfDefaultTypeFace = tfBoldTypeFace;
				break;
			case Typeface.ITALIC:
			case Typeface.BOLD_ITALIC:
				if (tfItalicTypeFace == null) {
					tfItalicTypeFace = Typeface.createFromAsset(
							applicationContext.getAssets(),
							"thanhnien_italic.ttf");
				}
				tfDefaultTypeFace = tfItalicTypeFace;
				break;
			case Typeface.NORMAL:
			default:
				if (tfRegularTypeFace == null) {
					tfRegularTypeFace = Typeface.createFromAsset(
							applicationContext.getAssets(),
							"thanhnien_regular.ttf");
				}
				tfDefaultTypeFace = tfRegularTypeFace;
				break;
			}

			return tfDefaultTypeFace;
		}
		return Typeface.DEFAULT;
	}

	public static Drawable buildGadientDrawableFromColor(int topColor,
			int bottomColor) {
		int[] colors = { topColor, bottomColor };
		GradientDrawable result = new GradientDrawable(
				GradientDrawable.Orientation.TOP_BOTTOM, colors);
		return result;
	}

	public static int getDeviceWidth() {
		if (applicationContext != null) {
			return applicationContext.getResources().getDisplayMetrics().widthPixels;
		}
		return 0;
	}

	public static Drawable getDrawableDensityScaled(Drawable drwSource,
			int dstWidth, int dstHeight) {
		if ((drwSource instanceof BitmapDrawable)) {
			BitmapDrawable src = (BitmapDrawable) drwSource;
			Bitmap bmp = Bitmap.createScaledBitmap(src.getBitmap(), dstWidth,
					dstHeight, false);
			if (bmp != null) {
				return new BitmapDrawable(applicationContext.getResources(),
						bmp);
			}
		}
		return drwSource;
	}

	/**
	 * @return
	 */
	public static boolean isLandscape(Context context) {
		return ((context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) ? true
				: false);
	}

	/**
	 * Function to convert milliseconds time to Timer Format
	 * Hours:Minutes:Seconds
	 * */
	public static String milliSecondsToTimer(long milliseconds) {
		String finalTimerString = "";
		String secondsString = "";

		// Convert total duration into time
		int hours = (int) (milliseconds / (1000 * 60 * 60));
		int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
		int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
		// Add hours if there
		if (hours > 0) {
			finalTimerString = hours + ":";
		}

		// Prepending 0 to seconds if it is one digit
		if (seconds < 10) {
			secondsString = "0" + seconds;
		} else {
			secondsString = "" + seconds;
		}

		finalTimerString = finalTimerString + minutes + ":" + secondsString;

		// return timer string
		return finalTimerString;
	}

	/**
	 * Function to get Progress percentage
	 * 
	 * @param currentDuration
	 * @param totalDuration
	 * */
	public static int getProgressPercentage(long currentDuration,
			long totalDuration) {
		Double percentage = (double) 0;

		long currentSeconds = (int) (currentDuration / 1000);
		long totalSeconds = (int) (totalDuration / 1000);

		// calculating percentage
		percentage = (((double) currentSeconds) / totalSeconds) * 100;

		// return percentage
		return percentage.intValue();
	}

	/**
	 * Function to change progress to timer
	 * 
	 * @param progress
	 *            -
	 * @param totalDuration
	 *            returns current duration in milliseconds
	 * */
	public static int progressToTimer(int progress, int totalDuration) {
		int currentDuration = 0;
		totalDuration = (int) (totalDuration / 1000);
		currentDuration = (int) ((((double) progress) / 100) * totalDuration);

		// return current duration in milliseconds
		return currentDuration * 1000;
	}
}
