/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.cobalt;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.metservice.argon.HashCoder;
import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;

/**
 * @author roach
 */
public class CobaltNCube implements Comparable<CobaltNCube>, Iterable<CobaltRecord> {

	public static final int ShowIndent = 2;
	public static final String ShowCL = "[";
	public static final String ShowCR = "]";

	KmlFeatureDescription newKmlFeatureDescription(boolean senseCol) {
		final KmlFeatureDescription neo = new KmlFeatureDescription();
		final int count = m_xptShapeAsc.length;
		for (int i = 0; i < count; i++) {
			m_xptShapeAsc[i].addShapeTo(neo, senseCol);
		}
		return neo;
	}

	@Override
	public int compareTo(CobaltNCube rhs) {
		final int clhs = m_xptShapeAsc.length;
		final int crhs = rhs.m_xptShapeAsc.length;
		if (clhs < crhs) return -1;
		if (clhs > crhs) return +1;
		for (int i = 0; i < clhs; i++) {
			final CobaltSequence sqlhs = m_xptShapeAsc[i];
			final CobaltSequence sqrhs = rhs.m_xptShapeAsc[i];
			final int cmp = sqlhs.compareTo(sqrhs);
			if (cmp != 0) return cmp;
		}
		return 0;
	}

	public boolean equals(CobaltNCube rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		final int clhs = m_xptShapeAsc.length;
		final int crhs = rhs.m_xptShapeAsc.length;
		if (clhs != crhs) return false;
		for (int i = 0; i < clhs; i++) {
			final CobaltSequence sqlhs = m_xptShapeAsc[i];
			final CobaltSequence sqrhs = rhs.m_xptShapeAsc[i];
			if (!sqlhs.equals(sqrhs)) return false;
		}
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof CobaltNCube)) return false;
		return equals((CobaltNCube) o);
	}

	@Override
	public int hashCode() {
		return hc(m_xptShapeAsc);
	}

	@Override
	public Iterator<CobaltRecord> iterator() {
		return new NCubeIterator(this);
	}

	public JsonObject newJsonObject() {
		return JsonCodec.newJson(this);
	}

	public int recordCount() {
		return m_recordCount;
	}

	public String show() {
		final int count = m_xptShapeAsc.length;
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < count; i++) {
			final String s = m_xptShapeAsc[i].showShape();
			if (i > 0) {
				sb.append("UNION\n");
			}
			sb.append(s);
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return show();
	}

	public CobaltSequence[] xptShapeAsc() {
		return m_xptShapeAsc;
	}

	private static int hc(CobaltSequence[] xptSequenceAsc) {
		int hc = HashCoder.INIT;
		for (int i = 0; i < xptSequenceAsc.length; i++) {
			hc = HashCoder.and(hc, xptSequenceAsc[i]);
		}
		return hc;
	}

	private static int recordCount(CobaltSequence[] xptShapeAsc) {
		int recordCount = 0;
		for (int i = 0; i < xptShapeAsc.length; i++) {
			recordCount += xptShapeAsc[i].dimensionDepth();
		}
		return recordCount;
	}

	public static CobaltNCube newInstance(CobaltSequence shape) {
		if (shape == null) throw new IllegalArgumentException("object is null");
		return new CobaltNCube(new CobaltSequence[] { shape });
	}

	public static CobaltNCube newInstance(JsonObject src)
			throws JsonSchemaException {
		if (src == null) throw new IllegalArgumentException("object is null");
		return JsonCodec.newNCube(src);
	}

	public static CobaltNCube newInstance(List<CobaltSequence> xlShapes) {
		if (xlShapes == null) throw new IllegalArgumentException("object is null");
		final int shapeCount = xlShapes.size();
		if (shapeCount == 0) throw new IllegalArgumentException("no shapes");
		final CobaltSequence[] xptShapeAsc = xlShapes.toArray(new CobaltSequence[shapeCount]);
		Arrays.sort(xptShapeAsc);
		return new CobaltNCube(xptShapeAsc);
	}

	private CobaltNCube(CobaltSequence[] xptShapeAsc) {
		assert xptShapeAsc != null && xptShapeAsc.length > 0;
		m_xptShapeAsc = xptShapeAsc;
		m_recordCount = recordCount(xptShapeAsc);
	}
	private final CobaltSequence[] m_xptShapeAsc;
	private final int m_recordCount;
}
