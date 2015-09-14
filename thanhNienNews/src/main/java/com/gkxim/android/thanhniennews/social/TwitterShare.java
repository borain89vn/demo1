/**
 * 
 */
package com.gkxim.android.thanhniennews.social;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.gkim.thanhniennews.R;
import com.gkxim.android.utils.GKIMLog;
import com.gkxim.android.utils.UIUtils;

/**
 * @author Timon
 * 
 */
public class TwitterShare extends SocialShare {
	private static final String TAG = TwitterShare.class.getSimpleName();
	public static final String TWITTER_SCHEMA_CALL_BACK = "http://tnmcms.dev2.gkxim.com/twitter/callback";
	private Twitter mTwAgent;
	private AccessToken mAccessToken = null;
	private Dialog mWebViewDialog;
	public String mVerifier;
	public RequestToken mRequestToken;
	protected Handler mHandler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				String verifier = (String) msg.obj;
				TwitterAsynTask oauthTask = new TwitterAsynTask();
				oauthTask.execute("oauth_verifier:" + verifier);
				break;
			default:
				break;
			}
			return false;
		}
	});
	private ProgressDialog mSpinner;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gkxim.android.thanhniennews.social.ISocialShare#getId()
	 */
	@Override
	public int getId() {
		return 2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gkxim.android.thanhniennews.social.ISocialShare#login()
	 */
	@Override
	public void login() {
		GKIMLog.lf(mApplicationContext, 1, TAG + "=>login");
		(new TwitterAsynTask()).execute("access_token");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gkxim.android.thanhniennews.social.ISocialShare#post()
	 */
	@Override
	public void post(String[] data) {
		GKIMLog.lf(mApplicationContext, 1, TAG + "=>post");
		// String tweetUrl =
		// "https://twitter.com/intent/tweet?text=PUT TEXT HERE &url="
		// + "https://www.google.com";
		// Uri uri = Uri.parse(tweetUrl);
		// mActivityContext.startActivity(new Intent(Intent.ACTION_VIEW, uri));
		TwitterAsynTask twtask = new TwitterAsynTask();
		String[] postCommand = new String[data.length + 1];
		postCommand[0] = "post";
		System.arraycopy(data, 0, postCommand, 1, data.length);
		twtask.execute(postCommand);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gkxim.android.thanhniennews.social.ISocialShare#getAccessToken()
	 */
	@Override
	public String getAccessToken() {
		GKIMLog.lf(mApplicationContext, 1, TAG + "=>getAccessToken");
		return mVerifier;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gkxim.android.thanhniennews.social.ISocialShare#initialize(android
	 * .content.Context)
	 */
	@Override
	public void initialize(Context context) {
		GKIMLog.lf(mApplicationContext, 1, TAG + "=>initialize");
		String twAPPId = context.getResources().getString(
				R.string.api_key_social_twitter_key);
		String twAPPSec = context.getResources().getString(
				R.string.api_key_social_twitter_serect);
		mTwAgent = (new TwitterFactory()).getInstance();
		mTwAgent.setOAuthConsumer(twAPPId, twAPPSec);

	}

	@Override
	public void initialize(Activity activity) {
		super.initialize(activity);
		GKIMLog.lf(activity, 1, TAG + "=>initialize from Activity");
		initialize((Context) activity);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gkxim.android.thanhniennews.social.ISocialShare#isReadyForShare()
	 */
	@Override
	public boolean isReadyForShare() {
		GKIMLog.lf(mApplicationContext, 1, TAG + "=>isReadyForShare");
		if (mAccessToken != null && mAccessToken.getUserId() > 0) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gkxim.android.thanhniennews.social.ISocialShare#handlingActivityForResult
	 * (int, int, android.content.Intent)
	 */
	@Override
	public boolean handlingActivityForResult(int requestCode, int resultCode,
			Intent data) {
		GKIMLog.lf(mApplicationContext, 1, TAG + "=>handlingActivityForResult");
		if (data != null) {
			if (requestCode == ACTIVITY_REQUESTCODE_LOGIN) {
				String oauthVerifier = data.getData().getQueryParameter(
						"oauth_verifier");
				(new TwitterAsynTask()).execute("oauth_verifier:"
						+ oauthVerifier);
			}
			return true;
		}
		return super.handlingActivityForResult(requestCode, resultCode, data);
	}

	private class TwitterAsynTask extends AsyncTask<String, Void, Object> {

		@Override
		protected Object doInBackground(String... params) {
			String obj1 = params[0];
			GKIMLog.lf(null, 0, TAG + "=>doInBackground : " + obj1);
			if (obj1 == null)
				return null;

			if (obj1.equals("access_token")) {
				try {
					mRequestToken = mTwAgent
							.getOAuthRequestToken(TWITTER_SCHEMA_CALL_BACK);
					if (mRequestToken != null) {
						showWebViewDialog(mRequestToken.getAuthorizationURL());
						GKIMLog.lf(null, 0, TAG + "=> authorized URL: "
								+ mRequestToken.getAuthorizationURL() + ", "
								+ mRequestToken.getToken());
					}
				} catch (TwitterException e) {
					GKIMLog.lf(null, 5, "=>TwitterException:" + e.getMessage());
				}
			} else if (obj1.startsWith("oauth_verifier:") && obj1.length() > 15) {
				try {
					String verifier = obj1.substring(15);
					mAccessToken = mTwAgent.getOAuthAccessToken(mRequestToken,
							verifier);
					if (mAccessToken != null) {
						GKIMLog.lf(null, 0, TAG + "=>oauth_verifier: "
								+ mAccessToken.getToken());

					}
					mTwAgent.setOAuthAccessToken(mAccessToken);
					setInitialized();
					postCompletedMessage();
				} catch (TwitterException e) {
					GKIMLog.lf(null, 5, "=>TwitterException:" + e.getMessage());
				}
			} else if (obj1.equals("post") && params.length >= 4) {
				String title = params[2];
				String description = mActivityContext.getResources().getString(
						R.string.social_post_description_full);
				String link = "";
				if (!(params[4] + "").equals("")
						&& !(params[4] + "").equals("null")) {
					link = params[4];
				}

				String pic = params[3];
				boolean postSucceed = false;
				URL urlObj = null;
				try {
					// FIXME: bug crash DUY
					mTwAgent.setOAuthAccessToken(mAccessToken);

					StatusUpdate stat = new StatusUpdate(title + " \r\n "
							+ link + " \r\n " + description);
					urlObj = new URL(pic);
					stat.setMedia(pic, urlObj.openStream());
					// FIXME: bug crash here
					mTwAgent.updateStatus(stat);
					GKIMLog.lf(null, 0, TAG + "=> post Twitter completed");
					postSucceed = true;
				} catch (MalformedURLException e) {
					GKIMLog.lf(null, 5,
							TAG + "=>MalformedURLException: " + e.getMessage());
				} catch (FileNotFoundException e) {
					GKIMLog.lf(null, 5,
							TAG + "=>File not found: " + e.getMessage());
				} catch (IOException e) {
					GKIMLog.lf(null, 5,
							TAG + "=>IOException: " + e.getMessage());
				} catch (TwitterException e) {
					GKIMLog.lf(null, 5, "=>TwitterException:" + e.getMessage());
				} finally {
					if (postSucceed) {
						UIUtils.showToast(null, mActivityContext.getResources()
								.getString(R.string.social_post_succeed));
					} else {
						UIUtils.showToast(null, mActivityContext.getResources()
								.getString(R.string.social_post_failed));
					}
					urlObj = null;
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			GKIMLog.lf(null, 0, TAG + "=>onPostExecute");
			if (result != null) {
			} else if (mAccessToken != null) {
				GKIMLog.lf(
						null,
						0,
						TAG + "=> login succeed: "
								+ mAccessToken.getScreenName());
				mActivityContext.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (mWebViewDialog != null
								&& mWebViewDialog.isShowing()) {
							mWebViewDialog.dismiss();
						}
					}
				});
			}
			super.onPostExecute(result);
		}

	}

	/**
	 * @param authorizationURL
	 */
	public void showWebViewDialog(final String authorizationURL) {
		if (authorizationURL != null && authorizationURL.contains("http")) {
			mActivityContext.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mWebViewDialog = new Dialog(mActivityContext);
					mWebViewDialog.getWindow().setLayout(
							WindowManager.LayoutParams.MATCH_PARENT,
							WindowManager.LayoutParams.MATCH_PARENT);
					mWebViewDialog
							.requestWindowFeature(Window.FEATURE_NO_TITLE);
					mWebViewDialog.setCancelable(true);
					mWebViewDialog
							.setOnCancelListener(new DialogInterface.OnCancelListener() {

								@Override
								public void onCancel(DialogInterface dialog) {
									GKIMLog.lf(null, 0, TAG + "=>onCancelled.");
								}
							});
					LinearLayout ll = new LinearLayout(mActivityContext);
					WebView wv = new WebView(mActivityContext);
					wv.setScrollbarFadingEnabled(false);
					wv.setHorizontalScrollBarEnabled(false);
					wv.getSettings().setJavaScriptEnabled(true);
					wv.clearCache(true);
					wv.setWebViewClient(new CustomWebViewClient());

					ll.addView(wv);
					mWebViewDialog.setContentView(ll);
					wv.loadUrl(authorizationURL);
					// mWebViewDialog.show();
				}
			});
		}
	}

	private class CustomWebViewClient extends WebViewClient {
		private static final String mTAG = "CustomWebViewClient";

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			GKIMLog.lf(null, 1, mTAG + "=>shouldOverrideUrlLoading: " + url);
			Uri uri = Uri.parse(url);
			String verifier = uri.getQueryParameter("oauth_verifier");
			if (null != verifier) {
				GKIMLog.lf(null, 0, mTAG + "=>oauth_verifier: " + verifier);
				view.stopLoading();
				mActivityContext.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						showSpinnerDialog(false);
						if (mWebViewDialog != null) {
							mWebViewDialog.dismiss();
						}
					}
				});

				mVerifier = verifier;
				Message msgLogin = new Message();
				msgLogin.what = 1;
				msgLogin.obj = mVerifier;
				mHandler.sendMessageDelayed(msgLogin, 500);
				return false;
			}
			view.loadUrl(url);
			return true;
		}

		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			GKIMLog.lf(null, 1, mTAG + "=>pagestarted: " + url);
			Uri uri = Uri.parse(url);
			String verifier = uri.getQueryParameter("oauth_verifier");
			if (null != verifier) {
				GKIMLog.lf(null, 0, mTAG + "=>oauth_verifier: " + verifier);
				view.stopLoading();
				mActivityContext.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						showSpinnerDialog(false);
						if (mWebViewDialog != null) {
							mWebViewDialog.dismiss();
						}
					}
				});

				mVerifier = verifier;
				Message msgLogin = new Message();
				msgLogin.what = 1;
				msgLogin.obj = mVerifier;
				mHandler.sendMessageDelayed(msgLogin, 500);
				return;
			}
			showSpinnerDialog(true);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			GKIMLog.lf(null, 1, mTAG + "=>onPageFinished: " + url);
			showSpinnerDialog(false);
			if (mWebViewDialog != null) {
				mWebViewDialog.show();
			}
		}
	}

	protected void showSpinnerDialog(boolean isShow) {
		if (mSpinner == null) {
			mSpinner = new ProgressDialog(mActivityContext);
			mSpinner.setTitle(mActivityContext.getResources().getString(
					R.string.please_wait));
		}
		try {
			if (isShow && !mSpinner.isShowing()) {
				mSpinner.show();
			} else {
				if (mSpinner != null && mSpinner.isShowing()) {
					mSpinner.dismiss();
				}
			}
		} catch (Exception e) {

		}
	}
}
