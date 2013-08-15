/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.nio.charset.Charset;
import java.util.TimeZone;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.metservice.argon.ArgonApiException;
import com.metservice.argon.ArgonFormatException;
import com.metservice.argon.ArgonText;
import com.metservice.argon.Binary;
import com.metservice.argon.Elapsed;
import com.metservice.argon.EnumDecoder;
import com.metservice.argon.TimeMask;
import com.metservice.argon.TimeOfDayFactory;
import com.metservice.argon.TimeOfDayRule;
import com.metservice.argon.TimeZoneFactory;
import com.metservice.argon.json.JsonObject;
import com.metservice.neon.EsPrimitiveNumber.SubType;

/**
 * @author roach
 */
class UNeon {

	private static EsObject[] zpt_zp(EsObject[] zp, int len) {
		final int exCap = zp.length;
		if (exCap == len) return zp;
		final EsObject[] zpt = new EsObject[len];
		System.arraycopy(zp, 0, zpt, 0, len);
		return zpt;
	}

	private static IEsOperand[] zpt_zp(IEsOperand[] zp, int len) {
		final int exCap = zp.length;
		if (exCap == len) return zp;
		final IEsOperand[] zpt = new IEsOperand[len];
		System.arraycopy(zp, 0, zpt, 0, len);
		return zpt;
	}

	private static String[] zpt_zp(String[] zp, int len) {
		final int exCap = zp.length;
		if (exCap == len) return zp;
		final String[] zpt = new String[len];
		System.arraycopy(zp, 0, zpt, 0, len);
		return zpt;
	}

	public static EsIntrinsicRegExp esIntrinsicRegexp(EsExecutionContext ecx, String qPath, IEsOperand nonnull)
			throws InterruptedException {
		if (nonnull == null) throw new IllegalArgumentException("object is null");
		if (nonnull instanceof EsIntrinsicRegExp) return (EsIntrinsicRegExp) nonnull;
		final String zValue = nonnull.toCanonicalString(ecx);
		try {
			return ecx.global().newIntrinsicRegExp(Pattern.compile(zValue));
		} catch (final PatternSyntaxException ex) {
			throw new EsTypeCodeException("Value of " + qPath + " is not a valid regular expression: " + ex.getMessage());
		}
	}

	public static EsIntrinsicTimemask esIntrinsicTimemask(EsExecutionContext ecx, String qPath, IEsOperand nonnull)
			throws InterruptedException {
		if (nonnull == null) throw new IllegalArgumentException("object is null");
		if (nonnull instanceof EsIntrinsicTimemask) return (EsIntrinsicTimemask) nonnull;
		final String zValue = nonnull.toCanonicalString(ecx);
		try {
			return ecx.global().newIntrinsicTimemask(TimeMask.newInstance(zValue));
		} catch (final ArgonApiException ex) {
			throw new EsTypeCodeException("Value of " + qPath + " is not a valid time mask: " + ex.getMessage());
		}
	}

	public static IEsOperand esoOperand(IEsOperand operand, boolean published, boolean defined, boolean nonnull) {
		if (operand == null) throw new IllegalArgumentException("object is null");
		final EsType t = operand.esType();
		if (published && !t.isPublished) return null;
		if (defined && t == EsType.TUndefined) return null;
		if (nonnull && t == EsType.TNull) return null;
		return operand;
	}

	public static IEsOperand esOperand(String qPath, IEsOperand operand, boolean published, boolean defined, boolean nonnull) {
		if (qPath == null) throw new IllegalArgumentException("object is null");
		if (operand == null) throw new IllegalArgumentException("object is null");
		final EsType t = operand.esType();
		if (published && !t.isPublished) {
			final String m = "Expecting a published type for " + qPath + "; actual type is '" + t + "'";
			throw new EsTypeCodeException(m);
		}
		if (defined && t == EsType.TUndefined) {
			final String m = "Value of " + qPath + " is undefined; expecting a value";
			throw new EsTypeCodeException(m);
		}
		if (nonnull && t == EsType.TNull) {
			final String m = "Value of " + qPath + " is null; expecting a non-null value";
			throw new EsTypeCodeException(m);
		}
		return operand;
	}

	public static IEsOperand esoproperty(EsObject container, String zccKey, boolean defined, boolean nonnull) {
		if (container == null) throw new IllegalArgumentException("object is null");
		if (zccKey == null) throw new IllegalArgumentException("object is null");
		final IEsOperand operand = container.esGet(zccKey);
		final EsType t = operand.esType();
		if (!t.isPublished) return null;
		if (defined && t == EsType.TUndefined) return null;
		if (nonnull && t == EsType.TNull) return null;
		return operand;
	}

	public static IEsOperand esproperty(EsObject container, String zccKey, boolean defined, boolean nonnull) {
		if (container == null) throw new IllegalArgumentException("object is null");
		if (zccKey == null) throw new IllegalArgumentException("object is null");
		final IEsOperand operand = container.esGet(zccKey);
		final EsType t = operand.esType();
		if (!t.isPublished) {
			final String m = "Expecting a published type for property '" + zccKey + "'; actual type is '" + t + "'";
			throw new EsTypeCodeException(m);
		}
		if (defined && t == EsType.TUndefined) {
			final String m = "Value of property '" + zccKey + "' is undefined; expecting a value";
			throw new EsTypeCodeException(m);
		}
		if (nonnull && t == EsType.TNull) {
			final String m = "Value of property '" + zccKey + "' is null; expecting a non-null value";
			throw new EsTypeCodeException(m);
		}
		return operand;
	}

	public static IEsOperand espropertyByIndex(EsObject container, int index) {
		if (container == null) throw new IllegalArgumentException("object is null");
		return container.esGet(toPropertyName(index));
	}

	public static float floatVerified(double value) {
		final double avalue = Math.abs(value);
		if (avalue < Float.MIN_VALUE || avalue > Float.MAX_VALUE) {
			final String m = "Magnitude of double value (" + value + ") exceeds range limit";
			throw new EsTypeCodeException(m);
		}
		return (float) value;
	}

	public static int intNonNegativeVerified(String zccPropertyKey, IEsOperand value) {
		if (value == null) throw new IllegalArgumentException("object is null");
		if (value instanceof EsPrimitiveNumber) {
			final EsPrimitiveNumber valueNumber = (EsPrimitiveNumber) value;
			if (!valueNumber.isNaN()) {
				final int ivalue = valueNumber.intVerified();
				if (ivalue >= 0) return ivalue;
			}
		}
		final String m = "Property '" + zccPropertyKey + "' value '" + value.show(1) + "' is not countable";
		throw new EsRangeCodeException(m);
	}

	public static int intVerified(double value) {
		return intVerified(Math.round(value));
	}

	public static int intVerified(long value) {
		if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
			final String m = "Magnitude of integer value (" + value + ") exceeds range limit";
			throw new EsTypeCodeException(m);
		}
		return (int) value;
	}

	public static boolean isLengthProperty(String zccPropertyKey) {
		return zccPropertyKey.equals(EsObject.PropertyName_length);
	}

	public static int length(EsExecutionContext ecx, EsObject object)
			throws InterruptedException {
		if (object == null) throw new IllegalArgumentException("object is null");
		if (object instanceof EsIntrinsicArray) return ((EsIntrinsicArray) object).length();
		return object.esGet(EsObject.PropertyName_length).toNumber(ecx).uint31Verified();
	}

	public static int lengthNonZero(EsExecutionContext ecx, String qPath, EsObject object)
			throws InterruptedException {
		final int length = length(ecx, object);
		if (length == 0) {
			final String m = "The length of " + qPath + " is zero; expecting a non-zero length";
			throw new EsApiCodeException(m);
		}
		return length;
	}

	public static Pattern pattern(EsExecutionContext ecx, String qPath, IEsOperand nonnull)
			throws InterruptedException {
		if (nonnull == null) throw new IllegalArgumentException("object is null");
		if (nonnull instanceof EsIntrinsicRegExp) return ((EsIntrinsicRegExp) nonnull).pattern();
		final String zValue = nonnull.toCanonicalString(ecx);
		try {
			return Pattern.compile(zValue);
		} catch (final PatternSyntaxException ex) {
			throw new EsTypeCodeException("Value of " + qPath + " is not a valid regular expression: " + ex.getMessage());
		}
	}

	public static Binary property_binary(EsExecutionContext ecx, EsObject container, String zccKey)
			throws InterruptedException {
		final EsIntrinsicBinary esBinary = property_esObject(ecx, container, zccKey, EsIntrinsicBinaryConstructor.ClassName,
				EsIntrinsicBinary.class);
		return esBinary.value();
	}

	public static boolean property_boolean(EsObject container, String zccKey) {
		return esproperty(container, zccKey, true, true).toCanonicalBoolean();
	}

	public static Charset property_charset(EsExecutionContext ecx, EsObject container, String zccKey)
			throws InterruptedException {
		final String qtwName = property_qtwString(ecx, container, zccKey);
		try {
			return ArgonText.selectCharset(qtwName);
		} catch (final ArgonApiException ex) {
			throw new EsTypeCodeException(ex);
		}
	}

	public static Charset property_charset(EsExecutionContext ecx, EsObject container, String zccName, Charset whenEmpty)
			throws InterruptedException {
		if (whenEmpty == null) throw new IllegalArgumentException("object is null");
		final String oqtwName = property_oqtwString(ecx, container, zccName);
		if (oqtwName == null) return whenEmpty;
		try {
			return ArgonText.selectCharset(oqtwName);
		} catch (final ArgonApiException ex) {
			throw new EsTypeCodeException(ex);
		}
	}

	public static double property_double(EsExecutionContext ecx, EsObject container, String zccKey)
			throws InterruptedException {
		return esproperty(container, zccKey, true, true).toNumber(ecx).doubleValue();
	}

	public static Elapsed property_elapsed(EsExecutionContext ecx, EsObject container, String zccKey)
			throws InterruptedException {
		if (container == null) throw new IllegalArgumentException("object is null");
		final IEsOperand property = esproperty(container, zccKey, true, true);
		return Elapsed.newInstance(property.toNumber(ecx).longValue());
	}

	public static <E extends Enum<?>> E property_enum(EsExecutionContext ecx, EsObject container, String zccKey,
			EnumDecoder<E> decoder)
			throws InterruptedException {
		final String qtwName = property_qtwString(ecx, container, zccKey);
		try {
			return decoder.select(qtwName, false);
		} catch (final ArgonApiException ex) {
			throw new EsTypeCodeException(ex);
		}
	}

	public static EsObject property_esObject(EsExecutionContext ecx, EsObject container, String zccKey)
			throws InterruptedException {
		final IEsOperand nonNull = esproperty(container, zccKey, true, true);
		return nonNull.toObject(ecx);
	}

	public static <T extends EsObject> T property_esObject(EsExecutionContext ecx, EsObject container, String zccKey,
			String esClass, Class<T> objectClass)
			throws InterruptedException {
		final EsObject esobject = property_esObject(ecx, container, zccKey);
		if (objectClass.isInstance(esobject)) return objectClass.cast(esobject);
		final String acn = esobject.esClass();
		final String m = "Expecting a '" + esClass + "' type property '" + zccKey + "'; actual type is '" + acn + "'";
		throw new EsTypeCodeException(m);
	}

	public static float property_float(EsExecutionContext ecx, EsObject container, String zccKey)
			throws InterruptedException {
		return floatVerified(property_double(ecx, container, zccKey));
	}

	public static int property_int(EsExecutionContext ecx, EsObject container, String zccKey)
			throws InterruptedException {
		return intVerified(property_long(ecx, container, zccKey));
	}

	public static JsonObject property_jsonObject(EsExecutionContext ecx, EsObject container, String zccKey)
			throws InterruptedException {
		final IEsOperand oNonNull = esoproperty(container, zccKey, true, true);
		if (oNonNull == null) return JsonObject.Empty;
		final EsObject esobject = oNonNull.toObject(ecx);
		return esobject.newJsonObject();
	}

	public static long property_long(EsExecutionContext ecx, EsObject container, String zccKey)
			throws InterruptedException {
		return esproperty(container, zccKey, true, true).toNumber(ecx).longValue();
	}

	public static Elapsed property_oElapsed(EsExecutionContext ecx, EsObject container, String zccKey)
			throws InterruptedException {
		if (container == null) throw new IllegalArgumentException("object is null");
		final IEsOperand oProperty = esoproperty(container, zccKey, true, true);
		if (oProperty == null) return null;
		return Elapsed.newInstance(oProperty.toNumber(ecx).longValue());
	}

	public static <E extends Enum<?>> E property_oEnum(EsExecutionContext ecx, EsObject container, String zccKey,
			EnumDecoder<E> decoder)
			throws InterruptedException {
		final String oqtwName = property_oqtwString(ecx, container, zccKey);
		if (oqtwName == null) return null;
		try {
			return decoder.select(oqtwName, false);
		} catch (final ArgonApiException ex) {
			throw new EsTypeCodeException(ex);
		}
	}

	public static String property_oqtwString(EsExecutionContext ecx, EsObject container, String zccKey)
			throws InterruptedException {
		if (container == null) throw new IllegalArgumentException("object is null");
		final IEsOperand oProperty = esoproperty(container, zccKey, true, true);
		if (oProperty == null) return null;
		final String ztw = oProperty.toCanonicalString(ecx).trim();
		if (ztw.length() == 0) return null;
		return ztw;
	}

	public static String property_ozString(EsExecutionContext ecx, EsObject container, String zccKey)
			throws InterruptedException {
		if (container == null) throw new IllegalArgumentException("object is null");
		final IEsOperand oProperty = esoproperty(container, zccKey, true, true);
		if (oProperty == null) return null;
		return oProperty.toCanonicalString(ecx);
	}

	public static String property_qtwString(EsExecutionContext ecx, EsObject container, String zccKey)
			throws InterruptedException {
		final IEsOperand property = esproperty(container, zccKey, true, true);
		final String ztw = property.toCanonicalString(ecx).trim();
		if (ztw.length() == 0) {
			final String m = "Expecting a non-empty, non-whitespace value for property '" + zccKey + "'";
			throw new EsTypeCodeException(m);
		}
		return ztw;
	}

	public static int property_secondOfDay(EsExecutionContext ecx, EsObject container, String zccKey)
			throws InterruptedException {
		final String qtwSpec = property_qtwString(ecx, container, zccKey);
		try {
			return TimeOfDayFactory.secondOfDay(qtwSpec);
		} catch (final ArgonFormatException ex) {
			throw new EsTypeCodeException(ex);
		}
	}

	public static long property_smsElapsed(EsExecutionContext ecx, EsObject container, String zccKey, long noDatum)
			throws InterruptedException {
		final IEsOperand oNonNull = esoproperty(container, zccKey, true, true);
		if (oNonNull == null) return noDatum;
		final EsPrimitiveNumber number = oNonNull.toNumber(ecx);
		final SubType subType = number.subType();
		if (subType == SubType.ELAPSED) return number.longValue();
		final String m = "Expecting an elapsed value for property '" + zccKey + "'; actual type is " + subType;
		throw new EsTypeCodeException(m);
	}

	public static long property_smsElapsedDatum(EsExecutionContext ecx, EsObject container, String zccKey)
			throws InterruptedException {
		final EsPrimitiveNumber number = esproperty(container, zccKey, true, true).toNumber(ecx);
		final SubType subType = number.subType();
		if (subType == SubType.ELAPSED) return number.longValue();
		final String m = "Expecting an elapsed value for property '" + zccKey + "'; actual type is " + subType;
		throw new EsTypeCodeException(m);
	}

	public static int property_smsiElapsed(EsExecutionContext ecx, EsObject container, String zccKey, int noDatum)
			throws InterruptedException {
		final IEsOperand oNonNull = esoproperty(container, zccKey, true, true);
		if (oNonNull == null) return noDatum;
		final EsPrimitiveNumber number = oNonNull.toNumber(ecx);
		final SubType subType = number.subType();
		if (subType == SubType.ELAPSED) {
			final long longValue = number.longValue();
			if (longValue < Integer.MIN_VALUE || longValue > Integer.MAX_VALUE) {
				final String m = "Magnitude of elapsed value for property '" + zccKey + " is out of bounds";
				throw new EsTypeCodeException(m);
			}
			return (int) longValue;
		}
		final String m = "Expecting an elapsed value for property '" + zccKey + "'; actual type is " + subType;
		throw new EsTypeCodeException(m);
	}

	public static TimeOfDayRule property_timeOfDayRuleDatum(EsExecutionContext ecx, EsObject container, String zccKey)
			throws InterruptedException {
		final String qtwSpec = property_qtwString(ecx, container, zccKey);
		try {
			return TimeOfDayFactory.newRule(qtwSpec);
		} catch (final ArgonApiException ex) {
			throw new EsTypeCodeException(ex);
		}
	}

	public static TimeZone property_timeZone(EsExecutionContext ecx, EsObject container, String zccKey, TimeZone whenEmpty)
			throws InterruptedException {
		if (whenEmpty == null) throw new IllegalArgumentException("object is null");
		final String oqtwName = property_oqtwString(ecx, container, zccKey);
		if (oqtwName == null) return whenEmpty;
		try {
			return TimeZoneFactory.selectById(oqtwName);
		} catch (final ArgonApiException ex) {
			throw new EsTypeCodeException(ex);
		}
	}

	public static TimeZone property_timeZoneDatum(EsExecutionContext ecx, EsObject container, String zccKey)
			throws InterruptedException {
		final String qtwName = property_qtwString(ecx, container, zccKey);
		try {
			return TimeZoneFactory.selectById(qtwName);
		} catch (final ArgonApiException ex) {
			throw new EsTypeCodeException(ex);
		}
	}

	public static long property_tsTimeDatum(EsExecutionContext ecx, EsObject container, String zccKey)
			throws InterruptedException {
		final EsPrimitiveNumber number = esproperty(container, zccKey, true, true).toNumber(ecx);
		final SubType subType = number.subType();
		if (subType == SubType.TIME) return number.longValue();
		final String m = "Expecting a time value for property '" + zccKey + "'; actual type is " + subType;
		throw new EsTypeCodeException(m);
	}

	public static String property_ztwString(EsExecutionContext ecx, EsObject container, String zccKey)
			throws InterruptedException {
		final IEsOperand property = esproperty(container, zccKey, true, true);
		return property.toCanonicalString(ecx).trim();
	}

	public static String qSubPath(String qPath, int i) {
		final int pl = qPath.length();
		final StringBuilder sb = new StringBuilder(pl + 8);
		sb.append(qPath).append('[').append(i).append(']');
		return sb.toString();
	}

	public static String qtwStringValue(EsExecutionContext ecx, String qPath, IEsOperand defined)
			throws InterruptedException {
		if (qPath == null || qPath.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final String ztw = zStringValue(ecx, defined).trim();
		if (ztw.length() > 0) return ztw;
		final String m = "Expecting a non empty, non-whitespace string value for " + qPath;
		throw new EsTypeCodeException(m);
	}

	public static Charset selectCharset(String qnctwName) {
		try {
			return ArgonText.selectCharset(qnctwName);
		} catch (final ArgonApiException ex) {
			throw new EsTypeCodeException(ex);
		}
	}

	public static String subTypeName(IEsOperand value) {
		if (value == null) throw new IllegalArgumentException("object is null");
		final EsType majorType = value.esType();
		switch (majorType) {
			case TNull:
				return "null";
			case TBoolean:
				return "boolean";
			case TNumber:
				if (value instanceof EsPrimitiveNumberInteger) return "integer";
				if (value instanceof EsPrimitiveNumberDouble) return "double";
				if (value instanceof EsPrimitiveNumberReal) return "real";
				if (value instanceof EsPrimitiveNumberTime) return "time";
				if (value instanceof EsPrimitiveNumberElapsed) return "elapsed";
				return "nan";
			case TString:
				return "string";
			case TObject:
				if (value instanceof EsFunction) return "Function";
				return ((EsObject) value).esClass();
			default:
				return "undefined";
		}
	}

	public static int toPositiveInteger(String zccKey) {
		try {
			return Integer.parseInt(zccKey);
		} catch (final NumberFormatException exNF) {
			return -1;
		}
	}

	public static String toPropertyName(int index) {
		if (index < 0) throw new IllegalArgumentException("Invalid index:" + index);
		return Integer.toString(index);
	}

	public static IEsOperand[] xptOperandsEvery(EsExecutionContext ecx, String qPath, EsObject container)
			throws InterruptedException {
		if (container == null) throw new IllegalArgumentException("object is null");
		final int length = lengthNonZero(ecx, qPath, container);
		final IEsOperand[] xptOperands = new IEsOperand[length];
		for (int i = 0; i < length; i++) {
			xptOperands[i] = container.esGet(toPropertyName(i));
		}
		return xptOperands;
	}

	public static EsObject[] zptEsObjectEvery(EsExecutionContext ecx, String qPath, IEsOperand[] zptOperands)
			throws InterruptedException {
		if (ecx == null) throw new IllegalArgumentException("object is null");
		if (zptOperands == null) throw new IllegalArgumentException("array is null");
		final int length = zptOperands.length;
		final EsObject[] zptEsObject = new EsObject[length];
		for (int i = 0; i < length; i++) {
			final String qSubPath = qPath + "[" + i + "]";
			final IEsOperand nonNull = esOperand(qSubPath, zptOperands[i], true, true, true);
			zptEsObject[i] = nonNull.toObject(ecx);
		}
		return zptEsObject;
	}

	public static EsObject[] zptEsObjectOnly(EsExecutionContext ecx, IEsOperand[] zptOperands)
			throws InterruptedException {
		if (ecx == null) throw new IllegalArgumentException("object is null");
		if (zptOperands == null) throw new IllegalArgumentException("array is null");
		final int length = zptOperands.length;
		final EsObject[] zpEsObject = new EsObject[length];
		int w = 0;
		for (int r = 0; r < length; r++) {
			final IEsOperand oNonNull = esoOperand(zptOperands[r], true, true, true);
			if (oNonNull != null) {
				zpEsObject[w] = oNonNull.toObject(ecx);
				w++;
			}
		}
		return zpt_zp(zpEsObject, w);
	}

	public static int[] zptIntValuesEvery(EsExecutionContext ecx, String qPath, IEsOperand[] zptOperands)
			throws InterruptedException {
		if (ecx == null) throw new IllegalArgumentException("object is null");
		if (zptOperands == null) throw new IllegalArgumentException("array is null");
		final int length = zptOperands.length;
		final int[] zpt = new int[length];
		for (int i = 0; i < length; i++) {
			final String qSubPath = qPath + "[" + i + "]";
			final IEsOperand nonnull = esOperand(qSubPath, zptOperands[i], true, true, true);
			zpt[i] = nonnull.toNumber(ecx).intVerified();
		}
		return zpt;
	}

	public static IEsOperand[] zptOperandsEvery(EsExecutionContext ecx, EsObject container)
			throws InterruptedException {
		if (container == null) throw new IllegalArgumentException("object is null");
		final int length = length(ecx, container);
		final IEsOperand[] zptOperands = new IEsOperand[length];
		for (int i = 0; i < length; i++) {
			zptOperands[i] = container.esGet(toPropertyName(i));
		}
		return zptOperands;
	}

	public static IEsOperand[] zptOperandsEvery(EsExecutionContext ecx, String qPath, EsObject container, boolean defined,
			boolean nonnull)
			throws InterruptedException {
		if (container == null) throw new IllegalArgumentException("object is null");
		final int length = length(ecx, container);
		final IEsOperand[] zptOperands = new IEsOperand[length];
		for (int i = 0; i < length; i++) {
			final IEsOperand operand = container.esGet(toPropertyName(i));
			final String qSubPath = qPath + "[" + i + "]";
			final IEsOperand compliant = esOperand(qSubPath, operand, true, defined, nonnull);
			zptOperands[i] = compliant;
		}
		return zptOperands;
	}

	public static IEsOperand[] zptOperandsOnly(EsExecutionContext ecx, EsObject container, boolean defined, boolean nonnull)
			throws InterruptedException {
		if (container == null) throw new IllegalArgumentException("object is null");
		final int length = length(ecx, container);
		final IEsOperand[] zptOperands = new IEsOperand[length];
		int w = 0;
		for (int r = 0; r < length; r++) {
			final IEsOperand operand = container.esGet(toPropertyName(r));
			final IEsOperand oOperand = esoOperand(operand, true, defined, nonnull);
			if (oOperand != null) {
				zptOperands[w] = oOperand;
				w++;
			}
		}
		return zpt_zp(zptOperands, w);
	}

	public static String[] zptqtwStringValuesEvery(EsExecutionContext ecx, String qPath, IEsOperand[] zptOperands)
			throws InterruptedException {
		if (ecx == null) throw new IllegalArgumentException("object is null");
		if (zptOperands == null) throw new IllegalArgumentException("array is null");
		final int length = zptOperands.length;
		final String[] zptqtw = new String[length];
		for (int i = 0; i < length; i++) {
			final String qSubPath = qPath + "[" + i + "]";
			final IEsOperand defined = esOperand(qSubPath, zptOperands[i], true, true, false);
			zptqtw[i] = qtwStringValue(ecx, qSubPath, defined);
		}
		return zptqtw;
	}

	public static String[] zptqtwStringValuesOnly(EsExecutionContext ecx, IEsOperand[] zptOperands)
			throws InterruptedException {
		if (ecx == null) throw new IllegalArgumentException("object is null");
		if (zptOperands == null) throw new IllegalArgumentException("array is null");
		final int length = zptOperands.length;
		final String[] zpqtw = new String[length];
		int w = 0;
		for (int r = 0; r < length; r++) {
			final IEsOperand oNonNull = esoOperand(zptOperands[r], true, true, true);
			if (oNonNull != null) {
				final String ztw = oNonNull.toCanonicalString(ecx);
				if (ztw.length() > 0) {
					zpqtw[w] = ztw;
					w++;
				}
			}
		}
		return zpt_zp(zpqtw, w);
	}

	public static String[] zptzStringValuesEvery(EsExecutionContext ecx, String qPath, IEsOperand[] zptOperands)
			throws InterruptedException {
		if (ecx == null) throw new IllegalArgumentException("object is null");
		if (zptOperands == null) throw new IllegalArgumentException("array is null");
		final int length = zptOperands.length;
		final String[] zptz = new String[length];
		for (int i = 0; i < length; i++) {
			final String qSubPath = qSubPath(qPath, i);
			final IEsOperand defined = esOperand(qSubPath, zptOperands[i], true, true, false);
			zptz[i] = zStringValue(ecx, defined);
		}
		return zptz;
	}

	public static String zStringValue(EsExecutionContext ecx, IEsOperand defined)
			throws InterruptedException {
		if (defined == null) throw new IllegalArgumentException("object is null");
		final EsType operandType = defined.esType();
		if (operandType == EsType.TNull) return "";
		if (defined instanceof EsIntrinsicBinary) {
			final Binary binaryValue = ((EsIntrinsicBinary) defined).value();
			return binaryValue.newStringUTF8();
		}
		return defined.toCanonicalString(ecx);
	}

	private UNeon() {
	}
}
