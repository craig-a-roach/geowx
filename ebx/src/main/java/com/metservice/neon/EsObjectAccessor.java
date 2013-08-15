/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.Elapsed;
import com.metservice.neon.EsPrimitiveNumber.SubType;

/**
 * @author roach
 */
public class EsObjectAccessor {

	public boolean booleanValue(String pname) {
		return esOperandPublished(pname).toCanonicalBoolean();
	}

	public boolean defaulted(String pname) {
		if (pname == null || pname.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final IEsOperand operand = src.esGet(pname);
		final EsType t = operand.esType();
		return !t.isDefined;
	}

	public double doubleValue(String pname)
			throws InterruptedException {
		return esPrimitiveNumber(pname).doubleValue();
	}

	public Elapsed elapsedValue(String pname)
			throws InterruptedException {
		return Elapsed.newInstance(smsElapsedValue(pname));
	}

	public EsObject esObject(String pname)
			throws InterruptedException {
		return esOperandNonNull(pname).toObject(ecx);
	}

	public <T extends EsObject> T esObject(String pname, String esClass, Class<T> objectClass)
			throws InterruptedException {
		if (esClass == null || esClass.length() == 0) throw new IllegalArgumentException("esClass is empty");
		if (objectClass == null) throw new IllegalArgumentException("objectClass is null");

		final EsObject object = esObject(pname);
		if (objectClass.isInstance(object)) return objectClass.cast(object);
		final String pn = qPath(pname);
		final String acn = object.esClass();
		final String m = "Expecting a '" + esClass + "' type property '" + pn + "'; actual type is '" + acn + "'";
		throw new EsTypeCodeException(m);
	}

	public EsObject esoObject(String pname)
			throws InterruptedException {
		final IEsOperand oOperand = esoOperandNonNull(pname);
		return oOperand == null ? null : oOperand.toObject(ecx);
	}

	public <T extends EsObject> T esoObject(String pname, String esClass, Class<T> objectClass)
			throws InterruptedException {
		if (objectClass == null) throw new IllegalArgumentException("object is null");
		final EsObject oObject = esoObject(pname);
		if (oObject == null) return null;
		if (objectClass.isInstance(oObject)) return objectClass.cast(oObject);
		final String pn = qPath(pname);
		final String acn = oObject.esClass();
		final String m = "Expecting a '" + esClass + "' type property '" + pn + "'; actual type is '" + acn + "'";
		throw new EsTypeCodeException(m);
	}

	public IEsOperand esoOperandNonNull(String pname) {
		final IEsOperand defined = esOperand(pname, true, true, false);
		final EsType t = defined.esType();
		return t == EsType.TNull ? null : defined;
	}

	public IEsOperand esOperand(String pname, boolean published, boolean defined, boolean nonnull) {
		if (pname == null || pname.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final IEsOperand operand = src.esGet(pname);
		final EsType t = operand.esType();
		if (published && !t.isPublished) {
			final String pn = qPath(pname);
			final String m = "Expecting a published type for property '" + pn + "'; actual type is '" + t + "'";
			throw new EsTypeCodeException(m);
		}
		if (defined && t == EsType.TUndefined) {
			final String pn = qPath(pname);
			final String m = "Value of property '" + pn + "' is undefined; expecting a value";
			throw new EsTypeCodeException(m);
		}
		if (nonnull && t == EsType.TNull) {
			final String pn = qPath(pname);
			final String m = "Value of property '" + pn + "' is null; expecting a non-null value";
			throw new EsTypeCodeException(m);
		}
		return operand;
	}

	public IEsOperand esOperandDefined(String pname) {
		return esOperand(pname, true, true, false);
	}

	public IEsOperand esOperandNonNull(String pname) {
		return esOperand(pname, true, true, true);
	}

	public IEsOperand esOperandPublished(String pname) {
		return esOperand(pname, true, false, false);
	}

	public EsPrimitiveBoolean esPrimitiveBoolean(String pname) {
		return EsPrimitiveBoolean.instance(booleanValue(pname));
	}

	public EsPrimitiveNumber esPrimitiveNumber(String pname)
			throws InterruptedException {
		return esOperandPublished(pname).toNumber(ecx);
	}

	public EsPropertyAccessor find(String pname) {
		final EsPropertyAccessor pacc = new EsPropertyAccessor(this, pname);
		return pacc.defaulted() ? null : pacc;
	}

	public int intValue(String pname)
			throws InterruptedException {
		return esPrimitiveNumber(pname).intVerified();
	}

	public long longValue(String pname)
			throws InterruptedException {
		return esPrimitiveNumber(pname).longValue();
	}

	public String qPath(String pname) {
		return (zPath.length() == 0) ? pname : (zPath + "." + pname);
	}

	public String qtwStringValue(String pname)
			throws InterruptedException {
		final String ztw = ztwStringValue(pname);
		if (ztw.length() > 0) return ztw;
		final String pn = qPath(pname);
		final String m = "Expecting a non empty, non-whitespace string value for property '" + pn + "'";
		throw new EsTypeCodeException(m);
	}

	public EsPropertyAccessor select(String pname) {
		return new EsPropertyAccessor(this, pname);
	}

	public long smsElapsedValue(String pname)
			throws InterruptedException {
		final EsPrimitiveNumber number = esPrimitiveNumber(pname);
		final SubType subType = number.subType();
		if (subType == SubType.ELAPSED) return number.longValue();
		final String pn = qPath(pname);
		final String m = "Expecting an elapsed value for property '" + pn + "'; actual type is '" + subType + "' (" + number
				+ ")";
		throw new EsTypeCodeException(m);
	}

	@Override
	public String toString() {
		final String s = src.show(1);
		return zPath.length() == 0 ? s : zPath + "=" + s;
	}

	public long tsTimeValue(String pname)
			throws InterruptedException {
		final EsPrimitiveNumber number = esPrimitiveNumber(pname);
		final SubType subType = number.subType();
		if (subType == SubType.TIME) return number.longValue();
		final String pn = qPath(pname);
		final String m = "Expecting a time value for property '" + pn + "'; actual type is '" + subType + "' (" + number + ")";
		throw new EsTypeCodeException(m);
	}

	public String zStringValue(String pname)
			throws InterruptedException {
		return UNeon.zStringValue(ecx, esOperandDefined(pname));
	}

	public String ztwStringValue(String pname)
			throws InterruptedException {
		return zStringValue(pname).trim();
	}

	public EsObjectAccessor(EsExecutionContext ecx, EsObject src, String zPath) {
		if (ecx == null) throw new IllegalArgumentException("object is null");
		if (src == null) throw new IllegalArgumentException("object is null");
		if (zPath == null) throw new IllegalArgumentException("object is null");
		this.ecx = ecx;
		this.src = src;
		this.zPath = zPath;
	}

	public final EsExecutionContext ecx;
	public final EsObject src;
	public final String zPath;
}
