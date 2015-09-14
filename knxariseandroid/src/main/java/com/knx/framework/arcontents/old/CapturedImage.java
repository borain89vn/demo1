package com.knx.framework.arcontents.old;

public class CapturedImage {

	private String link;
	private String asset;
	private int height;
	private int width;
	private int column;
	private int row;
	private int type;
	private String widget;
	private int urlOpenType;
	
	public int getUrlOpenType() {
		return urlOpenType;
	}

	public void setUrlOpenType(int urlOpenType) {
		this.urlOpenType = urlOpenType;
	}

	public int getType() {
		return type;
	}

	public int getCol() {
		return column;
	}

	public int getRow() {
		return row;
	}

	public void setCol(int col) {
		this.column = col;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getAsset() {
		return asset;
	}

	public void setAsset(String asset) {
		this.asset = asset;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public String getWidget() {
		return this.widget;
	}

	public CapturedImage(String link, String asset, int width, int height,
			int row, int col, int type) {
		super();
		this.link = link;
		this.asset = asset;
		this.height = height;
		this.width = width;
		this.type = type;
		this.column = col;
		this.row = row;

		if (this.type == 1) {
			this.widget = "button";
		} else if (this.type == 2) {
			this.widget = "webform";
		} else if (this.type == 3) {
			this.widget = "banner";
		} else if (this.type == 4) {
			this.widget = "video";
		}
	}

	public CapturedImage(String link, String asset, int width, int height,
			int row, int col, int type, int urlOpenType) {
		super();
		this.link = link;
		this.asset = asset;
		this.height = height;
		this.width = width;
		this.type = type;
		this.column = col;
		this.row = row;
		this.urlOpenType = urlOpenType;
		
		if (this.type == 1) {
			this.widget = "button";
		} else if (this.type == 2) {
			this.widget = "webform";
		} else if (this.type == 3) {
			this.widget = "banner";
		} else if (this.type == 4) {
			this.widget = "video";
		}
	}	
}
