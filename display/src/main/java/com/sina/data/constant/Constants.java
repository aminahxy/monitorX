package com.sina.data.constant;

/**
 * Created by lile1 on 2015/12/1.
 */
public class Constants {

    public static final String REST_SERVER_PORT = "rest.server.port";
    public static final String REST_SERVER_CONTEXT = "rest.server.context";


    public static String hbase_namespace="ns_hadoopadmin:";
    /**元信息表*/
    public static String ClusterConfTableName = hbase_namespace+"BigMonitorClusterConfTable";
    public static String ClusterConfTableName_cf_c1 = "c1";
    public static String MetricConfTableName = hbase_namespace+"BigMonitorMetricConfTable";
    public static String MetricConfTableName_cf_c1 = "c1";
    public static String MetricConfTableName_cf_col_id = "id";
    public static String MetricConfTableName_cf_col_type = "type";
    public static String HostConfTableName = hbase_namespace+"BigMonitorHostConfTable";

    /**集群指标表*/
    public static String Metric_Cluster_Data_Prefix = hbase_namespace+"BigMonitorMetricDataCluster";
    public static String Metric_Cluster_Data_Orig = Metric_Cluster_Data_Prefix+"Orig";
    public static String Metric_Cluster_Data_Hour = Metric_Cluster_Data_Prefix+"Hour";
    public static String Metric_Cluster_Data_Day = Metric_Cluster_Data_Prefix+"Day";
    public static String Metric_Cluster_Data_Week = Metric_Cluster_Data_Prefix+"Week";
    public static String Metric_Cluster_Data_Month = Metric_Cluster_Data_Prefix+"Month";

    public static String Metric_Cluster_DataX_cf_c1 = "c1";
    public static String Metric_Cluster_DataX_cf_col_d = "d";


    /**机器指标表*/
    public static String Metric_Host_Data_Prefix = hbase_namespace+"BigMonitorMetricDataHost";
    public static String Metric_Host_Data_Orig = Metric_Host_Data_Prefix+"Orig";
    public static String Metric_Host_Data_Hour = Metric_Host_Data_Prefix+"Hour";
    public static String Metric_Host_Data_Day = Metric_Host_Data_Prefix+"Day";
    public static String Metric_Host_Data_Week = Metric_Host_Data_Prefix+"Week";
    public static String Metric_Host_Data_Month = Metric_Host_Data_Prefix+"Month";

    public static String Metric_Host_DataX_cf_c1 = "c1";
    public static String Metric_Host_DataX_cf_col_d = "d";



    /**告警指标表*/
    public static String MetricDataAlertTablePrefix = hbase_namespace+"BigMonitorMetricDataAlert";

    public static final String ROWKEY_SEP = "#";
}
