package com.blogspot.horiga3.example.nettyrest;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.util.CharsetUtil;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blogspot.horiga3.example.nettyrest.common.controller.ApplicationController;
import com.blogspot.horiga3.example.nettyrest.common.jaxrs.Body;
import com.blogspot.horiga3.example.nettyrest.common.jaxrs.PathParam;
import com.blogspot.horiga3.example.nettyrest.common.jaxrs.Procedure;
import com.blogspot.horiga3.example.nettyrest.common.jaxrs.QueryString;
import com.blogspot.horiga3.example.nettyrest.common.jaxrs.Resource;

@Resource("echo")
public class EchoController extends ApplicationController {

	private static Logger logger = LoggerFactory.getLogger(EchoController.class);

	@Procedure(httpMethod="GET", path="")
	public void echo(ChannelHandlerContext ctx, 
			@QueryString("message") String message) {
		logger.info("GET /v1/echo");
		writeText(ctx, message);
	}

	@SuppressWarnings("unchecked")
	@Procedure(httpMethod="GET", path="/{fmt}/{message}")
	public void echo(ChannelHandlerContext ctx, 
			@PathParam("fmt") String fmt, @PathParam("message") String message, 
			@QueryString(value="q", defaultValue="null") String q) {
		logger.debug("GET /v1/echo/fmt/message: {}, {}, {}", fmt, message, q);
		if ("text".equals(fmt)) {
			writeText(ctx, message);
		} else {
			JSONObject json = new JSONObject();
			json.put("message", message);
			json.put("q", q);
			writeJson(ctx, json);
		}
	}

	@Procedure(httpMethod="POST", path="")
	public void post(ChannelHandlerContext ctx, @Body ChannelBuffer content) {
		logger.debug("POST /v1/echo");
		writeText(ctx, content.toString(CharsetUtil.UTF_8));
	}
	
	@SuppressWarnings("unchecked")
	@Procedure(httpMethod="POST", path="/message")
	public void post2(ChannelHandlerContext ctx, @Body ChannelBuffer content) {
		logger.debug("POST /v1/echo/message");
		JSONObject json = new JSONObject();
		json.put("context", content.toString(CharsetUtil.UTF_8));
		writeJson(ctx, json);
	}
	
	@Procedure(httpMethod="DELETE", path="/{bbsid}")
	public void delete(ChannelHandlerContext ctx, @PathParam("bbsid") String bbsid) {
		logger.debug("DELETE /v1/echo/{bbsid}");
		writeJson(ctx, new JSONObject());
	}
}
