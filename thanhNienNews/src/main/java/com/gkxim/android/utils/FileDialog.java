/**
 * 
 */
package com.gkxim.android.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.VideoView;

import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;

/**
 * @author Timon Trinh
 * 
 */
@SuppressLint("ValidFragment")
public class FileDialog extends DialogFragment {

	private static final String TAG = "FileDialog";
	/**
	 * Root directory
	 */
	private static final String ROOT = "/";
	public static final String START_PATH = "START_PATH";
	public static final String FORMAT_FILTER = "FORMAT_FILTER";
	public static final String RESULT_PATH = "RESULT_PATH";
	private static final String ITEM_IMAGE = "image";
	private static final String ITEM_KEY = "key";
	public static final String SELECTION_MODE = "SELECTION_MODE";
	public static final String CAN_SELECT_DIR = "CAN_SELECT_DIR";
	public static final int FILTER_TYPE_IMAGE = 0;
	public static final int FILTER_TYPE_VIDEO = 1;
	public static final int FILTER_TYPE_AUDIO = 2;
	private static final String[] FILTER_IMAGE = new String[] { "png", "jpg",
			"jpeg" };
	private static final String[] FILTER_VIDEO = new String[] { "3gp", "3g2",
			"avi", "mp4", "mpg" };
	private static final String[] FILTER_AUDIO = new String[] { "mp3", "m4a",
			"mid", "wav", "wma" };

	// properties
	private TextView myPath;
	private EditText mFileName;
	private ListView mListView;
	private List<String> path = null;
	private ArrayList<HashMap<String, Object>> mList;

	private long mMaxFilesSize;
	private long mCurrentFilesSize;
	private String parentPath;
	private String currentPath = ROOT;
	private boolean canSelectDir = false;
	private File selectedFile;
	private ArrayList<File> mSelectedFiles = null;

	private String[] formatFilter = null;
	private HashMap<String, Integer> lastPositions = new HashMap<String, Integer>();
	private Context mContext;

	private InputMethodManager inputManager;
	private Button mSelectButton;
	private LinearLayout mLayoutSelect;
	private LinearLayout mLayoutCreate;
	private int mSelectionMode = SelectionMode.MODE_OPEN;
	private String mStartPath = ROOT;
	private Dialog mReviewDialog;
	private int mPostMaxFiles = 1;

	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> l, View v, int position, long id) {
			File file = new File(path.get(position));

			// setSelectVisible(v);
			//
			if (file.isDirectory()) {
				if (file.canRead()) {
					lastPositions.put(currentPath, position);
					getDir(path.get(position));
					if (canSelectDir) {
						selectedFile = file;
						v.setSelected(true);
						// selectButton.setEnabled(true);
					}
				} else {
					// new AlertDialog.Builder(this)
					// .setIcon(R.drawable.icon)
					// .setTitle(
					// "["
					// + file.getName()
					// + "] "
					// + getText(R.string.cant_read_folder))
					// .setPositiveButton("OK",
					// new DialogInterface.OnClickListener() {
					//
					// @Override
					// public void onClick(
					// DialogInterface dialog,
					// int which) {
					//
					// }
					// }).show();
				}
			} else {
				long addedSize = mCurrentFilesSize + file.length();
				if (addedSize > mMaxFilesSize
						|| (mSelectedFiles != null && mSelectedFiles.size() >= mPostMaxFiles)) {
					AlertDialog dlg = (new AlertDialog.Builder(mContext))
							.setTitle(R.string.upload_file_oversize).create();
					dlg.show();
					return;
				}
				selectedFile = file;
				v.setSelected(true);
				showReviewDialog(file);
				// Intent intentOpen = new Intent(Intent.ACTION_VIEW);
				// String type = TNPreferenceManager.getFileType(file);
				// intentOpen.setDataAndType(Uri.fromFile(file),
				// type);
				// startActivityForResult(intentOpen, 5);
			}
		}
	};
	
	
	public void getFileFromShare(String path) {
		File file = new File(path);
		long addedSize = mCurrentFilesSize + file.length();
		if (addedSize > mMaxFilesSize
				|| (mSelectedFiles != null && mSelectedFiles.size() >= mPostMaxFiles)) {
			AlertDialog dlg = (new AlertDialog.Builder(mContext))
					.setTitle(R.string.upload_file_oversize).create();
			dlg.show();
			return;
		}
		selectedFile = file;
		if (mSelectedFiles == null) {
			mSelectedFiles = new ArrayList<File>();
		}
		if (!mSelectedFiles.contains(selectedFile)
				&& mSelectedFiles.size() < mPostMaxFiles) {
			mSelectedFiles.add(selectedFile);
			mCurrentFilesSize += selectedFile.length();
		}
		selectedFile = null;
	}

	private View.OnClickListener mBtnOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			int filesSelected = 0;
			switch (v.getId()) {
			case R.id.btn_poststory_review_fileok:
				if (mSelectedFiles == null) {
					mSelectedFiles = new ArrayList<File>();
				}
				if (!mSelectedFiles.contains(selectedFile)
						&& mSelectedFiles.size() < mPostMaxFiles) {
					mSelectedFiles.add(selectedFile);
					mCurrentFilesSize += selectedFile.length();
				}
				filesSelected = mSelectedFiles.size();
				selectedFile = null;
				mReviewDialog.dismiss();
				// FileDialog.this.dismiss();
				break;
			case R.id.btn_poststory_review_filecancel:
				if (mSelectedFiles != null
						&& mSelectedFiles.contains(selectedFile)) {
					mSelectedFiles.remove(selectedFile);
					mCurrentFilesSize -= selectedFile.length();
				}
				selectedFile = null;
				mReviewDialog.dismiss();
				// mListView.setSelection(0);
				break;
			default:
				break;
			}
//			GKIMLog.lf(null, 0, TAG + "=> selected: " + filesSelected
//					+ "files with total: " + mCurrentFilesSize);
//			UIUtils.showToast(null, Gravity.CENTER_VERTICAL, String.format(
//					getResources().getString(
//							R.string.dlg_poststory_number_of_file),
//							filesSelected, mPostMaxFiles));
		}
	};

	private MediaController.MediaPlayerControl mMediaPlayerControl = new MediaController.MediaPlayerControl() {

		@Override
		public void start() {
			GKIMLog.lf(null, 0, TAG + "=>start");
		}

		@Override
		public void seekTo(int pos) {
			GKIMLog.lf(null, 0, TAG + "=>seekTo " + pos);
		}

		@Override
		public void pause() {
			GKIMLog.lf(null, 0, TAG + "=>pause");
		}

		@Override
		public boolean isPlaying() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public int getDuration() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getCurrentPosition() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getBufferPercentage() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public boolean canSeekForward() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean canSeekBackward() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean canPause() {
			GKIMLog.lf(null, 0, TAG + "=>canPause");
			return true;
		}

		@Override
		public int getAudioSessionId() {
			return 0;
		}
	};
	private OnDismissListener mDialogDismissListener;

	/**
	 * 
	 */
	public FileDialog() {
		GKIMLog.lf(null, 0, TAG + "=>constructor.");
	}

	public FileDialog(Context context) {
		GKIMLog.lf(null, 0, TAG + "=>constructor with context.");
		mContext = context;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		GKIMLog.lf(null, 0, TAG + "=>onCreateDialog");
		return super.onCreateDialog(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		GKIMLog.lf(null, 0, TAG + "=>onCreateView");
		Window w = this.getDialog().getWindow();
		w.requestFeature(Window.FEATURE_NO_TITLE);
		View dlgView = inflater.inflate(R.layout.file_dialog_main, container);

		myPath = (TextView) dlgView.findViewById(R.id.path);
		mFileName = (EditText) dlgView.findViewById(R.id.fdEditTextFile);

		inputManager = (InputMethodManager) inflater.getContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);

		// mSelectionMode = savedInstanceState.getIntExtra(SELECTION_MODE,
		// SelectionMode.MODE_CREATE);
		//
		// formatFilter = getIntent().getStringArrayExtra(FORMAT_FILTER);
		//
		// canSelectDir = getIntent().getBooleanExtra(CAN_SELECT_DIR, false);

		mSelectButton = (Button) dlgView.findViewById(R.id.fdButtonSelect);
		mSelectButton.setVisibility(View.GONE);
		mSelectButton.setEnabled(false);
		mSelectButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (selectedFile != null) {
					dismiss();
					// getIntent().putExtra(RESULT_PATH,
					// selectedFile.getPath());
					// setResult(RESULT_OK, getIntent());
					// finish();
				}
			}
		});
		final Button newButton = (Button) dlgView
				.findViewById(R.id.fdButtonNew);
		newButton.setVisibility(View.GONE);
		newButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setCreateVisible(v);

				mFileName.setText("");
				mFileName.requestFocus();
			}
		});

		mLayoutSelect = (LinearLayout) dlgView
				.findViewById(R.id.fdLinearLayoutSelect);
		mLayoutCreate = (LinearLayout) dlgView
				.findViewById(R.id.fdLinearLayoutCreate);
		mLayoutCreate.setVisibility(View.GONE);

		final Button cancelButton = (Button) dlgView
				.findViewById(R.id.fdButtonCancel);
		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setSelectVisible(v);
			}

		});
		final Button createButton = (Button) dlgView
				.findViewById(R.id.fdButtonCreate);
		createButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mFileName.getText().length() > 0) {
					// getIntent().putExtra(RESULT_PATH, currentPath + "/" +
					// mFileName.getText());
					// setResult(RESULT_OK, getIntent());
					// finish();
				}
			}
		});

		if (mSelectionMode == SelectionMode.MODE_OPEN) {
			newButton.setEnabled(false);
		}

		// String startPath = getIntent().getStringExtra(START_PATH);
		mStartPath = mStartPath != null ? mStartPath : ROOT;
		if (canSelectDir) {
			File file = new File(mStartPath);
			selectedFile = file;
			mSelectButton.setEnabled(true);
		}

		Button btnDone = (Button) dlgView
				.findViewById(R.id.btn_selectfile_upload);
		if (btnDone != null) {
			btnDone.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});
		}
		mListView = (ListView) dlgView.findViewById(android.R.id.list);
		if (mListView != null) {
			mListView.setOnItemClickListener(mOnItemClickListener);
			
		}
		return dlgView;
	}

	@Override
	public void onAttach(Activity activity) {
		GKIMLog.lf(null, 0, TAG + "=>onAttach from: "
				+ activity.getClass().getSimpleName());
		super.onAttach(activity);
	}

	@Override
	public void onStart() {
		GKIMLog.lf(null, 0, TAG + "=onStart.");
		getDir(mStartPath);
		super.onStart();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		if (mDialogDismissListener != null) {
			mDialogDismissListener.onDismiss(dialog);
		}
		super.onDismiss(dialog);
	}

	private void addItem(String fileName, int imageId) {
		HashMap<String, Object> item = new HashMap<String, Object>();
		item.put(ITEM_KEY, fileName);
		item.put(ITEM_IMAGE, imageId);
		mList.add(item);
	}

	private void getDir(String dirPath) {

		boolean useAutoSelection = dirPath.length() < currentPath.length();

		Integer position = lastPositions.get(parentPath);

		getDirImpl(dirPath);

		if (mListView != null && position != null && useAutoSelection) {
			mListView.setSelection(position);
		}

	}

	private void getDirImpl(final String dirPath) {

		currentPath = dirPath;

		final List<String> item = new ArrayList<String>();
		path = new ArrayList<String>();
		mList = new ArrayList<HashMap<String, Object>>();

		File f = new File(currentPath);
		File[] files = f.listFiles();
		if (files == null) {
			currentPath = ROOT;
			f = new File(currentPath);
			files = f.listFiles();
		}
		myPath.setText(getText(R.string.location) + ": " + currentPath);

		if (!currentPath.equals(ROOT)) {

			item.add(ROOT);
			addItem(ROOT, R.drawable.folder);
			path.add(ROOT);

			item.add("../");
			addItem("../", R.drawable.folder);
			path.add(f.getParent());
			parentPath = f.getParent();

		}

		TreeMap<String, String> dirsMap = new TreeMap<String, String>();
		TreeMap<String, String> dirsPathMap = new TreeMap<String, String>();
		TreeMap<String, String> filesMap = new TreeMap<String, String>();
		TreeMap<String, String> filesPathMap = new TreeMap<String, String>();
		for (File file : files) {
			if (file.isDirectory()) {
				String dirName = file.getName();
				dirsMap.put(dirName, dirName);
				dirsPathMap.put(dirName, file.getPath());
			} else {
				final String fileName = file.getName();
				final String fileNameLwr = fileName.toLowerCase();
				// se ha um filtro de formatos, utiliza-o
				if (formatFilter != null) {
					boolean contains = false;
					for (int i = 0; i < formatFilter.length; i++) {
						final String formatLwr = formatFilter[i].toLowerCase();
						if (fileNameLwr.endsWith(formatLwr)) {
							contains = true;
							break;
						}
					}
					if (contains) {
						filesMap.put(fileName, fileName);
						filesPathMap.put(fileName, file.getPath());
					}
					// senao, adiciona todos os arquivos
				} else {
					filesMap.put(fileName, fileName);
					filesPathMap.put(fileName, file.getPath());
				}
			}
		}
		item.addAll(dirsMap.tailMap("").values());
		item.addAll(filesMap.tailMap("").values());
		path.addAll(dirsPathMap.tailMap("").values());
		path.addAll(filesPathMap.tailMap("").values());

		SimpleAdapter fileList = new SimpleAdapter(mContext, mList,
				R.layout.file_dialog_row,
				new String[] { ITEM_KEY, ITEM_IMAGE }, new int[] {
						R.id.fdrowtext, R.id.fdrowimage });

		for (String dir : dirsMap.tailMap("").values()) {
			addItem(dir, R.drawable.folder);
		}

		for (String file : filesMap.tailMap("").values()) {
			addItem(file, R.drawable.file);
		}

		fileList.notifyDataSetChanged();

		if (mListView != null) {
			mListView.setAdapter(fileList);
		}
	}

	private void setCreateVisible(View v) {
		mLayoutCreate.setVisibility(View.VISIBLE);
		mLayoutSelect.setVisibility(View.GONE);

		inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
		mSelectButton.setEnabled(false);
	}

	private void setSelectVisible(View v) {
		mLayoutCreate.setVisibility(View.GONE);
		mLayoutSelect.setVisibility(View.VISIBLE);

		inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
		mSelectButton.setEnabled(false);
	}

	public class SelectionMode {
		public static final int MODE_CREATE = 0;
		public static final int MODE_OPEN = 1;
	}

	public void setFilterType(int filterType) {
		switch (filterType) {
		case FILTER_TYPE_IMAGE:
			formatFilter = FILTER_IMAGE;
			break;
		case FILTER_TYPE_VIDEO:
			formatFilter = FILTER_VIDEO;
			break;
		case FILTER_TYPE_AUDIO:
			formatFilter = FILTER_AUDIO;
			break;
		default:
			formatFilter = null;
			break;
		}
	}

	public void setStartPath(String startPath) {
		if (startPath != null && startPath.length() > 0) {
			mStartPath = startPath;
		}
	}

	public void setMaxSize(long postFilesMaxSize) {
		mMaxFilesSize = postFilesMaxSize;
	}

	public void setCurrentFilesSize(long currentFilesSize) {
		mCurrentFilesSize = currentFilesSize;
	}

	private View getReviewView(File afile) {
		String type = TNPreferenceManager.getFileType(afile).toLowerCase();
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout ll = (LinearLayout) inflater.inflate(
				R.layout.dlg_poststory_review_file, null);
		DisplayMetrics d = mContext.getResources().getDisplayMetrics();

		MediaController medController = null;
		Button btnOk = (Button) ll
				.findViewById(R.id.btn_poststory_review_fileok);
		btnOk.setOnClickListener(mBtnOnClickListener);
		Button btnCancel = (Button) ll
				.findViewById(R.id.btn_poststory_review_filecancel);
		btnCancel.setOnClickListener(mBtnOnClickListener);
		if (type.contains("image")) {
			ImageView imgView = (ImageView) ll
					.findViewById(R.id.imv_poststory_review_file);
			imgView.setVisibility(View.VISIBLE);
			Options thumbOpts = new Options();
			long filelength = afile.length();
			if (filelength > 10 * 1024 * 1024) {
				thumbOpts.inSampleSize = 256;
			} else if (filelength > 1024 * 1024) {
				thumbOpts.inSampleSize = 64;
			} else {
				thumbOpts.inSampleSize = 4;
			}
			Bitmap scaled = BitmapFactory.decodeFile(afile.getAbsolutePath(),
					thumbOpts);
			imgView.setImageBitmap(scaled);
		} else if (type.contains("audio")) {
			MediaPlayer mp = new MediaPlayer();

			try {
				mp.setDataSource(afile.getAbsolutePath());
				mp.prepare();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			medController = (MediaController) ll
					.findViewById(R.id.medv_poststory_review_file);
			medController.setVisibility(View.VISIBLE);

		} else if (type.contains("video") || type.contains("audio")) {
			medController = (MediaController) ll
					.findViewById(R.id.medv_poststory_review_file);
			medController.setVisibility(View.VISIBLE);
			VideoView videoView = new VideoView(mContext);
			videoView.setVideoPath(afile.getAbsolutePath());
			videoView.setVisibility(View.VISIBLE);
			medController.setAnchorView(videoView);

		} else {
			TextView tv = new TextView(mContext);
			tv.setText("Content not match!!");
		}
		if (medController != null) {
			medController.setMediaPlayer(mMediaPlayerControl);
		}
		ll.setLayoutParams(new LinearLayout.LayoutParams(d.widthPixels,
				d.widthPixels));
		return ll;
	}

	private void showReviewDialog(File afile) {
		View view = getReviewView(afile);
		if (mReviewDialog == null) {
			mReviewDialog = new Dialog(mContext);
			mReviewDialog.setCanceledOnTouchOutside(true);
			mReviewDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		}
		mReviewDialog.setContentView(view);
		mReviewDialog.show();
	}

	public void setMaxFiles(int postMaxFiles) {
		mPostMaxFiles = postMaxFiles;
	}

	public void setOnDismissListener(OnDismissListener l) {
		mDialogDismissListener = l;
	}

	public int getFilterType() {
		if (formatFilter == null || formatFilter.length <= 0) {
			return -1;
		}
		if (formatFilter.equals(FILTER_AUDIO)) {
			return 2;
		} else if (formatFilter.equals(FILTER_VIDEO)) {
			return 1;
		} else if (formatFilter.equals(FILTER_IMAGE)) {
			return 0;
		}
		return -1;
	}

	public ArrayList<File> getSelectedFiles() {
		if (mSelectedFiles == null || mSelectedFiles.size() <= 0) {
			return null;
		}
		return mSelectedFiles;
	}
	/* Nam.nguyen close 
	 * We should return to ArrayList to UserNewStoryActivity using this. 
	public Collection<File> getSelectedFiles() {
		if (mSelectedFiles == null || mSelectedFiles.size() <= 0) {
			return null;
		}
		return mSelectedFiles;
	}*/
}
