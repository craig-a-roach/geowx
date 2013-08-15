package com.metservice.neon;

/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */

/**
 * @author roach
 */
public class EsGlobalAccessor {

	public static EsObject esObject(EsExecutionContext ecx, String pname)
			throws InterruptedException {
		return esOperandNonNull(ecx, pname).toObject(ecx);
	}

	public static <T extends EsObject> T esObject(EsExecutionContext ecx, String pname, String esClass, Class<T> objectClass)
			throws InterruptedException {
		if (pname == null || pname.length() == 0) throw new IllegalArgumentException("pname is null or empty");
		if (esClass == null || esClass.length() == 0) throw new IllegalArgumentException("esClass is empty");
		if (objectClass == null) throw new IllegalArgumentException("objectClass is null");

		final EsObject object = esObject(ecx, pname);
		if (objectClass.isInstance(object)) return objectClass.cast(object);
		final String acn = object.esClass();
		final String m = "Expecting a '" + esClass + "' type global '" + pname + "'; actual type is '" + acn + "'";
		throw new EsTypeCodeException(m);
	}

	public static IEsOperand esoOperandNonNull(EsExecutionContext ecx, String pname) {
		final IEsOperand defined = esOperand(ecx, pname, true, true, false);
		final EsType t = defined.esType();
		return t == EsType.TNull ? null : defined;
	}

	public static IEsOperand esOperand(EsExecutionContext ecx, String pname, boolean published, boolean defined, boolean nonnull) {
		if (pname == null || pname.length() == 0) throw new IllegalArgumentException("pname is null or empty");
		final IEsOperand operand = ecx.global().esGet(pname);
		final EsType t = operand.esType();
		if (published && !t.isPublished) {
			final String m = "Expecting a published type for global '" + pname + "'; actual type is '" + t + "'";
			throw new EsTypeCodeException(m);
		}
		if (defined && t == EsType.TUndefined) {
			final String m = "Value of global '" + pname + "' is undefined; expecting a value";
			throw new EsTypeCodeException(m);
		}
		if (nonnull && t == EsType.TNull) {
			final String m = "Value of global '" + pname + "' is null; expecting a non-null value";
			throw new EsTypeCodeException(m);
		}
		return operand;
	}

	public static IEsOperand esOperandNonNull(EsExecutionContext ecx, String pname) {
		return esOperand(ecx, pname, true, true, true);
	}
}
