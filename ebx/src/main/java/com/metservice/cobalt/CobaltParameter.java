/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.cobalt;

import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;

/**
 * @author roach
 */
public class CobaltParameter implements ICobaltCoordinate {

	public static final String PName_id = "id";
	public static final String PName_unit = "u";
	public static final String PName_description = "d";

	@Override
	public void addTo(KmlFeatureText kft) {
		if (kft instanceof KmlFeatureDescription) {
			final KmlFeatureDescription kfd = (KmlFeatureDescription) kft;
			kfd.addBoldText(m_qccId);
			if (m_zccUnit.length() > 0) {
				kfd.addText(" (" + m_zccUnit + ")");
			}
			if (m_zDescription.length() > 0) {
				kfd.addText(" ");
				kfd.addText(m_zDescription);
			}
		} else {
			kft.addText(m_qccId);
		}
	}

	@Override
	public int compareTo(ICobaltProduct rhs) {
		if (rhs instanceof CobaltParameter) {
			final CobaltParameter r = (CobaltParameter) rhs;
			return m_qccId.compareTo(r.m_qccId);
		}
		throw new IllegalArgumentException("invalid rhs>" + rhs + "<");
	}

	@Override
	public CobaltDimensionName dimensionName() {
		return CobaltDimensionName.Parameter;
	}

	@Override
	public CobaltDimensionSet dimensionSet() {
		return CobaltDimensionSet.Parameter;
	}

	public boolean equals(CobaltParameter rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return m_qccId.equals(rhs.m_qccId);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof CobaltParameter)) return false;
		return equals((CobaltParameter) o);
	}

	@Override
	public int hashCode() {
		return m_qccId.hashCode();
	}

	public String qccId() {
		return m_qccId;
	}

	@Override
	public void saveTo(JsonObject dst) {
		dst.putString(PName_id, m_qccId);
		dst.putString(PName_unit, m_zccUnit);
		dst.putString(PName_description, m_zDescription);
	}

	@Override
	public String show() {
		final StringBuilder sb = new StringBuilder();
		sb.append(CobaltNCube.ShowCL);
		sb.append(m_qccId);
		if (m_zccUnit.length() > 0) {
			sb.append(';').append(m_zccUnit);
		}
		sb.append(CobaltNCube.ShowCR);
		return sb.toString();
	}

	@Override
	public String toString() {
		return show();
	}

	public String zccUnit() {
		return m_zccUnit;
	}

	public static CobaltParameter newInstance(JsonObject src)
			throws JsonSchemaException {
		if (src == null) throw new IllegalArgumentException("object is null");
		final String qccId = src.accessor(PName_id).datumQtwString();
		final String zccUnit = src.accessor(PName_unit).datumZtwString();
		final String zDescription = src.accessor(PName_description).datumZtwString();
		return new CobaltParameter(qccId, zccUnit, zDescription);
	}

	public CobaltParameter(String qccId, String zccUnit, String zDescription) {
		if (qccId == null || qccId.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (zccUnit == null) throw new IllegalArgumentException("object is null");
		if (zDescription == null) throw new IllegalArgumentException("object is null");
		this.m_qccId = qccId;
		this.m_zccUnit = zccUnit;
		this.m_zDescription = zDescription;
	}
	private final String m_qccId;
	private final String m_zccUnit;
	private final String m_zDescription;
}
