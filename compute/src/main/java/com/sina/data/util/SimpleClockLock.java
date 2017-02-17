package com.sina.data.util;



public class SimpleClockLock extends Thread implements ClockLock {
  
  private static final long rate_default = ConfUtils.getLong(
          "monitor.compute.clock.split", 15)*1000;
  
  private volatile long time ;
  
  private final long rate;
  
  private volatile boolean stop = false;
  
  private SimpleClockLock(long rate){
    this.rate = rate;
    this.time = (MillisecondClock.CLOCK.now() / this.rate * this.rate) / 1000;
    this.setName("Clock4Fetcher");
    this.setDaemon(true);
    this.start();
  }
  
  @Override
  public long getTime() {
    return time;
  }

  @Override
  public long getRate() {
    return rate;
  }

  @Override
  public void stopClock() {
    this.stop = true;
    this.interrupt();
  }

  @Override
  public void run() {
    while (!stop && !Thread.currentThread().isInterrupted()) {
      try {
        Thread.sleep(500);
        long now = (MillisecondClock.CLOCK.now() / this.rate * this.rate) / 1000;
        if(now != time)
          time = now;
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
  
  public static SimpleClockLock MonitorTimer = new SimpleClockLock(rate_default);

}
