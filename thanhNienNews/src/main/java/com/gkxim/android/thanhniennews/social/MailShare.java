/**
 * 
 */
package com.gkxim.android.thanhniennews.social;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;

import com.gkim.thanhniennews.R;
import com.gkxim.android.utils.GKIMLog;

/**
 * @author Timon
 * 
 */
public class MailShare extends SocialShare {
	private static final String TAG = MailShare.class.getSimpleName();
	// private static final String HTML_MAIL_FORMAT =
	// "<table><tbody><tr><td align=\"center\"><table><tbody><tr><td><img src=\"%1s\"> </td><td align=\"top\"><h3 style=\"color:blue;\">%2s</h3><br><a href=\"%3s\">%4s</a>.</td><td></td></tr></tbody></table></td></tr></tbody></table>";
	private static final String HTML_MAIL_FORMAT = "<table><tbody><tr><td align=\"center\"><table><tbody><tr><td align=\"top\"><h3 style=\"color:blue;\">%1s</h3><br><a href=\"%2s\">%3s</a><br><a href=\"%4s\">%5s</a></td><td></td></tr></tbody></table></td></tr></tbody></table>";

	@Override
	public int getId() {
		return 0;
	}

	@Override
	public void initialize(Context context) {
		GKIMLog.lf(context, 1, TAG + "=>initialize from Context");
		setInitialized();
	}

	@Override
	public void initialize(Activity activity) {
		super.initialize(activity);
		GKIMLog.lf(activity, 1, TAG + "=>initialize from Activity");
		initialize((Context) activity);
	}

	@Override
	public boolean isReadyForShare() {
		// Note: always return true since we will use platform's email provider
		// activity.
		return true;
	}

	@Override
	public void post(String[] data) {
		String description = mActivityContext.getResources().getString(
				R.string.social_post_description_full);
		String url = "";
		if (!(data[3] + "").equals("") && !(data[3] + "").equals("null")) {
			url = data[3];
		}
		String linkApp = mActivityContext.getResources().getString(
				R.string.social_post_link_url)
				+ "&referrer=utm_source%3DEmail%26utm_medium%3Dlink";
		Intent mailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
				"mailto", "", null));
		mailIntent.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		// mailIntent.putExtra(Intent.EXTRA_SUBJECT, description);
		mailIntent.putExtra(Intent.EXTRA_SUBJECT, data[1]);

		String str = "<a href='" + url + "'>" + data[1] + "</a> "
				+ "<p><a href='" + data[2] + "'>" + data[2] + "</a> </p>"
				+ "<p><a href='" + linkApp + "'>" + description + "</a> </p>";
		mailIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(str));
		// mailIntent.putExtra(Intent.EXTRA_TEXT,
		// Html.fromHtml(String.format(HTML_MAIL_FORMAT, data[2], data[1], url,
		// description)));
		// mailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(data[2]));
		mActivityContext.startActivityForResult(Intent.createChooser(
				mailIntent,
				mActivityContext.getResources().getString(
						R.string.social_post_select_mailapp)),
				ACTIVITY_REQUESTCODE_POST);
	}

	@Override
	public boolean handlingActivityForResult(int requestCode, int resultCode,
			Intent data) {
		GKIMLog.lf(null, 1, TAG + "=>handlingActivityForResult :" + requestCode);

		return super.handlingActivityForResult(requestCode, resultCode, data);
	}

}
