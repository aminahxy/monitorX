package com.sina.data;

import com.sina.data.entry.AlertEntry;
import com.sina.data.hbase.HbaseUtils;
import com.sina.data.service.ComputeContext;
import com.sina.data.service.MonitorXComputeWorker;
import com.sina.data.util.ConfUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class ComputeServer {

    private static final Logger LOG = Logger.getLogger(ComputeServer.class);

    public static void main(String[] args) throws IOException{

        //启动判断过程
        Configuration conf = new Configuration();
        conf.set("hbase.zookeeper.quorum",ConfUtils.getString("monitor.hbase.zk",
                "10.39.3.85:2181,10.39.3.86:2181,10.39.3.87:2181"));
        HbaseUtils.initConn(conf);

        ComputeContext context = ComputeContext.getInstance();
        Set<String> currMonitors = new HashSet<String>();

        while (true){
            try {
                //假如加入的告警指标是一个新的集群，根据cluster_monitor_register表的cluster_name字段判断，就启动一个新的MonitorXComputeWorker去处理对应集群的告警
                Map<String,Map<String,AlertEntry>> monitorRegisters = context.getMonitorRegisterTable().getMonitors();
                Set<String> monitors = monitorRegisters.keySet();
                for(String monitorId : monitors){
                    String trimMonitorId = monitorId == null ? monitorId : monitorId.trim();
                    if(currMonitors.contains(trimMonitorId)){
                       continue;
                    }
                    MonitorXComputeWorker worker = new MonitorXComputeWorker(trimMonitorId,context);
                    worker.start();
                    currMonitors.add(trimMonitorId);
                    LOG.info("have new Cluster:("+monitorId+") alarm join on.");
                }

                Thread.sleep(60*1000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
