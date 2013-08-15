/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import org.eclipse.jetty.client.Address;

/**
 * @author roach
 */
class TestImpHostPort implements IBerylliumHttpAddressable {

	@Override
	public Address httpAddress() {
		return m_httpAddress;
	}

	@Override
	public String toString() {
		return m_httpAddress.toString();
	}

	public TestImpHostPort(String host, int port) {
		m_httpAddress = new Address(host, port);
	}

	private final Address m_httpAddress;
}
