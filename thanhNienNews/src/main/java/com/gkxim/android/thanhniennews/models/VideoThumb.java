package com.gkxim.android.thanhniennews.models;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

public class VideoThumb implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5272493878518428713L;
	// Key Json
	public static final String KEY_JSON_VIDEO_THUMB = "video_thumb";
	public static final String KEY_JSON_VIDEO_URL = "video_url";
	public static final String KEY_JSON_CAPTION = "caption";
	public static final String KEY_JSON_TYPE = "type";

	@SerializedName(KEY_JSON_VIDEO_THUMB)
	private String VideoThumb;
	@SerializedName(KEY_JSON_VIDEO_URL)
	private String VideoUrl;
	@SerializedName(KEY_JSON_CAPTION)
	private String Caption;
	@SerializedName(KEY_JSON_TYPE)
	private String Type;

	public String getVideoThumb() {
		return VideoThumb;
	}

	public void setVideoThumb(String videoThumb) {
		VideoThumb = videoThumb;
	}

	public String getVideoUrl() {
		return VideoUrl;
	}

	public void setVideoUrl(String videoUrl) {
		VideoUrl = videoUrl;
	}

	public String getCaption() {
		return Caption;
	}

	public void setCaption(String caption) {
		Caption = caption;
	}

	public String getType() {
		return Type;
	}

	public void setType(String type) {
		Type = type;
	}

}
