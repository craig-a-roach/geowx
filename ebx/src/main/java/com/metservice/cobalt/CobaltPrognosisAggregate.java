/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.cobalt;

import com.metservice.argon.ArgonCompare;
import com.metservice.argon.Elapsed;
import com.metservice.argon.ElapsedFormatter;
import com.metservice.argon.HashCoder;
import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;

/**
 * @author roach
 */
public class CobaltPrognosisAggregate extends AbstractPrognosis {

	public static final String PName_type = "t";
	public static final String PName_rangeFrom = "rf";
	public static final String PName_rangeToex = "rt";

	public static CobaltPrognosisAggregate newAccumulation(int ssecsFrom, int ssecsToex) {
		return new CobaltPrognosisAggregate(CobaltAggregateType.Accumulation, ssecsFrom, ssecsToex);
	}

	public static CobaltPrognosisAggregate newAverage(int ssecsFrom, int ssecsToex) {
		return new CobaltPrognosisAggregate(CobaltAggregateType.Average, ssecsFrom, ssecsToex);
	}

	public static CobaltPrognosisAggregate newBetween(int ssecsFrom, int ssecsToex) {
		return new CobaltPrognosisAggregate(CobaltAggregateType.Between, ssecsFrom, ssecsToex);
	}

	public static CobaltPrognosisAggregate newDifference(int ssecsFrom, int ssecsToex) {
		return new CobaltPrognosisAggregate(CobaltAggregateType.Difference, ssecsFrom, ssecsToex);
	}

	public static CobaltPrognosisAggregate newInstance(JsonObject src)
			throws JsonSchemaException {
		final CobaltAggregateType type = CobaltAggregateType.newInstance(src, PName_type);
		final int ssecsFrom = src.accessor(PName_rangeFrom).datumElapsedSecs();
		final int ssecsToex = src.accessor(PName_rangeToex).datumElapsedSecs();
		return new CobaltPrognosisAggregate(type, ssecsFrom, ssecsToex);
	}

	public static CobaltPrognosisAggregate newMaximum(int ssecsFrom, int ssecsToex) {
		return new CobaltPrognosisAggregate(CobaltAggregateType.Maximum, ssecsFrom, ssecsToex);
	}

	public static CobaltPrognosisAggregate newMinimum(int ssecsFrom, int ssecsToex) {
		return new CobaltPrognosisAggregate(CobaltAggregateType.Minimum, ssecsFrom, ssecsToex);
	}

	private String qInterval() {
		final int ssInterval = m_ssecsToex - m_ssecsFrom;
		return ElapsedFormatter.formatSecsSingleUnit(ssInterval);
	}

	private String qRange() {
		return ElapsedFormatter.formatSecsSingleUnit(m_ssecsToex);
	}

	@Override
	protected int subOrder() {
		return 2;
	}

	@Override
	public void addTo(KmlFeatureText kft) {
		kft.addText(qInterval());
		kft.addText(m_type.qccPrefix());
		kft.addText("+");
		kft.addText(qRange());
	}

	@Override
	public int compareTo(ICobaltProduct rhs) {
		if (rhs instanceof CobaltPrognosisAggregate) {
			final CobaltPrognosisAggregate r = (CobaltPrognosisAggregate) rhs;
			final int c0 = m_type.compareByName(r.m_type);
			if (c0 != 0) return c0;
			final int c1 = ArgonCompare.fwd(m_ssecsFrom, r.m_ssecsFrom);
			if (c1 != 0) return c1;
			final int c2 = ArgonCompare.fwd(m_ssecsToex, r.m_ssecsToex);
			return c2;
		}
		return comparePrognosis(rhs);
	}

	public Elapsed elapsedFrom() {
		return elapsed_ssec(m_ssecsFrom);
	}

	public Elapsed elapsedToex() {
		return elapsed_ssec(m_ssecsToex);
	}

	public Elapsed elapsedValidity() {
		return elapsed_ssec(m_ssecsToex - m_ssecsFrom);
	}

	public boolean equals(CobaltPrognosisAggregate rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return m_type == rhs.m_type && m_ssecsFrom == rhs.m_ssecsFrom && m_ssecsToex == rhs.m_ssecsToex;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof CobaltPrognosisAggregate)) return false;
		return equals((CobaltPrognosisAggregate) o);
	}

	@Override
	public int hashCode() {
		return m_hashCode;
	}

	@Override
	public void saveTo(JsonObject dst) {
		m_type.saveTo(dst, PName_type);
		dst.putElapsedSecs(PName_rangeFrom, m_ssecsFrom);
		dst.putElapsedSecs(PName_rangeToex, m_ssecsToex);
	}

	@Override
	public String show() {
		final StringBuilder sb = new StringBuilder();
		sb.append(CobaltNCube.ShowCL);
		sb.append(qInterval());
		sb.append(m_type.qccPrefix());
		sb.append('+');
		sb.append(qRange());
		sb.append(CobaltNCube.ShowCR);
		return sb.toString();
	}

	@Override
	public String toString() {
		return show();
	}

	private CobaltPrognosisAggregate(CobaltAggregateType type, int ssecsFrom, int ssecsToex) {
		m_type = type;
		m_ssecsFrom = ssecsFrom;
		m_ssecsToex = ssecsToex;
		int hc = HashCoder.INIT;
		hc = HashCoder.and(hc, m_type);
		hc = HashCoder.and(hc, m_ssecsFrom);
		hc = HashCoder.and(hc, m_ssecsToex);
		m_hashCode = hc;
	}
	private final CobaltAggregateType m_type;
	private final int m_ssecsFrom;
	private final int m_ssecsToex;
	private final int m_hashCode;
}
