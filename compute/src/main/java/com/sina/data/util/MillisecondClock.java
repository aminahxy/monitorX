package com.sina.data.util;

/**
 * Because of the overhead of System.currentTimeMillis function is too waste.
 * So the class is to instead of it
 */
public class MillisecondClock {
  
  private final long rate;
  private volatile long now = 0;

  private MillisecondClock(long rate) {
    this.rate = rate;
    this.now = System.currentTimeMillis();
    start();
  }

  private void start() {
    Thread s = new Thread(new Runnable() {
      @Override
      public void run() {
        while (true) {
          try {
            Thread.sleep(rate);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          now = System.currentTimeMillis();
        }
      }
    });
    s.setDaemon(true);
    s.setName("Clock_Thread");
    s.start();
  }

  public long now() {
    return now;
  }
  
  public static final MillisecondClock CLOCK = new MillisecondClock(50);

}