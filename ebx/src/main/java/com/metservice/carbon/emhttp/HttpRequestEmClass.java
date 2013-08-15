/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emhttp;

import java.util.List;

import com.metservice.argon.json.JsonObject;
import com.metservice.neon.EmClass;
import com.metservice.neon.EmMethod;
import com.metservice.neon.EsExecutionContext;
import com.metservice.neon.EsFunction;
import com.metservice.neon.EsIntrinsicArray;
import com.metservice.neon.EsIntrinsicObject;
import com.metservice.neon.EsIntrinsicObjectBuilder;
import com.metservice.neon.EsMethodAccessor;
import com.metservice.neon.EsObject;
import com.metservice.neon.EsType;
import com.metservice.neon.IEsOperand;

/**
 * @author roach
 */
class HttpRequestEmClass extends EmClass {

	private static final String Name = CClass.HttpRequest;
	static final EmMethod[] Methods = { new method_newParameterObject() };

	static final HttpRequestEmClass Instance = new HttpRequestEmClass(Name, Methods);

	static void delete(EsIntrinsicObject dst, String[] zptqtwDeletions) {
		assert dst != null;
		assert zptqtwDeletions != null;
		for (int i = 0; i < zptqtwDeletions.length; i++) {
			dst.delete(zptqtwDeletions[i]);
		}
	}

	static void extend(EsIntrinsicObject dst, EsObject esExtension) {
		assert dst != null;
		assert esExtension != null;
		final List<String> zlExtensionNames = esExtension.esPropertyNames();
		final int pcount = zlExtensionNames.size();
		for (int i = 0; i < pcount; i++) {
			final String pname = zlExtensionNames.get(i);
			final IEsOperand esNeo = esExtension.esGet(pname);
			final EsType estNeo = esNeo.esType();
			if (!estNeo.isDatum) {
				continue;
			}
			if (esNeo instanceof EsFunction) {
				continue;
			}
			final IEsOperand esEx = dst.esGet(pname);
			if (esEx instanceof EsIntrinsicArray) {
				final EsIntrinsicArray arrayEx = (EsIntrinsicArray) esEx;
				if (esNeo instanceof EsIntrinsicArray) {
					final EsIntrinsicArray arrayNeo = (EsIntrinsicArray) esNeo;
					extendArrayMulti(arrayEx, arrayNeo);
				} else {
					extendArraySingle(arrayEx, esNeo);
				}
			} else {
				dst.putView(pname, esNeo);
			}
		}
	}

	static void extendArrayMulti(EsIntrinsicArray dst, EsIntrinsicArray src) {
		final int dstLength = dst.length();
		final int srcLength = src.length();
		int pos = dstLength;
		for (int i = 0; i < srcLength; i++) {
			final IEsOperand esValue = src.getByIndex(i);
			final EsType estNeo = esValue.esType();
			if (!estNeo.isDatum) {
				continue;
			}
			dst.putByIndex(pos, esValue);
			pos++;
		}
	}

	static void extendArraySingle(EsIntrinsicArray dst, IEsOperand esValue) {
		final int dstLength = dst.length();
		dst.putByIndex(dstLength, esValue);
	}

	static HttpRequestEm self(EsExecutionContext ecx) {
		return ecx.thisObject(Name, HttpRequestEm.class);
	}

	private HttpRequestEmClass(String qccClassName, EmMethod[] ozptMethods) {
		super(qccClassName, ozptMethods);
	}

	static class method_newParameterObject extends EmMethod {

		@Override
		protected IEsOperand eval(EsExecutionContext ecx)
				throws InterruptedException {
			final HttpRequestEm self = self(ecx);
			final EsMethodAccessor ac = new EsMethodAccessor(ecx);
			final EsObject esoExtension = ac.defaulted(0) ? null : ac.esoObject(0);
			final String[] ozptqtwDeletions = ac.defaulted(1) ? null : ac.select(1).zptqtwStringValuesEvery();
			final JsonObject jsonObject = self.newContentJsonObject();
			final EsIntrinsicObjectBuilder builder = EsIntrinsicObjectBuilder.newInstance(ecx, jsonObject);
			final EsIntrinsicObject result = builder.target;
			if (esoExtension != null) {
				extend(result, esoExtension);
			}
			if (ozptqtwDeletions != null) {
				delete(result, ozptqtwDeletions);
			}
			return result;
		}

		public method_newParameterObject() {
			super("newParameterObject", 0, CArg.extensions, CArg.deletions);
		}
	}

}
