/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

/**
 * @author roach
 */
public class EsIntrinsicJsonDecoderConstructor extends EsIntrinsicConstructor {

	public static final String ClassName = "JsonDecoder";

	public static final EsIntrinsicMethod[] Methods = { method_decode(), method_toObject(), method_toString() };

	@Override
	protected IEsOperand eval(EsExecutionContext ecx)
			throws InterruptedException {
		final EsMethodAccessor ac = new EsMethodAccessor(ecx);
		final String qtwSource = ac.qtwStringValue(0);
		final EsIntrinsicJsonDecoder neo;
		if (calledAsFunction(ecx)) {
			neo = ecx.global().newIntrinsicJsonDecoder(qtwSource);
		} else {
			neo = thisIntrinsicObject(ecx, EsIntrinsicJsonDecoder.class);
			neo.setSource(qtwSource);
		}
		return neo;
	}

	@Override
	public EsObject declarePrototype(EsGlobal global) {
		final EsIntrinsicJsonDecoder neo = new EsIntrinsicJsonDecoder(global.prototypeObject);
		neo.loadPrototype();
		return neo;
	}

	private static EsIntrinsicMethod method_decode() {
		return new EsIntrinsicMethod("decode") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsIntrinsicJsonDecoder esThis = thisIntrinsicObject(ecx, EsIntrinsicJsonDecoder.class);
				return esThis.decode(ecx, true);
			}
		};
	}

	private static EsIntrinsicMethod method_toObject() {
		return new EsIntrinsicMethod("toObject") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsIntrinsicJsonDecoder esThis = thisIntrinsicObject(ecx, EsIntrinsicJsonDecoder.class);
				return esThis.decode(ecx, false);
			}
		};
	}

	private static EsIntrinsicMethod method_toString() {
		return new EsIntrinsicMethod("toString") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsIntrinsicJsonDecoder esThis = thisIntrinsicObject(ecx, EsIntrinsicJsonDecoder.class);
				return esThis.toPrimitiveString(ecx);
			}
		};
	}

	public static EsIntrinsicJsonDecoderConstructor newInstance() {
		return new EsIntrinsicJsonDecoderConstructor();
	}

	private EsIntrinsicJsonDecoderConstructor() {
		super(ClassName, new String[] { "source" }, 1);
	}
}
