/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.esri;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.metservice.argon.Ds;
import com.metservice.argon.text.ArgonJoiner;

/**
 * @author roach
 */
class ParameterMap implements IWktList {

	public static ParameterMap newDefault(Object... zpt) {
		final ParameterMap neo = new ParameterMap();
		for (int iName = 0, iValue = 1; iValue < zpt.length; iName += 2, iValue += 2) {
			final Object oDef = zpt[iName];
			final Object oValue = zpt[iValue];
			if (oDef instanceof ParameterDefinition && oValue instanceof Double) {
				final ParameterDefinition def = (ParameterDefinition) oDef;
				final double value = ((Double) oValue).doubleValue();
				neo.add(new ParameterValue(def, value));
			} else {
				final String s = ArgonJoiner.zComma(zpt);
				final String m = "Malformed default parameter bindings at index " + iName + " of [" + s + "]";
				throw new IllegalArgumentException(m);
			}
		}
		return neo;
	}

	public boolean add(ParameterValue neo) {
		if (neo == null) throw new IllegalArgumentException("object is null");
		return m_mapDefinition.put(neo.definition, neo) == null;
	}

	public boolean contains(ParameterDefinition def) {
		if (def == null) throw new IllegalArgumentException("object is null");
		return m_mapDefinition.containsKey(def);
	}

	public ParameterValue find(ParameterDefinition def) {
		if (def == null) throw new IllegalArgumentException("object is null");
		return m_mapDefinition.get(def);
	}

	public ParameterValue find(ParameterDefinition def, ParameterMap defaultValues) {
		if (def == null) throw new IllegalArgumentException("object is null");
		if (defaultValues == null) throw new IllegalArgumentException("object is null");
		final ParameterValue oMatch = find(def);
		return oMatch == null ? defaultValues.find(def) : oMatch;
	}

	@Override
	public List<? extends IWktEmit> listEmit() {
		final ArrayList<ParameterValue> dst = new ArrayList<>(m_mapDefinition.values());
		Collections.sort(dst);
		return dst;
	}

	public ParameterValue select(ParameterDefinition def, ParameterMap defaultValues)
			throws GalliumProjectionException {
		final ParameterValue oMatch = find(def, defaultValues);
		if (oMatch != null) return oMatch;
		final String m = "No explicit or default value defined for parameter '" + def + "'";
		throw new GalliumProjectionException(m);
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("map", m_mapDefinition);
		return ds.s();
	}

	public ParameterMap() {
		m_mapDefinition = new HashMap<>(16);
	}
	private final Map<ParameterDefinition, ParameterValue> m_mapDefinition;
}
