/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.regex.Pattern;

import com.metservice.argon.CodedEnumTable;
import com.metservice.argon.Elapsed;
import com.metservice.argon.ICodedEnum;
import com.metservice.argon.json.IJsonNative;
import com.metservice.argon.json.JsonArray;
import com.metservice.argon.json.JsonNull;
import com.metservice.neon.EsPrimitiveNumber.SubType;

/**
 * @author roach
 */
public class EsMethodAccessor {

	public boolean booleanValue(int index) {
		return esOperand(index).toCanonicalBoolean();
	}

	public EsObjectAccessor createObjectAccessor(int index)
			throws InterruptedException {
		final EsArgumentAccessor oaa = find(index);
		return oaa == null ? null : oaa.createObjectAccessor();
	}

	public boolean defaulted(int index) {
		return argc <= index;
	}

	public double doubleValue(int index)
			throws InterruptedException {
		return esPrimitiveNumber(index).doubleValue();
	}

	public Elapsed elapsedValue(int index)
			throws InterruptedException {
		return Elapsed.newInstance(smsElapsedValue(index));
	}

	public <T extends ICodedEnum> T enumValue(int index, CodedEnumTable<T> codedEnumTable, T defaultValue)
			throws InterruptedException {
		if (codedEnumTable == null) throw new IllegalArgumentException("codedEnumTable is null");
		if (defaultValue == null) throw new IllegalArgumentException("defaultValue is null");
		final IEsOperand oOperand = esvOperand(index, EsOperandFilter.TypeException, EsOperandFilter.OptionNull);
		final String ztwCode = oOperand == null ? "" : oOperand.toCanonicalString(ecx).trim();
		if (ztwCode.length() == 0) return defaultValue;
		final T oValue = codedEnumTable.find(ztwCode);
		if (oValue != null) return oValue;

		final String opts = codedEnumTable.qCommaValues();
		final String fpn = formalParameterName(index);
		final String m = "Expecting a '" + codedEnumTable + "' value for formal parameter '" + fpn + "'; actual value is '"
				+ ztwCode + "'. Valid values are:\n" + opts;
		throw new EsTypeCodeException(m);
	}

	public EsFunction esFunction(int index)
			throws InterruptedException {
		return esObject(index, EsFunction.ClassName, EsFunction.class);
	}

	public EsIntrinsicArray esIntrinsicArray(int index)
			throws InterruptedException {
		return esObject(index, EsIntrinsicArrayConstructor.ClassName, EsIntrinsicArray.class);
	}

	public EsIntrinsicBinary esIntrinsicBinary(int index)
			throws InterruptedException {
		return esObject(index, EsIntrinsicBinaryConstructor.ClassName, EsIntrinsicBinary.class);
	}

	public EsIntrinsicRegExp esIntrinsicRegExp(int index)
			throws InterruptedException {
		final IEsOperand esDatum = esOperandDatum(index);
		final String qPath = qPath(index);
		return UNeon.esIntrinsicRegexp(ecx, qPath, esDatum);
	}

	public EsIntrinsicTimemask esIntrinsicTimemask(int index)
			throws InterruptedException {
		final IEsOperand esDatum = esOperandDatum(index);
		final String qPath = qPath(index);
		return UNeon.esIntrinsicTimemask(ecx, qPath, esDatum);
	}

	public EsIntrinsicTimezone esIntrinsicTimezone(int index)
			throws InterruptedException {
		return esObject(index, EsIntrinsicTimezoneConstructor.ClassName, EsIntrinsicTimezone.class);
	}

	public EsObject esObject(int index)
			throws InterruptedException {
		return esOperandDatum(index).toObject(ecx);
	}

	public <T extends EsObject> T esObject(int index, String esClass, Class<T> objectClass)
			throws InterruptedException {
		if (esClass == null || esClass.length() == 0) throw new IllegalArgumentException("esClass is empty");
		if (objectClass == null) throw new IllegalArgumentException("objectClass is null");

		final EsObject object = esObject(index);
		if (objectClass.isInstance(object)) return objectClass.cast(object);
		final String fpn = formalParameterName(index);
		final String acn = object.esClass();
		final String m = "Expecting a '" + esClass + "' type formal parameter '" + fpn + "'; actual type is '" + acn + "'";
		throw new EsTypeCodeException(m);
	}

	public EsFunction esoFunction(int index)
			throws InterruptedException {
		return esoObject(index, EsFunction.ClassName, EsFunction.class);
	}

	public EsIntrinsicArray esoIntrinsicArray(int index)
			throws InterruptedException {
		return esoObject(index, EsIntrinsicArrayConstructor.ClassName, EsIntrinsicArray.class);
	}

	public EsIntrinsicRegExp esoIntrinsicRegExp(int index)
			throws InterruptedException {
		final IEsOperand oOperand = esvOperand(index, EsOperandFilter.TypeException, EsOperandFilter.OptionNull);
		if (oOperand == null) return null;
		final String qPath = qPath(index);
		return UNeon.esIntrinsicRegexp(ecx, qPath, oOperand);
	}

	public EsIntrinsicTimemask esoIntrinsicTimemask(int index)
			throws InterruptedException {
		final IEsOperand oOperand = esvOperand(index, EsOperandFilter.TypeException, EsOperandFilter.OptionNull);
		if (oOperand == null) return null;
		final String qPath = qPath(index);
		return UNeon.esIntrinsicTimemask(ecx, qPath, oOperand);
	}

	public EsIntrinsicTimezone esoIntrinsicTimezone(int index)
			throws InterruptedException {
		return esoObject(index, EsIntrinsicTimezoneConstructor.ClassName, EsIntrinsicTimezone.class);
	}

	public EsObject esoObject(int index)
			throws InterruptedException {
		final IEsOperand oOperand = esvOperand(index, EsOperandFilter.TypeException, EsOperandFilter.OptionNull);
		return oOperand == null ? null : oOperand.toObject(ecx);
	}

	public <T extends EsObject> T esoObject(int index, String esClass, Class<T> objectClass)
			throws InterruptedException {
		if (objectClass == null) throw new IllegalArgumentException("object is null");
		final EsObject oObject = esoObject(index);
		if (oObject == null) return null;
		if (objectClass.isInstance(oObject)) return objectClass.cast(oObject);
		final String fpn = formalParameterName(index);
		final String acn = oObject.esClass();
		final String m = "Expecting a '" + esClass + "' type formal parameter '" + fpn + "'; actual type is '" + acn + "'";
		throw new EsTypeCodeException(m);
	}

	public IEsOperand esoOperandDatum(int index, boolean undefinedException) {
		final EsOperandFilter undefinedFilter = undefinedException ? EsOperandFilter.TypeException : EsOperandFilter.OptionNull;
		return esvOperand(index, undefinedFilter, EsOperandFilter.OptionNull);
	}

	public IEsOperand esOperand(int index) {
		return esOperand(index, true, false, false);
	}

	public IEsOperand esOperand(int index, boolean published, boolean defined, boolean nonnull) {
		final IEsOperand operand = args.operand(index);
		final EsType t = operand.esType();
		if (published && !t.isPublished) {
			final String fpn = formalParameterName(index);
			final String m = "Expecting a published type for formal parameter '" + fpn + "'; actual type is '" + t + "'";
			throw new EsTypeCodeException(m);
		}
		if (defined && t == EsType.TUndefined) {
			final String fpn = formalParameterName(index);
			final String m = "Value of formal parameter '" + fpn + "' is undefined; expecting a value";
			throw new EsTypeCodeException(m);
		}
		if (nonnull && t == EsType.TNull) {
			final String fpn = formalParameterName(index);
			final String m = "Value of formal parameter '" + fpn + "' is null; expecting a non-null value";
			throw new EsTypeCodeException(m);
		}
		return operand;
	}

	public IEsOperand esOperandDatum(int index) {
		return esOperand(index, true, true, true);
	}

	public IEsOperand esOperandDefined(int index) {
		return esOperand(index, true, true, false);
	}

	public EsPrimitive esPrimitive(int index)
			throws InterruptedException {
		return esOperand(index).toPrimitive(ecx, null);
	}

	public EsPrimitiveBoolean esPrimitiveBoolean(int index)
			throws InterruptedException {
		return EsPrimitiveBoolean.instance(booleanValue(index));
	}

	public EsPrimitiveNumber esPrimitiveNumber(int index)
			throws InterruptedException {
		return esOperand(index).toNumber(ecx);
	}

	/**
	 * <pre>
	 * undefined : throws Type exception
	 * null : empty PrimitiveString
	 * Object : PrimitiveString result of toString() call
	 * Number : PrimitiveString representation of number, or 'NaN'
	 * String : PrimitiveString, possibly empty
	 * Boolean : 'true' or 'false' PrimitiveString
	 * </pre>
	 * 
	 * @param index
	 * @return String, possibly empty
	 * @throws InterruptedException
	 */
	public EsPrimitive esPrimitiveString(int index)
			throws InterruptedException {
		return new EsPrimitiveString(zStringValue(index));
	}

	public <T extends EsObject> T esThis(String esClass, Class<T> objectClass) {
		return ecx.thisObject(esClass, objectClass);
	}

	public EsType esType(int index) {
		return args.operand(index).esType();
	}

	public IEsOperand esvOperand(int index, EsOperandFilter esundefined, EsOperandFilter esnull) {
		final IEsOperand operand = args.operand(index);
		final EsType t = operand.esType();
		if (!t.isPublished) {
			final String fpn = formalParameterName(index);
			final String m = "Expecting a published type for formal parameter '" + fpn + "'; actual type is '" + t + "'";
			throw new EsTypeCodeException(m);
		}
		if (t == EsType.TUndefined) {
			if (esundefined == EsOperandFilter.Accept) return EsPrimitiveUndefined.Instance;
			if (esundefined == EsOperandFilter.OptionNull) return null;
			final String fpn = formalParameterName(index);
			final String m = "Value of formal parameter '" + fpn + "' is undefined; expecting a value";
			throw new EsTypeCodeException(m);
		}
		if (t == EsType.TNull) {
			if (esnull == EsOperandFilter.Accept) return EsPrimitiveNull.Instance;
			if (esnull == EsOperandFilter.OptionNull) return null;
			final String fpn = formalParameterName(index);
			final String m = "Value of formal parameter '" + fpn + "' is null; expecting a non-null value";
			throw new EsTypeCodeException(m);
		}
		return operand;
	}

	public String exType(int index, Throwable ex) {
		final String fpn = formalParameterName(index);
		final String ozCause = ex.getMessage();
		final String zCause = ozCause == null || ozCause.length() == 0 ? "" : "..." + ozCause;
		return "Value of formal parameter '" + fpn + "' is malformed" + zCause;
	}

	public EsArgumentAccessor find(int index) {
		if (index >= argc) return null;
		final String qPath = qPath(index);
		return new EsArgumentAccessor(this, index, qPath);
	}

	public String formalParameterName(int index) {
		return args.formalParameterName(index);
	}

	public int intValue(int index)
			throws InterruptedException {
		return esPrimitiveNumber(index).intVerified();
	}

	public JsonArray jsonArguments(int indexStart)
			throws InterruptedException {
		final int count = Math.max(0, argc - indexStart);
		final JsonArray neo = JsonArray.newImmutable(count);
		for (int index = indexStart, iarray = 0; index < argc; index++, iarray++) {
			final IEsOperand esoDefined = esvOperand(index, EsOperandFilter.OptionNull, EsOperandFilter.OptionNull);
			final IJsonNative oJsonNative = esoDefined == null ? null : esoDefined.createJsonNative();
			final IJsonNative jsonNative = oJsonNative == null ? JsonNull.Instance : oJsonNative;
			neo.jsonAdd(iarray, jsonNative);
		}
		return neo;
	}

	public long longValue(int index)
			throws InterruptedException {
		return esPrimitiveNumber(index).longValue();
	}

	public EsObjectAccessor newObjectAccessor(int index)
			throws InterruptedException {
		return select(index).newObjectAccessor();
	}

	public Pattern oPattern(int index)
			throws InterruptedException {
		final IEsOperand oOperand = esvOperand(index, EsOperandFilter.TypeException, EsOperandFilter.OptionNull);
		if (oOperand == null) return null;
		final String qPath = qPath(index);
		return UNeon.pattern(ecx, qPath, oOperand);
	}

	public Pattern pattern(int index)
			throws InterruptedException {
		final IEsOperand esDatum = esOperandDatum(index);
		final String qPath = qPath(index);
		return UNeon.pattern(ecx, qPath, esDatum);
	}

	public String qPath(int index) {
		return "formal parameter " + formalParameterName(index);
	}

	/**
	 * <pre>
	 * undefined : throws Type exception
	 * null : throws Type exception
	 * Object : result of toString() call if non-empty, otherwise Type exception
	 * Number : string representation of number, or 'NaN'
	 * String : value if non-empty, otherwise Type exception
	 * Boolean : 'true' or 'false'
	 * </pre>
	 * 
	 * @param index
	 * @return String, possibly empty
	 * @throws InterruptedException
	 */
	public String qStringValue(int index)
			throws InterruptedException {
		final String z = zStringValue(index);
		if (z.length() > 0) return z;
		final String fpn = formalParameterName(index);
		final String m = "Expecting a non empty string value for formal parameter '" + fpn + "'";
		throw new EsTypeCodeException(m);
	}

	/**
	 * <pre>
	 * undefined : throws Type exception
	 * null : throws Type exception
	 * Object : result of toString() or Binary.decodeUtf8 call if non-empty+non-whitespace, otherwise Type exception
	 * Number : string representation of number, or 'NaN'
	 * String : value if non-empty+non-whitespace, otherwise Type exception
	 * Boolean : 'true' or 'false'
	 * </pre>
	 * 
	 * @param index
	 * @return String, possibly empty
	 * @throws InterruptedException
	 */
	public String qtwStringValue(int index)
			throws InterruptedException {
		final String ztw = ztwStringValue(index);
		if (ztw.length() > 0) return ztw;
		final String fpn = formalParameterName(index);
		final String m = "Expecting a non empty, non-whitespace string value for formal parameter '" + fpn + "'";
		throw new EsTypeCodeException(m);
	}

	public String qtwStringValueThis()
			throws InterruptedException {
		final String ztw = ztwStringValueThis();
		if (ztw.length() > 0) return ztw;
		final String m = "Expecting a non empty, non-whitespace string value for this object";
		throw new EsTypeCodeException(m);
	}

	public EsArgumentAccessor select(int index) {
		if (index >= argc) {
			final String m = "invalid argument index " + index + "; argument count is " + argc;
			throw new IllegalArgumentException(m);
		}
		final String fpn = formalParameterName(index);
		return new EsArgumentAccessor(this, index, fpn);
	}

	public long smsElapsedValue(int index)
			throws InterruptedException {
		final EsPrimitiveNumber number = esPrimitiveNumber(index);
		final SubType subType = number.subType();
		if (subType == SubType.ELAPSED) return number.longValue();
		final String fpn = formalParameterName(index);
		final String m = "Expecting an elapsed value for formal parameter '" + fpn + "'; actual type is '" + subType + "' ("
				+ number + ")";
		throw new EsTypeCodeException(m);
	}

	public boolean specified(int index) {
		return index < argc;
	}

	public boolean specifiedDatum(int index) {
		return specified(index) && args.operand(index).esType().isDatum;
	}

	public boolean specifiedDefined(int index) {
		return specified(index) && args.operand(index).esType().isDefined;
	}

	@Override
	public String toString() {
		return args.toString();
	}

	public long tsTimeValue(int index)
			throws InterruptedException {
		final EsPrimitiveNumber number = esPrimitiveNumber(index);
		final SubType subType = number.subType();
		if (subType == SubType.TIME) return number.longValue();
		final String fpn = formalParameterName(index);
		final String m = "Expecting a time value for formal parameter '" + fpn + "'; actual type is '" + subType + "' ("
				+ number + ")";
		throw new EsTypeCodeException(m);
	}

	/**
	 * <pre>
	 * undefined : throws Type exception
	 * null : empty string
	 * Object : result of toString() call
	 * Number : string representation of number, or 'NaN'
	 * String : value, possibly empty
	 * Boolean : 'true' or 'false'
	 * </pre>
	 * 
	 * @param index
	 * @return String, possibly empty
	 * @throws InterruptedException
	 */
	public String zStringValue(int index)
			throws InterruptedException {
		return UNeon.zStringValue(ecx, esOperandDefined(index));
	}

	public String zStringValueThis()
			throws InterruptedException {
		return esThis.toCanonicalString(ecx);
	}

	public String ztwStringValue(int index)
			throws InterruptedException {
		return zStringValue(index).trim();
	}

	public String ztwStringValueThis()
			throws InterruptedException {
		return esThis.toCanonicalString(ecx).trim();
	}

	public EsMethodAccessor(EsExecutionContext ecx) {
		if (ecx == null) throw new IllegalArgumentException("object is null");
		this.ecx = ecx;
		this.esThis = ecx.thisObject();
		this.activation = ecx.activation();
		this.args = this.activation.arguments();
		this.argc = this.args.length();
	}

	public final EsExecutionContext ecx;
	public final EsObject esThis;
	public final EsActivation activation;
	public final EsArguments args;
	public final int argc;
}
