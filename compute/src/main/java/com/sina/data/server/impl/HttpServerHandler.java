package com.sina.data.server.impl;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.util.CharsetUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.*;

public class HttpServerHandler extends SimpleChannelUpstreamHandler {
  private static final Logger log = Logger.getLogger(HttpServerHandler.class);
  
  private static Charset charset =  CharsetUtil.getEncoder(Charset.forName("UTF-8")).charset();
  
  protected final Object handler;
  protected final String CONTENT_TYPE_V;
  protected Map<String, Method> methodMap;

  public HttpServerHandler(Object handler) {
    this.handler = handler;
    this.CONTENT_TYPE_V = "text/plain; charset=UTF-8";

    Method[] handlerMethods = handler.getClass().getMethods();
    Map<String, Method> m = new HashMap<String, Method>();
    for (Method method : handlerMethods)
      m.put(method.getName(), method);
    this.methodMap = Collections.unmodifiableMap(m);
  }

  public HttpServerHandler(Object handler, String CONTENT_TYPE) {
    this.handler = handler;
    this.CONTENT_TYPE_V = CONTENT_TYPE;

    Method[] handlerMethods = handler.getClass().getMethods();
    Map<String, Method> m = new HashMap<String, Method>();
    for (Method method : handlerMethods)
      m.put(method.getName(), method);
    this.methodMap = Collections.unmodifiableMap(m);
  }
  
  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent ev) {
    ev.getCause().printStackTrace();
    try {
      ev.getChannel().close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
      throws Exception {

    HttpRequest request = (HttpRequest) e.getMessage();
    if (request == null)
      return;

    JSONObject json = packRequest(request);

    json.put("uri", request.getUri())
        .put("RemoteAddress", e.getRemoteAddress())
        .put("HttpMethod", request.getMethod().getName());
    processOneMessage(e, request, json);
  }

  protected void processOneMessage(MessageEvent e, HttpRequest request,
      JSONObject json) throws Exception {

    boolean keepAlive = HttpHeaders.isKeepAlive(request);
    

    HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
        HttpResponseStatus.OK);

    Object handlerResult = null;
    if (!json.has("method"))
      json.put("method", "process");
    try {
      handlerResult = callMethod(json.getString("method"), json);
    } catch (Exception rpc_e) {
      response.setStatus(HttpResponseStatus.NOT_ACCEPTABLE);
      handlerResult = "Process Exception:"+rpc_e.getMessage();
      rpc_e.printStackTrace();
    }

    response.setContent(ChannelBuffers.copiedBuffer((String) handlerResult,charset));

    response.setHeader("Content-Type", CONTENT_TYPE_V);

//    if (keepAlive) {
      response.setHeader("Content-Length",
          Integer.valueOf(response.getContent().readableBytes()));
//    }

    String cookieString = request.getHeader("Cookie");
    if (cookieString != null) {
      CookieDecoder cookieDecoder = new CookieDecoder();
      Set<Cookie> cookies = cookieDecoder.decode(cookieString);
      if (!cookies.isEmpty()) {
        CookieEncoder cookieEncoder = new CookieEncoder(true);
        for (Cookie cookie : cookies) {
          cookieEncoder.addCookie(cookie);
        }
        response.addHeader("Set-Cookie", cookieEncoder.encode());
      }

    }

    ChannelFuture future = e.getChannel().write(response);

    if (!keepAlive)
      future.addListener(ChannelFutureListener.CLOSE);
  }

  protected Object callMethod(String method, JSONObject json) throws Exception {
    Method m = (Method) this.methodMap.get(method);
    if (m == null)
      throw new IOException("No such method");
    return m.invoke(this.handler, new Object[] { json });
  }

  public JSONObject parseQueryString(HttpRequest request) throws JSONException {
    log.debug("Parse the query string: " + request.getUri());
    QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());
    return traversalDecoder(decoder, null);
  }

  @SuppressWarnings("rawtypes")
  private JSONObject traversalDecoder(QueryStringDecoder decoder,
      JSONObject parameters) throws JSONException {
    if (parameters == null) {
      parameters = new JSONObject();
    }
    Iterator iterator = decoder.getParameters().entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry entry = (Map.Entry) iterator.next();
      parameters.put((String) entry.getKey(), ((List) entry.getValue()).get(0));
    }
    return parameters;
  }

  public JSONObject packRequest(HttpRequest request) throws Exception {
    JSONObject json = parseQueryString(request);
    if (!request.getMethod().equals(HttpMethod.GET)) {
      QueryStringDecoder decoder = new QueryStringDecoder("/HTML?"
          + request.getContent().toString(Charset.forName("UTF-8")) + "&");
      log.debug(request.getMethod() + ": " + request.getUri());
      return traversalDecoder(decoder, json);
    }
    return json;
  }
}
