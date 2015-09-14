/**
 * File: SectionPage.java
 * Creator: Timon.Trinh (timon@gkxim.com)
 * Date: 07-11-2012
 * 
 */
package com.gkxim.android.thanhniennews.models;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;

import android.text.format.DateFormat;

import com.gkxim.android.thanhniennews.utils.TNPreferenceManager;
import com.gkxim.android.utils.GKIMLog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.Expose;

/**
 * @author Timon Trinh
 */
public class SectionPage implements IGenericPage {

	public static final String SECTIONPAGE_SECTIONID = "sectionid";
	private static final CharSequence DATEFORMAT_STRING = "dd.MM.yyyy";
	@SuppressWarnings("unused")
	private static final String SECTIONPAGE_SECTIONTYPE = "sectiontype";
	public static final String SECTIONPAGE_SECTIONTITLE = "sectiontitle";
	public static final String SECTIONPAGE_ISSUEID = "issueid";
	public static final String SECTIONPAGE_ISSUEDATE = "issuedate";
	public static final String SECTIONPAGE_ISSUEDATESTRING = "issuedatestring";
	public static final String SECTIONPAGE_LAYOUTWIDTH = "layoutwidth";
	public static final String SECTIONPAGE_BOXWIDTH = "boxwidth";
	public static final String SECTIONPAGE_GAPWIDTH = "gapwidth";
	public static final String SECTIONPAGE_BOXES = "boxes";
	private static final String TAG = "SectionPage";
	public static boolean SECTIONPAGE_VIDEO = false;

	private String sectionId;
	private String sectionTitle;
	private String issueId;
	private String issueDateString;
	private long issueDate;
	private int layoutWidth;
	private int boxWidth;
	private int gapwidth;

	private String mStoryIds;

	@Expose
	private boolean hasFavoritedChanged = false;

	private ArrayList<BoxStory> mBoxStories = null;

	/**
	 * 07-11-2012
	 */
	public SectionPage() {
		// Non-agrument contructor suport for Gson serialization
	}

	public String getSectionId() {
		return sectionId;
	}

	public void setSectionId(String id) {
		this.sectionId = id;
	}

	public String getSectionTitle() {
		return sectionTitle;
	}

	public String getIssueDateString() {
		if (issueDateString == null || issueDateString.length() == 0) {
			issueDateString = DateFormat.format(DATEFORMAT_STRING,
					new Date(issueDate * 1000L)).toString();
		}
		return issueDateString;
	}

	public void setSectionTitle(String secTitle) {
		this.sectionTitle = secTitle;
	}

	public String getIssueId() {
		return issueId;
	}

	public void setIssueId(String id) {
		this.issueId = id;
	}

	public long getIssueDate() {
		return issueDate;
	}

	public int getBoxWidth() {
		return boxWidth;
	}

	public int getGapwidth() {
		return gapwidth;
	}

	public int getLayoutWidth() {
		return layoutWidth;
	}

	public void setLayoutWidth(int layoutW) {
		this.layoutWidth = layoutW;
	}

	public int getBoxStoryCount() {
		if (mBoxStories != null) {
			return mBoxStories.size();
		}
		return 0;
	}

	public String getBoxStoryIds() {
		return mStoryIds;
	}

	public void setFavoriteChanged() {
		hasFavoritedChanged = true;
	}

	public boolean hasFavoriteChanged() {
		return hasFavoritedChanged;
	}

	public BoxStory[] getBoxes() {
		if (mBoxStories != null && mBoxStories.size() > 0) {
			return mBoxStories.toArray(new BoxStory[mBoxStories.size()]);
		}
		return new BoxStory[0];
	}

	public BoxStory getBoxStory(int index) {
		if (mBoxStories != null && mBoxStories.size() > 0) {
			if (index > -1 && index < mBoxStories.size()) {
				return mBoxStories.get(index);
			}
		}
		return null;
	}

	/**
	 * @Description: Get an array of BoxStory in this issue by boxIndex input.
	 * @param int boxIndex
	 * @return null if
	 */
	public BoxStory[] getBoxStorybyIndex(int boxIndex) {
		BoxStory[] result = null;
		if (mBoxStories != null && mBoxStories.size() > 0) {
			ArrayList<BoxStory> array = new ArrayList<BoxStory>();
			for (BoxStory box : mBoxStories) {
				if (boxIndex == box.getBoxIndex()) {
					array.add(box);
				}
			}
			GKIMLog.lf(null, 0, TAG + "=>getBoxStorybyIndex(int) have: "
					+ array.size());
			if (array.size() > 0) {
				result = new BoxStory[array.size()];
				return array.toArray(result);
			}
		}
		return result;
	}

	public BoxStory getBoxStorybyId(String storyId) {
		if (mBoxStories != null && mBoxStories.size() > 0) {
			for (BoxStory box : mBoxStories) {
				if (storyId.equalsIgnoreCase(box.getStoryId())) {
					GKIMLog.lf(null, 0, TAG + "=> getBoxStorybyId found: "
							+ storyId + " at: " + box.getBoxIndex());
					return box;
				}
			}
		}
		return null;
	}

	/**
	 * @Description: Add a BoxStory into current SectionPage
	 * @param BoxStory
	 *            abox
	 * @return the index of the first occurrence of the object, or -1 if it was
	 *         not found.
	 */
	public int addBoxStory(BoxStory abox) {
		if (mBoxStories == null) {
			mBoxStories = new ArrayList<BoxStory>();
		}
		if (mBoxStories.add(abox)) {
			return mBoxStories.indexOf(abox);
		}
		return -1;
	}

	@Override
	public String toString() {
		return "SectionPage [sectionId=" + sectionId + ", sectionTitle="
				+ sectionTitle + ", issueId=" + issueId + ", issueDate="
				+ issueDate + ", layoutWidth=" + layoutWidth + "]";
	}

	public static class SectionPageConverter implements
			JsonSerializer<SectionPage>, JsonDeserializer<SectionPage> {

		@Override
		public SectionPage deserialize(JsonElement element, Type type,
				JsonDeserializationContext context) {
			SectionPage result = null;
			try {
				result = new SectionPage();
				JsonObject jObj = element.getAsJsonObject();
				if (jObj.has(SECTIONPAGE_ISSUEID)) {
					result.issueId = jObj.getAsJsonPrimitive(
							SECTIONPAGE_ISSUEID).getAsString();
				}
				if (jObj.has(SECTIONPAGE_ISSUEDATE)) {
					result.issueDate = jObj.getAsJsonPrimitive(
							SECTIONPAGE_ISSUEDATE).getAsLong();
				}
				if (!SECTIONPAGE_VIDEO) {
					if (jObj.has(SECTIONPAGE_SECTIONID)) {
						result.sectionId = jObj.getAsJsonPrimitive(
								SECTIONPAGE_SECTIONID).getAsString();
					}
					if (jObj.has(SECTIONPAGE_SECTIONTITLE)) {
						result.sectionTitle = jObj.getAsJsonPrimitive(
								SECTIONPAGE_SECTIONTITLE).getAsString();
					}
				} else {
					result.sectionId = TNPreferenceManager.getMediaSectionId();
					result.sectionTitle = "Video Title";
				}
				if (jObj.has(SECTIONPAGE_ISSUEDATESTRING)) {
					result.issueDateString = jObj.getAsJsonPrimitive(
							SECTIONPAGE_ISSUEDATESTRING).getAsString();
				}
				if (jObj.has(SECTIONPAGE_LAYOUTWIDTH)) {
					result.layoutWidth = jObj.getAsJsonPrimitive(
							SECTIONPAGE_LAYOUTWIDTH).getAsInt();
				}
				if (jObj.has(SECTIONPAGE_BOXWIDTH)) {
					result.boxWidth = jObj.getAsJsonPrimitive(
							SECTIONPAGE_BOXWIDTH).getAsInt();
				}
				if (jObj.has(SECTIONPAGE_GAPWIDTH)) {
					result.gapwidth = jObj.getAsJsonPrimitive(
							SECTIONPAGE_GAPWIDTH).getAsInt();
				}
				if (!jObj.has(SECTIONPAGE_BOXES)) {
					return result;
				}
				JsonArray array = jObj.getAsJsonArray(SECTIONPAGE_BOXES);
				if (array == null || array.size() == 0) {
					return result;
				}
				Gson gson = (new GsonBuilder()).registerTypeAdapter(
						BoxStory.class, new BoxStory.BoxStoryConverter())
						.create();
				StringBuilder sb = new StringBuilder();
				for (JsonElement jsonElement : array) {
					BoxStory aBox = gson.fromJson(jsonElement, BoxStory.class);
					if (aBox != null) {
						GKIMLog.lf(null, 0,
								TAG + "=>deserialize SectionPage, add box: "
										+ aBox.getBoxIndex());
						result.addBoxStory(aBox);
						sb.append(aBox.getStoryId()).append(",");
					}
				}
				if (sb.length() > 0) {
					sb.deleteCharAt(sb.length() - 1);
					result.mStoryIds = sb.toString();
				}
				GKIMLog.lf(null, 0, TAG + "=>deserialize completed: "
						+ result.mBoxStories.size());
			} catch (IllegalArgumentException e) {
				GKIMLog.lf(
						null,
						4,
						TAG + "=>deserialize IllegalArgumentException: "
								+ e.getMessage());
			} catch (JsonParseException e) {
				GKIMLog.lf(null, 4, TAG + "=>deserialize JsonParseException: "
						+ e.getMessage());
			} catch (Exception e) {
				GKIMLog.lf(null, 4, TAG
						+ "=>deserialize SectionPage got generic exception: "
						+ e.getMessage());
			}
			return result;
		}

		@Override
		public JsonElement serialize(SectionPage arg0, Type arg1,
				JsonSerializationContext arg2) {
			return null;
		}

	}

}
