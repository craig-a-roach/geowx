/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.esri;

import com.metservice.argon.ArgonText;

class Authority implements IWktEmit, Comparable<Authority> {

	public static final String NamespaceEPSG = "EPSG";
	public static final String NamespaceESRI = "ESRI";
	public static final String NamespaceCUSTOM = "CUSTOM";
	public static final int CodeLo_EPSG = 1000;
	public static final int CodeHiEx_EPSG = 32_767;
	public static final int CodeLo_ESRI = 32_767;
	public static final int CodeHiEx_ESRI = 200_000;
	public static final int CodeLo_CUSTOM = 200_000;
	public static final int CodeHiEx_CUSTOM = 209_199;
	public static final char Separator = ':';

	private static void validate(String ns, int code, int lo, int hiex) {
		if (code >= lo && code < hiex) return;
		final String m = "invalid " + ns + " code " + code + "; valid range is " + lo + " to " + hiex;
		throw new IllegalArgumentException(m);
	}

	public static final Authority createInstance(Title t) {
		if (t == null) throw new IllegalArgumentException("object is null");
		Authority oMatch = null;
		oMatch = createNS(t, NamespaceEPSG);
		if (oMatch != null) return oMatch;
		oMatch = createNS(t, NamespaceESRI);
		if (oMatch != null) return oMatch;
		final int code = ArgonText.parse(t.quctwKey(), 0);
		if (code == 0) return null;
		return createWKID(code);
	}

	public static final Authority createNS(Title t, String qucNS) {
		if (t == null) throw new IllegalArgumentException("object is null");
		if (qucNS == null || qucNS.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final String quctwKey = t.quctwKey();
		final int lenKey = quctwKey.length();
		final int lenNS = qucNS.length();
		if (lenKey <= lenNS) return null;
		if (!quctwKey.startsWith(qucNS)) return null;
		final String qtwCode = quctwKey.substring(lenNS);
		final int code = ArgonText.parse(qtwCode, 0);
		return (code == 0) ? null : newInstance(qucNS, code);
	}

	public static final Authority createWKID(int code) {
		if (code >= CodeLo_EPSG && code < CodeHiEx_EPSG) return newInstance(NamespaceEPSG, code);
		if (code >= CodeLo_ESRI && code < CodeHiEx_ESRI) return newInstance(NamespaceESRI, code);
		if (code >= CodeLo_CUSTOM && code < CodeHiEx_CUSTOM) return newInstance(NamespaceCUSTOM, code);
		return null;
	}

	public static final Authority newCUSTOM(int code) {
		validate(NamespaceCUSTOM, code, CodeLo_CUSTOM, CodeHiEx_CUSTOM);
		return newInstance(NamespaceCUSTOM, code);
	}

	public static final Authority newEPSG(int code) {
		validate(NamespaceEPSG, code, CodeLo_EPSG, CodeHiEx_EPSG);
		return newInstance(NamespaceEPSG, code);
	}

	public static final Authority newESRI(int code) {
		validate(NamespaceESRI, code, CodeLo_ESRI, CodeHiEx_ESRI);
		return newInstance(NamespaceESRI, code);
	}

	public static final Authority newInstance(String qncNamespace, int code) {
		return newInstance(qncNamespace, Integer.toString(code));
	}

	public static final Authority newInstance(String qncNamespace, String qccCode) {
		if (qncNamespace == null) throw new IllegalArgumentException("object is null");
		if (qccCode == null) throw new IllegalArgumentException("object is null");
		final String oqtwNamespace = ArgonText.oqtw(qncNamespace);
		if (oqtwNamespace == null) throw new IllegalArgumentException("empty namespace");
		final String oqccCode = ArgonText.oqtw(qccCode);
		if (oqccCode == null) throw new IllegalArgumentException("empty code");
		return new Authority(oqtwNamespace.toUpperCase(), oqccCode);
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

	public String qcctwCode() {
		return m_qcctwCode;
	}

	public String qcctwQualifiedCode() {
		return m_qcctwQualifiedCode;
	}

	public String quctwNamespace() {
		return m_quctwNamespace;
	}

	@Override
	public String toString() {
		return m_qcctwQualifiedCode;
	}

	@Override
	public WktStructure toWkt() {
		return new WktStructure("AUTHORITY", m_quctwNamespace, m_qcctwCode);
	}

	private Authority(String quctwNamespace, String qcctwCode) {
		assert quctwNamespace != null && quctwNamespace.length() > 0;
		assert qcctwCode != null && qcctwCode.length() > 0;
		m_quctwNamespace = quctwNamespace;
		m_qcctwCode = qcctwCode;
		final StringBuilder sb = new StringBuilder();
		sb.append(quctwNamespace);
		sb.append(Separator);
		sb.append(qcctwCode);
		m_qcctwQualifiedCode = sb.toString();
	}
	private final String m_quctwNamespace;
	private final String m_qcctwCode;
	private final String m_qcctwQualifiedCode;
}