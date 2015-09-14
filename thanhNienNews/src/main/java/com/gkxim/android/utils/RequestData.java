/**
 * File: RequestData.java
 * Creator: Timon.Trinh (timon@gkxim.com)
 * Date: 04-12-2012
 * 
 */
package com.gkxim.android.utils;

public class RequestData extends Object {

	protected String host;
	public String params;
	public int type;
	public String method;
	public boolean forceUpdate;
	public String keyCacher;
	/**
	 * 04-12-2012
	 */
	protected RequestData() {
		this("", 0,  "");
	}

	public RequestData(String inParams, int pType, String key) {
		host = "";
		params = inParams;
		type = pType;
		forceUpdate = false;
		keyCacher = key;
	}

	public RequestData(String inHost, String api, String inParams, int pType, String key) {
		host = inHost + api;
		params = inParams;
		type = pType;
		forceUpdate = false;
		keyCacher = key;
	}

	public RequestData(String inHost, String api, String inParams, int pType,
			String pmethod, String key) {
		host = inHost + api;
		type = pType;
		forceUpdate = false;
		if (inParams == null) {
			params = "";
		} else {
			params = inParams;
		}
		this.method = pmethod;
		keyCacher = key;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getURLString();
	}

	public String getHost() {
		if (host != null) {
			return host;
		}
		return "";
	}

	public String getURLString() {
//		try {
//			return host + "?" + URLEncoder.encode(params, "utf-8");
//		} catch (UnsupportedEncodingException e) {
//			GKIMLog.lf(null, 4,
//					"RequestData=>getUrlString UnsupportedEncodingException: "
//							+ e.getMessage());
//		}
		return host + "?" + params;
	}

	public String getKeyCacher() {
		return keyCacher;
	}
}
