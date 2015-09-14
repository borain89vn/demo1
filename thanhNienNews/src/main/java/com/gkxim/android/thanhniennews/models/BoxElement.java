/**
 * File: BoxElement.java
 * Creator: Timon.Trinh (timon@gkxim.com)
 * Date: 08-11-2012
 * 
 */
package com.gkxim.android.thanhniennews.models;

import android.graphics.Color;

import com.gkxim.android.utils.GKIMLog;
import com.google.gson.annotations.SerializedName;

/**
 * @author Timon Trinh
 */
public class BoxElement {

	public static final int BOXELEMENT_TYPE_TITLE = 1;
	public static final int BOXELEMENT_TYPE_SHORTCONTENT = 2;
	public static final int BOXELEMENT_TYPE_IMAGE = 3;

	@SerializedName("weatherimg")
	private String weatherimg;
	@SerializedName("weathercontent")
	private String weathercontent;
	@SerializedName("type")
	private int type;
	@SerializedName("content")
	private String content;
	@SerializedName("widthcell")
	private int widthCell;
	@SerializedName("aligninbox")
	private int alignmentInBox;
	@SerializedName("texttype")
	private int textType;
	@SerializedName("size")
	private int textSize;
	@SerializedName("color")
	private String textColorHexString;
	private int textColor = -1;

	/**
	 * 08-11-2012
	 */
	public BoxElement() {
		// Non-agrument contructor suport for Gson serialization
	}

	public BoxElement(int t, String con, int align) {
		type = t;
		content = con;
		alignmentInBox = align;
		widthCell = 1;
	}
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getWidthCell() {
		return widthCell;
	}

	public void setWidthCell(int widthCell) {
		this.widthCell = widthCell;
	}

	public int getAlignmentInBox() {
		return alignmentInBox;
	}

	public void setAlignmentInBox(int alignmentInBox) {
		this.alignmentInBox = alignmentInBox;
	}

	public int getTextType() {
		return textType;
	}

	public void setTextType(int textType) {
		this.textType = textType;
	}

	public int getTextSize() {
		return textSize;
	}

	public void setTextSize(int textSize) {
		this.textSize = textSize;
	}

	public int getTextColor() {
		if (textColor == -1 && textColorHexString != null) {
			try {
				if (textColorHexString.length() == 7 
						&& textColorHexString.startsWith("#")) {
					textColorHexString = textColorHexString.replace("#", "#FF");
				}
				textColor = Color.parseColor(textColorHexString);
			} catch (Exception e) {
				GKIMLog.lf(null, 0, "failed parsing textcolor: " + textColorHexString);
				textColor = Color.WHITE;
			}
		}
		return textColor;
	}

	public void setTextColor(int textColor) {
		this.textColor = textColor;
	}
	
	public String getWeatherimg() {
		return weatherimg;
	}
	
	public String getWeathercontent() {
		return weathercontent;
	}
}
