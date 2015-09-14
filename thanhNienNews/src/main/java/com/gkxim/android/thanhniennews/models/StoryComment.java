/**
 * File: StoryComment.java
 * Creator: Timon.Trinh (timon@gkxim.com)
 * Date: 02-01-2013
 * 
 */
package com.gkxim.android.thanhniennews.models;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.annotations.SerializedName;

/**
 *
 */
public class StoryComment {

	@SerializedName("comment_id")
	private int comment_id;
	@SerializedName("comment_time")
	private String comment_time;
	@SerializedName("name")
	private String name;
	@SerializedName("comment_count")
	private int comment_count;
	@SerializedName("reply_to")
	private String reply_to;
	@SerializedName("title")
	private String comment_title;
	@SerializedName("comment")
	private String comment;
	@SerializedName("rating")
	private String urlRatingIcon;

	public StoryComment(String fName, String title, String content) {
		comment_time = (new SimpleDateFormat("dd-MM-yyy hh:mm")).format(new Date());
		comment = content;
		comment_time = title;
	}

	/**
	 * @return the comment_id
	 */
	public int getCommentId() {
		return comment_id;
	}

	/**
	 * @return the comment_time
	 */
	public String getCommentTimeText() {
		return comment_time;
	}
	
	/**
	 * @return the comment_count
	 */
	public int getCommentCount() {
		return comment_count;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the reply_to
	 */
	public String getReplyTo() {
		return reply_to;
	}
	
	/**
	 * @return the comment_title
	 */
	public String getCommentTitle() {
		return comment_title;
	}
	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	public String getUrlRatingIcon() {
		return urlRatingIcon;
	}

}
