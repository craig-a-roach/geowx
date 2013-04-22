/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.cobalt;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author roach
 */
class NCubeIterator implements Iterator<CobaltRecord> {

	@Override
	public boolean hasNext() {
		return m_shapeIndex < m_xptShapeAsc.length;
	}

	@Override
	public CobaltRecord next() {
		final int shapeCount = m_xptShapeAsc.length;
		if (m_shapeIndex == shapeCount) throw new NoSuchElementException("Exhausted at " + toString());
		if (m_oShapeIterator == null) {
			m_oShapeIterator = m_xptShapeAsc[m_shapeIndex].shapeIterator();
		}
		final CobaltRecord record = m_oShapeIterator.next();
		if (!m_oShapeIterator.hasNext()) {
			m_oShapeIterator = null;
			m_shapeIndex++;
		}

		return record;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Cannot remove shape from ncube");
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(m_shapeIndex);
		if (m_oShapeIterator != null) {
			sb.append(">");
			sb.append(m_oShapeIterator);
		}
		return sb.toString();
	}

	public NCubeIterator(CobaltNCube src) {
		if (src == null) throw new IllegalArgumentException("object is null");
		m_xptShapeAsc = src.xptShapeAsc();
		m_shapeIndex = 0;
	}
	private final CobaltSequence[] m_xptShapeAsc;
	private int m_shapeIndex;
	private Iterator<CobaltRecord> m_oShapeIterator;
}
