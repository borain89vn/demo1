/**
 * 
 */
package com.gkxim.android.thanhniennews.layout;

import java.util.HashMap;
import java.util.Set;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.models.GenericResponse;
import com.gkxim.android.thanhniennews.models.UserAccount;
import com.gkxim.android.thanhniennews.networking.RequestDataFactory;
import com.gkxim.android.thanhniennews.tracking.Tracking;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.DataDownloader;
import com.gkxim.android.utils.DataDownloader.OnDownloadCompletedListener;
import com.gkxim.android.utils.GKIMLog;
import com.gkxim.android.utils.RequestData;
import com.gkxim.android.utils.UIUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Timon Trinh
 * 
 */
public class GUIStoryCommentDialog extends Dialog {

	private static final String TAG = "GUIStoryCommentDialog";

	private AlertDialog mProcessingDialog;
	protected long mSelectedIconId;
	protected View mLastSelectedView;
	private String mStoryId, mStoryTitle;
	private boolean mSucceed = false;
	private int mWidthIcon, mHeightIcon = 0;
	private HashMap<Long, Drawable> mHashIcons;
	private ImageView mImageBox;
	private String mPoststorySuccess;
	private LinearLayout mLLGallery;
	private TextView mTvTitle;

	private String mNoConnection;
	private GUIAccountDialog mDialog;

	public boolean isSucceed() {
		return mSucceed;
	}

	private View.OnClickListener mDefaultOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			GKIMLog.lf(null, 0, TAG + "=>onClick: "
					+ v.getClass().getSimpleName());
			switch (v.getId()) {
			case R.id.imb_storycomment_compose_send:
				// v.setEnabled(false);
				sendStoryComment();

				break;
			case R.id.imb_storycomment_compose_cancel:
				dismiss();
				break;
			default:
				Object tag = v.getTag();
				if (v instanceof ImageView && tag != null) {
					// may be icon click
					Long iconId = (Long) tag;
					Drawable draw = cloneDrawble(mHashIcons.get(iconId));
					mImageBox.setImageDrawable(draw);
					mImageBox.setSelected(false);
					mImageBox.invalidate();

					GKIMLog.lf(null, 0,
							TAG + "=> icon id: " + iconId.toString());
					mSelectedIconId = iconId.longValue();

					if (!UIUtils.hasJellyBean()) {
						mLastSelectedView.setSelected(false);
						mLastSelectedView = v;
						mLastSelectedView.setSelected(true);
					} else {
						// Close function Zoom
						// mLastSelectedView.animate().scaleXBy(-0.2f)
						// .scaleYBy(-0.2f).setDuration(500);
						mLastSelectedView = v;
						// mLastSelectedView.animate().scaleXBy(0.2f)
						// .scaleYBy(0.2f).setDuration(500);
					}
				}
				break;
			}
		}
	};

	private DataDownloader.OnDownloadCompletedListener mPostingDataListener = new OnDownloadCompletedListener() {

		@Override
		public void onCompleted(Object key, String result) {
			RequestData contentKey = (RequestData) key;
			GKIMLog.lf(GUIStoryCommentDialog.this.getContext(), 0, TAG
					+ "=> onCompleted, key: " + contentKey.getURLString());
			mProcessingDialog.dismiss();
			if (result == null || result.length() == 0) {
				GKIMLog.lf(null, 0, "NO Internet!!!Get out of App..");
				UIUtils.showToast(null, mNoConnection);
				return;
			}
			int type = contentKey.type;
			if (type == RequestDataFactory.DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_POST_COMMENT) {
				Gson gson = new GsonBuilder().registerTypeAdapter(
						GenericResponse.class,
						new GenericResponse.GenericResponseConverter())
						.create();
				GenericResponse gres = gson.fromJson(result,
						GenericResponse.class);
				if (gres != null && gres.isSucceed()) {
					mSucceed = true;
					HashMap<String, String> map = new HashMap<String, String>();
					map.put(TNPreferenceManager.EVENT_KEY_STORY_ID, mStoryId);
					map.put(TNPreferenceManager.EVENT_KEY_STORY_NAME,
							mStoryTitle);
					// FlurryAgent.onEvent(TNPreferenceManager.EVENT_STORY_COMMENT,
					// map);
					Tracking.sendEvent(TNPreferenceManager.EVENT_STORY_COMMENT,
							map);
					if (mPoststorySuccess != null) {
						UIUtils.showToast(null, mPoststorySuccess);
					}
					try {
						if (GUIStoryCommentDialog.this != null
								&& GUIStoryCommentDialog.this.isShowing()) {
							dismiss();
						}
					} catch (Exception e) {
						GKIMLog.lf(
								getContext(),
								4,
								TAG
										+ "=> failed to dismiss comment dialog on finishing post: "
										+ getContext().getClass().getName()
										+ ", error: " + e.getMessage());
					}
				} else {
					if (gres != null && gres.resultMsg != null) {
						UIUtils.showToast(null, gres.resultMsg);
					}
				}
			}
		}

		@Override
		public String doInBackgroundDebug(Object... params) {
			return null;
		}
	};

	/**
	 * @param context
	 */
	public GUIStoryCommentDialog(Context context) {
		super(context);
		initDialog(R.layout.dlg_storycomment_compose);
	}

	/**
	 * @param context
	 * @param theme
	 */
	public GUIStoryCommentDialog(Context context, int theme) {
		super(context, theme);
		initDialog(theme);
	}

	/**
	 * @param context
	 * @param cancelable
	 * @param cancelListener
	 */
	public GUIStoryCommentDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		initDialog(R.layout.dlg_storycomment_compose);
	}

	@SuppressWarnings("deprecation")
	private void initDialog(int theme) {
		GKIMLog.lf(getContext(), 0, TAG + "=>initDialog.");
		mPoststorySuccess = getContext().getResources().getString(
				R.string.dlg_poststory_success);
		mNoConnection = getContext().getResources().getString(
				R.string.close_application_no_connection);
		setCanceledOnTouchOutside(true);
		Window w = getWindow();
		w.requestFeature(Window.FEATURE_NO_TITLE);
		w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		// w.setBackgroundDrawableResource(R.drawable.bg_storycomment_compose_dlg);

		setContentView(theme);
		mImageBox = (ImageView) findViewById(R.id.id_storycomment_compose_box);
		// HorizontalScrollView hsv = (HorizontalScrollView)
		// findViewById(R.id.hsv_storycomment_compose_smileys);
		mLLGallery = (LinearLayout) findViewById(R.id.ll_storycomment_compose_smileys);
		if (mLLGallery != null) {
			mHashIcons = TNPreferenceManager.getSmileyIcons();
			if (mHashIcons != null && mHashIcons.size() > 0) {
				Set<Long> set = mHashIcons.keySet();
				Context context = getContext();
				for (long longId : set) {
					if (longId > 0) {
						ImageView i = new ImageView(context);
						i.setTag(longId);

						i.setImageDrawable(mHashIcons.get(longId));
						i.setScaleType(ScaleType.FIT_XY);

						i.setOnClickListener(mDefaultOnClickListener);
						i.setAdjustViewBounds(true);
						i.setLayoutParams(new LinearLayout.LayoutParams(
								getContext().getResources()
										.getDimensionPixelSize(
												R.dimen.icon_smile_size),
								getContext().getResources()
										.getDimensionPixelSize(
												R.dimen.icon_smile_size)));
						mLLGallery.addView(i);
						if (mLastSelectedView == null) {
							mLastSelectedView = i;
							mSelectedIconId = longId;

							if (!UIUtils.hasJellyBean()) {
								mLastSelectedView.setSelected(true);
							} else {
								// Close function zoom
								// mLastSelectedView.animate().scaleXBy(0.2f)
								// .scaleYBy(0.2f).setDuration(500);
							}
						}
					}
				}
			}
		}
		EditText et = (EditText) findViewById(R.id.ed_storycomment_compose_title);
		if (et != null) {
			et.setTypeface(TNPreferenceManager.getTNTypeface());
		}
		et = (EditText) findViewById(R.id.ed_storycomment_compose_edit);
		if (et != null) {
			et.setTypeface(TNPreferenceManager.getTNTypeface());
		}
		mTvTitle = (TextView) findViewById(R.id.tv_storycomment_compose_title);
		mTvTitle.setTypeface(TNPreferenceManager.getTNTypeface());
		ImageButton imb = (ImageButton) findViewById(R.id.imb_storycomment_compose_send);
		if (imb != null) {
			imb.setOnClickListener(mDefaultOnClickListener);
		}
		imb = (ImageButton) findViewById(R.id.imb_storycomment_compose_cancel);
		if (imb != null) {
			imb.setOnClickListener(mDefaultOnClickListener);
		}

	}

	public void setStoryId(String storyId) {
		mStoryId = storyId;
	}

	public void setStoryTitle(String storyTitle) {
		mStoryTitle = storyTitle;

	}

	protected void sendStoryComment() {
		if (mStoryId == null || mStoryId.length() == 0) {
			return;
		}
		EditText et = (EditText) findViewById(R.id.ed_storycomment_compose_title);
		String title = "";
		if (et != null) {
			title = et.getText().toString();
		}
		et = (EditText) findViewById(R.id.ed_storycomment_compose_edit);
		String content = "";
		if (et != null) {
			content = et.getText().toString();
		}
		if (title.length() == 0 || content.length() == 0) {
			UIUtils.showToast(getContext(), getContext().getResources()
					.getString(R.string.dlg_poststory_not_enough_data));
			//
			// failed with system validating
			return;
		}
		if (!TNPreferenceManager.checkLoggedIn()) {
			showLoginDialog();
			return;
		}

		String uid = TNPreferenceManager.getUserId();
		if (uid == null || uid.length() == 0
				|| uid.equals(TNPreferenceManager.EXTRAVALUE_SECTION_HOME)) {
			// can't post a comment without a user id.
			return;
		}

		new DataDownloader(mPostingDataListener).addDownload(RequestDataFactory
				.makeStoryPostCommentRequest(uid, mStoryId,
						String.valueOf(mSelectedIconId), title, content));

		try {
			// FIX bug: on phone, the context will be invalided and made
			// application crash
			if (mProcessingDialog == null) {
				mProcessingDialog = new GUISimpleLoadingDialog(getContext());
			}
			mProcessingDialog.show();
		} catch (Exception e) {
			GKIMLog.lf(
					getContext(),
					4,
					TAG
							+ "=> failed to show processing dialog on closing context: "
							+ getContext().getClass().getName() + ", error: "
							+ e.getMessage());
		}
	}

	public Drawable cloneDrawble(Drawable draw) {
		mWidthIcon = draw.getBounds().width();
		mHeightIcon = draw.getBounds().height();
		Bitmap bitmap = Bitmap.createBitmap(mWidthIcon, mHeightIcon,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		draw.setBounds(0, 0, mWidthIcon, mHeightIcon);
		draw.draw(canvas);
		BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
		draw = (Drawable) bitmapDrawable;
		return draw;
	}

	private OnDismissListener mOnAccountDlgDismissListener = new OnDismissListener() {
		@Override
		public void onDismiss(DialogInterface dialog) {
			if (dialog instanceof GUIAccountDialog) {
				UserAccount ua = ((GUIAccountDialog) dialog).getUserAccount();
				if (ua != null) {
					sendStoryComment();
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
		mDialog = new GUIAccountDialog(this.getContext());
		mDialog.setOnDismissListener(mOnAccountDlgDismissListener);
		mDialog.show();
	}

}
