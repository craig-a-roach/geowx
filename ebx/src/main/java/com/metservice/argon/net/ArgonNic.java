/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.net;

import java.net.InetAddress;
import java.net.NetworkInterface;

/**
 * @author roach
 */
public class ArgonNic {

	@Override
	public String toString() {
		return qccName + "=" + inetAddress;
	}

	public ArgonNic(String qccName, NetworkInterface networkInterface, InetAddress inetAddress) {
		if (qccName == null || qccName.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (networkInterface == null) throw new IllegalArgumentException("object is null");
		if (inetAddress == null) throw new IllegalArgumentException("object is null");
		this.qccName = qccName;
		this.networkInterface = networkInterface;
		this.inetAddress = inetAddress;
	}

	/**
	 * Name of network interface (case-sensitive).
	 */
	public final String qccName;

	/**
	 * Hardware information, and a list of all IP addresses bound to this network interface.
	 */
	public final NetworkInterface networkInterface;

	/**
	 * The IP address that will be used when sending and receiving from this interface.
	 */
	public final InetAddress inetAddress;
}
