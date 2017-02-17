package com.sina.data.alarm.util;


import com.sina.data.common.JudgeType;
import com.sina.data.util.ConfUtils;
import com.sina.data.util.DBPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class MonitorCache extends Thread {
    public static final Logger LOG = LoggerFactory.getLogger(MonitorCache.class);
    private static String loadSql = "select cluster_monitor_register.*,sms_monitor_register.*," +
            "email_monitor_register.* from cluster_monitor_register,email_monitor_register,sms_monitor_register where cluster_monitor_register.register_name=email_monitor_register.register_name and cluster_monitor_register.register_name=sms_monitor_register.register_name; ";

    private static String loadOffline = "select email_monitor_register.*,sms_monitor_register.* from `email_monitor_register` , `sms_monitor_register` where email_monitor_register.register_name = 0 and sms_monitor_register.register_name=0";

    private static String loadUserMappingSql = "SELECT monitor_user,sms_users,email_users FROM monitor_user_mapping";
    private static String loadSmsMonitorRegisterSql = "SELECT register_name,receive_list FROM sms_monitor_register where enable = 1";
    private static String clusterR = "cluster_monitor_register.";
    private static String smsR = "sms_monitor_register.";
    private static String emR = "email_monitor_register.";
    private static MonitorCache instance = new MonitorCache();
    private ConcurrentHashMap<String, MonitorData> map;
    public Map<String, String> smsUserMapping = new HashMap<String, String>();
    public Map<String, String> emailUserMapping = new HashMap<String, String>();
    public Map<String, String> regNameToSmsUsers = new HashMap<String, String>();
    private Connection con;
    private long refreshDaley = ConfUtils.getInt("alarm.reload.daley", 60000);

    private MonitorCache() {
        map = new ConcurrentHashMap<String, MonitorData>();
        con = DBPoolManager.getConnection("monitorX");
        loadData();
        loadUserMapping();
        loadMonitorRegister();
        this.setDaemon(true);
        this.setName("load data thread");
        this.start();
    }

    private void loadData() {
        Statement stat = null;

        try {
            if (con == null || con.isClosed()) {
                con = DBPoolManager.getConnection("monitorX");
            }

            stat = con.createStatement();
            ResultSet rs = stat.executeQuery(loadSql);
            int counter = 0;
            while (rs.next()) {
                counter++;
                String id = rs.getString("register_name");
                String registerName = rs.getString(clusterR + "register_name");
                LOG.info("boyan test");
                LOG.info("load registerName : " + registerName);
                MonitorData data = new MonitorData();
                data.register.setMetricName(rs.getString(clusterR + "metric_name"));
                String hostList = rs.getString(clusterR + "host_list");
                String[] lists = hostList.split(",");
                ArrayList<String> array = new ArrayList<String>();
                if (lists != null) {
                    for (String list : lists)
                        array.add(list);
                }
                data.register.setRegisterName(registerName);
                data.register.setHostList(array);
                data.register.setThreshold(rs.getFloat(clusterR + "threshold"));
                data.register.setMobileThreshold(rs.getFloat(clusterR + "mobile_threshold"));
                String judgeStr = rs.getString(clusterR + "judge_type").toLowerCase();
                JudgeType judgeType =  JudgeType.valueOf(rs.getString(clusterR + "judge_type").toLowerCase());
                data.register.setJudgeType(judgeType);
                data.register.setExtend(rs.getString(clusterR + "extends"));
                String eHostList = rs.getString(emR + "receive_list");
                String[] eLists = eHostList.split(",");
                ArrayList<String> eArray = new ArrayList<String>();
                if (eLists != null) {
                    for (String list : eLists)
                        eArray.add(list);
                }
                data.email.setReceiveList(eArray);
                data.email.setEnable(rs.getBoolean(emR + "enable"));
                data.email.setSentPeople(rs.getString(emR + "sent_people"));
                data.email.setTheme(rs.getString(emR + "theme"));
                data.email.setContent(rs.getString(emR + "content"));
                data.email.setIntervalTime(rs.getInt(emR + "interval_time"));
                data.email.setLastTime(rs.getInt(emR + "last_time"));
                data.email.setExtend(rs.getInt(emR + "extends"));
                String sHostList = rs.getString(smsR + "receive_list");
                data.sms.setReceiveList(sHostList);
                data.sms.setEnable(rs.getBoolean(smsR + "enable"));
                data.sms.setContent(rs.getString(smsR + "content"));
                data.sms.setIntervalTime(rs.getInt(smsR + "interval_time"));
                data.sms.setLastTime(rs.getInt(smsR + "last_time"));
                data.sms.setExtend(rs.getInt(smsR + "extends"));
                map.put(id, data);
            }

            /*
            ResultSet rs1 = stat.executeQuery(loadOffline);
            while (rs1.next()) {
                String id = rs1.getString("register_name");
                MonitorData data = new MonitorData();

                String eHostList = rs1.getString(emR + "receive_list");
                String[] eLists = eHostList.split(",");
                ArrayList<String> eArray = new ArrayList<String>();
                if (eLists != null) {
                    for (String list : eLists)
                        eArray.add(list);
                }
                data.email.setReceiveList(eArray);
                data.email.setEnable(rs1.getBoolean(emR + "enable"));
                data.email.setSentPeople(rs1.getString(emR + "sent_people"));
                data.email.setTheme(rs1.getString(emR + "theme"));
                data.email.setContent(rs1.getString(emR + "content"));
                data.email.setIntervalTime(rs1.getInt(emR + "interval_time"));
                data.email.setLastTime(rs1.getInt(emR + "last_time"));
                data.email.setExtend(rs1.getInt(emR + "extends"));
                String sHostList = rs1.getString(smsR + "receive_list");
                data.sms.setReceiveList(sHostList);
                data.sms.setEnable(rs1.getBoolean(smsR + "enable"));
                data.sms.setContent(rs1.getString(smsR + "content"));
                data.sms.setIntervalTime(rs1.getInt(smsR + "interval_time"));
                data.sms.setLastTime(rs1.getInt(smsR + "last_time"));
                data.sms.setExtend(rs1.getInt(smsR + "extends"));
                map.put(id, data);

                LOG.info("Load offline data from mysql id=" + id);
            }
            */

            LOG.info("Load data from mysql, there are " + counter + " entries");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                stat.close();
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadMonitorRegister() {
        Statement stat = null;
        try {
            if (con == null || con.isClosed()) {
                con = DBPoolManager.getConnection("monitorX");
            }
            stat = con.createStatement();
            ResultSet rs = stat.executeQuery(loadSmsMonitorRegisterSql);
            int counter = 0;
            Map<String, String> smsMapping = new HashMap<String, String>();
            while (rs.next()) {
                counter++;
                String register_name = rs.getString("register_name");
                smsMapping.put(register_name, rs.getString("receive_list"));

            }
            regNameToSmsUsers = smsMapping;

            LOG.info("Load monitor register from mysql, there are " + counter + " entries");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                stat.close();
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadUserMapping() {
        Statement stat = null;
        try {
            if (con == null || con.isClosed()) {
                con = DBPoolManager.getConnection("monitorX");
            }
            stat = con.createStatement();
            ResultSet rs = stat.executeQuery(loadUserMappingSql);
            int counter = 0;
            Map<String, String> smsMapping = new HashMap<String, String>();
            Map<String, String> emailMapping = new HashMap<String, String>();
            while (rs.next()) {
                counter++;
                String monitor_user = rs.getString("monitor_user");
                emailMapping.put(monitor_user, rs.getString("email_users"));
                smsMapping.put(monitor_user, rs.getString("sms_users"));

            }
            smsUserMapping = smsMapping;
            emailUserMapping = emailMapping;

            LOG.info("Load user mapping from mysql, there are " + counter + " entries");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                stat.close();
                con.close();
                LOG.info("connect close");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public ConcurrentHashMap<String, MonitorData> getData() {
        return map;
    }

    public static MonitorCache getInstance() {
        return instance;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(refreshDaley);
                loadData();
                loadUserMapping();
                loadMonitorRegister();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        MonitorCache.getInstance();
    }
}
