package com.sina.data.service;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Interact with monitor aggregation system,when there was new app 
 * was be register,off line,and modified nodes
 */
public class ComputeService {


  private ComputeContext context;
  
  public ComputeService(ComputeContext context) throws Exception {
    this.context = context;
  }

  
  public String process(JSONObject obj) throws JSONException {
    return "";
  }
}
