package com.sina.data.entry;


import com.sina.data.common.AppType;
import com.sina.data.common.JudgeType;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * Alarm information is that alert need to send alarm system.
 *
 */
public class AlarmEntry {
  
  public static final String ALARM_ENTRY_ID = "id";
  public static final String ALARM_ENTRY_APPTYPE = "appType";
  public static final String ALARM_ENTRY_USER = "user";
  public static final String ALARM_ENTRY_METRIC_NAME = "metric";
  public static final String ALARM_ENTRY_APPID = "appId";
  public static final String ALARM_ENTRY_TIMESTAMP = "timestamp";
  public static final String ALARM_ENTRY_CURRENT_VALUE = "current";
  public static final String ALARM_ENTRY_THRESHOLD = "threshold";
  public static final String ALARM_ENTRY_JUDGE_TYPE = "judgeType";
  public static final String ALARM_ENTRY_EXTEND = "extended";
  public static final String ALARM_ENTRY_MONITORID = "monitorId";
  public static final String ALARM_ENTRY_NODE_NAME = "node";
  public static final String ALARM_ENTRY_COMPONENT = "component";
  public static final String ALARM_ENTRY_REGISTER_NAME = "register_name";
  
  private String id;
  private AppType appType;
  private String metricName;
  private String appId;
  private long timestamp;
  private String register_name;
  
  /**
   * if we can not get the data of current_value from hbase, 
   * then defaul value is Double.valueOf(Integer.MIN_VALUE)
   */
  private String currentValue;
  private String threshold;
  private JudgeType judgeType;
  //others information
  private String extend;
  
  private String user;
  
  //the follow attributes are used for ols
  private String monitorId;
  private String node;
  private String component;
  
  public Map<String,Object> toPostData(){
    
    Map<String,Object> tmp = new HashMap<String, Object>();
    tmp.put(ALARM_ENTRY_ID, this.id);
    tmp.put(ALARM_ENTRY_APPTYPE, this.appType.toString());
    tmp.put(ALARM_ENTRY_METRIC_NAME, this.metricName);
    tmp.put(ALARM_ENTRY_TIMESTAMP, String.valueOf(timestamp));
    tmp.put(ALARM_ENTRY_JUDGE_TYPE, this.judgeType.toString());
    tmp.put(ALARM_ENTRY_USER, this.user);
    tmp.put(ALARM_ENTRY_NODE_NAME,this.getNode());
    tmp.put(ALARM_ENTRY_REGISTER_NAME,this.getRegister_name());

    if(this.appId==null)
      tmp.put(ALARM_ENTRY_APPID, "null");
    else
      tmp.put(ALARM_ENTRY_APPID, this.appId);

    if(this.threshold==null)
      tmp.put(ALARM_ENTRY_THRESHOLD, "null");
    else
      tmp.put(ALARM_ENTRY_THRESHOLD, this.threshold);

    if(this.currentValue==null)
      tmp.put(ALARM_ENTRY_CURRENT_VALUE, "null");
    else
      tmp.put(ALARM_ENTRY_CURRENT_VALUE, this.currentValue);

    if(StringUtils.isNotBlank(extend)){
      tmp.put(ALARM_ENTRY_EXTEND, "");
    }
    
    if(this.monitorId==null)
      tmp.put(ALARM_ENTRY_MONITORID, "null");
    else
      tmp.put(ALARM_ENTRY_MONITORID, this.monitorId);
    
    if(this.appType==AppType.ols){

      
      if(this.node==null)
        tmp.put(ALARM_ENTRY_NODE_NAME, "null");
      else
        tmp.put(ALARM_ENTRY_NODE_NAME, this.node);
      
      if(this.component==null)
        tmp.put(ALARM_ENTRY_COMPONENT, "null");
      else
        tmp.put(ALARM_ENTRY_COMPONENT, this.component); 
    }
    
    return tmp;
  }
  
  public AppType getAppType() {
    return appType;
  }
  public void setAppType(AppType appType) {
    this.appType = appType;
  }
  public String getMetricName() {
    return metricName;
  }
  public void setMetricName(String metricName) {
    this.metricName = metricName;
  }
  public String getAppId() {
    return appId;
  }
  public void setAppId(String appId) {
    this.appId = appId;
  }
  public long getTimestamp() {
    return timestamp;
  }
  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }
  public String getCurrentValue() {
    return currentValue;
  }
  public void setCurrentValue(String currentValue) {
    this.currentValue = currentValue;
  }
  public String getThreshold() {
    return threshold;
  }
  public void setThreshold(String threshold) {
    this.threshold = threshold;
  }
  public JudgeType getJudgeType() {
    return judgeType;
  }
  public void setJudgeType(JudgeType judgeType) {
    this.judgeType = judgeType;
  }
  public String getExtend() {
    return extend;
  }

  public void setExtend(String extend) {
    this.extend = extend;
  }

  public String getMonitorId() {
    return monitorId;
  }
  public void setMonitorId(String monitorId) {
    this.monitorId = monitorId;
  }
  public String getNode() {
    return node;
  }
  public void setNode(String node) {
    this.node = node;
  }
  public String getComponent() {
    return component;
  }
  public void setComponent(String component) {
    this.component = component;
  }
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("AlarmEntry [appType=");
    builder.append(appType);
    builder.append(", metricName=");
    builder.append(metricName);
    builder.append(", appId=");
    builder.append(appId);
    builder.append(", timestamp=");
    builder.append(timestamp);
    builder.append(", currentValue=");
    builder.append(currentValue);
    builder.append(", threshold=");
    builder.append(threshold);
    builder.append(", judgeType=");
    builder.append(judgeType);
    builder.append(", extended=");
    builder.append(extend);
    builder.append(", monitorId=");
    builder.append(monitorId);
    builder.append(", node=");
    builder.append(node);
    builder.append(", component=");
    builder.append(component);
    builder.append(", user=");
    builder.append(user);
    builder.append("]");
    return builder.toString();
  }


  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getRegister_name() {
    return register_name;
  }

  public void setRegister_name(String register_name) {
    this.register_name = register_name;
  }
}
