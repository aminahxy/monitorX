package com.sina.data.bean;

/**
 * Created by lile1 on 2015/12/3.
 */
public class TimeStamp2Value {

    private String timeStamp;
    private String value;

    public TimeStamp2Value(String timeStamp, String value) {
        this.timeStamp = timeStamp;
        this.value = value;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
