package com.sina.data.cons;
/**
 * Constants for Hbase Table Name,Column Name,and others 
 */
public class AppRegConstant {


  public static String hbase_namespace="ns_hadoopadmin:";

  /*
   * Hbase table name constants
   */
  public static final String APP_CLUSTER_CONF_TABLE_NAME = "BigMonitorClusterConfTable";
  public static final String APP_CLUSTER_CONF_HISTORY_TABLE_NAME = "BigMonitorClusterConfHistoryTable";
  public static final String APP_CLUSTER_CONF_CF_BASE = "base";
  public static final String APP_CLUSTER_CONF_CF_NODE = "node";
  public static final String APP_CLUSTER_CONF_COLUMN_APPID = "appId";
  public static final String APP_CLUSTER_CONF_COLUMN_REGTIME = "regTime";
  public static final String APP_CLUSTER_CONF_COLUMN_STATUS = "status";
  public static final String APP_CLUSTER_CONF_COLUMN_TYPE = "type";
  public static final String APP_CLUSTER_CONF_COLUMN_USER = "user";
  public static final String APP_CLUSTER_CONF_COLUMN_LASTTIME = "lastTime";
  public static final String APP_CLUSTER_CONF_COLUMN_EXPIREDTIME="expiredTime";
  public static final String APP_CLUSTER_CONF_COLUMN_MASTER="master";

  public static final String APP_CLUSTER_METRIC_TABLE_NAME = "BigMonitorMetricConfTable";
  public static final String METRIC_TABLE_COLUMN_NAME_ID = "id";
  public static final String METRIC_TABLE_COLUMN_NAME_SLOPE = "slope";
  public static final String METRIC_TABLE_COLUMN_NAME_TYPE = "type";
  public static final String METRIC_TABLE_COLUMN_NAME_GATHER_METHOD = "gather_method";
  public static final String METRIC_TABLE_COLUMN_NAME_NOTE = "note";
  
  


  public static final String APP_CLUSTER_METRIC_DATA_TABLE_NAME = "BigMonitorMetricDataAlert";
  public static final byte[] METRIC_DATA_COLUMN_D = "d".getBytes();
  
  public static final String APP_CLUSTER_MONITOR_APPLY_TABLE_NAME = "BigMonitorApplyTable";
  public static final String MONITOR_APPLY_TABLE_CF="c1";
  public static final String MONITOR_APPLY_TABLE_COLUMN_USER="user";
  public static final String MONITOR_APPLY_TABLE_COLUMN_TIME="applyTime";
  public static final String MONITOR_APPLY_TABLE_COLUMN_APPDESC="appDesc";
  public static final String MONITOR_APPLY_TABLE_COLUMN_TYPE="type";
  public static final String MONITOR_APPLY_TABLE_SEQUENCE = "max_sequence";
  public static final String MONITOR_APPLY_TABLE_COLUMN_NAME_MAX = "max";
  
  //this table is not used now.
  public static final String APP_CLUSTER_HOST_CONF_TABLE_NAME = "BigMonitorHostConfTable";

  /*
   *Hbase table column name and column family name 
   */
  public static final String COLUMN_FAMILY_COMMON1 = "c1";
  public static final byte[] COLUMN_FAMILY_COMMON1_BYTES = "c1".getBytes();
  public static final String COLUMN_FAMILY_COMMON2 = "c2";
  public static final byte[] COLUMN_FAMILY_COMMON2_BYTES = "c2".getBytes();


  public static final String ROWKEY_SEP = "#";
  
}
