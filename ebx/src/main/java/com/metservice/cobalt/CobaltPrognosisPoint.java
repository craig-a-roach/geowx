/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.cobalt;

import com.metservice.argon.ArgonCompare;
import com.metservice.argon.Elapsed;
import com.metservice.argon.ElapsedFormatter;
import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;

/**
 * @author roach
 */
public class CobaltPrognosisPoint extends AbstractPrognosis {

	public static final String PName_range = "r";

	public static final CobaltPrognosisPoint Zero = new CobaltPrognosisPoint(0);

	public static CobaltPrognosisPoint newAt(Elapsed range) {
		if (range == null) throw new IllegalArgumentException("object is null");
		return newAt(range.intSecsSigned());
	}

	public static CobaltPrognosisPoint newAt(int ssecsRange) {
		if (ssecsRange == 0) return Zero;
		return new CobaltPrognosisPoint(ssecsRange);
	}

	public static CobaltPrognosisPoint newInstance(JsonObject src)
			throws JsonSchemaException {
		return newAt(src.accessor(PName_range).datumElapsedSecs());
	}

	@Override
	protected int subOrder() {
		return 1;
	}

	@Override
	public void addTo(KmlFeatureText kft) {
		kft.addText(qRange());
	}

	@Override
	public int compareTo(ICobaltProduct rhs) {
		if (rhs instanceof CobaltPrognosisPoint) {
			final CobaltPrognosisPoint r = (CobaltPrognosisPoint) rhs;
			final int c0 = ArgonCompare.fwd(m_ssecsRange, r.m_ssecsRange);
			return c0;
		}
		return comparePrognosis(rhs);
	}

	public Elapsed elapsedRange() {
		return elapsed_ssec(m_ssecsRange);
	}

	public boolean equals(CobaltPrognosisPoint rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return m_ssecsRange == rhs.m_ssecsRange;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof CobaltPrognosisPoint)) return false;
		return equals((CobaltPrognosisPoint) o);
	}

	@Override
	public int hashCode() {
		return m_ssecsRange;
	}

	public String qRange() {
		return ElapsedFormatter.formatSecsSingleUnit(m_ssecsRange);
	}

	@Override
	public void saveTo(JsonObject dst) {
		dst.putElapsedSecs(PName_range, m_ssecsRange);
	}

	@Override
	public String show() {
		return qRange();
	}

	@Override
	public String toString() {
		return show();
	}

	private CobaltPrognosisPoint(int ssecsRange) {
		m_ssecsRange = ssecsRange;
	}

	private final int m_ssecsRange;
}
