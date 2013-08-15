/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

/**
 * @jsobject Object
 * @jsnote A vanilla object.
 * @author roach
 */
public class EsIntrinsicObjectConstructor extends EsIntrinsicConstructor {
	@Override
	protected IEsOperand eval(EsExecutionContext ecx) {
		return null;
	}

	@Override
	public EsObject declarePrototype(EsGlobal global) {
		return null;
	}

	// ECMA 15.2.4.2
	/**
	 * @jsmethod toString
	 * @jsreturn A String describing the Object object.
	 */
	private static EsIntrinsicMethod method_toString() {
		return new EsIntrinsicMethod("toString") {
			@Override
			protected IEsOperand eval(EsExecutionContext ecx) {
				return new EsPrimitiveString("[object " + ecx.thisObject().esClass() + "]");
			}
		};
	}

	/**
	 * @jsconstructor Object
	 */
	private EsIntrinsicObjectConstructor() {
		super(ClassName, NOARGS, 0);
	}

	public static final String ClassName = "Object";

	public static final EsIntrinsicMethod[] Methods = { method_toString() };

	public static final EsIntrinsicObjectConstructor Instance = new EsIntrinsicObjectConstructor();
}
