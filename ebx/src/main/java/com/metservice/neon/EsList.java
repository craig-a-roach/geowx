/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.Real;
import com.metservice.argon.json.IJsonNative;
import com.metservice.argon.json.JsonArray;
import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonType;

/**
 * 
 * @author roach
 */
public class EsList implements IEsOperand {

	private void append(IEsOperand operand) {
		if (m_operands == null) {
			m_operands = new IEsOperand[4];
		} else {
			final int cap = m_operands.length;
			final int reqd = m_operandCount + 1;
			if (cap < reqd) {
				final int neoCap = cap * 3 / 2;
				final IEsOperand[] save = m_operands;
				m_operands = new IEsOperand[neoCap];
				System.arraycopy(save, 0, m_operands, 0, m_operandCount);
			}
		}
		m_operands[m_operandCount] = operand;
		m_operandCount++;
	}

	public void add(double value) {
		add(new EsPrimitiveNumberDouble(value));
	}

	public void add(EsExecutionContext ecx, JsonArray oValue) {
		if (oValue == null) {
			addNull();
		} else {
			add(JsonTranscoder.newIntrinsicArray(ecx, oValue, false));
		}
	}

	public void add(EsExecutionContext ecx, JsonObject oValue) {
		if (oValue == null) {
			addNull();
		} else {
			add(JsonTranscoder.newIntrinsicObject(ecx, oValue, false));
		}
	}

	public void add(IEsOperand operand) {
		if (operand == null) throw new IllegalArgumentException("operand is null");
		append(operand);
	}

	public void add(int value) {
		add(EsPrimitiveNumberInteger.newInstance(value));
	}

	public void add(Real oValue) {
		if (oValue == null) {
			addNull();
		} else {
			add(new EsPrimitiveNumberReal(oValue));
		}
	}

	public void add(String ozValue) {
		if (ozValue == null) {
			addNull();
		} else {
			add(EsPrimitiveString.newInstance(ozValue));
		}
	}

	public void addElapsed(long sms) {
		add(EsPrimitiveNumberElapsed.newInstance(sms));
	}

	public void addNull() {
		add(EsPrimitiveNull.Instance);
	}

	public void addTime(long ts) {
		add(new EsPrimitiveNumberTime(ts));
	}

	@Override
	public IJsonNative createJsonNative() {
		return null;
	}

	public EsType esType() {
		return EsType.TList;
	}

	@Override
	public JsonType getJsonType() {
		return null;
	}

	public int length() {
		return m_operandCount;
	}

	public IEsOperand operand(int index) {
		return m_operands[index];
	}

	public IEsOperand popHead() {
		IEsOperand oHead = null;
		if (m_headIndex < m_operandCount) {
			oHead = m_operands[m_headIndex];
			m_headIndex++;
		}
		return oHead;
	}

	public String show(int depth) {
		final StringBuilder b = new StringBuilder();
		if (depth > 0) {
			b.append('[');
			for (int i = m_headIndex; i < m_operandCount; i++) {
				if (i > m_headIndex) {
					b.append(",");
				}
				b.append(m_operands[i].show(depth - 1));
			}
			b.append(']');
		} else {
			b.append("headIndex=").append(m_headIndex);
			b.append(", operandCount=").append(m_operandCount);
		}
		return b.toString();
	}

	public boolean toCanonicalBoolean() {
		throw new EsInterpreterException("Cannot convert List (" + show(1) + ") to a canonical boolean");
	}

	public String toCanonicalString(EsExecutionContext ecx) {
		throw new EsInterpreterException("Cannot convert List (" + show(1) + ") to a canonical string");
	}

	public EsPrimitiveNumber toNumber(EsExecutionContext ecx) {
		throw new EsInterpreterException("Cannot convert List (" + show(1) + ") to a number");
	}

	public EsObject toObject(EsExecutionContext ecx) {
		throw new EsInterpreterException("Cannot convert List (" + show(1) + ") to an object");
	}

	public EsPrimitive toPrimitive(EsExecutionContext ecx, EsType oPreference) {
		throw new EsInterpreterException("Cannot convert List (" + show(1) + ") to a primitive");
	}

	@Override
	public String toString() {
		return show(1);
	}

	public EsList() {
	}
	private IEsOperand[] m_operands;
	private int m_operandCount;
	private int m_headIndex;
}
