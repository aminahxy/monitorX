package com.sina.data.service;

import com.sina.data.common.JudgeType;
import com.sina.data.entry.AlertEntry;
import com.sina.data.util.ConfUtils;

public class ComputeWorker extends Thread {
  
  public static final String default_user = ConfUtils.getString(
          "compute.default.alarm.user", "hadoop");

  String monitorId;

  volatile boolean stop;

  ComputeContext context;

  long lastTime;
  
  public ComputeWorker(ComputeContext context){
    this.context = context;
  }
  
  boolean needAlert(double threshold, double value, JudgeType type) {

    switch (type) {

    case le:
      if (value <= threshold)
        return true;
      break;
    case lt:
      if (value < threshold)
        return true;
      break;
    case gt:
      if (value > threshold)
        return true;
      break;
    case ge:
      if (value >= threshold)
        return true;
      break;
    case eq:
      if (value == threshold)
        return true;
      break;
    case ne:
      if (value != threshold)
        return true;
      break;
    default:
      break;
    }

    return false;
  }

  public void shutdown() {
    stop = true;
    this.interrupt();
  }
  
  protected String alertString(AlertEntry alertInfo,double current,String monitor){
    StringBuilder sb = new StringBuilder("Alert information :");
    sb.append("metric=").append(alertInfo.getMetricName()).append(" ");
    sb.append("current value=").append(current).append(" ");
    sb.append(alertInfo.getJudgeType().toString()).append(" ");
    sb.append("threshold=").append(alertInfo.getEmailThreshold()).append(" ");
    sb.append("for ").append(monitor);
    return sb.toString();
  }
}
