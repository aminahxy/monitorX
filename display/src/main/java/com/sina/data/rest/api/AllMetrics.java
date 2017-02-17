package com.sina.data.rest.api;

import com.sina.data.bean.ClusterHostBean;
import com.sina.data.bean.MetricBean;
import com.sina.data.constant.Constants;
import com.sina.data.hbase.HBaseEnv;
import com.sina.data.util.JsonConvert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.json.JSONArray;
import org.json.JSONException;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by lile1 on 2015/12/1.
 */
public class AllMetrics extends BaseResource{

    Log LOG = LogFactory.getLog(AllMetrics.class);

    Table table;

    @Override
    void init() {
        try {
            table = HBaseEnv.getTable(Constants.MetricConfTableName);
        } catch (IOException e) {
            LOG.error("get "+Constants.MetricConfTableName + " fail.",e);
        }
    }

    @Override
    protected Representation postOper(Representation entity) throws ResourceException {
        try{
            if(table == null){
                return status("-1","table object:"+Constants.MetricConfTableName +" is null.");
            }
            List<MetricBean> metricBeans = getMetricBeans();
            JSONArray data = JsonConvert.convertMetricBeansToJsonArray(metricBeans);
            return status(SUCCESS_CODE,SUCCESS_DESC,data);
        }catch (IOException e){
            return status("-1",e.getMessage());
        }catch (JSONException e){
            return status("-1",e.getMessage());
        }
    }

    List<MetricBean> getMetricBeans() throws IOException{
        List<MetricBean> metricBeans = new ArrayList<MetricBean>();
        Scan scan = new Scan();
        scan.addFamily(Constants.MetricConfTableName_cf_c1.getBytes());
        ResultScanner rs = null;
        try {
            rs = table.getScanner(scan);
            Iterator<Result> it = rs.iterator();
            while (it.hasNext()){
                Result result = it.next();
                String metricName = Bytes.toString(result.getRow());
                String metricId = Bytes.toString(result.getValue(Constants.MetricConfTableName_cf_c1.getBytes(),
                        Constants.MetricConfTableName_cf_col_id.getBytes()));
                String metricType = Bytes.toString(result.getValue(Constants.MetricConfTableName_cf_c1.getBytes(),
                        Constants.MetricConfTableName_cf_col_type.getBytes()));
                MetricBean bean = new MetricBean(metricName,metricId,metricType);
                metricBeans.add(bean);
            }
        } catch (IOException e) {
            LOG.error("scan table "+Constants.MetricConfTableName+" error.",e);
            throw e;
        } finally {
            if(rs != null) {
                rs.close();
            }
        }
        return metricBeans;
    }

}
