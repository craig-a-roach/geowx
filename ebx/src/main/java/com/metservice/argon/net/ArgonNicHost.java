/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.net;

/**
 * @author roach
 */
public class ArgonNicHost {

	@Override
	public String toString() {
		return qlcName;
	}

	public static ArgonNicHost newInstance(ArgonNic nic) {
		if (nic == null) throw new IllegalArgumentException("object is null");
		final String qlcHostName = nic.inetAddress.getCanonicalHostName().toLowerCase();
		return new ArgonNicHost(nic, qlcHostName);
	}

	private ArgonNicHost(ArgonNic nic, String qlcName) {
		assert nic != null;
		assert qlcName != null && qlcName.length() > 0;
		this.nic = nic;
		this.qlcName = qlcName;
	}

	public final ArgonNic nic;
	public final String qlcName;
}
