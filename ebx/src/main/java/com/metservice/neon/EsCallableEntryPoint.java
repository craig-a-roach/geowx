/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.Ds;
import com.metservice.argon.json.IJsonNative;
import com.metservice.argon.json.IJsonValue;
import com.metservice.argon.json.JsonArray;

/**
 * @author roach
 */
public class EsCallableEntryPoint {

	private static final Object[] ZARGS = new Object[0];

	public EsList newArgumentList(EsExecutionContext ecx) {
		final EsList neo = new EsList();
		final int fixedCount = m_ztFixedArguments.length;
		for (int i = 0; i < fixedCount; i++) {
			final IEsOperand es = newOperand(ecx, m_ztFixedArguments[i]);
			neo.add(es);
		}
		if (m_oVariableArguments == null) return neo;
		if (m_oVariableArguments instanceof JsonArray) {
			addVariableArguments(ecx, neo, (JsonArray) m_oVariableArguments);
			return neo;
		}
		if (m_oVariableArguments instanceof Object[]) {
			addVariableArguments(ecx, neo, (Object[]) m_oVariableArguments);
			return neo;
		}
		final String cn = m_oVariableArguments.getClass().getName();
		throw new UnsupportedOperationException("No support for variable arguments collection " + cn);
	}

	public String qtwFunctionName() {
		return m_qtwFunctionName;
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("functionName", m_qtwFunctionName);
		ds.a("ztFixedArguments", m_ztFixedArguments);
		ds.a("oVariableArguments", m_oVariableArguments);
		return ds.s();
	}

	private static void addVariableArguments(EsExecutionContext ecx, EsList dst, JsonArray varArgs) {
		assert dst != null;
		assert varArgs != null;
		final int vcount = varArgs.size();
		for (int i = 0; i < vcount; i++) {
			final IJsonNative jsonNative = varArgs.get(i);
			final IEsOperand esArg = JsonTranscoder.newOperand(ecx, jsonNative, false);
			dst.add(esArg);
		}
	}

	private static void addVariableArguments(EsExecutionContext ecx, EsList dst, Object[] varArgs) {
		assert dst != null;
		assert varArgs != null;
		final int vcount = varArgs.length;
		for (int i = 0; i < vcount; i++) {
			final Object oarg = varArgs[i];
			final IEsOperand esArg = newOperand(ecx, oarg);
			dst.add(esArg);
		}
	}

	private static IEsOperand newOperand(EsExecutionContext ecx, Object osrc) {
		if (osrc == null) return EsPrimitiveNull.Instance;
		if (osrc instanceof IEsOperand) return (IEsOperand) osrc;
		if (osrc instanceof IJsonValue) return JsonTranscoder.newOperand(ecx, (IJsonValue) osrc, false);
		return EsPrimitiveString.newInstance(osrc.toString());
	}

	public static EsCallableEntryPoint newFixed(String qtwFunctionName, Object[] oztFixedArguments) {
		final Object[] ztFixedArguments = oztFixedArguments == null ? ZARGS : oztFixedArguments;
		return new EsCallableEntryPoint(qtwFunctionName, ztFixedArguments, null);
	}

	public static EsCallableEntryPoint newInstance(String qtwFunctionName, Object[] oztFixedArguments, Object oVariableArguments) {
		final Object[] ztFixedArguments = oztFixedArguments == null ? ZARGS : oztFixedArguments;
		return new EsCallableEntryPoint(qtwFunctionName, ztFixedArguments, oVariableArguments);
	}

	public static EsCallableEntryPoint newUni(String qtwFunctionName, Object oArgument) {
		final Object[] ztFixed = { oArgument };
		return new EsCallableEntryPoint(qtwFunctionName, ztFixed, null);
	}

	public static EsCallableEntryPoint newVoid(String qtwFunctionName) {
		return new EsCallableEntryPoint(qtwFunctionName, ZARGS, null);
	}

	private EsCallableEntryPoint(String qtwFunctionName, Object[] ztFixedArguments, Object oVariableArguments) {
		if (qtwFunctionName == null || qtwFunctionName.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		if (ztFixedArguments == null) throw new IllegalArgumentException("object is null");
		m_qtwFunctionName = qtwFunctionName;
		m_ztFixedArguments = ztFixedArguments;
		m_oVariableArguments = oVariableArguments;
	}

	private final String m_qtwFunctionName;
	private final Object[] m_ztFixedArguments;
	private final Object m_oVariableArguments;
}
