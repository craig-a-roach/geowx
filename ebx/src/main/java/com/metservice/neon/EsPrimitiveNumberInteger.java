/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.DecimalMask;
import com.metservice.argon.HashCoder;
import com.metservice.argon.Real;
import com.metservice.argon.json.IJsonNativeNumber;
import com.metservice.argon.json.IJsonNumberInteger;
import com.metservice.argon.json.JsonNumberInteger;
import com.metservice.argon.json.JsonType;

/**
 * 
 * @author roach
 */
public final class EsPrimitiveNumberInteger extends EsPrimitiveNumber implements IJsonNumberInteger {

	public static final EsPrimitiveNumberInteger ZERO = new EsPrimitiveNumberInteger(0);
	public static final EsPrimitiveNumberInteger ONE = new EsPrimitiveNumberInteger(1);
	public static final EsPrimitiveNumberInteger MINUSONE = new EsPrimitiveNumberInteger(-1);

	@Override
	public EsPrimitiveNumber abs() {
		return m_value >= 0 ? this : new EsPrimitiveNumberInteger(-m_value);
	}

	@Override
	public double doubleValue() {
		return m_value;
	}

	@Override
	public JsonType getJsonType() {
		return JsonType.TNumberInteger;
	}

	@Override
	public int intVerified() {
		return UNeon.intVerified(m_value);
	}

	@Override
	public boolean isLessThan(EsPrimitiveNumber rn) {
		final SubType rt = rn.subType();
		if (rt == SubType.INTEGER || rt == SubType.ELAPSED) return m_value < rn.longValue();
		if (rt == SubType.TIME || rt == SubType.NAN) return true;
		return doubleValue() < rn.doubleValue();
	}

	@Override
	public boolean isNaN() {
		return false;
	}

	@Override
	public long jsonDatum() {
		return m_value;
	}

	@Override
	public long longValue() {
		return m_value;
	}

	@Override
	public EsPrimitiveNumber negated() {
		return new EsPrimitiveNumberInteger(-m_value);
	}

	@Override
	public IJsonNativeNumber newJsonNativeNumber() {
		return JsonNumberInteger.newInstance(m_value);
	}

	@Override
	public Real realValue() {
		return Real.newInstance(m_value);
	}

	@Override
	public boolean sameNumberValue(EsPrimitiveNumber rn) {
		final SubType rt = rn.subType();
		if (rt == SubType.INTEGER || rt == SubType.ELAPSED) return m_value == rn.longValue();
		if (rt == SubType.TIME || rt == SubType.NAN) return false;
		return doubleValue() == rn.doubleValue();
	}

	public String show(int depth) {
		return Long.toString(m_value);
	}

	@Override
	public SubType subType() {
		return SubType.INTEGER;
	}

	@Override
	public String toCanonicalString() {
		return Long.toString(m_value);
	}

	@Override
	public String toCanonicalString(DecimalMask decimalMask) {
		return decimalMask.format(m_value);
	}

	@Override
	public int toHash() {
		return HashCoder.field(m_value);
	}

	public static EsPrimitiveNumberInteger newInstance(long value) {
		if (value == 0L) return ZERO;
		if (value == 1L) return ONE;
		if (value == -1L) return MINUSONE;
		return new EsPrimitiveNumberInteger(value);
	}

	public EsPrimitiveNumberInteger(long value) {
		m_value = value;
	}

	private final long m_value;
}
