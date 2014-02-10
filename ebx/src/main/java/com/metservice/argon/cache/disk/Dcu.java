/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.cache.disk;

import com.metservice.argon.Binary;
import com.metservice.argon.CArgon;

/**
 * @author roach
 */
class Dcu {

	private static final int KPU = 8;
	private static final int BPU = KPU * CArgon.K;
	private static final int NA = -1;

	public static final Dcu NotAvailable = new Dcu(NA);
	private static final Dcu Zero = new Dcu(0);
	private static final Dcu One = new Dcu(1);

	private static int bc2u(int bc) {
		if (bc <= 0L) return 0;
		if (bc <= BPU) return 1;
		final int u = bc / BPU;
		final int bcf = (u * BPU);
		return bc == bcf ? u : (u + 1);
	}

	private static int dcu2kb(int dcu) {
		return (dcu == NA || dcu == 0) ? 0 : (dcu * KPU);
	}

	private static Dcu fromu(int u) {
		if (u < 0) return NotAvailable;
		if (u == 0) return Zero;
		if (u == 1) return One;
		return new Dcu(u);
	}

	public static boolean exists(int dcu) {
		return dcu != NA;
	}

	public static int kbUsage(int dcu) {
		return dcu2kb(dcu);
	}

	public static Dcu newInstance(Binary content) {
		if (content == null) throw new IllegalArgumentException("object is null");
		return fromu(bc2u(content.byteCount()));
	}

	public static Dcu newInstance(int un) {
		return fromu(un);
	}

	public boolean exists() {
		return m_un != NA;
	}

	public int toInteger() {
		return m_un;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		if (m_un == NA) {
			sb.append("na");
		} else {
			sb.append(m_un);
			sb.append("k8");
		}
		return sb.toString();
	}

	public int usage() {
		return m_un == NA ? 0 : m_un;
	}

	private Dcu(int un) {
		m_un = un;
	}
	private final int m_un;
}
