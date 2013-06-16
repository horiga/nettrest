package com.blogspot.horiga3.example.nettyrest.common.controller;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.util.CharsetUtil;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blogspot.horiga3.example.nettyrest.common.jaxrs.Body;
import com.blogspot.horiga3.example.nettyrest.common.jaxrs.HeaderParam;
import com.blogspot.horiga3.example.nettyrest.common.jaxrs.PathParam;
import com.blogspot.horiga3.example.nettyrest.common.jaxrs.Procedure;
import com.blogspot.horiga3.example.nettyrest.common.jaxrs.QueryString;
import com.blogspot.horiga3.example.nettyrest.common.jaxrs.QueryStrings;
import com.blogspot.horiga3.example.nettyrest.common.jaxrs.Resource;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public abstract class ApplicationController
		implements Controller {

	protected static boolean keepalive = true;

	protected static Logger logger = LoggerFactory.getLogger(ApplicationController.class);

	protected final Map<String, List<Method>> targetMethods;
	protected final String pathPrefix;

	@Inject(optional = true)
	public void setKeepalive(@Named("use.keepalive") boolean _keepalive) {
		keepalive = _keepalive;
	}

	public ApplicationController() {
		super();
		this.targetMethods = new HashMap<String, List<Method>>();
		// TODO http-method enum.
		targetMethods.put("GET", new ArrayList<Method>());
		targetMethods.put("POST", new ArrayList<Method>());
		targetMethods.put("PUT", new ArrayList<Method>());
		targetMethods.put("DELETE", new ArrayList<Method>());
		for (Method m : this.getClass().getMethods()) {
			if (m.isAnnotationPresent(Procedure.class)) {
				Procedure procedure = m.getAnnotation(Procedure.class);
				targetMethods.get(procedure.httpMethod()).add(m);
			}
		}
		final Resource res = this.getClass().getAnnotation(Resource.class);
		this.pathPrefix = "/" + res.version() + "/" + res.value();
	}

	@Override
	public void releaseExternalResources() {
		// TODO: do anything
	}

	private Method getProcedureMethod(HttpMessage msg) {
		for (Method m : targetMethods.get(((HttpRequest) msg).getMethod().getName())) {
			final String path = pathPrefix + StringUtils.defaultString(m.getAnnotation(Procedure.class).path(), "");
			if (((HttpRequest) msg).getUri().split("\\?")[0].split("/").length != path.split("/").length) continue;
			return m;
		}
		return null;
	}

	@Override
	public void handleMessage(ChannelHandlerContext ctx, HttpMessage msg) throws Exception {
		try {
			if (!(msg instanceof HttpRequest)) {
				writeErrorMessage(HttpResponseStatus.INTERNAL_SERVER_ERROR, ctx, "protocol unsupported");
				return;
			}

			Method invoker = getProcedureMethod(msg);
			if (invoker == null) {
				writeErrorMessage(HttpResponseStatus.NOT_IMPLEMENTED, ctx, "request unsupported");
				return;
			}

			// procedure method arguments.
			List<Object> args = new ArrayList<>();
			args.add(ctx);

			final Annotation[][] argumentsAnnotaions = invoker.getParameterAnnotations();
			if (argumentsAnnotaions != null) {
				final Map<String, List<String>> queries = new QueryStringDecoder(((HttpRequest) msg).getUri()).getParameters();
				for (Annotation[] a : argumentsAnnotaions) {
					for (Annotation aa : a) {
						final Class<? extends Annotation> argumentType = aa.annotationType();
						if (QueryString.class.equals(argumentType)) {
							final String qs = ((QueryString) aa).value();
							args.add(queries.containsKey(qs) ? StringUtils.join(queries.get(qs), ",")
									: ((QueryString) aa).defaultValue());
						} else if (PathParam.class.equals(argumentType)) {
							final String keyName = "{" + ((PathParam) aa).value() + "}";
							boolean finded = false;
							int findIndex = 0;
							for (String path : (pathPrefix + invoker.getAnnotation(Procedure.class).path()).split("/")) {
								if (path.equals(keyName)) {
									finded = true;
									break;
								}
								findIndex++;
							}
							args.add(finded ? URLDecoder.decode(
									((HttpRequest) msg).getUri().split("\\?")[0].split("/")[findIndex], "UTF-8") : "");
						} else if (Body.class.equals(argumentType)) {
							args.add(((HttpRequest) msg).getContent());
						} else if (HeaderParam.class.equals(argumentType)) {
							final List<String> headerValues = ((HttpRequest) msg)
									.getHeaders(((HeaderParam) aa).value());
							if (headerValues == null || headerValues.size() == 0) args.add(null);
							else if (headerValues.size() == 1) args.add(headerValues.get(0));
							else if (headerValues.size() > 1) args.add(StringUtils.join(headerValues, ","));
						} else if (QueryStrings.class.equals(argumentType)) {
							args.add(queries);
						} else {
							logger.warn("invoke argument type is unknown. {}", argumentType.getName());
						}
					}
				}
			}
			
			invoker.invoke(this, args.toArray());

		} catch (Exception e) {
			throw e;
		}
	}

	protected void writeText(ChannelHandlerContext ctx, String text) {
		writeMessage(HttpResponseStatus.OK, ctx, text, "text/plain");
	}

	protected void writeJson(ChannelHandlerContext ctx, JSONObject json) {
		writeMessage(HttpResponseStatus.OK, ctx, json.toJSONString(), "application/json");
	}

	@SuppressWarnings("unchecked")
	protected void writeErrorMessage(HttpResponseStatus status, ChannelHandlerContext ctx, String errorMessage) {
		JSONObject json = new JSONObject();
		json.put("message", errorMessage);
		writeMessage(status, ctx, json.toJSONString(), "application/json");
	}

	private void writeMessage(HttpResponseStatus status, ChannelHandlerContext ctx, String message, String contentType) {
		if (ctx.getChannel().isWritable()) {
			HttpResponse res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);
			MessageAttachment attachment = (MessageAttachment) ctx.getAttachment();
			boolean keepalive = HttpHeaders.isKeepAlive(attachment.getMessage());
			res.setHeader(HttpHeaders.Names.CONTENT_TYPE, String.format("%s; charset=utf-8", contentType));
			res.setContent(ChannelBuffers.copiedBuffer(message, CharsetUtil.UTF_8));
			if (keepalive) {
				res.setHeader(HttpHeaders.Names.CONTENT_LENGTH, res.getContent().readableBytes());
				res.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
			}
			ChannelFuture f = ctx.getChannel().write(res);
			if (!keepalive) f.addListener(ChannelFutureListener.CLOSE);
			else f.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
		} else {
			ctx.getChannel().close();
		}
	}
}
