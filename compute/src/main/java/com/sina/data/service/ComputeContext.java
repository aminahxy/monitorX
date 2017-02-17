package com.sina.data.service;

import com.sina.data.client.MonitorClient;
import com.sina.data.client.impl.HttpMonitorClient;
import com.sina.data.util.ConfUtils;

import java.io.IOException;

/**
 * context includes all needed dependencies 
 *
 */
public class ComputeContext {
  //cache of mysql or hbase table
  private MonitorRegisterTableCache monitorRegisterTable;
  private MetricConfCache metricConfTable;

  private AlertService alertService;
  
  private MonitorClient httpClient;
  private Thread alertThread;
  
  private ComputeContext(){
    
  }
  
  private void init() {
    //http client for alarm
    String httpUrl = ConfUtils.getString("monitor.http.client.alarm.url",
            "http://localhost:28188");
    this.httpClient = new HttpMonitorClient(httpUrl);
    
    this.alertService = new AlertService(this);
    alertThread = new Thread(this.alertService);
    alertThread.setName("AlarmThread");
    alertThread.setDaemon(true);
    alertThread.start();
    
    this.monitorRegisterTable = MonitorRegisterTableCache.getInstance(this);
    //must initialize metric data
    this.metricConfTable = MetricConfCache.getInstance();
    
    //must initialize dataFetcher
//    DataFetcher.getInstance();
    
  }
  public static ComputeContext INSTANCE ;
  
  public synchronized static ComputeContext getInstance() {
    if(INSTANCE==null){
      INSTANCE = new ComputeContext();
      INSTANCE.init();
    }
    return INSTANCE;
  }
  
  public void close(){
   this.alertThread.interrupt(); 
  }
  
  public MonitorRegisterTableCache getMonitorRegisterTable() {
    return monitorRegisterTable;
  }

  public AlertService getAlertService() {
    return alertService;
  }

  public MetricConfCache getMetricConfTable() {
    return metricConfTable;
  }

  public MonitorClient getHttpClient() {
    return httpClient;
  }
  
}
