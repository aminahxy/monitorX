package com.sina.data.util;

import com.sina.data.constant.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lile1 on 2015/12/3.
 */
public class RequestParam {

    public static Map<TimeUnitType,String> LastTypeToClusterTables = new HashMap<TimeUnitType, String>();
    public static Map<TimeUnitType,String> LastTypeToHostTables = new HashMap<TimeUnitType, String>();



    static {
        LastTypeToClusterTables.put(TimeUnitType.ORIG, Constants.Metric_Cluster_Data_Orig);
        LastTypeToClusterTables.put(TimeUnitType.HOUR, Constants.Metric_Cluster_Data_Hour);
        LastTypeToClusterTables.put(TimeUnitType.DAY, Constants.Metric_Cluster_Data_Day);
        LastTypeToClusterTables.put(TimeUnitType.WEEK, Constants.Metric_Cluster_Data_Week);
        LastTypeToClusterTables.put(TimeUnitType.MONTH, Constants.Metric_Cluster_Data_Month);

        LastTypeToHostTables.put(TimeUnitType.ORIG,Constants.Metric_Host_Data_Orig);
        LastTypeToHostTables.put(TimeUnitType.HOUR,Constants.Metric_Host_Data_Hour);
        LastTypeToHostTables.put(TimeUnitType.DAY,Constants.Metric_Host_Data_Day);
        LastTypeToHostTables.put(TimeUnitType.WEEK,Constants.Metric_Host_Data_Week);
        LastTypeToHostTables.put(TimeUnitType.MONTH,Constants.Metric_Host_Data_Month);

    }

    public enum TimeUnitType {
        ORIG(""),
        HOUR("h"),
        DAY("d"),
        WEEK("w"),
        MONTH("m");

        private String type;

        TimeUnitType(String str){
            this.type = str;
        }

        public static TimeUnitType getLastType(String type){
            if(type.equalsIgnoreCase("h")){
                return HOUR;
            }else if(type.equalsIgnoreCase("d")){
                return DAY;
            }else if(type.equalsIgnoreCase("w")){
                return WEEK;
            }else if(type.equalsIgnoreCase("m")){
                return MONTH;
            }else {
                return ORIG;
            }
        }

    }


    public static TimeUnitType getTargetTableByTime(long startTimeMillSec,long endTimeMillSec){
        long internal = endTimeMillSec - startTimeMillSec;
        if(internal / (60*60*1000) <= 0){// <1h
            return TimeUnitType.ORIG;
        }else if (internal / (24*60*60*1000) <= 0){//<1d
            return TimeUnitType.HOUR;
        }else if(internal / (7*24*60*60*1000) <= 0){//1w
            return TimeUnitType.DAY;
        }else if(internal / (30*24*60*60*1000) <= 0){//1m
            return TimeUnitType.WEEK;
        }else{
            return TimeUnitType.MONTH;
        }
    }

    public static String getStartTimeSecBaseLast(String lastTypeStr, String lastValue){
        long currTime = System.currentTimeMillis();
        long internal = 0L;
        if(lastTypeStr.equalsIgnoreCase("h")){
            internal = Integer.parseInt(lastValue) * 60*60*1000;
        }else if (lastTypeStr.equalsIgnoreCase("d")){
            internal = Integer.parseInt(lastValue) * 24*60*60*1000;
        }else if (lastTypeStr.equalsIgnoreCase("w")){
            internal = Integer.parseInt(lastValue) * 7*24*60*60*1000;
        }else if (lastTypeStr.equalsIgnoreCase("m")){
            internal = Integer.parseInt(lastValue) * 30*24*60*60*1000;
        }
        long startSec = (currTime - internal)/1000;
        return String.valueOf(startSec);
    }

    public static String getEndTimeSecBaseLast(String lastTypeStr, String count, long startTime){
        long internal = 0L;
        if(lastTypeStr.equalsIgnoreCase("h")){
            internal = Integer.parseInt(count) * 60*60*1000;
        }else if (lastTypeStr.equalsIgnoreCase("d")){
            internal = Integer.parseInt(count) * 24*60*60*1000;
        }else if (lastTypeStr.equalsIgnoreCase("w")){
            internal = Integer.parseInt(count) * 7*24*60*60*1000;
        }else if (lastTypeStr.equalsIgnoreCase("m")){
            internal = Integer.parseInt(count) * 30*24*60*60*1000;
        }
        long nextSec = (startTime + internal)/1000;
        return String.valueOf(nextSec);
    }


}
