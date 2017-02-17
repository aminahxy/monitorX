package com.sina.data.service;

import com.sina.data.cons.AppRegConstant;
import com.sina.data.entry.MetricColumnEntry;
import com.sina.data.hbase.HbaseUtils;
import com.sina.data.util.ConfUtils;
import com.sina.data.util.SimpleClockLock;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This Class fetch the data of BigMonitorMetricDataAlert
 */
public class DataFetcher {
    private static final Logger LOG = Logger.getLogger(DataFetcher.class);

    private Map<String, List<MetricColumnEntry>> dataCache;

    // the latest time of fetcher data
    private volatile long latestTime;

    private String tableName;
    private long delayTime;

    private volatile long rowkeyTime;

    private String monitorId;

//    private static DataFetcher INSTANCE;

//    public static DataFetcher getInstance() {
//        if (INSTANCE == null)
//            INSTANCE = new DataFetcher();
//        return INSTANCE;
//    }

    public DataFetcher(String monitorId){
        this.monitorId = monitorId;
        this.tableName = ConfUtils.getString(
                "monitor.hbase.cluster.data.table.name",
                AppRegConstant.hbase_namespace + AppRegConstant.APP_CLUSTER_METRIC_DATA_TABLE_NAME);
        this.latestTime = -1l;
        this.delayTime = ConfUtils.getLong("monitor.compute.fetche.data.delay", 30) / 15 * 15;//delayTime  300s 5分钟


        this.init();
        TimerTask tt = new TimerTask() {

            @Override
            public void run() {
                long cur = SimpleClockLock.MonitorTimer.getTime();

                // it is timer to get new data from hbase
              //  LOG.debug("curTime:" + cur + " delayTime:" + delayTime + " latestTime:" + latestTime);
                if ((cur - delayTime) != latestTime) {
                    toScanData(cur - delayTime, cur - delayTime + 15); //每次扫15s的
                }
            }
        };
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(tt, 120000, 3000);//3s

    }

    public void init() {
        long time = SimpleClockLock.MonitorTimer.getTime();
        this.toScanData(time - delayTime, time);
    }

    private void toScanData(long startTime, long endTime) {
        LOG.debug("startTime:" + startTime + " endTime:" + endTime);
        Map<String, List<MetricColumnEntry>> tmp_dataCache = new ConcurrentHashMap<String, List<MetricColumnEntry>>();

        Scan scan = new Scan();
        String startRow = monitorId + AppRegConstant.ROWKEY_SEP + startTime + AppRegConstant.ROWKEY_SEP;
        String endRow = monitorId + AppRegConstant.ROWKEY_SEP + endTime + AppRegConstant.ROWKEY_SEP;
        scan.setStartRow(Bytes.toBytes(startRow));
        scan.setStopRow(Bytes.toBytes(endRow));
        LOG.debug("startKey="+ startRow + "  endKey="+endRow);
        scan.addFamily(AppRegConstant.COLUMN_FAMILY_COMMON1_BYTES);

        Table table = null;
        ResultScanner results = null;
        try {
            table = HbaseUtils.getTable(this.tableName);
            results = table.getScanner(scan);
            Iterator<Result> its = results.iterator();
            String rowkey = null;
            while (its.hasNext()) {
                Result result = its.next();
                rowkey = Bytes.toString(result.getRow());

                NavigableMap<byte[], byte[]> colums = result
                        .getFamilyMap(AppRegConstant.COLUMN_FAMILY_COMMON1_BYTES);
                List<MetricColumnEntry> ms = new LinkedList<MetricColumnEntry>();
                for (Entry<byte[], byte[]> entry : colums.entrySet()) {
                    ms.add(new MetricColumnEntry(Bytes.toString(entry.getKey()), Bytes
                            .toString(entry.getValue())));
                }
                tmp_dataCache.put(rowkey, ms);
            }
            synchronized (this) {
                this.dataCache = tmp_dataCache;
                LOG.debug("-------cache size ----" + this.dataCache.size());
                this.latestTime = startTime;
                if (rowkey != null) {
                    int firstIndex = rowkey.indexOf(AppRegConstant.ROWKEY_SEP);
                    int secondIndex = rowkey.indexOf(AppRegConstant.ROWKEY_SEP,firstIndex+1);
                    rowkeyTime = Long.parseLong(rowkey.substring(firstIndex+1,secondIndex));
                }
            }
        } catch (IOException e) {
            LOG.error(e);
        } finally {
            if (results != null) {
                results.close();
            }
            try {
                if (table != null)
                    table.close();
            } catch (IOException e) {
                LOG.error(e);
            }

        }

        LOG.debug("Load metric data alter finished size "
                + dataCache.size() + " at time=" + startTime);
    }

    public long getLatestTime() {
        synchronized (this){
            return latestTime;
        }
    }

    public synchronized long getRowkeyTime() {
        synchronized (this) {
            return rowkeyTime;
        }
    }

    public Map<String, List<MetricColumnEntry>> getDataCache() {
        return dataCache;
    }

    public void setDataCache(Map<String, List<MetricColumnEntry>> dataCache) {
        this.dataCache = dataCache;
    }

    /**
     * This performance may be low
     *
     * @param rowkey
     * @return
     */
    public List<MetricColumnEntry> getMonitorXMetricValue(String rowkey) {
        synchronized (this) {
            return this.dataCache.get(rowkey);
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        conf.set("hbase.zookeeper.quorum", "10.39.3.80:2181,10.39.3.79:2181,10.39.3.78:2181");
        HbaseUtils.initConn(conf);
        DataFetcher d = new DataFetcher("kafka");
        while (true) {
            Thread.sleep(5000);
        }
    }

}
