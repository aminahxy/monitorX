package com.sina.data.server;

import java.io.IOException;

public interface MonitorServer {
  
  public void start() throws IOException;
  
  public void stop();
  
  public void init();
}
