/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.metservice.argon.ArgonApiException;
import com.metservice.argon.ArgonText;
import com.metservice.argon.json.IJsonNative;
import com.metservice.argon.json.JsonArray;
import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonString;

/**
 * @author roach
 */
public class EsArgumentAccessor {

	public EsObjectAccessor createObjectAccessor()
			throws InterruptedException {
		final EsObject osrc = esoObject();
		if (osrc == null) return null;
		return new EsObjectAccessor(acc.ecx, osrc, qPath);
	}

	public EsIntrinsicArray esIntrinsicArray()
			throws InterruptedException {
		return acc.esIntrinsicArray(index);
	}

	public EsObject esObject()
			throws InterruptedException {
		return acc.esObject(index);
	}

	public <T extends EsObject> T esObject(String esClass, Class<T> objectClass)
			throws InterruptedException {
		return acc.esObject(index, esClass, objectClass);
	}

	public EsIntrinsicArray esoIntrinsicArray()
			throws InterruptedException {
		return acc.esoIntrinsicArray(index);
	}

	public EsObject esoObject()
			throws InterruptedException {
		return acc.esoObject(index);
	}

	public <T extends EsObject> T esoObject(String esClass, Class<T> objectClass)
			throws InterruptedException {
		return acc.esoObject(index, esClass, objectClass);
	}

	public String exType(Throwable ex) {
		return acc.exType(index, ex);
	}

	public int intValue()
			throws InterruptedException {
		return acc.intValue(index);
	}

	public JsonArray jsonArray(boolean retainIndex)
			throws InterruptedException {
		return acc.esIntrinsicArray(index).newJsonArray(retainIndex);
	}

	public JsonObject jsonObject()
			throws InterruptedException {
		return acc.esObject(index).newJsonObject();
	}

	public JsonObject jsonObjectNullEmpty()
			throws InterruptedException {
		final EsObject esoObject = acc.esoObject(index);
		if (esoObject == null) return JsonObject.Empty;
		return esoObject.newJsonObject();
	}

	public JsonObject jsonObjectNullEmptyConvertString()
			throws InterruptedException {
		return jsonObjectNullEmptyConvertString(CProp.value);
	}

	public JsonObject jsonObjectNullEmptyConvertString(String oqccPropertyName)
			throws InterruptedException {
		if (oqccPropertyName == null) return jsonObjectNullEmpty();
		if (esType == EsType.TObject) return jsonObjectNullEmpty();
		final String zValue = acc.zStringValue(index);
		final Map<String, IJsonNative> map = new HashMap<String, IJsonNative>(1);
		map.put(oqccPropertyName, JsonString.newInstance(zValue));
		return JsonObject.newImmutable(map);
	}

	public JsonObject jsonoObject()
			throws InterruptedException {
		final EsObject esoObject = acc.esoObject(index);
		if (esoObject == null) return null;
		return esoObject.newJsonObject();
	}

	public EsObjectAccessor newObjectAccessor()
			throws InterruptedException {
		final EsObject src = esObject();
		return new EsObjectAccessor(acc.ecx, src, qPath);
	}

	public int[] ozptIntValuesEvery()
			throws InterruptedException {
		final EsIntrinsicArray oArray = esoIntrinsicArray();
		if (oArray == null) return null;
		final IEsOperand[] zptMembers = UNeon.zptOperandsEvery(acc.ecx, oArray);
		return UNeon.zptIntValuesEvery(acc.ecx, qPath, zptMembers);
	}

	public String[] ozptqtwStringValuesEvery()
			throws InterruptedException {
		final EsIntrinsicArray oArray = esoIntrinsicArray();
		if (oArray == null) return null;
		final IEsOperand[] zptMembers = UNeon.zptOperandsEvery(acc.ecx, oArray);
		return UNeon.zptqtwStringValuesEvery(acc.ecx, qPath, zptMembers);
	}

	public String qcctwFunctionName()
			throws InterruptedException {
		final EsObject esObject = acc.esObject(index);
		if (esObject instanceof EsFunction) {
			final EsFunction esf = (EsFunction) esObject;
			final String oqccFunctionName = esf.callable().oqccName();
			if (oqccFunctionName == null) {
				final String fpn = acc.formalParameterName(index);
				final String m = "Expecting a named function for formal parameter '" + fpn + "'";
				throw new EsTypeCodeException(m);
			}
			return oqccFunctionName;
		}
		final String qcctw = esObject.toCanonicalString(acc.ecx);
		try {
			return ArgonText.qtwEcmaName(qcctw);
		} catch (final ArgonApiException ex) {
			final String fpn = acc.formalParameterName(index);
			final String erm = ex.getMessage();
			final String m = "Expecting value of formal parameter '" + fpn + "' to be a well formed function name; " + erm;
			throw new EsTypeCodeException(m);
		}
	}

	public String qtwCanonicalLocalFileSystemPath()
			throws InterruptedException {
		final File f = new File(qtwStringValue());
		try {
			return f.getCanonicalPath();
		} catch (final IOException ex) {
			final String fpn = acc.formalParameterName(index);
			final String erm = ex.getMessage();
			final String m = "Expecting value of formal parameter '" + fpn + "' to be a well formed local file system path; "
					+ erm;
			throw new EsTypeCodeException(m);
		}
	}

	public String qtwPosixName()
			throws InterruptedException {
		try {
			return ArgonText.qtwPosixName(qtwStringValue());
		} catch (final ArgonApiException ex) {
			throw new EsTypeCodeException(exType(ex));
		}
	}

	public String qtwStringValue()
			throws InterruptedException {
		return acc.qtwStringValue(index);
	}

	@Override
	public String toString() {
		return qPath + " (" + esType + ")";
	}

	public List<String> xlzStringValuesEvery()
			throws InterruptedException {
		return Arrays.asList(xptzStringValuesEvery());
	}

	public String[] xptzStringValuesEvery()
			throws InterruptedException {
		final EsIntrinsicArray array = esIntrinsicArray();
		final IEsOperand[] xptMembers = UNeon.xptOperandsEvery(acc.ecx, qPath, array);
		return UNeon.zptzStringValuesEvery(acc.ecx, qPath, xptMembers);
	}

	public String[] zptqtwStringValueOnlyDistinct(boolean elipsis)
			throws InterruptedException {
		final Set<String> zsqtw = new HashSet<String>();
		final int lastIndex = elipsis ? acc.argc - 1 : index;
		for (int i = index; i <= lastIndex; i++) {
			final IEsOperand operand = acc.args.operand(i);
			if (operand instanceof EsIntrinsicArray) {
				final EsIntrinsicArray arrayOperand = (EsIntrinsicArray) operand;
				final IEsOperand[] zptMembers = UNeon.zptOperandsOnly(acc.ecx, arrayOperand, true, true);
				final String[] zptqtw = UNeon.zptqtwStringValuesOnly(acc.ecx, zptMembers);
				for (int m = 0; m < zptqtw.length; m++) {
					zsqtw.add(zptqtw[m]);
				}
			} else {
				final String ztw = UNeon.zStringValue(acc.ecx, operand).trim();
				if (ztw.length() > 0) {
					zsqtw.add(ztw);
				}
			}
		}
		final String[] zptqtwAsc = zsqtw.toArray(new String[zsqtw.size()]);
		Arrays.sort(zptqtwAsc);
		return zptqtwAsc;
	}

	public String[] zptqtwStringValuesEvery()
			throws InterruptedException {
		final EsIntrinsicArray array = esIntrinsicArray();
		final IEsOperand[] zptMembers = UNeon.zptOperandsEvery(acc.ecx, array);
		return UNeon.zptqtwStringValuesEvery(acc.ecx, qPath, zptMembers);
	}

	public String ztwStringValue()
			throws InterruptedException {
		return acc.ztwStringValue(index);
	}

	EsArgumentAccessor(EsMethodAccessor accessor, int index, String qPath) {
		if (accessor == null) throw new IllegalArgumentException("object is null");
		if (qPath == null || qPath.length() == 0) throw new IllegalArgumentException("string is null or empty");
		this.acc = accessor;
		this.index = index;
		this.qPath = qPath;
		this.esType = accessor.esType(index);
	}

	private final EsMethodAccessor acc;
	public final int index;
	public final String qPath;
	public final EsType esType;
}
