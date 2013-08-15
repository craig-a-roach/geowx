/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

/**
 * 
 * @author roach
 */
public class EsIntrinsicJsonEncoderConstructor extends EsIntrinsicConstructor {

	@Override
	protected IEsOperand eval(EsExecutionContext ecx)
			throws InterruptedException {
		final EsMethodAccessor ac = new EsMethodAccessor(ecx);
		final EsObject source = ac.esObject(0);
		final EsIntrinsicJsonEncoder neo;
		if (calledAsFunction(ecx)) {
			neo = ecx.global().newIntrinsicJsonEncoder(source);
		} else {
			neo = thisIntrinsicObject(ecx, EsIntrinsicJsonEncoder.class);
			neo.setSource(source);
		}
		return neo;
	}

	@Override
	public EsObject declarePrototype(EsGlobal global) {
		final EsIntrinsicJsonEncoder neo = new EsIntrinsicJsonEncoder(global.prototypeObject);
		neo.loadPrototype();
		return neo;
	}

	private static EsIntrinsicMethod method_toBinary() {
		return new EsIntrinsicMethod("toBinary") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsIntrinsicJsonEncoder esThis = thisIntrinsicObject(ecx, EsIntrinsicJsonEncoder.class);
				return ecx.global().newIntrinsicBinary(esThis.newBinary(ecx));
			}
		};
	}

	private static EsIntrinsicMethod method_toString() {
		return new EsIntrinsicMethod("toString") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsIntrinsicJsonEncoder esThis = thisIntrinsicObject(ecx, EsIntrinsicJsonEncoder.class);
				return esThis.toPrimitiveString(ecx);
			}
		};
	}

	public static EsIntrinsicJsonEncoderConstructor newInstance() {
		return new EsIntrinsicJsonEncoderConstructor();
	}

	private EsIntrinsicJsonEncoderConstructor() {
		super(ClassName, new String[] { "source" }, 1);
	}

	public static final String ClassName = "JsonEncoder";
	public static final EsIntrinsicMethod[] Methods = { method_toString(), method_toBinary() };
}
