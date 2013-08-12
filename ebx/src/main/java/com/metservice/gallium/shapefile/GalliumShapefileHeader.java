/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.shapefile;

import com.metservice.argon.Ds;
import com.metservice.gallium.GalliumBoundingBoxD;

/**
 * @author roach
 */
public class GalliumShapefileHeader {

	public GalliumBoundingBoxD boundingBox() {
		return m_box;
	}

	public double mMax() {
		return m_mMax;
	}

	public double mMin() {
		return m_mMin;
	}

	public int shapeType() {
		return m_shapeType;
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("shapeType", m_shapeType);
		ds.a("boundingBox", m_box);
		ds.a("zMin", m_zMin);
		ds.a("zMax", m_zMax);
		ds.a("mMin", m_mMin);
		ds.a("mMax", m_mMax);
		ds.a("version", m_version);
		return ds.s();
	}

	public double zMax() {
		return m_zMax;
	}

	public double zMin() {
		return m_zMin;
	}

	public GalliumShapefileHeader(int version, int shapeType, GalliumBoundingBoxD box, double zMin, double zMax, double mMin,
			double mMax) {
		if (box == null) throw new IllegalArgumentException("object is null");
		m_version = version;
		m_shapeType = shapeType;
		m_box = box;
		m_zMin = zMin;
		m_zMax = zMax;
		m_mMin = mMin;
		m_mMax = mMax;
	}
	private final int m_version;
	private final int m_shapeType;
	private final GalliumBoundingBoxD m_box;
	private final double m_zMin;
	private final double m_zMax;
	private final double m_mMin;
	private final double m_mMax;
}
