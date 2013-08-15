/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.metservice.argon.json.IJsonNative;
import com.metservice.argon.json.IJsonString;
import com.metservice.argon.json.JsonString;
import com.metservice.argon.json.JsonType;

/**
 * 
 * @author roach
 */
public class EsIntrinsicRegExp extends EsObject implements IJsonString {

	private static final Pattern ANY = Pattern.compile(".*");

	private String newSerial(Pattern pattern) {
		final int flags = m_pattern.flags();
		if (flags == 0) return m_pattern.pattern();

		final boolean ignoreCase = (flags & Pattern.CASE_INSENSITIVE) == Pattern.CASE_INSENSITIVE;
		final boolean multiline = (flags & Pattern.MULTILINE) == Pattern.MULTILINE;
		final StringBuilder bflags = new StringBuilder();
		if (ignoreCase) {
			bflags.append("i");
		}
		if (multiline) {
			bflags.append("m");
		}
		final StringBuilder sb = new StringBuilder();
		if (bflags.length() > 0) {
			sb.append("(?");
			sb.append(bflags.toString());
			sb.append(")");
		}
		sb.append(m_pattern.pattern());
		return sb.toString();
	}

	@Override
	protected void cascadeUpdate(String qccPropertyName, IEsOperand neoPropertyValue) {
	}

	@Override
	protected void loadProperties(EsExecutionContext ecx) {
	}

	public EsIntrinsicArray capture(EsExecutionContext ecx, String zs) {
		final EsIntrinsicArray array = ecx.global().newIntrinsicArray();
		final Matcher matcher = m_pattern.matcher(zs);
		int findex = 0;
		while (matcher.find()) {
			final int groupCount = matcher.groupCount();
			final EsIntrinsicArray ga = ecx.global().newIntrinsicArray();
			ga.setLength(groupCount + 1);
			for (int i = 0; i <= groupCount; i++) {
				ga.putByIndex(i, EsPrimitiveString.newInstance(matcher.group(i)));
			}
			array.putByIndex(findex, ga);
			findex++;
		}
		return array;
	}

	@Override
	public IJsonNative createJsonNative() {
		return JsonString.newInstance(jsonDatum());
	}

	@Override
	public EsObject createObject() {
		return new EsIntrinsicRegExp(this);
	}

	@Override
	public String esClass() {
		return EsIntrinsicRegExpConstructor.ClassName;
	}

	@Override
	public JsonType getJsonType() {
		return JsonType.TString;
	}

	@Override
	public String jsonDatum() {
		return newSerial(m_pattern);
	}

	public Pattern pattern() {
		return m_pattern;
	}

	public void setValue(Pattern pattern) {
		if (pattern == null) throw new IllegalArgumentException("object is null");
		m_pattern = pattern;
	}

	@Override
	public String show(int depth) {
		final StringBuilder b = new StringBuilder();
		b.append('/');
		b.append(m_pattern.pattern());
		b.append('/');
		final int flags = m_pattern.flags();
		final boolean ignoreCase = (flags & Pattern.CASE_INSENSITIVE) == Pattern.CASE_INSENSITIVE;
		final boolean multiline = (flags & Pattern.MULTILINE) == Pattern.MULTILINE;
		if (ignoreCase) {
			b.append('i');
		}
		if (multiline) {
			b.append('m');
		}
		return b.toString();
	}

	public EsIntrinsicArray split(EsExecutionContext ecx, String zs, boolean trim, boolean retainEmpty) {
		final String[] xptz = m_pattern.split(zs);
		final List<IEsOperand> zl = new ArrayList<IEsOperand>(xptz.length);
		for (int i = 0; i < xptz.length; i++) {
			final String z = xptz[i];
			final String zt = trim ? z.trim() : z;
			if (retainEmpty) {
				if (zt.length() == 0) {
					zl.add(EsPrimitiveString.EMPTY);
				} else {
					zl.add(new EsPrimitiveString(zt));
				}
			} else {
				if (zt.length() > 0) {
					zl.add(new EsPrimitiveString(zt));
				}
			}
		}
		return ecx.global().newIntrinsicArray(zl);
	}

	public EsPrimitiveString toPrimitiveString() {
		return new EsPrimitiveString(jsonDatum());
	}

	public EsIntrinsicRegExp(EsObject prototype) {
		super(prototype);
	}

	private Pattern m_pattern = ANY;
}
