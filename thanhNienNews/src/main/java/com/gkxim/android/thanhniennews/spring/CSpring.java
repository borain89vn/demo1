/**
 * @author Nam.Nguyen
 * @Date:Jan 14, 2014
 */
package com.gkxim.android.thanhniennews.spring;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;

/**
 * @author Nam.Nguyen
 * 
 */
public class CSpring {
	@SerializedName("resultCode")
	private String resultCode;
	@SerializedName("result")
	private String result;
	@SerializedName("requesteddate")
	private long requesteddate;
	@SerializedName("totalpage")
	private int totalpage;
	@SerializedName("pagesize")
	private int pagesize;
	@SerializedName("data")
	private ArrayList<CSpringData> data;
	@SerializedName("sectiontitle")
	private String sectiontitle;
	@SerializedName("title")
	private String title;
	@SerializedName("storyid")
	private String storyid;

	@SerializedName("fblike_count")
	private int fblike_count;
	@SerializedName("wap_story_url")
	private String wap_story_url;
	

	public String getResultCode() {
		return resultCode;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public long getRequesteddate() {
		return requesteddate;
	}

	public void setRequesteddate(long requesteddate) {
		this.requesteddate = requesteddate;
	}

	public int getTotalpage() {
		return totalpage;
	}

	public void setTotalpage(int totalpage) {
		this.totalpage = totalpage;
	}

	public int getPagesize() {
		return pagesize;
	}

	public void setPagesize(int pagesize) {
		this.pagesize = pagesize;
	}

	public ArrayList<CSpringData> getData() {
		return data;
	}

	public void setData(ArrayList<CSpringData> data) {
		this.data = data;
	}

	public String getSectiontitle() {
		return sectiontitle;
	}

	public void setSectiontitle(String sectiontitle) {
		this.sectiontitle = sectiontitle;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getStoryid() {
		return storyid;
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

}
