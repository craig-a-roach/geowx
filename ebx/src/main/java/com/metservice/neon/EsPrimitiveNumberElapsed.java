/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.DecimalMask;
import com.metservice.argon.Elapsed;
import com.metservice.argon.ElapsedFormatter;
import com.metservice.argon.Real;
import com.metservice.argon.json.IJsonNativeNumber;
import com.metservice.argon.json.IJsonNumberElapsed;
import com.metservice.argon.json.JsonNumberElapsed;
import com.metservice.argon.json.JsonType;

/**
 * 
 * @author roach
 */
public final class EsPrimitiveNumberElapsed extends EsPrimitiveNumber implements IJsonNumberElapsed {

	public static final EsPrimitiveNumberElapsed ZERO = new EsPrimitiveNumberElapsed(0L);

	@Override
	public EsPrimitiveNumber abs() {
		return m_smsValue >= 0L ? this : new EsPrimitiveNumberElapsed(-m_smsValue);
	}

	@Override
	public double doubleValue() {
		return m_smsValue;
	}

	@Override
	public JsonType getJsonType() {
		return JsonType.TNumberElapsed;
	}

	@Override
	public int intVerified() {
		return UNeon.intVerified(m_smsValue);
	}

	@Override
	public boolean isLessThan(EsPrimitiveNumber rn) {
		final SubType rt = rn.subType();
		if (rt == SubType.ELAPSED || rt == SubType.INTEGER) return m_smsValue < rn.longValue();
		if (rt == SubType.TIME || rt == SubType.NAN) return true;
		return doubleValue() < rn.doubleValue();
	}

	@Override
	public boolean isNaN() {
		return false;
	}

	@Override
	public long jsonDatum() {
		return m_smsValue;
	}

	@Override
	public long longValue() {
		return m_smsValue;
	}

	@Override
	public EsPrimitiveNumber negated() {
		return new EsPrimitiveNumberElapsed(-m_smsValue);
	}

	public Elapsed newElapsed() {
		return Elapsed.newInstance(m_smsValue);
	}

	@Override
	public IJsonNativeNumber newJsonNativeNumber() {
		return JsonNumberElapsed.newInstance(m_smsValue);
	}

	@Override
	public Real realValue() {
		return Real.newInstance(m_smsValue);
	}

	@Override
	public boolean sameNumberValue(EsPrimitiveNumber rn) {
		final SubType rt = rn.subType();
		if (rt == SubType.ELAPSED || rt == SubType.INTEGER) return m_smsValue == rn.longValue();
		if (rt == SubType.TIME || rt == SubType.NAN) return false;
		return doubleValue() == rn.doubleValue();
	}

	public String show(int depth) {
		return ElapsedFormatter.formatSingleUnit(m_smsValue);
	}

	@Override
	public SubType subType() {
		return SubType.ELAPSED;
	}

	@Override
	public String toCanonicalString() {
		return ElapsedFormatter.formatSingleUnit(m_smsValue);
	}

	@Override
	public String toCanonicalString(DecimalMask decimalMask) {
		return decimalMask.format(m_smsValue);
	}

	@Override
	public int toHash() {
		return (int) (m_smsValue ^ (m_smsValue >>> 32));
	}

	public static EsPrimitiveNumberElapsed newInstance(Elapsed oValue) {
		if (oValue == null || oValue.sms == 0L) return ZERO;
		return new EsPrimitiveNumberElapsed(oValue.sms);
	}

	public static EsPrimitiveNumberElapsed newInstance(long smsValue) {
		if (smsValue == 0L) return ZERO;
		return new EsPrimitiveNumberElapsed(smsValue);
	}

	public EsPrimitiveNumberElapsed(Elapsed value) {
		if (value == null) throw new IllegalArgumentException("object is null");
		m_smsValue = value.sms;
	}

	public EsPrimitiveNumberElapsed(long smsValue) {
		m_smsValue = smsValue;
	}

	private final long m_smsValue;
}
