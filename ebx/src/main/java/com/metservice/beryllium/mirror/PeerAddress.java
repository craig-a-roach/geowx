/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium.mirror;

import org.eclipse.jetty.client.Address;

import com.metservice.beryllium.IBerylliumHttpAddressable;

/**
 * @author roach
 */
class PeerAddress implements IBerylliumHttpAddressable {

	@Override
	public Address httpAddress() {
		return address;
	}

	@Override
	public String toString() {
		return qnctwPeerHost + ":" + listenPort;
	}

	public PeerAddress(String qnctwPeerHost, int listenPort) {
		if (qnctwPeerHost == null || qnctwPeerHost.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		this.qnctwPeerHost = qnctwPeerHost;
		this.listenPort = listenPort;
		this.address = new Address(qnctwPeerHost, listenPort);
	}
	public final String qnctwPeerHost;
	public final int listenPort;
	public final Address address;

}
