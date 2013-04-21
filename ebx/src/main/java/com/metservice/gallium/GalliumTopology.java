/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium;

import java.util.Arrays;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
public class GalliumTopology {

	private static final int InitCap = 8;
	private static final int CapMin = 4;
	private static final int ExpandMax = 100;

	private static GalliumBoundingBoxF createBounds(GalliumTopologyLevel[] zptLevels) {
		GalliumBoundingBoxF oLhs = null;
		for (int i = 0; i < zptLevels.length; i++) {
			oLhs = GalliumBoundingBoxF.createUnion(oLhs, zptLevels[i].getBounds());
		}
		return oLhs;
	}

	public static Builder newBuilder() {
		return newBuilder(InitCap);
	}

	public static Builder newBuilder(int initCap) {
		return new Builder(Math.max(CapMin, initCap));
	}

	public static GalliumTopology newInstance(Builder builder) {
		if (builder == null) throw new IllegalArgumentException("object is null");
		final GalliumTopologyLevel[] zptLevelAsc = builder.zptLevelAscImmutable();
		final GalliumBoundingBoxF oBounds = createBounds(zptLevelAsc);
		return new GalliumTopology(zptLevelAsc, oBounds);
	}

	public String describePoints() {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < m_zptLevelAsc.length; i++) {
			final GalliumTopologyLevel level = m_zptLevelAsc[i];
			if (i > 0) {
				sb.append("\n");
			}
			sb.append(level.threshold()).append(">\n");
			sb.append(level.describePoints());
		}
		return sb.toString();
	}

	public GalliumBoundingBoxF getBounds() {
		return m_oBounds;
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("bounds", m_oBounds);
		ds.a("levels", m_zptLevelAsc);
		return ds.s();
	}

	public GalliumTopologyLevel[] zptLevelAsc() {
		return m_zptLevelAsc;
	}

	private GalliumTopology(GalliumTopologyLevel[] zptLevelsAsc, GalliumBoundingBoxF oBounds) {
		m_zptLevelAsc = zptLevelsAsc;
		m_oBounds = oBounds;
	}
	private final GalliumTopologyLevel[] m_zptLevelAsc;
	private final GalliumBoundingBoxF m_oBounds;

	public static class Builder {

		private void ensure(int reqdGrowth) {
			final int neoCount = m_count + reqdGrowth;
			final int exCap = m_levels.length;
			if (neoCount <= exCap) return;
			final int estGrowth = (exCap * 3 / 2) - exCap;
			final int extendBy = Math.min(ExpandMax, estGrowth);
			final int neoCap = Math.max(neoCount, exCap + extendBy);
			m_levels = Arrays.copyOf(m_levels, neoCap);
		}

		public void add(GalliumTopologyLevel level) {
			if (level == null) throw new IllegalArgumentException("object is null");
			ensure(1);
			m_levels[m_count] = level;
			m_count++;
		}

		public GalliumTopologyLevel[] zptLevelAscImmutable() {
			final GalliumTopologyLevel[] zptAsc = Arrays.copyOfRange(m_levels, 0, m_count);
			Arrays.sort(zptAsc);
			return zptAsc;
		}

		private Builder(int initCap) {
			m_levels = new GalliumTopologyLevel[initCap];
		}
		private GalliumTopologyLevel[] m_levels;
		private int m_count;
	}
}
