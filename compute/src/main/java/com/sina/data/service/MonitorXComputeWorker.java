package com.sina.data.service;

import com.sina.data.common.JudgeType;
import com.sina.data.cons.AppRegConstant;
import com.sina.data.entry.AlarmEntry;
import com.sina.data.entry.AlertEntry;
import com.sina.data.entry.MetricColumnEntry;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by lile1 on 2015/12/14.
 */
    public class MonitorXComputeWorker extends ComputeWorker{

    private static final Logger LOG = Logger.getLogger(MonitorXComputeWorker.class);

    private DataFetcher dataFetcher;

    public MonitorXComputeWorker(String monitorId, ComputeContext context) {
        super(context);
        this.monitorId = monitorId;
        this.dataFetcher = new DataFetcher(monitorId);
        this.lastTime = dataFetcher.getLatestTime();
    }

    @Override
    public void run() {

        while (!stop && !Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(1000);
                // to check if time to get data from hbase
                long nowTime = this.dataFetcher.getLatestTime();
                if (lastTime == nowTime) {
                    // no new metric data
                    continue;
                }

                // new metric data has been ready
                LOG.info(monitorId + " : worker start to compute.");


                //the monitored metrics of this app
                //this data from mysql
                Map<String,AlertEntry> alertsEntry = this.context.getMonitorRegisterTable().getAlertEntries(monitorId);
                if (alertsEntry == null || alertsEntry.size() == 0) {
                    LOG.warn("monitorId=" + this.monitorId
                            + " have no metrics to be monitored.Please Check");
                    this.lastTime = nowTime;
                    continue;
                }

                this.lastTime = nowTime;

                //a section of row key
                String rowkey = this.dataFetcher.getRowkeyTime() + AppRegConstant.ROWKEY_SEP;

                Map<String,List<MetricColumnEntry>> dataCache = this.dataFetcher.getDataCache();

                Iterator<AlertEntry> aes = alertsEntry.values().iterator();

                while(aes.hasNext()){
                    AlertEntry alert = aes.next();

                    String metricId = this.context.getMetricConfTable().getMetricIdByName(alert.getMetricName().trim());

                    String[] nodeList = alert.getHostList().split(",");

                    for(String nodeIp : nodeList){

                        String row = this.monitorId+AppRegConstant.ROWKEY_SEP+rowkey+metricId+AppRegConstant.ROWKEY_SEP+nodeIp;
                        LOG.debug("rowkey=" + row);
                        List<MetricColumnEntry> data = dataCache.get(row);

                        //if one node have no metric data ,then alarm
                        if(data==null||data.size()==0){
                            AlarmEntry entry = new AlarmEntry();
                            //0 is a specific template of warning message
                            entry.setId("0");
                            entry.setMonitorId(this.monitorId);
                            entry.setAppType(alert.getMonitorType());
                            entry.setAppId(alert.getId());
                            entry.setExtend(alert.getMetricName() + " have no data");
                            entry.setTimestamp(nowTime);
                            entry.setJudgeType(JudgeType.no_data);
                            entry.setMetricName(alert.getMetricName());
                            entry.setNode(nodeIp);
                            context.getAlertService().enQueue(entry);
                            LOG.warn("row="+row+" not data found.");
                            continue;
                        }

                        for(MetricColumnEntry column:data){
                            double value = Double.parseDouble(column.value);
                            LOG.debug("-value-" + value + "-thorded-" + alert.getEmailThreshold() + "--" + (value - alert.getEmailThreshold() > 0));

                            if (super.needAlert(alert.getEmailThreshold(), value,
                                    alert.getJudgeType())) {
                                AlarmEntry entry = new AlarmEntry();
                                entry.setId(alert.getId());
                                entry.setMonitorId(this.monitorId);
                                entry.setAppType(alert.getMonitorType());
                                entry.setAppId(alert.getId());
                                entry.setCurrentValue(column.value);
                                entry.setExtend("nothing");
                                entry.setJudgeType(alert.getJudgeType());
                                entry.setMetricName(alert.getMetricName());
                                entry.setRegister_name(alert.getRegisterName());
                                entry.setThreshold(String.valueOf(alert.getEmailThreshold()));
                                entry.setTimestamp(nowTime);
                                entry.setComponent(column.name);
                                entry.setNode(nodeIp);
                                context.getAlertService().enQueue(entry);
                                LOG.info(alertString(alert, value, this.monitorId));

                                //just send one warning if all component is wrong
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error(e);
            }
        }
    }
}
