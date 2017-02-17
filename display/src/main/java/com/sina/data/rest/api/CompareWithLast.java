package com.sina.data.rest.api;

import com.sina.data.bean.CompareWithLastBean;
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
public class CompareWithLast extends BaseResource{

    Log LOG = LogFactory.getLog(CompareWithLast.class);

    Table hostTable;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    void init() {

    }

    @Override
    protected Representation postOper(Representation entity) throws ResourceException {
        JSONObject paramJson = requestedJson;
        try {
            String hostsStr = paramJson.getString("hosts");
            String metricIdsStr = paramJson.getString("metrics");
            String lastStr = paramJson.getString("last");
            String specifyStartTimeStr = paramJson.getString("startTime");

            String lastStartTimeSec;
            String lastEndTimeSec;
            String specifyStartTimeSec;
            String specifyEndTimeSec;

            if (StringUtils.isEmpty(lastStr)) {
            }
            String lastTypeStr = lastStr.substring(lastStr.length() - 1);
            String lastValue =  lastStr.substring(0,lastStr.length() - 1);
            RequestParam.TimeUnitType lastType = RequestParam.TimeUnitType.getLastType(lastTypeStr);
            hostTable = HBaseEnv.getTable(RequestParam.LastTypeToHostTables.get(lastType));

            lastStartTimeSec = RequestParam.getStartTimeSecBaseLast(lastTypeStr, lastValue);
            lastEndTimeSec = String.valueOf(System.currentTimeMillis()/1000); //当前时间

            long specifyStartTimeMillSec = sdf.parse(specifyStartTimeStr).getTime();
            specifyStartTimeSec = String.valueOf(specifyStartTimeMillSec/1000);
            specifyEndTimeSec = RequestParam.getEndTimeSecBaseLast(lastTypeStr, lastValue, specifyStartTimeMillSec);

            CompareWithLastBean compareWithLastBean = getCompareWithLastMetric(
                    hostTable, hostsStr.split(","), metricIdsStr.split(","),
                    lastStartTimeSec, lastEndTimeSec,
                    specifyStartTimeSec,specifyEndTimeSec);
            JSONArray data = JsonConvert.convertCompareWithLastBeansToJsonArray(compareWithLastBean);
            return status(SUCCESS_CODE,SUCCESS_DESC,data);
        } catch (JSONException e) {
            return status("-1",e.getMessage());
        } catch (IOException e) {
            return status("-1",e.getMessage());
        }catch (ParseException e){
            return status("-1",e.getMessage());
        }
    }


    public CompareWithLastBean getCompareWithLastMetric(Table hostTable,
                                                           String[] hosts, String[] metrics,
                                                           String lastStartTimeSec, String lastEndTimeSec,
                                                           String specifyStartTimeSec,String specifyEndTimeSec
    ) throws IOException {

        //TODO  启动多线程去并发查询
        CompareWithLastBean compareWithLastBean = new CompareWithLastBean();
        Map<String,Map<String,Map<String,List<TimeStamp2Value>>>> host2last2metric2TSValsMap =
                new HashMap<String,Map<String,Map<String,List<TimeStamp2Value>>>>();
        compareWithLastBean.setHost2last2metric2TSValsMap(host2last2metric2TSValsMap);

        ResultScanner rs = null;
        try {
            //每个机器的值
            for (String hostName : hosts) {
                Map<String,Map<String,List<TimeStamp2Value>>> last2metric2TSValsMap =
                        new HashMap<String, Map<String, List<TimeStamp2Value>>>();
                for(int i=0; i<=1; i++) {//last和specify分别计算一次
                    Map<String, List<TimeStamp2Value>> metric2TSValsMap = new HashMap<String, List<TimeStamp2Value>>();
                    String subfixStartTime = (i==0?lastStartTimeSec:specifyStartTimeSec);
                    String subfixEndTime = (i==0?lastEndTimeSec:specifyEndTimeSec);
                    for (String metricId : metrics) {
                        String startRow = hostName + Constants.ROWKEY_SEP + metricId + Constants.ROWKEY_SEP + subfixStartTime;
                        String stopRow = hostName + Constants.ROWKEY_SEP + metricId + Constants.ROWKEY_SEP + subfixEndTime;
                        Scan hostScan = new Scan(startRow.getBytes(), stopRow.getBytes());
                        rs = hostTable.getScanner(hostScan);
                        Iterator<Result> it = rs.iterator();
                        List<TimeStamp2Value> timeStamp2ValueList = new ArrayList<TimeStamp2Value>();
                        while (it.hasNext()) {
                            Result result = it.next();
                            String rowKeyStr = Bytes.toString(result.getRow());
                            String timeStamp = rowKeyStr.substring(rowKeyStr.lastIndexOf(Constants.ROWKEY_SEP) + 1);
                            String value = Bytes.toString(result.getValue(Constants.Metric_Host_DataX_cf_c1.getBytes(),
                                    Constants.Metric_Host_DataX_cf_col_d.getBytes()));
                            timeStamp2ValueList.add(new TimeStamp2Value(timeStamp, value));
                        }
                        metric2TSValsMap.put(metricId,timeStamp2ValueList);
                    }
                    if(i==0) {
                        last2metric2TSValsMap.put("last", metric2TSValsMap);
                    }else{
                        last2metric2TSValsMap.put("specify", metric2TSValsMap);
                    }
                }
                host2last2metric2TSValsMap.put(hostName, last2metric2TSValsMap);
            }
        } catch (IOException e) {
            LOG.error("scan table " + hostTable.getName() + " error.", e);
            throw e;
        }finally {
            if(rs != null){
                rs.close();
            }
        }
        return compareWithLastBean;
    }
}
