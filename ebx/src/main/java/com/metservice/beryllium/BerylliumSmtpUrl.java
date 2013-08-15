/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import com.metservice.argon.ArgonCompare;
import com.metservice.argon.HashCoder;

/**
 * @author roach
 */
public class BerylliumSmtpUrl implements Comparable<BerylliumSmtpUrl> {

	private static final int DefaultPort = -1;
	private static final int SavePort = BerylliumSmtpConnectionFactory.SavePort;

	public static BerylliumSmtpUrl newInstance(String qHost, String qUserName) {
		return newInstance(qHost, qUserName, DefaultPort);
	}

	public static BerylliumSmtpUrl newInstance(String qHost, String qUserName, int port) {
		if (qHost == null || qHost.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (qUserName == null || qUserName.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final String zlctwHost = qHost.trim().toLowerCase();
		if (zlctwHost.length() == 0) throw new IllegalArgumentException("invalid host>" + qHost + "<");
		final String zcctwUserName = qUserName.trim();
		if (zcctwUserName.length() == 0) throw new IllegalArgumentException("invalid userName>" + qUserName + "<");
		final int cport = port <= 0 ? DefaultPort : port;
		return new BerylliumSmtpUrl(zlctwHost, zcctwUserName, cport);
	}

	public static BerylliumSmtpUrl newSave(String qHost, String qUserName) {
		return newInstance(qHost, qUserName, SavePort);
	}

	private String hostPort() {
		final StringBuilder sb = new StringBuilder();
		sb.append(m_qlctwHost);
		if (m_port != DefaultPort) {
			sb.append(":").append(m_port);
		}
		return sb.toString();
	}

	@Override
	public int compareTo(BerylliumSmtpUrl rhs) {
		final int c0 = m_qlctwHost.compareTo(rhs.m_qlctwHost);
		if (c0 != 0) return c0;
		final int c1 = m_qcctwUserName.compareTo(rhs.m_qcctwUserName);
		if (c1 != 0) return c1;
		final int c2 = ArgonCompare.fwd(m_port, rhs.m_port);
		return c2;
	}

	public String credential(String zcctwPassword) {
		return m_qcctwUserName + "/" + zcctwPassword + "@" + hostPort();
	}

	public boolean equals(BerylliumSmtpUrl rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		if (m_hc != rhs.m_hc) return false;
		return m_qlctwHost.equals(rhs.m_qlctwHost) && m_qcctwUserName.equals(rhs.m_qcctwUserName) && (m_port == rhs.m_port);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof BerylliumSmtpUrl)) return false;
		return equals((BerylliumSmtpUrl) o);
	}

	public Integer getPort() {
		return m_port == DefaultPort ? null : new Integer(m_port);
	}

	@Override
	public int hashCode() {
		return m_hc;
	}

	public String qcctwUserName() {
		return m_qcctwUserName;
	}

	public String qlctwHost() {
		return m_qlctwHost;
	}

	@Override
	public String toString() {
		return m_qcctwUserName + "@" + hostPort();
	}

	private BerylliumSmtpUrl(String qlctwHost, String qcctwUserName, int port) {
		assert qlctwHost != null && qlctwHost.length() > 0;
		assert qcctwUserName != null && qcctwUserName.length() > 0;
		m_qlctwHost = qlctwHost;
		m_qcctwUserName = qcctwUserName;
		m_port = port;
		int hc = HashCoder.INIT;
		hc = HashCoder.and(hc, m_qlctwHost);
		hc = HashCoder.and(hc, m_qcctwUserName);
		hc = HashCoder.and(hc, m_port);
		m_hc = hc;
	}
	private final String m_qlctwHost;
	private final String m_qcctwUserName;
	private final int m_port;
	private final int m_hc;
}
