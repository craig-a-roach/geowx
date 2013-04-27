/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

import java.util.ArrayList;
import java.util.List;

import com.metservice.argon.ArgonText;
import com.metservice.argon.CodedEnumTable;
import com.metservice.argon.Ds;
import com.metservice.argon.ICodedEnum;

/**
 * @author roach
 */
class WKTCoordinateSystemFactory {

	private static final CodedEnumTable<Keyword> KeywordTable = new CodedEnumTable<Keyword>(Keyword.class, true, Keyword.values());
	private static final int SyntaxPreLen = 20;
	private static final int SyntaxPostLen = 20;
	private static final int SyntaxPreCount = 5;
	private static final int SyntaxPostCount = 5;
	private static final String SyntaxMarker = "^^^";

	private static final char PuncQuote = '\"';
	private static final char PuncPlus = '+';
	private static final char PuncNeg = '-';
	private static final char PuncDot = '.';
	private static final char PuncUnderscore = '_';
	private static final char PuncSquareL = '[';
	private static final char PuncSquareR = ']';
	private static final char PuncComma = ',';
	private static final char[] PuncSymbolArray = { PuncQuote, PuncPlus, PuncNeg, PuncDot, PuncUnderscore, PuncSquareL,
			PuncSquareR, PuncComma };
	private static final String PuncSymbols = new String(PuncSymbolArray);

	private static final TokenListDelimiter TokenList_Open = new TokenListDelimiter(ListDelimiter.Open);
	private static final TokenListDelimiter TokenList_Close = new TokenListDelimiter(ListDelimiter.Close);
	private static final TokenListDelimiter TokenList_Separator = new TokenListDelimiter(ListDelimiter.Separator);

	private static final String KA_CS = "<CS>";
	private static final String KA_S = "<S>";

	private static String diagnostic(String qtwSpec, int chIndex) {
		final String zPre = qtwSpec.substring(0, chIndex);
		final int preStart = Math.max(0, zPre.length() - SyntaxPreLen);
		final String zPost = qtwSpec.substring(chIndex);
		final int postEnd = Math.max(0, zPost.length() - SyntaxPostLen);
		final String zPreClamp = zPre.substring(preStart);
		final String zPostClamp = zPost.substring(0, postEnd);
		final String diag = zPreClamp + SyntaxMarker + zPostClamp;
		return diag;
	}

	private static TokenListDelimiter findListDelimiterToken(char ch) {
		switch (ch) {
			case PuncSquareL:
				return TokenList_Open;
			case PuncComma:
				return TokenList_Separator;
			case PuncSquareR:
				return TokenList_Close;
			default:
				return null;
		}
	}

	private static boolean isPuncListDelimiter(char ch) {
		return ch == PuncComma | ch == PuncSquareL || ch == PuncSquareR;
	}

	private static boolean isPuncNumber(char ch) {
		return ch == PuncDot | ch == PuncNeg || ch == PuncPlus;
	}

	private static boolean isPuncQuote(char ch) {
		return ch == PuncQuote;
	}

	private static List<Token> newTokenStream(String qtwSpec)
			throws GalliumSyntaxException {
		final List<Token> zlTokens = new ArrayList<>(32);
		final Controller ctl = new Controller(zlTokens);
		State state = new StateInit();
		final int len = qtwSpec.length();
		int chIndex = 0;
		CharacterClass oCharacterClass = null;
		while (chIndex < len) {
			final char ch = qtwSpec.charAt(chIndex);
			try {
				if (oCharacterClass == null) {
					oCharacterClass = selectCharacterClass(ch);
				}
				switch (oCharacterClass) {
					case Digit:
						state = state.digit(ctl, ch);
					break;
					case Letter:
						state = state.letter(ctl, ch);
					break;
					case Punctuation:
						state = state.punctuation(ctl, ch);
					break;
					case Whitespace:
						state = state.whitespace(ctl);
					default:
				}
			} catch (final SyntaxException ex) {
				final String m = "Malformed WKT spec; " + ex.getMessage() + " at character index " + chIndex;
				final String diag = diagnostic(qtwSpec, chIndex);
				throw new GalliumSyntaxException(m + "\n" + diag);
			}
			if (ctl.popAdvance()) {
				chIndex++;
				oCharacterClass = null;
			}
		}
		try {
			state.terminator(ctl);
		} catch (final SyntaxException ex) {
			final String m = "Incomplete WKT spec; " + ex.getMessage();
			throw new GalliumSyntaxException(m);
		}
		return zlTokens;
	}

	private static Authority parseAuthority(TokenReader tr)
			throws SyntaxException {
		tr.consumeListDelimiterOpen();
		final String qtwNamespace = tr.consumeLiteralQtw();
		tr.consumeListDelimiterSeparator();
		final String qtwCode = tr.consumeLiteralQtw();
		tr.consumeListDelimiterClose();
		return Authority.newInstance(qtwNamespace, qtwCode);
	}

	private static IGalliumCoordinateSystem parseCS(TokenReader tr)
			throws GalliumSyntaxException {
		assert tr != null;
		try {
			final Keyword keyword = tr.consumeKeyword();
			if (!keyword.isCoordinateSystem()) {
				final String m = "'" + keyword.qCode() + "' is not a coordinate system";
				throw new SyntaxException(m);
			}
			switch (keyword) {
				case GEOGCS:
					return parseGeographicCS(tr);
				default:
					throw new SyntaxException("Coordinate system '" + keyword.qCode() + "' is not supported");
			}
		} catch (final SyntaxException ex) {
			final String m = "Malformed WKT spec; " + ex.getMessage();
			final String diag = tr.diagnostic();
			throw new GalliumSyntaxException(m + "\n" + diag);
		}
	}

	private static Datum parseDatum(TokenReader tr)
			throws SyntaxException {
		tr.consumeListDelimiterOpen();
		final String qtwName = tr.consumeLiteralQtw();
		if (!tr.consumeListDelimiterMore()) {
			final Datum oDatum = DatumDictionary.findByName(qtwName);
			if (oDatum != null) return oDatum;
			final String m = "Datum '" + qtwName + "' is not in dictionary; require definition";
			throw new SyntaxException(m);
		}
		tr.consumeKeyword(Keyword.SPHEROID);
		final Ellipsoid ellipsoid = parseEllipsoid(tr);
		ParameterArray oTransform = null;
		Authority oAuthority = null;
		while (tr.consumeListDelimiterMore()) {
			final Keyword keyword = tr.consumeKeyword();
			switch (keyword) {
				case TOWGS84:
					oTransform = parseParameterArray(tr, 7);
				break;
				case AUTHORITY:
					oAuthority = parseAuthority(tr);
				break;
				default:
					throw new SyntaxException("Unexpected datum attribute '" + keyword + "'");
			}
		}
		return Datum.newInstance(qtwName, ellipsoid, oTransform, oAuthority);
	}

	private static Ellipsoid parseEllipsoid(TokenReader tr)
			throws SyntaxException {
		tr.consumeListDelimiterOpen();
		final String qtwName = tr.consumeLiteralQtw();
		if (!tr.consumeListDelimiterMore()) {
			final Ellipsoid oEllipsoid = EllipsoidDictionary.findByName(qtwName);
			if (oEllipsoid != null) return oEllipsoid;
			final String m = "Ellipsoid/spheroid '" + qtwName + "' is not in dictionary; require definition";
			throw new SyntaxException(m);
		}
		final double semiMajorMetres = tr.consumeLiteralDouble();
		tr.consumeListDelimiterSeparator();
		final double inverseFlattening = tr.consumeLiteralDouble();
		Authority oAuthority = null;
		if (tr.consumeListDelimiterMore()) {
			tr.consumeKeyword(Keyword.AUTHORITY);
			oAuthority = parseAuthority(tr);
			tr.consumeListDelimiterClose();
		}

		return Ellipsoid.newInverseFlattening(qtwName, semiMajorMetres, inverseFlattening, oAuthority);
	}

	private static IGalliumCoordinateSystem parseGeographicCS(TokenReader tr)
			throws SyntaxException {
		tr.consumeListDelimiterOpen();
		final String qtwName = tr.consumeLiteralQtw();
		tr.consumeKeyword(Keyword.DATUM);
		final Datum datum = parseDatum(tr);

		// TODO Auto-generated method stub
		return null;
	}

	private static ParameterArray parseParameterArray(TokenReader tr, int max)
			throws SyntaxException {
		tr.consumeListDelimiterOpen();
		final ParameterArray pa = new ParameterArray(max);
		pa.add(tr.consumeLiteralDouble());
		while (tr.consumeListDelimiterMore()) {
			pa.add(tr.consumeLiteralDouble());
		}
		return pa;
	}

	private static CharacterClass selectCharacterClass(char ch)
			throws SyntaxException {
		if (ArgonText.isDigit(ch)) return CharacterClass.Digit;
		if (ArgonText.isLetter(ch)) return CharacterClass.Letter;
		if (ArgonText.isWhitespace(ch)) return CharacterClass.Whitespace;
		if (PuncSymbols.indexOf(ch) >= 0) return CharacterClass.Punctuation;
		throw new SyntaxException("Unrecognised punctuation symbol");
	}

	public static IGalliumCoordinateSystem newCoordinateSystem(String ozSpec)
			throws GalliumSyntaxException {
		final String oqtwSpec = ArgonText.oqtw(ozSpec);
		if (oqtwSpec == null) throw new GalliumSyntaxException("Empty WKT Spec");
		final List<Token> zlTokens = newTokenStream(oqtwSpec);
		final TokenReader tr = new TokenReader(zlTokens);
		return parseCS(tr);
	}

	private WKTCoordinateSystemFactory() {
	}

	private static enum CharacterClass {
		Letter, Digit, Whitespace, Punctuation;
	}

	private static class Controller {

		public void consume() {
			m_advance = true;
		}

		public void consume(char ch) {
			m_buffer.append(ch);
			m_advance = true;
		}

		public boolean popAdvance() {
			final boolean value = m_advance;
			m_advance = false;
			return value;
		}

		public double popDouble()
				throws SyntaxException {
			final String qtw = popQ().trim();
			if (qtw.length() == 0) throw new SyntaxException("Value is just white space");
			try {
				return Double.parseDouble(qtw);
			} catch (final NumberFormatException ex) {
				throw new SyntaxException("Malformed floating-point value");
			}
		}

		public String popQ()
				throws SyntaxException {
			final String z = popZ();
			if (z.length() == 0) throw new SyntaxException("Expecting a name/keyword of non-zero length");
			return z;
		}

		public String popQtw()
				throws SyntaxException {
			final String qtw = popQ().trim();
			if (qtw.length() == 0) throw new SyntaxException("Name/keyword is just white space");
			return qtw;
		}

		public String popZ() {
			final String neo = m_buffer.toString();
			m_buffer.setLength(0);
			return neo;
		}

		public void push(Token t) {
			assert t != null;
			m_tokenList.add(t);
		}

		@Override
		public String toString() {
			final Ds ds = Ds.o("Controller");
			ds.a("buffer", m_buffer);
			ds.a("advance", m_advance);
			return ds.s();
		}

		public Controller(List<Token> tokenList) {
			assert tokenList != null;
			m_tokenList = tokenList;
			m_buffer = new StringBuilder();
		}
		private final List<Token> m_tokenList;
		private final StringBuilder m_buffer;
		private boolean m_advance;
	}

	private static enum Keyword implements ICodedEnum {
		GEOGCS("GEOGCS", KA_CS + KA_S),
		PROJCS("PROJCS", KA_CS + KA_S),
		DATUM("DATUM", KA_S),
		SPHEROID("SPHEROID", KA_S),
		PRIMEM("PRIMEM", KA_S),
		TOWGS84("TOWGS84", KA_S),
		UNIT("UNIT", KA_S),
		AUTHORITY("AUTHORITY", KA_S);

		public boolean isCoordinateSystem() {
			return zAttributes.contains(KA_CS);
		}

		@Override
		public String qCode() {
			return qCode;
		}

		Keyword(String qCode, String zAttributes) {
			assert qCode != null && qCode.length() > 0;
			assert zAttributes != null;
			this.qCode = qCode;
			this.zAttributes = zAttributes;
		}
		public final String qCode;
		public final String zAttributes;
	}

	private static enum ListDelimiter {
		Open("["), Close("]"), Separator(",");

		public String qCode() {
			return m_qCode;
		}

		private ListDelimiter(String qCode) {
			m_qCode = qCode;
		}
		private final String m_qCode;
	}

	private static abstract class State {

		private String expectedMessage() {
			return "Expecting " + expectingAdvice();
		}

		protected abstract String expectingAdvice();

		public State digit(Controller ctl, char ch)
				throws SyntaxException {
			throw new SyntaxException(expectedMessage());
		}

		public State letter(Controller ctl, char ch)
				throws SyntaxException {
			throw new SyntaxException(expectedMessage());
		}

		public State punctuation(Controller ctl, char ch)
				throws SyntaxException {
			throw new SyntaxException(expectedMessage());
		}

		public State terminator(Controller ctl)
				throws SyntaxException {
			throw new SyntaxException(expectedMessage());
		}

		public State whitespace(Controller ctl)
				throws SyntaxException {
			throw new SyntaxException(expectedMessage());
		}

		protected State() {
		}
	}

	private static class StateInit extends State {

		@Override
		protected String expectingAdvice() {
			return "a keyword, number or string";
		}

		@Override
		public State digit(Controller ctl, char ch)
				throws SyntaxException {
			return new StateNumber();
		}

		@Override
		public State letter(Controller ctl, char ch)
				throws SyntaxException {
			return new StateKeyword();
		}

		@Override
		public State punctuation(Controller ctl, char ch)
				throws SyntaxException {
			if (isPuncQuote(ch)) return new StateString();
			if (isPuncListDelimiter(ch)) return new StateListDelimiter();
			return super.punctuation(ctl, ch);
		}

		@Override
		public State terminator(Controller ctl)
				throws SyntaxException {
			return this;
		}

		@Override
		public State whitespace(Controller ctl)
				throws SyntaxException {
			ctl.consume();
			return this;
		}

	}

	private static class StateKeyword extends State {

		private void push(Controller ctl)
				throws SyntaxException {
			final String qtw = ctl.popQtw();
			final Keyword oKeyword = KeywordTable.find(qtw);
			if (oKeyword == null) {
				final String m = "Unrecognised keyword '" + qtw + "'";
				throw new SyntaxException(m);
			}
			ctl.push(new TokenKeyword(oKeyword));
		}

		@Override
		protected String expectingAdvice() {
			return "a keyword containing letter, digits or underscore";
		}

		@Override
		public State digit(Controller ctl, char ch)
				throws SyntaxException {
			ctl.consume(ch);
			return this;
		}

		@Override
		public State letter(Controller ctl, char ch)
				throws SyntaxException {
			ctl.consume(ch);
			return this;
		}

		@Override
		public State punctuation(Controller ctl, char ch)
				throws SyntaxException {
			if (ch == PuncUnderscore) {
				ctl.consume(ch);
				return this;
			}
			push(ctl);
			return new StateInit();
		}

		@Override
		public State whitespace(Controller ctl)
				throws SyntaxException {
			push(ctl);
			return new StateInit();
		}
	}

	private static class StateListDelimiter extends State {

		@Override
		protected String expectingAdvice() {
			return "a square brace or comma";
		}

		@Override
		public State punctuation(Controller ctl, char ch)
				throws SyntaxException {
			final TokenListDelimiter oToken = findListDelimiterToken(ch);
			if (oToken != null) {
				ctl.consume();
				ctl.push(oToken);
				return new StateInit();
			}
			return super.punctuation(ctl, ch);
		}

	}

	private static class StateNumber extends State {

		private void push(Controller ctl)
				throws SyntaxException {
			final double n = ctl.popDouble();
			ctl.push(new TokenLiteralNumber(n));
		}

		@Override
		protected String expectingAdvice() {
			return "floating-point digits, indicators, keywords or punctuation";
		}

		@Override
		public State digit(Controller ctl, char ch)
				throws SyntaxException {
			ctl.consume(ch);
			return this;
		}

		@Override
		public State letter(Controller ctl, char ch)
				throws SyntaxException {
			ctl.consume(ch);
			return this;
		}

		@Override
		public State punctuation(Controller ctl, char ch)
				throws SyntaxException {
			if (isPuncNumber(ch)) {
				ctl.consume(ch);
				return this;
			}
			push(ctl);
			return new StateInit();
		}

		@Override
		public State whitespace(Controller ctl)
				throws SyntaxException {
			push(ctl);
			return new StateInit();
		}
	}

	private static class StateString extends State {

		private State consumeBlack(Controller ctl, char ch) {
			ctl.consume(ch);
			m_preBlack = true;
			return this;
		}

		private void push(Controller ctl)
				throws SyntaxException {
			final String qtw = ctl.popQtw();
			ctl.push(new TokenLiteralString(qtw));
		}

		@Override
		protected String expectingAdvice() {
			return "a quoted string";
		}

		@Override
		public State digit(Controller ctl, char ch)
				throws SyntaxException {
			return consumeBlack(ctl, ch);
		}

		@Override
		public State letter(Controller ctl, char ch)
				throws SyntaxException {
			return consumeBlack(ctl, ch);
		}

		@Override
		public State punctuation(Controller ctl, char ch)
				throws SyntaxException {
			if (isPuncQuote(ch)) {
				ctl.consume();
				if (m_open) {
					push(ctl);
					return new StateInit();
				}
				m_open = true;
				return this;
			}
			return consumeBlack(ctl, ch);
		}

		@Override
		public State whitespace(Controller ctl)
				throws SyntaxException {
			if (m_preBlack) {
				ctl.consume(' ');
				m_preBlack = false;
			}
			return this;
		}

		public StateString() {
		}
		private boolean m_open = false;
		private boolean m_preBlack = false;
	}

	private static class SyntaxException extends Exception {

		public SyntaxException(String message) {
			super(message);
		}
	}

	private static interface Token {

		public String echo();
	}

	private static class TokenKeyword implements Token {

		@Override
		public String echo() {
			return keyword.qCode();
		}

		@Override
		public String toString() {
			return "Keyword:" + keyword;
		}

		public TokenKeyword(Keyword keyword) {
			assert keyword != null;
			this.keyword = keyword;
		}
		public final Keyword keyword;
	}

	private static class TokenListDelimiter implements Token {

		@Override
		public String echo() {
			return listDelimiter.qCode();
		}

		@Override
		public String toString() {
			return "ListDelimiter:" + listDelimiter;
		}

		public TokenListDelimiter(ListDelimiter d) {
			assert d != null;
			this.listDelimiter = d;
		}
		public final ListDelimiter listDelimiter;
	}

	private static interface TokenLiteral extends Token {
	}

	private static class TokenLiteralNumber implements TokenLiteral {

		@Override
		public String echo() {
			return Double.toString(value);
		}

		@Override
		public String toString() {
			return "LiteralNumber:" + value;
		}

		public TokenLiteralNumber(double value) {
			this.value = value;
		}
		public final double value;
	}

	private static class TokenLiteralString implements TokenLiteral {

		@Override
		public String echo() {
			return "\"" + qtwValue + "\"";
		}

		@Override
		public String toString() {
			return "LiteralString:" + qtwValue;
		}

		public TokenLiteralString(String qtwValue) {
			assert qtwValue != null && qtwValue.length() > 0;
			this.qtwValue = qtwValue;
		}
		public final String qtwValue;
	}

	private static class TokenReader {

		private <T extends Token> T consumeToken(Class<T> tokenClass, String type)
				throws SyntaxException {
			if (m_index < m_count) {
				final Token t = m_zlTokens.get(m_index);
				if (tokenClass.isInstance(t)) {
					m_index++;
					return tokenClass.cast(t);
				}
			}
			throw new SyntaxException("Expecting a " + type);
		}

		// @TODO
		// private <T extends Token> T peekToken(Class<T> tokenClass) {
		// if (m_index < m_count) {
		// final Token t = m_zlTokens.get(m_index);
		// if (tokenClass.isInstance(t)) return tokenClass.cast(t);
		// }
		// return null;
		// }
		public Keyword consumeKeyword()
				throws SyntaxException {
			return consumeToken(TokenKeyword.class, "keyword").keyword;
		}

		public void consumeKeyword(Keyword expected)
				throws SyntaxException {
			final Keyword actual = consumeKeyword();
			if (actual != expected) {
				final String m = "Expecting keyword '" + expected + "'";
				throw new SyntaxException(m);
			}
		}

		public ListDelimiter consumeListDelimiter()
				throws SyntaxException {
			return consumeToken(TokenListDelimiter.class, "list delimiter").listDelimiter;
		}

		public void consumeListDelimiter(ListDelimiter expected)
				throws SyntaxException {
			final ListDelimiter d = consumeListDelimiter();
			if (d != expected) {
				final String m = "Expecting a '" + expected.qCode() + "' list delimiter character";
				throw new SyntaxException(m);
			}
		}

		public void consumeListDelimiterClose()
				throws SyntaxException {
			consumeListDelimiter(ListDelimiter.Close);
		}

		public boolean consumeListDelimiterMore()
				throws SyntaxException {
			final ListDelimiter d = consumeListDelimiter();
			switch (d) {
				case Separator:
					return true;
				case Close:
					return false;
				default:
					throw new SyntaxException("Expecting a comma or closing bracket");
			}
		}

		public void consumeListDelimiterOpen()
				throws SyntaxException {
			consumeListDelimiter(ListDelimiter.Open);
		}

		public void consumeListDelimiterSeparator()
				throws SyntaxException {
			consumeListDelimiter(ListDelimiter.Separator);
		}

		public double consumeLiteralDouble()
				throws SyntaxException {
			return consumeToken(TokenLiteralNumber.class, "numeric literal").value;
		}

		public String consumeLiteralQtw()
				throws SyntaxException {
			return consumeToken(TokenLiteralString.class, "string literal").qtwValue;
		}

		public String diagnostic() {
			final int start = Math.max(0, m_index - SyntaxPreCount);
			final int end = Math.min(m_count, m_index + SyntaxPostCount);
			final StringBuilder sb = new StringBuilder();
			for (int i = start; i < end; i++) {
				final Token token = m_zlTokens.get(i);
				if (i == m_index) {
					sb.append(SyntaxMarker);
				}
				sb.append(token.echo());
			}
			return sb.toString();
		}

		// @TODO
		// public Keyword peekKeyword() {
		// final TokenKeyword oToken = peekToken(TokenKeyword.class);
		// return oToken == null ? null : oToken.keyword;
		// }

		@Override
		public String toString() {
			return diagnostic();
		}

		public TokenReader(List<Token> zlTokens) {
			if (zlTokens == null) throw new IllegalArgumentException("object is null");
			m_zlTokens = zlTokens;
			m_count = zlTokens.size();
		}
		private final List<Token> m_zlTokens;
		private final int m_count;
		private int m_index;
	}
}
