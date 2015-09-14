/**
 * 
 */
package com.gkxim.android.thanhniennews.social;

import android.content.Context;
import android.content.Intent;

/**
 * @author Timon
 * 
 */
public interface ISocialShare {

	int EMAIL = 0;
	int FACEBOOK = 1;
	int TWITTER = 2;
	/**
	 * Get Social network id <br>
	 * 0: Mail <br>
	 * 1: Facebook <br>
	 * 2: Twitter
	 * 
	 * @return int
	 */
	public int getId();
	
	public void initialize(Context context);
	
	public boolean isReadyForShare();
	
	public void login();
	
	public void post(String[] data);
	
	public String getAccessToken();
	
	public boolean handlingActivityForResult(int requestCode, int resultCode, Intent data);
	
	public void loginApp();
	
	public void logout();
}
