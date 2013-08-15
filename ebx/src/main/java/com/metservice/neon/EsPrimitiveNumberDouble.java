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
public final class EsPrimitiveNumberDouble extends EsPrimitiveNumber implements IJsonNumberDouble {

	public static final EsPrimitiveNumberDouble ZERO = new EsPrimitiveNumberDouble(0.0);
	public static final EsPrimitiveNumberDouble ONE = new EsPrimitiveNumberDouble(1.0);
	public static final EsPrimitiveNumberDouble MINUSONE = new EsPrimitiveNumberDouble(-1.0);

	@Override
	public EsPrimitiveNumber abs() {
		return (m_value <= 0.0) ? new EsPrimitiveNumberDouble(0.0 - m_value) : this;
	}

	@Override
	public double doubleValue() {
		return m_value;
	}

	public float floatVerified() {
		return UNeon.floatVerified(m_value);
	}

	@Override
	public JsonType getJsonType() {
		return JsonType.TNumberDouble;
	}

	@Override
	public int intVerified() {
		return UNeon.intVerified(m_value);
	}

	@Override
	public boolean isLessThan(EsPrimitiveNumber rn) {
		final SubType rt = rn.subType();
		if (rt == SubType.TIME || rt == SubType.NAN) return true;
		return doubleValue() < rn.doubleValue();
	}

	@Override
	public boolean isNaN() {
		return Double.isNaN(m_value);
	}

	@Override
	public double jsonDatum() {
		return m_value;
	}

	@Override
	public long longValue() {
		return (long) m_value;
	}

	@Override
	public EsPrimitiveNumber negated() {
		return new EsPrimitiveNumberDouble(-m_value);
	}

	@Override
	public IJsonNativeNumber newJsonNativeNumber() {
		return new JsonNumberDouble(m_value);
	}

	@Override
	public com.metservice.argon.Real realValue() {
		return Real.newInstance(m_value);
	}

	@Override
	public boolean sameNumberValue(EsPrimitiveNumber rn) {
		final SubType rt = rn.subType();
		if (rt == SubType.TIME || rt == SubType.NAN) return false;
		return doubleValue() == rn.doubleValue();
	}

	public String show(int depth) {
		return Double.toString(m_value);
	}

	@Override
	public SubType subType() {
		return SubType.DOUBLE;
	}

	@Override
	public String toCanonicalString() {
		return Double.toString(m_value);
	}

	@Override
	public String toCanonicalString(DecimalMask decimalMask) {
		return decimalMask.format(m_value);
	}

	@Override
	public int toHash() {
		final long bits = Double.doubleToLongBits(m_value);
		return (int) (bits ^ (bits >>> 32));
	}

	public EsPrimitiveNumberDouble(double value) {
		m_value = value;
	}

	private final double m_value;
}
