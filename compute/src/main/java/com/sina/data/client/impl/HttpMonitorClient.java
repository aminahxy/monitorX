package com.sina.data.client.impl;


import com.sina.data.client.MonitorClient;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.Map.Entry;

public class HttpMonitorClient implements MonitorClient {
  private static final Logger log = Logger.getLogger(HttpMonitorClient.class);

  private static MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
  private static int TIMEOUT_DEFAULT = 3000;
  private static int MAX_HTTP_CONNECTION_DEFAULT = 100;
  
  private HttpClient httpClient;
  
  private final String url;
  
  static {
    HttpConnectionManagerParams httpConnectionManagerParams = new HttpConnectionManagerParams();
    httpConnectionManagerParams.setMaxTotalConnections(MAX_HTTP_CONNECTION_DEFAULT);
    httpConnectionManagerParams.setSoTimeout(TIMEOUT_DEFAULT);
    httpConnectionManagerParams.setConnectionTimeout(TIMEOUT_DEFAULT);
//    httpConnectionManagerParams.setTcpNoDelay(false);
    connectionManager.setParams(httpConnectionManagerParams);
  }
  
  public HttpMonitorClient(String url) {
    this.url = url;
    httpClient = new HttpClient(connectionManager);
  }
  
  @Override
  public String post(Map<String,Object> data) throws Exception {

    String ret = "";
//    log.info("post data to " + url);
    PostMethod postMethod = new PostMethod(url);

    postMethod.setRequestHeader("Content-Encoding", "text/html");
    postMethod.setRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
    postMethod.setRequestHeader("Connection", "close");
    try
    {
      for(Entry<String, Object> kv : data.entrySet()){
        postMethod.addParameter(new NameValuePair(kv.getKey(), String.valueOf(kv.getValue())));
      }
      httpClient.executeMethod(postMethod);

      ret = postMethod.getResponseBodyAsString();
    } catch (Exception e) {
      throw e;
    }
    finally {
      postMethod.releaseConnection();
    }

    return ret;
  
  }
  
  public static void shutdown(){
    if(connectionManager!=null)
      connectionManager.shutdown();
  }
}
