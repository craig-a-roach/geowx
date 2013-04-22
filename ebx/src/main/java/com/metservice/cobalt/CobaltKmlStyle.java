/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.cobalt;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
public class CobaltKmlStyle {

	public boolean isColumnSenseBalloon() {
		return m_isColumnSenseBalloon;
	}

	public CobaltKmlColor polygonFillColor() {
		return m_polygonFillColor;
	}

	public int polygonLineWidth() {
		return m_polygonLineWidth;
	}

	public void setColumnSenseBalloon(boolean enable) {
		m_isColumnSenseBalloon = enable;
	}

	public void setPolygonFillColor(String zSpec) {
		if (zSpec == null) throw new IllegalArgumentException("object is null");
		m_polygonFillColor = CobaltKmlColor.newABGR(zSpec);
	}

	public void setPolygonLineWidth(int width) {
		m_polygonLineWidth = width;
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("polygonFillColor", m_polygonFillColor);
		ds.a("polygonLineWidth", m_polygonLineWidth);
		ds.a("isColumnSenseBalloon", m_isColumnSenseBalloon);
		return ds.s();
	}

	public CobaltKmlStyle() {
	}
	private CobaltKmlColor m_polygonFillColor = CobaltKmlColor.newABGR("7F????7F");
	private int m_polygonLineWidth = 1;
	private boolean m_isColumnSenseBalloon = true;
}
