package com.gkxim.android.thanhniennews.layout;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.models.GenericResponse;
import com.gkxim.android.thanhniennews.networking.RequestDataFactory;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.DataDownloader;
import com.gkxim.android.utils.DataDownloader.OnDownloadCompletedListener;
import com.gkxim.android.utils.GKIMLog;
import com.gkxim.android.utils.RequestData;
import com.gkxim.android.utils.UIUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Timon
 * 
 */
public class GUIPNSCheckDialog extends Dialog {

	private static final String TAG = GUIPNSCheckDialog.class.getSimpleName();
	private Button mBtnCheckAll;
	private String mTextCheckAll;
	private String mTextUnCheckAll;
	protected class DataItem {
		String title = "";
		String id = "";

		// boolean checked = false;

		public DataItem(String ptitle, String pid) {
			super();
			this.title = ptitle;
			this.id = pid;
		}
	}

	private android.view.View.OnClickListener mOnClickListener = getOnClickListener();

	private ListView mListView = null;
	private PNSSectionAdapter mListAdapter;
	private Typeface mTypeFace;
	private ArrayList<DataItem> mArray = new ArrayList<DataItem>();
	private boolean mHasSelectAll = false;
	private SparseBooleanArray mArrayBoolParse;
	private boolean mUnCheckAll = false;

	public GUIPNSCheckDialog(Context context) {
		super(context);
		GKIMLog.lf(context, 1, TAG + "=>GUIPNSCheckDialog");
		mTypeFace = TNPreferenceManager.getTNTypeface();
		initDialogList();
	}

	@Override
	protected void onStop() {
		if (mListView != null) {
			mListView.setOnItemSelectedListener(null);
		}
		super.onStop();
	}

	@Override
	public void show() {
		GKIMLog.lf(getContext(), 1, TAG + "=>show");
		// updateCheckState();
		super.show(); 
		boolean isCheckAll = TNPreferenceManager.getPNSCheckAll();
		if (!isCheckAll) {
			TNPreferenceManager.setPNSCheckAll(true);
			mHasSelectAll = true;
			checkAll(mHasSelectAll);
		}
		setTextButtonCheck(mHasSelectAll);
	}

	private void initDialogList() {
		mTextCheckAll = getContext().getResources().getString(R.string.dlg_pnscheck_title_selectall);
		mTextUnCheckAll = getContext().getResources().getString(R.string.dlg_pnscheck_title_cancelall);
		GKIMLog.lf(null, 1, TAG + "=>initDialogList");
		setCanceledOnTouchOutside(false);
		Typeface tf = TNPreferenceManager.getTNTypeface();

		Window w = getWindow();
		w.requestFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.dlg_pnschech_list);
		TextView tv = (TextView) findViewById(R.id.tv_pnscheck_title);
		tv.setTypeface(tf);

		mBtnCheckAll = (Button) findViewById(R.id.btn_pnscheck_selectall);
		if (mBtnCheckAll != null) {
			mBtnCheckAll.setTypeface(tf);
			mBtnCheckAll.setOnClickListener(mOnClickListener);
		}
		Button btn = (Button) findViewById(R.id.btn_pnscheck_agree);
		if (btn != null) {
			btn.setTypeface(tf);
			btn.setOnClickListener(mOnClickListener);
		}
		mListView = (ListView) findViewById(R.id.lv_pnscheck_list);
		mListAdapter = new PNSSectionAdapter();
		initSectionAdapter();
		mListView.setAdapter(mListAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if(mListView.getCheckItemIds().length == mListView.getCount()){
					mHasSelectAll = true;
					setTextButtonCheck(true);
				}else{
					mHasSelectAll = false;
					setTextButtonCheck(false);
				}
			}
		});
	}
	
	public void setTextButtonCheck(boolean state) {
		if (mBtnCheckAll != null) {
			if (state) {
				mBtnCheckAll.setText(mTextUnCheckAll);
			} else {
				mBtnCheckAll.setText(mTextCheckAll);
			}
		}
	}

//	private OnItemClickListener getOnItemClickListener() {
//		return (new OnItemClickListener() {
//			@Override
//			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
//					long arg3) {
//				// TODO Auto-generated method stub
//				
//			}
//		});
//	}

	private void initSectionAdapter() {
		
		String selectedSecIds = TNPreferenceManager.getListeningSections();
		String[] secIds = TNPreferenceManager.getSectionIDs();
		if (secIds != null && secIds.length > 0) {
			mHasSelectAll = true;
			for (String secId : secIds) {
				String textTitle = TNPreferenceManager
						.getSectionTitleFromPref(secId);
				boolean bCheck = selectedSecIds.contains(" " + secId + ",");
				mArray.add(new DataItem(textTitle, secId));
				if (mHasSelectAll && !bCheck) {
					mHasSelectAll = false;
				}
				// mListAdapter.addPNSSection(secId, textTitle, false);
			}
		}
		setTextButtonCheck(mHasSelectAll);
	}

	public void updateCheckState() {
		String selectedSecIds = TNPreferenceManager.getListeningSections();
		selectedSecIds += ",";
		int len = mArray.size();
		mHasSelectAll = true;
		for (int i = 0; i < len; i++) {
			DataItem di = mArray.get(i);
			boolean bChecked = false;
			if (selectedSecIds.contains(" " + di.id + ",")) {
				bChecked = true;
			}else{
				mHasSelectAll = false;
			}
			mListView.setItemChecked(i, bChecked);
		}
		mArrayBoolParse = mListView.getCheckedItemPositions();
	}

	private android.view.View.OnClickListener getOnClickListener() {
		return (new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				GKIMLog.lf(null, 1, TAG + "=>onClick");
				int id = v.getId();
				if (id == R.id.btn_pnscheck_agree) {
					registerPNSSections();
				} else if (id == R.id.btn_pnscheck_selectall) {
					mHasSelectAll = !mHasSelectAll;
					setTextButtonCheck(mHasSelectAll);
					checkAll(mHasSelectAll);
				}
			}
		});
	}

	protected void registerPNSSections() {
		String secids = getSelectedSectionIds();
		GKIMLog.lf(null, 0, TAG + "=>registerPNSSections: " + secids);
		if (secids != null) {
			// 1. save to sharePreferences
			TNPreferenceManager.setListeningSections(secids);
			// then 2 is register to Server
			String secids2Submit = "";
			if (secids.trim().length() > 0) {
				secids2Submit = secids.substring(0, secids.length() - 1);
				mUnCheckAll = false;
			} else {
				mUnCheckAll = true;
			}
			(new DataDownloader(new OnDownloadCompletedListener() {

				@Override
				public void onCompleted(Object key, String result) {
					GKIMLog.lf(null, 1, TAG + "=>onCompleted: " + result);
					RequestData contentKey = (RequestData) key;
					boolean bSucceed = true;
					if (result == null || result.trim().length() <= 0) {
						bSucceed = false;
					} else {
						if (contentKey.type == RequestDataFactory.DATA_JSON_DEF_REQUESTTYPE_PNS_REGISTER_SECTION) {
							Gson gson = new GsonBuilder()
									.registerTypeAdapter(
											GenericResponse.class,
											new GenericResponse.GenericResponseConverter())
									.create();
							GenericResponse gres = gson.fromJson(result,
									GenericResponse.class);
							if (gres == null || !gres.isSucceed()) {
								bSucceed = false;
							}
						}
					}
					
					if (bSucceed) {
						if(!mUnCheckAll){
							UIUtils.showToast(
									getContext(),
									getContext().getResources().getString(
											R.string.dlg_pnssuccess_content));
						}
						else{
							UIUtils.showToast(
									getContext(),
									getContext().getResources().getString(
											R.string.dlg_pnsempty_content));
						}
					} else {
						UIUtils.showToast(
								getContext(),
								getContext().getResources().getString(
										R.string.dlg_pnsfailed_content));
					}
				}

				@Override
				public String doInBackgroundDebug(Object... params) {
					return null;
				}
			})).addDownload(RequestDataFactory.makePNSRegisterSectionRequest(
					TNPreferenceManager.getUserId(),
					TNPreferenceManager.getXtifyId(), secids2Submit));
		}
		dismiss();
	}

	protected void checkAll(boolean bCheckState) {
		int len = mArray.size();
		for (int i = 0; i < len; i++) {
			mListView.setItemChecked(i, bCheckState);
		}
	}

	public String getSelectedSectionIds() {
		StringBuilder sb = new StringBuilder();
		// int selectedCount = mListView.getCheckedItemCount();
		// if (selectedCount > 0) {
		mArrayBoolParse = mListView.getCheckedItemPositions();
		if (mArrayBoolParse != null) {
			int length = mArray.size();
			for (int i = 0; i < length; i++) {
				if (mArrayBoolParse.get(i)) {
					sb.append(" ").append(mArray.get(i).id).append(",");
				}
			}
		}
		// }
		return sb.toString();
	}

	public class PNSSectionAdapter extends BaseAdapter {
		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount() {
			return mArray.size();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public DataItem getItem(int position) {
			if (position >= 0 && position < mArray.size()) {
				return mArray.get(position);
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int position) {
			long id = -1;
			if (position >= 0 && position < mArray.size()) {
				try {
					String secId = mArray.get(position).id;
					id = Long.valueOf(secId);
				} catch (NumberFormatException e) {
					GKIMLog.lf(null, 0, TAG + "=>getItemId: " + position
							+ " failed: " + e.getMessage());
				}
			}
			return id;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getView(int, android.view.View,
		 * android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			GKIMLog.lf(null, 1, TAG + "=>getView: " + position);
			CheckViewAutoMarquee cv = null;
			if (convertView == null) {
				LayoutInflater inflater = LayoutInflater.from(getContext());
				cv = (CheckViewAutoMarquee) inflater.inflate(
						R.layout.dlg_pnscheck_item, null);
				cv.setTypeface(mTypeFace);
				// cv.setOnClickListener(mOnClickListener);
			} else {
				cv = (CheckViewAutoMarquee) convertView;
			}
			DataItem di = getItem(position);
			if (di != null) {
				cv.setText(di.title);
				cv.setTag(di);
			}
			return cv;
		}

		/**
		 * @param secId
		 * @param textTitle
		 * @param bCheckState
		 */
		public void addPNSSection(String secId, String textTitle,
				boolean bCheckState) {
			mArray.add(new DataItem(textTitle, secId));
		}
		
	
	}
}
