package com.blogspot.horiga3.example.nettyrest.common.module;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.ExternalResourceReleasable;
import org.jboss.netty.util.HashedWheelTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blogspot.horiga3.example.nettyrest.EchoController;
import com.blogspot.horiga3.example.nettyrest.MockController;
import com.blogspot.horiga3.example.nettyrest.common.DefaultNettyServer;
import com.blogspot.horiga3.example.nettyrest.common.NettyServer;
import com.blogspot.horiga3.example.nettyrest.common.RestfulMessageHandler;
import com.blogspot.horiga3.example.nettyrest.common.channel.EstablishedChannelGroup;
import com.blogspot.horiga3.example.nettyrest.common.controller.ControllerRegistry;
import com.blogspot.horiga3.example.nettyrest.common.controller.DefaultControllerRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

public class ServerModule2 extends AbstractModule {

	private static Logger logger = LoggerFactory.getLogger(ServerModule2.class);

	@Override
	protected void configure() {
		bind(ServerBootstrap.class).to(SampleServerBootstrap.class);
		bind(NettyServer.class).to(DefaultNettyServer.class);
		bind(DefaultNettyServer.class).in(Singleton.class);
		bind(EstablishedChannelGroup.class).in(Singleton.class);
		bind(ControllerRegistry.class).to(MyControllerRegistry.class);
		bind(MyControllerRegistry.class).in(Singleton.class);
		bind(RestfulMessageHandler.class).in(Singleton.class);
	}

	static class Controllers {
		@Inject EchoController echoController;
		@Inject MockController mockController;
	}

	static class MyControllerRegistry
			extends DefaultControllerRegistry {
		@Inject
		public MyControllerRegistry(Controllers controllers) {
			super();
			this.register(controllers.echoController);
			this.register(controllers.mockController);
		}
	}

	static class SampleChannelPipelineFactory
			implements ChannelPipelineFactory, ExternalResourceReleasable {

		protected static int readerIdleTimeSeconds = 10;

		protected final RestfulMessageHandler handler;

		@Inject(optional = true)
		public void setReaderIdleTimeSeconds(@Named("idlestate.timerseconds") int timerSeconds) {
			readerIdleTimeSeconds = timerSeconds;
		}

		@Inject
		public SampleChannelPipelineFactory(RestfulMessageHandler handler) {
			logger.info("constructor: args={}", handler);
			this.handler = handler;
		}

		@Override
		public ChannelPipeline getPipeline() throws Exception {
			ChannelPipeline pipeline = Channels.pipeline();
			if (readerIdleTimeSeconds > 0) pipeline.addLast("idlestate", new IdleStateHandler(new HashedWheelTimer(),
					readerIdleTimeSeconds, 0, 0));
			pipeline.addLast("decoder", new HttpRequestDecoder());
			pipeline.addLast("aggregator", new HttpChunkAggregator(65535));
			pipeline.addLast("encoder", new HttpResponseEncoder());
			pipeline.addLast("handler", handler);
			return pipeline;
		}

		@Override
		public void releaseExternalResources() {
			logger.info("release resources.[pipeline]");
		}
	}

	static class SampleServerBootstrap extends ServerBootstrap {
		@Inject
		public SampleServerBootstrap(final SampleChannelPipelineFactory pipeline) {
			super(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(new ThreadFactory() {
				@Override
				public Thread newThread(Runnable task) {
					return new Thread(task, "boss");
				}
			}), Executors.newCachedThreadPool(new ThreadFactory() {
				@Override
				public Thread newThread(Runnable task) {
					return new Thread(task, "worker");
				}
			})) {
				@Override
				public void releaseExternalResources() {

				}
			});
			logger.info("constructor: args={}", pipeline);
			this.setPipelineFactory(pipeline);
			this.setOption("reuseAddress", true);
		}
	}

}
