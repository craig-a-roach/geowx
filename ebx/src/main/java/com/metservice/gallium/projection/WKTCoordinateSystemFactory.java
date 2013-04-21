/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

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

	private static String diagnostic(String qtwSpec, int chIndex) {
		final String zPre = qtwSpec.substring(0, chIndex);
		final int preStart = Math.max(0, zPre.length() - SyntaxPreLen);
		final String zPost = qtwSpec.substring(chIndex);
		final int postEnd = Math.max(0, zPost.length() - SyntaxPostLen);
		final String zPreClamp = zPre.substring(preStart);
		final String zPostClamp = zPost.substring(0, postEnd);
		final String diag = zPreClamp + "^^^" + zPostClamp;
		return diag;
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

	private static void newTokenStream(String qtwSpec)
			throws GalliumSyntaxException {
		final Controller ctl = new Controller();
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
		newTokenStream(oqtwSpec);
		return null;
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

		public void pushKeyword(Keyword k) {
			System.out.println("KEYWORD=" + k);
		}

		public void pushListDelimiter(char ch) {
			System.out.println("LISTDELIMITER=" + ch);
		}

		public void pushNumber(double numeric) {
			System.out.println("NUMBER=" + numeric);
		}

		public void pushString(String qtw) {
			System.out.println("STRING=" + qtw);
		}

		@Override
		public String toString() {
			final Ds ds = Ds.o("Controller");
			ds.a("buffer", m_buffer);
			ds.a("advance", m_advance);
			return ds.s();
		}

		public Controller() {
			m_buffer = new StringBuilder();
		}
		private final StringBuilder m_buffer;
		private boolean m_advance;
	}

	private static enum Keyword implements ICodedEnum {
		GEOGCS("GEOGCS"), PROJCS("PROJCS"), DATUM("DATUM"), SPHEROID("SPHEROID"), PRIMEM("PRIMEM"), UNIT("UNIT");

		@Override
		public String qCode() {
			return qCode;
		}

		Keyword(String qCode) {
			assert qCode != null && qCode.length() > 0;
			this.qCode = qCode;
		}
		public final String qCode;
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
			ctl.pushKeyword(oKeyword);
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
			if (isPuncListDelimiter(ch)) {
				ctl.consume();
				ctl.pushListDelimiter(ch);
				return new StateInit();
			}
			return super.punctuation(ctl, ch);
		}

	}

	private static class StateNumber extends State {

		private void push(Controller ctl)
				throws SyntaxException {
			final double n = ctl.popDouble();
			ctl.pushNumber(n);
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
			ctl.pushString(qtw);
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

	private static abstract class Token {
	}
}
