package com.sina.data.bean;

/**
 * Created by lile1 on 2015/12/3.
 */
public class MetricBean {

    String metricName;
    String metricId;
    String type;

    public MetricBean() {
    }

    public MetricBean(String metricName, String metricId) {
        this.metricName = metricName;
        this.metricId = metricId;
    }

    public MetricBean(String metricName, String metricId, String type) {
        this(metricName,metricId);
        this.type = type;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public String getMetricId() {
        return metricId;
    }

    public void setMetricId(String metricId) {
        this.metricId = metricId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
