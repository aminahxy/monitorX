package com.sina.data.hbase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Table;

import java.io.IOException;

/**
 * Created by lile1 on 2015/12/3.
 */
public class HBaseEnv {
    private static Log LOG = LogFactory.getLog(HBaseEnv.class);


    static Connection hbaseConn;

    public static void initConn(Configuration conf) throws IOException{
        hbaseConn = ConnectionFactory.createConnection(conf);
        LOG.info("--------hbase conn has init..--------");
    }

    public static Table getTable(String tableName) throws IOException{
        TableName tableNameObj = TableName.valueOf(tableName);
        return hbaseConn.getTable(tableNameObj);
    }

    public static void closeConn() throws IOException{
        if(hbaseConn != null && !hbaseConn.isClosed()){
            hbaseConn.close();
        }
    }
}
