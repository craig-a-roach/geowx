/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium;

import java.util.Arrays;

import com.metservice.argon.ArgonCompare;
import com.metservice.argon.Ds;

/**
 * @author roach
 */
public class GalliumTopologyLevel implements Comparable<GalliumTopologyLevel> {

	private static final int InitCap = 8;
	private static final int CapMin = 4;
	private static final int ExpandMax = 500;

	private static GalliumBoundingBoxF createBounds(GalliumPoly[] zptPolyImmutable) {
		final int polyCount = zptPolyImmutable.length;
		if (polyCount == 0) return null;
		final GalliumBoundingBoxF.BuilderF builder = GalliumBoundingBoxF.newBuilderF();
		builder.init(zptPolyImmutable[0].bounds());
		for (int i = 1; i < polyCount; i++) {
			builder.add(zptPolyImmutable[i].bounds());
		}
		return GalliumBoundingBoxF.newInstance(builder);
	}

	public static Builder newBuilder(float threshold) {
		return newBuilder(threshold, InitCap);
	}

	public static Builder newBuilder(float threshold, int initCap) {
		return new Builder(threshold, Math.max(CapMin, initCap));
	}

	public static GalliumTopologyLevel newInstance(Builder builder) {
		if (builder == null) throw new IllegalArgumentException("object is null");
		final float threshold = builder.threshold();
		final GalliumPoly[] zptPolyAsc = builder.zptPolyAscImmutable();
		final GalliumBoundingBoxF oBounds = createBounds(zptPolyAsc);
		return new GalliumTopologyLevel(threshold, zptPolyAsc, oBounds);
	}

	@Override
	public int compareTo(GalliumTopologyLevel rhs) {
		return ArgonCompare.fwd(m_threshold, rhs.m_threshold);
	}

	public String describePoints() {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < m_zptPolyAsc.length; i++) {
			final GalliumPoly poly = m_zptPolyAsc[i];
			if (i > 0) {
				sb.append("\n");
			}
			sb.append(poly.describePoints());
		}
		return sb.toString();
	}

	public GalliumBoundingBoxF getBounds() {
		return m_oBounds;
	}

	public float threshold() {
		return m_threshold;
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("threshold", m_threshold);
		ds.a("bounds", m_oBounds);
		ds.a("polygonCount", m_zptPolyAsc.length);
		return ds.s();
	}

	public GalliumPoly[] zptPolyAsc() {
		return m_zptPolyAsc;
	}

	private GalliumTopologyLevel(float threshold, GalliumPoly[] zptPolyAsc, GalliumBoundingBoxF oBounds) {
		m_threshold = threshold;
		m_zptPolyAsc = zptPolyAsc;
		m_oBounds = oBounds;
	}
	private final float m_threshold;
	private final GalliumPoly[] m_zptPolyAsc;
	private final GalliumBoundingBoxF m_oBounds;

	public static class Builder {

		private void ensure(int reqdGrowth) {
			final int neoCount = m_count + reqdGrowth;
			final int exCap = m_polys.length;
			if (neoCount <= exCap) return;
			final int estGrowth = (exCap * 3 / 2) - exCap;
			final int extendBy = Math.min(ExpandMax, estGrowth);
			final int neoCap = Math.max(neoCount, exCap + extendBy);
			m_polys = Arrays.copyOf(m_polys, neoCap);
		}

		public void add(GalliumPoly poly) {
			if (poly == null) throw new IllegalArgumentException("object is null");
			ensure(1);
			m_polys[m_count] = poly;
			m_count++;
		}

		public float threshold() {
			return m_threshold;
		}

		public GalliumPoly[] zptPolyAscImmutable() {
			final GalliumPoly[] zptAsc = Arrays.copyOfRange(m_polys, 0, m_count);
			Arrays.sort(zptAsc);
			return zptAsc;
		}

		private Builder(float threshold, int initCap) {
			m_threshold = threshold;
			m_polys = new GalliumPoly[initCap];
		}
		private final float m_threshold;
		private GalliumPoly[] m_polys;
		private int m_count;
	}
}
