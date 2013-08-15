/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @jsobject RegExp
 * @jsnote An object representing a regular expression.
 * @jsproperty source
 * @jsproperty global
 * @jsproperty ignoreCase
 * @jsproperty multiline
 * @jsproperty lastIndex
 * @jsproperty index
 * @jsproperty input
 * @author roach
 */
public class EsIntrinsicRegExpConstructor extends EsIntrinsicConstructor {

	public static final String ClassName = "RegExp";

	public static final EsIntrinsicMethod[] Methods = { method_capture(), method_matches(), method_split(), method_toString() };

	@Override
	protected IEsOperand eval(EsExecutionContext ecx)
			throws InterruptedException {
		final EsMethodAccessor ac = new EsMethodAccessor(ecx);
		final String qPattern = ac.qStringValue(0);
		final String zccFlags = ac.defaulted(1) ? "" : ac.ztwStringValue(1);
		int patternFlags = 0;
		final int flagCount = zccFlags.length();
		for (int i = 0; i < flagCount; i++) {
			final char cf = zccFlags.charAt(i);
			switch (cf) {
				case 'i': {
					patternFlags = patternFlags | Pattern.CASE_INSENSITIVE;
				}
				break;
				case 'm': {
					patternFlags = patternFlags | Pattern.MULTILINE;
				}
				break;
				default: {
					throw new EsApiCodeException("Unsupported regular expression flag '" + cf + "'");
				}
			}
		}

		try {
			final EsIntrinsicRegExp neo;
			final Pattern pattern = Pattern.compile(qPattern, patternFlags);
			if (calledAsFunction(ecx)) {
				neo = ecx.global().newIntrinsicRegExp(pattern);
			} else {
				neo = thisIntrinsicObject(ecx, EsIntrinsicRegExp.class);
				neo.setValue(pattern);
			}
			return neo;
		} catch (final PatternSyntaxException exPS) {
			throw new EsApiCodeException("Invalid regular expression: " + exPS.getMessage());
		}
	}

	@Override
	public EsObject declarePrototype(EsGlobal global) {
		return new EsIntrinsicRegExp(global.prototypeObject);
	}

	private static EsIntrinsicMethod method_capture() {
		return new EsIntrinsicMethod("capture", new String[] { "string" }, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final String zs = ac.zStringValue(0);
				final EsIntrinsicRegExp self = thisIntrinsicObject(ecx, EsIntrinsicRegExp.class);
				return self.capture(ecx, zs);
			}
		};
	}

	private static EsIntrinsicMethod method_matches() {
		return new EsIntrinsicMethod("matches", new String[] { "string" }, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final String zs = ac.zStringValue(0);
				final EsIntrinsicRegExp self = thisIntrinsicObject(ecx, EsIntrinsicRegExp.class);
				return EsPrimitiveBoolean.instance(self.pattern().matcher(zs).matches());
			}
		};
	}

	private static EsIntrinsicMethod method_split() {
		return new EsIntrinsicMethod("split", new String[] { "string", "trim", "retainEmpty" }, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final String zs = ac.zStringValue(0);
				final boolean trim = ac.defaulted(1) ? true : ac.booleanValue(1);
				final boolean retainEmpty = ac.defaulted(2) ? false : ac.booleanValue(2);
				final EsIntrinsicRegExp self = thisIntrinsicObject(ecx, EsIntrinsicRegExp.class);
				return self.split(ecx, zs, trim, retainEmpty);
			}
		};
	}

	// ECMA 15.10.6.4
	/**
	 * @jsmethod toString
	 * @jsreturn A string representation of the RegExp object.
	 */
	private static EsIntrinsicMethod method_toString() {
		return new EsIntrinsicMethod("toString") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx) {
				return thisIntrinsicObject(ecx, EsIntrinsicRegExp.class).toPrimitiveString();
			}
		};
	}

	public static EsIntrinsicRegExpConstructor newInstance() {
		return new EsIntrinsicRegExpConstructor();
	}

	/**
	 * @jsconstructor RegExp
	 * @jsparam pattern Required. A string containing the regular expression
	 * @jsparam flags Optional. A string. Containing any of the following: "g" for a global pattern, "i" for case
	 *          insensitive or "m" for multiline.
	 */
	private EsIntrinsicRegExpConstructor() {
		super(ClassName, new String[] { "pattern", "flags" }, 1);
	}
}
