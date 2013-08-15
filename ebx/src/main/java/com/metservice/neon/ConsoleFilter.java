/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

/**
 * @author roach
 */
class ConsoleFilter {

	public boolean match(ConsoleEntry entry) {
		if (m_ozpt == null) return true;
		for (int i = 0; i < m_ozpt.length; i++) {
			if (entry.type == m_ozpt[i]) return true;
		}
		return false;
	}

	public static ConsoleFilter newInstance(ConsoleType ct) {
		if (ct == null) throw new IllegalArgumentException("object is null");
		final ConsoleType[] xpt = new ConsoleType[1];
		xpt[0] = ct;
		return new ConsoleFilter(xpt);
	}

	public static ConsoleFilter newInstance(ConsoleType[] xpt) {
		if (xpt == null || xpt.length == 0) throw new IllegalArgumentException("array is null or empty");
		return new ConsoleFilter(xpt);
	}

	private ConsoleFilter(ConsoleType[] ozpt) {
		m_ozpt = ozpt;
	}

	public static final ConsoleFilter Any = new ConsoleFilter(null);
	public static final ConsoleFilter None = new ConsoleFilter(new ConsoleType[0]);

	private final ConsoleType[] m_ozpt;
}
