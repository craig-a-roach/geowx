/*
 * Copyright 2009 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

import java.util.Hashtable;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.metservice.argon.management.IArgonSpaceId;

/**
 * @author roach
 */
public class BoronSpaceId implements Comparable<BoronSpaceId>, IArgonSpaceId {

	public static BoronSpaceId newInstance(String qccId)
			throws BoronCfgSyntaxException {
		if (qccId == null || qccId.length() == 0) throw new IllegalArgumentException("string is null or empty");

		final Hashtable<String, String> sourceTable = new Hashtable<String, String>(4);
		sourceTable.put("type", "Space");
		sourceTable.put("id", qccId);
		try {
			final ObjectName spaceName = new ObjectName(CBoron.ServiceId.qtwDomain, sourceTable);
			return new BoronSpaceId(qccId, spaceName);
		} catch (final MalformedObjectNameException ex) {
			throw new BoronCfgSyntaxException("Malformed boron space name  '" + sourceTable + "'..." + ex);
		}
	}

	@Override
	public int compareTo(BoronSpaceId rhs) {
		return m_qccId.compareTo(rhs.m_qccId);
	}

	public boolean equals(BoronSpaceId rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return m_qccId.equals(rhs.m_qccId);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof BoronSpaceId)) return false;
		return equals((BoronSpaceId) o);
	}

	public String format() {
		return m_qccId;
	}

	@Override
	public int hashCode() {
		return m_qccId.hashCode();
	}

	/**
	 * Returns the JMX name of a space object constructed from this id. The name will have a domain of
	 * <code>org.metservice.boron</code> and the properties:
	 * <ul>
	 * <li>type=Space</li>
	 * <li>id=<i>space identifier</i></li>
	 * </ul>
	 * 
	 * @return JMX object name
	 */
	public ObjectName spaceObjectName() {
		return m_spaceName;
	}

	@Override
	public String toString() {
		return m_qccId;
	}

	private BoronSpaceId(String qccId, ObjectName spaceName) {
		m_qccId = qccId;
		m_spaceName = spaceName;
	}

	private final String m_qccId;
	private final ObjectName m_spaceName;
}
