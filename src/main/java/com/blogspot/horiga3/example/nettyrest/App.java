package com.blogspot.horiga3.example.nettyrest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blogspot.horiga3.example.nettyrest.common.NettyServer;
import com.blogspot.horiga3.example.nettyrest.common.module.ServerModule2;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;

/**
 * Hello world!
 * 
 */
public class App {

	static Logger logger = LoggerFactory.getLogger(App.class);

	static Injector injector;

	public static void main(String[] args) {
		try {
			injector = Guice.createInjector(Stage.DEVELOPMENT, new ServerModule2());

			final NettyServer server = injector.getInstance(NettyServer.class);

			Runtime.getRuntime().addShutdownHook(new Thread() {
				{
					setName("shutdown");
				}

				@Override
				public void run() {
					logger.info("shutdown server");
					server.unbind();
				}
			});
			server.bind();
		} catch (Exception e) {
			logger.error("server booting failed.", e);
			System.exit(1);
		}
	}
}
