package com.sina.data.alarm;



import com.sina.data.alarm.util.ReceiveBean;
import com.sina.data.common.AppType;
import com.sina.data.common.JudgeType;
import com.sina.data.util.ConfUtils;
import com.sina.rpchttp.server.HttpServer;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlarmMainServer implements ServiceInterface {
    public static final Logger LOG = LoggerFactory.getLogger(AlarmMainServer.class);
    private WarnningServer server;

    private int port = ConfUtils.getInt("alarm.server.port", 28188);

    public AlarmMainServer() {
        server = new WarnningServer();
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String process(JSONObject obj) throws JSONException {
        LOG.info("receive--"+obj.toString());
        if (obj == null)
            return null;
        try {
            LOG.info("boyan add process");
            ReceiveBean bean = new ReceiveBean();
            long time = obj.getLong("timestamp");
            bean.setTimestamp(time);
            String id = obj.getString("id");
            bean.setId(id);
            String registerName = obj.getString("register_name");
            bean.setRegister_name(registerName);
            String appId = obj.getString("appId");
            bean.setAppId(appId);
            String judgeType = obj.getString("judgeType");
            bean.setJudgeType(JudgeType.valueOf(judgeType));
            String appType = obj.getString("appType");
            bean.setAppType(AppType.valueOf(appType));
            String extended = obj.getString("extended");
            bean.setExtended(extended);
            String metricName = obj.getString("metric");
            bean.setMetric(metricName);
            String monitorId = obj.getString("monitorId");
            bean.setMonitorId(monitorId);
            String node = obj.getString("node");
            bean.setNode(node);

            if (JudgeType.offline != bean.getJudgeType() && JudgeType.no_data != bean.getJudgeType()) {
                double currentValue = obj.getDouble("current");
                bean.setCurrent_value(currentValue);
                double threshold = obj.getDouble("threshold");
                bean.setThreshold(threshold);

                if (AppType.ols == bean.getAppType()) {
                    String component = obj.getString("component");
                    bean.setComponent(component);
                }
            }
//            bean.toSql();
            LOG.info("boyan add end process");
            server.putReceive(bean);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            System.out.println("process error .");
            e.printStackTrace();
            return "failed:" + e.getMessage();
        }

        return "ok";
    }

    public static void main(String[] args) {
        try {
            start(args);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void start(String[] args) throws JSONException {

        AlarmMainServer m = new AlarmMainServer();
        HttpServer s = new HttpServer("0.0.0.0", m.getPort(), m);
        try {
            s.serv();
            LOG.info("Server startup at " + m.getPort());
        } catch (Exception e) {
            e.printStackTrace();
            LOG.info("Server fail");
        }
    }

}
