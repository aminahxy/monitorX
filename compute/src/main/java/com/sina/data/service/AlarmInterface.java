package com.sina.data.service;

import com.sina.data.entry.AlarmEntry;
import com.sina.data.util.ConfUtils;
import org.apache.log4j.Logger;

import java.util.Map;

public abstract class AlarmInterface {
  private static final Logger LOG = Logger.getLogger(AlarmInterface.class);
  private ComputeContext context; 
  
  public AlarmInterface() {
    context = ComputeContext.getInstance();
  }
  
  public abstract AlarmEntry alarmInfo(AlarmEntry entry);
  
  public void doAlarm(AlarmEntry alert){
    AlarmEntry ae = alarmInfo(alert);
    try{
      if(ae!=null){
        if (!"test".equalsIgnoreCase(ConfUtils.getString(
                "monitor.http.send.client.mode", "test"))) {
          Map<String,Object> da = ae.toPostData();
          String r = context.getHttpClient().post(da);
          LOG.info("Send alarm information to alarm system  "+da);
          LOG.info("With result "+r);
        }else
          LOG.info("Test send alarm information to alarm system  "+ae.toPostData());
        
      }
    }catch(Exception e){
      LOG.error("alarm post failed to url "+ ae.toString());
      System.out.println("alarm post failed");
      e.printStackTrace();
    }
  }
  
}
