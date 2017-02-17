package com.sina.data.entry;



import com.sina.data.common.AppType;

import java.util.HashMap;
import java.util.Map;

public class MonitorDataEntry {
  
  /*
   * variable for monitor data between monitor system and application 
   */
  public static final String metricKey = "metric";
  public static final String monitorIdKey = "monitorId";
  public static final String valueKey = "value";
  public static final String timeKey = "time";
  //just used for ols
  public static final String componentKey = "component";
  public static final String nodeIdKey = "nodeId";
  
  
  private String metric;
  private String monitorId;
  private String value;
  private String time;
  
  //ols specific attribute
  private String component;
  private String nodeId;
  
  
  public void clear(){
    this.metric = "";
    this.monitorId = "";
    this.value = "";
    this.time = "";
    this.component = "";
    this.nodeId = "";
  }
  
  public Map<String,String> generateTranData(AppType type){
    Map<String,String> data = null;
    if(type == AppType.ols ){
      data = new HashMap<String, String>();
      data.put(metricKey, this.metric);
      data.put(monitorId, this.monitorId);
      data.put(valueKey, this.value);
      data.put(timeKey, this.time);
      data.put(componentKey, this.component);
      data.put(nodeIdKey, this.nodeId);
    }else if(type == AppType.job){
      data = new HashMap<String, String>();
      data.put(metricKey, this.metric);
      data.put(monitorId, this.monitorId);
      data.put(valueKey, this.value);
      data.put(timeKey, this.time);
    }
    return data;
  }
  
  public static Map<String,String> generatJonTranData(String metric,String monitorId,String value,String time){
    Map<String,String> data = new HashMap<String, String>();
    data.put(metricKey, metric);
    data.put(monitorId, monitorId);
    data.put(valueKey, value);
    data.put(timeKey, time);
    return data;
  }
 
 public static Map<String,String> generateJobTranData(String metric,String monitorId,String value,String time,String nodeId,String component){
   Map<String,String> data = new HashMap<String, String>();
   data.put(metricKey, metric);
   data.put(monitorId, monitorId);
   data.put(valueKey, value);
   data.put(timeKey, time);
   data.put(componentKey, component);
   data.put(nodeIdKey, nodeId);
   return data;
  }
  
  public String getMetric() {
    return metric;
  }
  public void setMetric(String metric) {
    this.metric = metric;
  }
  public String getMonitorId() {
    return monitorId;
  }
  public void setMonitorId(String monitorId) {
    this.monitorId = monitorId;
  }
  public String getValue() {
    return value;
  }
  public void setValue(String value) {
    this.value = value;
  }
  public String getTime() {
    return time;
  }
  public void setTime(String time) {
    this.time = time;
  }
  public String getComponent() {
    return component;
  }
  public void setComponent(String component) {
    this.component = component;
  }
  public String getNodeId() {
    return nodeId;
  }
  public void setNodeId(String nodeId) {
    this.nodeId = nodeId;
  }
  
}
