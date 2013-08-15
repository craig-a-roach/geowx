/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.metservice.argon.Binary;
import com.metservice.argon.Elapsed;
import com.metservice.argon.ICodedEnum;
import com.metservice.argon.Real;
import com.metservice.argon.TimeOfDayRule;
import com.metservice.argon.json.IJsonDeObject;
import com.metservice.argon.json.IJsonDeValue;
import com.metservice.argon.json.IJsonNative;
import com.metservice.argon.json.IJsonObject;
import com.metservice.argon.json.IJsonValue;
import com.metservice.argon.json.JsonArray;
import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonType;

/**
 * 
 * @author roach
 */
public abstract class EmObject extends EsObject implements IJsonObject, IJsonDeObject {

	private final EsIntrinsicObject declareIntrinsicObject(EsExecutionContext ecx, String qccPropertyName, boolean allowUpdate) {
		final IEsOperand ex = esGet(qccPropertyName);
		if (ex instanceof EsIntrinsicObject) return (EsIntrinsicObject) ex;
		final EsIntrinsicObject neo = ecx.global().newIntrinsicObject();
		add(qccPropertyName, allowUpdate, neo);
		return neo;
	}

	private void putProperty(EsExecutionContext ecx, String qccPropertyName, boolean allowUpdate, Binary oValue) {
		if (oValue == null) {
			putPropertyNull(qccPropertyName, allowUpdate);
			return;
		}
		add(qccPropertyName, allowUpdate, ecx.global().newIntrinsicBinary(oValue));
	}

	private void putProperty(EsExecutionContext ecx, String qccPropertyName, boolean allowUpdate, IEsOperand[] ozptValues) {
		if (ozptValues == null) {
			putPropertyNull(qccPropertyName, allowUpdate);
			return;
		}
		add(qccPropertyName, allowUpdate, ecx.global().newIntrinsicArray(ozptValues));
	}

	private void putProperty(EsExecutionContext ecx, String qccPropertyName, boolean allowUpdate, JsonArray oValue,
			boolean mutableValue) {
		if (oValue == null) {
			putPropertyNull(qccPropertyName, allowUpdate);
			return;
		}
		add(qccPropertyName, allowUpdate, JsonTranscoder.newIntrinsicArray(ecx, oValue, mutableValue));
	}

	private void putProperty(EsExecutionContext ecx, String qccPropertyName, boolean allowUpdate, JsonObject oValue,
			boolean mutableValue) {
		if (oValue == null) {
			putPropertyNull(qccPropertyName, allowUpdate);
			return;
		}
		add(qccPropertyName, allowUpdate, JsonTranscoder.newIntrinsicObject(ecx, oValue, mutableValue));
	}

	private void putProperty(EsExecutionContext ecx, String qccPropertyName, boolean allowUpdate, String[] ozptzValues) {
		final IEsOperand[] ozptOperands;
		if (ozptzValues == null) {
			ozptOperands = null;
		} else {
			ozptOperands = new IEsOperand[ozptzValues.length];
			for (int i = 0; i < ozptzValues.length; i++) {
				ozptOperands[i] = new EsPrimitiveString(ozptzValues[i]);
			}
		}
		putProperty(ecx, qccPropertyName, allowUpdate, ozptOperands);
	}

	private void putProperty(EsExecutionContext ecx, String qccPropertyName, boolean allowUpdate, TimeZone oValue) {
		if (oValue == null) {
			putPropertyNull(qccPropertyName, allowUpdate);
			return;
		}
		add(qccPropertyName, allowUpdate, ecx.global().newIntrinsicTimezone(oValue));
	}

	private void putProperty(String qccPropertyName, boolean allowUpdate, boolean value) {
		add(qccPropertyName, allowUpdate, EsPrimitiveBoolean.instance(value));
	}

	private void putProperty(String qccPropertyName, boolean allowUpdate, Charset oValue) {
		if (oValue == null) {
			putPropertyNull(qccPropertyName, allowUpdate);
			return;
		}
		add(qccPropertyName, allowUpdate, EsPrimitiveString.newInstance(oValue));
	}

	private void putProperty(String qccPropertyName, boolean allowUpdate, Date oValue) {
		if (oValue == null) {
			putPropertyNull(qccPropertyName, allowUpdate);
			return;
		}
		add(qccPropertyName, allowUpdate, new EsPrimitiveNumberTime(oValue));
	}

	private void putProperty(String qccPropertyName, boolean allowUpdate, double value) {
		add(qccPropertyName, allowUpdate, new EsPrimitiveNumberDouble(value));
	}

	private void putProperty(String qccPropertyName, boolean allowUpdate, Elapsed oValue) {
		if (oValue == null) {
			putPropertyNull(qccPropertyName, allowUpdate);
			return;
		}
		add(qccPropertyName, allowUpdate, EsPrimitiveNumberElapsed.newInstance(oValue));
	}

	private void putProperty(String qccPropertyName, boolean allowUpdate, ICodedEnum oValue) {
		if (oValue == null) {
			putPropertyNull(qccPropertyName, allowUpdate);
			return;
		}
		add(qccPropertyName, allowUpdate, new EsPrimitiveString(oValue.qCode()));
	}

	private void putProperty(String qccPropertyName, boolean allowUpdate, int value) {
		add(qccPropertyName, allowUpdate, EsPrimitiveNumberInteger.newInstance(value));
	}

	private void putProperty(String qccPropertyName, boolean allowUpdate, Real oValue) {
		if (oValue == null) {
			putPropertyNull(qccPropertyName, allowUpdate);
			return;
		}
		add(qccPropertyName, allowUpdate, new EsPrimitiveNumberReal(oValue));
	}

	private void putProperty(String qccPropertyName, boolean allowUpdate, String ozValue) {
		if (ozValue == null) {
			putPropertyNull(qccPropertyName, allowUpdate);
			return;
		}
		add(qccPropertyName, allowUpdate, EsPrimitiveString.newInstance(ozValue));
	}

	private void putProperty(String qccPropertyName, boolean allowUpdate, TimeOfDayRule oValue) {
		if (oValue == null) {
			putPropertyNull(qccPropertyName, allowUpdate);
			return;
		}
		add(qccPropertyName, allowUpdate, EsPrimitiveString.newInstance(oValue.format()));
	}

	private void putPropertyElapsed(String qccPropertyName, boolean allowUpdate, long sms) {
		add(qccPropertyName, allowUpdate, EsPrimitiveNumberElapsed.newInstance(sms));
	}

	private void putPropertyNull(String qccPropertyName, boolean allowUpdate) {
		add(qccPropertyName, allowUpdate, EsPrimitiveNull.Instance);
	}

	private void putPropertyTime(String qccPropertyName, boolean allowUpdate, long ts) {
		add(qccPropertyName, allowUpdate, new EsPrimitiveNumberTime(ts));
	}

	@Override
	protected final void loadProperties(EsExecutionContext ecx)
			throws InterruptedException {
		if (!m_propertiesLoaded) {
			putProperties(ecx);
			m_propertiesLoaded = true;
		}
	}

	protected final void reloadProperties() {
		m_propertiesLoaded = false;
	}

	@Override
	public IJsonNative createJsonNative() {
		return newJsonObject();
	}

	public final EsIntrinsicObject declareUpdateObject(EsExecutionContext ecx, String qccPropertyName) {
		return declareIntrinsicObject(ecx, qccPropertyName, true);
	}

	public final EsIntrinsicObject declareViewObject(EsExecutionContext ecx, String qccPropertyName) {
		return declareIntrinsicObject(ecx, qccPropertyName, false);
	}

	@Override
	public final String esClass() {
		return esPrototype().esClass();
	}

	@Override
	public JsonType getJsonType() {
		return JsonType.TObject;
	}

	@Override
	public List<String> jsonNames() {
		return esPropertyNames();
	}

	@Override
	public void jsonPut(String name, IJsonDeValue value) {
		if (value instanceof IEsOperand) {
			esPut(name, (IEsOperand) value);
		}
	}

	@Override
	public IJsonValue jsonValue(String name) {
		return esGet(name);
	}

	public abstract void putProperties(EsExecutionContext ecx)
			throws InterruptedException;

	public final void putUpdate(EsExecutionContext ecx, String qccPropertyName, Binary oValue) {
		putProperty(ecx, qccPropertyName, true, oValue);
	}

	public final void putUpdate(EsExecutionContext ecx, String qccPropertyName, IEsOperand[] ozptValues) {
		putProperty(ecx, qccPropertyName, true, ozptValues);
	}

	public final void putUpdate(EsExecutionContext ecx, String qccPropertyName, String[] ozptzValues) {
		putProperty(ecx, qccPropertyName, true, ozptzValues);
	}

	public final void putUpdate(EsExecutionContext ecx, String qccPropertyName, TimeZone oValue) {
		putProperty(ecx, qccPropertyName, true, oValue);
	}

	public final void putUpdate(String qccPropertyName, Charset oValue) {
		putProperty(qccPropertyName, true, oValue);
	}

	public final void putUpdate(String qccPropertyName, Date oValue) {
		putProperty(qccPropertyName, true, oValue);
	}

	public final void putUpdate(String qccPropertyName, Elapsed oValue) {
		putProperty(qccPropertyName, true, oValue);
	}

	public final void putUpdate(String qccPropertyName, ICodedEnum oValue) {
		putProperty(qccPropertyName, true, oValue);
	}

	public final void putUpdate(String qccPropertyName, IEsOperand value) {
		add(qccPropertyName, true, value);
	}

	public final void putUpdate(String qccPropertyName, String ozValue) {
		putProperty(qccPropertyName, true, ozValue);
	}

	public final void putUpdate(String qccPropertyName, TimeOfDayRule oValue) {
		putProperty(qccPropertyName, true, oValue);
	}

	public final void putUpdateBoolean(String qccPropertyName, boolean value) {
		putProperty(qccPropertyName, true, value);
	}

	public final void putUpdateDouble(String qccPropertyName, double value) {
		putProperty(qccPropertyName, true, value);
	}

	public final void putUpdateElapsed(String qccPropertyName, long sms) {
		putPropertyElapsed(qccPropertyName, true, sms);
	}

	public final void putUpdateImmutable(EsExecutionContext ecx, String qccPropertyName, JsonObject oValue) {
		putProperty(ecx, qccPropertyName, true, oValue, false);
	}

	public final void putUpdateInteger(String qccPropertyName, int value) {
		putProperty(qccPropertyName, true, value);
	}

	public final void putUpdateMutable(EsExecutionContext ecx, String qccPropertyName, JsonObject oValue) {
		putProperty(ecx, qccPropertyName, true, oValue, true);
	}

	public final void putUpdateNull(String qccPropertyName) {
		putPropertyNull(qccPropertyName, true);
	}

	public final void putUpdateTime(String qccPropertyName, long ts) {
		putPropertyTime(qccPropertyName, true, ts);
	}

	public final void putView(EsExecutionContext ecx, String qccPropertyName, Binary oValue) {
		putProperty(ecx, qccPropertyName, false, oValue);
	}

	public final void putView(EsExecutionContext ecx, String qccPropertyName, IEsOperand[] ozptValues) {
		putProperty(ecx, qccPropertyName, false, ozptValues);
	}

	public final void putView(EsExecutionContext ecx, String qccPropertyName, JsonArray oValue) {
		putProperty(ecx, qccPropertyName, false, oValue, false);
	}

	public final void putView(EsExecutionContext ecx, String qccPropertyName, JsonObject oValue) {
		putProperty(ecx, qccPropertyName, false, oValue, false);
	}

	public final void putView(EsExecutionContext ecx, String qccPropertyName, String[] ozptzValues) {
		putProperty(ecx, qccPropertyName, false, ozptzValues);
	}

	public final void putView(EsExecutionContext ecx, String qccPropertyName, TimeZone oValue) {
		putProperty(ecx, qccPropertyName, false, oValue);
	}

	public final void putView(String qccPropertyName, Charset oValue) {
		putProperty(qccPropertyName, false, oValue);
	}

	public final void putView(String qccPropertyName, Date oValue) {
		putProperty(qccPropertyName, false, oValue);
	}

	public final void putView(String qccPropertyName, Elapsed oValue) {
		putProperty(qccPropertyName, false, oValue);
	}

	public final void putView(String qccPropertyName, ICodedEnum oValue) {
		putProperty(qccPropertyName, false, oValue);
	}

	public final void putView(String qccPropertyName, IEsOperand value) {
		add(qccPropertyName, false, value);
	}

	public final void putView(String qccPropertyName, Real oValue) {
		putProperty(qccPropertyName, false, oValue);
	}

	public final void putView(String qccPropertyName, String ozValue) {
		putProperty(qccPropertyName, false, ozValue);
	}

	public final void putView(String qccPropertyName, TimeOfDayRule oValue) {
		putProperty(qccPropertyName, false, oValue);
	}

	public final void putViewBoolean(String qccPropertyName, boolean value) {
		putProperty(qccPropertyName, false, value);
	}

	public final void putViewDouble(String qccPropertyName, double value) {
		putProperty(qccPropertyName, false, value);
	}

	public final void putViewElapsed(String qccPropertyName, long sms) {
		putPropertyElapsed(qccPropertyName, false, sms);
	}

	public final void putViewIfQtw(String qccPropertyName, String ozValue) {
		final String ztwValue = ozValue == null ? "" : ozValue.trim();
		if (ztwValue.length() > 0) {
			putProperty(qccPropertyName, false, ztwValue);
		}
	}

	public final void putViewInteger(String qccPropertyName, int value) {
		putProperty(qccPropertyName, false, value);
	}

	public final void putViewNull(String qccPropertyName) {
		putPropertyNull(qccPropertyName, false);
	}

	public final void putViewTime(String qccPropertyName, long ts) {
		putPropertyTime(qccPropertyName, false, ts);
	}

	public EmObject(EmClass emClass) {
		super(emClass);
	}

	public EmObject(EmObject prototype) {
		super(prototype);
	}

	private boolean m_propertiesLoaded;
}
