/**
 * File: UserNewStoryActivity.java
 * Creator: Timon.Trinh (timon@gkxim.com)
 * Date: 18-12-2012
 * 
 */
package com.gkxim.android.thanhniennews;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.layout.EggingListSectionColor;
import com.gkxim.android.thanhniennews.layout.GUIAccountDialog;
import com.gkxim.android.thanhniennews.layout.GUIPhotoOptionDialog;
import com.gkxim.android.thanhniennews.layout.GUISimpleLoadingDialog;
import com.gkxim.android.thanhniennews.models.GenericResponse;
import com.gkxim.android.thanhniennews.models.UserAccount;
import com.gkxim.android.thanhniennews.networking.RequestDataFactory;
import com.gkxim.android.thanhniennews.tracking.Tracking;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.*;
import com.gkxim.android.utils.DataDownloader.OnDownloadCompletedListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Timon Trinh
 */
public class UserNewStoryActivity extends FragmentActivity {

	private static final String TAG = "UserNewStoryActivity";
	protected static final int CAPTURE_PICTURE_INTENT = 201;

	private Typeface mDefaultTF;
	private String fileNameImgCapture = "";
	private int mMinTitleLength = 8;
	private int mNumberPhoto = 0;
	private int mMaxContentLength = 500;
	private long mCurrentFilesSize;
	private EditText mETContent = null;
	private EditText mETTitle = null;
	private ImageView mImvSend = null;
	private AlertDialog mProcessingDialog;
	private FileDialog mFileDialog = null;
	private ArrayList<File> mSelectedFiles = null;
	private GUIPhotoOptionDialog mGUIPhotoOptionDialog = null;
	private Uri mCapturedImageURI;
	private TextView mPhotoCounter;
	private GUIAccountDialog mDialog;

	private OnDismissListener mPhotoDialogDismissListener = new OnDismissListener() {

		@Override
		public void onDismiss(DialogInterface dialog) {
			int selected = ((GUIPhotoOptionDialog) dialog).getSelectedOption();
			GKIMLog.lf(null, 0, TAG + "=>onDismiss selected: " + selected);
			if (selected == 1) {

				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				File tempFile = null;
				String storageState = Environment.getExternalStorageState();
				try {
					String path = "";
					if (storageState.equals(Environment.MEDIA_MOUNTED)) {
						path = Environment.getExternalStorageDirectory()
								.getAbsolutePath()
								+ File.separatorChar
								+ "Android/data/"
								+ UserNewStoryActivity.this.getPackageName()
								+ "/files/"
								+ System.currentTimeMillis()
								/ 1000
								+ ".jpg";
					} else {
						path = File.createTempFile("tncapture_", ".jpg")
								.getAbsolutePath();
					}
					GKIMLog.lf(null, 1, TAG + "=>File path for capture: "
							+ path);
					tempFile = new File(path);
					if (!tempFile.exists()) {
						tempFile.getParentFile().mkdirs();
						tempFile.createNewFile();
					}
					fileNameImgCapture = tempFile.getAbsolutePath();
				} catch (IOException e) {
					GKIMLog.lf(UserNewStoryActivity.this, 4, TAG
							+ "=>onDismiss IOException:" + e.getMessage());
				}

				Uri uri = Uri.fromFile(tempFile);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
				startActivityForResult(intent, CAPTURE_PICTURE_INTENT);
			} else if (selected == 2) {
				File fileDir = null;
				String folderPath = "/mnt/sdcard";
				// UIUtils.showToast(this, "Coming.../add voice");
				mFileDialog.setCurrentFilesSize(mCurrentFilesSize);
				mFileDialog.setFilterType(0);
				fileDir = Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
				if (fileDir != null) {
					folderPath = fileDir.getAbsolutePath();
				}
				mFileDialog.setStartPath(folderPath);
				if (!mFileDialog.isVisible()) {
					mFileDialog.show(getSupportFragmentManager(),
							"fragment_file_dialog");
				}
			}
		}
	};

	@SuppressWarnings("unused")
	private LoaderCallbacks<Cursor> mLoaderCallback = new LoaderCallbacks<Cursor>() {

		@Override
		public void onLoaderReset(Loader<Cursor> cursorloader) {
			GKIMLog.lf(null, 0, TAG + "=>onLoaderReset");
		}

		@Override
		public void onLoadFinished(Loader<Cursor> cursorloader, Cursor cursor) {
			GKIMLog.lf(null, 0, TAG + "=>onLoadFinished");
			String capturedImageFilePath = "";
			int columnIndex = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			capturedImageFilePath = cursor.getString(columnIndex);
			File filetoadd = new File(capturedImageFilePath);
			if (mSelectedFiles == null) {
				mSelectedFiles = new ArrayList<File>();
			}
			mSelectedFiles.add(filetoadd);
			updateSelectedFiles(1, mSelectedFiles);
		}

		@Override
		public Loader<Cursor> onCreateLoader(int uriid, Bundle bundle) {
			GKIMLog.lf(null, 0, TAG + "=>onCreateLoader");
			if (uriid == 1) {
				return new CursorLoader(UserNewStoryActivity.this,
						mCapturedImageURI,
						new String[] { MediaStore.Images.Media.DATA }, null,
						null, null);
			}
			return null;
		}
	};

	private OnDownloadCompletedListener mPostingDataListener = new OnDownloadCompletedListener() {

		@Override
		public void onCompleted(Object key, String result) {
			RequestData contentKey = (RequestData) key;
			GKIMLog.lf(UserNewStoryActivity.this, 0, TAG
					+ "=> onCompleted, key: " + contentKey.getURLString());
			mProcessingDialog.dismiss();
			if (result == null || result.length() == 0) {
				GKIMLog.lf(null, 0, "NO Internet!!..");
				return;
			}
			int type = contentKey.type;
			if (type == RequestDataFactory.DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_POST_STORY) {
				try {
					Gson gson = new GsonBuilder().registerTypeAdapter(
							GenericResponse.class,
							new GenericResponse.GenericResponseConverter())
							.create();
					GenericResponse gres = gson.fromJson(result,
							GenericResponse.class);
					if (gres != null && gres.isSucceed()) {
						UIUtils.showToast(
								null,
								getResources().getString(
										R.string.thank_post_story));
					} else {
						UIUtils.showToast(null, gres.resultMsg);
						// mImvSend.setEnabled(true);
					}
				} catch (Exception e) {
					GKIMLog.lf(null, 4,
							TAG + "=>parse failed: " + e.getMessage());
					UIUtils.showToast(
							null,
							getResources().getString(
									R.string.dlg_poststory_failed));
				}
				(new Handler()).postDelayed(new Runnable() {
					@Override
					public void run() {
						UserNewStoryActivity.this.finish();
					}
				}, UIUtils.TOAST_LONG);
			}
		}

		@Override
		public String doInBackgroundDebug(Object... params) {
			return null;
		}
	};

	private DialogInterface.OnDismissListener mFileDialogDismissListener = new DialogInterface.OnDismissListener() {

		@Override
		public void onDismiss(DialogInterface dialog) {
			GKIMLog.lf(null, 0, TAG + "=>onDismiss dialog: "
					+ dialog.getClass().getSimpleName());
			updateSelectedFiles();
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_newstory);
		TNPreferenceManager.setContext(this);
		mDefaultTF = TNPreferenceManager.getTNTypeface();
		boolean bCannotRotate = getResources().getBoolean(R.bool.portrait_only);
		if (bCannotRotate) {
			GKIMLog.lf(this, 0, TAG + "=>Not support for rotation");
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		TextView tv = (TextView) findViewById(R.id.tv_newstory_title);
		tv.setTypeface(mDefaultTF);

		mPhotoCounter = (TextView) findViewById(R.id.tv_newstory_size_photo);
		mETTitle = (EditText) findViewById(R.id.et_newstory_storytitle);
		mETContent = (EditText) findViewById(R.id.et_newstory_storycontent);
		mImvSend = (ImageView) findViewById(R.id.imv_newstory_send);

		mETTitle.setTypeface(mDefaultTF);
		mETContent.setTypeface(mDefaultTF);

		mMinTitleLength = TNPreferenceManager.getMinTitleLengthForNewStory();
		mMaxContentLength = TNPreferenceManager
				.getMaxContentLengthForNewStory();
		mProcessingDialog = new GUISimpleLoadingDialog(this);
		mFileDialog = new FileDialog(this);
		mFileDialog.setMaxSize(TNPreferenceManager.getPostFilesMaxSize());
		mFileDialog.setMaxFiles(TNPreferenceManager.getPostMaxFiles());
		mFileDialog.setOnDismissListener(mFileDialogDismissListener);
		mCurrentFilesSize = 0;

		Intent openIntent = getIntent();
		String action = openIntent.getAction();
		if (action != null && action.equalsIgnoreCase(Intent.ACTION_SEND)) {
			GKIMLog.lf(this, 0, TAG + "=>open to share an media.");
			Bundle extras = openIntent.getExtras();
			if (extras.containsKey(Intent.EXTRA_STREAM)) {
				try {
					GKIMLog.lf(this, 0, TAG + "=>with Stream extra");
					// Get resource path from intent callee
					Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);

					String[] proj = { MediaStore.Images.Media.DATA };
					Cursor cursor = managedQuery(uri, proj, // Which columns to
															// return
							null, // WHERE clause; which rows to return (all
									// rows)
							null, // WHERE clause selection arguments (none)
							null); // Order-by clause (ascending by name)
					int columnIndex = cursor
							.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
					cursor.moveToFirst();
					String path = cursor.getString(columnIndex);

					mFileDialog.getFileFromShare(path);
					mFileDialog.setFilterType(0);
					updateSelectedFiles();
				} catch (Exception e) {
					GKIMLog.lf(this, 0, TAG + "=>exception: " + e.toString());
				}

			} else if (extras.containsKey(Intent.EXTRA_TEXT)) {
				GKIMLog.lf(this, 0, TAG + "=>with Text extra");
			}
		}

		//NOTE: Comment out after Tet 2014
		boolean mTabletVersion = UIUtils.isTablet(this);
//		Log.d("initGUIHeader", "mTabletVersion:" + mTabletVersion);
        if(TNPreferenceManager.SECTION_SPRING) {
            if (!mTabletVersion) {
                ImageView imgv = (ImageView) findViewById(R.id.imgv_horse_phone);
                imgv.setVisibility(View.VISIBLE);
            } else {
                ImageView imgv1 = (ImageView) findViewById(R.id.imgv_horse_tablet);
                imgv1.setVisibility(View.VISIBLE);
            }
        }
		GKIMLog.lf(this, 0, TAG + "=>story will be send from section: "
				+ TNPreferenceManager.getCurrentStandingSectionId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
        Tracking.startSession(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		GKIMLog.lf(this, 0, TAG + "=> back pressed");
		super.onBackPressed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#finish()
	 */
	@Override
	public void finish() {
		Tracking.endSeesion(this);
		super.finish();
		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onAttachedToWindow()
	 */
	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		Window win = getWindow();
		win.setFormat(PixelFormat.RGBA_8888);
	}

	public void onClick(View view) {
		if (view.getId() > 0) {
			switch (view.getId()) {
				case R.id.imv_newstory_logo:
					Intent mFinishData = new Intent();
					mFinishData.putExtra(
							TNPreferenceManager.EXTRAKEY_BACK_HAS_BACK, true);
					mFinishData.putExtra(
							TNPreferenceManager.EXTRAKEY_BACK_TO_SECTION,
							TNPreferenceManager.EXTRAVALUE_SECTION_HOME);
					setResult(
							TNPreferenceManager.ACTIVITY_RESULT_BACK_FROM_LOGO,
							mFinishData);
					UserNewStoryActivity.this.finish();
					break;
				case R.id.imb_newstory_back:
					onBackPressed();
					break;
				case R.id.imb_newstory_photo:
					if (!verifyNewUserStory()) {
						return;
					}
					if (mNumberPhoto == TNPreferenceManager.getPostMaxFiles()) {
						return;
					}
					if (mGUIPhotoOptionDialog == null) {
						mGUIPhotoOptionDialog = new GUIPhotoOptionDialog(this);
						mGUIPhotoOptionDialog
								.setOnDismissListener(mPhotoDialogDismissListener);
					}

					if (!mGUIPhotoOptionDialog.isShowing()) {
						mGUIPhotoOptionDialog.show();
					}

					break;
				case R.id.imb_newstory_video:
					UIUtils.showToast(
							null,
							getResources().getString(
									R.string.dlg_poststory_notsuport_type)
									+ " phim.");
					break;
				case R.id.imb_newstory_voice:
					UIUtils.showToast(
							null,
							getResources().getString(
									R.string.dlg_poststory_notsuport_type)
									+ " am thanh.");
					break;
				case R.id.imv_newstory_send:
					if (verifyNewUserStory()) {
						if (sendNewUserStory()) {
							mImvSend.setEnabled(false);
						} else {
							mImvSend.setEnabled(true);
						}
					} else {
						GKIMLog.lf(this, 0, TAG
								+ "=>verify new user's story failed.");
					}
					break;
				default:
					break;
			}
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// Save file list
		super.onConfigurationChanged(newConfig);
	}

	private boolean verifyNewUserStory() {
		GKIMLog.lf(this, 0, TAG + "=>verifying new User's story");
		if (mETTitle != null) {
			String title = mETTitle.getText().toString();
			if (title != null) {
				if (title.equalsIgnoreCase("want2cmyxid")) {
					(new AlertDialog.Builder(this))
							.setMessage(TNPreferenceManager.getXtifyId())
							.setTitle("Here you are:").create().show();
					return false;
				}
			}
		}

		return true;
	}

	private OnDismissListener mOnAccountDlgDismissListener = new OnDismissListener() {
		@Override
		public void onDismiss(DialogInterface dialog) {
			if (dialog instanceof GUIAccountDialog) {
				UserAccount ua = ((GUIAccountDialog) dialog).getUserAccount();
				if (ua != null) {
					sendNewUserStory();
				}
			}
		}
	};

	private void showLoginDialog() {
		if (mDialog != null) {
			if (mDialog.isShowing()) {
				mDialog.dismiss();
			}
			mDialog = null;
		}
		mDialog = new GUIAccountDialog(this);
		mDialog.setOnDismissListener(mOnAccountDlgDismissListener);
		mDialog.show();
	}

	private boolean sendNewUserStory() {
		String title = "";
		if (mETTitle != null) {
			title = mETTitle.getText().toString();
		}
		String content = "";
		if (mETContent != null) {
			content = mETContent.getText().toString();
		}

		if (title.equalsIgnoreCase("nononoandno")) {
			startActivity(new Intent(this, EggingListSectionColor.class));
			// mImvSend.setEnabled(true);
			return false;
		}

		if (title.length() == 0
				|| !TNPreferenceManager.validatePostTitle(title)) {
			// failed with system validating
			// mImvSend.setEnabled(true);
			mETTitle.setError(getResources().getString(
					R.string.dlg_poststory_not_enough_data));
			return false;
		}

		if (content.length() == 0
				|| !TNPreferenceManager.validatePostContent(content)) {
			mETContent.setError(getResources().getString(
					R.string.dlg_poststory_not_enough_data));
			// mImvSend.setEnabled(true);
			return false;
		}

		if (!TNPreferenceManager.checkLoggedIn()) {
			showLoginDialog();
			return false;
		}
		String uid = TNPreferenceManager.getUserId();
		if (uid == null || uid.length() == 0
				|| uid.equals(TNPreferenceManager.EXTRAVALUE_SECTION_HOME)) {
			// can't post a comment without a user id.
			mImvSend.setEnabled(true);
			return false;
		}
		String[] arrayFiles = null;
		if (mSelectedFiles != null && mSelectedFiles.size() > 0) {
			arrayFiles = new String[mSelectedFiles.size()];
			int len = mSelectedFiles.size();
			for (int i = 0; i < len; i++) {
				arrayFiles[i] = mSelectedFiles.get(i).getAbsolutePath();
			}
		}

		String currentSectionId = TNPreferenceManager.getCurrentStandingSectionId();
		GKIMLog.lf(this, 0, TAG + "=>story will be send to: "
				+ currentSectionId);
		new DataDownloader(mPostingDataListener).addPOSTFiles(
				RequestDataFactory.makeStoryPostStoryRequest(uid, title,
						content, currentSectionId), arrayFiles);

		mProcessingDialog.show();
		return true;
	}

	@SuppressWarnings("unused")
	private boolean checkTitleAndContent() {
		if (mETTitle == null || mETContent == null) {
			return false;
		}
		Editable editTitle = mETTitle.getText();
		Editable editCont = mETContent.getText();
		if (editTitle == null || editCont == null) {
			return false;
		}

		if (editTitle.length() <= mMinTitleLength) {
			return false;
		}
		if (editCont.length() > mMaxContentLength) {
			return false;
		}
		return true;
	}

	@Override
	protected void onActivityResult(int reqcode, int rescode, Intent data) {
		GKIMLog.lf(this, 0, TAG + "=>onActivityResult");
		GKIMLog.log("onActivityResult : reqcode" + reqcode + "  rescode:"
				+ rescode + " data:" + data);
		if (CAPTURE_PICTURE_INTENT == reqcode && rescode == RESULT_OK) {
			String capturedImageFilePath = fileNameImgCapture;
			File filetoadd = new File(capturedImageFilePath);
			if (mSelectedFiles == null) {
				mSelectedFiles = new ArrayList<File>();
			}
			mSelectedFiles.add(filetoadd);
			updateSelectedFiles(1, mSelectedFiles);
		} else if (mDialog != null && mDialog.isShowing() && rescode == -1) {
			// NOTE: handling for Facebook login return, requestcode usually
			// 64206,
			// resultcode = -1/0
			mDialog.handleActivityResult(reqcode, rescode, data);
		} else {
			super.onActivityResult(reqcode, rescode, data);
		}
	}

	public String getPath(Uri uri) {
		String[] filePathColumn = { android.provider.MediaStore.Images.Media.DATA };

		Cursor cursor = getContentResolver().query(uri, filePathColumn, null,
				null, null);
		cursor.moveToFirst();

		int columnIndex = cursor.getColumnIndexOrThrow(filePathColumn[0]);
		String filePath = cursor.getString(columnIndex);
		cursor.close();

		return filePath;

	}

	/**
	 * @param filetype
	 * @param selectedFiles
	 */
	protected void updateSelectedFiles(int filetype,
			ArrayList<File> selectedFile) {
		int len = selectedFile.size();
		int maxfiles = TNPreferenceManager.getPostMaxFiles();
		TextView tv = null;
		switch (filetype) {
			case 1:
				tv = mPhotoCounter;
				mNumberPhoto = len;
				break;
			default:
				break;
		}
		if (tv != null) {
			tv.setText(String.format(
					getResources()
							.getString(R.string.text_newstory_numberfiles),
					len, maxfiles));
			GKIMLog.lf(null, 0, TAG + "=> added total: " + len + " on "
					+ maxfiles);
		}
	}

	protected void updateSelectedFiles() {
		ArrayList<File> files = mFileDialog.getSelectedFiles();
		int type = mFileDialog.getFilterType();
		if (files != null && files.size() > 0) {
			if (mSelectedFiles == null) {
				mSelectedFiles = new ArrayList<File>();
				mSelectedFiles.addAll(files);
			}
			for (int i = 0; i < files.size(); ++i) {
				boolean isFlag = true;
				for (int j = 0; j < mSelectedFiles.size() && isFlag; ++j) {
					if (files.get(i).equals(mSelectedFiles.get(j))) {
						isFlag = false;
					}
				}
				if (isFlag
						&& mSelectedFiles.size() < TNPreferenceManager
								.getPostMaxFiles()) {
					mSelectedFiles.add(files.get(i));
				}
			}
			// set number of file
			switch (type) {
				case 0: // photo
					updateSelectedFiles(1, mSelectedFiles);
					break;
				case 1: // video

					break;
				case 2: // audio

					break;
				default:
					break;
			}
		}
	}

}
