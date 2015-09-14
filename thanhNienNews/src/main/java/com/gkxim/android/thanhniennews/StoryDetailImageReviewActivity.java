/**
 * 
 */
package com.gkxim.android.thanhniennews;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.models.ImageThumb;
import com.gkxim.android.thanhniennews.models.VideoThumb;
import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.GKIMLog;
import com.gkxim.android.utils.UIUtils;
import com.polites.android.GestureImageView;

/**
 * @author Timon
 * 
 */
public class StoryDetailImageReviewActivity extends Activity {
	private static final String TAG = StoryDetailImageReviewActivity.class
			.getSimpleName();
	public static final boolean DEBUG = GKIMLog.DEBUG_ON;
	private int mImageCount;
	private String[] mUrls;
	private String[] mCaptions;
	private View.OnClickListener mDefaultOnClickListener = getDefaultOnClickListener();
	private ImageButton mImbBack;
	private LinearLayout mLLImages;
	private ViewPager mPager;
	private WebImagePagerAdapter mPagerAdapter;
	private ArrayList<ImageThumb> listImageThumbs;
	private ArrayList<VideoThumb> listVideoThumbs;
	private boolean mImageFirst;
	public static boolean mVideoFirst;
	private boolean mTabletVersion = false;

	protected class ViewHolder {
		public TextView mTvCaption;
		public ImageView mIvImage;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GKIMLog.lf(this, 1, TAG + "=>onCreate");
		setContentView(R.layout.activity_story_imageview);
		mTabletVersion = (getResources().getBoolean(R.bool.istablet));
		if (!mTabletVersion) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			//NOTE: Comment out after Tet 2014
            if(TNPreferenceManager.SECTION_SPRING) {
                ImageView imgv = (ImageView) findViewById(R.id.imgv_horse_phone);
                imgv.setVisibility(View.VISIBLE);
            }
		}else{
			//NOTE: Comment out after Tet 2014
            if(TNPreferenceManager.SECTION_SPRING) {
                ImageView imgv1 = (ImageView) findViewById(R.id.imgv_horse_tablet);
                imgv1.setVisibility(View.VISIBLE);
            }
		}
	
		Bundle extras_ = getIntent().getExtras();
		if (extras_ != null) {
			if (extras_.containsKey("dataImageThumbs")) {
				try {
					listImageThumbs = (ArrayList<ImageThumb>) extras_
							.get("dataImageThumbs");
				} catch (ClassCastException e) {
					GKIMLog.l(4, e.getMessage());
				}
			}
			if (extras_.containsKey("dataVideoThumbs")) {
				try {
					listVideoThumbs = (ArrayList<VideoThumb>) extras_
							.get("dataVideoThumbs");
				} catch (ClassCastException e) {
					GKIMLog.l(4, e.getMessage());
				}
			}
			if (extras_.containsKey("dataImagesFirst")) {
				try {
					mImageFirst = (Boolean) extras_.get("dataImagesFirst");
					GKIMLog.l(1, TAG + " mImageFirst:" + mImageFirst);
				} catch (ClassCastException e) {
					GKIMLog.l(4, e.getMessage());
				}
			}
		}

		if ((listImageThumbs == null && listVideoThumbs == null)
				|| (listImageThumbs != null && listVideoThumbs != null
						&& listImageThumbs.size() <= 0 && listVideoThumbs
						.size() <= 0)) {
			// NOTE stop the activity
			finish();
			return;
		}
		if (mVideoFirst) {
			VideoThumb videoThumb = listVideoThumbs.get(0);
			if (videoThumb != null) {
				Intent mIntentVideoThumb = new Intent(
						StoryDetailImageReviewActivity.this,
						StoryDetailVideoPlayActivity.class);
				mIntentVideoThumb.putExtra("objvideo", videoThumb);
				startActivityForResult(mIntentVideoThumb, 0);
			}
		}
		initLayout();
	}

	/**
	 * 
	 */
	private void initLayout() {
		// DisplayMetrics dm = new DisplayMetrics();
		// getWindowManager().getDefaultDisplay().getMetrics(dm);
		// // mScreenWidth = dm.widthPixels;
		// mScreenHeight = dm.heightPixels;
		ImageView img_logo = (ImageView) findViewById(R.id.header_iv_logo);
		img_logo.setOnClickListener(mDefaultOnClickListener);

		mImbBack = (ImageButton) findViewById(R.id.imb_storydetail_back);
		// mLLImages = (LinearLayout)
		// findViewById(R.id.ll_storydetail_reviewimages);
		mPagerAdapter = new WebImagePagerAdapter(listImageThumbs,
				listVideoThumbs, mImageFirst);
		mPager = (ViewPager) findViewById(R.id.pager);

		mPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				GKIMLog.lf(null, 1, TAG + "=>onPageSelected: " + position);
			}

			@Override
			public void onPageScrolled(int h, float w, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		mPager.setAdapter(mPagerAdapter);

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
		// if (mPager != null) {
		// mPager.setOnClickListener(mDefaultOnClickListener);
		// }
		if (mPagerAdapter != null) {
			mPagerAdapter.notifyDataSetChanged();
		}
		super.onResume();
	}

	@Override
	protected void onStop() {
		GKIMLog.lf(this, 1, TAG + "=>onStop");
		if (mImbBack != null) {
			mImbBack.setOnClickListener(null);
		}
		if (mPager != null) {
			mPager.setOnClickListener(null);
		}
		super.onStop();
	}

	public void onClick(View view) {
		GKIMLog.lf(this, 1, TAG + "=>onClick: " + view);
	}

	public View generateImageReviewView(String url, String caption) {
		FrameLayout result = (FrameLayout) LayoutInflater.from(this).inflate(
				R.layout.inc_story_imagereview, null);
		// FrameLayout result = (FrameLayout) this.getLayoutInflater().inflate(
		// R.layout.inc_story_imagereview, null);
		FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) result
				.getLayoutParams();
		if (lp == null) {
			lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);
		}
		result.setLayoutParams(lp);

		ImageView imvImage = (ImageView) findViewById(R.id.imv_story_review);
		if (url != null && url.length() > 0) {
			UIUtils.loadToImageView(url, imvImage);
		}
		TextView tvCaption = (TextView) findViewById(R.id.tv_story_review);
		tvCaption.setTypeface(TNPreferenceManager.getTNTypeface(),
				Typeface.NORMAL);
		if (caption != null && caption.length() > 0) {
			tvCaption.setText(caption);
		}
		return result;
	}

	private View.OnClickListener getDefaultOnClickListener() {
		return (new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				GKIMLog.lf(StoryDetailImageReviewActivity.this, 1, TAG
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
					StoryDetailImageReviewActivity.this.finish();
					break;
				case R.id.imb_storydetail_back:
					finish();
					break;
				case R.id.pager:
					// toggleCaption();
					break;
				case R.id.btn_playvideoreview:
					VideoThumb videoThumb = (VideoThumb) v.getTag();
					if (videoThumb != null) {
						Intent mIntentVideoThumb = new Intent(
								StoryDetailImageReviewActivity.this,
								StoryDetailVideoPlayActivity.class);
						mIntentVideoThumb.putExtra("objvideo", videoThumb);
						startActivityForResult(mIntentVideoThumb, 0);
					}
					break;
				default:
					if (v instanceof GestureImageView) {
						Object obj = v.getTag();
						if (obj != null && obj instanceof TextView) {
							TextView cap = (TextView) obj;
							if (cap != null) {
								int visible = cap.getVisibility();
								if (visible == View.INVISIBLE) {
									cap.setVisibility(View.VISIBLE);
								} else {
									cap.setVisibility(View.INVISIBLE);
								}
							}
						}
					}
					break;
				}
			}
		});
	}

	protected void toggleCaption() {
		if (mPager != null && mPagerAdapter != null) {
		}
	}

	public class WebImagePagerAdapter extends PagerAdapter {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.support.v4.view.PagerAdapter#getCount()
		 */

		private ArrayList<ImageThumb> listImage;
		private ArrayList<VideoThumb> listVideo;
		private boolean imageFirst;

		public WebImagePagerAdapter(ArrayList<ImageThumb> listImage,
				ArrayList<VideoThumb> listVideo, boolean imageFirst) {
			this.listImage = listImage;
			this.listVideo = listVideo;
			this.imageFirst = imageFirst;
		}

		@Override
		public int getCount() {
			if (listImage != null && listVideo != null) {
				return (listImage.size() + listVideo.size());
			} else if (listImage != null) {
				return listImage.size();
			} else if (listVideo != null) {
				return listVideo.size();
			}
			// if (listImage != null) {
			// return listImage.size();
			// }
			return 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.support.v4.view.PagerAdapter#isViewFromObject(android.view
		 * .View, java.lang.Object)
		 */
		@Override
		public boolean isViewFromObject(View view, Object object) {
			return (view.equals(object));
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			if (object instanceof RelativeLayout
					&& container.getChildCount() > position) {
				container.removeView((View) object);
			}
		}

		@SuppressLint("DefaultLocale")
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			GKIMLog.lf(null, 1, TAG + "=>instantiateItem: " + position
					+ " in: " + container);
			String url = null;
			String caption = null;
			String linkvideo = null;
			VideoThumb videoThumb = null;
			if (imageFirst) {
				if (listImage != null && listImage.size() > 0
						&& position < listImage.size()) {
					ImageThumb imageThumb = listImage.get(position);
					url = imageThumb.getUrl();
					caption = imageThumb.getCaption();
				} else if (listVideo != null) {
					if (listImage != null && listImage.size() > 0) {
						videoThumb = listVideo.get(position - listImage.size());
					} else {
						videoThumb = listVideo.get(position);
					}
					url = videoThumb.getVideoThumb();
					caption = videoThumb.getCaption();
					linkvideo = videoThumb.getVideoUrl();
				}
			} else {
				if (listVideo != null && listVideo.size() > 0
						&& position < listVideo.size()) {
					videoThumb = listVideo.get(position);
					url = videoThumb.getVideoThumb();
					caption = videoThumb.getCaption();
					linkvideo = videoThumb.getVideoUrl();
				} else if (listImage != null && listImage.size() > 0) {
					ImageThumb imageThumb;
					if (listVideo != null && listVideo.size() > 0) {
						imageThumb = listImage.get(position - listVideo.size());
					} else {
						imageThumb = listImage.get(position);
					}
					url = imageThumb.getUrl();
					caption = imageThumb.getCaption();
				}
			}
			RelativeLayout result = null;
			if (imageFirst) {
				if (listImage != null && listImage.size() > 0
						&& position < listImage.size()) {
					result = (RelativeLayout) LayoutInflater.from(
							container.getContext()).inflate(
							R.layout.inc_story_review_webimage, null);
				} else {
					result = (RelativeLayout) LayoutInflater.from(
							container.getContext()).inflate(
							R.layout.inc_story_review_video, null);
				}
			} else {
				if (listVideo != null && listVideo.size() > 0
						&& position < listVideo.size()) {
					result = (RelativeLayout) LayoutInflater.from(
							container.getContext()).inflate(
							R.layout.inc_story_review_video, null);
				} else {
					result = (RelativeLayout) LayoutInflater.from(
							container.getContext()).inflate(
							R.layout.inc_story_review_webimage, null);
				}
			}
			if (result != null) {
				RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) result
						.getLayoutParams();
				if (lp == null) {
					lp = new RelativeLayout.LayoutParams(
							LayoutParams.MATCH_PARENT,
							LayoutParams.MATCH_PARENT);
				}
				result.setLayoutParams(lp);

				GestureImageView giv = (GestureImageView) result
						.findViewById(R.id.gim_review);
				if (giv != null && url != null) {
					UIUtils.loadToImageView(url, giv);
					giv.setOnClickListener(mDefaultOnClickListener);
					if (imageFirst) {
						if (listImage != null && listImage.size() > 0
								&& position < listImage.size()) {
							giv.setEnabled(true);
						} else {
							giv.setEnabled(false);
						}
					} else {
						if (listVideo != null && listVideo.size() > 0
								&& position < listVideo.size()) {
							giv.setEnabled(false);
						} else {
							giv.setEnabled(true);
						}
					}
				}

				TextView tvCaption = (TextView) result
						.findViewById(R.id.tv_story_review);
				tvCaption.setTypeface(TNPreferenceManager.getTNTypeface(),
						Typeface.NORMAL);
				tvCaption
						.setVisibility(TNPreferenceManager.CAPTION_VISIBLE_DEFAULT_STATE);
				giv.setTag(tvCaption);
				tvCaption.setTextSize(Float.parseFloat(getResources()
						.getString(R.dimen.storydetail_title_textsize)
						.toLowerCase().replace("sp", "")));
				if (caption != null) {
					if (DEBUG) {
						caption = "Caption: " + caption;
					}
					if (caption != null && caption.length() > 0) {
						tvCaption.setText(caption);
					}
				}

				Button button = (Button) result
						.findViewById(R.id.btn_playvideoreview);
				if (button != null && videoThumb != null) {
					button.setTag(videoThumb);
					button.setOnClickListener(mDefaultOnClickListener);
				}
				// VideoView videoView = (VideoView) result
				// .findViewById(R.id.videoView_thanhnien);
				// if (videoView != null && linkvideo != null) {
				// GKIMLog.l(4, "Link video : " + linkvideo);
				// Uri localUri = Uri.parse(linkvideo);
				// videoView.setVideoURI(localUri);
				// videoView.start();
				// }
				((ViewPager) container).addView(result);
				return result;
			}
			return container;
		}
	}
}
