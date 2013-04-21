/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author roach
 */
public class ArgonText {

	public static final String CHARSET_NAME_ASCII = "US-ASCII";
	public static final String CHARSET_NAME_UTF8 = "UTF-8";
	public static final String CHARSET_NAME_ISO8859 = "ISO-8859-1";

	public static final Charset ASCII = Charset.forName(CHARSET_NAME_ASCII);
	public static final Charset UTF8 = Charset.forName(CHARSET_NAME_UTF8);
	public static final Charset ISO8859_1 = Charset.forName(CHARSET_NAME_ISO8859);
	public static final int CH_ASCII_LF = 10;
	public static final int CH_ASCII_CR = 13;

	private static final int IsoControlLLo = 0x0000;
	private static final int IsoControlLHi = 0x001F;
	private static final int IsoControlULo = 0x007F;
	private static final int IsoControlUHi = 0x009F;

	private static final int DigitLo = '0';
	private static final int DigitHi = '9';
	private static final int AlphaULo = 'A';
	private static final int AlphaUHi = 'Z';
	private static final int AlphaLLo = 'a';
	private static final int AlphaLHi = 'z';
	private static final int HexAlphaULo = 'A';
	private static final int HexAlphaUHi = 'F';
	private static final int HexAlphaLLo = 'a';
	private static final int HexAlphaLHi = 'f';

	private static boolean isHexLetter(char ch) {
		final int cp = ch;
		return (cp >= HexAlphaLLo && cp <= HexAlphaLHi) || (cp >= HexAlphaULo && cp <= HexAlphaUHi);
	}

	public static StringBuilder append(StringBuilder sb, char ch, int count) {
		for (int i = 0; i < count; i++) {
			sb.append(ch);
		}
		return sb;
	}

	public static StringBuilder append(StringBuilder sb, String zSep, String ozRhs) {
		if (sb == null) throw new IllegalArgumentException("object is null");
		if (ozRhs == null || ozRhs.length() == 0) return sb;
		if (sb.length() > 0) {
			sb.append(zSep);
		}
		sb.append(ozRhs);
		return sb;
	}

	public static StringBuilder appendLeftPad(StringBuilder sb, int rhs, int width) {
		return appendLeftPad(sb, Integer.toString(rhs), width, ' ');
	}

	public static StringBuilder appendLeftPad(StringBuilder sb, long rhs, int width) {
		return appendLeftPad(sb, Long.toString(rhs), width, ' ');
	}

	public static StringBuilder appendLeftPad(StringBuilder sb, String ozRhs, int width) {
		return appendLeftPad(sb, ozRhs, width, ' ');
	}

	public static StringBuilder appendLeftPad(StringBuilder sb, String ozRhs, int width, char padding) {
		if (sb == null) throw new IllegalArgumentException("object is null");
		if (ozRhs == null) return sb;
		final int rlen = ozRhs.length();
		for (int i = rlen; i < width; i++) {
			sb.append(padding);
		}
		sb.append(ozRhs);
		return sb;
	}

	public static StringBuilder appendRightPad(StringBuilder sb, String ozRhs, int width) {
		return appendRightPad(sb, ozRhs, width, ' ');
	}

	public static StringBuilder appendRightPad(StringBuilder sb, String ozRhs, int width, char padding) {
		if (sb == null) throw new IllegalArgumentException("object is null");
		if (ozRhs == null) return sb;
		sb.append(ozRhs);
		final int rlen = ozRhs.length();
		for (int i = rlen; i < width; i++) {
			sb.append(padding);
		}
		return sb;
	}

	public static StringBuilder appendSpace(StringBuilder sb, int count) {
		return append(sb, ' ', count);
	}

	public static String charsetName(Charset charset) {
		if (charset == null) throw new IllegalArgumentException("object is null");
		if (charset.equals(UTF8)) return CHARSET_NAME_UTF8;
		if (charset.equals(ISO8859_1)) return CHARSET_NAME_ISO8859;
		if (charset.equals(ASCII)) return CHARSET_NAME_ASCII;
		return charset.name();
	}

	public static boolean isDigit(char ch) {
		final int cp = ch;
		return (cp >= DigitLo && cp <= DigitHi);
	}

	public static boolean isEcmaName(char ch, boolean first) {
		return isLetter(ch) || isDigit(ch) || ch == '_' || (ch == '$' && !first);
	}

	public static boolean isHexDigit(char ch) {
		final int cp = ch;
		return (cp >= DigitLo && cp <= DigitHi) || isHexLetter(ch);
	}

	public static boolean isIsoControl(char ch) {
		final int cp = ch;
		return (cp >= IsoControlLLo && cp <= IsoControlLHi) || (cp >= IsoControlULo && cp <= IsoControlUHi);
	}

	public static boolean isLetter(char ch) {
		final int cp = ch;
		return (cp >= AlphaLLo && cp <= AlphaLHi) || (cp >= AlphaULo && cp <= AlphaUHi);
	}

	public static boolean isLetterOrDigit(char ch) {
		return isLetter(ch) || isDigit(ch);
	}

	public static boolean isPosixName(char ch, boolean lead) {
		return isLetter(ch) || isDigit(ch) || isPosixPunctuation(ch, lead);
	}

	public static boolean isPosixPunctuation(char ch, boolean lead) {
		if (ch == '_') return true;
		if (lead) return false;
		return (ch == '.' || ch == '-');
	}

	public static boolean isWhitespace(char ch) {
		return ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t';
	}

	public static Iterator<byte[]> iterator(List<String> zlLines, Charset charset, String zTerminator) {
		if (zlLines == null) throw new IllegalArgumentException("object is null");
		if (charset == null) throw new IllegalArgumentException("object is null");
		if (zTerminator == null) throw new IllegalArgumentException("object is null");
		return new StringListIterator(zlLines, charset, zTerminator);
	}

	public static String oqtw(String oz) {
		if (oz == null || oz.length() == 0) return null;
		final String ztw = oz.trim();
		return ztw.length() == 0 ? null : ztw;
	}

	public static double parse(String oz, double defaultValue) {
		if (oz == null) return defaultValue;
		final String ztw = oz.trim();
		if (ztw.length() == 0) return defaultValue;
		try {
			return Double.parseDouble(ztw);
		} catch (final NumberFormatException ex) {
			return defaultValue;
		}
	}

	public static int parse(String oz, int defaultValue) {
		if (oz == null) return defaultValue;
		final String ztw = oz.trim();
		if (ztw.length() == 0) return defaultValue;
		try {
			return Integer.parseInt(ztw);
		} catch (final NumberFormatException ex) {
			return defaultValue;
		}
	}

	public static long parse(String oz, long defaultValue) {
		if (oz == null) return defaultValue;
		final String ztw = oz.trim();
		if (ztw.length() == 0) return defaultValue;
		try {
			return Long.parseLong(ztw);
		} catch (final NumberFormatException ex) {
			return defaultValue;
		}
	}

	public static Double parseDouble(String oz, Double oDefaultValue) {
		if (oz == null) return oDefaultValue;
		final String ztw = oz.trim();
		if (ztw.length() == 0) return oDefaultValue;
		try {
			return new Double(ztw);
		} catch (final NumberFormatException ex) {
			return oDefaultValue;
		}
	}

	public static Integer parseInteger(String oz, Integer oDefaultValue) {
		if (oz == null) return oDefaultValue;
		final String ztw = oz.trim();
		if (ztw.length() == 0) return oDefaultValue;
		try {
			return new Integer(ztw);
		} catch (final NumberFormatException ex) {
			return oDefaultValue;
		}
	}

	public static Long parseLong(String oz, Long oDefaultValue) {
		if (oz == null) return oDefaultValue;
		final String ztw = oz.trim();
		if (ztw.length() == 0) return oDefaultValue;
		try {
			return new Long(ztw);
		} catch (final NumberFormatException ex) {
			return oDefaultValue;
		}
	}

	public static Long parseLongB36(String oz, Long oDefaultValue) {
		return parseLongRadix(oz, 36, oDefaultValue);
	}

	public static Long parseLongHex(String oz, Long oDefaultValue) {
		return parseLongRadix(oz, 16, oDefaultValue);
	}

	public static Long parseLongRadix(String oz, int radix, Long oDefaultValue) {
		if (oz == null) return oDefaultValue;
		final String ztw = oz.trim();
		if (ztw.length() == 0) return oDefaultValue;
		try {
			return Long.parseLong(ztw, radix);
		} catch (final NumberFormatException ex) {
			return oDefaultValue;
		}
	}

	public static String qtw(String oz)
			throws ArgonApiException {
		final String oqtw = oqtw(oz);
		if (oqtw != null) return oqtw;
		throw new ArgonApiException("Value is empty or all whitespace");
	}

	public static String qtwEcmaName(String ozIn)
			throws ArgonApiException {
		if (ozIn == null) throw new ArgonApiException("Name is empty");
		final int count = ozIn.length();
		if (count == 0) throw new ArgonApiException("Name is empty");
		final char ch0 = ozIn.charAt(0);
		if (!isEcmaName(ch0, true)) {
			final int cp0 = ch0;
			final String m = "The leading '" + ch0 + "' character (0x" + Integer.toHexString(cp0)
					+ ") is not permitted in the name  '" + ozIn + "'";
			throw new ArgonApiException(m);
		}
		for (int i = 1; i < count; i++) {
			final char ch = ozIn.charAt(i);
			if (!isEcmaName(ch, false)) {
				final int cp = ch;
				final String m = "The '" + ch + "' character (0x" + Integer.toHexString(cp)
						+ ") is not permitted in the name '" + ozIn + "'";
				throw new ArgonApiException(m);
			}
		}
		return ozIn;
	}

	public static String qtwPosixName(String ozIn)
			throws ArgonApiException {
		if (ozIn == null) throw new ArgonApiException("Name is empty");
		final int count = ozIn.length();
		if (count == 0) throw new ArgonApiException("Name is empty");
		final char ch0 = ozIn.charAt(0);
		if (!isPosixName(ch0, true)) {
			final int cp0 = ch0;
			final String m = "The leading '" + ch0 + "' character (0x" + Integer.toHexString(cp0)
					+ ") is not permitted in the name  '" + ozIn + "'";
			throw new ArgonApiException(m);
		}
		for (int i = 1; i < count; i++) {
			final char ch = ozIn.charAt(i);
			if (!isPosixName(ch, false)) {
				final int cp = ch;
				final String m = "The '" + ch + "' character (0x" + Integer.toHexString(cp)
						+ ") is not permitted in the name '" + ozIn + "'";
				throw new ArgonApiException(m);
			}
		}
		return ozIn;
	}

	public static String qtwTitleName(String qtwName) {
		if (qtwName == null) throw new IllegalArgumentException("Name is null");
		final int count = qtwName.length();
		if (count == 0) throw new IllegalArgumentException("Name is empty");
		final char ch0 = qtwName.charAt(0);
		final char uch0 = Character.toUpperCase(ch0);
		if (ch0 == uch0) return qtwName;
		final StringBuilder sb = new StringBuilder();
		sb.append(uch0);
		sb.append(qtwName.substring(1));
		return sb.toString();
	}

	public static Charset selectCharset(String qncName)
			throws ArgonApiException {
		if (qncName == null || qncName.length() == 0) throw new IllegalArgumentException("string is null or empty");
		try {
			if (qncName.equals(CHARSET_NAME_UTF8)) return UTF8;
			if (qncName.equals(CHARSET_NAME_ISO8859)) return ISO8859_1;
			if (qncName.equals(CHARSET_NAME_ASCII)) return ASCII;
			return Charset.forName(qncName);
		} catch (final UnsupportedCharsetException ex) {
			throw new ArgonApiException("Unsupported character set '" + qncName + "'");
		}
	}

	public static String ztw(String oz) {
		return (oz == null || oz.length() == 0) ? "" : oz.trim();
	}

	private static class StringListIterator implements Iterator<byte[]> {

		@Override
		public boolean hasNext() {
			return m_lineIndex < m_lineCount;
		}

		@Override
		public byte[] next() {
			if (m_lineIndex >= m_lineCount) {
				final String m = "index=" + m_lineIndex + ", count=" + m_lineCount;
				throw new NoSuchElementException(m);
			}
			final byte[] zptNext;
			if (m_enabledTerminate) {
				if (m_terminate) {
					zptNext = m_zptTerminator;
					m_lineIndex++;
					m_terminate = false;
				} else {
					final String zLine = m_zlLines.get(m_lineIndex);
					zptNext = zLine.getBytes(m_charset);
					m_terminate = true;
				}
			} else {
				final String zLine = m_zlLines.get(m_lineIndex);
				m_lineIndex++;
				zptNext = zLine.getBytes(m_charset);
			}
			return zptNext;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Remove not implemented");
		}

		public StringListIterator(List<String> zlLines, Charset charset, String zTerminator) {
			m_zlLines = zlLines;
			m_charset = charset;
			m_zptTerminator = zTerminator.getBytes(charset);
			m_lineCount = zlLines.size();
			m_enabledTerminate = zTerminator.length() > 0;
		}
		private final List<String> m_zlLines;
		private final Charset m_charset;
		private final byte[] m_zptTerminator;
		private final int m_lineCount;
		private final boolean m_enabledTerminate;
		private int m_lineIndex;
		private boolean m_terminate;
	}
}
