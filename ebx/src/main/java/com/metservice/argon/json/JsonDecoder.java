/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.json;

import com.metservice.argon.ArgonFormatException;
import com.metservice.argon.Binary;
import com.metservice.argon.DateFactory;

/**
 * @author roach
 */
public class JsonDecoder {

	public static final JsonDecoder Default = new JsonDecoder();

	static final char EOS = '\u0000';
	static final char BDelim = '~';
	static final int ERMFrag = 40;
	static final int DigitLo = '0';
	static final int DigitHi = '9';
	static final int AlphaULo = 'A';
	static final int AlphaUHi = 'Z';
	static final int AlphaLLo = 'a';
	static final int AlphaLHi = 'z';

	private static boolean requireDouble(String qValue) {
		final int len = qValue.length();
		for (int i = 0; i < len; i++) {
			final char ch = qValue.charAt(i);
			if (ch == '.' || ch == 'E' || ch == 'e') return true;
		}
		return false;
	}

	private IJsonDeValue parseArray(Parser p, IJsonDeFactory df)
			throws ArgonFormatException {
		final IJsonDeArray deArray = df.newArray();
		p.consume('[');
		p.consumeWhite();
		int memberIndex = 0;
		while (p.notMatch(']')) {
			if (memberIndex > 0) {
				p.consume(',');
				p.consumeWhite();
			}
			final IJsonDeValue value = parseValue(p, df);
			deArray.jsonAdd(memberIndex, value);
			memberIndex++;
			p.consumeWhite();
		}
		p.consume(']');
		return deArray;
	}

	private IJsonDeValue parseBinary(Parser p, IJsonDeFactory df)
			throws ArgonFormatException {
		final String zB64 = p.consumeDelimited(BDelim);
		final Binary binary = Binary.newFromB64MIME(zB64);
		return df.newBinary(binary);
	}

	private IJsonDeValue parseObject(Parser p, IJsonDeFactory df)
			throws ArgonFormatException {
		final IJsonDeObject deObject = df.newObject();
		p.consume('{');
		p.consumeWhite();
		boolean first = true;
		while (p.notMatch('}')) {
			if (!first) {
				p.consume(',');
				p.consumeWhite();
			}
			final String qtwName = p.consumeName();
			p.consumeWhite();
			p.consume(':');
			p.consumeWhite();
			final IJsonDeValue value = parseValue(p, df);
			deObject.jsonPut(qtwName, value);
			first = false;
			p.consumeWhite();
		}
		p.consume('}');
		return deObject;
	}

	private IJsonDeValue parseObjectOrArray(Parser p, IJsonDeFactory df)
			throws ArgonFormatException {
		if (p.match('{')) return parseObject(p, df);
		if (p.match('[')) return parseArray(p, df);
		final String r = "Expecting object or array";
		throw new ArgonFormatException(p.erm(r));
	}

	private IJsonDeValue parseString(Parser p, IJsonDeFactory df)
			throws ArgonFormatException {
		return df.newString(p.consumeQuoted());
	}

	private IJsonDeValue parseToken(Parser p, IJsonDeFactory df)
			throws ArgonFormatException {
		final String qValue = p.consumeToken();
		if (qValue.equals(CJson.TokenTrue)) return df.newBoolean(true);
		if (qValue.equals(CJson.TokenFalse)) return df.newBoolean(false);
		if (qValue.equals(CJson.TokenNull)) return df.instanceNull();
		if (qValue.equals(CJson.TokenNaN)) return df.instanceNotNumber();

		final int len = qValue.length();
		final char chSuffix = qValue.charAt(len - 1);
		if (chSuffix == CJson.SuffixTime) {
			try {
				final long ts = Long.parseLong(qValue.substring(0, len - 1));
				return df.newNumberTime(ts);
			} catch (final NumberFormatException ex) {
				final String r = "Expecting a numeric timestamp";
				throw new ArgonFormatException(p.erm(r));
			}
		}
		if (chSuffix == CJson.SuffixElapsed) {
			try {
				final long ms = Long.parseLong(qValue.substring(0, len - 1));
				return df.newNumberElapsed(ms);
			} catch (final NumberFormatException ex) {
				final String r = "Expecting numeric elapsed time in milliseconds";
				throw new ArgonFormatException(p.erm(r));
			}
		}

		if (!requireDouble(qValue)) {
			try {
				final int ivalue = Integer.parseInt(qValue);
				return df.newNumberInt(ivalue);
			} catch (final NumberFormatException ex) {
			}
		}

		try {
			final double dvalue = Double.parseDouble(qValue);
			return df.newNumberDouble(dvalue);
		} catch (final NumberFormatException ex) {
		}

		if (!DateFactory.isMarkerMalformedT8(qValue)) {
			final long ts = DateFactory.newTsFromT8(qValue);
			return df.newNumberTime(ts);
		}

		final String r = "Expecting a numeric value";
		throw new ArgonFormatException(p.erm(r));
	}

	private IJsonDeValue parseValue(Parser p, IJsonDeFactory df)
			throws ArgonFormatException {
		if (p.match('\"')) return parseString(p, df);
		if (p.match(BDelim)) return parseBinary(p, df);
		if (p.match('{')) return parseObject(p, df);
		if (p.match('[')) return parseArray(p, df);
		return parseToken(p, df);
	}

	public IJsonNative decode(String zSpec)
			throws ArgonFormatException {
		final IJsonDeValue deValue = decode(zSpec, NativeDeFactory.Instance);
		if (!(deValue instanceof IJsonNative)) {
			final String m = "Native factory returned a value of class " + deValue.getClass();
			throw new IllegalStateException(m);
		}
		return (IJsonNative) deValue;
	}

	public IJsonDeValue decode(String zSpec, IJsonDeFactory df)
			throws ArgonFormatException {
		if (zSpec == null) throw new IllegalArgumentException("object is null");
		if (df == null) throw new IllegalArgumentException("object is null");
		final String ztwSpec = zSpec.trim();
		final int len = ztwSpec.length();
		if (len == 0) throw new ArgonFormatException("JSON string is empty");
		if (len < 2) throw new ArgonFormatException("Expecting JSON string '" + ztwSpec + "' to be at least two characters");

		final Parser p = new Parser(ztwSpec);
		return parseObjectOrArray(p, df);
	}

	public JsonObject decodeObject(String zSpec)
			throws JsonSchemaException, ArgonFormatException {
		final IJsonNative decode = decode(zSpec);
		if (decode instanceof JsonObject) return (JsonObject) decode;
		final String actual = decode.getJsonType().title;
		throw new JsonSchemaException("Expecting a JSON Object, but actual type is '" + actual + "'");
	}

	public JsonDecoder() {
	}

	private static class Parser {

		static boolean isDigit(char ch) {
			return (ch >= DigitLo && ch <= DigitHi);
		}

		static boolean isLetter(char ch) {
			return (ch >= AlphaULo && ch <= AlphaUHi) || (ch >= AlphaLLo && ch <= AlphaLHi);
		}

		static boolean isName(char ch) {
			return isLetter(ch) || isDigit(ch) || ch == '_';
		}

		static boolean isToken(char ch) {
			return isLetter(ch) || isDigit(ch) || ch == '.' || ch == '+' || ch == '-';
		}

		static boolean isWhitespace(char ch) {
			return ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t';
		}

		public void consume() {
			if (pos < posLast) {
				pos++;
				ch = qtwSpec.charAt(pos);
			} else {
				pos = lenSpec;
				ch = EOS;
			}
		}

		public void consume(char expected)
				throws ArgonFormatException {
			if (ch != expected) {
				final String r = "Expecting '" + expected + "'";
				throw new ArgonFormatException(erm(r));
			}
			consume();
		}

		public String consumeDelimited(char delimiter)
				throws ArgonFormatException {
			consume(delimiter);
			final int posEnd = qtwSpec.indexOf(delimiter, pos);
			if (posEnd < pos || posEnd > posLast) {
				final String r = "Missing closing delimiter (" + delimiter + ")";
				throw new ArgonFormatException(erm(r));
			}
			final String zSub = qtwSpec.substring(pos, posEnd);
			pos = posEnd;
			consume();
			return zSub;
		}

		public String consumeName()
				throws ArgonFormatException {
			final boolean quoted = consumeOptional('\"');
			final StringBuilder sb = new StringBuilder();
			while (notEnd() && isName(ch)) {
				sb.append(ch);
				consume();
			}
			if (quoted) {
				consume('\"');
			}
			final String zname = sb.toString();
			if (zname.length() == 0) {
				final String r = "Expecting a property name";
				throw new ArgonFormatException(erm(r));
			}
			return zname;
		}

		public boolean consumeOptional(char optional) {
			if (ch == optional) {
				consume();
				return true;
			}
			return false;
		}

		public String consumeQuoted()
				throws ArgonFormatException {
			consume('\"');
			final StringBuilder sb = new StringBuilder();
			boolean escapeOn = false;
			StringBuilder oUni = null;
			while (notEnd() && (escapeOn || ch != '\"')) {
				if (escapeOn) {
					if (oUni == null) {
						switch (ch) {
							case '\"':
								sb.append('\"');
								escapeOn = false;
							break;
							case '\\':
								sb.append('\\');
								escapeOn = false;
							break;
							case '/':
								sb.append('/');
								escapeOn = false;
							break;
							case 'b':
								sb.append('\b');
								escapeOn = false;
							break;
							case 'f':
								sb.append('\f');
								escapeOn = false;
							break;
							case 'n':
								sb.append('\n');
								escapeOn = false;
							break;
							case 'r':
								sb.append('\r');
								escapeOn = false;
							break;
							case 't':
								sb.append('\t');
								escapeOn = false;
							break;
							case 'u':
								oUni = new StringBuilder(4);
							break;
							default: {
								final String r = "Unsupported escape sequence '\\" + ch + "'";
								throw new ArgonFormatException(erm(r));
							}
						}
					} else {
						oUni.append(ch);
						if (oUni.length() == 4) {
							final String qUni = oUni.toString();
							try {
								final int ic = Integer.parseInt(qUni, 16);
								final char uic = (char) ic;
								sb.append(uic);
								oUni = null;
								escapeOn = false;
							} catch (final NumberFormatException ex) {
								final String r = "Malformed unicode escape sequence '\\u" + qUni + "'";
								throw new ArgonFormatException(erm(r));
							}
						}
					}
				} else {
					if (ch == '\\') {
						escapeOn = true;
					} else {
						sb.append(ch);
					}
				}
				consume();
			}
			if (escapeOn) {
				final String r = "Incomplete escape sequence";
				throw new ArgonFormatException(erm(r));
			}
			if (oUni != null) {
				final String r = "Incomplete unicode escape sequence '\\u" + oUni + "'";
				throw new ArgonFormatException(erm(r));
			}
			consume('\"');
			return sb.toString();
		}

		public String consumeToken()
				throws ArgonFormatException {
			final StringBuilder sb = new StringBuilder();
			while (notEnd() && isToken(ch)) {
				sb.append(ch);
				consume();
			}
			if (sb.length() == 0) {
				final String r = "Expecting a token value";
				throw new ArgonFormatException(erm(r));
			}

			return sb.toString();
		}

		public void consumeWhite() {
			while (notEnd() && isWhitespace(ch)) {
				consume();
			}
		}

		public String erm(String rsn) {
			final StringBuilder sb = new StringBuilder();
			sb.append("Syntax error in JSON string at position ");
			sb.append(pos);
			sb.append("...");
			sb.append(rsn);
			sb.append('\n');
			final int spos = Math.max(0, pos - ERMFrag);
			final int epos = Math.min(lenSpec, pos + ERMFrag);
			if (spos > 0) {
				sb.append("...");
			}
			for (int i = spos; i < epos; i++) {
				if (i == pos) {
					sb.append(" HERE>>>");
				}
				sb.append(qtwSpec.charAt(i));
				if (i == pos) {
					sb.append("<<<");
				}
			}
			if (pos == lenSpec) {
				sb.append("<<<HERE");
			}
			if (epos < lenSpec) {
				sb.append("...");
			}
			return sb.toString();
		}

		public boolean match(char expected)
				throws ArgonFormatException {
			if (notEnd()) return ch == expected;
			final String r = "Unexpected end of structure (checking for '" + expected + "')";
			throw new ArgonFormatException(erm(r));
		}

		public boolean notEnd() {
			return pos < lenSpec;
		}

		public boolean notMatch(char expected)
				throws ArgonFormatException {
			if (notEnd()) return ch != expected;
			final String r = "Unexpected end of structure (checking for '" + expected + "')";
			throw new ArgonFormatException(erm(r));
		}

		@Override
		public String toString() {
			if (pos == lenSpec) return "END";
			final StringBuilder sb = new StringBuilder();
			sb.append(pos);
			sb.append('/');
			sb.append(posLast);
			sb.append('>');
			sb.append(ch);
			sb.append('<');
			return sb.toString();
		}

		public Parser(String qtwSpec) {
			assert qtwSpec != null && qtwSpec.length() > 0;
			this.qtwSpec = qtwSpec;
			this.lenSpec = qtwSpec.length();
			this.posLast = this.lenSpec - 1;
			ch = qtwSpec.charAt(0);
		}

		final String qtwSpec;
		final int lenSpec;
		final int posLast;
		int pos;
		char ch;
	}

}
