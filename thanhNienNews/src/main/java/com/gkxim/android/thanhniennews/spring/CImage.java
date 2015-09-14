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
public class CImage {
	@SerializedName("resultCode")
	private String resultCode;
	@SerializedName("result")
	private String result;
	@SerializedName("requesteddate")
	private String requesteddate;
	@SerializedName("data")
	private CImageData data;
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
	public String getRequesteddate() {
		return requesteddate;
	}
	public void setRequesteddate(String requesteddate) {
		this.requesteddate = requesteddate;
	}
	public CImageData getData() {
		return data;
	}
	public void setData(CImageData data) {
		this.data = data;
	}
	
}
