package com.blogspot.horiga3.example.nettyrest.common.jaxrs;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@HttpMethod(HttpMethod.DELETE)
@Documented
public @interface DELETE {
	String value();
}
