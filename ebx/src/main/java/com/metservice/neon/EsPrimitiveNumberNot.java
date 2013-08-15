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
public final class EsPrimitiveNumberNot extends EsPrimitiveNumber implements IJsonNumberDouble {

	public static final EsPrimitiveNumberNot Instance = new EsPrimitiveNumberNot();

	@Override
	public EsPrimitiveNumber abs() {
		return this;
	}

	@Override
	public double doubleValue() {
		return Double.NaN;
	}

	@Override
	public JsonType getJsonType() {
		return JsonType.TNumberDouble;
	}

	@Override
	public int intVerified() {
		return 0;
	}

	@Override
	public boolean isLessThan(EsPrimitiveNumber rhsNumber) {
		return false;
	}

	@Override
	public boolean isNaN() {
		return true;
	}

	@Override
	public double jsonDatum() {
		return Double.NaN;
	}

	@Override
	public long longValue() {
		return 0L;
	}

	@Override
	public EsPrimitiveNumber negated() {
		return this;
	}

	@Override
	public IJsonNativeNumber newJsonNativeNumber() {
		return JsonNumberDouble.NaN;
	}

	@Override
	public Real realValue() {
		return Real.NaN;
	}

	@Override
	public boolean sameNumberValue(EsPrimitiveNumber rn) {
		final SubType rt = rn.subType();
		return rt == SubType.NAN;
	}

	public String show(int depth) {
		return "NaN";
	}

	@Override
	public SubType subType() {
		return SubType.NAN;
	}

	@Override
	public String toCanonicalString() {
		return "NaN";
	}

	@Override
	public String toCanonicalString(DecimalMask decimalMask) {
		return decimalMask.format(Double.NaN);
	}

	@Override
	public int toHash() {
		return 1249;
	}

	private EsPrimitiveNumberNot() {
	}
}
