package com.sina.data.rest;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.data.Protocol;

public class RestServer {
  private Component component;
  
  public RestServer(Protocol protocal, int port, String context,
                    Class<? extends Application> appClazz)
          throws InstantiationException, IllegalAccessException {
    component = new Component();
    component.getServers().add(protocal, port);
    component.getDefaultHost().attach(context, appClazz.newInstance());
  }

  public RestServer(Protocol protocal, int port, String context,
                    Application app)
          throws InstantiationException, IllegalAccessException {
    component = new Component();
    component.getServers().add(protocal, port);
    component.getDefaultHost().attach(context, app);
  }
  
  public void start() throws Exception {
    if (component != null && !component.isStarted()) {
      component.start();
    }
  }

  public void stop() throws Exception {
    if (component != null && !component.isStopped()) {
      component.stop();
    }
  }
}
