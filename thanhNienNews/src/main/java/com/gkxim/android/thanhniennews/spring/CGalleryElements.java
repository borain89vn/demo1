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
public class CGalleryElements {
	@SerializedName("type")
	private int type;
	@SerializedName("content")
	private String content;
	@SerializedName("widthcell")
	private int widthcell;
	@SerializedName("aligninbox")
	private int aligninbox;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getWidthcell() {
		return widthcell;
	}

	public void setWidthcell(int widthcell) {
		this.widthcell = widthcell;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getAligninbox() {
		return aligninbox;
	}

	public void setAligninbox(int aligninbox) {
		this.aligninbox = aligninbox;
	}

}
