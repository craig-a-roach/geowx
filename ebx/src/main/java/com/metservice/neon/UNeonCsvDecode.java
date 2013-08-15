/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.ArgonFormatException;
import com.metservice.argon.ArgonText;
import com.metservice.argon.DateFactory;
import com.metservice.argon.ElapsedFactory;
import com.metservice.argon.text.ArgonSplitter;

/**
 * @author roach
 */
class UNeonCsvDecode {

	public static final String TypeSuffix_Name = "Name";
	public static final String TypeSuffix_Time = "Time";
	public static final String TypeSuffix_Elapsed = "Elapsed";
	public static final String TypeSuffix_Count = "Count";
	public static final String TypeSuffix_Flag = "Flag";
	public static final char DefaultDelimiter = '|';

	private static char findDelimiter(String qtwHeader) {
		final int len = qtwHeader.length();
		for (int i = 0; i < len; i++) {
			final char ch = qtwHeader.charAt(i);
			if (!ArgonText.isEcmaName(ch, (i == 0)) && !ArgonText.isWhitespace(ch)) return ch;
		}
		return DefaultDelimiter;
	}

	private static DecodeRule newDecodeRule(String qName) {
		assert qName != null;
		final int posType = qName.indexOf('$');
		if (posType <= 0) return new DecodeRuleDefault(qName);
		final String qBase = qName.substring(0, posType);
		final String zType = qName.substring(posType + 1);
		final String pname = qBase + zType;
		if (zType.equals(TypeSuffix_Time)) return new DecodeRuleTime(pname);
		if (zType.equals(TypeSuffix_Name)) return new DecodeRuleName(pname);
		if (zType.equals(TypeSuffix_Count)) return new DecodeRuleCount(pname);
		if (zType.equals(TypeSuffix_Elapsed)) return new DecodeRuleElapsed(pname);
		if (zType.equals(TypeSuffix_Flag)) return new DecodeRuleFlag(pname);
		return new DecodeRuleDefault(qName);
	}

	public static EsObject newMap(EsExecutionContext ecx, EsObject src, String ozErrorSuffix)
			throws InterruptedException {
		if (src == null) throw new IllegalArgumentException("object is null");
		final int rowCount = UNeon.length(ecx, src);
		final EsIntrinsicObject esMap = ecx.global().newIntrinsicObject();
		if (rowCount < 2) return esMap;
		final String ztwHeader = UNeon.espropertyByIndex(src, 0).toCanonicalString(ecx).trim();
		if (ztwHeader.length() == 0) return esMap;
		final char delimiter = findDelimiter(ztwHeader);
		final String[] zptqtwHeader = ArgonSplitter.zptqtwSplit(ztwHeader, delimiter);
		final int colCount = zptqtwHeader.length;
		if (colCount == 0) return esMap;
		final DecodeRule[] xptDecodeRules = new DecodeRule[colCount];
		for (int i = 0; i < colCount; i++) {
			final String qtwHeader = zptqtwHeader[i];
			xptDecodeRules[i] = newDecodeRule(qtwHeader);
		}
		for (int irow = 1; irow < rowCount; irow++) {
			final String ztwRow = UNeon.espropertyByIndex(src, irow).toCanonicalString(ecx).trim();
			final String[] zptzFields = ArgonSplitter.zptzDelimited(ztwRow, delimiter);
			final int rowColCount = zptzFields.length;
			if (rowColCount == 0) {
				continue;
			}
			final String ztwKey = zptzFields[0].trim();
			if (ztwKey.length() == 0) {
				continue;
			}
			final EsIntrinsicObject esTuple = ecx.global().newIntrinsicObject();
			for (int icol = 0; icol < colCount && icol < rowColCount; icol++) {
				final DecodeRule decodeRule = xptDecodeRules[icol];
				final String zFieldValue = zptzFields[icol];
				decodeRule.put(esTuple, zFieldValue, ozErrorSuffix);
			}
			esMap.add(ztwKey, false, esTuple);
		}
		return esMap;
	}

	static class DecodeRuleCount extends DecodeRule {

		@Override
		public void put(EsIntrinsicObject dst, String zValue, String ozErrorSuffix) {
			final String ztwValue = zValue.trim();
			if (ztwValue.length() == 0) {
				dst.putViewNull(pname);
			} else {
				try {
					dst.putViewInteger(pname, Integer.parseInt(ztwValue));
				} catch (final NumberFormatException ex) {
					putError(dst, zValue, ozErrorSuffix);
				}
			}
		}

		public DecodeRuleCount(String pname) {
			super(pname);
		}
	}

	static class DecodeRuleDefault extends DecodeRule {

		@Override
		public void put(EsIntrinsicObject dst, String zValue, String ozErrorSuffix) {
			dst.putView(pname, zValue);
		}

		public DecodeRuleDefault(String pname) {
			super(pname);
		}
	}

	static class DecodeRuleElapsed extends DecodeRule {

		@Override
		public final void put(EsIntrinsicObject dst, String zValue, String ozErrorSuffix) {
			final String ztwValue = zValue.trim();
			if (ztwValue.length() == 0) {
				dst.putViewNull(pname);
			} else {
				try {
					dst.putViewElapsed(pname, ElapsedFactory.ms(ztwValue));
				} catch (final ArgonFormatException ex) {
					putError(dst, zValue, ozErrorSuffix);
				}
			}
		}

		public DecodeRuleElapsed(String pname) {
			super(pname);
		}
	}

	static class DecodeRuleFlag extends DecodeRule {

		@Override
		public void put(EsIntrinsicObject dst, String zValue, String ozErrorSuffix) {
			final String ztwValue = zValue.trim();
			if (ztwValue.length() == 0) {
				dst.putViewNull(pname);
			} else {
				dst.putViewBoolean(pname, Boolean.parseBoolean(ztwValue));
			}
		}

		public DecodeRuleFlag(String pname) {
			super(pname);
		}
	}

	static class DecodeRuleName extends DecodeRule {

		@Override
		public void put(EsIntrinsicObject dst, String zValue, String ozErrorSuffix) {
			final String ztwValue = zValue.trim();
			if (ztwValue.length() == 0) {
				dst.putViewNull(pname);
			} else {
				dst.putView(pname, ztwValue);
			}
		}

		public DecodeRuleName(String pname) {
			super(pname);
		}
	}

	static class DecodeRuleTime extends DecodeRule {

		@Override
		public void put(EsIntrinsicObject dst, String zValue, String ozErrorSuffix) {
			final String ztwValue = zValue.trim();
			if (ztwValue.length() == 0) {
				dst.putViewNull(pname);
			} else {
				try {
					dst.putViewTime(pname, DateFactory.newTsFromT8(ztwValue));
				} catch (final ArgonFormatException ex) {
					putError(dst, zValue, ozErrorSuffix);
				}
			}
		}

		public DecodeRuleTime(String pname) {
			super(pname);
		}
	}

	public static abstract class DecodeRule {

		protected final void putError(EsIntrinsicObject dst, String zValue, String ozErrorSuffix) {
			if (ozErrorSuffix == null || ozErrorSuffix.length() == 0) return;
			dst.putView((pname + ozErrorSuffix), zValue);
		}

		public abstract void put(EsIntrinsicObject dst, String zValue, String ozErrorSuffix);

		protected DecodeRule(String pname) {
			assert pname != null && pname.length() > 0;
			this.pname = pname;
		}
		public final String pname;
	}

}
