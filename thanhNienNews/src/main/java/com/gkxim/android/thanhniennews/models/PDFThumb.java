package com.gkxim.android.thanhniennews.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PDFThumb implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	// Key Json
	public static final String KEY_JSON_ID = "id";
	public static final String KEY_JSON_TYPE = "type";
	public static final String KEY_JSON_URL = "url";
	public static final String KEY_JSON_CAPTION = "caption";

	@SerializedName(KEY_JSON_ID)
	private String Id;
	@SerializedName(KEY_JSON_TYPE)
	private String Type;
	@SerializedName(KEY_JSON_URL)
	private String Url;
	@SerializedName(KEY_JSON_CAPTION)
	private String Caption;

	public String getId() {
		return Id;
	}

	public void setId(String id) {
		Id = id;
	}

	public String getType() {
		return Type;
	}

	public void setType(String type) {
		Type = type;
	}

	public String getUrl() {
		return Url;
	}

	public void setUrl(String url) {
		Url = url;
	}

	public String getCaption() {
		return Caption;
	}

	public void setCaption(String caption) {
		Caption = caption;
	}
}
