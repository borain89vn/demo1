/**
 * 
 */
package com.gkxim.android.thanhniennews.models;

import java.lang.reflect.Type;
import java.util.ArrayList;

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

/**
 * @author Timon
 * 
 */
public class GeneralPage implements IGenericPage {

	private static final String TAG = "GeneralPage";
	public static final String GENERALPAGE_LAYOUTWIDTH = "layoutwidth";
	public static final String GENERALPAGE_BOXWIDTH = "boxwidth";
	public static final String GENERALPAGE_GAPWIDTH = "gapwidth";
	public static final String GENERALPAGE_BOXES = "boxes";

	private String mSectionId;

	private int layoutWidth;
	private int boxWidth;
	private int gapwidth;
	private boolean hasFavoritedChanged = false;
	private ArrayList<BoxStory> mBoxStories = null;
	private String mStoryIds;
	private String mPageTitle = "";

	public int getLayoutWidth() {
		return layoutWidth;
	}

	public int getBoxWidth() {
		return boxWidth;
	}

	public int getGapwidth() {
		return gapwidth;
	}
	
	public void setLayoutWidth(int layoutWidth) {
		this.layoutWidth = layoutWidth;
	}

	public void setBoxWidth(int boxWidth) {
		this.boxWidth = boxWidth;
	}

	public void setGapwidth(int gapwidth) {
		this.gapwidth = gapwidth;
	}

	public void setFavoriteChanged() {
		hasFavoritedChanged = true;
	}

	public boolean hasFavoriteChanged() {
		return hasFavoritedChanged;
	}

	/**
	 * @return the mSectionId
	 */
	public String getSectionId() {
		return mSectionId;
	}

	/**
	 * @param mSectionId
	 *            the mSectionId to set
	 */
	public void setSectionId(String mSectionId) {
		this.mSectionId = mSectionId;
	}

	public void setSectionTitle(String title) {
		mPageTitle = title;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gkxim.android.thanhniennews.models.IGenericPage#getSectionTitle()
	 */
	@Override
	public String getSectionTitle() {
		return mPageTitle;
	}

	public int getBoxStoryCount() {
		if (mBoxStories != null) {
			return mBoxStories.size();
		}
		return 0;
	}

	public BoxStory[] getBoxes() {
		if (mBoxStories != null && mBoxStories.size() > 0) {
			return mBoxStories.toArray(new BoxStory[mBoxStories.size()]);
		}
		return new BoxStory[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gkxim.android.thanhniennews.models.IGenericPage#getBoxStoryIds()
	 */
	@Override
	public String getBoxStoryIds() {
//		if (mStoryIds == null && mBoxStories != null && mBoxStories.size() > 0) {
		if (mBoxStories != null && mBoxStories.size() > 0) {
			StringBuilder sb = new StringBuilder();
			int len = mBoxStories.size();
			for (int i = 0; i < len; i++) {
				BoxStory aBox = mBoxStories.get(i);
				if (aBox != null) {
					sb.append(aBox.getStoryId()).append(",");
				}
			}
			if (sb.length() > 0) {
				sb.deleteCharAt(sb.length() - 1);
				mStoryIds = sb.toString();
			}
		}
		return mStoryIds;
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

	public int addBoxStory(BoxStory abox) {
		if (mBoxStories == null) {
			mBoxStories = new ArrayList<BoxStory>();
		}
		if ((mBoxStories.indexOf(abox) < 0) && mBoxStories.add(abox)) {
			return mBoxStories.indexOf(abox);
		}
		return -1;
	}

	public BoxStory removeBoxStory(String storyid) {
		if (mBoxStories == null || mBoxStories.size() == 0) {
			return null;
		}
		BoxStory result = null;
		for (BoxStory box : mBoxStories) {
			if (storyid.equalsIgnoreCase(box.getStoryId())) {
				mBoxStories.remove(box);
				result = box;
				break;
			}
		}
		return result;
	}

	public static class GeneralPageConverter implements
			JsonSerializer<GeneralPage>, JsonDeserializer<GeneralPage> {

		@Override
		public GeneralPage deserialize(JsonElement element, Type type,
				JsonDeserializationContext context) {
			GeneralPage result = null;
			try {
				result = new GeneralPage();
				JsonObject jObj = element.getAsJsonObject();
				if (jObj.has(GENERALPAGE_LAYOUTWIDTH)) {
					result.layoutWidth = jObj.getAsJsonPrimitive(
							GENERALPAGE_LAYOUTWIDTH).getAsInt();
				}
				if (jObj.has(GENERALPAGE_BOXWIDTH)) {
					result.boxWidth = jObj.getAsJsonPrimitive(
							GENERALPAGE_BOXWIDTH).getAsInt();
				}
				if (jObj.has(GENERALPAGE_GAPWIDTH)) {
					result.gapwidth = jObj.getAsJsonPrimitive(
							GENERALPAGE_GAPWIDTH).getAsInt();
				}
				if (!jObj.has(GENERALPAGE_BOXES)) {
					return result;
				}
				JsonArray array = jObj.getAsJsonArray(GENERALPAGE_BOXES);
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
						+ "=>deserialize GeneralPage got generic exception: "
						+ e.getMessage());
			}
			return result;
		}

		@Override
		public JsonElement serialize(GeneralPage arg0, Type arg1,
				JsonSerializationContext arg2) {
			// TODO serialize from object to JsonElement.
			return null;
		}

	}

}
