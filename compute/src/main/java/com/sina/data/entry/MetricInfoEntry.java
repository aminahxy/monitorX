package com.sina.data.entry;


import com.sina.data.common.AppType;

public class MetricInfoEntry {
  private String metricId;
  private String metricName;
  private AppType type;
  
  //the following attribute may not use in program;
  private String slope;
  private String note;
  private String gather_method;
  public String getMetricId() {
    return metricId;
  }
  public void setMetricId(String metricId) {
    this.metricId = metricId;
  }
  public String getMetricName() {
    return metricName;
  }
  public void setMetricName(String metricName) {
    this.metricName = metricName;
  }
  public AppType getType() {
    return type;
  }
  public void setType(AppType type) {
    this.type = type;
  }
  public String getSlope() {
    return slope;
  }
  public void setSlope(String slope) {
    this.slope = slope;
  }
  public String getNote() {
    return note;
  }
  public void setNote(String note) {
    this.note = note;
  }
  public String getGather_method() {
    return gather_method;
  }
  public void setGather_method(String gather_method) {
    this.gather_method = gather_method;
  }
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("MetricInfoEntry [metricId=");
    builder.append(metricId);
    builder.append(", metricName=");
    builder.append(metricName);
    builder.append(", type=");
    builder.append(type);
    builder.append("]");
    return builder.toString();
  }
  
}
