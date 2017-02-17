package com.sina.data.server;

import org.json.JSONException;
import org.json.JSONObject;

public interface ServerHandlerInterface {
  public String process(JSONObject obj) throws JSONException;
}
