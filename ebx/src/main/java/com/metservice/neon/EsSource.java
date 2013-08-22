/*
 * Copyright 2005 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.metservice.argon.ArgonFormatException;
import com.metservice.argon.ArgonNumber;
import com.metservice.argon.CArgon;
import com.metservice.argon.Ds;
import com.metservice.argon.ElapsedFactory;
import com.metservice.argon.ElapsedUnit;
import com.metservice.argon.Real;
import com.metservice.argon.xml.W3cDom;

/**
 * 
 * @author roach
 */
public class EsSource {

	private static final String Tag_td_n = "<td class=\"n\">";

	public static String lineHere(String qccSourcePath, String[] zptLines, int lineIndex, int startIndex) {
		if (qccSourcePath == null || qccSourcePath.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		if (zptLines == null) throw new IllegalArgumentException("object is null");

		if (lineIndex >= zptLines.length) return "End of Script";
		final String zLine = zptLines[lineIndex];
		final int ll = zLine.length();
		final StringBuilder b = new StringBuilder(ll * 3);
		b.append("Line ");
		b.append(ArgonNumber.intToDec3(lineIndex + 1));
		if (startIndex > 0) {
			b.append(", Position ");
			b.append(startIndex + 1);
		}
		b.append(" of {");
		b.append(qccSourcePath);
		b.append('}');
		b.append('\n');
		b.append(zLine);
		b.append('\n');
		if (startIndex > 0) {
			for (int i = 0; i < startIndex; i++) {
				b.append(' ');
			}
			b.append('^');
			b.append('\n');
		}
		return b.toString();
	}

	public static Token[] new_xptTokens(String qccSourcePath, String[] xptztwLines)
			throws EsSyntaxException {
		if (qccSourcePath == null || qccSourcePath.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		if (xptztwLines == null || xptztwLines.length == 0) throw new IllegalArgumentException("array is null or empty");

		final ScriptState scriptState = new ScriptState(qccSourcePath, xptztwLines);
		final int lineCount = xptztwLines.length;
		boolean multiLineCommentOn = false;
		TokenState oContinued = null;
		for (int lineIndex = 0; lineIndex < lineCount; lineIndex++) {
			final String zLine = xptztwLines[lineIndex];
			final int lineLength = zLine.length();
			final LineState lineState = new LineState(scriptState, lineIndex, oContinued);
			boolean singleLineComment = false;
			int r = 0;
			while (r < lineLength) {
				final int chIndex = r;
				final char ch = zLine.charAt(chIndex);
				final int cpeekIndex = r + 1;
				final char chpeek = cpeekIndex < lineLength ? zLine.charAt(cpeekIndex) : '\u0000';
				r++;

				if (multiLineCommentOn) {
					if (ch == '*' && chpeek == '/') {
						multiLineCommentOn = false;
						r++;
					}
					continue;
				}
				if (singleLineComment) {
					continue;
				}

				if (ch == '/' && !lineState.quoting()) {
					if (chpeek == '*') {
						multiLineCommentOn = true;
						r++;
						continue;
					}

					if (chpeek == '/') {
						singleLineComment = true;
						r++;
						continue;
					}
				}

				lineState.transition(chIndex, ch);
			}
			oContinued = lineState.endOfLine();
		}
		scriptState.endOfScript(oContinued);
		final List<Token> xlTokens = scriptState.tokens();
		final int tokenCount = xlTokens.size();
		return xlTokens.toArray(new Token[tokenCount]);
	}

	public static String toProfileHtmlTR(int count, String zTiming, String zInfo) {
		final StringBuilder b = new StringBuilder();
		b.append("<tr>");
		b.append("<td>");
		b.append("Line");
		b.append("</td>");
		b.append("<td>");
		b.append("%");
		b.append("</td>");
		b.append(Tag_td_n).append(count).append("*</td>");
		b.append(Tag_td_n).append(zTiming).append("</td>");
		b.append("<td class=\"t\">");
		b.append("<b>Program</b>");
		b.append("</td>");
		b.append("<td class=\"t\">");
		b.append(zInfo);
		b.append("</td>");
		b.append("</tr>\n");
		return b.toString();
	}

	public int checksumAdler32() {
		return m_adler32;
	}

	public EsSourceHtml createSourceHtml() {
		try {
			return newSourceHtml();
		} catch (final EsSyntaxException ex) {
			return null;
		}
	}

	public boolean equals(EsSource r) {
		if (r == this) return true;
		if (r == null) return false;
		return m_adler32 == r.m_adler32 && m_qccPath.equals(r.m_qccPath);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof EsSource)) return false;
		return equals((EsSource) o);
	}

	public String format(boolean includeMeta) {
		final StringBuilder b = new StringBuilder();
		if (includeMeta) {
			b.append(m_meta).append('\n');
		}
		final int c = m_xptztwLines.length;
		for (int i = 0; i < c; i++) {
			b.append(ArgonNumber.intToDec3(i + 1));
			b.append(' ');
			b.append(m_xptztwLines[i]);
			b.append('\n');
		}
		return b.toString();
	}

	@Override
	public int hashCode() {
		return m_adler32;
	}

	public int lineCount() {
		return m_xptztwLines.length;
	}

	public String lineHere(int lineIndex) {
		return lineHere(m_qccPath, m_xptztwLines, lineIndex, 0);
	}

	public String lineHere(int lineIndex, int startIndex) {
		return lineHere(m_qccPath, m_xptztwLines, lineIndex, startIndex);
	}

	public EsSourceMeta meta() {
		return m_meta;
	}

	public IEsCallable newCallable()
			throws EsSyntaxException {
		return newTokenReader().newParsedProgram().newCallable();
	}

	public EsSourceHtml newSourceHtml()
			throws EsSyntaxException {
		return newTokenReader().newSourceHtml();
	}

	public TokenReader newTokenReader()
			throws EsSyntaxException {
		final Token[] xptTokens = new_xptTokens(m_qccPath, m_xptztwLines);
		return new TokenReader(this, xptTokens);
	}

	public String qccPath() {
		return m_qccPath;
	}

	@Override
	public String toString() {
		return format(true);
	}

	public EsSource(String qccPath, EsSourceMeta meta, String[] xptztwLines, int adler32) {
		if (qccPath == null || qccPath.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (meta == null) throw new IllegalArgumentException("object is null");
		if (xptztwLines == null || xptztwLines.length == 0) throw new IllegalArgumentException("array is null or empty");
		m_qccPath = qccPath;
		m_meta = meta;
		m_xptztwLines = xptztwLines;
		m_adler32 = adler32;
	}

	private final String m_qccPath;
	private final EsSourceMeta m_meta;
	private final String[] m_xptztwLines;
	private final int m_adler32;

	private static class LineState {

		private TokenState digit(int chIndex, char ch) {
			if (m_oTokenState == null) {
				m_oTokenState = new NumericLiteralState(this, chIndex, ch);
				return m_oTokenState;
			}
			return m_oTokenState.digit(chIndex, ch);
		}

		private TokenState eol() {
			if (m_oTokenState == null) return null;
			return m_oTokenState.eol();
		}

		private TokenState letter(int chIndex, char ch) {
			if (m_oTokenState == null) {
				m_oTokenState = new WordState(this, chIndex, ch);
				return m_oTokenState;
			}
			return m_oTokenState.letter(chIndex, ch);
		}

		private TokenState punctuation(int chIndex, char ch) {
			if (m_oTokenState == null) {
				if (ch == '_') {
					m_oTokenState = new WordState(this, chIndex, ch);
				} else if (ch == '\'' || ch == '\"') {
					m_oTokenState = new StringLiteralState(this, chIndex, ch);
				} else if (ch == '/' && scriptState.allowRegexp()) {
					m_oTokenState = new RegexpLiteralState(this, chIndex);
				} else if (ch == '<' && scriptState.allowRegexp()) {
					m_oTokenState = new XmlLiteralState(this, chIndex);
				} else {
					m_oTokenState = new PuncState(this, chIndex, ch);
				}
				return m_oTokenState;
			}
			return m_oTokenState.punctuation(chIndex, ch);
		}

		private void pushTokenState(TokenState oNeoTokenState) {
			if (m_oTokenState != null) {
				final Token oPopped = m_oTokenState.popPending();
				if (oPopped != null) {
					scriptState.addToken(oPopped);
				}
			}
			m_oTokenState = oNeoTokenState;
		}

		private TokenState whitespace() {
			if (m_oTokenState == null) return null;
			return m_oTokenState.whitespace();
		}

		public TokenState endOfLine()
				throws EsSyntaxException {
			try {
				pushTokenState(eol());
				return m_oTokenState;
			} catch (final TokenizerException exTK) {
				final String here = here(m_oTokenState.startIndex);
				throw new EsSyntaxException(exTK.getMessage(), here, lineIndex);
			}
		}

		public String here(int startIndex) {
			return scriptState.here(lineIndex, startIndex);
		}

		public String qLineIndex() {
			return ArgonNumber.intToDec3(lineIndex + 1);
		}

		public boolean quoting() {
			return (m_oTokenState instanceof StringLiteralState) || (m_oTokenState instanceof XmlLiteralState);
		}

		@Override
		public String toString() {
			final Ds ds = Ds.o("EsSource.LineState");
			ds.a("oTokenState", m_oTokenState);
			return ds.s();
		}

		public void transition(int chIndex, char ch)
				throws EsSyntaxException {
			try {
				final TokenState oNeoTokenState;
				if (Character.isWhitespace(ch)) {
					oNeoTokenState = whitespace();
				} else if (Character.isLetter(ch)) {
					oNeoTokenState = letter(chIndex, ch);
				} else if (Character.isDigit(ch)) {
					oNeoTokenState = digit(chIndex, ch);
				} else {
					oNeoTokenState = punctuation(chIndex, ch);
				}
				pushTokenState(oNeoTokenState);
			} catch (final TokenizerException exTK) {
				final String here;
				if (m_oTokenState == null) {
					here = here(chIndex);
				} else {
					here = here(m_oTokenState.startIndex);
				}
				throw new EsSyntaxException(exTK.getMessage(), here, lineIndex);
			}
		}

		LineState(ScriptState scriptState, int lineIndex, TokenState oContinued) {
			assert scriptState != null;
			this.scriptState = scriptState;
			this.lineIndex = lineIndex;
			m_oTokenState = oContinued;
		}

		final ScriptState scriptState;
		final int lineIndex;
		private TokenState m_oTokenState;
	}// LineState

	private static class NumericLiteralState extends TokenState {

		private TokenLiteralNumeric createTokenInteger(String qccNumericLiteral) {
			try {
				final int i = Integer.parseInt(qccNumericLiteral);
				return new TokenLiteralNumericIntegral(lineState.lineIndex, startIndex, i);
			} catch (final NumberFormatException ex) {
			}
			return null;
		}

		private void emit() {
			final String qccNumericLiteral = buffer.toString();
			if (seenDoubleExponent) {
				setPending(newTokenDouble(qccNumericLiteral));
				return;
			}
			if (seenRealExponent) {
				setPending(newTokenReal(qccNumericLiteral));
				return;
			}
			if (seenElapsedIndicator) {
				setPending(newTokenElapsed(qccNumericLiteral));
				return;
			}
			if (seenDecimal) {
				setPending(newTokenDouble(qccNumericLiteral));
				return;
			}

			final TokenLiteralNumeric oIntegralToken = createTokenInteger(qccNumericLiteral);
			if (oIntegralToken != null) {
				setPending(oIntegralToken);
				return;
			}

			setPending(newTokenDouble(qccNumericLiteral));
		}

		private TokenLiteralNumeric newTokenDouble(String qccNumericLiteral) {
			try {
				final double d = Double.parseDouble(qccNumericLiteral);
				return new TokenLiteralNumericDouble(lineState.lineIndex, startIndex, d);
			} catch (final NumberFormatException ex) {
				throw new TokenizerException("Malformed double '" + qccNumericLiteral + "'");
			}
		}

		private TokenLiteralNumeric newTokenElapsed(String qccNumericLiteral) {
			try {
				final long sms = ElapsedFactory.ms(qccNumericLiteral);
				return new TokenLiteralNumericElapsed(lineState.lineIndex, startIndex, sms);
			} catch (final ArgonFormatException ex) {
				throw new TokenizerException("Malformed elapsed '" + qccNumericLiteral + "'; " + ex.getMessage());
			}
		}

		private TokenLiteralNumeric newTokenReal(String qccNumericLiteral) {
			try {
				final Real r = Real.newInstance(qccNumericLiteral);
				return new TokenLiteralNumericReal(lineState.lineIndex, startIndex, r);
			} catch (final ArgonFormatException ex) {
				throw new TokenizerException("Malformed real '" + qccNumericLiteral + "'; " + ex.getMessage());
			}
		}

		@Override
		TokenState digit(int chIndex, char ch) {
			allowExponentSign = false;
			buffer.append(ch);
			return this;
		}

		@Override
		TokenState eol() {
			emit();
			return null;
		}

		@Override
		TokenState letter(int chIndex, char ch) {
			final char lch = Character.toLowerCase(ch);
			if (lch == 'e') {
				seenDoubleExponent = true;
				allowExponentSign = true;
				buffer.append(ch);
				return this;
			}
			if (lch == CArgon.REAL_LEXPONENT) {
				seenRealExponent = true;
				allowExponentSign = true;
				buffer.append(ch);
				return this;
			}
			if (ElapsedUnit.isCodeLower(lch)) {
				seenElapsedIndicator = true;
				buffer.append(ch);
				return this;
			}
			throw new TokenizerException("Unrecognised indicator letter '" + ch + "' in numeric literal");
		}

		@Override
		TokenState punctuation(int chIndex, char ch) {
			if (ch == '.') {
				seenDecimal = true;
				allowExponentSign = false;
				buffer.append(ch);
				return this;
			}
			if (ch == '+' || ch == '-') {
				if (allowExponentSign) {
					allowExponentSign = false;
					buffer.append(ch);
					return this;
				}
			}
			emit();
			return new PuncState(lineState, chIndex, ch);
		}

		@Override
		String type() {
			return "Numeric Literal";
		}

		@Override
		TokenState whitespace() {
			emit();
			return null;
		}

		@Override
		public String toString() {
			final Ds ds = Ds.o("EsSource.NumericLiteralState");
			ds.a("seenDecimal", seenDecimal);
			ds.a("seenDoubleExponent", seenDoubleExponent);
			ds.a("seenRealExponent", seenRealExponent);
			ds.a("seenElapsedIndicator", seenElapsedIndicator);
			ds.a("allowExponentSign", allowExponentSign);
			return ds.s();
		}

		NumericLiteralState(LineState lineState, int chIndex, char ch) {
			super(lineState, chIndex, ch);
		}

		boolean seenDecimal;
		boolean seenDoubleExponent;
		boolean seenRealExponent;
		boolean seenElapsedIndicator;
		boolean allowExponentSign;
	}// NumericState

	private static class PuncState extends TokenState {

		private static final char[] TRANS_EQ = { '=', '!', '<', '>', '*', '/', '%', '^' };

		private static final char[] NEXT_NONE = {};

		private static final char[] NEXT_EQ = { '=' };

		private static final char[] NEXT_PLUSEQ = { '+', '=' };

		private static final char[] NEXT_MINUSEQ = { '-', '=' };

		private static final char[] NEXT_AMPEQ = { '&', '=' };

		private static final char[] NEXT_BAREQ = { '|', '=' };

		private static boolean in(char ch, char[] zptch) {
			final int count = zptch.length;
			for (int i = 0; i < count; i++) {
				if (zptch[i] == ch) return true;
			}
			return false;
		}

		private void emit() {
			final String qccPunc = buffer.toString();
			final Punctuator oPunctuator = Punctuator.Table.find(qccPunc);
			if (oPunctuator == null) throw new TokenizerException("Unrecognised punctuation '" + qccPunc + "'");
			setPending(new TokenPunctuator(lineState.lineIndex, startIndex, oPunctuator));
		}

		@Override
		TokenState digit(int chIndex, char ch) {
			emit();
			return new NumericLiteralState(lineState, chIndex, ch);
		}

		@Override
		TokenState eol() {
			emit();
			return null;
		}

		@Override
		TokenState letter(int chIndex, char ch) {
			emit();
			return new WordState(lineState, chIndex, ch);
		}

		@Override
		TokenState punctuation(int chIndex, char ch) {
			if (in(ch, zptchNext)) {
				buffer.append(ch);
				if (ch == '=') {
					final int len = buffer.length();
					if (len == 2) {
						final char ch0 = buffer.charAt(0);
						if (ch0 == '=' || ch0 == '!') {
							zptchNext = NEXT_EQ;
							return this;
						}
					}
				}
				emit();
				return null;
			}
			emit();

			if (ch == '_') return new WordState(lineState, chIndex, ch);

			if (ch == '\'' || ch == '\"') return new StringLiteralState(lineState, chIndex, ch);

			if (ch == '/' && lineState.scriptState.allowRegexp()) return new RegexpLiteralState(lineState, chIndex);

			if (ch == '<' && lineState.scriptState.allowRegexp()) return new XmlLiteralState(lineState, chIndex);

			return new PuncState(lineState, chIndex, ch);
		}

		@Override
		String type() {
			return "Punctuation";
		}

		@Override
		TokenState whitespace() {
			emit();
			return null;
		}

		PuncState(LineState lineState, int chIndex, char ch) {
			super(lineState, chIndex, ch);
			if (in(ch, TRANS_EQ)) {
				zptchNext = NEXT_EQ;
			} else if (ch == '+') {
				zptchNext = NEXT_PLUSEQ;
			} else if (ch == '-') {
				zptchNext = NEXT_MINUSEQ;
			} else if (ch == '&') {
				zptchNext = NEXT_AMPEQ;
			} else if (ch == '|') {
				zptchNext = NEXT_BAREQ;
			} else {
				zptchNext = NEXT_NONE;
			}
		}

		private char[] zptchNext;

	}// PuncState

	private static class RegexpLiteralState extends TokenState {

		private void emit() {
			final String zccRegexp = buffer.toString();
			if (zccRegexp.length() == 0) throw new TokenizerException("Empty regular expression");

			final String zccFlags = flagsBuffer.toString();
			int patternFlags = 0;
			boolean caseInsensitive = false;
			boolean multiline = false;
			final int flagCount = zccFlags.length();
			for (int i = 0; i < flagCount; i++) {
				final char cf = zccFlags.charAt(i);
				switch (cf) {
					case 'i': {
						patternFlags = patternFlags | Pattern.CASE_INSENSITIVE;
						caseInsensitive = true;
					}
					break;
					case 'm': {
						patternFlags = patternFlags | Pattern.MULTILINE;
						multiline = true;
					}
					break;
					default: {
						throw new TokenizerException("Unsupported regular expression flag '" + cf + "'");
					}
				}
			}
			try {
				final Pattern pattern = Pattern.compile(zccRegexp, patternFlags);
				setPending(new TokenLiteralRegexp(lineState.lineIndex, startIndex, pattern, caseInsensitive, multiline));
			} catch (final PatternSyntaxException exPS) {
				throw new TokenizerException("Invalid regular expression:" + exPS.getMessage());
			}
		}

		@Override
		TokenState digit(int chIndex, char ch) {
			if (flagsOn) {
				emit();
				return new NumericLiteralState(lineState, chIndex, ch);
			}
			if (escapeOn) {
				buffer.append('\\');
				escapeOn = false;
			}
			buffer.append(ch);
			return this;
		}

		@Override
		TokenState eol() {
			if (flagsOn) {
				emit();
				return null;
			}
			throw new TokenizerException("Missing closing / mark");
		}

		@Override
		TokenState letter(int chIndex, char ch) {
			if (flagsOn) {
				flagsBuffer.append(ch);
			} else {
				if (escapeOn) {
					buffer.append('\\');
					escapeOn = false;
				}
				buffer.append(ch);
			}
			return this;
		}

		@Override
		TokenState punctuation(int chIndex, char ch) {
			if (flagsOn) {
				emit();
				return new PuncState(lineState, chIndex, ch);
			}
			if (escapeOn) {
				if (ch == '/') {
					buffer.append('/');
				} else if (ch == '\\') {
					buffer.append('\\');
				} else {
					buffer.append('\\');
					buffer.append(ch);
				}
				escapeOn = false;
			} else {
				if (ch == '/') {
					flagsOn = true;
				} else if (ch == '\\') {
					escapeOn = true;
				} else {
					buffer.append(ch);
				}
			}
			return this;
		}

		@Override
		String type() {
			return "Regular Expression Literal";
		}

		@Override
		TokenState whitespace() {
			if (flagsOn) {
				emit();
				return null;
			}
			if (escapeOn) {
				buffer.append('\\');
				escapeOn = false;
			}
			buffer.append(' ');
			return this;
		}

		RegexpLiteralState(LineState lineState, int chIndex) {
			super(lineState, chIndex);
		}
		private final StringBuilder flagsBuffer = new StringBuilder();

		private boolean flagsOn;

		private boolean escapeOn;
	}// RegexpLiteralState

	private static class ScriptState {

		public void addToken(Token token) {
			assert token != null;
			m_tokens.add(token);
		}

		public boolean allowRegexp() {
			return m_allowRegexp;
		}

		public void endOfScript(TokenState oContinued)
				throws EsSyntaxException {
			if (oContinued != null) {
				final String qType = oContinued.type();
				final String qHere = oContinued.lineState.qLineIndex();
				throw new EsSyntaxException("The " + qType + " which started on line " + qHere
						+ " is not properly terminated");
			}
			m_tokens.add(new TokenEof(m_lineCount));
		}

		public String here(int lineIndex, int startIndex) {
			return lineHere(m_qccSourcePath, m_xptztwLines, lineIndex, startIndex);
		}

		public void setGoal(Token token) {
			assert token != null;
			if (token instanceof TokenKeyword) {
				m_allowRegexp = true;
			} else if (token instanceof TokenPunctuator) {
				final Punctuator punctuator = ((TokenPunctuator) token).punctuator;
				m_allowRegexp = punctuator != Punctuator.RPAREN;
			} else {
				m_allowRegexp = false;
			}
		}

		public List<Token> tokens() {
			return m_tokens;
		}

		public ScriptState(String qccSourcePath, String[] xptztwLines) {
			assert qccSourcePath != null && qccSourcePath.length() > 0;
			assert xptztwLines != null && xptztwLines.length > 0;
			m_qccSourcePath = qccSourcePath;
			m_xptztwLines = xptztwLines;
			m_lineCount = xptztwLines.length;
		}
		private final String m_qccSourcePath;
		private final String[] m_xptztwLines;
		private final int m_lineCount;
		private final List<Token> m_tokens = new ArrayList<Token>(256);
		private boolean m_allowRegexp;
	}

	private static class StringLiteralState extends TokenState {

		private void emit() {
			final String zccPunc = buffer.toString();
			setPending(new TokenLiteralString(lineState.lineIndex, startIndex, zccPunc));
		}

		private void pushHexUnicode(char ch) {
			if (oHexUnicodeBuffer == null) return;

			oHexUnicodeBuffer.append(ch);
			if (oHexUnicodeBuffer.length() == 4) {
				try {
					final char codePoint = (char) Integer.parseInt(oHexUnicodeBuffer.toString(), 16);
					buffer.append(codePoint);
					oHexUnicodeBuffer = null;
					escapeOn = false;
				} catch (final NumberFormatException exNF) {
					throw new TokenizerException("Invalid hex unicode escape sequence (use uxxx form)");
				}
			}
		}

		@Override
		TokenState digit(int chIndex, char ch) {
			if (escapeOn) {
				if (oHexUnicodeBuffer == null) {
					buffer.append(ch);
					escapeOn = false;
				} else {
					pushHexUnicode(ch);
				}
			} else {
				buffer.append(ch);
			}
			return this;
		}

		@Override
		TokenState eol() {
			throw new TokenizerException("Missing closing quote mark");
		}

		@Override
		TokenState letter(int chIndex, char ch) {
			if (escapeOn) {
				if (ch == 'u') {
					oHexUnicodeBuffer = new StringBuilder(4);
				} else {
					if (oHexUnicodeBuffer == null) {
						if (ch == 'n') {
							buffer.append('\n');
						} else if (ch == 'r') {
							buffer.append('\r');
						} else if (ch == 't') {
							buffer.append('\u0009');
						} else if (ch == 'b') {
							buffer.append('\u0008');
						} else if (ch == 'f') {
							buffer.append('\u000C');
						} else if (ch == 'v') {
							buffer.append('\u000B');
						} else {
							buffer.append(ch);
						}
						escapeOn = false;
					} else {
						pushHexUnicode(ch);
					}
				}
			} else {
				buffer.append(ch);
			}
			return this;
		}

		@Override
		TokenState punctuation(int chIndex, char ch) {
			if (escapeOn) {
				if (ch == '\\' || ch == '\'' || ch == '\"') {
					buffer.append(ch);
				} else {
					buffer.append('\\');
					buffer.append(ch);
				}
				escapeOn = false;
				return this;
			}
			if (ch == quoteMark) {
				emit();
				return null;
			} else if (ch == '\\') {
				escapeOn = true;
				return this;
			} else {
				buffer.append(ch);
				return this;
			}
		}

		@Override
		String type() {
			return "String Literal";
		}

		@Override
		TokenState whitespace() {
			buffer.append(' ');
			return this;
		}

		StringLiteralState(LineState lineState, int chIndex, char ch) {
			super(lineState, chIndex);
			quoteMark = ch;
		}

		final char quoteMark;

		boolean escapeOn;

		StringBuilder oHexUnicodeBuffer;
	}// StringLiteralState

	private static abstract class TokenState {

		abstract TokenState digit(int chIndex, char ch);

		abstract TokenState eol();

		abstract TokenState letter(int chIndex, char ch);

		Token popPending() {
			final Token oPopped = m_oPendingToken;
			m_oPendingToken = null;
			return oPopped;
		}

		abstract TokenState punctuation(int chIndex, char ch);

		abstract String type();

		abstract TokenState whitespace();

		protected void setPending(Token token) {
			assert token != null;
			lineState.scriptState.setGoal(token);
			m_oPendingToken = token;
		}

		TokenState(LineState lineState, int startIndex) {
			assert lineState != null;
			this.lineState = lineState;
			this.startIndex = startIndex;
		}

		TokenState(LineState lineState, int startIndex, char ch) {
			assert lineState != null;
			this.lineState = lineState;
			this.startIndex = startIndex;
			buffer.append(ch);
		}
		protected final LineState lineState;

		protected final int startIndex;

		protected final StringBuilder buffer = new StringBuilder();

		private Token m_oPendingToken;
	}// TokenState

	private static class WordState extends TokenState {

		private void emit() {
			final String qccWord = buffer.toString();
			final Keyword oKeyword = Keyword.Table.find(qccWord);
			if (oKeyword != null) {
				setPending(new TokenKeyword(lineState.lineIndex, startIndex, oKeyword));
				return;
			}

			final BooleanLiteral oBooleanLiteral = BooleanLiteral.find(qccWord);
			if (oBooleanLiteral != null) {
				setPending(new TokenBooleanLiteral(lineState.lineIndex, startIndex, oBooleanLiteral));
				return;
			}

			final NullLiteral oNullLiteral = NullLiteral.find(qccWord);
			if (oNullLiteral != null) {
				setPending(new TokenNullLiteral(lineState.lineIndex, startIndex, oNullLiteral));
				return;
			}

			setPending(new TokenIdentifier(lineState.lineIndex, startIndex, qccWord));
		}

		@Override
		TokenState digit(int chIndex, char ch) {
			buffer.append(ch);
			return this;
		}

		@Override
		TokenState eol() {
			emit();
			return null;
		}

		@Override
		TokenState letter(int chIndex, char ch) {
			buffer.append(ch);
			return this;
		}

		@Override
		TokenState punctuation(int chIndex, char ch) {
			if (ch == '_' || ch == '$') {
				buffer.append(ch);
				return this;
			}
			emit();
			return new PuncState(lineState, chIndex, ch);
		}

		@Override
		String type() {
			return "Word";
		}

		@Override
		TokenState whitespace() {
			emit();
			return null;
		}

		WordState(LineState lineState, int chIndex, char ch) {
			super(lineState, chIndex, ch);
		}
	}// WordState

	private static class XmlLiteralState extends TokenState {

		private void emit() {
			final String zccXml = buffer.toString();
			final W3cDom dom = W3cDom.newInstance(zccXml, false);
			final String oqClean = dom.oqClean(false);
			if (oqClean != null) throw new TokenizerException("Invalid xml literal: " + oqClean);
			setPending(new TokenLiteralXml(lineState.lineIndex, startIndex, dom));
		}

		private void saveBlack(char ch) {
			buffer.append(ch);
			preBlack0 = preBlack1;
			preBlack1 = ch;
		}

		private void saveWhite(char ch) {
			buffer.append(ch);
		}

		@Override
		TokenState digit(int chIndex, char ch) {
			saveBlack(ch);
			return this;
		}

		@Override
		TokenState eol() {
			saveWhite('\n');
			return this;
		}

		@Override
		TokenState letter(int chIndex, char ch) {
			if (cdata || comment) {
				saveBlack(ch);
				return this;
			}

			if (preBlack1 == '<') {
				depth++;
			} else if (preBlack0 == '<' && preBlack1 == '/') {
				endTag = true;
			} else if (oBangBuffer != null) {
				oBangBuffer.append(ch);
			}

			saveBlack(ch);
			return this;
		}

		@Override
		TokenState punctuation(int chIndex, char ch) {
			if (doctype) {
				if (doctypeInternal) {
					if (ch == ']') {
						doctypeInternal = false;
					}
				} else {
					if (ch == '[') {
						doctypeInternal = true;
					} else if (ch == '>') {
						doctype = false;
					}
				}
				saveBlack(ch);
				return this;
			}

			if (cdata) {
				if (preBlack0 == ']' && preBlack1 == ']' && ch == '>') {
					cdata = false;
				}
				saveBlack(ch);
				return this;
			}

			if (comment) {
				if (preBlack0 == '-' && preBlack1 == '-' && ch == '>') {
					comment = false;
				}
				saveBlack(ch);
				return this;
			}

			if (processingInstruction) {
				if (preBlack1 == '?' && ch == '>') {
					processingInstruction = false;
				}
				saveBlack(ch);
				return this;
			}

			if (endTag && ch == '>') {
				endTag = false;
				depth--;
				saveBlack(ch);
				if (depth > 0) return this;
				if (depth == 0 && !comment && !processingInstruction && !cdata && !doctype && oBangBuffer == null) {
					emit();
					return null;
				}
				throw new TokenizerException("Elements are unbalanced");
			}

			if (oBangBuffer != null) {
				if (ch == '[') {
					if (oBangBuffer.length() == 0) {
						oBangBuffer.append('[');
					} else {
						final String qBang = oBangBuffer.toString();
						if (qBang.equals("[CDATA")) {
							cdata = true;
							oBangBuffer = null;
						} else
							throw new TokenizerException("Expecting CDATA, but found '" + qBang + "'");
					}
				} else if (preBlack1 == '-' && ch == '-') {
					comment = true;
					oBangBuffer = null;
				}

				saveBlack(ch);
				return this;
			}

			if (preBlack1 == '<' && ch == '!') {
				oBangBuffer = new StringBuilder();
			} else if (preBlack1 == '<' && ch == '?') {
				processingInstruction = true;
			} else if (preBlack1 == '/' && ch == '>') {
				depth--;
			}

			saveBlack(ch);
			return this;
		}

		@Override
		String type() {
			return "Xml Literal";
		}

		@Override
		TokenState whitespace() {
			if (!cdata && oBangBuffer != null) {
				final String qBang = oBangBuffer.toString();
				if (qBang.equals("DOCTYPE")) {
					if (depth == 0) {
						doctype = true;
						oBangBuffer = null;
					} else
						throw new TokenizerException("Misplaced DOCTYPE");
				} else
					throw new TokenizerException("Expecting DOCTYPE, but found '" + qBang + "'");
			}
			saveWhite(' ');
			return this;
		}

		XmlLiteralState(LineState lineState, int chIndex) {
			super(lineState, chIndex);
			depth = 0;
			saveBlack('<');
		}
		char preBlack0 = ' ';
		char preBlack1 = ' ';
		boolean endTag;
		boolean comment;
		boolean processingInstruction;
		boolean cdata;
		boolean doctype;
		boolean doctypeInternal;
		StringBuilder oBangBuffer;
		int depth;
	}

}
