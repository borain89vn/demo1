/*
 * SplashActivity.java
 * @author Timon Trinh
 * 1 Download (update) sections information in queue.
 * 2 Download issue content in queue.
 * 3 Handle issue process is completed.
 * 4 Parse, add images URL in download queue and display Home page.
 */
package com.gkxim.android.thanhniennews;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.location.LocationHelper;
import com.gkxim.android.thanhniennews.models.GenericResponse;
import com.gkxim.android.thanhniennews.models.StoryDetail;
import com.gkxim.android.thanhniennews.models.TNTemplate;
import com.gkxim.android.thanhniennews.networking.RequestDataFactory;
import com.gkxim.android.thanhniennews.networking.XTifyController;
import com.gkxim.android.thanhniennews.service.CatchLowMemoryService;
import com.gkxim.android.thanhniennews.tracking.Tracking;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.DataDownloader;
import com.gkxim.android.utils.DataDownloader.OnDownloadCompletedListener;
import com.gkxim.android.utils.GKIMLog;
import com.gkxim.android.utils.RequestData;
import com.gkxim.android.utils.UIUtils;
import com.gkxim.android.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * @author Timon Trinh SplashActivity of ThanhNienNews
 * 
 */
public class SplashActivity extends FragmentActivity {

	private static final boolean DEBUG = GKIMLog.DEBUG_ON;
	protected static final String TAG = "SplashActivity";
	protected static final int GO_GPS_SETTINGS = 7;
	protected static final String EXTRA_SECTION_BOXES = "extra_section_boxes";
	public static final String EXTRA_SECTION_DEFINITIONS = "extra_section_definitions";
	private Intent mSectionIntent = null;
	protected boolean bCompleted = false;
	private static boolean mHasPNS = false;
	public static boolean isAppIndexing = false;
	private boolean mCancelLoadServerDate = false;
	private String appIndexingStoryId = "";
	private DataDownloader mTNDownloader;

	private String mDataDownloading;
	private boolean forceStop;
	public static LocationHelper mLocationHelper;
	public static Location mLocation = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SplashActivity.isAppIndexing = false;
		if(getIntent().getBooleanExtra(ThanhNienNewsApp.KEY_CATCH_LOW_MEMORY,false)){
			this.finish();
			stopService();


		}else {
			final Intent intent1 = getIntent();
			final Uri data = intent1.getData();
			GKIMLog.lf(this, 0, TAG + "=>onCreate");
			startService();
			if (data != null) {
				String path = data.getPath();
				GKIMLog.lf(this, 0, TAG + " data= " + path);
				// final Intent intent = new Intent(Intent.ACTION_VIEW);
				// intent.setData(data);
				// startActivity(intent);
				if (path != null && path.contains("/")) {
					String[] strs = path.split("/");
					if (strs != null && strs.length > 0) {
						String storiId = strs[strs.length - 1];
						GKIMLog.lf(this, 0, TAG + "storiId=" + storiId);
						SplashActivity.isAppIndexing = true;
						appIndexingStoryId = storiId;
					}
				}
				// finish();
				// forceStop = true;
				// return;
			}
			// init error log
			// ErrorReporter errReporter = new ErrorReporter();
			// errReporter.Init(this);
			// errReporter.CheckErrorAndSendMail(this);
			setContentView(R.layout.activity_splash);
			// This screen is not support for landscape
			// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			if (DEBUG) {
				Button btn = (Button) SplashActivity.this
						.findViewById(R.id.btnTest1);
				if (btn != null) {
					btn.setEnabled(false);
				}
			}

			DataDownloader.setHTTPDefaultMethod(DataDownloader.HTTPMETHOD_POST);

			// track download on first launch
			Tracking.trackDownload(this);

			// FIXME: fix pns
			Intent intent = this.getIntent();
			if (intent.hasExtra(TNPreferenceManager.EXTRAKEY_OPEN_STORY_FROM_PNS)) {
				if (mHasPNS) {
					mCancelLoadServerDate = true;
				}
				mHasPNS = intent.getBooleanExtra(
						TNPreferenceManager.EXTRAKEY_OPEN_STORY_FROM_PNS, false);
				String storiId = intent
						.getStringExtra(TNPreferenceManager.EXTRAKEY_STORYID);
				if (mHasPNS && storiId != null) {
					mSectionIntent = new Intent(this, SectionActivity.class);

					mSectionIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					mSectionIntent.putExtra(TNPreferenceManager.EXTRAKEY_STORYID,
							storiId);
					mSectionIntent.putExtra(
							TNPreferenceManager.EXTRAKEY_OPEN_STORY_FROM_PNS, true);
				}
			}
			mDataDownloading = getResources().getString(R.string.data_downloading);
			if (TNPreferenceManager.GPS_GETTINGS) {
				mLocationHelper = new LocationHelper(this);
			}


		}


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	protected void onStart() {
		if (forceStop) {
			super.onStart();
			return;
		}
		showInfoDebug();
		TNPreferenceManager.setContext(this);
		// if (!TNPreferenceManager.getInstallShorcut()) {
		// TNPreferenceManager.installShorcut();
		// TNPreferenceManager.setInstallShorcut(true);
		// }

		// This is a cheat for UIUtils could retrieve the context.
		if (mDataDownloading != null) {
			UIUtils.showToast(this, mDataDownloading);
		}
		if (DEBUG) {
			Button btnTest1 = (Button) findViewById(R.id.btnTest1);
			if (btnTest1 != null) {
				btnTest1.setVisibility(View.VISIBLE);
			}
		}
		// FlurryAgent.onStartSession(this,
		Tracking.startSession(this);

		// FIXME: fix pns
		if (!mHasPNS) {
			XTifyController.start();
			XTifyController.registerOnXtifyListenPNS(
					this.getApplicationContext(), null);
		}
		if (!mCancelLoadServerDate) {
			if (!TNPreferenceManager.isConnectionAvailable()) {
				confirmReadOffline();
			} else {
				if (Utils.checkGPSProviderEnabled(this)) {
					startLocationHelper();
				} else if (TNPreferenceManager.getGpsShow()) {
					if (TNPreferenceManager.GPS_GETTINGS) {
						showNoGPSDialog();
					}
				}
				loadServerData();
			}
		} else {
			bCompleted = true;
			postComplete();
		}
		// FlurryAgent.onEvent(TNPreferenceManager.EVENT_START);
		Tracking.sendEvent(TNPreferenceManager.EVENT_START, null);
		super.onStart();

	}

	private void confirmReadOffline() {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					// Yes button clicked

					if (!TNPreferenceManager.checkHasSavedStory()) {
						noSectionToReadOffline();
					} else {
						loadServerData();
					}
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					System.exit(1);
					finish();
					break;
				default:
					break;
				}
			}
		};
		(new AlertDialog.Builder(this))
				.setTitle(R.string.title_activity_splash)
				.setMessage(R.string.close_application_read_offline)
				.setPositiveButton(R.string.close_application_confirm_yes,
						dialogClickListener)
				.setNegativeButton(R.string.close_application_confirm_no,
						dialogClickListener).create().show();
	}

	private void noSectionToReadOffline() {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					// Yes button clicked
					System.exit(1);
					finish();
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					System.exit(1);
					finish();
					break;
				default:
					break;
				}
			}
		};
		(new AlertDialog.Builder(this))
				.setTitle(R.string.title_activity_splash)
				.setMessage(R.string.close_application_read_offline_no_page)
				.setPositiveButton(R.string.close, dialogClickListener)
				.create().show();
	}

	protected void loadServerData() {
		mTNDownloader = new DataDownloader(new OnDownloadCompletedListener() {
			@Override
			public void onCompleted(Object key, String result) {
				RequestData contentKey = (RequestData) key;
				GKIMLog.lf(SplashActivity.this, 5, TAG
						+ "=> onCompleted, key: " + contentKey.getKeyCacher()
						+ " type:" + contentKey.type);
				if (result == null || result.length() == 0) {
					GKIMLog.l(1, TAG + "NO Internet!!!Get out of App..");
					if (!TNPreferenceManager.isConnectionAvailable()) {
						noSectionToReadOffline();
					}
					return;
				}

				// Cache 1 day for all request from the splash page
				String theUrl = contentKey.getURLString();
				// boolean bCheckCache = TNPreferenceManager.checkCache(theUrl);

				String keyCacher = contentKey.getKeyCacher();
				boolean bCheckCache = false;
				if (keyCacher != null && keyCacher != "") {
					bCheckCache = TNPreferenceManager.checkCache(keyCacher);
				}
				if (!bCheckCache) {
					TNPreferenceManager.addOrUpdateCache(theUrl, result,
							keyCacher);
				}
				boolean bConnectionAvailable = TNPreferenceManager
						.isConnectionAvailable();

				int type = contentKey.type;
				if (mSectionIntent == null) {
					mSectionIntent = new Intent(SplashActivity.this,
							SectionActivity.class);
					// mSectionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					// | Intent.FLAG_ACTIVITY_CLEAR_TASK
					// | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
				}
				if (type == RequestDataFactory.DATA_JSON_DEF_REQUESTTYPE_TEMPLATES) {
					Gson gson = new GsonBuilder().registerTypeAdapter(
							TNTemplate.class,
							new TNTemplate.TNTemplateConverter()).create();
					TNTemplate template = gson.fromJson(result,
							TNTemplate.class);
					mSectionIntent.putExtra(EXTRA_SECTION_DEFINITIONS, result);
					TNPreferenceManager.updateTNTemplate(template);
					bCompleted = false;

					if (TNPreferenceManager.hasMediaFeature()) {
						// if (template != null && template.getSectionCount() >
						// 0) {
						// if (template.getSection(template.getSectionCount() -
						// 1) != null) {
						// TNPreferenceManager.EXTRAVALUE_SECTION_MEDIA =
						// template
						// .getSection(
						// template.getSectionCount() - 1)
						// .getSectionId();
						// }
						// }
						requestSectionSpecialList(TNPreferenceManager
								.getMediaSectionId());
					} else {
						requestDisplaySection(TNPreferenceManager.EXTRAVALUE_SECTION_HOME);
					}
				} else if (type == RequestDataFactory.DATA_JSON_DEF_REQUESTTYPE_MEDIALIST) {
					// /Note: this has have the same JSON format with
					// DATA_JSON_DEF_REQUESTTYPE_TEMPLATES
					Gson gson = new GsonBuilder().registerTypeAdapter(
							TNTemplate.class,
							new TNTemplate.TNTemplateConverter()).create();
					TNTemplate videoMenuList = gson.fromJson(result,
							TNTemplate.class);
					if (videoMenuList != null
							&& videoMenuList.getSectionCount() > 0) {
						if (videoMenuList.getSection(videoMenuList
								.getSectionCount() - 1) != null) {
							TNPreferenceManager.VIDEO_OF_YOU_ID = videoMenuList
									.getSection(
											videoMenuList.getSectionCount() - 1)
									.getSectionId();
						}
					}

					TNPreferenceManager.updateVideoMenuList(videoMenuList);
					bCompleted = false;
					// TODO: request for first Section content, normally the
					// sectionid
					// should be EXTRAVALUE_SECTION_HOME, but can switch to any
					// other section
					if (TNPreferenceManager.SECTION_SPRING) {
						// Set spring page first screen when open app
						GKIMLog.l(
								3,
								"SectionID xuan :"
										+ TNPreferenceManager
												.getXuanSectionId());
						requestDisplaySection(TNPreferenceManager
								.getXuanSectionId());
					} else {
						requestDisplaySection(TNPreferenceManager.EXTRAVALUE_SECTION_HOME);
					}

				} else if (type == RequestDataFactory.DATA_JSON_DEF_REQUESTTYPE_DATA_BOXES
						|| type == RequestDataFactory.DATA_JSON_DEF_REQUESTTYPE_DATA_BOXES_SECTION) {
					// finishSplash and start Section page.
					// XXX: make sure that the request for Section page
					// should be the latest request.
					mSectionIntent.putExtra(EXTRA_SECTION_BOXES, result);
					mTNDownloader.addDownload(bConnectionAvailable,
							RequestDataFactory.makeStoryCommentIconsRequest());
				} else if (type == RequestDataFactory.DATA_JSON_DEF_REQUESTTYPE_TEMPLATES_ICONS) {
					bCompleted = true;
					Gson gson = new GsonBuilder().registerTypeAdapter(
							GenericResponse.class,
							new GenericResponse.GenericResponseConverter())
							.create();
					GenericResponse gres = gson.fromJson(result,
							GenericResponse.class);
					if (gres != null && gres.isHasData()) {
						JsonElement jAE = new JsonParser().parse(gres.getData());
						if (jAE != null && jAE.isJsonArray()) {
							TNPreferenceManager.updateTNSmileyIcons(jAE
									.getAsJsonArray());
						}
					}
				} else if (type == RequestDataFactory.DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_POST_STORY_ATTACHT) {
					GKIMLog.lf(null, 0,
							"DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_POST_STORY_ATTACHT");
				} else if (type == RequestDataFactory.DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_DETAIL) {
					Gson gson = new GsonBuilder().registerTypeAdapter(
							GenericResponse.class,
							new GenericResponse.GenericResponseConverter())
							.create();
					GenericResponse gres = gson.fromJson(result,
							GenericResponse.class);
					if (gres != null && gres.isHasData()) {
						StoryDetail sd = gson.fromJson(gres.getData(),
								StoryDetail.class);
						if (sd != null) {
							String storyId = String.valueOf(sd.getStoryid());
							GKIMLog.lf(null, 0, "App indexing real storyId: "
									+ storyId);
							if (storyId != null && storyId.length() > 0) {
								if (mSectionIntent != null) {
									mSectionIntent
											.putExtra(
													TNPreferenceManager.EXTRAKEY_STORYID,
													storyId);
									mSectionIntent
											.putExtra(
													TNPreferenceManager.EXTRAKEY_OPEN_STORY_FROM_PNS,
													true);
								}

							}
						}
					}
				}

				if (bCompleted) {
					if (SplashActivity.isAppIndexing) {
						requestAppIndexingDetail();
						SplashActivity.isAppIndexing = false;
					} else {
						TNPreferenceManager.postDataCreated();
						if (DEBUG) {
							Button btn = (Button) SplashActivity.this
									.findViewById(R.id.btnTest1);
							if (btn != null && !btn.isEnabled()) {
								btn.setEnabled(true);
							}
						} else {
							postComplete();
						}
					}
				}

				if (bConnectionAvailable) {
					TNPreferenceManager.clearCacheToDay();
				}
			}

			@Override
			public String doInBackgroundDebug(Object... params) {
				return null;
			}

		});

		boolean forceUpdate = true;
		if (!TNPreferenceManager.isConnectionAvailable()) {
			forceUpdate = false;
		}
		mTNDownloader.addDownload(forceUpdate,
				RequestDataFactory.makeSectionListRequest());
	}

	public void onClick(View v) {
		if (v.getId() == R.id.btnTest1) {
			if (mSectionIntent != null) {
				postComplete();
			}
		}
	}

	@Override
	protected void onResume() {
		GKIMLog.lf(this, 0, TAG + "=>onResume");
		if (forceStop) {
			super.onResume();
			return;
		}
		com.facebook.Settings.publishInstallAsync(getApplicationContext(),
				getResources().getString(R.string.api_key_social_facebook_id));
		super.onResume();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onStop()
	 */
	@Override
	protected void onStop() {
		GKIMLog.lf(this, 0, TAG + "=>onStop");
		if (forceStop) {
			super.onStop();
			return;
		}
		// FlurryAgent.onEndSession(this);
		Tracking.endSeesion(this);
		if (mTNDownloader != null) {
			mTNDownloader.setExitTasksEarly(true);
		}
		super.onStop();

	}

	// @Override
	// protected void onActivityResult(int requestCode, int resultCode, Intent
	// data) {
	// super.onActivityResult(requestCode, resultCode, data);
	// GKIMLog.l(2, TAG + "==> onActivityResult");
	// }
	//
	// @Override
	// public void onBackPressed() {
	// super.onBackPressed();
	// GKIMLog.l(2, TAG + "==> onBackPressed");
	// if(goGpsSetting){
	// postComplete();
	// }
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		GKIMLog.lf(this, 0, TAG + "=>onDestroy");
		super.onDestroy();
	}

	private void startLocationHelper() {
		if (mLocationHelper != null) {
			mLocationHelper.checkAndStartLocationClientConnect();
		}
	}

	private void showInfoDebug() {
		if (GKIMLog.DEBUG_ON) {
			Configuration conf = getResources().getConfiguration();
			int iscreenlayout = conf.screenLayout
					& Configuration.SCREENLAYOUT_SIZE_MASK;
			String screenlayout = "";
			if (iscreenlayout == Configuration.SCREENLAYOUT_SIZE_NORMAL) {
				screenlayout = "normal";
			} else if (iscreenlayout == Configuration.SCREENLAYOUT_SIZE_LARGE) {
				screenlayout = "large";
			} else if (iscreenlayout == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
				screenlayout = "xlarge";
			} else {
				screenlayout = "small";
			}
			DisplayMetrics metrics = getResources().getDisplayMetrics();
			String densityQualifier = "";
			if (metrics.densityDpi == DisplayMetrics.DENSITY_LOW) {
				densityQualifier = "ldpi";
			} else if (metrics.densityDpi == DisplayMetrics.DENSITY_MEDIUM) {
				densityQualifier = "mdpi";
			} else if (metrics.densityDpi == DisplayMetrics.DENSITY_HIGH) {
				densityQualifier = "hdpi";
			} else if (metrics.densityDpi == DisplayMetrics.DENSITY_XHIGH) {
				densityQualifier = "xhdpi";
			} else {
				densityQualifier = "default-" + metrics.densityDpi;
			}

			GKIMLog.lf(
					this.getApplicationContext(),
					1,
					TAG
							+ "=> showInfoDebug: "
							+ "[{"
							+ " locale.getDisplayLanguage(): "
							+ conf.locale.getDisplayLanguage()
							+ ", conf.locale.getLanguage(): "
							+ conf.locale.getLanguage()
							+ ", conf.screenLayout: "
							+ screenlayout
							+ ", conf.orientation: "
							+ (conf.orientation == Configuration.ORIENTATION_PORTRAIT ? "portrait"
							: "landscape") + ", conf.fontScale: "
							+ conf.fontScale + ", density: " + metrics.density
							+ ", scaledDensity: " + metrics.scaledDensity
							+ ", densityDpi: " + densityQualifier
							+ ", heightPixels: " + metrics.heightPixels
							+ ", widthPixels: " + metrics.widthPixels
							+ ", xdpi: " + metrics.xdpi + ", ydpi: "
							+ metrics.ydpi

							+ "},  " + conf.toString() + "]");
		}
	}

	private void postComplete() {
		if (bCompleted) {
			if (mDialog != null && mDialog.isShowing()) {

			} else {
				startActivity(mSectionIntent);
				finish();
			}
		}
	}

	public void requestDisplaySection(String sectionId) {
		if (!TextUtils.isEmpty(sectionId)
				&& !TNPreferenceManager.EXTRAVALUE_SECTION_HOME
						.equals(sectionId)) {

			mTNDownloader.addDownload(true, RequestDataFactory
					.makeSectionRequest(TNPreferenceManager.getUserId(), "",
							String.valueOf(UIUtils.getDeviceWidth()), null,
							sectionId, null));

		} else {

			if (!TNPreferenceManager.GPS_GETTINGS) {
				mTNDownloader.addDownload(false, RequestDataFactory
						.makeIssueRequest(TNPreferenceManager.getUserId(), "",
								String.valueOf(UIUtils.getDeviceWidth()), null,
								sectionId));
			} else {

				if (SplashActivity.mLocation != null) {
					mTNDownloader.addDownload(RequestDataFactory
							.makeIssueHomeGPSWithLocationRequest(
									TNPreferenceManager.getUserId(), "",
									String.valueOf(UIUtils.getDeviceWidth()),
									null, sectionId,
									SplashActivity.mLocation.getLatitude(),
									SplashActivity.mLocation.getLongitude()));
				} else {

					mTNDownloader.addDownload(RequestDataFactory
							.makeIssueHomeGPSWithLocationRequest(
									TNPreferenceManager.getUserId(), "",
									String.valueOf(UIUtils.getDeviceWidth()),
									null, sectionId, -1, -1));

				}
			}

		}
	}

	/**
	 * Request for Video's menu list items.
	 * 
	 * @param String
	 *            mediaSectionId
	 */
	protected void requestSectionSpecialList(String mediaSectionId) {
		if (!TextUtils.isEmpty(mediaSectionId)) {
			// /Note: this condition checking for future - other special
			// sections.
			if (mediaSectionId.equalsIgnoreCase(TNPreferenceManager
					.getMediaSectionId())) {
				mTNDownloader.addDownload(true,
						RequestDataFactory.makeSectionMediaListRequest());
			}
		} else {
			requestDisplaySection(TNPreferenceManager.EXTRAVALUE_SECTION_HOME);
		}
	}

	protected void requestAppIndexingDetail() {
		mTNDownloader.addDownload(
				TNPreferenceManager.isConnectionAvailable(),
				RequestDataFactory.makeStoryAppIndexRequest(
						TNPreferenceManager.getUserId(), appIndexingStoryId));
	}

	private boolean goGpsSetting = false;

	private void showNoGPSDialog() {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					dialog.dismiss();
					break;

				case DialogInterface.BUTTON_NEUTRAL:
					TNPreferenceManager.setGpsShow(false);
					dialog.dismiss();
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					goGpsSetting = true;
					gotoGPSProvider(SplashActivity.this);
					break;
				default:
					break;
				}
			}
		};
		mDialog = (new AlertDialog.Builder(this))
				.setTitle(R.string.title_activity_splash)
				.setMessage(R.string.txt_no_gps_on)
				.setPositiveButton(R.string.no_gps_btn3, dialogClickListener)
				.setNeutralButton(R.string.no_gps_btn2, dialogClickListener)
				.setNegativeButton(R.string.no_gps_btn1, dialogClickListener)
				.create();

		mDialog.setCanceledOnTouchOutside(false);
		mDialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				GKIMLog.l(2, TAG + "==> onDismiss gps dialog");
				if (!goGpsSetting) {
					postComplete();
				}
			}
		});
		mDialog.show();
	}

	private Dialog mDialog = null;

	public void gotoGPSProvider(Context mContext) {
		Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivityForResult(intent, GO_GPS_SETTINGS);
	}
	public void startService() {
		if (!ThanhNienNewsApp.preferences.getBoolean(ThanhNienNewsApp.KEY_START_SERVICE, false)) {
			startService(new Intent(getBaseContext(), CatchLowMemoryService.class));
		}

	}
	public void stopService() {
		stopService(new Intent(getBaseContext(), CatchLowMemoryService.class));
	}
}
