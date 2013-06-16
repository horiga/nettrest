/*
 * - EstablishedChannelGroup.java
 * - @since 2013/04/02
 * - hiroyuki.horigami
 */
package com.blogspot.horiga3.example.nettyrest.common.channel;

import org.jboss.netty.channel.group.DefaultChannelGroup;

import com.google.inject.Singleton;

@Singleton
public class EstablishedChannelGroup extends DefaultChannelGroup {

    public EstablishedChannelGroup() {
	super();
    }

    public EstablishedChannelGroup(String name) {
	super(name);
    }
}
