/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.ArgonApiException;
import com.metservice.argon.Elapsed;
import com.metservice.argon.TimeOfDayFactory;
import com.metservice.argon.TimeOfDayRule;
import com.metservice.neon.EsPrimitiveNumber.SubType;

/**
 * @author roach
 */
public class EsPropertyAccessor {

	public boolean booleanValue() {
		return pvalue.toCanonicalBoolean();
	}

	public boolean defaulted() {
		return !esType.isDefined;
	}

	public double doubleValue()
			throws InterruptedException {
		return esPrimitiveNumber().doubleValue();
	}

	public Elapsed elapsedValue()
			throws InterruptedException {
		return Elapsed.newInstance(smsElapsedValue());
	}

	public EsFunction esFunction()
			throws InterruptedException {
		return esObject(EsFunction.ClassName, EsFunction.class);
	}

	public EsIntrinsicArray esIntrinsicArray()
			throws InterruptedException {
		return esObject(EsIntrinsicArrayConstructor.ClassName, EsIntrinsicArray.class);
	}

	public EsIntrinsicBinary esIntrinsicBinary()
			throws InterruptedException {
		return esObject(EsIntrinsicBinaryConstructor.ClassName, EsIntrinsicBinary.class);
	}

	public EsIntrinsicRegExp esIntrinsicRegExp()
			throws InterruptedException {
		final IEsOperand nonnull = esOperandNonNull();
		return UNeon.esIntrinsicRegexp(acc.ecx, qPath, nonnull);
	}

	public EsIntrinsicTimezone esIntrinsicTimezone()
			throws InterruptedException {
		return esObject(EsIntrinsicTimezoneConstructor.ClassName, EsIntrinsicTimezone.class);
	}

	public EsObject esObject()
			throws InterruptedException {
		return esOperandNonNull().toObject(acc.ecx);
	}

	public <T extends EsObject> T esObject(String esClass, Class<T> objectClass)
			throws InterruptedException {
		if (esClass == null || esClass.length() == 0) throw new IllegalArgumentException("esClass is empty");
		if (objectClass == null) throw new IllegalArgumentException("objectClass is null");

		final EsObject object = esObject();
		if (objectClass.isInstance(object)) return objectClass.cast(object);
		final String acn = object.esClass();
		final String m = "Expecting a '" + esClass + "' type property '" + qPath + "'; actual type is '" + acn + "'";
		throw new EsTypeCodeException(m);
	}

	public EsFunction esoFunction()
			throws InterruptedException {
		return esoObject(EsFunction.ClassName, EsFunction.class);
	}

	public EsIntrinsicArray esoIntrinsicArray()
			throws InterruptedException {
		return esoObject(EsIntrinsicArrayConstructor.ClassName, EsIntrinsicArray.class);
	}

	public EsIntrinsicRegExp esoIntrinsicRegExp()
			throws InterruptedException {
		final IEsOperand oNonNull = esoOperandNonNull();
		if (oNonNull == null) return null;
		return UNeon.esIntrinsicRegexp(acc.ecx, qPath, oNonNull);
	}

	public EsIntrinsicTimezone esoIntrinsicTimezone()
			throws InterruptedException {
		return esoObject(EsIntrinsicTimezoneConstructor.ClassName, EsIntrinsicTimezone.class);
	}

	public EsObject esoObject()
			throws InterruptedException {
		final IEsOperand oOperand = esoOperandNonNull();
		return oOperand == null ? null : oOperand.toObject(acc.ecx);
	}

	public <T extends EsObject> T esoObject(String esClass, Class<T> objectClass)
			throws InterruptedException {
		if (objectClass == null) throw new IllegalArgumentException("object is null");
		final EsObject oObject = esoObject();
		if (oObject == null) return null;
		if (objectClass.isInstance(oObject)) return objectClass.cast(oObject);
		final String acn = oObject.esClass();
		final String m = "Expecting a '" + esClass + "' type property '" + qPath + "'; actual type is '" + acn + "'";
		throw new EsTypeCodeException(m);
	}

	public IEsOperand esoOperandNonNull() {
		final IEsOperand defined = esOperand(true, false);
		final EsType t = defined.esType();
		return t == EsType.TNull ? null : defined;
	}

	public IEsOperand esOperand(boolean defined, boolean nonnull) {
		if (defined && esType == EsType.TUndefined) {
			final String m = "Value of property '" + qPath + "' is undefined; expecting a value";
			throw new EsTypeCodeException(m);
		}
		if (nonnull && esType == EsType.TNull) {
			final String m = "Value of property '" + qPath + "' is null; expecting a non-null value";
			throw new EsTypeCodeException(m);
		}
		return pvalue;
	}

	public IEsOperand esOperandDefined() {
		return esOperand(true, false);
	}

	public IEsOperand esOperandNonNull() {
		return esOperand(true, true);
	}

	public EsPrimitiveBoolean esPrimitiveBoolean() {
		return EsPrimitiveBoolean.instance(booleanValue());
	}

	public EsPrimitiveNumber esPrimitiveNumber()
			throws InterruptedException {
		return pvalue.toNumber(acc.ecx);
	}

	public float floatValue()
			throws InterruptedException {
		return UNeon.floatVerified(esPrimitiveNumber().doubleValue());
	}

	public int intValue()
			throws InterruptedException {
		return esPrimitiveNumber().intVerified();
	}

	public long longValue()
			throws InterruptedException {
		return esPrimitiveNumber().longValue();
	}

	public String qtwStringValue()
			throws InterruptedException {
		final String ztw = ztwStringValue();
		if (ztw.length() > 0) return ztw;
		final String m = "Expecting a non empty, non-whitespace string value for property '" + qPath + "'";
		throw new EsTypeCodeException(m);
	}

	public long smsElapsedValue()
			throws InterruptedException {
		final EsPrimitiveNumber number = esPrimitiveNumber();
		final SubType subType = number.subType();
		if (subType == SubType.ELAPSED) return number.longValue();
		final String m = "Expecting an elapsed value for property '" + qPath + "'; actual type is '" + subType + "' (" + number
				+ ")";
		throw new EsTypeCodeException(m);
	}

	public TimeOfDayRule timeOfDayRule()
			throws InterruptedException {
		final String qtwSpec = qtwStringValue();
		try {
			return TimeOfDayFactory.newRule(qtwSpec);
		} catch (final ArgonApiException ex) {
			final String m = "Time of day rule '" + qtwSpec + "' specified by property '" + qPath + "' is malformed..."
					+ ex.getMessage();
			throw new EsTypeCodeException(m);
		}
	}

	@Override
	public String toString() {
		return qPath + "=" + pvalue.toString();
	}

	public long tsTimeValue()
			throws InterruptedException {
		final EsPrimitiveNumber number = esPrimitiveNumber();
		final SubType subType = number.subType();
		if (subType == SubType.TIME) return number.longValue();
		final String m = "Expecting a time value for property '" + qPath + "'; actual type is '" + subType + "' (" + number
				+ ")";
		throw new EsTypeCodeException(m);
	}

	public String zStringValue()
			throws InterruptedException {
		return UNeon.zStringValue(acc.ecx, esOperandDefined());
	}

	public String ztwStringValue()
			throws InterruptedException {
		return zStringValue().trim();
	}

	public EsPropertyAccessor(EsObjectAccessor accessor, String pname) {
		if (accessor == null) throw new IllegalArgumentException("object is null");
		if (pname == null || pname.length() == 0) throw new IllegalArgumentException("string is null or empty");
		this.acc = accessor;
		this.pname = pname;
		this.qPath = accessor.qPath(pname);
		final IEsOperand esPublished = accessor.esOperandPublished(pname);
		this.pvalue = esPublished;
		this.esType = esPublished.esType();
	}
	private final EsObjectAccessor acc;
	public final String pname;
	public final String qPath;
	public final IEsOperand pvalue;
	public final EsType esType;
}
