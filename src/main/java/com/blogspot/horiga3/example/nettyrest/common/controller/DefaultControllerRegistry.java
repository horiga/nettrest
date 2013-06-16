package com.blogspot.horiga3.example.nettyrest.common.controller;

import java.util.Collection;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blogspot.horiga3.example.nettyrest.common.jaxrs.Resource;

public class DefaultControllerRegistry
	implements ControllerRegistry {
	
	static Logger logger = LoggerFactory.getLogger(DefaultControllerRegistry.class); 
	
	protected final HashMap<String, Controller> controllers;
	
	public DefaultControllerRegistry() {
		this.controllers = new HashMap<>();
	}
	
	@Override
	public void register(Controller controller) {
		if (controller.getClass().isAnnotationPresent(Resource.class)) {
			Resource res = controller.getClass().getAnnotation(Resource.class);
			final String key = "/" + res.version() + "/" + res.value();
			logger.info("add controller {}={}", key, controller.getClass().getName());
			controllers.put(key, controller);
			
			return;
		}
		throw new RuntimeException("This controller is not controller format. [" 
		+ controller.getClass().getName() + "]");
	}

	@Override
	public Controller get(String uri) {
		// uri : /{Resource.version}/{Resource.value}
		String[] path = uri.split("/", 3);
		if ( path.length < 3) return null;
		return get(path[1], path[2]);
	}

	@Override
	public Collection<Controller> getControllers() {
		return controllers.values();
	}

	@Override
	public Controller get(String version, String resource) {
		return controllers.get("/" + version + "/" + resource);
	}

}
