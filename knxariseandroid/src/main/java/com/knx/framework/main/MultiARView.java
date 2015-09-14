package com.knx.framework.main;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImage.ResponseListener;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageGaussianBlurFilter;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.knx.framework.R;
import com.knx.framework.helper.ARiseConfigs;
import com.knx.framework.main.history.HistoryThumbnailLoader;

public class MultiARView extends RelativeLayout {
	
	private Context context;
	
	private ImageView blurBackground;
	private ListAdapter adapter;
	private ArrayList<JSONObject> jsonObjectList = new ArrayList<JSONObject>();
	
	private AlphaAnimation fadeInAnim;
	
	public MultiARView(Context cxt) {
		super(cxt);
		context = cxt;
		adapter = new ListAdapter(context, jsonObjectList);
		this.addView(View.inflate(context, R.layout.multi_ar_layout, null));
		
		TextView multiARHeaderSection = (TextView) findViewById(R.id.arHeader);
		multiARHeaderSection.setBackgroundColor(ARiseConfigs.THEME_COLOR);
		
		ListView listV = (ListView) findViewById(R.id.listview);
		listV.setAdapter(adapter);
		listV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				JSONObject chosenAR = jsonObjectList.get(arg2);
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						hideMultiARLayout();
					}
				});
				((CameraActivity) context).setJSON(chosenAR, chosenAR.toString());
			}
		});
		listV.setChoiceMode(ListView.CHOICE_MODE_NONE);
	}
	
	public void setJSONList(ArrayList<JSONObject> jsonList) {
		jsonObjectList = jsonList;
		
		adapter.changeJSONObjectList(jsonList);
		
		Button but = (Button) findViewById(R.id.numerItem);
		but.setBackgroundColor(ARiseConfigs.THEME_COLOR);
		but.setText("Showing "+ adapter.getCount() + " items");
	}
	
	public void clearJSONList() {
		jsonObjectList.clear();
		
	}
	
	public boolean shouldDisplay() {
		return (jsonObjectList.size() > 0);
	}
	
	private class ListAdapter extends BaseAdapter {

		private ArrayList<JSONObject> objects;
		private Context context;

		public ListAdapter(Context context, ArrayList<JSONObject> objs) {
			super();
			this.objects = objs;
			this.context = context;
		}

		public int getCount() {
			return objects.size();
		}

		public JSONObject getItem(int position){
			return (null == objects) ? null : objects.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				v = LayoutInflater.from(context).inflate(R.layout.history_row, null);
			}
			JSONObject o = objects.get(position);
			//String title = o.optString("isnap_id");
			String clientName = o.optString("clientName");
			String title = o.optString("name");
			String posterUrl = o.optString("poster_url");
			if (o != null) {
				TextView titleTextView = (TextView) v.findViewById(R.id.title);
				if(clientName != null)
				titleTextView.setText(clientName);
				
				TextView clientNameTextView = (TextView)v.findViewById(R.id.viewtime);
				if(title != null)
					clientNameTextView.setText(title);
				
				ImageView posterImageView = (ImageView) v.findViewById(R.id.thumbnail);

				HistoryThumbnailLoader imageLoader = new HistoryThumbnailLoader((CameraActivity) context);
				imageLoader.displayImage(posterUrl, posterImageView);
			}

			return v;
		}
		
		public synchronized void changeJSONObjectList(ArrayList<JSONObject> objs) {
			objects = objs;
			notifyDataSetChanged();
		}
	}
	
	public void setCameraFrame(byte[] mCameraFrame, int mCameraPreviewWidth, int mCameraPreviewHeight) {
		blurBackground = (ImageView) findViewById(R.id.blur_camera_frame);
		
		if (mCameraFrame == null || mCameraPreviewWidth == 0 || mCameraPreviewHeight == 0) {
			blurBackground.setBackgroundColor(0xFFAAAAAA);
		} else {
			YuvImage yuvImg = new YuvImage(mCameraFrame, ImageFormat.NV21, mCameraPreviewWidth, mCameraPreviewHeight, null);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			yuvImg.compressToJpeg(new Rect(0, 0, mCameraPreviewWidth, mCameraPreviewHeight), 100, baos);
			byte[] jpegByteArray = baos.toByteArray();
			Bitmap cameraFrameBitmap = BitmapFactory.decodeByteArray(jpegByteArray, 0, jpegByteArray.length); // bitmap of camera frame in landscape
			Matrix matrix = new Matrix();
			matrix.postRotate(90);
			Bitmap rotatedBmp = Bitmap.createBitmap(cameraFrameBitmap, 0, 0, cameraFrameBitmap.getWidth(), cameraFrameBitmap.getHeight(), matrix, true);
			
			GPUImageGaussianBlurFilter blurFilter = new GPUImageGaussianBlurFilter(2.5f);
			ArrayList<GPUImageFilter> filters = new ArrayList<GPUImageFilter>();
			filters.add(blurFilter);
			GPUImage.getBitmapForMultipleFilters(rotatedBmp, filters, new ResponseListener<Bitmap>() {

				@Override
				public void response(final Bitmap arg0) {
					((Activity) context).runOnUiThread(new Runnable() {
						@Override
						public void run() {
							blurBackground.setImageBitmap(arg0);
						}
					});
				}
			});
		}
	}
	
	public void showMultiARLayout() {
		setClickable(true);
		if (fadeInAnim == null) {
			fadeInAnim = new AlphaAnimation(0.f, 1.f);
			fadeInAnim.setDuration(300);
			fadeInAnim.setFillAfter(true);
			fadeInAnim.setRepeatCount(0);
			fadeInAnim.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
					for (int i = 0; i < getChildCount(); i++) {
						View curChildView = getChildAt(i);
						curChildView.setVisibility(View.VISIBLE);
					}
					setVisibility(View.VISIBLE);
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {}
				
				@Override
				public void onAnimationEnd(Animation animation) {}
			});
		}
		startAnimation(fadeInAnim);
	}
	
	public void hideMultiARLayout() {
		setClickable(false);
		for (int i = 0; i < getChildCount(); i++) {
			View curChildView = getChildAt(i);
			curChildView.setVisibility(View.GONE);
		}
		setVisibility(View.GONE);
	}
}
