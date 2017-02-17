package com.sina.data;

import com.sina.data.constant.Constants;
import com.sina.data.hbase.HBaseEnv;
import com.sina.data.rest.RestApplication;
import com.sina.data.rest.RestServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.restlet.data.Protocol;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by lile1 on 2015/12/1.
 */
public class Server {
    private static Log LOG = LogFactory.getLog(Server.class);


    private RestServer restServer;

    Server(Properties prop,Configuration conf) throws Exception{
        RestApplication restApplication = new RestApplication();
        String restPortStr = prop.getProperty(Constants.REST_SERVER_PORT);
        Integer restPort = Integer.parseInt(restPortStr == null ? "8111" : restPortStr);
        String restContext = prop.getProperty(Constants.REST_SERVER_CONTEXT);
        if(restContext == null){
            restContext = "/interface";
        }
        restServer = new RestServer(Protocol.HTTP, restPort, restContext,
                restApplication);
    }

    public void start() throws Exception{
        restServer.start();
    }

    public void stop() throws Exception{
        if(restServer != null ) {
            restServer.stop();
        }
    }

    public static void main(String[] args) throws Exception{
        Server ser = null;
        try {
            Properties prop = new Properties();
            InputStream propInputStream = new FileInputStream("conf/monitorX.properties");
            prop.load(propInputStream);

            Configuration conf = new Configuration();
            conf.addResource(new FileInputStream("conf/HBaseClientSetting.xml"));
            LOG.info(conf.get("hbase.zookeeper.quorum"));
            HBaseEnv.initConn(conf);

            ser = new Server(prop, conf);
            ser.start();
            LOG.info("--------server has started.--------");
        }catch (Exception e){
            LOG.error("server don't start because of : ", e);
            if(ser != null) {
                ser.stop();
            }
            HBaseEnv.closeConn();
            System.exit(-1);
        }
    }
}
