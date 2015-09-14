package com.knx.framework.videoplayer;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.knx.framework.R;
import com.knx.framework.helper.ARiseConfigs;

public class MP4VideoPlayer extends Activity {
	
    private ProgressBar progressBar;
    private String url;
    private boolean autoplay;
    private boolean callback;
    private int delay = 0;
    private Timer timer;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.videoplayer_mp4);
        url = getIntent().getExtras().getString("url");
        autoplay = getIntent().getExtras().getBoolean("autoplay");
        callback = getIntent().getExtras().getBoolean("callback");
        delay = getIntent().getExtras().getInt("delay");
        showVideo();
    }

    private void showVideo() {
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        VideoView vd = (VideoView) findViewById(R.id.videoview);
        Uri uri;
        if (!url.startsWith("file:///")) {
        	uri = Uri.parse(url);
        } else {
        	String fileNameWithExtension = url.substring(8);
        	String fileNameWithoutExtension;
        	final int lastPeriodPos = fileNameWithExtension.lastIndexOf(".");
        	if (lastPeriodPos <= 0) {
        		fileNameWithoutExtension = fileNameWithExtension;
        	} else {
        		fileNameWithoutExtension = fileNameWithExtension.substring(0, lastPeriodPos);
        	}
        	uri = Uri.parse("android.resource://" + ARiseConfigs.PACKAGE_NAME + "/raw/" + fileNameWithoutExtension);
        }
        MediaController mc = new MediaController(this) {
        	// added by Hung, date: 22-04-2014
        	public boolean dispatchKeyEvent(KeyEvent event) {
        		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
        			((Activity) getContext()).finish();

        		return super.dispatchKeyEvent(event);
        	}
        };
        vd.setMediaController(mc);
        vd.setVideoURI(uri);
        
        if (autoplay)
            vd.start();
        
        if (callback && delay > 0) {
        	timer = new Timer();
        	timer.schedule(new BackTask(), 1000 * delay);
        }
        
        vd.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (callback)
                    finish();
            }
        });
        
        vd.setOnErrorListener(new ModifiedErrorListener());

        vd.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                //To change body of implemented methods use File | Settings | File Templates.
                progressBar.setVisibility(View.GONE);
            }
        });
    }
    
    private class ModifiedErrorListener implements MediaPlayer.OnErrorListener {

		@Override
		public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
//			progressBar.setVisibility(View.GONE);
//			String what, errorDescription;
//			Log.e(TAG, "Error while playing MP4. " + arg1 + ", " + arg2);
//			
//			switch (arg1) {
//				case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
//					what = "Media server died";
//					break;
//				case MediaPlayer.MEDIA_ERROR_UNKNOWN:
//					what = "Unspecified media player error";
//					break;
//				default:
//					what = "Unknown";
//					break;
//			}
//			if (arg1 != MediaPlayer.MEDIA_ERROR_UNKNOWN) {
//				switch (arg2) {
//					case MediaPlayer.MEDIA_ERROR_IO:
//						errorDescription = "I/O error";
//						break;
//					case MediaPlayer.MEDIA_ERROR_MALFORMED:
//						errorDescription = "Malformed error";
//						break;
//					case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
//						errorDescription = "Unsupported format";
//						break;
//					case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
//						errorDescription = "Time out";
//						break;
//					default:
//						errorDescription = "Unknown";
//						break;
//				}
//			} else {
//				errorDescription = "";
//			}
//			String message = what + "\n" + errorDescription;
//			
//			new AlertDialog.Builder(MP4VideoPlayer.this)
//				.setTitle("ERROR")
//				.setMessage(message)
//				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//					
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						finish();
//					}
//				})
//				.setCancelable(false)
//				.show();
			
			return true;
		}
    	
    }
    
    /** hungnm - date:20131023 - begin */
    //task to be implemented
    private class BackTask extends TimerTask {

        @Override
        public void run() {
            finish();
        }
    }
    /** hungnm - date:20131023 - end */
}