/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

/**
 * @author roach
 */
public class ArgonServiceId {

	public boolean equals(ArgonServiceId rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return qtwDomain.equals(rhs.qtwDomain);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof ArgonServiceId)) return false;
		return equals((ArgonServiceId) o);
	}

	@Override
	public int hashCode() {
		return m_hc;
	}

	@Override
	public String toString() {
		return qtwDomain;
	}

	public ArgonServiceId(String vendor, String service) {
		if (vendor == null) throw new IllegalArgumentException("object is null");
		if (service == null) throw new IllegalArgumentException("object is null");
		final String ztwVendor = vendor.trim();
		if (ztwVendor.length() == 0) throw new IllegalArgumentException("invalid vendor>" + vendor + "<");
		this.qtwVendor = ztwVendor;
		final String ztwService = service.trim();
		if (ztwService.length() == 0) throw new IllegalArgumentException("invalid service>" + service + "<");
		this.qtwService = ztwService;
		final StringBuilder bdn = new StringBuilder();
		bdn.append(qtwVendor);
		bdn.append('.');
		bdn.append(qtwService);
		this.qtwDomain = bdn.toString();
		m_hc = this.qtwDomain.hashCode();
	}

	public final String qtwVendor;
	public final String qtwService;
	public final String qtwDomain;
	private final int m_hc;
}
