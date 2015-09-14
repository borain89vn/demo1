/**
 * File: TNTemplate.java
 * Creator: Timon.Trinh (timon@gkxim.com)
 * Date: 08-11-2012
 * 
 */
package com.gkxim.android.thanhniennews.models;

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.gkxim.android.utils.GKIMLog;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * @author Timon Trinh
 */
public class TNTemplate {

	private static final String TAG = "TNTemplate";
	@SuppressWarnings("unused")
	private static final String TNTEMPLATE_USERID = "userid";
	private static final String TNTEMPLATE_REQUESTEDDATE = "requesteddate";
	private static final String TNTEMPLATE_RESULTCODE = "resultCode";
	private static final String TNTEMPLATE_SECTIONS = "data";

	private String userId;
	private long requestedDate;
	private int resultCode;

	private ArrayList<SectionTemplate> sectionArray = null;

	/**
	 * 08-11-2012
	 */
	public TNTemplate() {
	}

	public int getSectionCount() {
		if (sectionArray != null) {
			return sectionArray.size();
		}
		return 0;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public long getRequestedDate() {
		return requestedDate;
	}

	public void setRequestedDate(long requestedDate) {
		this.requestedDate = requestedDate;
	}

	public SectionTemplate getSection(int index) {
		if (sectionArray != null && sectionArray.size() > 0) {
			if (index > -1 && index < sectionArray.size()) {
				return sectionArray.get(index);
			}
		}
		return null;
	}

	public SectionTemplate[] getSections() {
		SectionTemplate[] result = null;
		if (sectionArray != null && sectionArray.size() > 0) {
			result = new SectionTemplate[sectionArray.size()];
			return sectionArray.toArray(result);
		}
		return null;
	}

	/**
	 * @Description: Get an array of SectionPage in this issue by sectionId
	 *               input.
	 * @param String
	 *            sectionId
	 * @return null if
	 */
	public SectionTemplate[] getSectionbyId(String sectionId) {
		SectionTemplate[] result = null;
		if (sectionArray != null && sectionArray.size() > 0) {
			ArrayList<SectionTemplate> array = new ArrayList<SectionTemplate>();
			for (SectionTemplate sectionTemplate : sectionArray) {
				if (sectionId.equalsIgnoreCase(sectionTemplate.getSectionId())) {
					array.add(sectionTemplate);
				}
			}
			GKIMLog.lf(null, 0,
					TAG + "=>getPagebyId(String) have: " + array.size());
			if (array.size() > 0) {
				result = new SectionTemplate[array.size()];
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
	public SectionTemplate[] getSectionbyTitle(String sectionTitle) {
		SectionTemplate[] result = null;
		if (sectionArray != null && sectionArray.size() > 0) {
			ArrayList<SectionTemplate> array = new ArrayList<SectionTemplate>();
			for (SectionTemplate sectionTemplate : sectionArray) {
				if (sectionTitle.equalsIgnoreCase(sectionTemplate
						.getSectionTitle())) {
					array.add(sectionTemplate);
				}
			}
			GKIMLog.lf(null, 0,
					TAG + "=>getPagebyTitle(String) have: " + array.size());
			if (array.size() > 0) {
				result = new SectionTemplate[array.size()];
				return array.toArray(result);
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return "TNTemplate [userId=" + userId + ", requestedDate="
				+ requestedDate + ", sectionArray=" + sectionArray.toString()
				+ "]";
	}

	public static class TNTemplateConverter implements
			JsonDeserializer<TNTemplate> {

		@Override
		public TNTemplate deserialize(JsonElement element, Type type,
				JsonDeserializationContext context) {
			TNTemplate result = null;
			try {
				result = new TNTemplate();
				JsonObject jObj = element.getAsJsonObject();
				if (jObj.has(TNTEMPLATE_RESULTCODE)) {
					result.resultCode = jObj.getAsJsonPrimitive(
							TNTEMPLATE_RESULTCODE).getAsInt();
					if (result.resultCode != 0) {
						// NOTE: response failed.
						return null;
					}
				}
				// XXX: no need userID in Section template
				// result.userId = jObj.getAsJsonPrimitive(TNTEMPLATE_USERID)
				// .getAsString();

				if (jObj.has(TNTEMPLATE_REQUESTEDDATE)) {
					result.requestedDate = jObj.getAsJsonPrimitive(
							TNTEMPLATE_REQUESTEDDATE).getAsLong();
				}

				JsonArray array = jObj.getAsJsonArray(TNTEMPLATE_SECTIONS);
				if (array == null || array.size() == 0) {
					return result;
				}
				Gson gson = new Gson();
				for (JsonElement jsonElement : array) {
					SectionTemplate aTemplate = gson.fromJson(jsonElement,
							SectionTemplate.class);
					if (aTemplate != null) {
						result.addSectionTemplate(aTemplate);
					}
				}
				GKIMLog.lf(null, 0, TAG + "=>deserialize completed: "
						+ result.sectionArray.size());
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
				GKIMLog.lf(null, 4,
						TAG + "=>deserialize Exception: " + e.getMessage());
			}
			return result;
		}
	}

	public int addSectionTemplate(SectionTemplate aTemplate) {
		if (sectionArray == null) {
			sectionArray = new ArrayList<SectionTemplate>();
		}
		if (sectionArray.add(aTemplate)) {
			return sectionArray.indexOf(aTemplate);
		}
		return -1;
	}

}
