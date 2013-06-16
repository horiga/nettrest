package com.blogspot.horiga3.example.nettyrest.common;

import org.jboss.netty.handler.codec.http.HttpMessage;

public class AuthnUserContext implements UserContext {
	
	final String channelId;
	final String memberId;
	final String accessToken;

	public AuthnUserContext(HttpMessage message) {
		this.channelId = "";
		this.memberId = "";
		this.accessToken = "";
	}

	@Override
	public String getChannelId() {
		return channelId;
	}

	@Override
	public String getMemberId() {
		return memberId;
	}

	@Override
	public String getToken() {
		return accessToken;
	}
}
