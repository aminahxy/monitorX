package com.sina.data.util;

import com.sina.data.bean.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by lile1 on 2015/12/3.
 */
public class JsonConvert {

    public static JSONArray convertClusterHostBeanListToJsonArray
            (List<ClusterHostBean> clusterHostBeanList) throws JSONException{
        JSONArray jsonArray = new JSONArray();
        for(ClusterHostBean bean : clusterHostBeanList){
            JSONObject jsonObject = new JSONObject();
            String clusterName = bean.getClusterName();
            String hostsStr = bean.getHosts().toString();
            jsonObject.put(clusterName,hostsStr.substring(1,hostsStr.length()-1));
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }


    public static JSONArray convertMetricBeansToJsonArray
            (List<MetricBean> metricBeans) throws JSONException{
        JSONArray jsonArray = new JSONArray();
        for(MetricBean bean : metricBeans){
            JSONObject jsonObject = new JSONObject();
            String id = bean.getMetricId();
            String name = bean.getMetricName();
            jsonObject.put(name,id);
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }

    public static JSONArray convertCompareAmongHostsBeansToJsonArray
            (CompareAmongHostsBean compareAmongHostsBean) throws JSONException{
        JSONArray jsonArray = new JSONArray();
        Map<String,Map<String,List<TimeStamp2Value>>> metric2TSValsMap = compareAmongHostsBean.getHost2MetricId2TSValsMap();
        Iterator<Map.Entry<String,Map<String,List<TimeStamp2Value>>>>  it = metric2TSValsMap.entrySet().iterator();
        while (it.hasNext()){
            JSONObject jsonObject = new JSONObject();
            Map.Entry<String,Map<String,List<TimeStamp2Value>>> entry = it.next();
            String host = entry.getKey();
            JSONArray  metricsJsonArray = new JSONArray();
            Iterator<Map.Entry<String,List<TimeStamp2Value>>> subIt = entry.getValue().entrySet().iterator();
            while (subIt.hasNext()){
                Map.Entry<String,List<TimeStamp2Value>> subEntry = subIt.next();
                String metric = subEntry.getKey();
                List<TimeStamp2Value> timeStamp2ValueList = subEntry.getValue();
                JSONObject metricJsonObj = new JSONObject();
                JSONArray timeStampJsonArr = new JSONArray();
                for(TimeStamp2Value timeStamp2Value : timeStamp2ValueList){
                    JSONObject timeStampJsonObj = new JSONObject();
                    timeStampJsonObj.put(timeStamp2Value.getTimeStamp(),timeStamp2Value.getValue());
                    timeStampJsonArr.put(timeStampJsonObj);
                }
                metricJsonObj.put(metric,timeStampJsonArr);
                metricsJsonArray.put(metricJsonObj);
            }
            jsonObject.put(host,metricsJsonArray);
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }

    public static JSONArray convertCompareWithLastBeansToJsonArray
            (CompareWithLastBean compareWithLastBean) throws JSONException{
        JSONArray hostJsonArray = new JSONArray();

        Map<String,Map<String,Map<String,List<TimeStamp2Value>>>> host2last2metric2TSValsMap =
                compareWithLastBean.getHost2last2metric2TSValsMap();

        Iterator<Map.Entry<String,Map<String,Map<String,List<TimeStamp2Value>>>>> it = host2last2metric2TSValsMap.entrySet().iterator();
        while (it.hasNext()){
            JSONObject hostJsonObject = new JSONObject();
            Map.Entry<String,Map<String,Map<String,List<TimeStamp2Value>>>> entry = it.next();
            String host = entry.getKey();
            JSONObject lastAndSpeJsonObj = new JSONObject();
            JSONArray  lastAndSpeJsonArray = new JSONArray();
            Iterator<Map.Entry<String,Map<String,List<TimeStamp2Value>>>> subIt = entry.getValue().entrySet().iterator();
            while (subIt.hasNext()){
                Map.Entry<String,Map<String,List<TimeStamp2Value>>> subEntry = subIt.next();
                String lastAndSpe = subEntry.getKey();
                Map<String,List<TimeStamp2Value>> metric2TSValsMap = subEntry.getValue();
                Iterator<Map.Entry<String,List<TimeStamp2Value>>> subSubIt = metric2TSValsMap.entrySet().iterator();
                JSONArray metricJsonArr = new JSONArray();
                while (subSubIt.hasNext()){
                    Map.Entry<String,List<TimeStamp2Value>> subSubEntry = subSubIt.next();
                    List<TimeStamp2Value> timeStamp2ValueList= subSubEntry.getValue();
                    String metricId = subSubEntry.getKey();
                    JSONObject metricJsonObj = new JSONObject();
                    JSONArray timeStampJsonArr = new JSONArray();
                    for(TimeStamp2Value timeStamp2Value : timeStamp2ValueList){
                        JSONObject timeStampJsonObj = new JSONObject();
                        timeStampJsonObj.put(timeStamp2Value.getTimeStamp(),timeStamp2Value.getValue());
                        timeStampJsonArr.put(timeStampJsonObj);
                    }
                    metricJsonObj.put(metricId,timeStampJsonArr);
                    metricJsonArr.put(metricJsonObj);
                }
                lastAndSpeJsonObj.put(lastAndSpe,metricJsonArr);
                lastAndSpeJsonArray.put(lastAndSpeJsonObj);
            }
            hostJsonObject.put(host,lastAndSpeJsonArray);
            hostJsonArray.put(hostJsonObject);
        }
        return hostJsonArray;
    }

}
