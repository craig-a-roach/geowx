/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.concurrent.atomic.AtomicReference;

import com.metservice.argon.json.IJsonNative;
import com.metservice.argon.json.JsonObject;

/**
 * @author roach
 */
public abstract class EsResponse {

	protected static final <T extends EsObject> T esObjectReturn(EsExecutionContext ecx, IEsOperand result, Class<T> expectedClass)
			throws InterruptedException {
		if (result == null) throw new IllegalArgumentException("object is null");
		if (expectedClass == null) throw new IllegalArgumentException("object is null");
		final EsObject es = result.toObject(ecx);
		if (expectedClass.isInstance(es)) return expectedClass.cast(es);
		final String expectedName = expectedClass.getName();
		final String m = "Could not convert '" + es.esClass() + " return value to a '" + expectedName + "' object";
		throw new EsTypeCodeException(m);
	}

	protected static final JsonObject newJsonObject(IEsOperand result, String pnameDefault) {
		if (result == null) throw new IllegalArgumentException("object is null");
		if (pnameDefault == null || pnameDefault.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final EsType esType = result.esType();
		final IJsonNative oNative = esType.isDatum ? result.createJsonNative() : null;
		if (oNative instanceof JsonObject) return (JsonObject) oNative;
		final String z = oNative == null ? "" : oNative.toString();
		final JsonObject s = JsonObject.newMutable(1);
		s.putString(pnameDefault, z);
		return s;
	}

	protected static <T> T returnValue(AtomicReference<T> ref) {
		if (ref == null) throw new IllegalArgumentException("object is null");
		final T oValue = ref.get();
		if (oValue == null) throw new IllegalStateException("return value is not available");
		return oValue;
	}

	protected static final String zReturn(EsExecutionContext ecx, IEsOperand result)
			throws InterruptedException {
		if (result == null) throw new IllegalArgumentException("object is null");
		final EsType returnType = result.esType();
		return returnType.isDefined ? result.toCanonicalString(ecx) : "";
	}

	protected static final String ztwThrow(EsExecutionContext ecx, EsCompletionThrow completion)
			throws InterruptedException {
		if (completion == null) throw new IllegalArgumentException("object is null");
		final EsType esType = completion.esType();
		return esType.isDatum ? completion.toCanonicalString(ecx).trim() : "";
	}

	void save(EsRequest request, EsGlobal global, EsExecutionContext ecx, IEsOperand callResult)
			throws InterruptedException {
		if (request == null) throw new IllegalArgumentException("object is null");
		if (global == null) throw new IllegalArgumentException("object is null");
		if (ecx == null) throw new IllegalArgumentException("object is null");
		if (callResult == null) throw new IllegalArgumentException("object is null");

		if (callResult instanceof EsCompletionThrow) {
			final EsCompletionThrow completion = (EsCompletionThrow) callResult;
			saveThrow(request, ecx, completion);
		} else {
			saveReturn(request, ecx, callResult);
			saveGlobals(request, global, ecx);
		}
	}

	protected abstract void saveGlobals(EsRequest request, EsGlobal global, EsExecutionContext ecx)
			throws InterruptedException;

	protected abstract void saveReturn(EsRequest request, EsExecutionContext ecx, IEsOperand callResult)
			throws InterruptedException;

	protected abstract void saveThrow(EsRequest request, EsExecutionContext ecx, EsCompletionThrow completion)
			throws InterruptedException;

	protected EsResponse() {
	}
}
