/**
 * @author Nam.Nguyen
 * @Date:Jan 16, 2014
 */
package com.gkxim.android.thanhniennews.spring;

import java.util.ArrayList;

import com.gkxim.android.thanhniennews.models.ImageThumb;
import com.google.gson.annotations.SerializedName;

/**
 * @author Nam.Nguyen
 * 
 */
public class CImageData {
	@SerializedName("section_id")
	private String section_id;
	@SerializedName("section_name")
	private String section_name;
	@SerializedName("section_slug")
	private String section_slug;
	@SerializedName("storyid")
	private String storyid;
	@SerializedName("storydatetext")
	private String storydatetext;
	@SerializedName("fblike_count")
	private int fblike_count;
	@SerializedName("wap_story_url")
	private String wap_story_url;
	@SerializedName("images")
	private ArrayList<ImageThumb> images;

	public String getSection_id() {
		return section_id;
	}

	public void setSection_id(String section_id) {
		this.section_id = section_id;
	}

	public String getSection_name() {
		return section_name;
	}

	public void setSection_name(String section_name) {
		this.section_name = section_name;
	}

	public String getSection_slug() {
		return section_slug;
	}

	public void setSection_slug(String section_slug) {
		this.section_slug = section_slug;
	}

	public String getStoryid() {
		return storyid;
	}

	public void setStoryid(String storyid) {
		this.storyid = storyid;
	}

	public String getStorydatetext() {
		return storydatetext;
	}

	public void setStorydatetext(String storydatetext) {
		this.storydatetext = storydatetext;
	}

	public int getFblike_count() {
		return fblike_count;
	}

	public void setFblike_count(int fblike_count) {
		this.fblike_count = fblike_count;
	}

	public String getWap_story_url() {
		return wap_story_url;
	}

	public void setWap_story_url(String wap_story_url) {
		this.wap_story_url = wap_story_url;
	}

	public ArrayList<ImageThumb> getImages() {
		return images;
	}

	public void setImages(ArrayList<ImageThumb> images) {
		this.images = images;
	}
}
