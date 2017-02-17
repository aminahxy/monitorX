package com.sina.data.util;

import com.sina.data.bean.MetricBean;
import com.sina.data.constant.Constants;
import com.sina.data.hbase.HBaseEnv;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.TimestampsFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Filter;

/**
 * Created by lile1 on 2016/1/22.
 */
public class Find721MetricsUtil {

    private static Log LOG = LogFactory.getLog(Find721MetricsUtil.class);

    // host:10.39.7.21
    // metrics: disk_free,cpu_io
    // startTime = "2015-01-22 13:44:44"
    // endTime = "2015-01-22 13:44:50"

    private static List<String> metricIdList = new ArrayList<String>();

    private static Map<String,String> metricId2metricName = new HashMap<String, String>();

    private static Map<String,String> metricName2metricId = new HashMap<String, String>();


    public static void main(String[] args) throws IOException{

        String host = args[0];
        String metrics = args[1];
        String startTime = args[2];
        String endTime = args[3];
        String outFilePath = args[4];

        File outFile = new File(outFilePath);
        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(outFile)));

        LOG.info("args[host:"+host+",metrics:"+metrics+",startTime:"+startTime+",endTime:"+endTime+"]");
        System.out.println("args[host:"+host+",metrics:"+metrics+",startTime:"+startTime+",endTime:"+endTime+"]");

        try {

            Configuration conf = new Configuration();
            conf.addResource(new FileInputStream("conf/HBaseClientSetting.xml"));
            LOG.info(conf.get("hbase.zookeeper.quorum"));
            HBaseEnv.initConn(conf);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");

            Table metrciConfTab = HBaseEnv.getTable(Constants.MetricConfTableName);
            getMetricId2NameMap(metrciConfTab);


            Table hostOrigTab = HBaseEnv.getTable(Constants.Metric_Host_Data_Orig);
            Scan scan = new Scan();
            String startRowStr;
            String endRowStr;

            List<String> speMetricList = new ArrayList<String>();

            //1. 不带metric的或者是all的查询所有
            if(metrics==null || metrics.length() ==0 || metrics.equals("all")) {


                speMetricList = metricIdList;

            }else{//2.带metric的可以构造rowkey范围查询

                String[] metricNames = metrics.split(",");

                for(String metricName : metricNames){
                    String metricId = metricName2metricId.get(metricName);
                    speMetricList.add(metricId);
                }

            }

            for(String metricArgId : speMetricList) {
                startRowStr = host + "#" + metricArgId + "#" + (sdf.parse(startTime).getTime() / 1000);
                endRowStr = host + "#" + metricArgId + "#" + (sdf.parse(endTime).getTime() / 1000);

                LOG.info("startRowStr=" + startRowStr + "  endRowStr=" + endRowStr);
                System.out.println("startRowStr=" + startRowStr + "  endRowStr=" + endRowStr);

                scan.setStartRow(Bytes.toBytes(startRowStr));
                scan.setStopRow(Bytes.toBytes(endRowStr));

                ResultScanner rsScanner = hostOrigTab.getScanner(scan);
                Iterator<Result> it = rsScanner.iterator();
                Map<String, List<String>> metricsMap = new HashMap<String, List<String>>();
                while (it.hasNext()) {
                    Result result = it.next();
                    String rowkey = Bytes.toString(result.getRow());
                    String metricValue = Bytes.toString(result.getValue("c1".getBytes(), "d".getBytes()));
                    String[] rowArr = rowkey.split("#");
                    String metricId = rowArr[1];
                    long millSec = Long.parseLong(rowArr[2] + "000");
                    String formatedTime = sdf2.format(new Date(millSec));
                    if (metricsMap.get(metricId) == null) {
                        List<String> list = new ArrayList<String>();
                        list.add(formatedTime + "  " + metricValue);
                        metricsMap.put(metricId, list);
                    } else {
                        List<String> list = metricsMap.get(metricId);
                        list.add(formatedTime + "  " + metricValue);
                    }
                }

                Set<String> metricIDSet = metricsMap.keySet();
                for (String metricId : metricIDSet) {
                    String metricName = metricId2metricName.get(metricId);
                    List<String> TS_Val = metricsMap.get(metricId);
                    pw.println(metricName);
                    pw.println("-------------------------------------------------------------------------");
                    for (String s : TS_Val) {
                        pw.println(s);
                    }
                    pw.println("-------------------------------------------------------------------------");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            HBaseEnv.closeConn();
            System.exit(-1);
        }finally {
            if(pw != null){
                pw.close();
            }
        }
    }


    public static void getMetricId2NameMap(Table metrciConfTab) throws IOException{
        Scan scan = new Scan();
        scan.addFamily(Constants.MetricConfTableName_cf_c1.getBytes());
        ResultScanner rs = null;
        try {
            rs = metrciConfTab.getScanner(scan);
            Iterator<Result> it = rs.iterator();
            while (it.hasNext()){
                Result result = it.next();
                String metricName = Bytes.toString(result.getRow());
                String metricId = Bytes.toString(result.getValue(Constants.MetricConfTableName_cf_c1.getBytes(),
                        Constants.MetricConfTableName_cf_col_id.getBytes()));
                metricId2metricName.put(metricId, metricName);
                metricName2metricId.put(metricName,metricId);
                metricIdList.add(metricId);
            }
        } catch (IOException e) {
            LOG.error("scan table "+Constants.MetricConfTableName+" error.",e);
            throw e;
        } finally {
            if(rs != null) {
                rs.close();
            }
        }
    }

}
