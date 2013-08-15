/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;

import javax.servlet.ServletInputStream;

import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.server.Request;

import com.metservice.argon.ArgonFormatException;
import com.metservice.argon.ArgonQuotaException;
import com.metservice.argon.ArgonStreamReadException;
import com.metservice.argon.ArgonText;
import com.metservice.argon.Binary;
import com.metservice.argon.CArgon;
import com.metservice.argon.DateFactory;
import com.metservice.argon.ElapsedFactory;
import com.metservice.argon.json.JsonArray;
import com.metservice.argon.json.JsonObject;

/**
 * @author roach
 */
class UBeryllium {

	private static final String ReservedValue_null = "null";
	private static final String TypeSuffix_Name = "Name";
	private static final String TypeSuffix_Time = "Time";
	private static final String TypeSuffix_Elapsed = "Elapsed";
	private static final String TypeSuffix_Count = "Count";
	private static final String TypeSuffix_Flag = "Flag";
	private static final long DefaultContentLength = 4 * CArgon.K;

	private static void addJson(JsonObject dst, String qName, Object value)
			throws BerylliumHttpBadRequestException {
		if (value instanceof Object[]) {
			addJsonArray(dst, qName, (Object[]) value);
		} else {
			addJsonSingle(dst, qName, value);
		}
	}

	private static void addJsonArray(JsonObject dst, String qName, Object[] zptValues)
			throws BerylliumHttpBadRequestException {
		if (zptValues.length == 0) return;
		if (zptValues.length == 1) {
			addJsonSingle(dst, qName, zptValues[0]);
		} else {
			final DecodeRule decodeRule = newDecodeRule(qName);
			final JsonArray array = JsonArray.newMutable(zptValues.length);
			for (int i = 0; i < zptValues.length; i++) {
				final Object oval = zptValues[i];
				if (oval != null) {
					addJsonValue(decodeRule, array, oval.toString());
				}
			}
			dst.put(qName, array);
		}
	}

	private static void addJsonSingle(JsonObject dst, String qName, Object oValue)
			throws BerylliumHttpBadRequestException {
		assert dst != null;
		assert qName != null && qName.length() > 0;
		if (oValue != null) {
			addJsonValue(dst, qName, oValue.toString());
		}
	}

	private static void addJsonValue(DecodeRule decodeRule, JsonArray dst, String zValue)
			throws BerylliumHttpBadRequestException {
		if (zValue.equals(ReservedValue_null)) {
			dst.addNull();
		} else {
			decodeRule.add(dst, zValue);
		}
	}

	private static void addJsonValue(JsonObject dst, String qName, String zValue)
			throws BerylliumHttpBadRequestException {
		if (zValue.equals(ReservedValue_null)) {
			dst.putNull(qName);
		} else {
			final DecodeRule decodeRule = newDecodeRule(qName);
			decodeRule.put(dst, zValue);
		}
	}

	static DecodeRule newDecodeRule(String qName) {
		assert qName != null;
		final int posType = qName.indexOf('$');
		if (posType <= 0) return new DecodeRuleDefault(qName);
		final String qBase = qName.substring(0, posType);
		final String zType = qName.substring(posType + 1);
		final String pname = qBase + zType;
		if (zType.equals(TypeSuffix_Time)) return new DecodeRuleTime(pname);
		if (zType.equals(TypeSuffix_Name)) return new DecodeRuleName(pname);
		if (zType.equals(TypeSuffix_Count)) return new DecodeRuleCount(pname);
		if (zType.equals(TypeSuffix_Elapsed)) return new DecodeRuleElapsed(pname);
		if (zType.equals(TypeSuffix_Flag)) return new DecodeRuleFlag(pname);
		return new DecodeRuleDefault(qName);
	}

	public static Binary readBinaryInput(Request rq, int bcQuota)
			throws ArgonQuotaException, ArgonStreamReadException, IOException {
		if (rq == null) throw new IllegalArgumentException("object is null");
		final String sourceName = rq.getPathInfo();
		final String ozContentLength = rq.getHeader(HttpHeaders.CONTENT_LENGTH);
		final Long oContentLength = ArgonText.parseLong(ozContentLength, null);
		final long bcContentLength = oContentLength == null ? DefaultContentLength : oContentLength.longValue();
		final ServletInputStream ins = rq.getInputStream();
		return Binary.newFromInputStream(ins, bcContentLength, sourceName, bcQuota);
	}

	public static JsonObject transformParameterMapToJson(Request rq)
			throws BerylliumHttpBadRequestException {
		Map<?, ?> oparameterMap = null;
		try {
			oparameterMap = rq.getParameterMap();
		} catch (final RuntimeException ex) {
		}
		if (oparameterMap == null || oparameterMap.isEmpty()) return JsonObject.Empty;
		final int keyCount = oparameterMap.size();
		final JsonObject neo = JsonObject.newMutable(keyCount);
		for (final Object key : oparameterMap.keySet()) {
			if (key == null) {
				continue;
			}
			final Object oValue = oparameterMap.get(key);
			if (oValue == null) {
				continue;
			}
			final String ozName = key.toString();
			if (ozName == null || ozName.length() == 0) {
				continue;
			}
			addJson(neo, ozName, oValue);
		}
		return neo;
	}

	public static String w3c_form_urlencoded(String ozValue, Charset charset) {
		if (ozValue == null || ozValue.length() == 0) return "";
		if (charset == null) throw new IllegalArgumentException("object is null");
		try {
			return URLEncoder.encode(ozValue, ArgonText.charsetName(charset));
		} catch (final UnsupportedEncodingException ex) {
			throw new UnsupportedOperationException("No UTF-8 support", ex);
		}
	}

	private UBeryllium() {
	}

	private static abstract class DecodeRule {

		public abstract void add(JsonArray dst, String zValue)
				throws BerylliumHttpBadRequestException;

		public abstract void put(JsonObject dst, String zValue)
				throws BerylliumHttpBadRequestException;

		protected DecodeRule(String pname) {
			assert pname != null && pname.length() > 0;
			this.pname = pname;
		}
		public final String pname;
	}

	static class DecodeRuleCount extends DecodeRule {

		@Override
		public void add(JsonArray dst, String zValue)
				throws BerylliumHttpBadRequestException {
			final String ztwValue = zValue.trim();
			if (ztwValue.length() == 0) {
				dst.addNull();
			} else {
				try {
					dst.addInteger(Integer.parseInt(ztwValue));
				} catch (final NumberFormatException ex) {
					throw new BerylliumHttpBadRequestException(ex, "integer", pname);
				}
			}
		}

		@Override
		public final void put(JsonObject dst, String zValue)
				throws BerylliumHttpBadRequestException {
			final String ztwValue = zValue.trim();
			if (ztwValue.length() == 0) {
				dst.putNull(pname);
			} else {
				try {
					dst.putInteger(pname, Integer.parseInt(ztwValue));
				} catch (final NumberFormatException ex) {
					throw new BerylliumHttpBadRequestException(ex, "integer", pname);
				}
			}
		}

		public DecodeRuleCount(String pname) {
			super(pname);
		}
	}

	static class DecodeRuleDefault extends DecodeRule {

		@Override
		public void add(JsonArray dst, String zValue) {
			dst.addString(zValue);
		}

		@Override
		public final void put(JsonObject dst, String zValue)
				throws BerylliumHttpBadRequestException {
			dst.putString(pname, zValue);
		}

		public DecodeRuleDefault(String pname) {
			super(pname);
		}
	}

	static class DecodeRuleElapsed extends DecodeRule {

		@Override
		public void add(JsonArray dst, String zValue)
				throws BerylliumHttpBadRequestException {
			final String ztwValue = zValue.trim();
			if (ztwValue.length() == 0) {
				dst.addNull();
			} else {
				try {
					dst.addElapsed(ElapsedFactory.newElapsed(ztwValue));
				} catch (final ArgonFormatException ex) {
					throw new BerylliumHttpBadRequestException(ex, pname);
				}
			}
		}

		@Override
		public final void put(JsonObject dst, String zValue)
				throws BerylliumHttpBadRequestException {
			final String ztwValue = zValue.trim();
			if (ztwValue.length() == 0) {
				dst.putNull(pname);
			} else {
				try {
					dst.putElapsed(pname, ElapsedFactory.newElapsed(ztwValue));
				} catch (final ArgonFormatException ex) {
					throw new BerylliumHttpBadRequestException(ex, pname);
				}
			}
		}

		public DecodeRuleElapsed(String pname) {
			super(pname);
		}
	}

	static class DecodeRuleFlag extends DecodeRule {

		@Override
		public void add(JsonArray dst, String zValue)
				throws BerylliumHttpBadRequestException {
			final String ztwValue = zValue.trim();
			if (ztwValue.length() == 0) {
				dst.addNull();
			} else {
				dst.addBoolean(Boolean.parseBoolean(ztwValue));
			}
		}

		@Override
		public final void put(JsonObject dst, String zValue)
				throws BerylliumHttpBadRequestException {
			final String ztwValue = zValue.trim();
			if (ztwValue.length() == 0) {
				dst.putNull(pname);
			} else {
				dst.putBoolean(pname, Boolean.parseBoolean(ztwValue));
			}
		}

		public DecodeRuleFlag(String pname) {
			super(pname);
		}
	}

	static class DecodeRuleName extends DecodeRule {

		@Override
		public void add(JsonArray dst, String zValue)
				throws BerylliumHttpBadRequestException {
			final String ztwValue = zValue.trim();
			if (ztwValue.length() == 0) {
				dst.addNull();
			} else {
				dst.addString(ztwValue);
			}
		}

		@Override
		public final void put(JsonObject dst, String zValue)
				throws BerylliumHttpBadRequestException {
			final String ztwValue = zValue.trim();
			if (ztwValue.length() == 0) {
				dst.putNull(pname);
			} else {
				dst.putString(pname, ztwValue);
			}
		}

		public DecodeRuleName(String pname) {
			super(pname);
		}
	}

	static class DecodeRuleTime extends DecodeRule {

		@Override
		public void add(JsonArray dst, String zValue)
				throws BerylliumHttpBadRequestException {
			final String ztwValue = zValue.trim();
			if (ztwValue.length() == 0) {
				dst.addNull();
			} else {
				try {
					dst.addTime(DateFactory.newTsFromT8(ztwValue));
				} catch (final ArgonFormatException ex) {
					throw new BerylliumHttpBadRequestException(ex, pname);
				}
			}
		}

		@Override
		public final void put(JsonObject dst, String zValue)
				throws BerylliumHttpBadRequestException {
			final String ztwValue = zValue.trim();
			if (ztwValue.length() == 0) {
				dst.putNull(pname);
			} else {
				try {
					dst.putTime(pname, DateFactory.newTsFromT8(ztwValue));
				} catch (final ArgonFormatException ex) {
					throw new BerylliumHttpBadRequestException(ex, pname);
				}
			}
		}

		public DecodeRuleTime(String pname) {
			super(pname);
		}
	}

}
