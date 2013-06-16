package com.blogspot.horiga3.example.nettyrest.common;

import org.jboss.netty.util.ExternalResourceReleasable;

public interface NettyServer extends ExternalResourceReleasable {
	int DEFAULT_HTTP_BIND_PORT = 19080;
	int DEFAULT_WS_BIND_PORT = 29080;
	
	abstract void bind() throws Exception;
	abstract void unbind();
}
