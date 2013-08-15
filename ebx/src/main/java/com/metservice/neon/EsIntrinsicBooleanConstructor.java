/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;


/**
 * 
 * 
 * @author roach
 */
public class EsIntrinsicBooleanConstructor extends EsIntrinsicConstructor {
	@Override
	protected IEsOperand eval(EsExecutionContext ecx) {
		final EsActivation activation = ecx.activation();
		final IEsOperand value = activation.esGet("value");
		final EsPrimitiveBoolean booleanValue;
		if (value instanceof EsPrimitiveUndefined) {
			booleanValue = EsPrimitiveBoolean.FALSE;
		} else {
			booleanValue = EsPrimitiveBoolean.instance(value.toCanonicalBoolean());
		}
		if (calledAsFunction(ecx)) return booleanValue;
		final EsIntrinsicBoolean neo = (EsIntrinsicBoolean) ecx.thisObject();
		neo.setValue(booleanValue);
		return null;
	}

	@Override
	public EsObject declarePrototype(EsGlobal global) {
		return new EsIntrinsicBoolean(global.prototypeObject);
	}

	// ECMA 15.6.4.2
	/**
	 * @jsmethod toString
	 * @jsreturn A string representation of the boolean value.
	 */
	private static EsIntrinsicMethod method_toString() {
		return new EsIntrinsicMethod("toString") {
			@Override
			protected IEsOperand eval(EsExecutionContext ecx) {
				return thisIntrinsicObject(ecx, EsIntrinsicBoolean.class).value().toPrimitiveString();
			}
		};
	}

	// ECMA 15.6.4.3
	/**
	 * @jsmethod valueOf
	 * @jsreturn The boolean value of the object.
	 */
	private static EsIntrinsicMethod method_valueOf() {
		return new EsIntrinsicMethod("valueOf") {
			@Override
			protected IEsOperand eval(EsExecutionContext ecx) {
				return thisIntrinsicObject(ecx, EsIntrinsicBoolean.class).value();
			}
		};
	}

	public static EsIntrinsicBooleanConstructor newInstance() {
		return new EsIntrinsicBooleanConstructor();
	}

	/**
	 * @jsconstructor Boolean
	 * @jsnote If the value parameter is not given then the boolean variable defaults to false.
	 * @jsparam value Optional. The initial value of the boolean variable.
	 */
	private EsIntrinsicBooleanConstructor() {
		super(ClassName, new String[] { "value" }, 0);
	}

	public static final String ClassName = "Boolean";

	public static final EsIntrinsicMethod[] Methods = { method_toString(), method_valueOf() };
}
