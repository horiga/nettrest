package com.blogspot.horiga3.example.nettyrest.common;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.slf4j.LoggerFactory;

import org.slf4j.Logger;

import com.blogspot.horiga3.example.nettyrest.common.channel.EstablishedChannelGroup;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class DefaultNettyServer
		implements NettyServer {

	private static Logger logger = LoggerFactory.getLogger(DefaultNettyServer.class);

	private int port = DEFAULT_HTTP_BIND_PORT;

	private ChannelGroup serverChannels = new DefaultChannelGroup();

	private final ServerBootstrap bootstrap;

	@Inject private EstablishedChannelGroup establishedChannels;

	@Inject
	public DefaultNettyServer(/*@Named("default.bootstrap")*/ ServerBootstrap bootstrap) {
		this.bootstrap = bootstrap;
	}

	@Inject(optional = true)
	public void setPort(@Named("default.port") int port) {
		this.port = port;
	}

	@Override
	public void bind() throws Exception {
		final Channel serverChannel = bootstrap.bind(new InetSocketAddress(port));
		serverChannels.add(serverChannel);
	}

	@Override
	public void unbind() {
		try {
			logger.debug("server channel unbind");
			serverChannels.close().awaitUninterruptibly();
			if (null != establishedChannels) establishedChannels.close().awaitUninterruptibly(10, TimeUnit.SECONDS);
			bootstrap.releaseExternalResources();
		} catch (Exception e) {}
	}

	@Override
	public void releaseExternalResources() {
		unbind();
	}
}
