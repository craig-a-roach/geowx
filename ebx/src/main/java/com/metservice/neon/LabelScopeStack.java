/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.HashMap;
import java.util.Map;

import com.metservice.argon.collection.DynamicArray;

/**
 * 
 * @author roach
 */
class LabelScopeStack {

	private Scope current() {
		final Scope oCurrent = m_scopes.getLast();
		if (oCurrent == null) throw new EsInterpreterException("Label Scope Stack Is Empty");
		return oCurrent;
	}

	public Scope popScope() {
		final Scope oScope = m_scopes.poll();
		if (oScope == null) throw new EsInterpreterException("Label Scope Stack Underflow");
		return oScope;
	}

	public void pushScope() {
		m_scopes.push(new Scope());
	}

	public void put(String qccName, InstructionAddress oAddress) {
		if (qccName == null || qccName.length() == 0) throw new IllegalArgumentException("qccName is empty");
		current().put(qccName, oAddress);
	}

	public void remove(String qccName) {
		if (qccName == null || qccName.length() == 0) throw new IllegalArgumentException("qccName is empty");
		current().remove(qccName);
	}

	public InstructionAddress select(String qccName) {
		if (qccName == null || qccName.length() == 0) throw new IllegalArgumentException("qccName is empty");
		return current().select(qccName);
	}

	public InstructionAddress selectBreak() {
		return select(VmLabel.LABEL_BREAK);
	}

	public InstructionAddress selectContinue() {
		return select(VmLabel.LABEL_CONTINUE);
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		for (int i = m_scopes.count - 1; i >= 0; i--) {
			b.append("\n");
			b.append(m_scopes.array[i]);
		}
		b.append("\nBottom");
		return b.toString();
	}

	public LabelScopeStack() {
		pushScope();
	}

	private final DynamicArray<Scope> m_scopes = new DynamicArray<Scope>() {
		@Override
		public int initialCapacity() {
			return 8;
		}

		@Override
		public Scope[] newArray(int cap) {
			return new Scope[cap];
		}
	};

	private static class Scope {
		public void put(String qccName, InstructionAddress oAddress) {
			if (oAddress == null) throw new EsInterpreterException("Unresolved label '" + qccName + "'");

			final InstructionAddress oEx = m_map.put(qccName, oAddress);
			if (oEx != null) throw new EsLabelCodeException("Duplicate Label '" + qccName + "'");
		}

		public void remove(String qccName) {
			m_map.remove(qccName);
		}

		public InstructionAddress select(String qccName) {
			final InstructionAddress oAddress = m_map.get(qccName);
			if (oAddress == null) throw new EsInterpreterException("Unresolved label '" + qccName + "'");
			return oAddress;
		}

		@Override
		public String toString() {
			if (m_map.isEmpty()) return "NoLabels";
			final StringBuilder b = new StringBuilder();
			for (final Map.Entry<String, InstructionAddress> entry : m_map.entrySet()) {
				if (b.length() > 0) {
					b.append(",");
				}
				b.append(entry.getKey());
				b.append('=');
				b.append(entry.getValue());
			}
			return b.toString();
		}

		public Scope() {
		}

		private final Map<String, InstructionAddress> m_map = new HashMap<String, InstructionAddress>(16);
	}
}
