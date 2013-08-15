/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.Date;

import com.metservice.argon.DateFactory;
import com.metservice.argon.DateFormatter;
import com.metservice.argon.DecimalMask;
import com.metservice.argon.Real;
import com.metservice.argon.json.IJsonNativeNumber;
import com.metservice.argon.json.IJsonNumberTime;
import com.metservice.argon.json.JsonNumberTime;
import com.metservice.argon.json.JsonType;

/**
 * 
 * @author roach
 */
public final class EsPrimitiveNumberTime extends EsPrimitiveNumber implements IJsonNumberTime {

	@Override
	public EsPrimitiveNumber abs() {
		return this;
	}

	@Override
	public double doubleValue() {
		return m_tsValue;
	}

	@Override
	public JsonType getJsonType() {
		return JsonType.TNumberTime;
	}

	@Override
	public int intVerified() {
		return UNeon.intVerified(m_tsValue);
	}

	@Override
	public boolean isLessThan(EsPrimitiveNumber rn) {
		final SubType rt = rn.subType();
		if (rt == SubType.TIME) return m_tsValue < rn.longValue();
		return true;
	}

	@Override
	public boolean isNaN() {
		return false;
	}

	@Override
	public long jsonDatum() {
		return m_tsValue;
	}

	@Override
	public long longValue() {
		return m_tsValue;
	}

	@Override
	public EsPrimitiveNumber negated() {
		return this;
	}

	public Date newDate() {
		return DateFactory.newDate(m_tsValue);
	}

	@Override
	public IJsonNativeNumber newJsonNativeNumber() {
		return new JsonNumberTime(m_tsValue);
	}

	@Override
	public Real realValue() {
		return Real.newInstance(m_tsValue);
	}

	@Override
	public boolean sameNumberValue(EsPrimitiveNumber rn) {
		final SubType rt = rn.subType();
		if (rt == SubType.TIME) return m_tsValue == rn.longValue();
		return false;
	}

	public String show(int depth) {
		return DateFormatter.newT8FromTs(m_tsValue);
	}

	@Override
	public SubType subType() {
		return SubType.TIME;
	}

	@Override
	public String toCanonicalString() {
		return DateFormatter.newT8FromTs(m_tsValue);
	}

	@Override
	public String toCanonicalString(DecimalMask decimalMask) {
		return decimalMask.format(m_tsValue);
	}

	@Override
	public int toHash() {
		return (int) (m_tsValue ^ (m_tsValue >>> 32));
	}

	public long ts() {
		return m_tsValue;
	}

	public EsPrimitiveNumberTime(Date value) {
		if (value == null) throw new IllegalArgumentException("object is null");
		m_tsValue = value.getTime();
	}

	public EsPrimitiveNumberTime(long tsValue) {
		m_tsValue = tsValue;
	}

	private final long m_tsValue;
}
