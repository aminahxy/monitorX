package com.sina.data.hbase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;

import java.io.IOException;

public class HbaseUtils {

  private static Log LOG = LogFactory.getLog(HbaseUtils.class);


  static Connection hbaseConn;

  public static void initConn(Configuration conf) throws IOException{
    if(hbaseConn != null)
      return;
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
