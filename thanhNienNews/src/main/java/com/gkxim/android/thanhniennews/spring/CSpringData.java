/**
 * @author Nam.Nguyen
 * @Date:Jan 14, 2014
 */
package com.gkxim.android.thanhniennews.spring;

import com.google.gson.annotations.SerializedName;

/**
 * @author Nam.Nguyen
 * 
 */
public class CSpringData {
	@SerializedName("icon_url")
	private String icon_url;
	@SerializedName("sep_url")
	private String sep_url;
	@SerializedName("comment_date")
	private String comment_date;
	@SerializedName("name")
	private String name;
	@SerializedName("comment")
	private String comment;
	public String getIcon_url() {
		return icon_url;
	}
	public void setIcon_url(String icon_url) {
		this.icon_url = icon_url;
	}
	public String getSep_url() {
		return sep_url;
	}
	public void setSep_url(String sep_url) {
		this.sep_url = sep_url;
	}
	public String getComment_date() {
		return comment_date;
	}
	public void setComment_date(String comment_date) {
		this.comment_date = comment_date;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	
}
