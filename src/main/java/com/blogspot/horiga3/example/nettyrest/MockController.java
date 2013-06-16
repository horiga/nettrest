package com.blogspot.horiga3.example.nettyrest;

import java.util.List;
import java.util.Map;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blogspot.horiga3.example.nettyrest.common.controller.ApplicationController;
import com.blogspot.horiga3.example.nettyrest.common.jaxrs.Procedure;
import com.blogspot.horiga3.example.nettyrest.common.jaxrs.QueryStrings;
import com.blogspot.horiga3.example.nettyrest.common.jaxrs.Resource;

@Resource(value="mock", version="v2")
public class MockController extends ApplicationController {

	static Logger logger = LoggerFactory.getLogger(MockController.class);

	@Procedure(httpMethod = "GET", path = "/hogehoge")
	public void hogehoge(ChannelHandlerContext ctx, @QueryStrings Map<String, List<String>> params) {
		logger.debug("GET /v1.1/mock/hogehoge");
		writeJson( ctx, new JSONObject());
	}
}
