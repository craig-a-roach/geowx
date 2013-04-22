/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import com.metservice.argon.HashCoder;
import com.metservice.argon.text.ArgonNumber;

/**
 * @author roach
 */
public class KryptonCentre {

	public boolean equals(KryptonCentre rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return m_idCentre == rhs.m_idCentre && m_idSubCentre == rhs.m_idSubCentre;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof KryptonCentre)) return false;
		return equals((KryptonCentre) o);
	}

	@Override
	public int hashCode() {
		int h = HashCoder.INIT;
		h = HashCoder.and(h, m_idCentre);
		h = HashCoder.and(h, m_idSubCentre);
		return h;
	}

	public int idCentre() {
		return m_idCentre;
	}

	public int idSubCentre() {
		return m_idSubCentre;
	}

	public boolean isSubCentre() {
		return m_idSubCentre > 0;
	}

	public String oqccSubCentre() {
		return m_oqccSubCentre;
	}

	public String qccCentre() {
		return m_qccCentre;
	}

	public String qccFullName() {
		final StringBuilder sb = new StringBuilder();
		sb.append(m_qccCentre);
		if (m_idSubCentre > 0) {
			sb.append("- ");
			if (m_oqccSubCentre == null) {
				sb.append(ArgonNumber.intToDec3(m_idSubCentre));
			} else {
				sb.append(m_oqccSubCentre);
			}
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(ArgonNumber.intToDec3(m_idCentre));
		if (m_idSubCentre > 0) {
			sb.append(".");
			sb.append(ArgonNumber.intToDec3(m_idSubCentre));
		}
		sb.append("-");
		sb.append(qccFullName());
		return sb.toString();
	}

	public KryptonCentre(int idCentre, int idSubCentre, String qccCentre, String oqccSubCentre) {
		if (idCentre < 0) throw new IllegalArgumentException("invalid idCentre>" + idCentre + "<");
		if (idSubCentre < 0) throw new IllegalArgumentException("invalid idSubCentre>" + idSubCentre + "<");
		if (qccCentre == null || qccCentre.length() == 0) throw new IllegalArgumentException("string is null or empty");
		m_idCentre = idCentre;
		m_idSubCentre = idSubCentre;
		m_qccCentre = qccCentre;
		m_oqccSubCentre = oqccSubCentre;
	}

	private final int m_idCentre;
	private final int m_idSubCentre;
	private final String m_qccCentre;
	private final String m_oqccSubCentre;
}
