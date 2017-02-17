package com.sina.data.bean;

import java.util.List;
import java.util.Map;

/**
 * Created by lile1 on 2015/12/3.
 */
public class CompareWithLastBean {

    //Map<host,Map<last,Map<metricId,List<TimeStamp,metricVal>>>>
    Map<String,Map<String,Map<String,List<TimeStamp2Value>>>> host2last2metric2TSValsMap;


    public Map<String, Map<String, Map<String, List<TimeStamp2Value>>>> getHost2last2metric2TSValsMap() {
        return host2last2metric2TSValsMap;
    }

    public void setHost2last2metric2TSValsMap(Map<String, Map<String, Map<String, List<TimeStamp2Value>>>> host2last2metric2TSValsMap) {
        this.host2last2metric2TSValsMap = host2last2metric2TSValsMap;
    }
}
