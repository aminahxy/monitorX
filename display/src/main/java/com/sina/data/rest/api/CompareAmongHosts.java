package com.sina.data.rest.api;

import com.sina.data.bean.CompareAmongHostsBean;
import com.sina.data.bean.TimeStamp2Value;
import com.sina.data.constant.Constants;
import com.sina.data.hbase.HBaseEnv;
import com.sina.data.util.JsonConvert;
import com.sina.data.util.RequestParam;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by lile1 on 2015/12/1.
 */
public class CompareAmongHosts extends BaseResource {

    Log LOG = LogFactory.getLog(CompareAmongHosts.class);

    Table clusterTable;
    Table hostTable;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    void init() {

    }

    @Override
    protected Representation postOper(Representation entity) throws ResourceException {
        JSONObject paramJson = requestedJson;
        String startTimeStr=null;
        String endTimeStr=null;
        try {
            String clusterStr = paramJson.getString("cluster");
            String hostsStr = paramJson.getString("hosts");
            String metricIdsStr = paramJson.getString("metrics");
            String lastStr = paramJson.getString("last");
            startTimeStr = paramJson.getString("startTime");
            endTimeStr = paramJson.getString("endTime");
            String startTimeSec;
            String endTimeSec;
            if (!StringUtils.isEmpty(lastStr)) {
                String lastTypeStr = lastStr.substring(lastStr.length() - 1);
                String lastValue =  lastStr.substring(0,lastStr.length() - 1);
                RequestParam.TimeUnitType lastType = RequestParam.TimeUnitType.getLastType(lastTypeStr);
                clusterTable = HBaseEnv.getTable(RequestParam.LastTypeToClusterTables.get(lastType));
                hostTable = HBaseEnv.getTable(RequestParam.LastTypeToHostTables.get(lastType));
                startTimeSec = RequestParam.getStartTimeSecBaseLast(lastTypeStr, lastValue);
                endTimeSec = String.valueOf(System.currentTimeMillis() / 1000);
            } else {
                long startTimeMillSec = sdf.parse(startTimeStr).getTime();
                long endTimeMillSec = sdf.parse(endTimeStr).getTime();
                startTimeSec = String.valueOf(startTimeMillSec / 1000);
                endTimeSec = String.valueOf(endTimeMillSec / 1000);
                RequestParam.TimeUnitType type = RequestParam.getTargetTableByTime(startTimeMillSec, endTimeMillSec);
                clusterTable = HBaseEnv.getTable(RequestParam.LastTypeToClusterTables.get(type));
                hostTable = HBaseEnv.getTable(RequestParam.LastTypeToHostTables.get(type));
            }
            LOG.info("need scan " + clusterTable.getName() + " and " + hostTable.getName() + " tables." +
                    " startTimeSec:" +startTimeSec  + " endTimeSec:"+endTimeSec);
            CompareAmongHostsBean compareAmongHostsBean = getCompareAmongHostMetric(
                                                                    clusterTable,clusterStr,
                                                                    hostTable, hostsStr.split(","), metricIdsStr.split(","),
                                                                    startTimeSec, endTimeSec);
            JSONArray data = JsonConvert.convertCompareAmongHostsBeansToJsonArray(compareAmongHostsBean);
            LOG.info("result size is: "+data.toString().length());
            return status(SUCCESS_CODE,SUCCESS_DESC,data);
        } catch (JSONException e) {
            LOG.error("json error.",e);
            return status("-1",e.getMessage());
        } catch (IOException e) {
            LOG.error("io error.",e);
            return status("-1",e.getMessage());
        }catch (ParseException e){
            LOG.error("argument error<startTime:"+startTimeStr+" ,entTime:"+endTimeStr);
            return status("-1",e.getMessage());
        }
    }

    public CompareAmongHostsBean getCompareAmongHostMetric(Table clusterTable, String clusterName,
                                                           Table hostTable,
                                                           String[] hosts, String[] metrics,
                                                           String startTimeSec, String endTimeSec) throws IOException {

        //TODO  启动多线程去并发查询
        CompareAmongHostsBean compareAmongHostsBean = new CompareAmongHostsBean();
        Map<String, Map<String, List<TimeStamp2Value>>> host2MetricId2TSValsMap =
                new HashMap<String, Map<String, List<TimeStamp2Value>>>();
        compareAmongHostsBean.setHost2MetricId2TSValsMap(host2MetricId2TSValsMap);

        ResultScanner rs = null;
        try {
            //集群的平均值
            Map<String, List<TimeStamp2Value>> clusterMetric2TSVals = new HashMap<String, List<TimeStamp2Value>>();
            for (String metricId : metrics) {
                String startRow = clusterName + Constants.ROWKEY_SEP + metricId + Constants.ROWKEY_SEP + startTimeSec;
                String stopRow = clusterName + Constants.ROWKEY_SEP + metricId + Constants.ROWKEY_SEP + endTimeSec;
                Scan clusterAvgScan = new Scan(startRow.getBytes(), stopRow.getBytes());
                rs = clusterTable.getScanner(clusterAvgScan);
                Iterator<Result> it = rs.iterator();
                List<TimeStamp2Value> timeStamp2ValueList = new ArrayList<TimeStamp2Value>();
                while (it.hasNext()) {
                    Result result = it.next();
                    String rowKeyStr = Bytes.toString(result.getRow());
                    String timeStamp = rowKeyStr.substring(rowKeyStr.lastIndexOf(Constants.ROWKEY_SEP) + 1);
                    String value = Bytes.toString(result.getValue(Constants.Metric_Cluster_DataX_cf_c1.getBytes(),
                            Constants.Metric_Cluster_DataX_cf_col_d.getBytes()));
                    timeStamp2ValueList.add(new TimeStamp2Value(timeStamp, value));
                }
                clusterMetric2TSVals.put(metricId, timeStamp2ValueList);
            }
            host2MetricId2TSValsMap.put("cluster_avg", clusterMetric2TSVals);
        } catch (IOException e) {
            LOG.error("scan table " + clusterTable.getName() + " error.", e);
            throw e;
        }finally {
            if(rs != null){
                rs.close();
            }
        }

        ResultScanner rs2 = null;
        try {
            //每个机器的值
            for (String hostName : hosts) {
                Map<String, List<TimeStamp2Value>> hostMetric2TSVals = new HashMap<String, List<TimeStamp2Value>>();
                for (String metricId : metrics) {
                    String startRow = hostName + Constants.ROWKEY_SEP + metricId + Constants.ROWKEY_SEP + startTimeSec;
                    String stopRow = hostName + Constants.ROWKEY_SEP + metricId + Constants.ROWKEY_SEP + endTimeSec;
                    Scan hostScan = new Scan(startRow.getBytes(), stopRow.getBytes());
                    rs2 = hostTable.getScanner(hostScan);
                    Iterator<Result> it = rs2.iterator();
                    List<TimeStamp2Value> timeStamp2ValueList = new ArrayList<TimeStamp2Value>();
                    while (it.hasNext()) {
                        Result result = it.next();
                        String rowKeyStr = Bytes.toString(result.getRow());
                        String timeStamp = rowKeyStr.substring(rowKeyStr.lastIndexOf(Constants.ROWKEY_SEP) + 1);
                        String value = Bytes.toString(result.getValue(Constants.Metric_Host_DataX_cf_c1.getBytes(),
                                Constants.Metric_Host_DataX_cf_col_d.getBytes()));
                        timeStamp2ValueList.add(new TimeStamp2Value(timeStamp, value));
                    }
                    hostMetric2TSVals.put(metricId,timeStamp2ValueList);
                }
                host2MetricId2TSValsMap.put(hostName, hostMetric2TSVals);
            }
        } catch (IOException e) {
            LOG.error("scan table " + hostTable.getName() + " error.", e);
            throw e;
        }finally {
            if(rs2 != null){
                rs2.close();
            }
        }
        return compareAmongHostsBean;
    }

}
