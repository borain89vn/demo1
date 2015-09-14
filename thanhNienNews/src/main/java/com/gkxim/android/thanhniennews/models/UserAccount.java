/**
 * File: UserAccount.java
 * Creator: Timon.Trinh (timon@gkxim.com)
 * Date: 18-12-2012
 * 
 */
package com.gkxim.android.thanhniennews.models;

import com.google.gson.annotations.SerializedName;

/**
 * @author Timon Trinh
 */
public class UserAccount {

	@SerializedName("uid")
	private String mUserId;
	@SerializedName("email")
	private String mEmail;
	@SerializedName("fname")
	private String mFName;
	@SerializedName("lname")
	private String mLName;
	@SerializedName("pass")
	private String mPass;

	/**
	 * @return the mUserId
	 */
	public String getUserId() {
		return mUserId;
	}

	/**
	 * @return the mEmail
	 */
	public String getEmail() {
		return mEmail;
	}

	/**
	 * @return the mFName
	 */
	public String getFName() {
		return mFName;
	}

	/**
	 * @return the mLName
	 */
	public String getLName() {
		return mLName;
	}

	public UserAccount(String mUserId, String mEmail, String mFName,
			String mLName, String pass) {
		super();
		this.mUserId = mUserId;
		this.mEmail = mEmail;
		this.mFName = mFName;
		this.mLName = mLName;
		this.mPass = pass;
	}

	/**
	 * @return
	 */
	public String getPass() {
		return mPass;
	}

	public void setPass(String pass) {
		mPass = pass;
	}

	private String jsonInfo;

	public void setJsonInfo(String str) {
		this.jsonInfo = str;
	}

	public String getJsonInfo() {
		return this.jsonInfo;
	}

}
