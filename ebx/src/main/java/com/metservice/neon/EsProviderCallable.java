/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author roach
 */
public abstract class EsProviderCallable implements IEsCallable, Comparable<EsProviderCallable> {

	protected static final String[] NOARGS = new String[0];

	protected abstract IEsOperand eval(EsExecutionContext ecx)
			throws InterruptedException;

	public final IEsOperand call(EsExecutionContext ecx)
			throws InterruptedException {
		ecx.populateVariableObject(m_zlFormalParameterNames, null, null);
		final IEsOperand oResult = eval(ecx);
		return oResult == null ? EsPrimitiveUndefined.Instance : oResult;
	}

	// Canonical
	public int compareTo(EsProviderCallable o) {
		return m_qccName.compareTo(o.m_qccName);
	}

	public boolean equals(EsProviderCallable r) {
		if (r == this) return true;
		if (r == null) return false;
		return m_qccName.equals(r.m_qccName);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof EsProviderCallable)) return false;
		return equals((EsProviderCallable) o);
	}

	public final EsSource getSource() {
		return null;
	}

	@Override
	public int hashCode() {
		return m_qccName.hashCode();
	}

	public final boolean isDeclared() {
		return true;
	}

	public final boolean isIntrinsic() {
		return true;
	}

	public final String oqccName() {
		return m_qccName;
	}

	public String qccName() {
		return m_qccName;
	}

	public final int requiredArgumentCount() {
		return m_requiredArgumentCount;
	}

	public final List<String> zlFormalParameterNames() {
		return m_zlFormalParameterNames;
	}

	public final Map<String, IEsCallable> zmCallables() {
		return Collections.emptyMap();
	}

	public final Set<String> zsVariableNames() {
		return Collections.emptySet();
	}

	protected static final boolean calledAsFunction(EsExecutionContext ecx) {
		return (ecx.thisObject() == ecx.global());
	}

	protected EsProviderCallable(String qccName, String[] zptFormalParameterNames, int requiredArgumentCount) {
		if (qccName == null || qccName.length() == 0) throw new IllegalArgumentException("qccName is empty");
		if (zptFormalParameterNames == null) throw new IllegalArgumentException("zptFormalParameterNames is null");
		m_qccName = qccName;
		m_zlFormalParameterNames = new ArrayList<String>(zptFormalParameterNames.length);
		for (int i = 0; i < zptFormalParameterNames.length; i++) {
			m_zlFormalParameterNames.add(zptFormalParameterNames[i]);
		}
		m_requiredArgumentCount = requiredArgumentCount;
	}
	private final String m_qccName;
	private final List<String> m_zlFormalParameterNames;
	private final int m_requiredArgumentCount;
}
