/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.metservice.argon.Ds;
import com.metservice.cobalt.CobaltAnalysis;
import com.metservice.cobalt.CobaltMember;
import com.metservice.cobalt.CobaltParameter;
import com.metservice.cobalt.ICobaltPrognosis;
import com.metservice.cobalt.ICobaltSurface;

/**
 * @author roach
 */
public class KryptonAnalysisPlacePrognosisTable implements IKryptonEditionTable<KryptonAnalysisPlacePrognosisTable> {

	public Set<KryptonPlaceEdition> newSet() {
		final HashSet<KryptonPlaceEdition> dst = new HashSet<>(m_count);
		for (final AnalysisPlacePrognosis v : m_analysisMap.values()) {
			v.save(dst);
		}
		return dst;
	}

	@Override
	public KryptonAnalysisPlacePrognosisTable newSum(KryptonAnalysisPlacePrognosisTable oRhs) {
		if (oRhs == null) return this;
		final KryptonAnalysisPlacePrognosisTable sum = new KryptonAnalysisPlacePrognosisTable();
		final Set<KryptonPlaceEdition> lhsSet = newSet();
		final Set<KryptonPlaceEdition> rhsSet = oRhs.newSet();
		for (final KryptonPlaceEdition ed : lhsSet) {
			sum.put(ed);
		}
		for (final KryptonPlaceEdition ed : rhsSet) {
			sum.put(ed);
		}
		return sum;
	}

	public void put(KryptonPlaceEdition ed) {
		if (ed == null) throw new IllegalArgumentException("object is null");
		AnalysisPlacePrognosis vEx = m_analysisMap.get(ed.analysis);
		if (vEx == null) {
			vEx = new AnalysisPlacePrognosis();
			m_analysisMap.put(ed.analysis, vEx);
		}
		if (vEx.put(ed)) {
			m_count++;
		}
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("editionCount", m_count);
		ds.a("analyses", m_analysisMap);
		return ds.s();
	}

	public KryptonAnalysisPlacePrognosisTable() {
	}
	private final Map<CobaltAnalysis, AnalysisPlacePrognosis> m_analysisMap = new HashMap<>();

	private int m_count;

	public static class AnalysisPlacePrognosis {

		public boolean put(KryptonPlaceEdition ed) {
			assert ed != null;
			PlacePrognosis vEx = m_placeMap.get(ed.placeId);
			if (vEx == null) {
				vEx = new PlacePrognosis();
				m_placeMap.put(ed.placeId, vEx);
			}
			return vEx.put(ed);
		}

		public void save(Set<KryptonPlaceEdition> dst) {
			for (final PlacePrognosis v : m_placeMap.values()) {
				v.save(dst);
			}
		}

		@Override
		public String toString() {
			final Ds ds = Ds.o("Analyses");
			ds.a("places", m_placeMap);
			return ds.s();
		}

		AnalysisPlacePrognosis() {
		}
		private final Map<KryptonPlaceId, PlacePrognosis> m_placeMap = new HashMap<>();
	}

	public static class ParameterSurface {

		public boolean put(KryptonPlaceEdition ed) {
			assert ed != null;
			Surface vEx = m_surfaceMap.get(ed.surface);
			if (vEx == null) {
				vEx = new Surface();
				m_surfaceMap.put(ed.surface, vEx);
			}
			return vEx.put(ed);
		}

		public void save(Set<KryptonPlaceEdition> dst) {
			for (final Surface v : m_surfaceMap.values()) {
				v.save(dst);
			}
		}

		@Override
		public String toString() {
			final Ds ds = Ds.o("Parameter");
			ds.a("surfaces", m_surfaceMap);
			return ds.s();
		}

		public ParameterSurface() {
		}
		private final Map<ICobaltSurface, Surface> m_surfaceMap = new HashMap<>();
	}

	public static class PlacePrognosis {

		public boolean put(KryptonPlaceEdition ed) {
			assert ed != null;
			Prognosis vEx = m_prognosisMap.get(ed.prognosis);
			if (vEx == null) {
				vEx = new Prognosis();
				m_prognosisMap.put(ed.prognosis, vEx);
			}
			return vEx.put(ed);
		}

		public void save(Set<KryptonPlaceEdition> dst) {
			for (final Prognosis v : m_prognosisMap.values()) {
				v.save(dst);
			}
		}

		@Override
		public String toString() {
			final Ds ds = Ds.o("Place");
			ds.a("prognoses", m_prognosisMap);
			return ds.s();
		}

		PlacePrognosis() {
		}
		private final Map<ICobaltPrognosis, Prognosis> m_prognosisMap = new HashMap<>();
	}

	public static class Prognosis {

		public boolean put(KryptonPlaceEdition ed) {
			assert ed != null;
			ParameterSurface vEx = m_parameterMap.get(ed.parameter);
			if (vEx == null) {
				vEx = new ParameterSurface();
				m_parameterMap.put(ed.parameter, vEx);
			}
			return vEx.put(ed);
		}

		public void save(Set<KryptonPlaceEdition> dst) {
			for (final ParameterSurface v : m_parameterMap.values()) {
				v.save(dst);
			}
		}

		@Override
		public String toString() {
			final Ds ds = Ds.o("Prognosis");
			ds.a("parameters", m_parameterMap);
			return ds.s();
		}
		private final Map<CobaltParameter, ParameterSurface> m_parameterMap = new HashMap<>();
	}

	public static class Surface {

		public boolean put(KryptonPlaceEdition ed) {
			assert ed != null;
			return (m_memberMap.put(ed.member, ed) == null);
		}

		public void save(Set<KryptonPlaceEdition> dst) {
			dst.addAll(m_memberMap.values());
		}

		@Override
		public String toString() {
			final List<CobaltMember> membersAsc = new ArrayList<>(m_memberMap.keySet());
			Collections.sort(membersAsc);
			float ensSum = 0.0f;
			float ensLo = 0.0f;
			float ensHi = 0.0f;
			int ensCount = 0;
			for (final CobaltMember cobaltMember : membersAsc) {
				final KryptonPlaceEdition oEd = m_memberMap.get(cobaltMember);
				if (oEd == null) {
					continue;
				}
				if (!Float.isNaN(oEd.datum)) {
					if (ensCount == 0) {
						ensLo = oEd.datum;
						ensHi = oEd.datum;
					} else {
						ensLo = Math.min(ensLo, oEd.datum);
						ensHi = Math.max(ensHi, oEd.datum);
					}
					ensSum += oEd.datum;
					ensCount++;
				}
			}
			if (ensCount == 0) return "NaN";
			final float ensMean = ensSum / ensCount;
			return "*" + ensCount + " " + ensLo + "<=" + ensMean + "<=" + ensHi;
		}

		Surface() {
		}
		private final Map<CobaltMember, KryptonPlaceEdition> m_memberMap = new HashMap<>();
	}
}
