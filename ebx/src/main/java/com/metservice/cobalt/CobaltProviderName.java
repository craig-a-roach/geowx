/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.cobalt;

import com.metservice.argon.ArgonApiException;
import com.metservice.argon.ArgonText;
import com.metservice.argon.text.ArgonTransformer;

/**
 * @author roach
 */
public class CobaltProviderName implements Comparable<CobaltProviderName> {

	public static CobaltProviderName createSanitizedInstance(String vqccName) {
		final String zccPosix = ArgonTransformer.zPosixSanitized(vqccName);
		if (zccPosix.length() == 0) return null;
		return new CobaltProviderName(zccPosix);
	}

	public static CobaltProviderName newInstance(String vqccName)
			throws ArgonApiException {
		return new CobaltProviderName(ArgonText.qtwPosixName(vqccName));
	}

	public static CobaltProviderName newSanitizedInstance(String vqccName) {
		final CobaltProviderName oNeo = createSanitizedInstance(vqccName);
		if (oNeo == null) throw new IllegalArgumentException("no safe characters>" + vqccName + "<");
		return oNeo;
	}

	@Override
	public int compareTo(CobaltProviderName rhs) {
		return m_qcctw.compareTo(rhs.m_qcctw);
	}

	public boolean equals(CobaltProviderName rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return m_qcctw.equals(rhs.m_qcctw);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof CobaltProviderName)) return false;
		return equals((CobaltProviderName) o);
	}

	public String format() {
		return m_qcctw;
	}

	@Override
	public int hashCode() {
		return m_qcctw.hashCode();
	}

	@Override
	public String toString() {
		return format();
	}

	private CobaltProviderName(String qcctw) {
		assert qcctw != null && qcctw.length() > 0;
		m_qcctw = qcctw;
	}

	private final String m_qcctw;
}
