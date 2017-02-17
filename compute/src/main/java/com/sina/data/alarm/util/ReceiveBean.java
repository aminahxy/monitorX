package com.sina.data.alarm.util;



import com.sina.data.common.AppType;
import com.sina.data.common.JudgeType;
import com.sina.data.util.DBPoolManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class ReceiveBean {
  private String id;
  private String appId;
  private AppType appType;
  private long timestamp;
  private String register_name;
  private String metric;
  private String host;
  private String extended;
  private String node;
  private String component;
  private Double current_value;
  private double threshold;
  private String user;
  private JudgeType judgeType;
  // extra information var
  private String others;
  // extra infomation switch,default is off
  private String on_off;
  private String monitorId;

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public String getRegister_name() {
    return register_name;
  }

  public void setRegister_name(String register_name) {
    this.register_name = register_name;
  }

  public String getMetric() {
    return metric;
  }

  public void setMetric(String metric) {
    this.metric = metric;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public Double getCurrent_value() {
    return current_value;
  }

  public void setCurrent_value(Double current_value) {
    this.current_value = current_value;
  }

  public double getThreshold() {
    return threshold;
  }

  public void setThreshold(double threshold) {
    this.threshold = threshold;
  }

  public String getOthers() {
    return others;
  }

  public void setOthers(String others) {
    this.others = others;
  }

  public String getOn_off() {
    return on_off;
  }

  public void setOn_off(String on_off) {
    this.on_off = on_off;
  }
  
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
  

  public JudgeType getJudgeType() {
    return judgeType;
  }

  public void setJudgeType(JudgeType judgeType) {
    this.judgeType = judgeType;
  }
  

  public String getExtended() {
    return extended;
  }

  public void setExtended(String extended) {
    this.extended = extended;
  }
  
  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public AppType getAppType() {
    return appType;
  }

  public void setAppType(AppType appType) {
    this.appType = appType;
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

  public String getMonitorId() {
    return monitorId;
  }

  public void setMonitorId(String monitorId) {
    this.monitorId = monitorId;
  }

  public void toSql() {
    Connection con = DBPoolManager.getConnection("monitorX");
    Statement stat = null;
    try {
      stat = con.createStatement();
      String sql = "insert into monitor_recored (mid,register_name,timestamp,metric,host,current_value,threshold,others,on_off,user,app_id) values('"
          + id
          + "','"
          + register_name
          + "',"
          + timestamp
          + ",'"
          + metric
          + "','"
          + node
          + "',"
          + current_value
          + ","
          + threshold
          + ",'"
          + others
          + "','"
          + on_off 
          +  "','" 
          + user 
          +  "','" 
          + appId + "');";
      stat.execute(sql);
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        stat.close();
        con.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }
  

}
