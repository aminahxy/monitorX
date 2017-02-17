package com.sina.data.alarm;

import org.json.JSONException;
import org.json.JSONObject;

public interface ServiceInterface {

	public String process(JSONObject obj) throws JSONException;

}
