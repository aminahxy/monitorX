package com.sina.data.service;

import com.sina.data.entry.AlarmEntry;
import com.sina.data.util.ConfUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class is used for cache alarm entry,and decorated with decoration class
 */
public class AlertService implements Runnable {

  private static final Logger LOG = Logger.getLogger(AlertService.class);
  
  private BlockingQueue<AlarmEntry> queue ;
  
  private Map<String,AlarmInterface> handlers = new ConcurrentHashMap<String, AlarmInterface>(10);
  
  private ComputeContext context;
  
  public AlertService( ComputeContext context){
    this();
    this.context = context;
  }
  
  @SuppressWarnings("rawtypes")
  public AlertService() {
    this.queue = new LinkedBlockingQueue<AlarmEntry>();
    this.handlers = new ConcurrentHashMap<String, AlarmInterface>(20);
    String str = ConfUtils.getString("compute.decorate.class.definition", "default:com.sina.data.service.impl.GenericAlarmImpl");
    String[] kv = StringUtils.split(str, ",");
    for (String s : kv) {
      String tmp[] = s.split(":");
      try {
        Class clazz = Class.forName(tmp[1]);
        AlarmInterface ins = (AlarmInterface) clazz.newInstance();
        handlers.put(tmp[0], ins);
      } catch (Exception e) {
        e.printStackTrace();
        System.exit(101);
      }
    }
    LOG.info("Alert Service is ready with decorate ["+str+"]");
  }
  
  public void run(){
    while(!Thread.currentThread().isInterrupted()){
      try{
        AlarmEntry alarm = queue.take();
        AlarmInterface monitor = handlers.get(alarm.getId())==null?handlers.get("default"):handlers.get(alarm.getId());
        if(monitor!=null)
          monitor.doAlarm(alarm);
      }catch (InterruptedException e){
        LOG.error(e);
      }
    }
  }
  
  public void enQueue(AlarmEntry alarm) throws InterruptedException {
    this.queue.put(alarm);
  }

}
