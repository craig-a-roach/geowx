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
public class CompositeIterator implements Iterator<CobaltRecord> {

	private void makeNext(CobaltRecord record) {
		if (!m_hasNext) throw new NoSuchElementException("Exhausted at " + toString());
		final int cardinality = m_path.length;
		for (int i = 0; i < cardinality; i++) {
			final int productIndex = m_path[i];
			final ICobaltProduct product = m_matrix[i][productIndex];
			if (product instanceof ICobaltCoordinate) {
				record.put((ICobaltCoordinate) product);
				continue;
			}
			if (product instanceof Composite) {
				final Composite composite = (Composite) product;
				CompositeIterator vSubIterator = m_subIterators[i];
				if (vSubIterator == null) {
					vSubIterator = new CompositeIterator(composite);
					m_subIterators[i] = vSubIterator;
				}
				vSubIterator.makeNext(record);
				continue;
			}
			throw new IllegalStateException("Unexpected product at " + toString() + "..." + product);
		}

		boolean carry = true;
		for (int i = cardinality - 1; carry && i >= 0; i--) {
			final int productCount = m_productCount[i];
			final int exProductIndex = m_path[i];
			final CompositeIterator oExSubIterator = m_subIterators[i];
			final int neoProductIndex;
			if (oExSubIterator == null) {
				neoProductIndex = exProductIndex + 1;
			} else {
				if (oExSubIterator.hasNext()) {
					neoProductIndex = exProductIndex;
				} else {
					m_subIterators[i] = null;
					neoProductIndex = exProductIndex + 1;
				}
			}
			if (neoProductIndex < productCount) {
				m_path[i] = neoProductIndex;
				carry = false;
			} else {
				m_path[i] = 0;
			}
		}
		if (carry) {
			m_hasNext = false;
		}
	}

	@Override
	public boolean hasNext() {
		return m_hasNext;
	}

	@Override
	public CobaltRecord next() {
		final CobaltRecord record = new CobaltRecord();
		makeNext(record);
		return record;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Cannot remove product from composite");
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < m_path.length; i++) {
			if (i > 0) {
				sb.append(',');
			}
			if (m_hasNext) {
				sb.append(m_path[i]);
			}
			sb.append('#');
			sb.append(m_productCount[i]);
			final CompositeIterator oSubIterator = m_subIterators[i];
			if (oSubIterator != null) {
				sb.append("(");
				sb.append(oSubIterator);
				sb.append(")");
			}
		}
		return sb.toString();
	}

	public CompositeIterator(Composite src) {
		final CobaltSequence[] xptSequenceAscDimSet = src.xptSequenceAscDimSet();
		final int cardinality = xptSequenceAscDimSet.length;
		m_path = new int[cardinality];
		m_productCount = new int[cardinality];
		m_subIterators = new CompositeIterator[cardinality];
		m_matrix = new ICobaltProduct[cardinality][];
		for (int i = 0; i < cardinality; i++) {
			final CobaltSequence cobaltSequence = xptSequenceAscDimSet[i];
			final ICobaltProduct[] xptProductsAsc = cobaltSequence.xptProductsAsc();
			final int productCount = xptProductsAsc.length;
			if (productCount == 0) throw new IllegalArgumentException("empty sequence index " + i + " in " + src);
			m_path[i] = 0;
			m_productCount[i] = productCount;
			m_matrix[i] = xptProductsAsc;
		}
		m_hasNext = true;
	}

	private final int[] m_path;
	private final int[] m_productCount;
	private final CompositeIterator[] m_subIterators;
	private final ICobaltProduct[][] m_matrix;
	private boolean m_hasNext;
}
