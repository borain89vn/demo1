package com.knx.framework.main.history;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageGaussianBlurFilter;
import jp.co.cyberagent.android.gpuimage.GPUImage.ResponseListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;

import com.knx.framework.R;
import com.knx.framework.helper.ARiseConfigs;
import com.knx.framework.helper.LangPref;

public class HistoryPage extends FragmentActivity implements ViewPager.OnPageChangeListener {
    public static final String TAG = "ARiseHistoryPager";
    
    private ImageView historyPageBackground;
    
    private PagerTitleStrip pagerTitleStrip;
    
    static final int ITEMS = 2;
    MyAdapter mAdapter;
    ViewPager mViewPager;
    int curPosition = 0;
    
    private Button deleteButton;
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        try {
        	getActionBar().hide();
        } catch (Exception e) {
        	Log.e(TAG, "Cannot hide action bar");
        }
        
        setContentView(R.layout.arise_activity_history);
        
        mAdapter = new MyAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPageChangeListener(this);
        
        makePagerTitleStrip();
        makeBlurBackground();
        makeDeleteButton();
    }
    
    private void makeBlurBackground() {
		
		historyPageBackground = (ImageView) findViewById(R.id.history_page_background);
		
		byte[] cameraFrame = HistoryPage.this.getIntent().getExtras().getByteArray("lastCameraFrame");
		int width = HistoryPage.this.getIntent().getExtras().getInt("cameraPreviewWidth");
		int height = HistoryPage.this.getIntent().getExtras().getInt("cameraPreviewHeight");
		
		if (cameraFrame == null || width == 0 || height == 0) {
			historyPageBackground.setBackgroundColor(0xFFAAAAAA);
		} else {
			YuvImage yuvImg = new YuvImage(cameraFrame, ImageFormat.NV21, width, height, null);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			yuvImg.compressToJpeg(new Rect(0, 0, width, height), 100, baos);
			byte[] jpegByteArray = baos.toByteArray();
			Bitmap cameraFrameBitmap = BitmapFactory.decodeByteArray(jpegByteArray, 0, jpegByteArray.length); // bitmap of camera frame in landscape
			Matrix matrix = new Matrix();
			matrix.postRotate(90);
			Bitmap rotatedBmp = Bitmap.createBitmap(cameraFrameBitmap, 0, 0, width, height, matrix, true);
			
			GPUImageGaussianBlurFilter blurFilter = new GPUImageGaussianBlurFilter(2.5f);
			ArrayList<GPUImageFilter> filters = new ArrayList<GPUImageFilter>();
			filters.add(blurFilter);
			GPUImage.getBitmapForMultipleFilters(rotatedBmp, filters, new ResponseListener<Bitmap>() {

				@Override
				public void response(final Bitmap arg0) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							historyPageBackground.setImageBitmap(arg0);
						}
					});
				}
			});
		}
	}
    
    private void makePagerTitleStrip() {
    	pagerTitleStrip = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
    	pagerTitleStrip.setBackgroundColor(ARiseConfigs.THEME_COLOR);
    	pagerTitleStrip.setNonPrimaryAlpha(0.5f);
    	pagerTitleStrip.setTextSpacing(100);
    }
    
    private void makeDeleteButton() {
    	deleteButton = (Button) findViewById(R.id.delete_button);
    	deleteButton.setBackgroundColor(ARiseConfigs.THEME_COLOR);
		deleteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				HistoryFragment fragment = (HistoryFragment) mAdapter.getItem(curPosition);
				fragment.removeItems();
			}
		});
    }
    
    public static class MyAdapter extends FragmentPagerAdapter {
    	
    	HistoryFragment historyFragment;
		HistoryFragment bookmarkFragment;
		
        public MyAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
            
            historyFragment = HistoryFragment.newInstance();
			Bundle args = new Bundle();
			args.putString("section", "History");
			historyFragment.setArguments(args);	
			
			bookmarkFragment = HistoryFragment.newInstance();
        	args = new Bundle();
			args.putString("section", "Favorites");
			bookmarkFragment.setArguments(args);
        }
 
        @Override
        public int getCount() {
            return ITEMS;
        }
 
        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
            	return historyFragment;
            } else {
            	return bookmarkFragment;
            }
        }
        
        @Override
        public CharSequence getPageTitle(int position) {
        	if (position == 0) return LangPref.TXTRECENTS;
        	else return LangPref.TXTBOOKMARK;
        }
    }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.history_menu, menu);
        return true;
    }

	@Override
	public void onPageScrollStateChanged(int arg0) { }

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

	@Override
	public void onPageSelected(int position) {
		curPosition = position;
		HistoryFragment fragment = (HistoryFragment) mAdapter.getItem(curPosition);
		fragment.reloadData();
		fragment.resetSelectionStatus();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		// try to reload data every time resuming to this activity
		try {
			HistoryFragment fragment = (HistoryFragment) mAdapter.getItem(curPosition);
			fragment.reloadData();
			fragment.resetSelectionStatus();
		} catch (Exception e) {
			
		}
		
		deleteButton.setVisibility(View.GONE);
		
		TranslateAnimation slidingUpAnimation = new TranslateAnimation(0, 0, deleteButton.getLayoutParams().height, 0);
		slidingUpAnimation.setStartOffset(500);
		slidingUpAnimation.setDuration(300);
		slidingUpAnimation.setFillAfter(true);
		slidingUpAnimation.setRepeatCount(0);
		slidingUpAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation anim) {
				deleteButton.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onAnimationRepeat(Animation anim) {}
			
			@Override
			public void onAnimationEnd(Animation anim) {}
		});
		deleteButton.startAnimation(slidingUpAnimation);
	}
}