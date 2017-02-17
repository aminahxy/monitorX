package com.sina.data.bean;

import java.util.List;

/**
 * Created by lile1 on 2015/12/3.
 */
public class ClusterHostBean {

    private String clusterName;
    private List<String> hosts;


    public ClusterHostBean(String clusterName,List<String> hosts){
        this.clusterName = clusterName;
        this.hosts = hosts;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }
}
