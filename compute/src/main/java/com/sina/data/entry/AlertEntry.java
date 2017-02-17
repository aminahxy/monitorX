package com.sina.data.entry;


import com.sina.data.common.AppType;
import com.sina.data.common.JudgeType;

/**
 * entry to mysql table Cluster_monitor_register 
 * 
 */
public class AlertEntry {
  private String registerName;
  private String metricName;
  private String hostList;
  private double emailThreshold;
  private double mobileThreshold;
  private JudgeType judgeType;
  private String clusterName;
  private AppType monitorType;
  private String id;

  public String getRegisterName() {
    return registerName;
  }

  public void setRegisterName(String registerName) {
    this.registerName = registerName;
  }

  public String getMetricName() {
    return metricName;
  }

  public void setMetricName(String metricName) {
    this.metricName = metricName;
  }

  public String getHostList() {
    return hostList;
  }

  public void setHostList(String hostList) {
    this.hostList = hostList;
  }

  public double getEmailThreshold() {
    return emailThreshold;
  }

  public void setEmailThreshold(double emailThreshold) {
    this.emailThreshold = emailThreshold;
  }

  public double getMobileThreshold() {
    return mobileThreshold;
  }

  public void setMobileThreshold(double mobileThreshold) {
    this.mobileThreshold = mobileThreshold;
  }

  public void setMobileThreshold(long mobileThreshold) {
    this.mobileThreshold = mobileThreshold;
  }

  public JudgeType getJudgeType() {
    return judgeType;
  }

  public void setJudgeType(JudgeType judgeType) {
    this.judgeType = judgeType;
  }

  public String getClusterName() {
    return clusterName;
  }

  public void setClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

  public AppType getMonitorType() {
    return monitorType;
  }

  public void setMonitorType(AppType monitorType) {
    this.monitorType = monitorType;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("AlertEntry [registerName=");
    builder.append(registerName);
    builder.append(", metricName=");
    builder.append(metricName);
    builder.append(", hostList=");
    builder.append(hostList);
    builder.append(", emailThreshold=");
    builder.append(emailThreshold);
    builder.append(", mobileThreshold=");
    builder.append(mobileThreshold);
    builder.append(", judgeType=");
    builder.append(judgeType);
    builder.append(", clusterName=");
    builder.append(clusterName);
    builder.append(", monitorType=");
    builder.append(monitorType);
    builder.append(", id=");
    builder.append(id);
    builder.append("]");
    return builder.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AlertEntry other = (AlertEntry) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }

}
