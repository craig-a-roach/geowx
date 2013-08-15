/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.TimeZone;

import com.metservice.argon.ArgonApiException;
import com.metservice.argon.TimeFactors;
import com.metservice.argon.TimeZoneFactory;

/**
 * 
 * @author roach
 */
public class EsIntrinsicTimezoneConstructor extends EsIntrinsicConstructor {

	public static final String ClassName = "Timezone";

	public static final EsIntrinsicMethod[] Methods = { method_timeFactors(), method_equivalentTo(), method_toString() };

	@Override
	protected IEsOperand eval(EsExecutionContext ecx)
			throws InterruptedException {
		final EsMethodAccessor ac = new EsMethodAccessor(ecx);
		final String oqtwTimezoneID = ac.defaulted(0) ? null : ac.qtwStringValue(0);
		final TimeZone timeZone;
		if (oqtwTimezoneID == null) {
			timeZone = TimeZoneFactory.GMT;
		} else {
			try {
				timeZone = TimeZoneFactory.selectById(oqtwTimezoneID);
			} catch (final ArgonApiException ex) {
				throw new EsApiCodeException(ex);
			}
		}

		final EsIntrinsicTimezone neo;
		if (calledAsFunction(ecx)) {
			neo = ecx.global().newIntrinsicTimezone(timeZone);
		} else {
			neo = ecx.thisObject(ClassName, EsIntrinsicTimezone.class);
			neo.setValue(timeZone);
		}
		return neo;
	}

	@Override
	public EsObject declarePrototype(EsGlobal global) {
		return new EsIntrinsicTimezone(global.prototypeObject);
	}

	private static EsIntrinsicMethod method_equivalentTo() {
		return new EsIntrinsicMethod("equivalentTo", new String[] { "rhs" }, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsIntrinsicTimezone self = self(ecx);
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final IEsOperand esRhs = ac.esOperandDatum(0);
				final TimeZone rhs;
				if (esRhs instanceof EsIntrinsicTimezone) {
					rhs = ((EsIntrinsicTimezone) esRhs).timeZoneValue();
				} else {
					try {
						rhs = TimeZoneFactory.selectById(ac.qtwStringValue(0));
					} catch (final ArgonApiException ex) {
						throw new EsApiCodeException(ex);
					}
				}
				return EsPrimitiveBoolean.instance(self.equivalentTo(rhs));
			}
		};
	}

	private static EsIntrinsicMethod method_timeFactors() {
		return new EsIntrinsicMethod("timeFactors", new String[] { "time" }, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsIntrinsicTimezone self = self(ecx);
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final long ts = ac.tsTimeValue(0);
				final TimeZone timeZone = self.timeZoneValue();
				final TimeFactors timeFactors = TimeFactors.newInstance(ts, timeZone);
				return ecx.global().newIntrinsicTimefactors(timeFactors);
			}
		};
	}

	private static EsIntrinsicMethod method_toString() {
		return new EsIntrinsicMethod("toString") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx) {
				final EsIntrinsicTimezone self = self(ecx);
				return self.toPrimitiveString();
			}
		};
	}

	static EsIntrinsicTimezone self(EsExecutionContext ecx) {
		return ecx.thisObject(ClassName, EsIntrinsicTimezone.class);
	}

	public static EsIntrinsicTimezoneConstructor newInstance() {
		return new EsIntrinsicTimezoneConstructor();
	}

	private EsIntrinsicTimezoneConstructor() {
		super(ClassName, new String[] { "value" }, 0);
	}
}
