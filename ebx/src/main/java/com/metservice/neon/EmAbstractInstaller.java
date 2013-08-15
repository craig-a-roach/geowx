/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.Date;
import java.util.Map;

import com.metservice.argon.ArgonFormatException;
import com.metservice.argon.Ds;
import com.metservice.argon.Elapsed;
import com.metservice.argon.json.JsonObject;

/**
 * 
 * @author roach
 */
public abstract class EmAbstractInstaller implements IEmInstaller {

	protected static final String CsqNoInstall = "Application model could not be installed into neon space";

	protected static final String PSuffixError = "Error";

	protected final void putClass(EsExecutionContext ecx, EmConstructor constructor)
			throws InterruptedException {
		if (ecx == null) throw new IllegalArgumentException("ecx is null");
		ecx.global().install(ecx, constructor);
	}

	protected final void putSingleton(EsExecutionContext ecx, EmObject emObject) {
		if (ecx == null) throw new IllegalArgumentException("ecx is null");
		ecx.global().putSingleton(emObject);
	}

	protected final void putView(EsExecutionContext ecx, Map<String, IEsOperand> zmName_Value) {
		if (ecx == null) throw new IllegalArgumentException("ecx is null");
		if (zmName_Value == null) throw new IllegalArgumentException("object is null");
		final EsGlobal global = ecx.global();
		for (final Map.Entry<String, IEsOperand> e : zmName_Value.entrySet()) {
			global.putReadOnly(e.getKey(), e.getValue());
		}
	}

	protected final void putView(EsExecutionContext ecx, String qccPropertyName, boolean value) {
		putView(ecx, qccPropertyName, EsPrimitiveBoolean.instance(value));
	}

	protected final void putView(EsExecutionContext ecx, String qccPropertyName, Date oValue) {
		if (oValue == null) {
			putViewNull(ecx, qccPropertyName);
			return;
		}
		putView(ecx, qccPropertyName, new EsPrimitiveNumberTime(oValue));
	}

	protected final void putView(EsExecutionContext ecx, String qccPropertyName, double value) {
		putView(ecx, qccPropertyName, new EsPrimitiveNumberDouble(value));
	}

	protected final void putView(EsExecutionContext ecx, String qccPropertyName, Elapsed oValue) {
		if (oValue == null) {
			putViewNull(ecx, qccPropertyName);
			return;
		}
		putView(ecx, qccPropertyName, EsPrimitiveNumberElapsed.newInstance(oValue));
	}

	protected final void putView(EsExecutionContext ecx, String qccPropertyName, IEsOperand value) {
		if (ecx == null) throw new IllegalArgumentException("ecx is null");
		ecx.global().putReadOnly(qccPropertyName, value);
	}

	protected final void putView(EsExecutionContext ecx, String qccPropertyName, int value) {
		putView(ecx, qccPropertyName, EsPrimitiveNumberInteger.newInstance(value));
	}

	protected final void putView(EsExecutionContext ecx, String qccPropertyName, JsonObject oValue) {
		if (oValue == null) {
			putViewNull(ecx, qccPropertyName);
			return;
		}
		putView(ecx, qccPropertyName, JsonTranscoder.newIntrinsicObject(ecx, oValue, false));
	}

	protected final void putView(EsExecutionContext ecx, String qccPropertyName, Map<String, IEsOperand> zmName_Value) {
		if (ecx == null) throw new IllegalArgumentException("ecx is null");
		if (zmName_Value == null) throw new IllegalArgumentException("object is null");
		final EsGlobal global = ecx.global();
		final EsIntrinsicObject container = global.newIntrinsicObject();
		for (final Map.Entry<String, IEsOperand> e : zmName_Value.entrySet()) {
			container.add(e.getKey(), EsProperty.newReadOnlyDontDelete(e.getValue()));
		}
		global.putReadOnly(qccPropertyName, container);
	}

	protected final void putView(EsExecutionContext ecx, String qccPropertyName, String ozValue) {
		if (ozValue == null) {
			putViewNull(ecx, qccPropertyName);
			return;
		}
		putView(ecx, qccPropertyName, EsPrimitiveString.newInstance(ozValue));
	}

	protected final void putViewElapsed(EsExecutionContext ecx, String qccPropertyName, long smsValue) {
		putView(ecx, qccPropertyName, EsPrimitiveNumberElapsed.newInstance(smsValue));
	}

	protected void putViewJson(EsExecutionContext ecx, String qccPropertyName, String zJsonSpec, boolean validate) {
		try {
			final IEsOperand value = JsonDeFactory.newOperand(ecx, zJsonSpec);
			ecx.global().putReadOnly(qccPropertyName, value);
		} catch (final ArgonFormatException ex) {
			if (!validate) {
				final Ds ds = Ds.triedTo("Decode JSON installation property", ex, CsqNoInstall);
				ds.a("propertyName", zJsonSpec);
				throw new EmException(ds.s());
			}
			final EsPrimitiveString erm = EsPrimitiveString.newInstance(ex.getMessage());
			ecx.global().putReadOnly(qccPropertyName + PSuffixError, erm);
		}
	}

	protected final void putViewNull(EsExecutionContext ecx, String qccPropertyName) {
		putView(ecx, qccPropertyName, EsPrimitiveNull.Instance);
	}

	protected final void putViewTime(EsExecutionContext ecx, String qccPropertyName, long ts) {
		putView(ecx, qccPropertyName, new EsPrimitiveNumberTime(ts));
	}

	protected EmAbstractInstaller() {
	}
}
