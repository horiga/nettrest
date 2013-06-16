package com.blogspot.horiga3.example.nettyrest.common;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.util.ExternalResourceReleasable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractChannelUpstreamHandler extends SimpleChannelUpstreamHandler
	implements ExternalResourceReleasable {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractChannelUpstreamHandler.class);
	
	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
		logger.debug("[n/a] open({})", ctx.getChannel().getRemoteAddress());
		ctx.sendUpstream(e);
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) {
		logger.debug("[n/a] closed({})", ctx.getChannel().getRemoteAddress());
		ctx.sendUpstream(e);
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
		logger.debug("[n/a] connected({})", ctx.getChannel().getRemoteAddress());
		ctx.sendUpstream(e);
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
		logger.debug("[n/a] disconnected({})", ctx.getChannel().getRemoteAddress());
		ctx.sendUpstream(e);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		logger.warn(String.format("[n/a] exceptionCaught(%s)", ctx.getChannel().getRemoteAddress().toString()), e.getCause());
		if (ctx.getChannel().isOpen()) {
			ctx.getChannel().close();
		}
	}
	
	@Override
	public void releaseExternalResources() {
		// do anything.
	}
}
