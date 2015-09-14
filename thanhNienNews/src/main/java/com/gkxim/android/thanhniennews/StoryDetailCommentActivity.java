package com.gkxim.android.thanhniennews;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.layout.GUIStoryCommentDialog;
import com.gkxim.android.thanhniennews.models.GenericResponse;
import com.gkxim.android.thanhniennews.models.StoryComment;
import com.gkxim.android.thanhniennews.networking.RequestDataFactory;
import com.gkxim.android.thanhniennews.social.SocialHelper;
import com.gkxim.android.thanhniennews.social.SocialShare;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.DataDownloader;
import com.gkxim.android.utils.DataDownloader.OnDownloadCompletedListener;
import com.gkxim.android.utils.GKIMLog;
import com.gkxim.android.utils.RequestData;
import com.gkxim.android.utils.UIUtils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.gson.*;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.util.ArrayList;

public class StoryDetailCommentActivity extends Activity {
	private static final String TAG = StoryDetailCommentActivity.class
			.getSimpleName();
	public static final String EXTRA_STORYID = "story_id";
	public static final String EXTRA_STORY_TITLE = "story_title";
	public static final String EXTRA_COMMENT_COUNT = "comment_count";

	private String mStoryId, mStoryTitle;
	private int mCommentCount;
	private ImageView mIvShare, mGUIHeader;
	private ImageButton mImbBack;
	private TextView mTvCommentCount;
	private PullToRefreshListView mPLvComment;
	private OnDismissListener mOnDialogDismissListener = getOnCommentDialogDissmisListener();
	private OnClickListener mDefaultOnClickListener = getOnClickListener();
	private OnRefreshListener<ListView> mOnRefreshListener = getOnRefreshListener();
	private GUIStoryCommentDialog mCommentDialog;
	private DataDownloader mDataDownloader = getDataDownloader();
	public Typeface mDefaultTF;
	public String mAuthorFormat;
	private StoryCommentAdapter mSCAdapter;
	private int mColorTitle, mColorAuthur, mColorComment, mColorBackground;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		GKIMLog.lf(this, 1, TAG + "=>onCreate");
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		if (!intent.hasExtra(EXTRA_STORYID)) {
			finish();
		}

		mColorTitle = getResources().getColor(
				R.color.story_comment_title_mode_night);
		mColorComment = getResources().getColor(
				R.color.story_comment_content_mode_night);
		mColorAuthur = getResources().getColor(
				R.color.story_comment_name_mode_night);
		mColorBackground = getResources().getColor(
				R.color.story_comment_background_mode_night);
		TNPreferenceManager.setContext(this);
		mDefaultTF = TNPreferenceManager.getTNTypeface();
		mAuthorFormat = getResources().getString(
				R.string.comment_author_n_count);
		initLayout();
		mStoryId = intent.getStringExtra(EXTRA_STORYID);
		mStoryTitle = intent.getStringExtra(EXTRA_STORY_TITLE);
		mCommentCount = intent.getIntExtra(EXTRA_COMMENT_COUNT, 0);
		if (mCommentCount > 0) {
			mDataDownloader.addDownload(TNPreferenceManager
					.isConnectionAvailable(), RequestDataFactory
					.makeStoryCommentsRequest(TNPreferenceManager.getUserId(),
							mStoryId));
		}

		// Look up the AdView as a resource and load a request.
		AdView adView = (AdView) this.findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		adView.loadAd(adRequest);
	}

	/**
	 * @return
	 */
	private OnRefreshListener getOnRefreshListener() {
		return (new OnRefreshListener<ListView>() {
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				GKIMLog.lf(StoryDetailCommentActivity.this, 1, TAG
						+ "=>onRefresh");
				if (mDataDownloader != null) {
					mDataDownloader.setExitTasksEarly(true);
					mDataDownloader = null;
					mDataDownloader = getDataDownloader();
				}
				mDataDownloader.addDownload(RequestDataFactory
						.makeStoryCommentsRequest(
								TNPreferenceManager.getUserId(), mStoryId));
			}
		});
	}

	@Override
	protected void onDestroy() {
		GKIMLog.lf(this, 1, TAG + "=>onDestroy");
		mOnRefreshListener = null;
		mPLvComment = null;
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		GKIMLog.lf(this, 1, TAG + "=>onStart");
		super.onStart();
	}

	@Override
	protected void onResume() {
		GKIMLog.lf(this, 1, TAG + "=>onResume");
		listenViewOnClick(mImbBack, mIvShare, mGUIHeader);
		if (mPLvComment != null) {
			mPLvComment.setOnRefreshListener(mOnRefreshListener);
		}
		if (mTvCommentCount != null) {
			mTvCommentCount.setText(String.valueOf(mCommentCount));
		}
		changeTextMode(TNPreferenceManager.isNightMode());
		// if (mSCAdapter != null) {
		// mSCAdapter.addComments(new StoryComment[] {
		// new StoryComment("aa", "aaaa", "hix"),
		// new StoryComment("bb", "bbb", "hix") });
		// }
		super.onResume();
	}

	public void changeTextMode(boolean mode) {
		LinearLayout ln = (LinearLayout) findViewById(R.id.FrameLayout1);
		if (mode) {
			if (ln != null) {
				ln.setBackgroundColor(mColorBackground);
			}
		}
	}

	@Override
	protected void onStop() {
		GKIMLog.lf(this, 1, TAG + "=>onStop");
		stopListenViewOnClick(mImbBack, mIvShare);
		if (mDataDownloader != null) {
			mDataDownloader.setExitTasksEarly(true);
		}
		super.onStop();
	}

	@Override
	public void onBackPressed() {
		GKIMLog.lf(this, 1, TAG + "=>onBackPressed");
		Intent data = new Intent();
		data.putExtra(TNPreferenceManager.EXTRAKEY_COMMENT_COUNT, mCommentCount);
		setResult(RESULT_OK, data);
		super.onBackPressed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		GKIMLog.lf(this, 0, TAG + "=>onActivityResult");
		if (resultCode == RESULT_OK) {
			SocialShare provider = SocialHelper.getLastInstance()
					.getSNSInstance();
			if (provider != null) {
				if (!provider.handlingActivityForResult(requestCode,
						resultCode, data)) {
					super.onActivityResult(requestCode, resultCode, data);
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void listenViewOnClick(View... views) {
		if (views != null && views.length > 0) {
			for (View view : views) {
				if (view != null) {
					view.setOnClickListener(mDefaultOnClickListener);
				}
			}
		}
	}

	private void stopListenViewOnClick(View... views) {
		if (views != null && views.length > 0) {
			for (View view : views) {
				if (view != null) {
					view.setOnClickListener(null);
				}
			}
		}
	}

	private OnClickListener getOnClickListener() {
		return (new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				GKIMLog.lf(StoryDetailCommentActivity.this, 0, TAG
						+ "=>onClick: " + v);
				switch (v.getId()) {
				case R.id.header_iv_logo:
					Intent mFinishData = new Intent();
					mFinishData.putExtra(
							TNPreferenceManager.EXTRAKEY_BACK_HAS_BACK, true);
					mFinishData.putExtra(
							TNPreferenceManager.EXTRAKEY_BACK_TO_SECTION,
							TNPreferenceManager.EXTRAVALUE_SECTION_HOME);
					setResult(
							TNPreferenceManager.ACTIVITY_RESULT_BACK_FROM_LOGO_DETAIL_IMAGE,
							mFinishData);
					StoryDetailCommentActivity.this.finish();
					break;
				case R.id.imb_storydetail_back:
					Intent data = new Intent();
					data.putExtra(TNPreferenceManager.EXTRAKEY_COMMENT_COUNT,
							mCommentCount);
					setResult(RESULT_OK, data);
					finish();
					break;
				case R.id.imv_storydetail_shareby_comment:
					// if (!TNPreferenceManager.checkLoggedIn()) {
					// UIUtils.showToast(
					// StoryDetailCommentActivity.this,
					// getResources().getString(
					// R.string.request_for_login));
					// } else {
					showCommentDialog();
					// }
					break;
				default:
					break;
				}
			}
		});
	}

	private DataDownloader getDataDownloader() {
		return (new DataDownloader(new OnDownloadCompletedListener() {

			@Override
			public void onCompleted(Object key, String result) {
				GKIMLog.lf(null, 1, TAG + "=>onCompleted: " + key.toString());
				if (result == null || result.length() <= 0) {
					return;
				}
				int type = ((RequestData) key).type;
				if (type == RequestDataFactory.DATA_JSON_DEF_REQUESTTYPE_DATA_STORY_COMMENTS) {
					Gson gson = new GsonBuilder().registerTypeAdapter(
							GenericResponse.class,
							new GenericResponse.GenericResponseConverter())
							.create();
					GenericResponse gres = gson.fromJson(result,
							GenericResponse.class);
					if (gres != null && gres.isHasData()) {
						JsonElement jAE = new JsonParser().parse(gres.getData());
						if (jAE != null && jAE.isJsonArray()) {

							JsonArray ja = jAE.getAsJsonArray();
							int length = ja.size();
							if (length > 0) {
								JsonElement je = ja.get(0);
								if (je.isJsonPrimitive()) {
									return;
								}
								StoryComment[] arrComments = new StoryComment[length];
								StoryComment aComment = null;
								for (int i = 0; i < length; i++) {
									aComment = gson.fromJson(ja.get(i),
											StoryComment.class);
									arrComments[i] = aComment;
								}
								GKIMLog.lf(null, 0, TAG + "=> loaded: "
										+ length);
								mCommentCount = arrComments.length;
								if (mTvCommentCount != null) {
									mTvCommentCount.setText(String
											.valueOf(mCommentCount));
								}
								if (mSCAdapter != null) {
									mSCAdapter.clear();
									mSCAdapter.addComments(arrComments);
									if (mPLvComment != null) {
										mPLvComment.onRefreshComplete();
									}
								}
								// if (mHandler != null) {
								// Message msg = new Message();
								// msg.what = 1;
								// msg.obj = arrComments;
								// mHandler.sendMessageDelayed(msg, 500);
								// }
							}
						}
					}
				}
			}

			@Override
			public String doInBackgroundDebug(Object... params) {
				return null;
			}
		}));
	}

	private void initLayout() {
		setContentView(R.layout.activity_storydetail_comment);
		mGUIHeader = (ImageView) findViewById(R.id.header_iv_logo);
		if (mGUIHeader != null) {
			mGUIHeader.setClickable(true);
		}
		mIvShare = (ImageView) findViewById(R.id.imv_storydetail_shareby_comment);
		mImbBack = (ImageButton) findViewById(R.id.imb_storydetail_back);
		TextView txt = (TextView) findViewById(R.id.tv_storydetail_comment_header_text);
		txt.setTypeface(TNPreferenceManager.getTNTypeface(), Typeface.BOLD);
		mTvCommentCount = (TextView) findViewById(R.id.tv_storydetail_comment_header_count);
		mTvCommentCount.setText(String.valueOf(mCommentCount));
		mPLvComment = (PullToRefreshListView) findViewById(R.id.lv_storydetail_comment);
		mSCAdapter = new StoryCommentAdapter();
		mPLvComment.getRefreshableView().setAdapter(mSCAdapter);

		boolean mTabletVersion = UIUtils.isTablet(this);
		Log.d("initGUIHeader", "mTabletVersion:" + mTabletVersion);
		// NOTE: Xuan 2014 has done, so no need horses here
        if(TNPreferenceManager.SECTION_SPRING) {
            if (!mTabletVersion) {
                ImageView imgv = (ImageView) findViewById(R.id.imgv_horse_phone);
                imgv.setVisibility(View.VISIBLE);
            } else {
                ImageView imgv1 = (ImageView) findViewById(R.id.imgv_horse_tablet);
                imgv1.setVisibility(View.VISIBLE);
            }
        }
	}

	protected void showCommentDialog() {
		if (mCommentDialog != null) {
			mCommentDialog = null;
		}
		mCommentDialog = new GUIStoryCommentDialog(this);
		mCommentDialog.setStoryId(mStoryId);
		mCommentDialog.setStoryTitle(mStoryTitle);
		mCommentDialog.show();
		mCommentDialog.setOnDismissListener(mOnDialogDismissListener);
	}

	private OnDismissListener getOnCommentDialogDissmisListener() {
		return (new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				GKIMLog.lf(null, 1, TAG + "=>onDismiss: " + dialog + ": "
						+ mStoryId);
				if (mDataDownloader != null) {
					mDataDownloader.setExitTasksEarly(true);
					mDataDownloader = null;
					mDataDownloader = getDataDownloader();
				}
				// Map<String, String> map = new Hashtable<String, String>();
				// map.put(TNPreferenceManager.EVENT_KEY_STORY_ID, mStoryId);
				// map.put(TNPreferenceManager.EVENT_KEY_STORY_NAME,
				// mStoryTitle);
				// FlurryAgent.onEvent(TNPreferenceManager.EVENT_STORY_COMMENT,
				// map);
				mDataDownloader.addDownload(TNPreferenceManager
						.isConnectionAvailable(), RequestDataFactory
						.makeStoryCommentsRequest(
								TNPreferenceManager.getUserId(), mStoryId));
			}
		});
	}

	public class StoryCommentAdapter extends BaseAdapter {

		public class ViewHolder {
			public TextView title;
			public TextView author;
			public TextView date;
			public TextView comment;
			public ImageView imageicon;
		}

		private ArrayList<StoryComment> mArrComments;

		@Override
		public int getCount() {
			if (mArrComments != null) {
				return mArrComments.size();
			}
			return 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public StoryComment getItem(int position) {
			if (mArrComments != null && position < mArrComments.size()) {
				return mArrComments.get(position);
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
			return 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getView(int, android.view.View,
		 * android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = View.inflate(parent.getContext(),
						R.layout.storydetail_comment_content, null);
				holder = new ViewHolder();
				holder.imageicon = (ImageView) convertView
						.findViewById(R.id.iv_storycomment_icon);
				holder.title = (TextView) convertView
						.findViewById(R.id.tv_storycomment_title);
				holder.title.setTypeface(mDefaultTF, Typeface.BOLD);
				holder.author = (TextView) convertView
						.findViewById(R.id.tv_storycomment_author);
				holder.author.setTypeface(mDefaultTF, Typeface.ITALIC);
				holder.date = (TextView) convertView
						.findViewById(R.id.tv_storycomment_date);
				holder.date.setTypeface(mDefaultTF, Typeface.ITALIC);
				holder.comment = (TextView) convertView
						.findViewById(R.id.tv_storycomment_content);
				holder.comment.setTypeface(mDefaultTF, Typeface.NORMAL);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			StoryComment sc = mArrComments.get(position);
			String sctitle = sc.getCommentTitle();
			if (sctitle != null && sctitle.length() > 0) {
				holder.title.setText(sctitle);
			}
			String urlIcon = sc.getUrlRatingIcon();
			if (urlIcon != null && urlIcon.length() > 0) {
				UIUtils.loadToImageView(urlIcon, holder.imageicon);
			}
			String authorNcount = String.format(mAuthorFormat, sc.getName(),
					sc.getCommentCount());
			holder.author.setText(authorNcount);
			holder.date.setText(sc.getCommentTimeText());
			holder.comment.setText(sc.getComment());
			changeTextMode(TNPreferenceManager.isNightMode(), holder);
			return convertView;
		}

		public void clear() {
			if (mArrComments != null && mArrComments.size() > 0) {
				mArrComments.clear();
			}
		}

		public void changeTextMode(boolean mode, ViewHolder holder) {
			if (mode && holder != null) {
				holder.title.setTextColor(mColorTitle);
				holder.comment.setTextColor(mColorComment);
				holder.author.setTextColor(mColorAuthur);
			}
		}

		public void addComments(StoryComment[] comments) {
			if (mArrComments == null) {
				mArrComments = new ArrayList<StoryComment>();
			}
			if (comments == null || comments.length == 0) {
				return;
			}
			int oldlen = mArrComments.size();
			for (StoryComment storyComment : comments) {
				mArrComments.add(storyComment);
			}
			int newlen = mArrComments.size();
			if (newlen != oldlen) {
				GKIMLog.lf(null, 0, TAG + "=>notifyDataSetChanged: " + newlen
						+ " from: " + oldlen);
				notifyDataSetChanged();
			}
		}
	}

}
