/**
 * File: GenericResponse.java
 * Creator: Timon.Trinh (timon@gkxim.com)
 * Date: 02-01-2013
 * 
 */
package com.gkxim.android.thanhniennews.models;

import java.lang.reflect.Type;

import com.gkxim.android.utils.GKIMLog;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 *
 */
public class GenericResponse {
	private static final String TAG = "GenericResponse";
	private static final String USERID = "userid";
	private static final String REQUESTEDDATE = "requesteddate";
	private static final String DATA = "data";
	private static final String RESULTCODE = "resultCode";
	private static final String RESULTMSG = "result";

	private String userId;
	private long requestedDate;
	private String data;
	private boolean hasData;
	public int resultCode;
	public String resultMsg;
	public JsonElement dataelement;

	public GenericResponse() {
		hasData = false;
	}

	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @return the requestedDate
	 */
	public long getRequestedDate() {
		return requestedDate;
	}

	public String getData() {
		return data;
	}
	
	public JsonElement getDataElement() {
		return dataelement;
	}
	
	public boolean isSucceed() {
		if (resultCode != 0) {
			// NOTE: response failed.
			return false;
		}
		return true;
	}

	/**
	 * @return the hasData
	 */
	public boolean isHasData() {
		return hasData;
	}

	public static class GenericResponseConverter implements
			JsonSerializer<GenericResponse>, JsonDeserializer<GenericResponse> {

		@Override
		public GenericResponse deserialize(JsonElement element, Type type,
				JsonDeserializationContext context) {
			GenericResponse result = null;
			try {
				JsonObject jObj = element.getAsJsonObject();
				result = new GenericResponse();

				if (jObj.has(RESULTMSG)) {
					result.resultMsg = jObj.getAsJsonPrimitive(RESULTMSG)
							.getAsString();
				}

				if (jObj.has(USERID)) {
					result.userId = jObj.getAsJsonPrimitive(USERID)
							.getAsString();
				}
				if (jObj.has(REQUESTEDDATE)) {
					result.requestedDate = (jObj
							.getAsJsonPrimitive(REQUESTEDDATE).getAsLong()) * 1000L;
				}
				if (jObj.has(RESULTCODE)) {
					result.resultCode = jObj.getAsJsonPrimitive(RESULTCODE)
							.getAsInt();
					if (result.resultCode != 0) {
						// NOTE: response failed.
						return result;
					}
				}
				if (jObj.has(DATA)) {
					try {
						result.dataelement = jObj.get(DATA);
						result.data = new Gson().toJson(result.dataelement);
						if (result.data != null && result.data.length() > 0) {
							result.hasData = true;
						}
					} catch (Exception e) {
						GKIMLog.lf(null, 0,
								TAG + "=>deserialize failed: " + e.getMessage());
						result.data = null;
						result.hasData = false;
					}
				}
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
				GKIMLog.lf(null, 4, TAG + "=>deserialize GenericResponse got generic exception: "
						+ e.getMessage());
			}
			return result;
		}

		@Override
		public JsonElement serialize(GenericResponse arg0, Type arg1,
				JsonSerializationContext arg2) {
			// TODO serialize to JSON for GenericResponse
			return null;
		}

	}

}
