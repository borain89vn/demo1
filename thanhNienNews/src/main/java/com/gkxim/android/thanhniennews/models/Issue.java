/**
 * File: Issue.java
 * Creator: Timon.Trinh (timon@gkxim.com)
 * Date: 07-11-2012
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
 * @author Timon Trinh
 */
public class Issue {

	private static final String TAG = "Issue";
	private static final String ISSUE_USERID = "userid";
	private static final String ISSUE_REQUESTEDDATE = "requesteddate";
	private static final String ISSUE_PAGES = "data";
	public static final String ISSUE_RESULTCODE = "resultCode";

	private String userId;
	private long requestedDate;
	public int resultCode;
	private ArrayList<SectionPage> mSectionArray = null;
	

	public Issue() {
		// Non-agrument contructor suport for Gson serialization
	}

	public String getUserid() {
		return userId;
	}

	public void setUserid(String userid) {
		this.userId = userid;
	}

	public long getRequesteddate() {
		return requestedDate;
	}

	public void setRequesteddate(long requesteddate) {
		this.requestedDate = requesteddate;
	}

	public SectionPage getPage(int index) {
		if (mSectionArray != null && mSectionArray.size() > 0) {
			if (index > -1 && index < mSectionArray.size()) {
				return mSectionArray.get(index);
			}
		}
		return null;
	}

	public SectionPage[] getPages() {
		if (mSectionArray == null || mSectionArray.size() <= 0) {
			return null;
		}
		SectionPage[] result = new SectionPage[mSectionArray.size()];
		return mSectionArray.toArray(result);
	}

	/**
	 * @Description: Get an array of SectionPage in this issue by sectionId
	 *               input.
	 * @param String
	 *            sectionId
	 * @return null if
	 */
	public SectionPage[] getPagebyId(String sectionId) {
		SectionPage[] result = null;
		int count = 0;
		if (mSectionArray != null && mSectionArray.size() > 0) {
			ArrayList<SectionPage> array = new ArrayList<SectionPage>();
			for (SectionPage page : mSectionArray) {
				if (sectionId.equalsIgnoreCase(page.getSectionId())) {
					array.add(page);
					count++;
				}
			}
			GKIMLog.lf(null, 0,
					TAG + "=>getPagebyId(String) have: " + array.size());
			if (count > 0) {
				result = new SectionPage[count];
				return array.toArray(result);
			}
		}
		return result;
	}

	/**
	 * @Description: Get an array of SectionPage in this issue by section's
	 *               title input.
	 * @param String
	 *            sectionTitle
	 * @return
	 */
	public SectionPage[] getPagebyTitle(String sectionTitle) {
		SectionPage[] result = null;
		int count = 0;
		if (mSectionArray != null && mSectionArray.size() > 0) {
			ArrayList<SectionPage> array = new ArrayList<SectionPage>();
			for (SectionPage page : mSectionArray) {
				if (sectionTitle.equalsIgnoreCase(page.getSectionTitle())) {
					array.add(page);
					count++;
				}
			}
			GKIMLog.lf(null, 0,
					TAG + "=>getPagebyTitle(String) have: " + array.size());
			if (count > 0) {
				result = new SectionPage[count];
				return array.toArray(result);
			}
		}
		return result;
	}

	/**
	 * @Description: Add a SectionPage into current Issue
	 * @param SectionPage
	 *            aPage
	 * @return the index of the first occurrence of the object, or -1 if it was
	 *         not found.
	 */
	public int addPage(SectionPage aPage) {
		if (mSectionArray == null) {
			mSectionArray = new ArrayList<SectionPage>();
		}
		if (mSectionArray.add(aPage)) {
			return mSectionArray.indexOf(aPage);
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		// TODO: convert to String
		return "";
	}

	public static SectionPage[] parseSections(Gson gson, JsonElement element) {
		JsonObject jObject = element.getAsJsonObject();
		JsonArray jArray = jObject.getAsJsonArray(ISSUE_PAGES);
		SectionPage[] result = null;
		if (jArray != null && jArray.size() > 0) {
			ArrayList<SectionPage> sectionArray = new ArrayList<SectionPage>();
			if (gson == null) {
				gson = new Gson();
			}
			for (JsonElement jsonElement : jArray) {
				SectionPage aPage = gson.fromJson(jsonElement,
						SectionPage.class);
				if (aPage != null) {
					sectionArray.add(aPage);
				}
			}
			if (sectionArray.size() > 0) {
				GKIMLog.lf(null, 0, TAG + "=>parseSections: completely added "
						+ sectionArray.size());
				result = sectionArray.toArray(result);
			}
		} else {
			GKIMLog.lf(null, 0, TAG + "=>parseSections: sectionArray was null");
		}
		return result;
	}

	public static class IssueConverter implements JsonSerializer<Issue>,
			JsonDeserializer<Issue> {

		@Override
		public Issue deserialize(JsonElement element, Type type,
				JsonDeserializationContext context) {
			Issue result = null;
			try {
				JsonObject jObj = element.getAsJsonObject();
				result = new Issue();
				if (jObj.has(ISSUE_RESULTCODE)) {
					result.resultCode = jObj.getAsJsonPrimitive(ISSUE_RESULTCODE).getAsInt();
					if (result.resultCode != 0) {
						//NOTE: response failed.
						return null;
					}
				}
				result.userId = jObj.getAsJsonPrimitive(ISSUE_USERID)
						.getAsString();
				result.requestedDate = (jObj.getAsJsonPrimitive(
						ISSUE_REQUESTEDDATE).getAsLong())*1000L;
				// From 24/12/2012
				// JsonArray array = jObj.getAsJsonArray(ISSUE_PAGES);
				// /api/issue became specified Home section, changed to
				if (!jObj.has(ISSUE_PAGES)) {
					return result;
				}
				JsonArray array = new JsonArray();
				array.add(jObj.get(ISSUE_PAGES));
				if (array == null || array.size() == 0) {
					return result;
				}
				Gson gson = (new GsonBuilder()).registerTypeAdapter(
						SectionPage.class,
						new SectionPage.SectionPageConverter()).create();
				for (JsonElement jsonElement : array) {
					SectionPage aPage = gson.fromJson(jsonElement,
							SectionPage.class);
					if (aPage != null) {
						result.addPage(aPage);
					}
				}
				GKIMLog.lf(null, 0, TAG + "=>deserialize completed: "
						+ result.mSectionArray.size());
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
				GKIMLog.lf(null, 4, TAG + "=>deserialize Issue got generic exception: "
						+ e.getMessage());
			}
			return result;
		}

		@Override
		public JsonElement serialize(Issue arg0, Type arg1,
				JsonSerializationContext arg2) {
			// TODO serialize to JSON for Issue
			return null;
		}

	}

	public int getPageCount() {
		if (mSectionArray != null) {
			return mSectionArray.size();
		}
		return 0;
	}

}
