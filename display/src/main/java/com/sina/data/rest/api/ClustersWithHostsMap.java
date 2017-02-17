package com.sina.data.rest.api;

import com.sina.data.bean.ClusterHostBean;
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
import java.util.NavigableMap;

/**
 * Created by lile1 on 2015/12/1.
 */
public class ClustersWithHostsMap extends BaseResource{

    Log LOG = LogFactory.getLog(ClustersWithHostsMap.class);

    Table table;

    @Override
    void init() {
        try {
            table = HBaseEnv.getTable(Constants.ClusterConfTableName);
        } catch (IOException e) {
            LOG.error("get "+Constants.ClusterConfTableName + " fail.",e);
        }
    }

    @Override
    protected Representation postOper(Representation entity) throws ResourceException {
        try{
            if(table == null){
                return status("-1","table object:"+Constants.ClusterConfTableName +" is null.");
            }
            List<ClusterHostBean> clusterHostBeanList = getClusterHostBeans();
            JSONArray data = JsonConvert.convertClusterHostBeanListToJsonArray(clusterHostBeanList);
            return status(SUCCESS_CODE,SUCCESS_DESC,data);
        }catch (IOException e){
            return status("-1",e.getMessage());
        }catch (JSONException e){
            return status("-1",e.getMessage());
        }
    }

    List<ClusterHostBean> getClusterHostBeans() throws IOException{
        List<ClusterHostBean> clusterHostBeanList = new ArrayList<ClusterHostBean>();
        Scan scan = new Scan();
        scan.addFamily(Constants.ClusterConfTableName_cf_c1.getBytes());
        ResultScanner rs = null;
        try {
            rs = table.getScanner(scan);
            Iterator<Result> it = rs.iterator();
            while (it.hasNext()){
                Result result = it.next();
                String clusterName = Bytes.toString(result.getRow());

                NavigableMap<byte[],byte[]> qulifier2valmap =  result.getFamilyMap(Constants.ClusterConfTableName_cf_c1.getBytes());
                List<String> hosts = new ArrayList<String>();
                for(Iterator<byte[]> its = qulifier2valmap.keySet().iterator();its.hasNext();){
                    byte[] rr = its.next();
                    hosts.add(Bytes.toString(rr));
                }
                ClusterHostBean bean = new ClusterHostBean(clusterName,hosts);
                clusterHostBeanList.add(bean);
            }
        } catch (IOException e) {
            LOG.error("scan table "+Constants.ClusterConfTableName+" error.",e);
            throw e;
        } finally {
            if(rs != null) {
                rs.close();
            }
        }
        return clusterHostBeanList;
    }
}
