package com.blogspot.horiga3.example.nettyrest.common.controller;

import java.util.Collection;

public interface ControllerRegistry {
	void register(Controller controller);
	Controller get(String uri);
	Collection<Controller> getControllers();
	Controller get(String version, String resource);
}
