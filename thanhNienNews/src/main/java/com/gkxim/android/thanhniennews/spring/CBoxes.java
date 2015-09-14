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
public class CBoxes {
	
	@SerializedName("type")
	private int type;
	@SerializedName("sectionref")
	private String sectionref;
	@SerializedName("title")
	private String title;
	@SerializedName("layout")
	private int layout;
	@SerializedName("count_image")
	private int count_image;
	@SerializedName("boxheightcell")
	private String boxheightcell;
	@SerializedName("boxwidthcell")
	private String boxwidthcell;
	@SerializedName("bgcolor1")
	private String bgcolor1;
	@SerializedName("bgcolor2")
	private String bgcolor2;
	@SerializedName("boxheight")
	private int boxheight;
	@SerializedName("boxwidth")
	private int boxwidth;
	@SerializedName("favourited")
	private boolean favourited;
	@SerializedName("boxindex")
	private int boxindex;
	@SerializedName("storyid")
	private int storyid;
	@SerializedName("video")
	private int video;
	@SerializedName("background")
	private String background;

	@SerializedName("elements")
	private ArrayList<CGalleryElements> elements;
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getLayout() {
		return layout;
	}
	public void setLayout(int layout) {
		this.layout = layout;
	}
	public int getCount_image() {
		return count_image;
	}
	public void setCount_image(int count_image) {
		this.count_image = count_image;
	}
	public String getBoxheightcell() {
		return boxheightcell;
	}
	public void setBoxheightcell(String boxheightcell) {
		this.boxheightcell = boxheightcell;
	}
	public int getBoxheight() {
		return boxheight;
	}
	public void setBoxheight(int boxheight) {
		this.boxheight = boxheight;
	}
	public int getBoxwidth() {
		return boxwidth;
	}
	public void setBoxwidth(int boxwidth) {
		this.boxwidth = boxwidth;
	}
	public int getBoxindex() {
		return boxindex;
	}
	public void setBoxindex(int boxindex) {
		this.boxindex = boxindex;
	}
	public int getStoryid() {
		return storyid;
	}
	public void setStoryid(int storyid) {
		this.storyid = storyid;
	}
	public ArrayList<CGalleryElements> getElements() {
		return elements;
	}
	public void setElements(ArrayList<CGalleryElements> elements) {
		this.elements = elements;
	}
	
}
