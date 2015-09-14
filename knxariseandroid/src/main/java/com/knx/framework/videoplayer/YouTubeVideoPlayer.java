/*
 * Copyright 2012 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.knx.framework.videoplayer;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.ErrorReason;
import com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener;
import com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener;
import com.google.android.youtube.player.YouTubePlayer.PlayerStyle;
import com.google.android.youtube.player.YouTubePlayer.PlaylistEventListener;
import com.google.android.youtube.player.YouTubePlayerView;
import com.knx.framework.R;
import com.knx.framework.helper.ARiseConfigs;

/**
 * A simple YouTube Android API demo application demonstrating the use of {@link YouTubePlayer}
 * programmatic controls.
 */
public class YouTubeVideoPlayer extends YouTubeFailureRecoveryActivity {

    private YouTubePlayerView youTubePlayerView;
    private YouTubePlayer youtubePlayer;

    private MyPlaylistEventListener playlistEventListener;
    private MyPlayerStateChangeListener playerStateChangeListener;
    private MyPlaybackEventListener playbackEventListener;
    private Timer timer;
    
    private boolean autoplay;
    private boolean callback;
    private int delay = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.videoplayer_youtube);

        youTubePlayerView = (YouTubePlayerView) findViewById(R.id.youtube_view);
        youTubePlayerView.initialize(ARiseConfigs.DEVELOPER_KEY, this);

        playlistEventListener = new MyPlaylistEventListener();
        playerStateChangeListener = new MyPlayerStateChangeListener();
        playbackEventListener = new MyPlaybackEventListener();
        
        autoplay = getIntent().getExtras().getBoolean("autoplay");
        callback = getIntent().getExtras().getBoolean("callback");
        delay = getIntent().getExtras().getInt("delay");
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
                                        boolean wasRestored) {
    	youtubePlayer = player;
    	youtubePlayer.setPlaylistEventListener(playlistEventListener);
    	youtubePlayer.setPlayerStateChangeListener(playerStateChangeListener);
    	youtubePlayer.setPlaybackEventListener(playbackEventListener); // error Intent receiver
    	youtubePlayer.setPlayerStyle(PlayerStyle.DEFAULT);
    	
    	youtubePlayer.setFullscreenControlFlags(YouTubePlayer.FULLSCREEN_FLAG_CONTROL_ORIENTATION);
    	youtubePlayer.setFullscreen(true);
        
        if (!wasRestored) {
	        if (autoplay) {
	        	youtubePlayer.loadVideo(getIntent().getExtras().getString("videoId"));
	        } else{
	        	youtubePlayer.cueVideo(getIntent().getExtras().getString("videoId"));
	        }
        }
    }

    @Override
    protected YouTubePlayer.Provider getYouTubePlayerProvider() {
        return youTubePlayerView;
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private final class MyPlaylistEventListener implements PlaylistEventListener {
    	
        @Override
        public void onNext() {

        }

        @Override
        public void onPrevious() {

        }

        @Override
        public void onPlaylistEnded() {

        }
    }

    private class MyPlaybackEventListener implements PlaybackEventListener {
    	
    	@Override
        public void onPlaying() {
        	if (timer == null) {
        		// start timer
                if (callback && delay > 0) {
    	        	timer = new Timer();
    	        	timer.schedule(new TimerTask() {
    	        		@Override
    	        		public void run() {
    	        			finish();
    	        		}
    	        	}, delay * 1000);
    	        }
        	}
        }

        @Override
        public void onBuffering(boolean isBuffering) {

        }

        @Override
        public void onStopped() {

        }

        @Override
        public void onPaused() {

        }

        @Override
        public void onSeekTo(int endPositionMillis) {

        }
    }

    private final class MyPlayerStateChangeListener implements PlayerStateChangeListener {

        @Override
        public void onLoading() {

        }

        @Override
        public void onLoaded(String videoId) {

        }

        @Override
        public void onAdStarted() {

        }

        @Override
        public void onVideoStarted() {

        }

        @Override
        public void onVideoEnded() {
            if (callback && delay <= 0)
            	finish();
        }

        @Override
        public void onError(ErrorReason reason) {
            if (reason == ErrorReason.UNEXPECTED_SERVICE_DISCONNECTION) {
                // When this error occurs the player is released and can no longer be used.
            }
        }
    }
}
