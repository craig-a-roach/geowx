/*
 * Copyright 2009 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.Hashtable;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.metservice.argon.CArgon;
import com.metservice.argon.management.IArgonSpaceId;
import com.metservice.argon.text.ArgonNumber;

/**
 * @author roach
 */
public class NeonSpaceId implements Comparable<NeonSpaceId>, IArgonSpaceId {

	public static NeonSpaceId newInstance(int port)
			throws NeonCfgSyntaxException {
		final int lo = CArgon.LIMIT_PORT_USERLO;
		final int hi = CArgon.LIMIT_PORT_HI;
		if (port < lo || port > hi) {
			final String m = "Invalid identifier '" + port + "'...must be in range " + lo + " to " + hi;
			throw new NeonCfgSyntaxException(m);
		}
		final String qId = ArgonNumber.intToDec(port, 5);
		final Hashtable<String, String> sourceTable = new Hashtable<String, String>(4);
		sourceTable.put("type", "Space");
		sourceTable.put("port", qId);
		try {
			final ObjectName spaceName = new ObjectName(CNeon.ServiceId.qtwDomain, sourceTable);
			return new NeonSpaceId(port, qId, spaceName);
		} catch (final MalformedObjectNameException ex) {
			throw new NeonCfgSyntaxException("Malformed neon space name '" + sourceTable + "..." + ex);
		}
	}

	public static NeonSpaceId newInstance(String qSpec)
			throws NeonCfgSyntaxException {
		if (qSpec == null || qSpec.length() == 0) throw new IllegalArgumentException("string is null or empty");

		try {
			return newInstance(Integer.parseInt(qSpec));
		} catch (final NumberFormatException exNF) {
			throw new NeonCfgSyntaxException("Malformed identifier '" + qSpec + "'...must be numeric");
		}
	}

	@Override
	public int compareTo(NeonSpaceId rhs) {
		if (m_port < rhs.m_port) return -1;
		if (m_port > rhs.m_port) return +1;
		return 0;
	}

	public boolean equals(NeonSpaceId rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return m_port == rhs.m_port;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof NeonSpaceId)) return false;
		return equals((NeonSpaceId) o);
	}

	public String format() {
		return m_qId;
	}

	@Override
	public int hashCode() {
		return m_port;
	}

	public int listenPort() {
		return m_port;
	}

	/**
	 * Returns the JMX name of a space object constructed from this id. The name will have a domain of
	 * <code>org.metservice.neon</code> and the properties:
	 * <ul>
	 * <li>type=Space</li>
	 * <li>port=<i>listening port number</i></li>
	 * </ul>
	 * 
	 * @return JMX object name
	 */
	public ObjectName spaceObjectName() {
		return m_spaceName;
	}

	@Override
	public String toString() {
		return m_qId;
	}

	private NeonSpaceId(int port, String qId, ObjectName spaceName) {
		m_port = port;
		m_qId = qId;
		m_spaceName = spaceName;
	}

	private final int m_port;
	private final String m_qId;
	private final ObjectName m_spaceName;
}
