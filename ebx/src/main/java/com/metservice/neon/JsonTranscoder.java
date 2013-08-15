/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.List;

import com.metservice.argon.json.IJsonArray;
import com.metservice.argon.json.IJsonBoolean;
import com.metservice.argon.json.IJsonNull;
import com.metservice.argon.json.IJsonNumber;
import com.metservice.argon.json.IJsonNumberDouble;
import com.metservice.argon.json.IJsonNumberElapsed;
import com.metservice.argon.json.IJsonNumberInteger;
import com.metservice.argon.json.IJsonNumberTime;
import com.metservice.argon.json.IJsonObject;
import com.metservice.argon.json.IJsonString;
import com.metservice.argon.json.IJsonValue;

/**
 * @author roach
 */
class JsonTranscoder {

	private static EsPrimitive newPrimitive(IJsonValue val) {
		assert val != null;
		if (val instanceof IJsonString) {
			final IJsonString jval = (IJsonString) val;
			return EsPrimitiveString.newInstance(jval.jsonDatum());
		}
		if (val instanceof IJsonNumber) {
			if (val instanceof IJsonNumberInteger) {
				final IJsonNumberInteger jval = (IJsonNumberInteger) val;
				return EsPrimitiveNumberInteger.newInstance(jval.jsonDatum());
			}
			if (val instanceof IJsonNumberTime) {
				final IJsonNumberTime jval = (IJsonNumberTime) val;
				return new EsPrimitiveNumberTime(jval.jsonDatum());
			}
			if (val instanceof IJsonNumberElapsed) {
				final IJsonNumberElapsed jval = (IJsonNumberElapsed) val;
				return new EsPrimitiveNumberElapsed(jval.jsonDatum());
			}
			if (val instanceof IJsonNumberDouble) {
				final IJsonNumberDouble jval = (IJsonNumberDouble) val;
				final double datum = jval.jsonDatum();
				if (Double.isNaN(datum)) return EsPrimitiveNumberNot.Instance;
				return new EsPrimitiveNumberDouble(jval.jsonDatum());
			}
		}
		if (val instanceof IJsonBoolean) {
			final IJsonBoolean jval = (IJsonBoolean) val;
			return EsPrimitiveBoolean.instance(jval.jsonDatum());
		}
		if (val instanceof IJsonNull) return EsPrimitiveNull.Instance;

		throw new UnsupportedOperationException("No mapping of Json type " + val.getClass() + " to EsPrimitive");
	}

	public static EsIntrinsicArray newIntrinsicArray(EsExecutionContext ecx, IJsonArray src, boolean allowUpdate) {
		if (src == null) throw new IllegalArgumentException("object is null");
		final int memberCount = src.jsonMemberCount();
		final IEsOperand[] neo = new IEsOperand[memberCount];
		for (int i = 0; i < memberCount; i++) {
			final IJsonValue pval = src.jsonValue(i);
			neo[i] = newOperand(ecx, pval, allowUpdate);
		}
		return ecx.global().newIntrinsicArray(neo);
	}

	public static EsIntrinsicObject newIntrinsicObject(EsExecutionContext ecx, IJsonObject src, boolean allowUpdate) {
		if (src == null) throw new IllegalArgumentException("object is null");
		final EsIntrinsicObject neo = ecx.global().newIntrinsicObject();
		final List<String> jsonNames = src.jsonNames();
		final int nameCount = jsonNames.size();
		for (int i = 0; i < nameCount; i++) {
			final String pname = jsonNames.get(i);
			final IJsonValue pval = src.jsonValue(pname);
			final IEsOperand operand = newOperand(ecx, pval, allowUpdate);
			neo.add(pname, allowUpdate, operand);
		}
		return neo;
	}

	public static IEsOperand newOperand(EsExecutionContext ecx, IJsonValue src, boolean allowUpdate) {
		if (src instanceof IJsonObject) return newIntrinsicObject(ecx, (IJsonObject) src, allowUpdate);
		if (src instanceof IJsonArray) return newIntrinsicArray(ecx, (IJsonArray) src, allowUpdate);
		return newPrimitive(src);
	}
}
