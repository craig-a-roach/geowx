/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.ArgonNumber;
import com.metservice.argon.collection.DynamicArray;

/**
 * 
 * @author roach
 */
class ListStack {
	public int depth() {
		return m_operands.count;
	}

	public boolean isEmpty() {
		return m_operands.isEmpty();
	}

	public EsList pop() {
		if (m_operands.isEmpty()) throw new EsInterpreterException("List Stack Underflow");
		return m_operands.pop();
	}

	public void push(EsList operand) {
		m_operands.push(operand);
	}

	@Override
	public String toString() {
		final StringBuffer b = new StringBuffer();
		for (int i = m_operands.count - 1; i >= 0; i--) {
			b.append(ArgonNumber.intToDec2(i));
			b.append(':');
			b.append(m_operands.array[i].show(1));
			b.append('\n');
		}
		b.append("(Bottom)");
		return b.toString();
	}

	public ListStack() {
	}

	private final com.metservice.argon.collection.DynamicArray<EsList> m_operands = new DynamicArray<EsList>() {

		@Override
		public int initialCapacity() {
			return 4;
		}

		@Override
		public EsList[] newArray(int cap) {
			return new EsList[cap];
		}
	};
}
