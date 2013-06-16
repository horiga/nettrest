package com.blogspot.horiga3.example.nettyrest.common.controller;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.util.ExternalResourceReleasable;

public interface Controller 
	extends ExternalResourceReleasable {
	abstract void handleMessage( ChannelHandlerContext ctx, HttpMessage msg) throws Exception;
}
