package com.sina.data.entry;


import com.sina.data.common.AppType;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * refer to hbase table BigMonitorClusterConfTable(default)
 * @date Oct 18, 2013
 *
 */
public class AppInfoEntry {
  
  private String monitorId;
  private String appId;
  private String regTime;
  private AppType type;
  private String master;
  /**
   * key->value => pu1@1 -> 1
   */
  private Map<String,String> olsNode = new ConcurrentHashMap<String,String>();
  /**
   * key->value => 1 -> pu1@1
   */
  private Map<String,String> olsNodereverse = new ConcurrentHashMap<String,String>();
  
  private String lastTime;
  private String user;
  /**
   * seconds to remove from running map if not data update
   */
  private int expiredTime;

  private volatile long updateTime=-1l;
  
  
  /**
   * just used for ols app
   * @param nodeName
   * @param nodeId
   */
  public synchronized void addNodeInfo(String nodeName,String nodeId){
    this.olsNode.put(nodeName, nodeId);
    this.olsNodereverse.put(nodeId,nodeName);
  }
  
  /**
   *according nodeId to find nodeName eg:
   *nodeId:2 -> nodeName:Spout1@2  
   * @param nodeId
   * @return
   */
  public synchronized String getOlsNodeName(String nodeId) {
    return this.olsNodereverse.get(nodeId);
  }
  
  /**
   * according nodeName to find nodeId eg:
   * nodeName:Spout1@2 -> nodeId:2
   * @param nodeName
   * @return
   */
  public synchronized String getOlsNodeId(String nodeName) {
    return this.olsNode.get(nodeName);
  }
  
  public synchronized void removeOlsNode(String nodeName,String nodeId){
    this.olsNode.remove(nodeName);
    this.olsNodereverse.remove(nodeId);
  }
  
  
  /**
   * just used when first initializing
   * @param olsNodereverse
   */
  public synchronized void setOlsNodes(Map<String, String> olsNodes,Map<String, String> olsNodereverse) {
    this.olsNode = olsNodes;
    this.olsNodereverse = olsNodereverse;
  }
  
  public synchronized Map<String, String> getOlsNodes(){
    
    return Collections.unmodifiableMap(this.olsNode);
  }
  
  public synchronized Map<String,String> getOlsNodereverse(){
    return Collections.unmodifiableMap(this.olsNodereverse);
  }
  
  public String getUser() {
    return user;
  }
  public void setUser(String user) {
    this.user = user;
  }
  public int getExpiredTime() {
    return expiredTime;
  }
  public void setExpiredTime(int expiredTime) {
    this.expiredTime = expiredTime;
  }
  public String getMonitorId() {
    return monitorId;
  }
  public void setMonitorId(String monitorId) {
    this.monitorId = monitorId;
  }
  public String getAppId() {
    return appId;
  }
  public void setAppId(String appId) {
    this.appId = appId;
  }
  public String getRegTime() {
    return regTime;
  }
  public void setRegTime(String regTime) {
    this.regTime = regTime;
  }
  public AppType getType() {
    return type;
  }
  public void setType(AppType type) {
    this.type = type;
  }
  public String getMaster() {
    return master;
  }
  public void setMaster(String master) {
    this.master = master;
  }
  
  public String getLastTime() {
    return lastTime;
  }
  public void setLastTime(String lastTime) {
    this.lastTime = lastTime;
  }
  
  
  public long getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(long updateTime) {
    this.updateTime = updateTime;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("[monitorId=");
    builder.append(monitorId);
    builder.append(", appId=");
    builder.append(appId);
    builder.append(", type=");
    builder.append(type);
    builder.append(", olsNode=");
    builder.append(olsNode);
    builder.append(", olsNodereverse=");
    builder.append(olsNodereverse);
    builder.append(", lastTime=");
    builder.append(lastTime);
    builder.append(", user=");
    builder.append(user);
    builder.append(",expiredTime=");
    builder.append(expiredTime);
    builder.append("]");
    return builder.toString();
  }

}
