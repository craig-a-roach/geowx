/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
public class KryptonDataRecord {

	public KryptonMetaRecord meta() {
		return m_meta;
	}

	public KryptonInterpolator newInterpolator()
			throws KryptonUnpackException, KryptonUnsupportedException {
		final IKryptonGeoProjector oProjector = m_gridDecode.oProjector;
		if (oProjector == null) {
			final String pt = m_gridDecode.geography.projectionType();
			final String m = "Interpolation not supported for " + pt + " projections";
			throw new KryptonUnsupportedException(m);
		}
		if (oProjector.isThin()) {
			final String m = "Interpolation not supported for thin grids";
			throw new KryptonUnsupportedException(m);
		}
		final KryptonArray array = m_dataSource.newArray(m_gridDecode.arrayFactory);
		return new KryptonInterpolator(array, oProjector);
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("meta", m_meta);
		ds.a("gridDecode", m_gridDecode);
		ds.a("dataSource", m_dataSource);
		return ds.s();
	}

	public KryptonDataRecord(KryptonMetaRecord meta, KryptonGridDecode gridDecode, IKryptonDataSource dataSource) {
		if (meta == null) throw new IllegalArgumentException("object is null");
		if (gridDecode == null) throw new IllegalArgumentException("object is null");
		if (dataSource == null) throw new IllegalArgumentException("object is null");
		m_meta = meta;
		m_gridDecode = gridDecode;
		m_dataSource = dataSource;
	}
	private final KryptonMetaRecord m_meta;
	private final KryptonGridDecode m_gridDecode;
	private final IKryptonDataSource m_dataSource;
}
