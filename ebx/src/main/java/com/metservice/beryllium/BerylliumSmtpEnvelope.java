/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.metservice.argon.ArgonCompare;
import com.metservice.argon.ArgonJoiner;
import com.metservice.argon.ArgonSplitter;
import com.metservice.argon.Ds;
import com.metservice.argon.HashCoder;

/**
 * @author roach
 */
public class BerylliumSmtpEnvelope implements Comparable<BerylliumSmtpEnvelope> {

	public static final Pattern DefaultListSplitter = Pattern.compile("[;\\s]");
	public static final String RegexBox = "[a-z0-9._%+-]+";
	public static final String RegexHost = "[a-z0-9.-]+\\.[a-z]{2,4}";
	public static final Pattern AddressValidator = Pattern.compile(RegexBox + "@" + RegexHost);
	public static final String AddressSep = ";";

	private static int compareAddr(String[] zptLhs, String[] zptRhs) {
		final int clhs = zptLhs.length;
		final int crhs = zptRhs.length;
		for (int i = 0; i < clhs && i < crhs; i++) {
			final int c = zptLhs[i].compareTo(zptRhs[i]);
			if (c != 0) return c;
		}
		return ArgonCompare.fwd(clhs, crhs);
	}

	private static boolean equalsAddr(String[] zptLhs, String[] zptRhs) {
		if (zptLhs.length != zptRhs.length) return false;
		for (int i = 0; i < zptLhs.length; i++) {
			if (!zptLhs[i].equals(zptRhs[i])) return false;
		}
		return true;
	}

	private static String[] zptqlctwAddrAsc(String tag, String ozncList, Pattern oSep)
			throws BerylliumApiException {
		final String zlctwList = ozncList == null ? "" : ozncList.trim().toLowerCase();
		final Pattern sep = oSep == null ? DefaultListSplitter : oSep;
		final String[] zptqlctw = ArgonSplitter.zptqtwSplit(zlctwList, sep);
		final int itemCount = zptqlctw.length;
		if (itemCount == 0) return zptqlctw;
		final Set<String> xs = new HashSet<String>(itemCount);
		for (int i = 0; i < itemCount; i++) {
			xs.add(zptqlctw[i]);
		}
		final List<String> xlAsc = new ArrayList<String>(xs);
		Collections.sort(xlAsc);
		final int setCount = xlAsc.size();
		final List<String> zlInvalid = new ArrayList<String>();
		for (int i = 0; i < setCount; i++) {
			final String qlctw = xlAsc.get(i);
			if (!AddressValidator.matcher(qlctw).matches()) {
				zlInvalid.add(qlctw);
			}
		}
		final int invalidCount = zlInvalid.size();
		if (invalidCount > 0) {
			final String cm = invalidCount == 1 ? "an invalid address" : Integer.toString(invalidCount)
					+ " invalid addresses";
			final String msg = tag + "-address list contains " + cm + ": '" + ArgonJoiner.zJoin(zlInvalid, ";") + "'";
			throw new BerylliumApiException(msg);
		}
		return xlAsc.toArray(new String[setCount]);
	}

	public static BerylliumSmtpEnvelope newInstance(String zccSubject, String zncFrom, String zncToList, String ozncCcList)
			throws BerylliumApiException {
		return newInstance(zccSubject, zncFrom, zncToList, ozncCcList, DefaultListSplitter);
	}

	public static BerylliumSmtpEnvelope newInstance(String zccSubject, String zncFrom, String zncToList, String ozncCcList,
			Pattern oSep)
			throws BerylliumApiException {
		if (zccSubject == null) throw new IllegalArgumentException("object is null");
		if (zncFrom == null) throw new IllegalArgumentException("object is null");
		if (zncToList == null) throw new IllegalArgumentException("object is null");
		final String zcctwSubject = zccSubject.trim();
		if (zccSubject.length() == 0) throw new BerylliumApiException("Message requires a subject");
		final String[] zptqlctwFromAsc = zptqlctwAddrAsc("FROM", zncFrom, oSep);
		final String[] zptqlctwToAsc = zptqlctwAddrAsc("TO", zncToList, oSep);
		final String[] zptqlctwCcAsc = zptqlctwAddrAsc("CC", ozncCcList, oSep);

		if (zptqlctwFromAsc.length != 1) throw new BerylliumApiException("Message requires a FROM address'");
		if (zptqlctwToAsc.length == 0) throw new BerylliumApiException("Message requires at least one TO-recipient'");
		return new BerylliumSmtpEnvelope(zcctwSubject, zptqlctwFromAsc[0], zptqlctwToAsc, zptqlctwCcAsc);
	}

	public static String ztwComposeAddressList(String ozLhs, String ozRhs) {
		final StringBuilder sb = new StringBuilder();
		if (ozLhs != null) {
			sb.append(ozLhs);
		}
		if (ozRhs != null && ozRhs.length() > 0) {
			if (sb.length() > 0) {
				sb.append(AddressSep);
			}
			sb.append(ozRhs);
		}
		return sb.toString().trim();
	}

	@Override
	public int compareTo(BerylliumSmtpEnvelope rhs) {
		final int c0 = m_qcctwSubject.compareTo(rhs.m_qcctwSubject);
		if (c0 != 0) return c0;
		final int c1 = m_qlctwFrom.compareTo(rhs.m_qlctwFrom);
		if (c1 != 0) return c1;
		final int c2 = compareAddr(m_xptqlctwToAsc, rhs.m_xptqlctwToAsc);
		if (c2 != 0) return c2;
		final int c3 = compareAddr(m_zptqlctwCcAsc, rhs.m_zptqlctwCcAsc);
		return c3;
	}

	public void describe(Ds ds) {
		ds.a("SUBJECT", m_qcctwSubject);
		ds.a("TO", m_xptqlctwToAsc);
		ds.a("CC", m_zptqlctwCcAsc);
		ds.a("FROM", m_qlctwFrom);
	}

	public boolean equals(BerylliumSmtpEnvelope rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		if (m_hc != rhs.m_hc) return false;
		if (!m_qcctwSubject.equals(rhs.m_qcctwSubject)) return false;
		if (!m_qlctwFrom.equals(rhs.m_qlctwFrom)) return false;
		if (!equalsAddr(m_xptqlctwToAsc, rhs.m_xptqlctwToAsc)) return false;
		if (!equalsAddr(m_zptqlctwCcAsc, rhs.m_zptqlctwCcAsc)) return false;
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof BerylliumSmtpEnvelope)) return false;
		return equals((BerylliumSmtpEnvelope) o);
	}

	@Override
	public int hashCode() {
		return m_hc;
	}

	public String qcctwSubject() {
		return m_qcctwSubject;
	}

	public String qlctwFrom() {
		return m_qlctwFrom;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("SUBJECT: " + m_qcctwSubject).append("\n");
		sb.append("TO: " + ArgonJoiner.zJoin(m_xptqlctwToAsc, ";")).append("\n");
		if (m_zptqlctwCcAsc.length > 0) {
			sb.append("CC: " + ArgonJoiner.zJoin(m_zptqlctwCcAsc, ";")).append("\n");
		}
		sb.append("FROM: ").append(m_qlctwFrom);
		return sb.toString();
	}

	public String[] xptqlctwToAsc() {
		return m_xptqlctwToAsc;
	}

	public String[] zptqlctwCcAsc() {
		return m_zptqlctwCcAsc;
	}

	private BerylliumSmtpEnvelope(String qcctwSubject, String qlctwFrom, String[] xptqlctwToAsc, String[] zptqlctwCcAsc) {
		assert qcctwSubject != null && qcctwSubject.length() > 0;
		assert qlctwFrom != null && qlctwFrom.length() > 0;
		assert xptqlctwToAsc != null && xptqlctwToAsc.length > 0;
		assert zptqlctwCcAsc != null;
		m_qcctwSubject = qcctwSubject;
		m_qlctwFrom = qlctwFrom;
		m_xptqlctwToAsc = xptqlctwToAsc;
		m_zptqlctwCcAsc = zptqlctwCcAsc;
		int hc = HashCoder.INIT;
		hc = HashCoder.and(hc, qcctwSubject);
		hc = HashCoder.and(hc, qlctwFrom);
		hc = HashCoder.and(hc, xptqlctwToAsc);
		hc = HashCoder.and(hc, zptqlctwCcAsc);
		m_hc = hc;
	}
	private final String m_qcctwSubject;
	private final String m_qlctwFrom;
	private final String[] m_xptqlctwToAsc;
	private final String[] m_zptqlctwCcAsc;
	private final int m_hc;
}
