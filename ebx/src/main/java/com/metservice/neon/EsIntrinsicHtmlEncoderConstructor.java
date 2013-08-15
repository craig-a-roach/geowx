/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

/**
 * @author roach
 */
public class EsIntrinsicHtmlEncoderConstructor extends EsIntrinsicConstructor {

	public static final String ClassName = "HtmlEncoder";

	public static final EsIntrinsicMethod[] Methods = { method_toString(), method_toBinary() };

	@Override
	protected IEsOperand eval(EsExecutionContext ecx)
			throws InterruptedException {
		final EsMethodAccessor ac = new EsMethodAccessor(ecx);
		final EsObject oRootSource = ac.esoObject(0);
		final EsIntrinsicHtmlEncoder neo;
		if (calledAsFunction(ecx)) {
			neo = ecx.global().newIntrinsicHtmlEncoder(oRootSource);
		} else {
			neo = thisIntrinsicObject(ecx, EsIntrinsicHtmlEncoder.class);
			neo.setRoot(oRootSource);
		}
		return neo;
	}

	@Override
	public EsObject declarePrototype(EsGlobal global) {
		final EsIntrinsicHtmlEncoder neo = new EsIntrinsicHtmlEncoder(global.prototypeObject);
		neo.loadPrototype();
		return neo;
	}

	private static EsIntrinsicMethod method_toBinary() {
		return new EsIntrinsicMethod("toBinary") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsIntrinsicHtmlEncoder esThis = thisIntrinsicObject(ecx, EsIntrinsicHtmlEncoder.class);
				return ecx.global().newIntrinsicBinary(esThis.newBinary(ecx));
			}
		};
	}

	private static EsIntrinsicMethod method_toString() {
		return new EsIntrinsicMethod("toString") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsIntrinsicHtmlEncoder esThis = thisIntrinsicObject(ecx, EsIntrinsicHtmlEncoder.class);
				return new EsPrimitiveString(esThis.newString(ecx));
			}
		};
	}

	public static EsIntrinsicHtmlEncoderConstructor newInstance() {
		return new EsIntrinsicHtmlEncoderConstructor();
	}

	private EsIntrinsicHtmlEncoderConstructor() {
		super(ClassName, new String[] { "rootSource" }, 1);
	}
}
