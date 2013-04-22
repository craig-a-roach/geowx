/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
public class KryptonInterpolator {

	public float bilinear(double longitude, double latititude) {
		return m_projector.bilinearInterpolate(m_array, longitude, latititude);
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("projector", m_projector);
		ds.a("array", m_array);
		return ds.s();
	}

	public KryptonInterpolator(KryptonArray array, IKryptonGeoProjector projector) {
		if (array == null) throw new IllegalArgumentException("object is null");
		if (projector == null) throw new IllegalArgumentException("object is null");
		m_array = array;
		m_projector = projector;
	}
	private final KryptonArray m_array;
	private final IKryptonGeoProjector m_projector;
}
