/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

import com.metservice.argon.ArgonText;

class Authority implements Comparable<Authority> {

	public static final String NamespaceEPSG = "EPSG";
	public static final String Separator = ":";

	public static final Authority newEPSG(String qccCode) {
		return newInstance(NamespaceEPSG, qccCode);
	}

	public static final Authority newInstance(String qncNamespace, String qccCode) {
		if (qncNamespace == null) throw new IllegalArgumentException("object is null");
		if (qccCode == null) throw new IllegalArgumentException("object is null");
		final String oquctwNamespace = ArgonText.oqtw(qncNamespace);
		if (oquctwNamespace == null) throw new IllegalArgumentException("empty namespace");
		final String oqccCode = ArgonText.oqtw(qccCode);
		if (oqccCode == null) throw new IllegalArgumentException("empty code");
		final StringBuilder sb = new StringBuilder();
		sb.append(oquctwNamespace.toUpperCase());
		sb.append(Separator);
		sb.append(oqccCode);
		return new Authority(sb.toString());
	}

	@Override
	public int compareTo(Authority rhs) {
		return m_qcctwQualifiedCode.compareTo(rhs.m_qcctwQualifiedCode);
	}

	public boolean equals(Authority rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return m_qcctwQualifiedCode.equals(rhs.m_qcctwQualifiedCode);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof Authority)) return false;
		return equals((Authority) o);
	}

	@Override
	public int hashCode() {
		return m_qcctwQualifiedCode.hashCode();
	}

	@Override
	public String toString() {
		return m_qcctwQualifiedCode;
	}

	private Authority(String qcctwQualifiedCode) {
		assert qcctwQualifiedCode != null && qcctwQualifiedCode.length() > 0;
		m_qcctwQualifiedCode = qcctwQualifiedCode;
	}

	private final String m_qcctwQualifiedCode;
}