/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.ArgonApiException;
import com.metservice.argon.DecimalMask;

/**
 * 
 * @author roach
 */
public class EsIntrinsicDecimalmaskConstructor extends EsIntrinsicConstructor {
	@Override
	protected IEsOperand eval(EsExecutionContext ecx)
			throws InterruptedException {
		final EsActivation activation = ecx.activation();
		final EsArguments arguments = activation.arguments();
		final int argCount = arguments.length();
		final String zMaskPattern = argCount == 0 ? "" : arguments.operand(0).toCanonicalString(ecx);
		try {
			final DecimalMask decimalMask = DecimalMask.newInstance(zMaskPattern);
			final EsIntrinsicDecimalmask neo;
			if (calledAsFunction(ecx)) {
				neo = ecx.global().newIntrinsicDecimalmask(decimalMask);
			} else {
				neo = ecx.thisObject(ClassName, EsIntrinsicDecimalmask.class);
				neo.setValue(decimalMask);
			}
			return neo;
		} catch (final ArgonApiException ex) {
			throw new EsApiCodeException(ex);
		}
	}

	@Override
	public EsObject declarePrototype(EsGlobal global) {
		return new EsIntrinsicDecimalmask(global.prototypeObject);
	}

	/**
	 * @jsmethod toString
	 * @jsreturn A string representation of the mask
	 */
	private static EsIntrinsicMethod method_toString() {
		return new EsIntrinsicMethod("toString", NOARGS, 0) {
			@Override
			protected IEsOperand eval(EsExecutionContext ecx) {
				return ecx.thisObject(ClassName, EsIntrinsicDecimalmask.class).toPrimitiveString();
			}
		};
	}

	public static EsIntrinsicDecimalmaskConstructor newInstance() {
		return new EsIntrinsicDecimalmaskConstructor();
	}

	/**
	 * @jsconstructor Decimalmask
	 * @jsparam maskPattern Optional. A string defining the mask format.
	 */
	private EsIntrinsicDecimalmaskConstructor() {
		super(ClassName, new String[] { "maskPattern" }, 0);
	}

	public static final String ClassName = "Decimalmask";

	public static final EsIntrinsicMethod[] Methods = { method_toString() };
}
