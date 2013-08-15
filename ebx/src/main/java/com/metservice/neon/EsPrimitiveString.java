/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.metservice.argon.ArgonFormatException;
import com.metservice.argon.ArgonText;
import com.metservice.argon.Binary;
import com.metservice.argon.DateFactory;
import com.metservice.argon.ElapsedFactory;
import com.metservice.argon.Real;
import com.metservice.argon.TimeZoneFactory;
import com.metservice.argon.TimeZoneFormatter;
import com.metservice.argon.json.IJsonNative;
import com.metservice.argon.json.IJsonString;
import com.metservice.argon.json.JsonString;
import com.metservice.argon.json.JsonType;
import com.metservice.argon.text.ArgonSplitter;

/**
 * 
 * @author roach
 */
public final class EsPrimitiveString extends EsPrimitive implements IJsonString {

	private static final Pattern PatternBind = Pattern.compile("\\$\\{\\w+\\}");

	public static final EsPrimitiveString EMPTY = new EsPrimitiveString("");
	public static final EsPrimitiveString COMMA = new EsPrimitiveString(",");
	public static final EsPrimitiveString UTF8 = new EsPrimitiveString(ArgonText.CHARSET_NAME_UTF8);
	public static final EsPrimitiveString ISO8859 = new EsPrimitiveString(ArgonText.CHARSET_NAME_ISO8859);
	public static final EsPrimitiveString ASCII = new EsPrimitiveString(ArgonText.CHARSET_NAME_ASCII);
	public static final EsPrimitiveString GMT = new EsPrimitiveString(TimeZoneFactory.NAME_GMT);

	public static final Comparator<EsPrimitiveString> ComparatorNatural = new Comparator<EsPrimitiveString>() {

		@Override
		public int compare(EsPrimitiveString lhs, EsPrimitiveString rhs) {
			return lhs.zValue().compareTo(rhs.zValue());
		}
	};

	private static EsIntrinsicArray toArray(EsExecutionContext ecx, List<String> zlo) {
		final int len = zlo.size();
		final IEsOperand[] zValues = new IEsOperand[len];
		for (int i = 0; i < len; i++) {
			final String oz = zlo.get(i);
			if (oz != null) {
				zValues[i] = new EsPrimitiveString(oz);
			}
		}
		return ecx.global().newIntrinsicArray(zValues, len);
	}

	private static EsIntrinsicArray toArray(EsExecutionContext ecx, String[] zto) {
		final int len = zto.length;
		final IEsOperand[] zValues = new IEsOperand[len];
		for (int i = 0; i < len; i++) {
			final String oz = zto[i];
			if (oz != null) {
				zValues[i] = new EsPrimitiveString(oz);
			}
		}
		return ecx.global().newIntrinsicArray(zValues, len);
	}

	private static String ztwBindPname(String qccExpr) {
		final int len = qccExpr.length();
		final StringBuilder bname = new StringBuilder();
		for (int i = 0; i < len; i++) {
			final char ch = qccExpr.charAt(i);
			if (ArgonText.isEcmaName(ch, (i == 0))) {
				bname.append(ch);
			}
		}
		return bname.toString();
	}

	public static EsIntrinsicArray newArrayInstance(EsExecutionContext ecx, List<String> zlo) {
		if (zlo == null) throw new IllegalArgumentException("object is null");
		return toArray(ecx, zlo);
	}

	public static EsPrimitiveString newInstance(Charset oValue) {
		if (oValue == null) return EMPTY;
		final String name = ArgonText.charsetName(oValue);
		if (name.equals(ArgonText.CHARSET_NAME_UTF8)) return UTF8;
		if (name.equals(ArgonText.CHARSET_NAME_ISO8859)) return ISO8859;
		if (name.equals(ArgonText.CHARSET_NAME_ASCII)) return ASCII;
		return new EsPrimitiveString(name);
	}

	public static EsPrimitiveString newInstance(String ozValue) {
		if (ozValue == null || ozValue.length() == 0) return EMPTY;
		return new EsPrimitiveString(ozValue);
	}

	public static EsPrimitiveString newInstance(TimeZone oValue) {
		if (oValue == null) return EMPTY;
		final String name = TimeZoneFormatter.id(oValue);
		if (name.equals(TimeZoneFactory.NAME_GMT)) return GMT;
		return new EsPrimitiveString(name);
	}

	private String zExpandExpr(String zExprReplace, Matcher matcher) {
		if (!zExprReplace.contains("$")) return zExprReplace;

		final int len = zExprReplace.length();
		final StringBuilder b = new StringBuilder(len * 2);
		int pos = 0;
		while (pos < len) {
			final char ch = zExprReplace.charAt(pos);
			if (ch == '$') {
				pos++;
				if (pos < len) {
					final char dch = zExprReplace.charAt(pos);
					pos++;
					if (Character.isDigit(dch)) {
						final StringBuilder nb = new StringBuilder(2);
						nb.append(dch);
						if (pos < len) {
							final char ddch = zExprReplace.charAt(pos);
							if (Character.isDigit(ddch)) {
								pos++;
								nb.append(ddch);
							}
						}
						final int n = Integer.parseInt(nb.toString());
						final int m = matcher.groupCount();
						if (n <= m) {
							b.append(matcher.group(n));
						}
					} else if (dch == '$') {
						b.append('$');
					} else if (dch == '&') {
						b.append(matcher.group());
					} else if (dch == '`') {
						b.append(m_zValue.substring(0, matcher.start()));
					} else if (dch == '\'') {
						b.append(m_zValue.substring(matcher.end()));
					} else {
						b.append('$');
						b.append(dch);
					}
				} else {
					b.append('$');
				}
			} else {
				pos++;
				b.append(ch);
			}
		}

		return b.toString();
	}

	private String zExpandFn(EsExecutionContext ecx, EsFunction fnReplace, Matcher matcher)
			throws InterruptedException {
		final EsList argsReplace = new EsList();
		argsReplace.add(new EsPrimitiveString(matcher.group()));
		final int groupCount = matcher.groupCount();
		for (int g = 1; g <= groupCount; g++) {
			argsReplace.add(new EsPrimitiveString(matcher.group(g)));
		}
		argsReplace.add(new EsPrimitiveNumberInteger(matcher.start()));
		argsReplace.add(this);
		final EsActivation activation = EsActivation.newInstance(ecx.global(), fnReplace, argsReplace);
		final EsObject oThis = ecx.thisObject();
		final EsExecutionContext neoExecutionContext = ecx.newInstance(fnReplace, activation, oThis);
		final IEsCallable callable = fnReplace.callable();
		final IEsOperand callResult = callable.call(neoExecutionContext);
		return callResult.toCanonicalString(ecx);
	}

	public EsPrimitiveString bind(EsExecutionContext ecx, EsObject parameters, Pattern oBinder)
			throws InterruptedException {
		if (parameters == null) throw new IllegalArgumentException("object is null");
		final Pattern binder = oBinder == null ? PatternBind : oBinder;
		final Matcher matcher = binder.matcher(m_zValue);
		StringBuilder ob = null;
		int pos = 0;
		while (matcher.find()) {
			if (ob == null) {
				final int est = m_zValue.length() * 2;
				ob = new StringBuilder(est);
			}
			final int posStart = matcher.start();
			final int posEnd = matcher.end();
			ob.append(m_zValue.substring(pos, posStart));
			final String qccExpr = m_zValue.substring(posStart, posEnd);
			final String zccPropName = ztwBindPname(qccExpr);
			final String zccValue;
			if (zccPropName.length() > 0) {
				final IEsOperand pvalue = parameters.esGet(zccPropName);
				final EsType esvt = pvalue.esType();
				if (esvt.isDefined) {
					if (esvt.isDatum) {
						zccValue = pvalue.toCanonicalString(ecx);
					} else {
						zccValue = "";
					}
				} else {
					zccValue = qccExpr;
				}
			} else {
				zccValue = qccExpr;
			}
			ob.append(zccValue);
			pos = posEnd;
		}
		if (ob == null) return this;
		ob.append(m_zValue.substring(pos));
		return new EsPrimitiveString(ob.toString());
	}

	public EsPrimitiveString charAt(EsPrimitiveNumber pos) {
		final int ipos = pos.intVerified();
		if (ipos < 0 || ipos >= m_zValue.length()) return EMPTY;
		return new EsPrimitiveString(m_zValue.substring(ipos, ipos + 1));
	}

	public EsPrimitiveBoolean contains(String zSearchString, boolean ignoreCase) {
		final String zccValue = ignoreCase ? m_zValue.toLowerCase() : m_zValue;
		final String zccSearchString = ignoreCase ? zSearchString.toLowerCase() : zSearchString;
		return EsPrimitiveBoolean.instance(zccValue.contains(zccSearchString));
	}

	@Override
	public IJsonNative createJsonNative() {
		return JsonString.newInstance(m_zValue);
	}

	public EsPrimitiveBoolean endsWith(String zSearchString, boolean ignoreCase) {
		final String zccValue = ignoreCase ? m_zValue.toLowerCase() : m_zValue;
		final String zccSearchString = ignoreCase ? zSearchString.toLowerCase() : zSearchString;
		return EsPrimitiveBoolean.instance(zccValue.endsWith(zccSearchString));
	}

	public EsType esType() {
		return EsType.TString;
	}

	@Override
	public JsonType getJsonType() {
		return JsonType.TString;
	}

	public EsPrimitiveNumberInteger indexOf(EsPrimitiveString searchString) {
		return new EsPrimitiveNumberInteger(m_zValue.indexOf(searchString.m_zValue));
	}

	public EsPrimitiveNumberInteger indexOf(EsPrimitiveString searchString, EsPrimitiveNumber position) {
		final int length = m_zValue.length();
		final int iposition = position.intVerified();
		final int fromIndex = Math.min(Math.max(iposition, 0), length);
		return new EsPrimitiveNumberInteger(m_zValue.indexOf(searchString.m_zValue, fromIndex));
	}

	public boolean isLessThan(EsPrimitiveString rhsString) {
		return m_zValue.compareTo(rhsString.m_zValue) < 0;
	}

	@Override
	public String jsonDatum() {
		return m_zValue;
	}

	public EsPrimitiveNumberInteger lastIndexOf(EsPrimitiveString searchString) {
		return new EsPrimitiveNumberInteger(m_zValue.lastIndexOf(searchString.m_zValue));
	}

	public EsPrimitiveNumberInteger lastIndexOf(EsPrimitiveString searchString, EsPrimitiveNumber position) {
		final int length = m_zValue.length();
		final int iposition = position.intVerified();
		final int fromIndex = Math.min(Math.max(iposition, 0), length);
		return new EsPrimitiveNumberInteger(m_zValue.lastIndexOf(searchString.m_zValue, fromIndex));
	}

	public int length() {
		return m_zValue.length();
	}

	public EsIntrinsicArray newMatchArray(EsExecutionContext ecx, Pattern pattern) {
		final List<String> matchList = new ArrayList<String>();
		final Matcher matcher = pattern.matcher(m_zValue);
		while (matcher.find()) {
			matchList.add(matcher.group());
		}
		return toArray(ecx, matchList);
	}

	public String qValue() {
		if (m_zValue.length() == 0) throw new IllegalStateException("Empty");
		return m_zValue;
	}

	public EsPrimitiveString replace(EsExecutionContext ecx, Pattern pattern, boolean global, EsFunction ofnReplace,
			String ozExprReplace)
			throws InterruptedException {
		final Matcher matcher = pattern.matcher(m_zValue);
		final int len = m_zValue.length();
		final StringBuilder b = new StringBuilder(len * 2);
		boolean firstMatch = true;
		int pos = 0;
		while (matcher.find() && (global || firstMatch)) {
			b.append(m_zValue.substring(pos, matcher.start()));
			if (ofnReplace != null) {
				b.append(zExpandFn(ecx, ofnReplace, matcher));
			} else if (ozExprReplace != null) {
				b.append(zExpandExpr(ozExprReplace, matcher));
			}
			pos = matcher.end();
			firstMatch = false;
		}
		b.append(m_zValue.substring(pos));
		return new EsPrimitiveString(b.toString());
	}

	public boolean sameStringValue(EsPrimitiveString rhsString) {
		return m_zValue.equals(rhsString.m_zValue);
	}

	public EsPrimitiveNumberInteger search(EsExecutionContext ecx, Pattern pattern) {
		final Matcher matcher = pattern.matcher(m_zValue);
		if (matcher.find()) return new EsPrimitiveNumberInteger(matcher.start());
		return EsPrimitiveNumberInteger.MINUSONE;
	}

	public String show(int depth) {
		return m_zValue;
	}

	public EsPrimitiveString slice(EsPrimitiveNumber start) {
		final int length = m_zValue.length();
		final int istart = start.intVerified();
		final int beginIndex = istart < 0 ? Math.max(length + istart, 0) : Math.min(istart, length);
		return new EsPrimitiveString(m_zValue.substring(beginIndex));
	}

	public EsPrimitiveString slice(EsPrimitiveNumber start, EsPrimitiveNumber end) {
		final int length = m_zValue.length();
		final int istart = start.intVerified();
		final int iend = end.intVerified();
		final int cstart = istart < 0 ? Math.max(length + istart, 0) : Math.min(istart, length);
		final int cend = iend < 0 ? Math.max(length + iend, 0) : Math.min(iend, length);
		final int beginIndex = cstart;
		final int endIndex = cstart + Math.max(cend - cstart, 0);
		return new EsPrimitiveString(m_zValue.substring(beginIndex, endIndex));
	}

	public EsIntrinsicArray split(EsExecutionContext ecx, Pattern pattern, int limit) {
		final List<String> segList = new ArrayList<String>();
		final Matcher matcher = pattern.matcher(m_zValue);
		final int valueLength = m_zValue.length();
		if (valueLength == 0) {
			if (!matcher.find()) {
				segList.add(m_zValue);
			}
			return toArray(ecx, segList);
		}

		int segStart = 0;
		while (matcher.find()) {
			if (segList.size() < limit) {
				final String seg = m_zValue.substring(segStart, matcher.start());
				segList.add(seg);
				segStart = matcher.end();
			}
			final int groupCount = matcher.groupCount();
			for (int g = 1; g <= groupCount && segList.size() < limit; g++) {
				segList.add(matcher.group(g));
			}
		}
		if (segList.size() < limit) {
			final String seg = m_zValue.substring(segStart, valueLength);
			segList.add(seg);
		}

		return toArray(ecx, segList);
	}

	public EsIntrinsicArray split(EsExecutionContext ecx, String zSeparator, int limit) {
		final Pattern pattern = Pattern.compile(zSeparator, Pattern.LITERAL);
		return split(ecx, pattern, limit);
	}

	public EsPrimitiveBoolean startsWith(String zSearchString, boolean ignoreCase, int position) {
		final String zccValue = ignoreCase ? m_zValue.toLowerCase() : m_zValue;
		final String zccSearchString = ignoreCase ? zSearchString.toLowerCase() : zSearchString;
		return EsPrimitiveBoolean.instance(zccValue.startsWith(zccSearchString, position));
	}

	public EsPrimitiveString substring(EsPrimitiveNumber start) {
		final int length = m_zValue.length();
		final int istart = start.intVerified();
		final int beginIndex = Math.min(Math.max(istart, 0), length);
		return new EsPrimitiveString(m_zValue.substring(beginIndex));
	}

	public EsPrimitiveString substring(EsPrimitiveNumber start, EsPrimitiveNumber end) {
		final int length = m_zValue.length();
		final int istart = start.intVerified();
		final int iend = end.intVerified();
		final int cstart = Math.min(Math.max(istart, 0), length);
		final int cend = Math.min(Math.max(iend, 0), length);
		final int beginIndex = Math.min(cstart, cend);
		final int endIndex = Math.max(cstart, cend);
		return new EsPrimitiveString(m_zValue.substring(beginIndex, endIndex));
	}

	public EsPrimitiveString tidySuffix(String qSuffix, boolean ensure) {
		final boolean hasSuffix = m_zValue.endsWith(qSuffix);
		if (!hasSuffix && ensure) return new EsPrimitiveString(m_zValue + qSuffix);
		if (hasSuffix && !ensure) {
			final int end = m_zValue.length() - qSuffix.length();
			return new EsPrimitiveString(m_zValue.substring(0, end));
		}
		return this;
	}

	public EsIntrinsicBinary toBinary(EsExecutionContext ecx, Charset charset) {
		if (charset == null) throw new IllegalArgumentException("object is null");
		return ecx.global().newIntrinsicBinary(Binary.newFromString(charset, m_zValue));
	}

	public boolean toCanonicalBoolean() {
		return m_zValue.length() > 0;
	}

	@Override
	public String toCanonicalString() {
		return m_zValue;
	}

	public EsIntrinsicArray toFields(EsExecutionContext ecx, char delimiter) {
		return toArray(ecx, ArgonSplitter.zptzDelimited(m_zValue, delimiter));
	}

	@Override
	public int toHash() {
		return m_zValue.hashCode();
	}

	public EsIntrinsicArray toLines(EsExecutionContext ecx, boolean trim, boolean retainBlankLines, Pattern oInPattern,
			Pattern oExPattern) {
		return toArray(ecx, ArgonSplitter.zptzLines(m_zValue, trim, retainBlankLines, oInPattern, oExPattern));
	}

	public EsPrimitiveString toLowerCase() {
		return new EsPrimitiveString(m_zValue.toLowerCase());
	}

	public EsPrimitiveNumber toNumber(EsExecutionContext ecx) {
		final String ztw = m_zValue.trim();
		final int c = ztw.length();
		if (c == 0) return EsPrimitiveNumberInteger.ZERO;

		try {
			return new EsPrimitiveNumberInteger(Integer.parseInt(m_zValue));
		} catch (final NumberFormatException ex) {
		}

		try {
			return new EsPrimitiveNumberDouble(Double.parseDouble(m_zValue));
		} catch (final NumberFormatException ex) {
		}

		if (DateFactory.isWellFormedTX(m_zValue)) {
			try {
				return new EsPrimitiveNumberTime(DateFactory.newTsFromTX(m_zValue));
			} catch (final ArgonFormatException ex) {
			}
		}

		if (ElapsedFactory.isWellFormed(m_zValue)) {
			try {
				return new EsPrimitiveNumberElapsed(ElapsedFactory.ms(m_zValue));
			} catch (final ArgonFormatException ex) {
			}
		}

		if (Real.isWellFormed(m_zValue)) {
			try {
				return new EsPrimitiveNumberReal(Real.newInstance(m_zValue));
			} catch (final ArgonFormatException ex) {
			}
		}
		return EsPrimitiveNumberNot.Instance;
	}

	public EsObject toObject(EsExecutionContext ecx) {
		return ecx.global().newIntrinsicString(this);
	}

	public EsPrimitiveString toUpperCase() {
		return new EsPrimitiveString(m_zValue.toUpperCase());
	}

	public EsPrimitiveString trim() {
		return new EsPrimitiveString(m_zValue.trim());
	}

	public String zValue() {
		return m_zValue;
	}

	public EsPrimitiveString(Enum<?> value) {
		if (value == null) throw new IllegalArgumentException("object is null");
		m_zValue = value.name();
	}

	public EsPrimitiveString(Object value) {
		if (value == null) throw new IllegalArgumentException("object is null");
		m_zValue = value.toString();
	}

	public EsPrimitiveString(String zValue) {
		if (zValue == null) throw new IllegalArgumentException("zValue is null");
		m_zValue = zValue;
	}

	private final String m_zValue;
}
