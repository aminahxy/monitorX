package com.sina.data.server.impl;

import com.sina.data.server.MonitorServer;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * The Http server implementation of monitor.
 */
public class MonitorHttpServersImpl implements MonitorServer {

  protected final InetSocketAddress addr;
  protected final ServerBootstrap bootstrap;
  protected final NioServerSocketChannelFactory factory;
  protected Channel ch;

  public MonitorHttpServersImpl(String host, int port, Object userHandler) {
    this(new InetSocketAddress(host, port), userHandler);
  }

  public MonitorHttpServersImpl(InetSocketAddress addr, Object userHandler) {
    this.addr = addr;
    this.factory = new NioServerSocketChannelFactory(
        Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
    this.bootstrap = new ServerBootstrap(this.factory);
    this.bootstrap.setOption("reuseAddress", Boolean.valueOf(true));

    this.bootstrap.setOption("child.tcpNoDelay", Boolean.valueOf(true));
    this.bootstrap.setOption("child.keepAlive", Boolean.valueOf(true));
    this.bootstrap.setPipelineFactory(new HttpServerPipelineFactory(userHandler));
  }

  @Override
  public synchronized void stop() {
    if (this.ch != null)
      this.ch.close().awaitUninterruptibly();
    this.factory.releaseExternalResources();
  }

  @Override
  public void start() throws IOException {
    // TODO Auto-generated method stub
    this.ch = this.bootstrap.bind(this.addr);
  }

  @Override
  public void init() {

  }

  public class HttpServerPipelineFactory implements ChannelPipelineFactory {
    private final HttpServerHandler handler;

    public HttpServerPipelineFactory(Object userHandler) {
      this.handler = new HttpServerHandler(userHandler);
    }

    public ChannelPipeline getPipeline() throws Exception {
      ChannelPipeline pipeline = Channels.pipeline();

      pipeline.addLast("decoder", new HttpRequestDecoder());

      pipeline.addLast("aggregator", new HttpChunkAggregator(1048576));
      pipeline.addLast("encoder", new HttpResponseEncoder());

      pipeline.addLast("handler", this.handler);
      return pipeline;
    }
    
  }

}
