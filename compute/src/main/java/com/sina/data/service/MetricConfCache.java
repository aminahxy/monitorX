package com.sina.data.service;

import com.sina.data.cons.AppRegConstant;
import com.sina.data.entry.MetricInfoEntry;
import com.sina.data.exp.InitMonitorException;
import com.sina.data.hbase.HbaseUtils;
import com.sina.data.util.ConfUtils;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * default to hbase table BigMonitorMetricConfTable cache
 */
public class MetricConfCache {

  private static final Logger LOG = Logger.getLogger(MetricConfCache.class);

  private String tableName;

  private static final byte[] cf = Bytes
      .toBytes(AppRegConstant.COLUMN_FAMILY_COMMON1);
  private static final byte[] cf2 = Bytes
      .toBytes(AppRegConstant.COLUMN_FAMILY_COMMON2);
  private static final byte[] id = Bytes
      .toBytes(AppRegConstant.METRIC_TABLE_COLUMN_NAME_ID);
  private static final byte[] type = Bytes
      .toBytes(AppRegConstant.MONITOR_APPLY_TABLE_COLUMN_TYPE);
  private static final byte[] slope = Bytes
      .toBytes(AppRegConstant.METRIC_TABLE_COLUMN_NAME_SLOPE);
  private static final byte[] node = Bytes
      .toBytes(AppRegConstant.METRIC_TABLE_COLUMN_NAME_NOTE);

  /**
   * key is metric name,value is metric entry
   */
  private Map<String, MetricInfoEntry> metricInfo;
  /**
   * key is metric id ,value is metric name
   */
  private Map<String, String> metricInfoReverse;

  private Thread updateThread;

  private MetricConfCache() {
    tableName = ConfUtils.getString("monitor.hbase.metric.table.name",
             AppRegConstant.APP_CLUSTER_METRIC_TABLE_NAME);
  }

  private static MetricConfCache metricConf;

  public synchronized static MetricConfCache getInstance() {
    if (metricConf == null) {

      metricConf = new MetricConfCache();
      try {
        metricConf.init();
      } catch (InitMonitorException e) {
        LOG.error("init table error.",e);
        System.exit(101);
      }
      metricConf.start();
    }
    return metricConf;

  }

  public synchronized Map<String,String> getMetricKeyToName(){
    return this.metricInfoReverse;
  }
  
  public synchronized Map<String,MetricInfoEntry> getMetricNameToEntry(){
    return this.metricInfo;
  }
  
  // default 5 minute
  private int updateInterval = 5 * 60 * 1000;

  private void init() throws InitMonitorException {
    this.getMetricConfData();
    if (this.metricInfo.size() == 0) {
      throw new InitMonitorException(
          "Can not scan any metric conf from hbase table.");
    }
  }

  public void start() {
    updateThread = new Thread(new UpdateMetricThread());
    updateThread.setName("MetricUpdateThread");
    updateThread.setDaemon(true);
    updateThread.start();
  }

  private void getMetricConfData() throws InitMonitorException {
    Map<String, MetricInfoEntry> laterMetric = new ConcurrentHashMap<String, MetricInfoEntry>(
        100);
    Map<String, String> laterMetricReverse = new ConcurrentHashMap<String, String>(
        100);

    Scan scan = new Scan();
    scan.addFamily(cf);
    scan.addFamily(cf2);
    ResultScanner results = null;
    Table htable = null;
    try {
      htable = HbaseUtils.getTable(this.tableName);
      results = htable.getScanner(scan);
    } catch (IOException e) {
      LOG.error("load hbase table="+tableName+" error",e);
      throw new InitMonitorException("load hbase table=" + tableName
          + " error ", e);
    }
    Iterator<Result> its = results.iterator();
    while (its.hasNext()) {
      Result res = its.next();
      String rowkey = Bytes.toString(res.getRow());
      String metricId = Bytes.toString(res.getValue(cf, id));
      MetricInfoEntry entry = new MetricInfoEntry();
      entry.setMetricId(metricId);
      entry.setMetricName(rowkey);
      laterMetric.put(rowkey, entry);
      laterMetricReverse.put(metricId, rowkey);
    }
    try {
      if (results != null)
        results.close();
      if(htable != null)
        htable.close();
    } catch (IOException e) {
      LOG.error(e);
    }
    LOG.info("load metric conf data from table=" + tableName + " with size="
        + laterMetric.size());
    synchronized (this) {
      this.metricInfo = laterMetric;
      this.metricInfoReverse = laterMetricReverse;
    }
  }
  
  public String getMetricNameById(String id) {
    return this.getMetricKeyToName().get(id);
  }
  
  public String getMetricIdByName(String name){
    MetricInfoEntry entry = this.getMetricNameToEntry().get(name);
    if(entry != null){
      return entry.getMetricId();
    }
    return null;
  }
  

  class UpdateMetricThread implements Runnable {

    @Override
    public void run() {
      while (true) {
        try {
          Thread.sleep(updateInterval);
          MetricConfCache.this.getMetricConfData();
        } catch (InitMonitorException e) {
          LOG.error(e);
        } catch (InterruptedException e1) {
          LOG.error(e1);
        }
      }

    }
  }

  public static void main(String[] args) {
      MetricConfCache.getInstance();
  }

}
