package com.blogspot.horiga3.example.nettyrest.common.controller;

import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.handler.codec.http.HttpMessage;

import com.blogspot.horiga3.example.nettyrest.common.AuthnUserContext;
import com.blogspot.horiga3.example.nettyrest.common.UserContext;

public class MessageAttachment {

		final HttpMessage origin;
		final UserContext userContext;
		final Map<String, Object> context;

		public MessageAttachment(HttpMessage message) {
			this.origin = message;
			this.userContext = new AuthnUserContext(message);
			this.context = new HashMap<String, Object>();
		}

		public HttpMessage getMessage() {
			return origin;
		}

		public UserContext getUserContext() {
			return userContext;
		}

		public void addContext(String key, Object value) {
			this.context.put(key, value);
		}

		public Object getContext(String key) {
			return getContext(key, null);
		}

		public Object getContext(String key, Object defaultValue) {
			return this.context.containsKey(key) ? this.context.get(key) : defaultValue;
		}
	}