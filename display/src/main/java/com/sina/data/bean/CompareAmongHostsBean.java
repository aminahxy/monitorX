package com.sina.data.bean;

import java.util.List;
import java.util.Map;

/**
 * Created by lile1 on 2015/12/3.
 */
public class CompareAmongHostsBean {


    //Map<host,Map<metricId,List<TimeStamp,metricVal>>>
    private Map<String,Map<String,List<TimeStamp2Value>>> host2MetricId2TSValsMap;


    public Map<String, Map<String, List<TimeStamp2Value>>> getHost2MetricId2TSValsMap() {
        return host2MetricId2TSValsMap;
    }

    public void setHost2MetricId2TSValsMap(Map<String, Map<String, List<TimeStamp2Value>>> host2MetricId2TSValsMap) {
        this.host2MetricId2TSValsMap = host2MetricId2TSValsMap;
    }
}
