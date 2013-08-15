/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.TimeZone;

import com.metservice.argon.ArgonApiException;
import com.metservice.argon.TimeFactors;
import com.metservice.argon.TimeFactors.AlignSense;
import com.metservice.argon.TimeFactors.CalendarUnit;
import com.metservice.argon.TimeFactors.NearestSense;
import com.metservice.argon.TimeMask;
import com.metservice.argon.TimeOfDayFactory;
import com.metservice.argon.TimeOfDayRule;

/**
 * 
 * @author roach
 */
public class EsIntrinsicTimefactorsConstructor extends EsIntrinsicConstructor {

	public static final String ClassName = "Timefactors";

	public static final EsIntrinsicMethod[] Methods = { method_nearest(), method_alignToCalendar(), method_alignToInterval(),
			method_valueOf(), method_toString() };

	@Override
	protected IEsOperand eval(EsExecutionContext ecx)
			throws InterruptedException {
		final EsMethodAccessor ac = new EsMethodAccessor(ecx);
		final long ts = ac.tsTimeValue(0);
		final EsIntrinsicTimezone timezone = ac.esIntrinsicTimezone(1);
		final TimeZone tz = timezone.timeZoneValue();
		final TimeFactors timeFactors = TimeFactors.newInstance(ts, tz);
		final EsIntrinsicTimefactors neo = ecx.thisObject(ClassName, EsIntrinsicTimefactors.class);
		neo.setValue(timeFactors);
		return neo;
	}

	@Override
	public EsObject declarePrototype(EsGlobal global) {
		return new EsIntrinsicTimefactors(global.prototypeObject);
	}

	private static EsIntrinsicMethod method_alignToCalendar() {
		return new EsIntrinsicMethod("alignToCalendar", new String[] { "calendarUnit", "sense" }, 2) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsIntrinsicTimefactors self = ecx.thisObject(ClassName, EsIntrinsicTimefactors.class);
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final String qtwCalendarUnit = ac.qtwStringValue(0);
				final String qtwSense = ac.qtwStringValue(1);
				final TimeFactors selfFactors = self.timeFactors();
				try {
					final CalendarUnit calendarUnit = TimeFactors.DecoderCalendarUnit.select(qtwCalendarUnit);
					final AlignSense alignSense = TimeFactors.DecoderAlignSense.select(qtwSense);
					final TimeFactors neo = selfFactors.newAlignedCalendar(calendarUnit, alignSense);
					return ecx.global().newIntrinsicTimefactors(neo);
				} catch (final ArgonApiException ex) {
					throw new EsApiCodeException(ex);
				}
			}
		};
	}

	private static EsIntrinsicMethod method_alignToInterval() {
		return new EsIntrinsicMethod("alignToInterval", new String[] { "interval", "sense" }, 2) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsIntrinsicTimefactors self = ecx.thisObject(ClassName, EsIntrinsicTimefactors.class);
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final long msInterval = ac.smsElapsedValue(0);
				final String qtwSense = ac.qtwStringValue(1);
				final TimeFactors selfFactors = self.timeFactors();
				try {
					final AlignSense alignSense = TimeFactors.DecoderAlignSense.select(qtwSense);
					final TimeFactors neo = selfFactors.newAlignedInterval(msInterval, alignSense);
					return ecx.global().newIntrinsicTimefactors(neo);
				} catch (final ArgonApiException ex) {
					throw new EsApiCodeException(ex);
				}
			}
		};
	}

	private static EsIntrinsicMethod method_nearest() {
		return new EsIntrinsicMethod("nearest", new String[] { "timesOfDay", "sense" }, 2) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsIntrinsicTimefactors self = ecx.thisObject(ClassName, EsIntrinsicTimefactors.class);
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final String qtwRule = ac.qtwStringValue(0);
				final String qtwSense = ac.qtwStringValue(1);
				final TimeFactors selfFactors = self.timeFactors();
				try {
					final TimeOfDayRule rule = TimeOfDayFactory.newRule(qtwRule);
					final NearestSense nearestSense = TimeFactors.DecoderNearestSense.select(qtwSense);
					final TimeFactors neo = selfFactors.newNearest(rule, nearestSense);
					return ecx.global().newIntrinsicTimefactors(neo);
				} catch (final ArgonApiException ex) {
					throw new EsApiCodeException(ex);
				}
			}
		};
	}

	private static EsIntrinsicMethod method_toString() {
		return new EsIntrinsicMethod("toString", new String[] { "mask" }, 0) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsIntrinsicTimefactors self = ecx.thisObject(ClassName, EsIntrinsicTimefactors.class);
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final EsIntrinsicTimemask esoTimemask = ac.defaulted(0) ? null : ac.esoIntrinsicTimemask(0);
				final TimeMask timeMask = esoTimemask == null ? TimeMask.Default : esoTimemask.timeMaskValue();
				return new EsPrimitiveString(timeMask.format(self.timeFactors()));
			}
		};
	}

	private static EsIntrinsicMethod method_valueOf() {
		return new EsIntrinsicMethod("valueOf") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsIntrinsicTimefactors self = ecx.thisObject(ClassName, EsIntrinsicTimefactors.class);
				return self.timeNumberPrimitive();
			}
		};
	}

	public static EsIntrinsicTimefactorsConstructor newInstance() {
		return new EsIntrinsicTimefactorsConstructor();
	}

	private EsIntrinsicTimefactorsConstructor() {
		super(ClassName, new String[] { "time", "timezone" }, 2);
	}
}
