/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.edge;

/**
 * @author roach
 */
public class BzeStrikeCell {

	public float height() {
		return m_eps;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("xL=").append(m_xL);
		sb.append(", yB=").append(m_yB);
		sb.append(", e=").append(m_eps);
		return sb.toString();
	}

	public float width() {
		return m_eps;
	}

	public float xLeft() {
		return m_xL;
	}

	public float yTop() {
		return m_yB + m_eps;
	}

	public BzeStrikeCell(float x, float y, float eps) {
		m_xL = x;
		m_yB = y;
		m_eps = eps;
	}
	private final float m_xL;
	private final float m_yB;
	private final float m_eps;
}
