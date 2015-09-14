/**
 * File: SectionTemplate.java
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
public class SectionTemplate {

	public static final String CONST_JSON_SECTIONTEMPLATE_ID = "sectionid";

	@SerializedName("sectionid")
	private String sectionId;
	@SerializedName("title")
	private String sectionTitle;
	@SerializedName("sectioncolor")
	private String sectionColorHexString;
	@SerializedName("sectionicon")
	private String sectionIconLink;
	@SerializedName("sectionicon_hover")
	private String sectionIconHoverLink;
	private int sectionColor = -1;
	// FIXME: there has a reverted here with server
	@SerializedName("bgcolor1")
	private String backgroundColor3;
	@SerializedName("bgcolor2")
	private String backgroundColor4;
	@SerializedName("bgcolor3")
	private String backgroundColor1;
	@SerializedName("bgcolor4")
	private String backgroundColor2;

	/**
	 * 08-11-2012
	 */
	public SectionTemplate() {
	}

	public SectionTemplate(String id, String title, String colorHexString) {
		sectionId = id;
		sectionTitle = title;
		sectionColorHexString = colorHexString;
	}

	public SectionTemplate(String id, String title, int color) {
		sectionId = id;
		sectionTitle = title;
		sectionColor = color;
		sectionColorHexString = Integer.toHexString(color);
	}

	public String getSectionId() {
		return sectionId;
	}

	public void setSectionId(String sectionId) {
		this.sectionId = sectionId;
	}

	public String getSectionTitle() {
		return sectionTitle;
	}

	public void setSectionTitle(String sectionTitle) {
		this.sectionTitle = sectionTitle;
	}

	public int getSectionColor() {
		if (sectionColor == -1
				&& (sectionColorHexString != null && sectionColorHexString
						.length() > 0)) {
			try {
				if (sectionColorHexString.length() == 7
						&& sectionColorHexString.startsWith("#")) {
					sectionColorHexString = sectionColorHexString.replace("#",
							"#FF");
				}
				sectionColor = Color.parseColor(sectionColorHexString);
			} catch (Exception e) {
				GKIMLog.lf(null, 0, "failed parsing textcolor: "
						+ sectionColorHexString);
				sectionColor = Color.WHITE;
			}
		}
		return sectionColor;
	}

	public String getSectionColorString() {
		if (sectionColorHexString != null
				&& sectionColorHexString.length() == 7
				&& sectionColorHexString.startsWith("#")) {
			sectionColorHexString = sectionColorHexString.replace("#", "#FF");
		}
		return sectionColorHexString;
	}

	public void setSectionColor(String sectionColorHexString) {
		this.sectionColorHexString = sectionColorHexString;
	}

	public void setSectionColor(int sectionColor) {
		this.sectionColor = sectionColor;
		this.sectionColorHexString = Long.toHexString(sectionColor);
	}

	@Override
	public String toString() {
		return "[sectionId=" + sectionId + ", sectionTitle=" + sectionTitle
				+ ", sectionColor=" + sectionColor + "]";
	}

	public String getSectionIconLink(){
		return sectionIconLink;
	}
	
	public String getSectionIconHoverLink(){
		return sectionIconHoverLink;
	}
	
	/**
	 * @return the SectionColors1 in long[fromColor, toColor]
	 */
	public long[] getSectionColors1() {
//		backgroundColor1 = replaceColorHexChar(backgroundColor1);
//		backgroundColor2 = replaceColorHexChar(backgroundColor2);
		long bgColor1 = parseHexString2Long(backgroundColor1);
		long bgColor2 = parseHexString2Long(backgroundColor2);
		return new long[] { bgColor1, bgColor2 };
	}

	public String[] getSectionColors1String() {
		setSectionColors1(backgroundColor1, backgroundColor2);
		return new String[] { backgroundColor1, backgroundColor2 };
	}

	/**
	 * @return the SectionColors2 in int[fromColor, toColor]
	 */
	public long[] getSectionColors2() {
//		backgroundColor3 = replaceColorHexChar(backgroundColor3);
//		backgroundColor4 = replaceColorHexChar(backgroundColor4);
		long bgColor3 = parseHexString2Long(backgroundColor3);
		long bgColor4 = parseHexString2Long(backgroundColor4);
		return new long[] { bgColor3, bgColor4 };
	}

	public String[] getSectionColors2String() {
		setSectionColors2(backgroundColor3, backgroundColor4);
		return new String[] { backgroundColor3, backgroundColor4 };
	}

	/**
	 * 
	 */
	public void setSectionColors1(String fromColor, String toColor) {
		this.backgroundColor1 = replaceColorHexChar(fromColor);
		this.backgroundColor2 = replaceColorHexChar(toColor);
	}

	/**
	 * 
	 */
	public void setSectionColors2(String fromColor, String toColor) {
		this.backgroundColor3 = replaceColorHexChar(fromColor);
		this.backgroundColor4 = replaceColorHexChar(toColor);
	}

	private String replaceColorHexChar(String colorHexString) {
		if (colorHexString != null && colorHexString.contains("#")) {
			if (colorHexString.length() == 7) {
				return colorHexString.replace("#", "#FF");
			}
		}
		return colorHexString;
	}

	private long parseHexString2Long(String hexString) {
		if (hexString == null) {
			return 0;
		}
		long result = 0;
		String colorAlpha = hexString;
		int len = hexString.length();
		if (len == 7) {
			colorAlpha = hexString.replace("#", "FF");
		}else if (len == 9) {
			colorAlpha = hexString.replace("#", "");
		}else if (len < 7) {
			colorAlpha = "FF" + hexString;
		} 
		try {
			result = Long.parseLong(colorAlpha, 16);
		} catch (NumberFormatException e) {
			GKIMLog.lf(
					null,
					0,
					"Parsing color text failed on SectionTemplate: "
							+ e.getMessage());
		}
		return result;
	}

}
