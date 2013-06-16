package com.blogspot.horiga3.example.nettyrest.common;

import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blogspot.horiga3.example.nettyrest.common.controller.Controller;
import com.blogspot.horiga3.example.nettyrest.common.controller.ControllerRegistry;
import com.blogspot.horiga3.example.nettyrest.common.controller.MessageAttachment;
import com.google.inject.Inject;


public class RestfulMessageHandler 
	extends AbstractChannelUpstreamHandler {
	
	static Logger logger = LoggerFactory.getLogger(RestfulMessageHandler.class);
	
	protected final ControllerRegistry controllers;
	
	@Inject
	public RestfulMessageHandler( ControllerRegistry controllers) {
		this.controllers = controllers;
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		logger.debug("[n/a] - messageReceived({})", ctx.getChannel().getRemoteAddress());
		
		// 100 continue
		
		if (!(e.getMessage() instanceof HttpRequest)) {
			ctx.getChannel().write(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST))
					.addListener(ChannelFutureListener.CLOSE);
			return;
		}
		
		final String[] path = ((HttpRequest) e.getMessage()).getUri().split("\\?")[0].split("/");
		if ( path.length < 3) {
			ctx.getChannel().write(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_IMPLEMENTED))
					.addListener(ChannelFutureListener.CLOSE);
			return;
		}
		
		final Controller controller = controllers.get("/" + path[1] + "/" + path[2]);
		if (controller != null) {
			ctx.setAttachment(new MessageAttachment((HttpMessage)e.getMessage()));
			controller.handleMessage(ctx, (HttpMessage) e.getMessage());
		} else {
			ctx.getChannel().write(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND)).addListener(ChannelFutureListener.CLOSE);
		}
	}
}
