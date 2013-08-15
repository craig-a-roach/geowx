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
public class EsIntrinsicXmlEncoderConstructor extends EsIntrinsicConstructor {

	@Override
	protected IEsOperand eval(EsExecutionContext ecx)
			throws InterruptedException {
		final EsMethodAccessor ac = new EsMethodAccessor(ecx);
		final String qtwRootTag = ac.qtwStringValue(0);
		final EsObject oRootSource = ac.esoObject(1);
		final EsIntrinsicXmlEncoder neo;
		if (calledAsFunction(ecx)) {
			neo = ecx.global().newIntrinsicXmlEncoder(qtwRootTag, oRootSource);
		} else {
			neo = thisIntrinsicObject(ecx, EsIntrinsicXmlEncoder.class);
			neo.setRoot(qtwRootTag, oRootSource);
		}
		return neo;
	}

	@Override
	public EsObject declarePrototype(EsGlobal global) {
		final EsIntrinsicXmlEncoder neo = new EsIntrinsicXmlEncoder(global.prototypeObject);
		neo.loadPrototype();
		return neo;
	}

	private static EsIntrinsicMethod method_toBinary() {
		return new EsIntrinsicMethod("toBinary") {
			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsIntrinsicXmlEncoder esThis = thisIntrinsicObject(ecx, EsIntrinsicXmlEncoder.class);
				return ecx.global().newIntrinsicBinary(esThis.newBinary(ecx));
			}
		};
	}

	private static EsIntrinsicMethod method_toString() {
		return new EsIntrinsicMethod("toString") {
			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsIntrinsicXmlEncoder esThis = thisIntrinsicObject(ecx, EsIntrinsicXmlEncoder.class);
				return new EsPrimitiveString(esThis.newString(ecx));
			}
		};
	}

	public static EsIntrinsicXmlEncoderConstructor newInstance() {
		return new EsIntrinsicXmlEncoderConstructor();
	}

	private EsIntrinsicXmlEncoderConstructor() {
		super(ClassName, new String[] { "rootTag", "rootSource", "typeUri" }, 2);
	}

	public static final String ClassName = "XmlEncoder";
	public static final EsIntrinsicMethod[] Methods = { method_toString(), method_toBinary() };
}
