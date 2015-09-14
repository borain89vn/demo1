package com.gkxim.android.thanhniennews.models;

/**
 * @author Timon
 *
 */
public interface IGenericPage {
	String getBoxStoryIds();
	String getSectionId();
	String getSectionTitle();
	int getLayoutWidth();
	BoxStory[] getBoxes();
	int getBoxStoryCount();
	
}
