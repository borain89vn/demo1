/**
 * @author Nam.Nguyen
 * @Date:Jan 16, 2014
 */
package com.gkxim.android.thanhniennews.spring;

import com.google.gson.annotations.SerializedName;

/**
 * @author Nam.Nguyen
 * 
 */
public class CGallery {
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
	@SerializedName("userid")
	private String userid;
	@SerializedName("fblike_count")
	private int fblike_count;
	@SerializedName("wap_story_url")
	private String wap_story_url;
	@SerializedName("background")
	private String background;
	@SerializedName("data")
	private CGalleryData data;

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

	public String getBackground() {
		return background;
	}

	public void setBackground(String background) {
		this.background = background;
	}

	public CGalleryData getData() {
		return data;
	}

	public void setData(CGalleryData data) {
		this.data = data;
	}

}
