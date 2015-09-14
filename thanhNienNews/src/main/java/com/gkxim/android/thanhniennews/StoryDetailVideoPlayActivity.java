/**
 * 
 */
package com.gkxim.android.thanhniennews;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.layout.VideoDetailFragment;
import com.gkxim.android.thanhniennews.models.VideoThumb;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.GKIMLog;
import com.gkxim.android.utils.UIUtils;

/**
 * @author Timon
 * 
 */
public class StoryDetailVideoPlayActivity extends FragmentActivity {
	private static final String TAG = StoryDetailVideoPlayActivity.class
			.getSimpleName();
	public static final boolean DEBUG = GKIMLog.DEBUG_ON;
	private View.OnClickListener mDefaultOnClickListener = getDefaultOnClickListener();
	private ImageButton mImbBack;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GKIMLog.lf(this, 1, TAG + "=>onCreate");
		Bundle extras_ = getIntent().getExtras();
		setContentView(R.layout.activity_story_video);
		ImageView img_logo = (ImageView) findViewById(R.id.header_iv_logo);
		img_logo.setOnClickListener(mDefaultOnClickListener);

		mImbBack = (ImageButton) findViewById(R.id.imb_storydetail_back);
		if (extras_ != null) {
			if (extras_.containsKey("objvideo")) {
				try {
					VideoThumb videoThumb = (VideoThumb) extras_
							.get("objvideo");
					if (videoThumb != null && videoThumb.getVideoUrl() != null
							&& videoThumb.getVideoUrl().length() > 0) {
						FragmentManager fragmentManager = getSupportFragmentManager();
						FragmentTransaction transaction = fragmentManager
								.beginTransaction();
						VideoDetailFragment videoDetailFragment = new VideoDetailFragment();
						videoDetailFragment.setVideoThumb(videoThumb);
						// transaction.setCustomAnimations(R.anim.slide_in_top,
						// R.anim.slide_out_bottom);
						transaction.replace(R.id.container_video,
								videoDetailFragment);
						// transaction.addToBackStack(RemoveFragment.class.getSimpleName());
						transaction.commit();
					}else{
						closeVideo();
					}
				} catch (ClassCastException e) {
					GKIMLog.l(4, e.getMessage());
				}
			}
		}
		boolean mTabletVersion = UIUtils.isTablet(this);
		Log.d("initGUIHeader", "mTabletVersion:" + mTabletVersion);
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

	@Override
	protected void onStart() {
		GKIMLog.lf(this, 1, TAG + "=>onStart");
		super.onStart();
	}

	@Override
	protected void onResume() {
		GKIMLog.lf(this, 1, TAG + "=>onResume");
		if (mImbBack != null) {
			mImbBack.setOnClickListener(mDefaultOnClickListener);
		}
		super.onResume();
	}

	@Override
	protected void onStop() {
		StoryDetailImageReviewActivity.mVideoFirst = false;
		GKIMLog.lf(this, 1, TAG + "=>onStop");
		if (mImbBack != null) {
			mImbBack.setOnClickListener(null);
		}
		StoryDetailVideoPlayActivity.this.finish();
		super.onStop();
	}

	private View.OnClickListener getDefaultOnClickListener() {
		return (new View.OnClickListener() {

			@Override
			public void onClick(View v) {
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
					StoryDetailVideoPlayActivity.this.finish();
					break;
				case R.id.imb_storydetail_back:
					StoryDetailVideoPlayActivity.this.finish();
					break;
				default:
					break;
				}
			}
		});
	}

	/**
	 * 
	 */
	public void closeVideo() {
		// TODO Auto-generated method stub
		StoryDetailImageReviewActivity.mVideoFirst = false;
		finish();
	}

}
