/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.DecimalMask;
import com.metservice.argon.Real;
import com.metservice.argon.json.IJsonNativeNumber;
import com.metservice.argon.json.IJsonNumberDouble;
import com.metservice.argon.json.JsonNumberDouble;
import com.metservice.argon.json.JsonType;

/**
 * 
 * @author roach
 */
public final class EsPrimitiveNumberReal extends EsPrimitiveNumber implements IJsonNumberDouble {

	@Override
	public EsPrimitiveNumber abs() {
		final Real absValue = m_value.unaryAbsolute();
		if (absValue == m_value) return this;
		return new EsPrimitiveNumberReal(absValue);
	}

	@Override
	public double doubleValue() {
		return m_value.central();
	}

	@Override
	public JsonType getJsonType() {
		return JsonType.TNumberDouble;
	}

	@Override
	public int intVerified() {
		return UNeon.intVerified(m_value.central());
	}

	@Override
	public boolean isLessThan(EsPrimitiveNumber rn) {
		final SubType rt = rn.subType();
		if (rt == SubType.REAL) return m_value.relationLessThan(rn.realValue());
		if (rt == SubType.TIME || rt == SubType.NAN) return true;
		return doubleValue() < rn.doubleValue();
	}

	@Override
	public boolean isNaN() {
		return m_value.isNaN();
	}

	@Override
	public double jsonDatum() {
		return m_value.central();
	}

	@Override
	public long longValue() {
		return m_value.convertToLong();
	}

	@Override
	public EsPrimitiveNumber negated() {
		return new EsPrimitiveNumberReal(m_value.unaryNegate());
	}

	@Override
	public IJsonNativeNumber newJsonNativeNumber() {
		return new JsonNumberDouble(doubleValue());
	}

	@Override
	public Real realValue() {
		return m_value;
	}

	@Override
	public boolean sameNumberValue(EsPrimitiveNumber rn) {
		final SubType rt = rn.subType();
		if (rt == SubType.REAL) return m_value.equals(rn.realValue());
		if (rt == SubType.TIME || rt == SubType.NAN) return false;
		return doubleValue() == rn.doubleValue();
	}

	public String show(int depth) {
		return m_value.toString();
	}

	@Override
	public SubType subType() {
		return SubType.REAL;
	}

	@Override
	public String toCanonicalString() {
		return m_value.toString();
	}

	@Override
	public String toCanonicalString(DecimalMask decimalMask) {
		return decimalMask.format(m_value);
	}

	@Override
	public int toHash() {
		return m_value.hashCode();
	}

	public EsPrimitiveNumberReal(Real value) {
		m_value = value;
	}

	private final Real m_value;
}
