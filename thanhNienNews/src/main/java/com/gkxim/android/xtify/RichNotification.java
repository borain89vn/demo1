package com.gkxim.android.xtify;

import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class RichNotification {

	private String actionData;
	private String actionLabel;
	private String actionType;
	private String content;
	private String date;
	private String subject;
	private Double ruleLat;
	private Double ruleLon;
	private String mid;
	private String expirationDate;

	public RichNotification(String actionData, String actionLabel,
			String actionType, String content, String date, String subject,
			Double ruleLat, Double ruleLon, String mid, String expirationDate) {
		setActionData(actionData);
		setActionLabel(actionLabel);
		setActionType(actionType);
		setContent(content);
		setDate(date);
		setSubject(subject);
		setRuleLat(ruleLat);
		setRuleLon(ruleLon);
		setMid(mid);
		setExpirationDate(expirationDate);
	}

	public static ArrayList<RichNotification> getRnFronJsonString(String jsonString) throws JSONException {
		JSONArray rnArray = getRNJsonArray(jsonString);
		if (rnArray != null && rnArray.length() > 0) {
			ArrayList<RichNotification> rnList = new ArrayList<RichNotification>();
			for (int i = 0; i < rnArray.length(); ++i) {
				JSONObject messageObj;
				messageObj = rnArray.getJSONObject(i);
				rnList.add(getRnFromJsonObject(messageObj));
			}
			return rnList;
		}
		return null;
	}

	private static JSONArray getRNJsonArray(String jsonString) throws JSONException {
		JSONObject jsonResp = new JSONObject(jsonString);
		JSONArray messageArray = jsonResp.getJSONArray("messages");
		return messageArray;
	}

	private static RichNotification getRnFromJsonObject(JSONObject rnJsonObject) {
		RichNotification rn = new RichNotification();
		rn.setActionData((String) checkJsonKey(rnJsonObject, "actionData"));
		rn.setActionLabel((String) checkJsonKey(rnJsonObject, "actionLabel"));
		rn.setActionType((String) checkJsonKey(rnJsonObject, "actionType"));
		rn.setContent((String) checkJsonKey(rnJsonObject, "content"));
		rn.setDate((String) checkJsonKey(rnJsonObject, "date"));
		rn.setSubject((String) checkJsonKey(rnJsonObject, "subject"));
		rn.setRuleLat((Double) checkJsonKey(rnJsonObject, "ruleLat"));
		rn.setRuleLon((Double) checkJsonKey(rnJsonObject, "ruleLon"));
		rn.setMid((String) checkJsonKey(rnJsonObject, "mid"));
		rn.setExpirationDate((String) checkJsonKey(rnJsonObject, "expirationDate"));
		return rn;
	}

	static Object checkJsonKey(JSONObject rnJsonObject, String key) {
		if (rnJsonObject.has(key)) {
			try {
				return rnJsonObject.get(key);
			} catch (JSONException e) {
			}
		}
		return null;
	}

	RichNotification() {
	}

	public void setRuleLat(Double ruleLat) {
		this.ruleLat = ruleLat;
	}

	public Double getRuleLat() {
		return ruleLat;
	}

	public void setRuleLon(Double ruleLon) {
		this.ruleLon = ruleLon;
	}

	public Double getRuleLon() {
		return ruleLon;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getSubject() {
		return subject;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	public String getActionType() {
		return actionType;
	}

	public void setActionLabel(String actionLabel) {
		this.actionLabel = actionLabel;
	}

	public String getActionLabel() {
		return actionLabel;
	}

	public void setHasAction(Boolean hasAction) {
	}

	public Boolean getHasAction() {
		if (getActionType() != null || getActionData() != null)
			return (getActionType().equals("PHN") || getActionType().equals("WEB") || getActionType().equals("CST"));
		else
			return false;
	}

	public boolean isExpired() {
		String expirationDate = getExpirationDate();
		if (expirationDate != null && !(expirationDate.length() == 0)) {
			try {
				Calendar now = Calendar.getInstance();
				Calendar ed = ISO8601.toCalendar(expirationDate);
				if (now.after(ed)) {
					return true;
				}
			} catch (Exception e) {
				System.out.println("Error while parsing expirationDate");
			}
		}
		return false;
	}

	public String toString() {
		return "subject = " + subject + " , " + "content = " + content + " , " + "actiontype = " + actionType;
	}

	public String getActionData() {
		return actionData;
	}

	public void setActionData(String actionData) {
		this.actionData = actionData;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getDate() {
		return date;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public String getMid() {
		return mid;
	}

	public void setExpirationDate(String expirationDate) {
		this.expirationDate = expirationDate;
	}

	public String getExpirationDate() {
		return expirationDate;
	}

}