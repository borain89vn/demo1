/**
 * @author Nam.Nguyen
 * @Date:Jan 16, 2014
 */
package com.gkxim.android.thanhniennews.spring;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;

/**
 * @author Nam.Nguyen
 * 
 */
public class CGalleryData {
	@SerializedName("issueid")
	private String issueid;
	@SerializedName("issuedate")
	private long issuedate;
	@SerializedName("issuedatestring")
	private String issuedatestring;
	@SerializedName("layoutwidth")
	private int layoutwidth;
	@SerializedName("boxwidth")
	private int boxwidth;
	@SerializedName("gapwidth")
	private int gapwidth;
	@SerializedName("boxes")
	private ArrayList<CBoxes> boxes;
	@SerializedName("sectionid")
	private String sectionid;
	@SerializedName("sectiontitle")
	private String sectiontitle;
	@SerializedName("title")
	private String title;
	
	
	public int getLayoutwidth() {
		return layoutwidth;
	}
	public void setLayoutwidth(int layoutwidth) {
		this.layoutwidth = layoutwidth;
	}
	public int getBoxwidth() {
		return boxwidth;
	}
	public void setBoxwidth(int boxwidth) {
		this.boxwidth = boxwidth;
	}
	public int getGapwidth() {
		return gapwidth;
	}
	public void setGapwidth(int gapwidth) {
		this.gapwidth = gapwidth;
	}
	public ArrayList<CBoxes> getBoxes() {
		return boxes;
	}
	public void setBoxes(ArrayList<CBoxes> boxes) {
		this.boxes = boxes;
	}
	public String getIssueid() {
		return issueid;
	}
	public void setIssueid(String issueid) {
		this.issueid = issueid;
	}
	public long getIssuedate() {
		return issuedate;
	}
	public void setIssuedate(long issuedate) {
		this.issuedate = issuedate;
	}
	public String getIssuedatestring() {
		return issuedatestring;
	}
	public void setIssuedatestring(String issuedatestring) {
		this.issuedatestring = issuedatestring;
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
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
}
