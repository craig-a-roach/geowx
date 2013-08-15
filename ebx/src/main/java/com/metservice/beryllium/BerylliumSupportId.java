/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.jetty.server.Request;

import com.metservice.argon.ArgonFormatException;
import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.json.JsonAccessor;
import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;
import com.metservice.argon.net.ArgonNic;
import com.metservice.argon.net.ArgonNicDiscoverer;

/**
 * @author roach
 */
public class BerylliumSupportId implements Comparable<BerylliumSupportId> {

	private static final String PrefixIP = "IP.";
	private static final int PrefixIPLen = PrefixIP.length();

	public static final BerylliumSupportId Loopback = new BerylliumSupportId(PrefixIP + "127.0.0.1");

	@Override
	public int compareTo(BerylliumSupportId rhs) {
		return m_qid.compareTo(rhs.m_qid);
	}

	public boolean equals(BerylliumSupportId rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return m_qid.equals(rhs.m_qid);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof BerylliumSupportId)) return false;
		return equals((BerylliumSupportId) o);
	}

	public String format() {
		return m_qid;
	}

	@Override
	public int hashCode() {
		return m_qid.hashCode();
	}

	public void saveTo(String pname, JsonObject dst) {
		if (dst == null) throw new IllegalArgumentException("object is null");
		dst.putString(pname, m_qid);
	}

	@Override
	public String toString() {
		return m_qid;
	}

	private static BerylliumSupportId newIP(String qtwSpec)
			throws BerylliumApiException {
		assert qtwSpec != null && qtwSpec.length() > 0;
		final String ztwBody = qtwSpec.substring(PrefixIPLen);
		if (ztwBody.length() == 0) {
			final String m = "Empty IP address field";
			throw new BerylliumApiException(m);
		}
		return newInstance(newInetAddress(ztwBody));
	}

	public static BerylliumSupportId createInstance(JsonObject src, String pname)
			throws JsonSchemaException {
		if (src == null) throw new IllegalArgumentException("object is null");
		final JsonAccessor accessor = src.accessor(pname);
		if (!accessor.isDefinedNonNull()) return null;
		return new BerylliumSupportId(accessor.datumQtwString());
	}

	public static InetAddress newInetAddress(String qtwSpec)
			throws BerylliumApiException {

		if (ArgonNicDiscoverer.isNicSpec(qtwSpec)) {
			try {
				final ArgonNic oNic = ArgonNicDiscoverer.findUnicast(qtwSpec);
				if (oNic == null) throw new BerylliumApiException("No network interface matches '" + qtwSpec + "'");
				return oNic.inetAddress;
			} catch (final ArgonPermissionException ex) {
				return ArgonNicDiscoverer.selectLoopbackIPv4InetAddress();
			} catch (final ArgonFormatException ex) {
				final String m = "Invalid network interface specification '" + qtwSpec + "'";
				throw new BerylliumApiException(m + "..." + ex.getMessage());
			}
		}

		try {
			return InetAddress.getByName(qtwSpec);
		} catch (final UnknownHostException ex) {
			final String m = "Cannot determine IP address of host name '" + qtwSpec + "'";
			throw new BerylliumApiException(m);
		}
	}

	public static BerylliumSupportId newInstance(InetAddress inetAddress) {
		final String qRemoteIP = inetAddress.getHostAddress();
		return new BerylliumSupportId(PrefixIP + qRemoteIP);
	}

	public static BerylliumSupportId newInstance(JsonObject src, String pname)
			throws JsonSchemaException {
		if (src == null) throw new IllegalArgumentException("object is null");
		return new BerylliumSupportId(src.accessor(pname).datumQtwString());
	}

	public static BerylliumSupportId newInstance(Request rq) {
		if (rq == null) throw new IllegalArgumentException("object is null");
		final String qRemoteIP = rq.getRemoteAddr();
		return new BerylliumSupportId(PrefixIP + qRemoteIP);
	}

	public static BerylliumSupportId newInstance(String ozSpec)
			throws BerylliumApiException {
		final String ztwSpec = ozSpec == null ? "" : ozSpec.trim();
		if (ztwSpec.length() == 0) return Loopback;

		if (ztwSpec.startsWith(PrefixIP)) return newIP(ztwSpec);
		throw new BerylliumApiException("Unsupported support id type");
	}

	private BerylliumSupportId(String qid) {
		assert qid != null && qid.length() > 0;
		m_qid = qid;
	}

	private final String m_qid;
}
