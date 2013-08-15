/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

import com.metservice.argon.ArgonApiException;
import com.metservice.argon.ArgonFormatException;
import com.metservice.argon.ArgonText;
import com.metservice.argon.DateFactory;
import com.metservice.argon.ElapsedFactory;
import com.metservice.argon.TimeZoneFactory;

/**
 * @jsobject String
 * @author roach
 */
public class EsIntrinsicStringConstructor extends EsIntrinsicConstructor {

	public static final String ClassName = "String";

	public static final EsIntrinsicMethod[] Methods = { method_toString(), method_valueOf(), method_charAt(), method_indexOf(),
			method_lastIndexOf(), method_replace(), method_search(), method_slice(), method_split(), method_substring(),
			method_toLowerCase(), method_toUpperCase(), method_trim(), method_startsWith(), method_endsWith(),
			method_contains(), method_toTime(), method_toTimezone(), method_toElapsed(), method_toLines(), method_toFields(),
			method_bind(), method_tidySuffix(), method_findFunction(), method_encodeUtf8(), method_encodeIso8859(),
			method_encodeAscii() };

	private static EsIntrinsicMethod method_bind() {
		return new EsIntrinsicMethod("bind", new String[] { "parameters", "sitePattern" }, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final EsObject oParameters = ac.esoObject(0);
				final Pattern oSitePattern = ac.defaulted(1) ? null : ac.oPattern(1);
				if (oParameters == null) return ecx.thisObject();
				final EsPrimitiveString self = thisToPrimitiveString(ecx);
				return self.bind(ecx, oParameters, oSitePattern);
			}
		};
	}

	// ECMA 15.5.4.4
	/**
	 * @jsmethod charAt
	 * @jsparam pos The position to get the character from. Optional.
	 * @jsnote get the character from a given position of the string
	 */
	private static EsIntrinsicMethod method_charAt() {
		return new EsIntrinsicMethod("charAt", new String[] { "pos" }, 0) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsArguments args = ecx.activation().arguments();
				final EsPrimitiveNumber pos = args.primitiveNumber(ecx, 0);
				return thisToPrimitiveString(ecx).charAt(pos);
			}
		};
	}

	/**
	 * @jsmethod contains
	 * @jsparam searchString The sub string to search for. Required.
	 * @jsparam ignoreCase Optional - default is false (i.e. case sensitive).
	 * @jsreturn true if this String contains the searchString.
	 */
	private static EsIntrinsicMethod method_contains() {
		return new EsIntrinsicMethod("contains", new String[] { "searchString", "ignoreCase" }, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final String zSearchString = ac.zStringValue(0);
				final boolean ignoreCase = ac.defaulted(1) ? false : ac.booleanValue(1);
				return thisToPrimitiveString(ecx).contains(zSearchString, ignoreCase);
			}
		};
	}

	private static EsIntrinsicMethod method_encodeAscii() {
		return new EsIntrinsicMethod("encodeAscii") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return thisToPrimitiveString(ecx).toBinary(ecx, ArgonText.ASCII);
			}
		};
	}

	private static EsIntrinsicMethod method_encodeIso8859() {
		return new EsIntrinsicMethod("encodeIso8859") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return thisToPrimitiveString(ecx).toBinary(ecx, ArgonText.ISO8859_1);
			}
		};
	}

	private static EsIntrinsicMethod method_encodeUtf8() {
		return new EsIntrinsicMethod("encodeUtf8") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return thisToPrimitiveString(ecx).toBinary(ecx, ArgonText.UTF8);
			}
		};
	}

	private static EsIntrinsicMethod method_endsWith() {
		return new EsIntrinsicMethod("endsWith", new String[] { "searchString", "ignoreCase" }, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final String zSearchString = ac.zStringValue(0);
				final boolean ignoreCase = ac.defaulted(1) ? false : ac.booleanValue(1);
				return thisToPrimitiveString(ecx).endsWith(zSearchString, ignoreCase);
			}
		};
	}

	private static EsIntrinsicMethod method_findFunction() {
		return new EsIntrinsicMethod("findFunction", new String[] { "defaultFunction" }, 0) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				EsFunction oFunction = null;
				final String ztwName = ecx.thisObject().toCanonicalString(ecx).trim();
				if (ztwName.length() > 0) {
					final EsReference ref = ecx.scopeChain().resolve(ztwName);
					final EsObject oBase = ref.getBase();
					if (oBase != null) {
						final IEsOperand esf = oBase.esGet(ztwName);
						if (esf instanceof EsFunction) {
							oFunction = (EsFunction) esf;
						}
					}
				}
				if (oFunction == null && !ac.defaulted(0)) {
					oFunction = ac.esFunction(0);
				}
				return oFunction == null ? EsPrimitiveUndefined.Instance : oFunction;
			}
		};
	}

	// ECMA 15.5.4.7
	/**
	 * @jsmethod indexOf
	 * @jsparam searchString Required. The sub string to search for.
	 * @jsparam position Optional. The position to start the search from.
	 * @jsreturn the index of the first occurence of the sub string in the string.
	 * 
	 */
	private static EsIntrinsicMethod method_indexOf() {
		return new EsIntrinsicMethod("indexOf", new String[] { "searchString", "position" }, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsArguments args = ecx.activation().arguments();
				final EsPrimitiveString searchString = args.primitiveString(ecx, 0);
				final EsPrimitive position = args.primitive(ecx, 1);
				if (position instanceof EsPrimitiveUndefined) return thisToPrimitiveString(ecx).indexOf(searchString);
				return thisToPrimitiveString(ecx).indexOf(searchString, position.toNumber(ecx));
			}
		};
	}

	// ECMA 15.5.4.8
	/**
	 * @jsmethod lastIndexOf
	 * @jsparam searchString The sub string to search for. Required.
	 * @jsparam position Optional. The position to start the search from.
	 * @jsreturn The index to the last occurance of the sub string in the string
	 */
	private static EsIntrinsicMethod method_lastIndexOf() {
		return new EsIntrinsicMethod("lastIndexOf", new String[] { "searchString", "position" }, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsArguments args = ecx.activation().arguments();
				final EsPrimitiveString searchString = args.primitiveString(ecx, 0);
				final EsPrimitive position = args.primitive(ecx, 1);
				if (position instanceof EsPrimitiveUndefined) return thisToPrimitiveString(ecx).lastIndexOf(searchString);
				return thisToPrimitiveString(ecx).lastIndexOf(searchString, position.toNumber(ecx));
			}
		};
	}

	// ECMA 15.5.4.11
	/**
	 * @jsmethod replace
	 * @jsparam searchValue RegExp object, or an object with a toString() method.
	 * @jsparam replaceValue Function, or an object with a toString() method.
	 * @jsparam flags Optional String containing flags (g and/or i) applied to searchValue if it is not a RegExp
	 * @jsreturn A string based upon this string, but with each sub string matching searchValue replaced with (or
	 *           using) replaceValue.
	 */
	private static EsIntrinsicMethod method_replace() {
		return new EsIntrinsicMethod("replace", new String[] { "searchValue", "replaceValue", "flags" }, 2) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final EsPrimitiveString self = thisToPrimitiveString(ecx);
				final IEsOperand searchOperand = ac.esOperandDatum(0);
				final IEsOperand replaceOperand = ac.esOperandDatum(1);
				final String zFlags = ac.defaulted(2) ? "" : ac.zStringValue(2).toLowerCase();
				final Pattern pattern;
				final boolean global;
				if (searchOperand instanceof EsIntrinsicRegExp) {
					final EsIntrinsicRegExp searchRegExp = (EsIntrinsicRegExp) searchOperand;
					pattern = searchRegExp.pattern();
					global = false;
				} else {
					final String zSearch = searchOperand.toCanonicalString(ecx);
					int pflags = Pattern.LITERAL;
					if (zFlags.contains("i")) {
						pflags = pflags | Pattern.CASE_INSENSITIVE;
					}
					pattern = Pattern.compile(zSearch, pflags);
					global = zFlags.contains("g");
				}

				if (replaceOperand instanceof EsFunction) {
					final EsFunction fnReplace = (EsFunction) replaceOperand;
					return self.replace(ecx, pattern, global, fnReplace, null);
				}
				final String zExprReplace = replaceOperand.toCanonicalString(ecx);
				return self.replace(ecx, pattern, global, null, zExprReplace);
			}
		};
	}

	// ECMA 15.5.4.12
	/**
	 * @jsmethod search
	 * @jsparam regexp A RegExp object, optional.
	 * @jsreturn An IntegerNumber containing the index of the first match.
	 */
	private static EsIntrinsicMethod method_search() {
		return new EsIntrinsicMethod("search", new String[] { "regexp" }, 0) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final EsPrimitiveString self = thisToPrimitiveString(ecx);
				final Pattern pattern = ac.pattern(0);
				return self.search(ecx, pattern);
			}
		};
	}

	// ECMA 15.5.4.13
	/**
	 * @jsmethod slice
	 * @jsparam start A Number. Optional
	 * @jsparam end A Number. Optional
	 * @jsreturn The substring defined by the slice start and end indices.
	 */
	private static EsIntrinsicMethod method_slice() {
		return new EsIntrinsicMethod("slice", new String[] { "start", "end" }, 0) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsArguments args = ecx.activation().arguments();
				final EsPrimitiveNumber start = args.primitiveNumber(ecx, 0);
				final EsPrimitive end = args.primitive(ecx, 1);
				if (end instanceof EsPrimitiveUndefined) return thisToPrimitiveString(ecx).slice(start);
				return thisToPrimitiveString(ecx).slice(start, end.toNumber(ecx));
			}
		};
	}

	// ECMA 15.5.4.14
	private static EsIntrinsicMethod method_split() {
		return new EsIntrinsicMethod("split", new String[] { "separator", "limit" }, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsPrimitiveString self = thisToPrimitiveString(ecx);
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final IEsOperand separatorNonNull = ac.esOperandDatum(0);
				final int limit = ac.defaulted(1) ? Integer.MAX_VALUE : ac.intValue(1);
				if (limit == 0) return ecx.global().newIntrinsicArray();
				if (separatorNonNull instanceof EsIntrinsicRegExp) {
					final EsIntrinsicRegExp regexp = (EsIntrinsicRegExp) separatorNonNull;
					return self.split(ecx, regexp.pattern(), limit);
				}
				return self.split(ecx, separatorNonNull.toCanonicalString(ecx), limit);
			}
		};
	}

	private static EsIntrinsicMethod method_startsWith() {
		return new EsIntrinsicMethod("startsWith", new String[] { "searchString", "ignoreCase", "position" }, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final String zSearchString = ac.zStringValue(0);
				final boolean ignoreCase = ac.defaulted(1) ? false : ac.booleanValue(1);
				final int position = ac.defaulted(2) ? 0 : ac.intValue(2);
				return thisToPrimitiveString(ecx).startsWith(zSearchString, ignoreCase, position);
			}
		};
	}

	// ECMA 15.5.4.15
	/**
	 * @jsmethod substring
	 * @jsparam start The start index of the substring to extract.
	 * @jsparam end The end index.
	 * @jsreturn A string containing the substring between the start and end indices.
	 */
	private static EsIntrinsicMethod method_substring() {
		return new EsIntrinsicMethod("substring", new String[] { "start", "end" }, 0) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsArguments args = ecx.activation().arguments();
				final EsPrimitiveNumber start = args.primitiveNumber(ecx, 0);
				final EsPrimitive end = args.primitive(ecx, 1);
				if (end instanceof EsPrimitiveUndefined) return thisToPrimitiveString(ecx).substring(start);
				return thisToPrimitiveString(ecx).substring(start, end.toNumber(ecx));
			}
		};
	}

	private static EsIntrinsicMethod method_tidySuffix() {
		return new EsIntrinsicMethod("tidySuffix", new String[] { "suffix", "ensure" }, 2) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final String qSuffix = ac.qStringValue(0);
				final boolean ensure = ac.booleanValue(1);
				return thisToPrimitiveString(ecx).tidySuffix(qSuffix, ensure);
			}
		};
	}

	/**
	 * @jsmethod toElapsed
	 * @jsreturn An elapsed time number if this string is not empty and represents a well-formed elapsed time
	 *           expression; otherwise null.
	 */
	private static EsIntrinsicMethod method_toElapsed() {
		return new EsIntrinsicMethod("toElapsed") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final String zExpr = ecx.thisObject().toCanonicalString(ecx);
				if (zExpr.length() == 0) return EsPrimitiveNull.Instance;
				try {
					return new EsPrimitiveNumberElapsed(ElapsedFactory.ms(zExpr));
				} catch (final ArgonFormatException ex) {
				}
				return EsPrimitiveNull.Instance;
			}
		};
	}

	/**
	 * @jsmethod toFields
	 * @jsnote Splits
	 * @jsparam delimiter. A String. Optional. Defaults to ",".
	 * @jsreturn An array of elements from the string.
	 */
	private static EsIntrinsicMethod method_toFields() {
		return new EsIntrinsicMethod("toFields", new String[] { "delimiter" }, 0) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final String qDelimiter = ac.defaulted(0) ? "," : ac.qStringValue(0);
				if (qDelimiter.length() > 1)
					throw new EsApiCodeException("Delimiter '" + qDelimiter + "' is too long; must be a single character");
				final char delimiter = qDelimiter.charAt(0);
				return thisToPrimitiveString(ecx).toFields(ecx, delimiter);
			}
		};
	}

	private static EsIntrinsicMethod method_toLines() {
		return new EsIntrinsicMethod("toLines", new String[] { "trim", "retainBlanks", "inPattern", "exPattern" }, 0) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final boolean trim = ac.defaulted(0) ? true : ac.booleanValue(0);
				final boolean retainBlankLines = ac.defaulted(1) ? false : ac.booleanValue(1);
				final Pattern oInPattern = ac.defaulted(2) ? null : ac.oPattern(2);
				final Pattern oExPattern = ac.defaulted(3) ? null : ac.oPattern(3);
				return thisToPrimitiveString(ecx).toLines(ecx, trim, retainBlankLines, oInPattern, oExPattern);
			}
		};
	}

	// ECMA 15.5.4.16
	/**
	 * @jsmethod toLowerCase
	 * @jsreturn A copy of the string converted to lower case.
	 */
	private static EsIntrinsicMethod method_toLowerCase() {
		return new EsIntrinsicMethod("toLowerCase") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return thisToPrimitiveString(ecx).toLowerCase();
			}
		};
	}

	// ECMA 15.5.4.2
	/**
	 * @jsmethod toString
	 * @jsreturn A String of the string.
	 */
	private static EsIntrinsicMethod method_toString() {
		return new EsIntrinsicMethod("toString", NOARGS, 0) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx) {
				return thisIntrinsicObject(ecx, EsIntrinsicString.class).value();
			}
		};
	}

	private static EsIntrinsicMethod method_toTime() {
		return new EsIntrinsicMethod("toTime", new String[] { "regex", "reorder", "timezone", "now" }, 0) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final EsIntrinsicRegExp oRegex = ac.defaulted(0) ? null : ac.esoIntrinsicRegExp(0);
				final EsArgumentAccessor oaReorder = ac.find(1);
				final int[] ozptGroupReorder = oaReorder == null ? null : oaReorder.ozptIntValuesEvery();
				final EsIntrinsicTimezone oTimezone = ac.defaulted(2) ? null : ac.esoIntrinsicTimezone(2);
				final Date oNow = ac.defaulted(3) ? null : DateFactory.newDate(ac.tsTimeValue(3));
				final String qtwSelf = ac.qtwStringValueThis();
				final Pattern oPattern = oRegex == null ? null : oRegex.pattern();
				final TimeZone timeZone = oTimezone == null ? TimeZoneFactory.GMT : oTimezone.timeZoneValue();
				try {
					final Date time = DateFactory.newInstance(qtwSelf, oPattern, ozptGroupReorder, timeZone, oNow);
					return new EsPrimitiveNumberTime(time);
				} catch (final ArgonFormatException ex) {
					return EsPrimitiveNull.Instance;
				} catch (final ArgonApiException ex) {
					throw new EsApiCodeException(ex);
				}
			}
		};
	}

	private static EsIntrinsicMethod method_toTimezone() {
		return new EsIntrinsicMethod("toTimezone") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final String zSelf = ecx.thisObject().toCanonicalString(ecx);
				if (zSelf.length() == 0) return EsPrimitiveNull.Instance;
				final TimeZone oTimeZone = TimeZoneFactory.findById(zSelf);
				if (oTimeZone == null) return EsPrimitiveNull.Instance;
				return ecx.global().newIntrinsicTimezone(oTimeZone);
			}
		};
	}

	// ECMA 15.5.4.18
	/**
	 * @jsmethod toUpperCase
	 * @jsreturn A copy of the string converted to all lower case
	 */
	private static EsIntrinsicMethod method_toUpperCase() {
		return new EsIntrinsicMethod("toUpperCase") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return thisToPrimitiveString(ecx).toUpperCase();
			}
		};
	}

	/**
	 * @jsmethod trim
	 * @jsreturn A copy of the string with the leading and trailing whitespace removed.
	 */
	private static EsIntrinsicMethod method_trim() {
		return new EsIntrinsicMethod("trim") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return thisToPrimitiveString(ecx).trim();
			}
		};
	}

	// ECMA 15.5.4.3
	/**
	 * @jsmethod valueOf
	 * @jsreturn The string
	 */
	private static EsIntrinsicMethod method_valueOf() {
		return new EsIntrinsicMethod("valueOf", NOARGS, 0) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx) {
				return thisIntrinsicObject(ecx, EsIntrinsicString.class).value();
			}
		};
	}

	// Private
	static EsPrimitiveString thisToPrimitiveString(EsExecutionContext ecx)
			throws InterruptedException {
		return new EsPrimitiveString(ecx.thisObject().toCanonicalString(ecx));
	}

	public static EsIntrinsicStringConstructor newInstance() {
		return new EsIntrinsicStringConstructor();
	}

	@Override
	protected IEsOperand eval(EsExecutionContext ecx)
			throws InterruptedException {
		final EsActivation activation = ecx.activation();
		final IEsOperand value = activation.esGet(CProp.value);
		final EsPrimitiveString stringValue;
		if (value instanceof EsPrimitiveUndefined) {
			stringValue = EsPrimitiveString.EMPTY;
		} else {
			stringValue = new EsPrimitiveString(value.toCanonicalString(ecx));
		}
		if (calledAsFunction(ecx)) return stringValue;
		final EsIntrinsicString neo = (EsIntrinsicString) ecx.thisObject();
		neo.setValue(stringValue);
		return null;
	}

	@Override
	public EsObject declarePrototype(EsGlobal global) {
		return new EsIntrinsicString(global.prototypeObject);
	}

	/**
	 * @jsconstructor String
	 * @jsparam value Optional
	 */
	private EsIntrinsicStringConstructor() {
		super(ClassName, new String[] { "value" }, 0);
	}
}
