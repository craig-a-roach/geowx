/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.ArgonFormatException;
import com.metservice.argon.Binary;
import com.metservice.argon.json.IJsonDeArray;
import com.metservice.argon.json.IJsonDeFactory;
import com.metservice.argon.json.IJsonDeObject;
import com.metservice.argon.json.IJsonDeValue;
import com.metservice.argon.json.JsonDecoder;

/**
 * @author roach
 */
class JsonDeFactory implements IJsonDeFactory {

	public static IEsOperand newOperand(EsExecutionContext ecx, String zSource)
			throws ArgonFormatException {
		if (ecx == null) throw new IllegalArgumentException("object is null");
		if (zSource == null) throw new IllegalArgumentException("object is null");
		return (IEsOperand) JsonDecoder.Default.decode(zSource, new JsonDeFactory(ecx));
	}

	@Override
	public IJsonDeValue instanceNotNumber() {
		return EsPrimitiveNumberNot.Instance;
	}

	@Override
	public IJsonDeValue instanceNull() {
		return EsPrimitiveNull.Instance;
	}

	@Override
	public IJsonDeArray newArray() {
		return m_global.newIntrinsicArray();
	}

	@Override
	public IJsonDeValue newBinary(Binary value) {
		return m_global.newIntrinsicBinary(value);
	}

	@Override
	public IJsonDeValue newBoolean(boolean value) {
		return EsPrimitiveBoolean.instance(value);
	}

	@Override
	public IJsonDeValue newNumberDouble(double value) {
		return new EsPrimitiveNumberDouble(value);
	}

	@Override
	public IJsonDeValue newNumberElapsed(long sms) {
		return new EsPrimitiveNumberElapsed(sms);
	}

	@Override
	public IJsonDeValue newNumberInt(int value) {
		return new EsPrimitiveNumberInteger(value);
	}

	@Override
	public IJsonDeValue newNumberTime(long ts) {
		return new EsPrimitiveNumberTime(ts);
	}

	@Override
	public IJsonDeObject newObject() {
		return m_global.newIntrinsicObject();
	}

	@Override
	public IJsonDeValue newString(String zValue) {
		return EsPrimitiveString.newInstance(zValue);
	}

	public JsonDeFactory(EsExecutionContext ecx) {
		m_global = ecx.global();
	}

	private final EsGlobal m_global;
}
