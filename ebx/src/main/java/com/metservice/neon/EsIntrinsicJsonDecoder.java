/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.ArgonFormatException;
import com.metservice.argon.json.IJsonDeFactory;
import com.metservice.argon.json.IJsonNative;
import com.metservice.argon.json.JsonDecoder;
import com.metservice.argon.json.JsonString;
import com.metservice.argon.json.JsonType;

/**
 * @author roach
 */
public final class EsIntrinsicJsonDecoder extends EsObject {

	void loadPrototype() {
	}

	@Override
	protected void loadProperties(EsExecutionContext ecx) {
	}

	@Override
	public IJsonNative createJsonNative() {
		return JsonString.newInstance(m_oqtwSource);
	}

	@Override
	public EsObject createObject() {
		return new EsIntrinsicJsonDecoder(this);
	}

	public IEsOperand decode(EsExecutionContext ecx, boolean validate) {
		return decode(ecx, m_oqtwSource, validate);
	}

	@Override
	public String esClass() {
		return EsIntrinsicJsonDecoderConstructor.ClassName;
	}

	@Override
	public JsonType getJsonType() {
		return JsonType.TString;
	}

	public void setSource(String qtwSource) {
		if (qtwSource == null || qtwSource.length() == 0) throw new IllegalArgumentException("string is null or empty");
		m_oqtwSource = qtwSource;
	}

	@Override
	public String show(int depth) {
		final StringBuilder sb = new StringBuilder();
		sb.append("JsonDecoder");
		if (depth > 0) {
			sb.append("(");
			sb.append(m_oqtwSource == null ? "" : m_oqtwSource);
			sb.append(")");
		}
		return sb.toString();
	}

	public EsPrimitiveString toPrimitiveString(EsExecutionContext ecx)
			throws InterruptedException {
		return EsPrimitiveString.newInstance(m_oqtwSource);
	}

	private static IEsOperand newOperand(EsExecutionContext ecx, String oztwSource)
			throws ArgonFormatException {
		if (oztwSource == null) return EsPrimitiveNull.Instance;
		final JsonDecoder decoder = newDecoder(ecx);
		final IJsonDeFactory factory = newDecodeFactory(ecx);
		return (IEsOperand) decoder.decode(oztwSource, factory);
	}

	static IJsonDeFactory newDecodeFactory(EsExecutionContext ecx) {
		return new JsonDeFactory(ecx);
	}

	static JsonDecoder newDecoder(EsExecutionContext ecx) {
		return JsonDecoder.Default;
	}

	public static IEsOperand decode(EsExecutionContext ecx, String oztwSource, boolean validate) {
		if (ecx == null) throw new IllegalArgumentException("object is null");

		if (validate) {
			final EsIntrinsicObject esReport = ecx.global().newIntrinsicObject();
			try {
				esReport.putViewResult(newOperand(ecx, oztwSource));
			} catch (final ArgonFormatException ex) {
				esReport.putViewError(ex);
			}
			return esReport;
		}

		try {
			return newOperand(ecx, oztwSource);
		} catch (final ArgonFormatException ex) {
			throw new EsApiCodeException(ex);
		}
	}

	public EsIntrinsicJsonDecoder(EsObject prototype) {
		super(prototype);
	}

	private String m_oqtwSource;
}
