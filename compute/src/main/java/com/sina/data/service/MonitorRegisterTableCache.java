package com.sina.data.service;

import com.sina.data.common.AppType;
import com.sina.data.common.JudgeType;
import com.sina.data.entry.AlertEntry;
import com.sina.data.util.ConfUtils;
import com.sina.data.util.DBPoolManager;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * support the content of mysql table Cluster_monitor_register
 */
public class MonitorRegisterTableCache {

  private static final Logger LOG = Logger
      .getLogger(MonitorRegisterTableCache.class);

  public static final String CLUSTER_MONITOR_REGISTER_TABLE_DEFAULT_NAME = "cluster_monitor_register";

  private String tableName;
  // just store the contend of id column of table
  // to check out if deleted or add a new monitor
  // key is id ,value is monitorId
  private Map<String, String> idsOfMonitorRegister;
  // key is monitorid ,value is the alert entry of the same monitor with key is metric name
  private ConcurrentHashMap<String, Map<String,AlertEntry>> monitorRegisters;
  private long refresh_interval;
  
  private ComputeContext context;

  private Thread updateThread ;
  
  private static MonitorRegisterTableCache INSTANCE;
  
  private MonitorRegisterTableCache(ComputeContext context) {
    this.context = context;
    this.tableName = ConfUtils.getString(
            "monitor.compute.table.cluster.monitor.register",
            CLUSTER_MONITOR_REGISTER_TABLE_DEFAULT_NAME);
    this.refresh_interval = ConfUtils.getLong(
            "monitor.compute.table.refresh.interval", 300l) * 1000l;

    this.monitorRegisters = new ConcurrentHashMap<String, Map<String,AlertEntry>>();
    this.idsOfMonitorRegister = new HashMap<String, String>();
    this.init();
    this.start();
  }

  public synchronized static MonitorRegisterTableCache getInstance(ComputeContext context){
    if(INSTANCE==null)
      INSTANCE = new MonitorRegisterTableCache(context);
    return INSTANCE;
  }
  
  // when init ,there is no need to synchronized
  private void init() {
    Connection conn = DBPoolManager.getConnection("monitorX");
    PreparedStatement state = null;
    ResultSet rs = null;
    try {

      state = conn
          .prepareStatement("SELECT id,register_name,metric_name,host_list,threshold,judge_type,mobile_threshold,cluster_name,monitor_type FROM "
              + this.tableName);
      rs = state.executeQuery();

      while (rs.next()) {
        AlertEntry bean = new AlertEntry();
        bean.setId(String.valueOf(rs.getInt("id")));
        bean.setRegisterName(rs.getString("register_name"));
        bean.setMetricName(rs.getString("metric_name"));
        bean.setHostList(rs.getString("host_list"));
        bean.setEmailThreshold(rs.getDouble("threshold"));
        bean.setMobileThreshold(rs.getDouble("mobile_threshold"));
        bean.setClusterName(rs.getString("cluster_name"));
        // only avoid enum exception
        try {
          bean.setJudgeType(JudgeType.valueOf(rs.getString("judge_type")));
          bean.setMonitorType(AppType.valueOf(rs.getString("monitor_type")));
        } catch (Exception e1) {
          LOG.error(e1);
          continue;
        }
        // clustername in mysql is monitorid
        Map<String,AlertEntry> alerts = this.monitorRegisters.get(bean
            .getClusterName());
        if (alerts == null) {
          alerts = new HashMap<String,AlertEntry>();
          this.monitorRegisters.put(bean.getClusterName(), alerts);
        }
        this.idsOfMonitorRegister.put(String.valueOf(bean.getId()),
            bean.getClusterName());
        alerts.put(bean.getRegisterName().trim(),bean);

      }

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
        if (state != null) {
          state.close();
        }
      } catch (Exception e) {
      }

      DBPoolManager.freeConnection(conn);
    }
    LOG.info("Loading cluster monitor register table with size=" + this.idsOfMonitorRegister.size());
  }

  private void start(){
    this.updateThread = new Thread(new UpdateThread());
    this.updateThread.setName("TableUpdate");
    this.updateThread.setDaemon(true);
    this.updateThread.start();
  }
  
  public Map<String,AlertEntry> getAlertEntries(String monitorId){
    synchronized (this) {
     return this.monitorRegisters.get(monitorId);
    }
  }
  
  public Map<String, Map<String,AlertEntry>> getMonitors(){
    synchronized (this) {
      return this.monitorRegisters;
    }
  }
  

  
  
  class UpdateThread implements Runnable {
    MonitorRegisterTableCache mysqlTable = MonitorRegisterTableCache.this;
    public void run() {
      while (true) {
        try {
          Thread.sleep(mysqlTable.refresh_interval);
        } catch (InterruptedException e) {
          LOG.error(e);
        }
        // get mysql table content
        Map<String, String> ids_tmp = new HashMap<String, String>();
        //new monitor records
        Map<String, String> ids_new = new HashMap<String, String>();
        //offline monitor records
        Map<String, String> ids_offline = new HashMap<String, String>();
        
        ConcurrentHashMap<String, Map<String, AlertEntry>> monitorRegisters_tmp = new ConcurrentHashMap<String, Map<String, AlertEntry>>();
        
        Connection conn = DBPoolManager.getConnection("monitorX");
        PreparedStatement state = null;
        ResultSet rs = null;
        try {

          state = conn
              .prepareStatement("SELECT id,register_name,metric_name,host_list,threshold,judge_type,mobile_threshold,cluster_name,monitor_type FROM "
                  + mysqlTable.tableName);
          rs = state.executeQuery();

          while (rs.next()) {
            AlertEntry bean = new AlertEntry();
            bean.setId(String.valueOf(rs.getInt("id")));
            bean.setRegisterName(rs.getString("register_name"));
            bean.setMetricName(rs.getString("metric_name"));
            bean.setHostList(rs.getString("host_list"));
            bean.setEmailThreshold(rs.getDouble("threshold"));
            bean.setMobileThreshold(rs.getDouble("mobile_threshold"));
            bean.setClusterName(rs.getString("cluster_name"));
            // only avoid enum exception
            try {
              bean.setJudgeType(JudgeType.valueOf(rs.getString("judge_type")));
              bean.setMonitorType(AppType.valueOf(rs.getString("monitor_type")));
            } catch (Exception e1) {
              LOG.error(e1);
              continue;
            }
            // clustername in mysql is monitorid
            Map<String,AlertEntry> alerts = monitorRegisters_tmp.get(bean
                .getClusterName());
            if (alerts == null) {
              alerts = new HashMap<String, AlertEntry>();
              monitorRegisters_tmp.put(bean.getClusterName(), alerts);
            }
            ids_tmp.put(String.valueOf(bean.getId()), bean.getClusterName());
            alerts.put(bean.getRegisterName().trim(),bean);
          }

        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          try {
            if (rs != null) {
              rs.close();
            }
            if (state != null) {
              state.close();
            }
          } catch (Exception e) {
            LOG.error(e);
          }

          DBPoolManager.freeConnection(conn);
        }
        //to issue the record that new added
        for(Entry<String, String> entry:ids_tmp.entrySet()){
          if(!mysqlTable.idsOfMonitorRegister.containsKey(entry.getKey())){
            ids_new.put(entry.getKey(), entry.getValue());
          }
        }
        LOG.info("There are new monitor entries ["+ids_new+"]");
        //to issue the record that offline
        for(Entry<String, String> entry:mysqlTable.idsOfMonitorRegister.entrySet()){
          if(!ids_tmp.containsKey(entry.getKey())){
            ids_offline.put(entry.getKey(), entry.getValue());
          }
        }
        LOG.info("There are offline monitor entries ["+ids_offline+"]");

        ConcurrentHashMap<String, Map<String,AlertEntry>> oldMonitorRegisters;
        synchronized (MonitorRegisterTableCache.this) {
          oldMonitorRegisters = mysqlTable.monitorRegisters;
          mysqlTable.monitorRegisters = monitorRegisters_tmp;
          mysqlTable.idsOfMonitorRegister = ids_tmp;
        }
        LOG.info("Reload mysql table of cluster_monitor_register content size="+mysqlTable.monitorRegisters);
        
        //begin to online new monitor record
        for(Entry<String, String> entry:ids_new.entrySet()){
          Map<String,AlertEntry> list = mysqlTable.monitorRegisters.get(entry.getValue());
          for(AlertEntry ae : list.values()){
            if(ae.getId().equals(entry.getKey())){
              //to notify to add new thread to process
              LOG.info("Found a new monitor entries " + entry.getValue());
            }
          }
        }
        
        //begin to offline deleted monitor record
        for(Entry<String, String> entry:ids_offline.entrySet()){
          Map<String,AlertEntry> list = oldMonitorRegisters.get(entry.getValue());
          for(AlertEntry ae : list.values()){
            if(ae.getId().equals(entry.getKey())){
              //to notify to stop off line compute thread
              Map<String,AlertEntry> onlineIds =mysqlTable.monitorRegisters.get(entry.getValue());
              //all monitor record of mysql are deleted ,and the monitor thread can be shutdown
              if(onlineIds==null||onlineIds.size()==0){
                LOG.info("Found a deleted monitor entries " + entry.getValue());
              }
            }
          }
        }
      }
    }
  }
  
  public static void main(String[] args) throws InterruptedException {
    MonitorRegisterTableCache.getInstance(null);
    Thread.sleep(10000000);
  }
  
}
