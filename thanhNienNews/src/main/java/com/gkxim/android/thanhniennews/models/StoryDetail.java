/**
 * File: StoryDetail.java
 * Creator: Timon.Trinh (timon@gkxim.com)
 * Date: 02-01-2013
 * 
 */
package com.gkxim.android.thanhniennews.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StoryDetail {

	@SerializedName("storyid")
	private int storyid;
	@SerializedName("storydate")
	private long storydate;
	@SerializedName("storydatetext")
	private String storydatetext;
	@SerializedName("author")
	private String author;
	@SerializedName("sectionid")
	private String sectionid;
	@SerializedName("sectiontitle")
	private String sectiontitle;
	@SerializedName("storytitle")
	private String storytitle;
	@SerializedName("wap_story_url")
	private String wapurl;
	@SerializedName("htmlcontent")
	private String htmlcontent;
	@SerializedName("totalcomments")
	private int totalcomments;
	@SerializedName("fblike_count")
	private int fblikecount;
	@SerializedName("images")
	private JsonElement jaImages;
	@SerializedName("videos")
	private JsonElement jaVideos;
    @SerializedName("pdf")
	private JsonElement jaPDFs;
	@SerializedName("imageFirst")
	private boolean imageFirst;
	@SerializedName("featured_image")
	private String featured_image;
	@Expose
	private String mTopImageUrl;

	
	public String getTopImageUrl() {
		return mTopImageUrl;
	}

	public void setTopImageUrl(String mTopImageUrl) {
		this.mTopImageUrl = mTopImageUrl;
	}

	public int getStoryid() {
		return storyid;
	}

	public void setStoryid(int storyid) {
		this.storyid = storyid;
	}

	public long getStorydate() {
		return storydate;
	}

	public void setStorydate(long storydate) {
		this.storydate = storydate;
	}

	public String getStorydatetext() {
		return storydatetext;
	}

	public void setStorydatetext(String storydatetext) {
		this.storydatetext = storydatetext;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getSectionid() {
		return sectionid;
	}

	public void setSectionid(String sectionid) {
		this.sectionid = sectionid;
	}

	public String getSectiontitle() {
		return sectiontitle;
	}

	public void setSectiontitle(String sectiontitle) {
		this.sectiontitle = sectiontitle;
	}

	public String getStorytitle() {
		return storytitle;
	}

	public void setStorytitle(String storytitle) {
		this.storytitle = storytitle;
	}

	public String getWapurl() {
		return wapurl;
	}

	public void setWapurl(String wapurl) {
		this.wapurl = wapurl;
	}

	public String getHtmlcontent() {
		return htmlcontent;
	}

	public void setHtmlcontent(String htmlcontent) {
		this.htmlcontent = htmlcontent;
	}

	public int getTotalcomments() {
		return totalcomments;
	}

	public void setTotalcomments(int totalcomments) {
		this.totalcomments = totalcomments;
	}

	public int getFblikecount() {
		return fblikecount;
	}

	public void setFblikecount(int fblikecount) {
		this.fblikecount = fblikecount;
	}

	public JsonArray getJaImages() {
		if (jaImages != null && jaImages.isJsonArray()) {
			return jaImages.getAsJsonArray();
		}
		return null;
	}

	public void setJaImages(JsonElement jaImages) {
		this.jaImages = jaImages;
	}

	public JsonArray getJaVideos() {
		if (jaVideos != null && jaVideos.isJsonArray()) {
			return jaVideos.getAsJsonArray();
		}
		return null;
	}

	public void setJaVideos(JsonElement jaVideos) {
		this.jaVideos = jaVideos;
	}

    public JsonArray getJaPDFs() {
        if (jaPDFs != null && jaPDFs.isJsonArray()) {
            return jaPDFs.getAsJsonArray();
        }
        return null;
    }

    public void setJaPDFs(JsonElement jaPDFs) {
        this.jaPDFs = jaPDFs;
    }

	public boolean isImageFirst() {
		return imageFirst;
	}

	public void setImageFirst(boolean imageFirst) {
		this.imageFirst = imageFirst;
	}

	public String getFeatured_image() {
		return featured_image;
	}

	public void setFeatured_image(String featured_image) {
		this.featured_image = featured_image;
	}

}
