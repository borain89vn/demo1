/**
 * 
 */
package com.gkxim.android.thanhniennews.social;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Base64;
import android.util.Log;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionDefaultAudience;
import com.facebook.SessionLoginBehavior;
import com.facebook.SessionState;
import com.facebook.internal.SessionAuthorizationType;
import com.facebook.internal.SessionTracker;
import com.facebook.internal.Utility;
import com.facebook.model.GraphUser;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.StoryDetailFragmentActivity;
import com.gkxim.android.thanhniennews.layout.StoryDetailFragment;
import com.gkxim.android.thanhniennews.models.UserAccount;
import com.gkxim.android.thanhniennews.networking.RequestDataFactory;
import com.gkxim.android.thanhniennews.spring.StoryDetailGalleryActivity;
import com.gkxim.android.thanhniennews.spring.StoryDetailSpringFragmentActivity;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.GKIMLog;
import com.gkxim.android.utils.UIUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * @author Timon
 * 
 */
@SuppressWarnings("deprecation")
public class FacebookShare extends SocialShare {
	private static final String TAG = FacebookShare.class.getSimpleName();
	public static final String HTTPMETHOD_GET = "GET";
	private static int REQUEST_TIMEOUT = 30000;
	public String mUserId;

	public String mUserName;
	public String mSecretKey;
	protected String applicationId;
	private SessionTracker sessionTracker;
	private GraphUser userFb = null;
	private Session userInfoSession = null; // the Session used to fetch the
	private Fragment parentFragment;
	private UserInfoChangedCallback userInfoChangedCallback;
	private LoginButtonProperties properties = new LoginButtonProperties();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gkxim.android.thanhniennews.social.ISocialShare#getId()
	 */
	@Override
	public int getId() {
		return 1;
	}

	static class LoginButtonProperties {
		private SessionDefaultAudience defaultAudience = SessionDefaultAudience.FRIENDS;
		private List<String> permissions = Collections.<String> emptyList();
		private SessionAuthorizationType authorizationType = null;
		private OnErrorListener onErrorListener;
		private SessionLoginBehavior loginBehavior = SessionLoginBehavior.SSO_WITH_FALLBACK;
		private Session.StatusCallback sessionStatusCallback;

		public void setOnErrorListener(OnErrorListener onErrorListener) {
			this.onErrorListener = onErrorListener;
		}

		public OnErrorListener getOnErrorListener() {
			return onErrorListener;
		}

		public void setDefaultAudience(SessionDefaultAudience defaultAudience) {
			this.defaultAudience = defaultAudience;
		}

		public SessionDefaultAudience getDefaultAudience() {
			return defaultAudience;
		}

		public void setReadPermissions(List<String> permissions, Session session) {
			if (SessionAuthorizationType.PUBLISH.equals(authorizationType)) {
				throw new UnsupportedOperationException(
						"Cannot call setReadPermissions after setPublishPermissions has been called.");
			}
			if (validatePermissions(permissions, SessionAuthorizationType.READ,
					session)) {
				this.permissions = permissions;
				authorizationType = SessionAuthorizationType.READ;
			}
		}

		public void setPublishPermissions(List<String> permissions,
				Session session) {
			if (SessionAuthorizationType.READ.equals(authorizationType)) {
				throw new UnsupportedOperationException(
						"Cannot call setPublishPermissions after setReadPermissions has been called.");
			}
			if (validatePermissions(permissions,
					SessionAuthorizationType.PUBLISH, session)) {
				this.permissions = permissions;
				authorizationType = SessionAuthorizationType.PUBLISH;
			}
		}

		private boolean validatePermissions(List<String> permissions,
				SessionAuthorizationType authType, Session currentSession) {
			if (SessionAuthorizationType.PUBLISH.equals(authType)) {
				if (Utility.isNullOrEmpty(permissions)) {
					throw new IllegalArgumentException(
							"Permissions for publish actions cannot be null or empty.");
				}
			}
			if (currentSession != null && currentSession.isOpened()) {
				if (!Utility.isSubset(permissions,
						currentSession.getPermissions())) {
					Log.e(TAG,
							"Cannot set additional permissions when session is already open.");
					return false;
				}
			}
			return true;
		}

		List<String> getPermissions() {
			return permissions;
		}

		public void clearPermissions() {
			permissions = null;
			authorizationType = null;
		}

		public void setLoginBehavior(SessionLoginBehavior loginBehavior) {
			this.loginBehavior = loginBehavior;
		}

		public SessionLoginBehavior getLoginBehavior() {
			return loginBehavior;
		}

		public void setSessionStatusCallback(Session.StatusCallback callback) {
			this.sessionStatusCallback = callback;
		}

		public Session.StatusCallback getSessionStatusCallback() {
			return sessionStatusCallback;
		}
	}

	/**
	 * Specifies a callback interface that will be called when the button's
	 * notion of the current user changes (if the fetch_user_info attribute is
	 * true for this control).
	 */
	public interface UserInfoChangedCallback {
		/**
		 * Called when the current user changes.
		 * 
		 * @param user
		 *            the current user, or null if there is no user
		 */
		void onUserInfoFetched(GraphUser user);
	}

	/**
	 * Callback interface that will be called when a network or other error is
	 * encountered while logging in.
	 */
	public interface OnErrorListener {
		/**
		 * Called when a network or other error is encountered.
		 * 
		 * @param error
		 *            a FacebookException representing the error that was
		 *            encountered.
		 */
		void onError(FacebookException error);
	}

	/**
	 * Sets an OnErrorListener for this instance of LoginButton to call into
	 * when certain exceptions occur.
	 * 
	 * @param onErrorListener
	 *            The listener object to set
	 */
	public void setOnErrorListener(OnErrorListener onErrorListener) {
		properties.setOnErrorListener(onErrorListener);
	}

	/**
	 * Returns the current OnErrorListener for this instance of LoginButton.
	 * 
	 * @return The OnErrorListener
	 */
	public OnErrorListener getOnErrorListener() {
		return properties.getOnErrorListener();
	}

	/**
	 * Sets the default audience to use when the session is opened. This value
	 * is only useful when specifying write permissions for the native login
	 * dialog.
	 * 
	 * @param defaultAudience
	 *            the default audience value to use
	 */
	public void setDefaultAudience(SessionDefaultAudience defaultAudience) {
		properties.setDefaultAudience(defaultAudience);
	}

	/**
	 * Gets the default audience to use when the session is opened. This value
	 * is only useful when specifying write permissions for the native login
	 * dialog.
	 * 
	 * @return the default audience value to use
	 */
	public SessionDefaultAudience getDefaultAudience() {
		return properties.getDefaultAudience();
	}

	/**
	 * Set the permissions to use when the session is opened. The permissions
	 * here can only be read permissions. If any publish permissions are
	 * included, the login attempt by the user will fail. The LoginButton can
	 * only be associated with either read permissions or publish permissions,
	 * but not both. Calling both setReadPermissions and setPublishPermissions
	 * on the same instance of LoginButton will result in an exception being
	 * thrown unless clearPermissions is called in between.
	 * <p/>
	 * This method is only meaningful if called before the session is open. If
	 * this is called after the session is opened, and the list of permissions
	 * passed in is not a subset of the permissions granted during the
	 * authorization, it will log an error.
	 * <p/>
	 * Since the session can be automatically opened when the LoginButton is
	 * constructed, it's important to always pass in a consistent set of
	 * permissions to this method, or manage the setting of permissions outside
	 * of the LoginButton class altogether (by managing the session explicitly).
	 * 
	 * @param permissions
	 *            the read permissions to use
	 * 
	 * @throws UnsupportedOperationException
	 *             if setPublishPermissions has been called
	 */
	public void setReadPermissions(List<String> permissions) {
		properties.setReadPermissions(permissions, sessionTracker.getSession());
	}

	/**
	 * Set the permissions to use when the session is opened. The permissions
	 * here should only be publish permissions. If any read permissions are
	 * included, the login attempt by the user may fail. The LoginButton can
	 * only be associated with either read permissions or publish permissions,
	 * but not both. Calling both setReadPermissions and setPublishPermissions
	 * on the same instance of LoginButton will result in an exception being
	 * thrown unless clearPermissions is called in between.
	 * <p/>
	 * This method is only meaningful if called before the session is open. If
	 * this is called after the session is opened, and the list of permissions
	 * passed in is not a subset of the permissions granted during the
	 * authorization, it will log an error.
	 * <p/>
	 * Since the session can be automatically opened when the LoginButton is
	 * constructed, it's important to always pass in a consistent set of
	 * permissions to this method, or manage the setting of permissions outside
	 * of the LoginButton class altogether (by managing the session explicitly).
	 * 
	 * @param permissions
	 *            the read permissions to use
	 * 
	 * @throws UnsupportedOperationException
	 *             if setReadPermissions has been called
	 * @throws IllegalArgumentException
	 *             if permissions is null or empty
	 */
	public void setPublishPermissions(List<String> permissions) {
		properties.setPublishPermissions(permissions,
				sessionTracker.getSession());
	}

	/**
	 * Clears the permissions currently associated with this LoginButton.
	 */
	public void clearPermissions() {
		properties.clearPermissions();
	}

	/**
	 * Sets the login behavior for the session that will be opened. If null is
	 * specified, the default ({@link SessionLoginBehavior
	 * SessionLoginBehavior.SSO_WITH_FALLBACK} will be used.
	 * 
	 * @param loginBehavior
	 *            The {@link SessionLoginBehavior SessionLoginBehavior} that
	 *            specifies what behaviors should be attempted during
	 *            authorization.
	 */
	public void setLoginBehavior(SessionLoginBehavior loginBehavior) {
		properties.setLoginBehavior(loginBehavior);
	}

	/**
	 * Gets the login behavior for the session that will be opened. If null is
	 * returned, the default ({@link SessionLoginBehavior
	 * SessionLoginBehavior.SSO_WITH_FALLBACK} will be used.
	 * 
	 * @return loginBehavior The {@link SessionLoginBehavior
	 *         SessionLoginBehavior} that specifies what behaviors should be
	 *         attempted during authorization.
	 */
	public SessionLoginBehavior getLoginBehavior() {
		return properties.getLoginBehavior();
	}

	/**
	 * Set the application ID to be used to open the session.
	 * 
	 * @param applicationId
	 *            the application ID to use
	 */
	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	/**
	 * Gets the callback interface that will be called when the current user
	 * changes.
	 * 
	 * @return the callback interface
	 */
	public UserInfoChangedCallback getUserInfoChangedCallback() {
		return userInfoChangedCallback;
	}

	/**
	 * Sets the callback interface that will be called when the current user
	 * changes.
	 * 
	 * @param userInfoChangedCallback
	 *            the callback interface
	 */
	public void setUserInfoChangedCallback(
			UserInfoChangedCallback userInfoChangedCallback) {
		this.userInfoChangedCallback = userInfoChangedCallback;
	}

	/**
	 * Sets the callback interface that will be called whenever the status of
	 * the Session associated with this LoginButton changes. Note that updates
	 * will only be sent to the callback while the LoginButton is actually
	 * attached to a window.
	 * 
	 * @param callback
	 *            the callback interface
	 */
	public void setSessionStatusCallback(Session.StatusCallback callback) {
		properties.setSessionStatusCallback(callback);
	}

	/**
	 * Sets the callback interface that will be called whenever the status of
	 * the Session associated with this LoginButton changes.
	 * 
	 * @return the callback interface
	 */
	public Session.StatusCallback getSessionStatusCallback() {
		return properties.getSessionStatusCallback();
	}

	/**
	 * Provides an implementation for {@link Activity#onActivityResult
	 * onActivityResult} that updates the Session based on information returned
	 * during the authorization flow. The Activity containing this view should
	 * forward the resulting onActivityResult call here to update the Session
	 * state based on the contents of the resultCode and data.
	 * 
	 * @param requestCode
	 *            The requestCode parameter from the forwarded call. When this
	 *            onActivityResult occurs as part of Facebook authorization
	 *            flow, this value is the activityCode passed to open or
	 *            authorize.
	 * @param resultCode
	 *            An int containing the resultCode parameter from the forwarded
	 *            call.
	 * @param data
	 *            The Intent passed as the data parameter from the forwarded
	 *            call.
	 * @return A boolean indicating whether the requestCode matched a pending
	 *         authorization request for this Session.
	 * @see Session#onActivityResult(Activity, int, int, Intent)
	 */
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		GKIMLog.log(TAG + "=>onActivityResult: requestCode" + resultCode
				+ " resultCode:" + resultCode + "  data:" + data);
		Session session = sessionTracker.getSession();
		if (session != null) {
			return session.onActivityResult(mActivityContext, requestCode,
					resultCode, data);
		} else {
			return false;
		}
	}

	/**
	 * Set the Session object to use instead of the active Session. Since a
	 * Session cannot be reused, if the user logs out from this Session, and
	 * tries to log in again, a new Active Session will be used instead.
	 * <p/>
	 * If the passed in session is currently opened, this method will also
	 * attempt to load some user information for display (if needed).
	 * 
	 * @param newSession
	 *            the Session object to use
	 * @throws FacebookException
	 *             if errors occur during the loading of user information
	 */
	public void setSession(Session newSession) {
		sessionTracker.setSession(newSession);
		fetchUserInfo();
		// setButtonText();
	}

	public void setFragment(Fragment fragment) {
		parentFragment = fragment;
	}

	List<String> getPermissions() {
		return properties.getPermissions();
	}

	void setProperties(LoginButtonProperties properties) {
		this.properties = properties;
	}

	private boolean initializeActiveSessionWithCachedToken(Context context) {
		if (context == null) {
			return false;
		}

		Session session = Session.getActiveSession();
		if (session != null) {
			return session.isOpened();
		}

		String applicationId = Utility.getMetadataApplicationId(context);
		if (applicationId == null) {
			return false;
		}

		return Session.openActiveSessionFromCache(context) != null;
	}

	private void fetchUserInfo() {
		final Session currentSession = sessionTracker.getOpenSession();
		GKIMLog.lf(null, 0, TAG + "=>fetchUserInfo:" + currentSession);
		if (currentSession != null) {
			if (currentSession != userInfoSession) {
				Request request = Request.newMeRequest(currentSession,
						new Request.GraphUserCallback() {
							@Override
							public void onCompleted(GraphUser me,
									Response response) {
								GKIMLog.lf(null, 0, TAG
										+ "=>onCompleted GraphUser:" + me
										+ "  Response:" + response);
								if (currentSession == sessionTracker
										.getOpenSession()) {
									userFb = me;

									GKIMLog.lf(null, 0, TAG
											+ "=>fetchUserInfo userFb:"
											+ userFb);

									if (userInfoChangedCallback != null) {
										userInfoChangedCallback
												.onUserInfoFetched(userFb);
									}
								}
								if (response.getError() != null) {
									handleError(response.getError()
											.getException());
								}
							}

						});
				Request.executeBatchAsync(request);
				userInfoSession = currentSession;
			}
		} else {
			userFb = null;
			if (userInfoChangedCallback != null) {
				userInfoChangedCallback.onUserInfoFetched(userFb);
			}
		}

	}

	private class LoginButtonCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			fetchUserInfo();
			// setButtonText();
			if (exception != null) {
				handleError(exception);
			}

			if (properties.sessionStatusCallback != null) {
				properties.sessionStatusCallback
						.call(session, state, exception);
			}
		}
	};

	void handleError(Exception exception) {
		if (properties.onErrorListener != null) {
			if (exception instanceof FacebookException) {
				properties.onErrorListener
						.onError((FacebookException) exception);
			} else {
				properties.onErrorListener.onError(new FacebookException(
						exception));
			}
		}
	}

	@Override
	public void initialize(Context context) {
		GKIMLog.lf(context, 1, TAG + "=>initialize from Context");
		initializeActiveSessionWithCachedToken(mApplicationContext);
		sessionTracker = new SessionTracker(mApplicationContext,
				new LoginButtonCallback(), null, false);
		fetchUserInfo();
	}

	@Override
	public void initialize(Activity activity) {
		super.initialize(activity);
		GKIMLog.lf(activity, 1, TAG + "=>initialize from Activity");
		initialize((Context) activity);
	}

	public boolean isReadyForShare() {
		final Session openSession = sessionTracker.getOpenSession();
		if (openSession != null) {
			return true;
		}
		return false;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gkxim.android.thanhniennews.social.ISocialShare#login()
	 */
	@Override
	public void login() {
		mLoginApp = false;
		GKIMLog.lf(mApplicationContext, 1, TAG + "=>login");
		final Session openSession = sessionTracker.getOpenSession();
		if (openSession == null) {
			Session currentSession = sessionTracker.getSession();
			setPublishPermissions(Arrays.asList("publish_actions", "email"));
			setLoginBehavior(SessionLoginBehavior.SUPPRESS_SSO);
			if (currentSession == null || currentSession.getState().isClosed()) {
				sessionTracker.setSession(null);
				Session session = new Session.Builder(mActivityContext)
						.setApplicationId(applicationId).build();
				Session.setActiveSession(session);
				currentSession = session;
			}
			if (!currentSession.isOpened()) {
				Session.OpenRequest openRequest = null;
				if (parentFragment != null) {
					openRequest = new Session.OpenRequest(parentFragment);
				} else if (mActivityContext instanceof Activity) {
					openRequest = new Session.OpenRequest(
							(Activity) mActivityContext);
				}

				if (openRequest != null) {
					openRequest.setDefaultAudience(properties.defaultAudience);
					openRequest.setPermissions(properties.permissions);
					openRequest.setLoginBehavior(properties.loginBehavior);

					if (SessionAuthorizationType.PUBLISH
							.equals(properties.authorizationType)) {
						currentSession.openForPublish(openRequest);
					} else {
						currentSession.openForRead(openRequest);
					}
				}
			}
		}
	}

	@Override
	public void logout() {
		GKIMLog.log("Nam.nguyen facebook logout");
		if (sessionTracker != null) {
			final Session openSession = sessionTracker.getOpenSession();
			if (openSession != null) {
				GKIMLog.l(1, "Nam.nguyen facebook logout has Session");
				openSession.closeAndClearTokenInformation();

			}
			sessionTracker = null;
			Session.setActiveSession(null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gkxim.android.thanhniennews.social.ISocialShare#post()
	 */
	@Override
	public void post(String[] data) {
		GKIMLog.lf(mApplicationContext, 1, TAG + " =>post");
		Bundle params = new Bundle();

		params.putString("name", data[1]);
		params.putString("description", mActivityContext.getResources()
				.getString(R.string.social_post_description_full));

		if (!(data[3] + "").equals("") && !(data[3] + "").equals("null")) {
			params.putString("link", data[3]);
		} else {
			params.putString(
					"link",
					mActivityContext.getResources().getString(
							R.string.social_post_link_url));
		}
		if (!(data[2] + "").equals("") && !(data[2] + "").equals("null")) {
			params.putString("picture", data[2]);
		} else {
			params.putString(
					"picture",
					"https://lh4.ggpht.com/PgPzFj-e_qtiTNsPfaCME6m_ZiyXBYh6YnWPlaAxO4-n1G_TtjhsJRJaxVKMU7RTbZw=w124");
		}

		publishFeedDialog(params);
	}

	private void publishFeedDialog(Bundle params) {
		WebDialog feedDialog = (new WebDialog.FeedDialogBuilder(
				mActivityContext, Session.getActiveSession(), params))
				.setOnCompleteListener(new OnCompleteListener() {
					@Override
					public void onComplete(Bundle values,
							FacebookException error) {
						if (error == null) {
							// When the story is posted, echo the success
							// and the post Id.
							final String postId = values.getString("post_id");
							if (postId != null) {
								UIUtils.showToast(
										mApplicationContext,
										mActivityContext
												.getResources()
												.getString(
														R.string.social_post_succeed));
								if (getmCompletedMessage() != null) {
									setCompletedMessage(null);
								}
							} else {
								UIUtils.showToast(
										mApplicationContext,
										mActivityContext
												.getResources()
												.getString(
														R.string.social_post_failed));
							}
						} else if (error instanceof FacebookOperationCanceledException) {
							// User clicked the "x" button
							GKIMLog.lf(null, 0, TAG
									+ "=>UpdateStatusListener.onCancel");
						} else {
							GKIMLog.lf(null, 0, TAG
									+ "=>UpdateStatusListener.onFacebookError");
						}

					}

				}).build();
		feedDialog.show();
	}

	public void likeAnUrl(final String url, final String pageid) {
		Session session = sessionTracker.getOpenSession();

		if (session != null) {

			Request.Callback callback = new Request.Callback() {
				public void onCompleted(Response response) {
					Log.i(TAG, "=>likeAnUrl, Result: " + response.toString());
					if (response != null) {

						JSONObject graphResponse = null;
						JSONArray jsonarray = null;
						String id = "";
						if (response.getGraphObject() != null) {
							graphResponse = response.getGraphObject()
									.getInnerJSONObject();

							try {
								jsonarray = graphResponse.getJSONArray("data");
								if (jsonarray.length() > 0) {
									JSONObject jsonobj = jsonarray
											.getJSONObject(0);
									id = jsonobj.getString("id");
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
						if (!id.equals("")) {
							unlikeStory(url, id, pageid);
						} else {
							likeStory(url, pageid);
						}
					}

				}
			};
			Bundle postParams = new Bundle();
			postParams.putString("object", url);
			postParams.putBoolean("fb:explicitly_shared", true);
			Request request = new Request(session, "me/og.likes", postParams,
					HttpMethod.GET, callback);
			Log.d("Share", "request" + request.toString());
			RequestAsyncTask task = new RequestAsyncTask(request);
			task.execute();
		}
	}

	public void checklikeAnUrl(final String url, final String pageid) {
		Session session = sessionTracker.getOpenSession();

		if (session != null) {

			Request.Callback callback = new Request.Callback() {
				public void onCompleted(Response response) {
					Log.i(TAG,
							"=>checklikeAnUrl, Result: " + response.toString());
					if (response != null) {

						JSONObject graphResponse = null;
						JSONArray jsonarray = null;
						String id = "";
						if (response.getGraphObject() != null) {
							graphResponse = response.getGraphObject()
									.getInnerJSONObject();

							try {
								jsonarray = graphResponse.getJSONArray("data");
								if (jsonarray.length() > 0) {
									JSONObject jsonobj = jsonarray
											.getJSONObject(0);
									id = jsonobj.getString("id");
									Log.e("ID", "Id: " + id);
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
						GKIMLog.lf(mActivityContext, 0, TAG
								+ "=>check like page: " + pageid + ", id: "
								+ id + ", on url: " + url);
						boolean liked = false;
						if (!id.equals("")) {
							liked = true;
						}
						if (mActivityContext instanceof StoryDetailFragmentActivity) {

							mActivityContext
									.getSharedPreferences(pageid,
											Context.MODE_PRIVATE).edit()
									.putBoolean(pageid, liked).commit();
							FragmentManager fm = ((StoryDetailFragmentActivity) mActivityContext)
									.getSupportFragmentManager();
							StoryDetailFragment frag = (StoryDetailFragment) fm
									.findFragmentByTag(getFragmentTag(((StoryDetailFragmentActivity) mActivityContext)
											.getmPager().getCurrentItem()));
							if (frag != null
									&& pageid.equals(frag.getStoryId())) {
								frag.updateGui();
							}
						} else if (mActivityContext instanceof StoryDetailSpringFragmentActivity) {
							mActivityContext
									.getSharedPreferences(
											TNPreferenceManager.SHAREDPREF_SPRING,
											Context.MODE_PRIVATE)
									.edit()
									.putBoolean(
											TNPreferenceManager.EXTRAKEY_SPRING_WISH_LIKE,
											liked).commit();
							((StoryDetailSpringFragmentActivity) mActivityContext)
									.updateGui();
						} else if (mActivityContext instanceof StoryDetailGalleryActivity) {
							mActivityContext
									.getSharedPreferences(
											TNPreferenceManager.SHAREDPREF_SPRING,
											Context.MODE_PRIVATE)
									.edit()
									.putBoolean(
											TNPreferenceManager.EXTRAKEY_SPRING_YOURTET_LIKE,
											liked).commit();
							((StoryDetailGalleryActivity) mActivityContext)
									.updateGui();
						}
					}
				}
			};
			Bundle postParams = new Bundle();
			postParams.putString("object", url);

			Request request = new Request(session, "me/og.likes", postParams,
					HttpMethod.GET, callback);
			Log.d("Share", "request" + request.toString());
			RequestAsyncTask task = new RequestAsyncTask(request);
			task.execute();
		}
	}

	public void likeStory(final String url, final String pageid) {
		GKIMLog.l(4, "FB's count for page: " + pageid);
		Session session = sessionTracker.getOpenSession();
		if (session != null) {

			Request.Callback callback = new Request.Callback() {
				public void onCompleted(Response response) {
					Log.i(TAG, "=>likeStory, Result: " + response.toString());
					String id = "";
					if (response != null) {
						try {
							JSONObject graphResponse = null;

							if (response.getGraphObject() != null) {
								graphResponse = response.getGraphObject()
										.getInnerJSONObject();
								id = graphResponse.getString("id");
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}

						GKIMLog.lf(mActivityContext, 0, TAG + "=>Like page: "
								+ pageid + ", id: " + id + ", on url: " + url);
						boolean liked = false;
						if (!id.equals("")) {
							liked = true;
						}

						if (mActivityContext instanceof StoryDetailFragmentActivity) {
							mActivityContext
									.getSharedPreferences(pageid,
											Context.MODE_PRIVATE).edit()
									.putBoolean(pageid, liked).commit();
							FragmentManager fm = ((FragmentActivity) mActivityContext)
									.getSupportFragmentManager();
							StoryDetailFragment frag = (StoryDetailFragment) fm
									.findFragmentByTag(getFragmentTag(((StoryDetailFragmentActivity) mActivityContext)
											.getmPager().getCurrentItem()));
							if (frag != null
									&& pageid.equals(frag.getStoryId())) {
								frag.updateGui();
							}
						} else if (mActivityContext instanceof StoryDetailSpringFragmentActivity) {
							mActivityContext
									.getSharedPreferences(
											TNPreferenceManager.SHAREDPREF_SPRING,
											Context.MODE_PRIVATE)
									.edit()
									.putBoolean(
											TNPreferenceManager.EXTRAKEY_SPRING_WISH_LIKE,
											liked).commit();
							((StoryDetailSpringFragmentActivity) mActivityContext)
									.updateGui();
						} else if (mActivityContext instanceof StoryDetailGalleryActivity) {
							mActivityContext
									.getSharedPreferences(
											TNPreferenceManager.SHAREDPREF_SPRING,
											Context.MODE_PRIVATE)
									.edit()
									.putBoolean(
											TNPreferenceManager.EXTRAKEY_SPRING_YOURTET_LIKE,
											liked).commit();
							((StoryDetailGalleryActivity) mActivityContext)
									.updateGui();
						}
						new AsynDownloadTask().execute(new String[] {
								RequestDataFactory.makecount(url).toString(),
								pageid });
					}
				}
			};
			Bundle postParams = new Bundle();
			postParams.putString("object", url);
			postParams.putBoolean("fb:explicitly_shared", true);

			Request request = new Request(session, "me/og.likes", postParams,
					HttpMethod.POST, callback);
			Log.d("Share", "request" + request.toString());
			RequestAsyncTask task = new RequestAsyncTask(request);
			task.execute();

		}
	}

	public void unlikeStory(final String url, final String idlike,
			final String pageid) {
		Session session = sessionTracker.getOpenSession();
		GKIMLog.l(4, TAG + "=>unlikeStory, FB's count for page : " + pageid);
		if (session != null) {

			Request.Callback callback = new Request.Callback() {
				public void onCompleted(Response response) {
					Log.i(TAG, "=>unlikeStory, Result: " + response.toString());
					if (response != null) {

						FacebookRequestError error = response.getError();
						if (error != null) {
							//
						} else {
							FragmentManager fm = ((FragmentActivity) mActivityContext)
									.getSupportFragmentManager();
							StoryDetailFragment frag = null;
							if (mActivityContext instanceof StoryDetailFragmentActivity) {
								mActivityContext
										.getSharedPreferences(pageid,
												Context.MODE_PRIVATE).edit()
										.putBoolean(pageid, false).commit();
								frag = (StoryDetailFragment) fm
										.findFragmentByTag(getFragmentTag(((StoryDetailFragmentActivity) mActivityContext)
												.getmPager().getCurrentItem()));
								if (frag != null
										&& pageid.equals(frag.getStoryId())) {
									frag.updateGui();
								}
							} else if (mActivityContext instanceof StoryDetailSpringFragmentActivity) {
								mActivityContext
										.getSharedPreferences(
												TNPreferenceManager.SHAREDPREF_SPRING,
												Context.MODE_PRIVATE)
										.edit()
										.putBoolean(
												TNPreferenceManager.EXTRAKEY_SPRING_WISH_LIKE,
												false).commit();
								((StoryDetailSpringFragmentActivity) mActivityContext)
										.updateGui();
							} else if (mActivityContext instanceof StoryDetailGalleryActivity) {
								mActivityContext
										.getSharedPreferences(
												TNPreferenceManager.SHAREDPREF_SPRING,
												Context.MODE_PRIVATE)
										.edit()
										.putBoolean(
												TNPreferenceManager.EXTRAKEY_SPRING_YOURTET_LIKE,
												false).commit();
								((StoryDetailGalleryActivity) mActivityContext)
										.updateGui();
							}

							new AsynDownloadTask().execute(new String[] {
									RequestDataFactory.makecount(url)
											.toString(), pageid });
						}

					}
				}
			};
			Request request = new Request(session, idlike, null,
					HttpMethod.DELETE, callback);
			Log.d("Share", "request" + request.toString());
			RequestAsyncTask task = new RequestAsyncTask(request);
			task.execute();

		}
	}

	private void getFacebookHashKey() {
		PackageInfo info;
		try {
			info = mActivityContext.getPackageManager().getPackageInfo(
					mActivityContext.getPackageName(),
					PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md;
				md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				String something = new String(Base64.encode(md.digest(), 0));
				GKIMLog.lf(null, 0, TAG + "=> hash key: " + something);
			}
		} catch (NameNotFoundException e1) {
		} catch (NoSuchAlgorithmException e) {
		} catch (Exception e) {
		}
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
		GKIMLog.log(TAG + "=>handlingActivityForResult :" + requestCode
				+ " resultCode:" + resultCode);
		GKIMLog.l(4, "requestCode: " + requestCode);
		if (mActivityContext != null) {
			Session session = Session.getActiveSession();
			session.onActivityResult(mActivityContext, requestCode, resultCode,
					data);
		}
		switch (requestCode) {
			case ACTIVITY_REQUESTCODE_LOGIN:
				// mFbAgent.authorizeCallback(requestCode, resultCode, data);
				return true;
			case ACTIVITY_REQUESTCODE_POST:
				return true;
			default:
				break;
		}

		if (mLoginApp) {
			getUserInfo();
		}

		return super.handlingActivityForResult(requestCode, resultCode, data);
	}

	private String getFragmentTag(int pos) {
		return ("android:switcher:" + R.id.pager + ":" + pos);
	}

	public class AsynDownloadTask extends AsyncTask<String, Integer, String> {
		private static final String TAG = "AsynDownloadTask";

		// private static final int POST_COMPLETED_DELAY = 100;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected String doInBackground(String... params) {

			try {
				String result = getJSONResponseFromURL(URLEncode(params[0]));
				GKIMLog.lf(mActivityContext, 1, "=> like count result : " + result + "in "
						+ mActivityContext.getClass().getSimpleName());
				if (result != null && !result.equals("")) {
					JsonParser jp = new JsonParser();
					JsonElement je = jp.parse(result);
					int likecount = -1;
					if (je != null && je.isJsonArray()) {
						JsonArray ja = je.getAsJsonArray();
						int length = ja.size();
						if (length > 0) {
							JsonElement jse = ja.get(0);
							likecount = jse.getAsJsonObject().get("like_count")
									.getAsInt();
							// mActivityContext
							// .getSharedPreferences(params[1] + "_",
							// Context.MODE_PRIVATE).edit()
							// .putInt(params[1] + "_", likecount)
							// .commit();

						}
					} else if (je != null && je.isJsonObject()) {
						likecount = je.getAsJsonObject().get("like_count")
								.getAsInt();
						// mActivityContext
						// .getSharedPreferences(params[1] + "_",
						// Context.MODE_PRIVATE).edit()
						// .putInt(params[1] + "_", likecount).commit();
					}

					if (likecount != -1) {
						if (mActivityContext instanceof StoryDetailFragmentActivity) {
							mActivityContext
									.getSharedPreferences(params[1] + "_",
											Context.MODE_PRIVATE).edit()
									.putInt(params[1] + "_", likecount)
									.commit();
						} else if (mActivityContext instanceof StoryDetailSpringFragmentActivity) {
							mActivityContext
									.getSharedPreferences(
											TNPreferenceManager.SHAREDPREF_SPRING,
											Context.MODE_PRIVATE)
									.edit()
									.putInt(TNPreferenceManager.EXTRAKEY_SPRING_WISH_LIKECOUNT,
											likecount).commit();
						} else if (mActivityContext instanceof StoryDetailGalleryActivity) {
							mActivityContext
									.getSharedPreferences(
											TNPreferenceManager.SHAREDPREF_SPRING,
											Context.MODE_PRIVATE)
									.edit()
									.putInt(TNPreferenceManager.EXTRAKEY_SPRING_YOURTET_LIKECOUNT,
											likecount).commit();
						}
					}
				}

			} catch (Exception e) {
				GKIMLog.lf(null, 4, TAG + "=> Exception: " + e.getMessage());
			}
			return "";
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			FragmentManager fm = ((FragmentActivity) mActivityContext)
					.getSupportFragmentManager();
			if (mActivityContext instanceof StoryDetailFragmentActivity) {
				StoryDetailFragment frag = (StoryDetailFragment) fm
						.findFragmentByTag(getFragmentTag(((StoryDetailFragmentActivity) mActivityContext)
								.getmPager().getCurrentItem()));
				if (frag != null) {
					frag.updateCountGui();
				}
			} else if (mActivityContext instanceof StoryDetailSpringFragmentActivity) {
				StoryDetailSpringFragmentActivity ac = (StoryDetailSpringFragmentActivity) mActivityContext;
				ac.updateFBLikeCount();
				ac.updateGui();
			} else if (mActivityContext instanceof StoryDetailGalleryActivity) {
				StoryDetailGalleryActivity ac = (StoryDetailGalleryActivity) mActivityContext;
				ac.updateFBLikeCount();
				ac.updateGui();
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

	}

	public String getJSONResponseFromURL(String url) {
		// Checking has cache
		String data = null;
		HttpParams httpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		// The default value is zero, that means the timeout is not used.
		// int timeoutConnection = 3000;
		// HttpConnectionParams.setConnectionTimeout(httpParameters,
		// timeoutConnection);
		// HttpConnectionParams.setSoTimeout(httpParameters, timeoutConnection);

		HttpClient httpclient = new DefaultHttpClient(httpParameters);
		// Prepare a request object
		HttpGet httpget = new HttpGet(url);
		// Execute the request
		HttpResponse response;
		try {
			response = httpclient.execute(httpget);
			// Examine the response status

			// Get hold of the response entity
			HttpEntity entity = response.getEntity();

			// If the response does not enclose an entity, there is no need
			// to worry about connection release
			if (entity != null) {
				// A Simple JSON Response Read
				InputStream instream = entity.getContent();
				data = convertStreamToString(instream);
				// now you have the string representation of the HTML request
				instream.close();
				GKIMLog.l(4, "message " + data);
			}

		} catch (Exception e) {
			GKIMLog.l(4, "ERRRO Have cache string : " + e.getMessage());

			return "";
		}
		return data;
	}

	private static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	public String URLEncode(String s) {
		StringBuffer sbuf = new StringBuffer();
		int ch;
		for (int i = 0; i < s.length(); i++) {
			ch = s.charAt(i);
			switch (ch) {
				case ' ': {
					sbuf.append("%20");
					break;
				}
				case '!': {
					sbuf.append("%21");
					break;
				}
				case '*': {
					sbuf.append("%2A");
					break;
				}
				// case '\'': { sbuf.append("%27"); break;}
				case '(': {
					sbuf.append("%28");
					break;
				}
				case ')': {
					sbuf.append("%29");
					break;
				}
				case ';': {
					sbuf.append("%3B");
					break;
				}
				// case ':': { sbuf.append("%3A"); break;}
				case '@': {
					sbuf.append("%40");
					break;
				}
				// case '&': { sbuf.append("%26"); break;}
				// case '=': { sbuf.append("%3D"); break;}
				case '+': {
					sbuf.append("%2B");
					break;
				}
				case '$': {
					sbuf.append("%24");
					break;
				}
				case ',': {
					sbuf.append("%2C");
					break;
				}
				// case '/': { sbuf.append("%2F"); break;}
				// case '?': { sbuf.append("%3F"); break;}
				// case '%': { sbuf.append("%25"); break;}
				case '#': {
					sbuf.append("%23");
					break;
				}
				case '[': {
					sbuf.append("%5B");
					break;
				}
				case ']': {
					sbuf.append("%5D");
					break;
				}
				case '"': {
					sbuf.append("%22");
					break;
				}

				default:
					sbuf.append((char) ch);
			}
		}
		return sbuf.toString();
	}

	@Override
	public void loginApp() {
		mLoginApp = true;
		GKIMLog.log(TAG + "=> loginApp");
		Session openSession = sessionTracker.getOpenSession();
		GKIMLog.lf(null, 0, TAG + "=>loginApp:" + openSession);
		if (openSession == null) {
			Session currentSession = sessionTracker.getSession();
			setPublishPermissions(Arrays.asList("publish_actions",
					"offline_access", "email", "user_birthday",
					"user_interests"));
			setLoginBehavior(SessionLoginBehavior.SUPPRESS_SSO);
			if (currentSession == null || currentSession.getState().isClosed()) {
				sessionTracker.setSession(null);
				Session session = new Session.Builder(mActivityContext)
						.setApplicationId(applicationId).build();
				Session.setActiveSession(session);
				currentSession = session;
			}
			if (!currentSession.isOpened()) {
				Session.OpenRequest openRequest = null;
				if (parentFragment != null) {
					openRequest = new Session.OpenRequest(parentFragment);
				} else if (mActivityContext instanceof Activity) {
					openRequest = new Session.OpenRequest(
							(Activity) mActivityContext);
				}

				if (openRequest != null) {
					openRequest.setDefaultAudience(properties.defaultAudience);
					openRequest.setPermissions(properties.permissions);
					openRequest.setLoginBehavior(properties.loginBehavior);

					if (SessionAuthorizationType.PUBLISH
							.equals(properties.authorizationType)) {
						currentSession.openForPublish(openRequest);
					} else {
						currentSession.openForRead(openRequest);
					}
				}
			}
		}
	}

	private void getUserInfo() {
		final Session currentSession = sessionTracker.getOpenSession();
		GKIMLog.lf(null, 0, TAG + "=>getUserInfo:" + currentSession);
		if (currentSession != null) {
			if (currentSession != userInfoSession) {
				Request request = Request.newMeRequest(currentSession,
						new Request.GraphUserCallback() {
							@Override
							public void onCompleted(GraphUser me,
									Response response) {
								GKIMLog.lf(null, 0, TAG
										+ "=>onCompleted GraphUser:" + me
										+ "  Response:" + response);
								if (currentSession == sessionTracker
										.getOpenSession()) {
									userFb = me;

									if (userFb != null) {
										GKIMLog.l(
												1,
												TAG + "birthday:"
														+ userFb.getBirthday());
										UserAccount user = new UserAccount(
												userFb.getId(), userFb
														.getEmail(), userFb
														.getFirstName(), userFb
														.getLastName(), "");
										String json = me
												.toString()
												.replace(
														"GraphObject{graphObjectClass=GraphUser, state=",
														"");
										json = json.substring(0,
												json.length() - 1);
										GKIMLog.lf(null, 0, TAG
												+ "=>onCompleted json info "
												+ json);
										user.setJsonInfo(json);
										// FIXME: Send server to comfirm state
										// login
										// GUIAccountDialog.setUserFacebook(user);
										Message msg = getmCompletedMessage();
										if (msg != null) {
											msg.obj = (Object) user;
											msg.sendToTarget();
										}
										mLoginApp = false;
									}

									if (userInfoChangedCallback != null) {
										userInfoChangedCallback
												.onUserInfoFetched(userFb);
									}
								}
								if (response.getError() != null) {
									handleError(response.getError()
											.getException());
								}
							}

						});
				Request.executeBatchAsync(request);
				userInfoSession = currentSession;
			}
		} else {
			userFb = null;
			if (userInfoChangedCallback != null) {
				userInfoChangedCallback.onUserInfoFetched(userFb);
			}
		}

	}
}
