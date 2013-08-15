/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.ArgonApiException;
import com.metservice.argon.TimeMask;

/**
 * 
 * @author roach
 */
public class EsIntrinsicTimemaskConstructor extends EsIntrinsicConstructor {

	public static final String ClassName = "Timemask";

	public static final EsIntrinsicMethod[] Methods = { method_toString() };

	@Override
	protected IEsOperand eval(EsExecutionContext ecx)
			throws InterruptedException {
		final EsActivation activation = ecx.activation();
		final EsArguments arguments = activation.arguments();
		final int argCount = arguments.length();
		final StringBuilder b = new StringBuilder();
		for (int index = 0; index < argCount; index++) {
			final IEsOperand arg = arguments.operand(index);
			final String sarg = arg.toCanonicalString(ecx);
			b.append(sarg);
		}

		try {
			final TimeMask timeMask = TimeMask.newInstance(b.toString());
			final EsIntrinsicTimemask neo;
			if (calledAsFunction(ecx)) {
				neo = ecx.global().newIntrinsicTimemask(timeMask);
			} else {
				neo = ecx.thisObject(ClassName, EsIntrinsicTimemask.class);
				neo.setValue(timeMask);
			}
			return neo;
		} catch (final ArgonApiException ex) {
			throw new EsApiCodeException(ex);
		}
	}

	@Override
	public EsObject declarePrototype(EsGlobal global) {
		return new EsIntrinsicTimemask(global.prototypeObject);
	}

	private static EsIntrinsicMethod method_toString() {
		return new EsIntrinsicMethod("toString") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx) {
				return ecx.thisObject(ClassName, EsIntrinsicTimemask.class).toPrimitiveString();
			}
		};
	}

	public static EsIntrinsicTimemaskConstructor newInstance() {
		return new EsIntrinsicTimemaskConstructor();
	}

	private EsIntrinsicTimemaskConstructor() {
		super(ClassName, new String[] { "value" }, 0);
	}
}
