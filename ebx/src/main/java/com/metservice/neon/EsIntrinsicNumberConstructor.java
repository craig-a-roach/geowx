/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.TimeZone;

import com.metservice.argon.DecimalMask;
import com.metservice.argon.TimeFactors;
import com.metservice.argon.TimeMask;
import com.metservice.argon.TimeZoneFactory;

/**
 * @jsobject Number
 * @jsnote The following numerical operations are defined:
 * @jsnote multiplication *
 * @jsnote division /
 * @jsnote remainder %
 * @jsnote addition +
 * @jsnote subtraction -
 * @jsnote
 * @jsnote The following Number subtypes are supported:
 * @jsnote NaN - not a number, Integer, Time, Elapsed, Real and Double.
 * @jsnote
 * @jsnote For Time and Elapsed numbers the following operator rules apply:
 * @jsnote <ul>
 *         <li>Time - Time = Elapsed</li>
 * @jsnote <li>Time + Number = Time</li>
 * @jsnote <li>Time - Number = Time</li>
 * @jsnote <li>Number + Time = Time</li>
 * @jsnote <li>Number - Time = Time</li>
 * @jsnote <li>Time + Elapsed = Time</li>
 * @jsnote <li>Time - Elapsed = Time</li>
 * @jsnote <li>Elapsed + Time = Time</li>
 * @jsnote <li>Elapsed - Time = Time</li>
 * @jsnote <li>Elapsed / Elapsed = Number</li>
 * @jsnote <li>Elapsed op Number = Elapsed</li>
 * @jsnote <li>Number op Elapsed = Elapsed</li>
 *         </ul>
 * @jsnote Where Number is Integer, Real or Double.
 * @jsnote Any other combination will result in a NaN.
 */
public class EsIntrinsicNumberConstructor extends EsIntrinsicConstructor {

	public static final String ClassName = "Number";
	public static final EsIntrinsicMethod[] Methods = { method_toString(), method_valueOf() };

	// ECMA 15.7.4.2
	/**
	 * @jsmethod toString
	 * @jsparam mask A DecimalMask object specifying the format for the string.
	 * @jsreturn The number as a string.
	 */
	private static EsIntrinsicMethod method_toString() {
		return new EsIntrinsicMethod("toString", new String[] { "mask", "timezone" }, 0) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final EsIntrinsicNumber self = thisIntrinsicObject(ecx, EsIntrinsicNumber.class);
				final EsPrimitiveNumber value = self.value();
				if (ac.argc == 0) return value.toPrimitiveString(ecx);
				final EsObject maskObject = ac.esObject(0);
				if (maskObject instanceof EsIntrinsicDecimalmask) {
					final EsIntrinsicDecimalmask decimalMaskObject = (EsIntrinsicDecimalmask) maskObject;
					final DecimalMask decimalMask = decimalMaskObject.decimalMaskValue();
					return new EsPrimitiveString(value.toCanonicalString(decimalMask));
				}
				if (maskObject instanceof EsIntrinsicTimemask) {
					final EsIntrinsicTimemask timeMaskObject = (EsIntrinsicTimemask) maskObject;
					final TimeMask timeMask = timeMaskObject.timeMaskValue();
					final long ts = value.longValue();
					final EsIntrinsicTimezone oInTimezone = ac.defaulted(1) ? null : ac.esoIntrinsicTimezone(1);
					final TimeZone timeZone = oInTimezone == null ? TimeZoneFactory.GMT : oInTimezone.timeZoneValue();
					final TimeFactors timeFactors = TimeFactors.newInstance(ts, timeZone);
					return new EsPrimitiveString(timeMask.format(timeFactors));
				}
				throw new EsApiCodeException("Invalid mask object type: '" + maskObject.esClass() + "'");
			}
		};
	}

	// ECMA 15.7.4.4
	/**
	 * @jsmethod valueOf
	 * @jsreturn The number.
	 */
	private static EsIntrinsicMethod method_valueOf() {
		return new EsIntrinsicMethod("valueOf") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx) {
				return ecx.thisObject(ClassName, EsIntrinsicNumber.class).value();
			}
		};
	}

	public static EsIntrinsicNumberConstructor newInstance() {
		return new EsIntrinsicNumberConstructor();
	}

	@Override
	protected IEsOperand eval(EsExecutionContext ecx)
			throws InterruptedException {
		final EsActivation activation = ecx.activation();
		final IEsOperand value = activation.esGet(CProp.value);
		final EsPrimitiveNumber numberValue;
		if (value instanceof EsPrimitiveUndefined) {
			numberValue = EsPrimitiveNumberInteger.ZERO;
		} else {
			numberValue = value.toNumber(ecx);
		}
		if (calledAsFunction(ecx)) return numberValue;

		final EsIntrinsicNumber neo = (EsIntrinsicNumber) ecx.thisObject();
		neo.setValue(numberValue);
		return null;
	}

	@Override
	public EsObject declarePrototype(EsGlobal global) {
		return new EsIntrinsicNumber(global.prototypeObject);
	}

	/**
	 * @jsconstructor Number
	 * @jsparam value Optional. The initial value of the number.
	 */
	private EsIntrinsicNumberConstructor() {
		super(ClassName, new String[] { "value" }, 0);
	}
}
