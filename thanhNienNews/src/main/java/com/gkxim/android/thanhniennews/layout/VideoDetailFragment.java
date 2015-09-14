package com.gkxim.android.thanhniennews.layout;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.VideoView;

import com.gkim.thanhniennews.R;
import com.gkxim.android.thanhniennews.StoryDetailVideoPlayActivity;
import com.gkxim.android.thanhniennews.models.VideoThumb;
import com.gkxim.android.utils.GKIMLog;
import com.gkxim.android.utils.UIUtils;

public class VideoDetailFragment extends Fragment {
	private static final String TAG = VideoDetailFragment.class.getSimpleName();
	private final int LOADING = 0;
	private final int PLAYING = 1;
	private final int PAUSE = 2;
	private final int STOP = 3;
	private final int RESUND = 4;

	private final int TIME_DISPLAY = 50;
	private Activity mActivity;
	private VideoThumb mVideoThumb;
	private VideoView mVideoView;
	// Handler to update UI timer, progress bar etc,.
	private Handler mHandler = new Handler();;
	private FrameLayout mControl;
	private LinearLayout loadingData;
	private boolean mControllerShowingStatus = false;
	private TextView mTxtTimeStart;
	private TextView mTxtTimeTotal;
	private ImageView btClose;
	private SeekBar mSeekbar;
	private Button btPlayPause;
	private static int iStatusPlay = -1;
	private Handler mMessageHandler = new Handler() {
		public void handleMessage(Message paramAnonymousMessage) {
			showVideo();
			switch (paramAnonymousMessage.what) {
			default:
				return;
			case 1:
				hideController();
				return;
			}
		}
	};

	public VideoDetailFragment() {

	}

	public void setVideoThumb(VideoThumb videoThumb) {
		mVideoThumb = videoThumb;
		GKIMLog.l(1, TAG + " url video :" + mVideoThumb.getVideoUrl());
	}

	@Override
	public void onAttach(Activity activity) {
		GKIMLog.lf(getActivity(), 1, TAG + "=>onAttach");
		super.onAttach(activity);
		mActivity = activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		GKIMLog.lf(getActivity(), 1, TAG + "=>onCreate");
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		GKIMLog.lf(getActivity(), 1, TAG + "=>onCreateView");
		View result = initViewfragment(inflater);
		return result;
	}

	@Override
	public void onStart() {
		GKIMLog.lf(getActivity(), 1, TAG + "=>onStart");
		super.onStart();
	}

	@Override
	public void onResume() {
		GKIMLog.lf(getActivity(), 1, TAG + "=>onResume");
		super.onResume();
		if (this.mVideoView != null) {
			this.mVideoView.start();
			btPlayPause.setBackgroundResource(R.drawable.bt_widget_pause_nor);
			hideController();
		}
	}

	@Override
	public void onPause() {
		GKIMLog.lf(getActivity(), 1, TAG + "=>onPause");
		super.onPause();

		if ((this.mVideoView != null) && (this.mVideoView.isPlaying())) {
			this.mVideoView.stopPlayback();
			iStatusPlay = PAUSE;
			btPlayPause.setBackgroundResource(R.drawable.bt_widget_play_nor);
		}
	}

	@Override
	public void onStop() {
		GKIMLog.lf(getActivity(), 1, TAG + "=>onStop");
		super.onStop();
	}

	@Override
	public void onDestroy() {
		GKIMLog.lf(getActivity(), 1, TAG + "=>onDestroy");
		super.onDestroy();
		if (this.mVideoView != null) {
			this.mVideoView.stopPlayback();
			this.mVideoView = null;
		}
		if (this.mMessageHandler != null) {
			this.mMessageHandler.removeCallbacksAndMessages(null);
		}
	}

	private View initViewfragment(LayoutInflater inflater) {
		ViewGroup result = (ViewGroup) (inflater.inflate(
				R.layout.frag_review_video, null));
		mVideoView = (VideoView) result.findViewById(R.id.videoView);
		mControl = (FrameLayout) result.findViewById(R.id.controlContainer);
		loadingData = (LinearLayout) result.findViewById(R.id.loadingData);
		mTxtTimeStart = (TextView) result.findViewById(R.id.startTime);
		mTxtTimeTotal = (TextView) result.findViewById(R.id.endTime);
		mSeekbar = (SeekBar) result.findViewById(R.id.timeSeekbar);
		mSeekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		mVideoView.setOnPreparedListener(mOnPreparedListener);
		mVideoView.setOnTouchListener(mOnTouchListener);
		mVideoView.setOnCompletionListener(mOnCompletionListener);
		btPlayPause = (Button) result.findViewById(R.id.bt_play_pause);
		btPlayPause.setOnClickListener(mOnClickListener);
		btClose = (ImageView) result.findViewById(R.id.bt_video_close);
		btClose.setOnClickListener(mOnClickListener);
		showLoading();
		playMV();
		return result;
	}

	private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener = new OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			mHandler.removeCallbacks(mUpdateTimeTask);
			int totalDuration = mVideoView.getDuration();
			int currentPosition = UIUtils.progressToTimer(
					seekBar.getProgress(), totalDuration);

			// forward or backward to certain seconds
			mVideoView.seekTo(currentPosition);

			// update timer progress again
			updateProgressBar();
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			// remove message Handler from updating progress bar
			mHandler.removeCallbacks(mUpdateTimeTask);
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub

		}
	};
	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.bt_play_pause) {
				if (iStatusPlay == PLAYING) {
					btPlayPause
							.setBackgroundResource(R.drawable.bt_widget_play_nor);
					mVideoView.pause();
					iStatusPlay = PAUSE;
				} else if (iStatusPlay == PAUSE) {
					mVideoView.start();
					btPlayPause
							.setBackgroundResource(R.drawable.bt_widget_pause_nor);
					iStatusPlay = PLAYING;

				}

			} else if (v.getId() == R.id.bt_video_close) {
				onDestroy();
				StoryDetailVideoPlayActivity storyDetailVideoPlayActivity = (StoryDetailVideoPlayActivity) getActivity();
				if (storyDetailVideoPlayActivity != null)
					storyDetailVideoPlayActivity.closeVideo();
			}
		}
	};
	private OnCompletionListener mOnCompletionListener = new OnCompletionListener() {

		@Override
		public void onCompletion(MediaPlayer mp) {
			// TODO Auto-generated method stub
			onDestroy();
			StoryDetailVideoPlayActivity storyDetailVideoPlayActivity = (StoryDetailVideoPlayActivity) getActivity();
			if (storyDetailVideoPlayActivity != null)
				storyDetailVideoPlayActivity.closeVideo();
		}
	};
	private OnPreparedListener mOnPreparedListener = new OnPreparedListener() {

		@Override
		public void onPrepared(MediaPlayer mp) {
			// TODO Auto-generated method stub
			mMessageHandler.sendEmptyMessage(0);
			mMessageHandler.sendEmptyMessageDelayed(1, 100L);
			// set Progress bar values
			mSeekbar.setProgress(0);
			mSeekbar.setMax(100);

			// Updating progress bar
			updateProgressBar();
			iStatusPlay = PLAYING;
			btPlayPause.setBackgroundResource(R.drawable.bt_widget_pause_nor);
		}
	};

	private OnTouchListener mOnTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent paramMotionEvent) {
			// TODO Auto-generated method stub
			if (paramMotionEvent.getAction() == 0) {
				if (!mControllerShowingStatus) {
					showControllerAutoTurnoff();
					return true;
				} else {
					mMessageHandler.removeCallbacksAndMessages(null);
					hideController();
				}
			}
			return false;
		}
	};

	private void hideController() {
		this.mControllerShowingStatus = false;
		this.mControl.setVisibility(View.GONE);
	}

	/**
	 * 
	 */
	protected void updateProgressBar() {
		// TODO Auto-generated method stub
		mHandler.postDelayed(mUpdateTimeTask, TIME_DISPLAY);
	}

	private void showController() {
		this.mMessageHandler.removeMessages(1);
		this.mControllerShowingStatus = true;
		this.mControl.setVisibility(View.VISIBLE);
	}

	private void showVideo() {
		loadingData.setVisibility(View.GONE);
		hideController();
	}

	private void showLoading() {
		this.loadingData.setVisibility(View.VISIBLE);
		hideController();
	}

	private void showControllerAutoTurnoff() {
		this.mControllerShowingStatus = true;
		this.mControl.setVisibility(View.VISIBLE);
		this.mMessageHandler.sendEmptyMessageDelayed(1, 2000L);
	}

	private void playMV() {
		if ((this.mVideoView != null) && (this.mVideoView.isPlaying())) {
			this.mVideoView.stopPlayback();
		}
		try {

			if (mVideoThumb != null) {
				Uri localUri = Uri.parse(mVideoThumb.getVideoUrl());
				iStatusPlay = LOADING;
				mVideoView.setVideoURI(localUri);
				mVideoView.start();
				return;
			}

		} catch (NullPointerException localNullPointerException) {
			localNullPointerException.printStackTrace();
			return;
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	/**
	 * Background Runnable thread
	 * */
	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			if (mVideoView != null) {
				long totalDuration = mVideoView.getDuration();
				long currentDuration = mVideoView.getCurrentPosition();
				mTxtTimeStart.setText(UIUtils
						.milliSecondsToTimer(totalDuration));
				mTxtTimeTotal.setText(UIUtils
						.milliSecondsToTimer(currentDuration));
				// Updating progress bar
				int progress = (int) (UIUtils.getProgressPercentage(
						currentDuration, totalDuration));
				mSeekbar.setProgress(progress);

				// Running this thread after 100 milliseconds
				mHandler.postDelayed(this, TIME_DISPLAY);
			}
		}
	};
}
