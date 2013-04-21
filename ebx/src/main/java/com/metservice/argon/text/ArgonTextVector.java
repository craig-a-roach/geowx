/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author roach
 */
public class ArgonTextVector {
	private int hardWidth() {
		final int c = m_xl.size();
		int width = 0;
		for (int i = 0; i < c; i++) {
			width = Math.max(width, m_xl.get(i).length());
		}
		return width;
	}

	private String hBorder(String zLeftCorner, String zHorz, String zRightCorner) {
		final int width = width();
		final int lenLCorner = zLeftCorner.length();
		final int lenHorz = zHorz.length();
		final int lenRCorner = zRightCorner.length();
		final boolean corners = width > (lenLCorner + lenRCorner);
		final StringBuilder b = new StringBuilder();
		if (corners) {
			b.append(zLeftCorner);
		}
		final int hrun = corners ? width - lenLCorner - lenRCorner : width;
		for (int i = 0; i < hrun; i++) {
			b.append(zHorz.charAt(i % lenHorz));
		}

		if (corners) {
			b.append(zRightCorner);
		}

		return b.toString();
	}

	public void addAbove(ArgonTextVector operand) {
		if (operand == null) throw new IllegalArgumentException("object is null");

		final int srcCount = operand.m_xl.size();
		final List<StringBuilder> ex = new ArrayList<StringBuilder>(m_xl);
		m_xl.clear();
		for (int isrc = 0; isrc < srcCount; isrc++) {
			final StringBuilder bsrc = operand.m_xl.get(isrc);
			m_xl.add(new StringBuilder(bsrc));
		}
		m_xl.addAll(ex);

		m_lzyWidth = null;
	}

	public void addBelow(ArgonTextVector operand) {
		if (operand == null) throw new IllegalArgumentException("object is null");

		final int srcCount = operand.m_xl.size();
		for (int isrc = 0; isrc < srcCount; isrc++) {
			final StringBuilder bsrc = operand.m_xl.get(isrc);
			m_xl.add(new StringBuilder(bsrc));
		}

		m_lzyWidth = null;
	}

	public void addBR() {
		m_xl.add(new StringBuilder());
	}

	public void addCRLFText(String z) {
		if (z == null) throw new IllegalArgumentException("object is null");
		final int len = z.length();
		if (len == 0) return;

		final StringBuilder line = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			final char ch = z.charAt(i);
			if (ch == '\n') {
				addPlainText(line.toString());
				line.setLength(0);
				addBR();
			} else if (ch != '\r') {
				line.append(ch);
			}
		}
		addPlainText(line.toString());
	}

	public void addHtmlEscapedTextNode(String zesc) {
		if (zesc == null) throw new IllegalArgumentException("object is null");
		final int escl = zesc.length();
		if (escl == 0) return;

		final StringBuilder line = new StringBuilder(escl + 32);
		final StringBuilder expansion = new StringBuilder();
		boolean expanding = false;
		for (int i = 0; i < escl; i++) {
			final char ch = zesc.charAt(i);
			switch (ch) {
				case '&': {
					expanding = true;
				}
				break;

				case ';': {
					if (expanding) {
						final String zexp = expansion.toString();
						expansion.setLength(0);
						expanding = false;
						if (zexp.equals("lt")) {
							line.append('<');
						} else if (zexp.equals("gt")) {
							line.append('>');
						} else if (zexp.equals("amp")) {
							line.append('&');
						} else if (zexp.equals("apos")) {
							line.append('\'');
						} else if (zexp.equals("quot")) {
							line.append('\"');
						} else if (zexp.startsWith("#")) {
							final int expl = zexp.length();
							if (expl >= 2 && expl <= 6) {
								final boolean hex = (expl >= 3) && (zexp.charAt(1) == 'x');
								try {
									final int startIndex = hex ? 2 : 1;
									final int radix = hex ? 16 : 10;
									final char expch = (char) Integer.parseInt(zexp.substring(startIndex), radix);
									if (expch == '\n') {
										addPlainText(line.toString());
										line.setLength(0);
										addBR();
									} else {
										line.append(expch);
									}
								} catch (final NumberFormatException exNF) {
								}
							}
						}
					} else {
						line.append(ch);
					}
				}
				break;

				default: {
					if (expanding) {
						expansion.append(ch);
					} else {
						line.append(ch);
					}
				}
			}
		}
		addPlainText(line.toString());
	}

	public void addLeft(ArgonTextVector operand) {
		if (operand == null) throw new IllegalArgumentException("object is null");

		final int lhsCount = operand.m_xl.size();
		final int rhsCount = m_xl.size();
		final int lhsWidth = operand.width();
		final int rhsWidth = width();
		final int neoWidth = lhsWidth + rhsWidth;
		int i = 0;
		while (i < lhsCount && i < rhsCount) {
			final StringBuilder blhs = operand.m_xl.get(i);
			final StringBuilder brhs = m_xl.get(i);
			final int rightPad = lhsWidth - blhs.length();
			leftPad(brhs, rightPad, ' ');
			brhs.insert(0, blhs);
			i++;
		}
		while (i < lhsCount) {
			final StringBuilder blhs = operand.m_xl.get(i);
			final StringBuilder neo = new StringBuilder(blhs);
			m_xl.add(neo);
			i++;
		}
		while (i < rhsCount) {
			final StringBuilder brhs = m_xl.get(i);
			leftPad(brhs, lhsWidth, ' ');
			i++;
		}

		m_lzyWidth = new Integer(neoWidth);
	}

	public void addPlainText(String z) {
		if (z == null) throw new IllegalArgumentException("object is null");
		final int len = z.length();
		if (len == 0) return;

		m_xl.get(m_xl.size() - 1).append(z);
		m_lzyWidth = null;
	}

	public void addRight(ArgonTextVector operand) {
		if (operand == null) throw new IllegalArgumentException("object is null");

		final int lhsCount = m_xl.size();
		final int rhsCount = operand.m_xl.size();
		final int lhsWidth = width();
		final int rhsWidth = operand.width();
		final int neoWidth = lhsWidth + rhsWidth;
		int i = 0;
		while (i < lhsCount && i < rhsCount) {
			final StringBuilder blhs = m_xl.get(i);
			final StringBuilder brhs = operand.m_xl.get(i);
			final int rightPad = lhsWidth - blhs.length();
			rightPad(blhs, rightPad, ' ');
			blhs.append(brhs);
			i++;
		}
		while (i < rhsCount) {
			final StringBuilder brhs = operand.m_xl.get(i);
			final StringBuilder neo = new StringBuilder(neoWidth);
			rightPad(neo, lhsWidth, ' ');
			neo.append(brhs);
			m_xl.add(neo);
			i++;
		}

		m_lzyWidth = new Integer(neoWidth);
	}

	public void borderAbove(String zLeftCorner, String zTop, String zRightCorner) {
		if (zLeftCorner == null) throw new IllegalArgumentException("object is null");
		if (zTop == null) throw new IllegalArgumentException("object is null");
		if (zRightCorner == null) throw new IllegalArgumentException("object is null");

		m_xl.add(0, new StringBuilder(hBorder(zLeftCorner, zTop, zRightCorner)));
	}

	public void borderBelow(String zLeftCorner, String zBottom, String zRightCorner) {
		if (zLeftCorner == null) throw new IllegalArgumentException("object is null");
		if (zBottom == null) throw new IllegalArgumentException("object is null");
		if (zRightCorner == null) throw new IllegalArgumentException("object is null");

		m_xl.add(new StringBuilder(hBorder(zLeftCorner, zBottom, zRightCorner)));
	}

	public void borderLeft(String zTopCorner, String zLeft, String zBottomCorner) {
		if (zTopCorner == null) throw new IllegalArgumentException("object is null");
		if (zLeft == null) throw new IllegalArgumentException("object is null");
		if (zBottomCorner == null) throw new IllegalArgumentException("object is null");

		final int count = m_xl.size();
		final int last = count - 1;
		final int lhsWidth = Math.max(zLeft.length(), Math.max(zTopCorner.length(), zBottomCorner.length()));
		final int rhsWidth = width();
		final int neoWidth = lhsWidth + rhsWidth;
		for (int i = 0; i < count; i++) {
			final String lhs = i == 0 ? zTopCorner : (i == last ? zBottomCorner : zLeft);
			final StringBuilder brhs = m_xl.get(i);
			final int rightPad = lhsWidth - lhs.length();
			leftPad(brhs, rightPad, ' ');
			brhs.insert(0, lhs);
		}

		m_lzyWidth = new Integer(neoWidth);
	}

	public void borderRight(String zTopCorner, String zRight, String zBottomCorner) {
		if (zTopCorner == null) throw new IllegalArgumentException("object is null");
		if (zRight == null) throw new IllegalArgumentException("object is null");
		if (zBottomCorner == null) throw new IllegalArgumentException("object is null");

		final int count = m_xl.size();
		final int last = count - 1;
		final int lhsWidth = width();
		final int rhsWidth = Math.max(zRight.length(), Math.max(zTopCorner.length(), zBottomCorner.length()));
		final int neoWidth = lhsWidth + rhsWidth;
		for (int i = 0; i < count; i++) {
			final StringBuilder blhs = m_xl.get(i);
			final String rhs = i == 0 ? zTopCorner : (i == last ? zBottomCorner : zRight);
			final int rightPad = lhsWidth - blhs.length();
			rightPad(blhs, rightPad, ' ');
			blhs.append(rhs);
		}

		m_lzyWidth = new Integer(neoWidth);
	}

	public void centreAlign(int minWidth) {
		final int width = Math.max(minWidth, width());
		final int srcCount = m_xl.size();
		for (int isrc = 0; isrc < srcCount; isrc++) {
			final StringBuilder b = m_xl.get(isrc);
			final int exWidth = b.length();
			if (exWidth > 0 && exWidth < width) {
				leftPad(b, (width - exWidth) / 2, ' ');
				m_lzyWidth = null;
			}
		}
	}

	public void leftAlign() {
		final int srcCount = m_xl.size();
		for (int isrc = 0; isrc < srcCount; isrc++) {
			final StringBuilder b = m_xl.get(isrc);
			if (leftTrim(b)) {
				m_lzyWidth = null;
			}
		}
	}

	public ArgonTextVector newCopy() {
		final ArrayList<StringBuilder> xlNeo = new ArrayList<StringBuilder>(m_xl.size());
		final int srcCount = m_xl.size();
		for (int isrc = 0; isrc < srcCount; isrc++) {
			final StringBuilder bsrc = m_xl.get(isrc);
			xlNeo.add(new StringBuilder(bsrc));
		}
		return new ArgonTextVector(xlNeo);
	}

	public ArgonTextVector newWrapped(int wrapWidth) {
		if (wrapWidth <= 0) return this;
		if (width() <= wrapWidth) return this;

		final ArrayList<StringBuilder> xlWrapped = new ArrayList<StringBuilder>(m_xl.size());
		final int srcCount = m_xl.size();
		for (int isrc = 0; isrc < srcCount; isrc++) {
			final StringBuilder b = m_xl.get(isrc);
			if (b.length() <= wrapWidth) {
				xlWrapped.add(b);
			} else {
				final List<String> xlqWords = xlqWords(b.toString());
				final int wordCount = xlqWords.size();
				StringBuilder bseg = new StringBuilder(wrapWidth);
				for (int iword = 0; iword < wordCount; iword++) {
					final String sword = xlqWords.get(iword);
					final int wordLength = sword.length();
					final int exSegLength = bseg.length();
					final int neoSegLength = (exSegLength == 0) ? wordLength : exSegLength + 1 + wordLength;
					if (neoSegLength <= wrapWidth) {
						if (exSegLength > 0) {
							bseg.append(' ');
						}
						bseg.append(sword);
					} else {
						xlWrapped.add(bseg);
						bseg = new StringBuilder(wrapWidth);
						String rem = sword;
						while (rem.length() > wrapWidth) {
							bseg.append(rem.substring(0, wrapWidth));
							xlWrapped.add(bseg);
							bseg = new StringBuilder(wrapWidth);
							rem = rem.substring(wrapWidth);
						}
						bseg.append(rem);
					}
				}
				if (bseg.length() > 0) {
					xlWrapped.add(bseg);
				}
			}
		}

		return new ArgonTextVector(xlWrapped);
	}

	public void padAbove(int count) {
		if (count == 0) return;
		final List<StringBuilder> ex = new ArrayList<StringBuilder>(m_xl);
		m_xl.clear();
		for (int i = 0; i < count; i++) {
			m_xl.add(new StringBuilder());
		}
		m_xl.addAll(ex);
	}

	public void padBelow(int count) {
		if (count == 0) return;
		for (int i = 0; i < count; i++) {
			m_xl.add(new StringBuilder());
		}
	}

	public void padLeft(int count) {
		if (count == 0) return;
		final int rhsCount = m_xl.size();
		final int rhsWidth = width();
		final int neoWidth = count + rhsWidth;
		for (int i = 0; i < rhsCount; i++) {
			final StringBuilder brhs = m_xl.get(i);
			final int crhs = brhs.length();
			if (crhs > 0) {
				leftPad(brhs, count, ' ');
			}
		}

		m_lzyWidth = new Integer(neoWidth);
	}

	public void padRight(int count) {
		if (count == 0) return;
		final int lhsCount = m_xl.size();
		final int lhsWidth = width();
		final int neoWidth = lhsWidth + count;
		for (int i = 0; i < lhsCount; i++) {
			final StringBuilder blhs = m_xl.get(i);
			final int clhs = blhs.length();
			if (clhs > 0 && clhs == lhsWidth) {
				rightPad(blhs, count, ' ');
			}
		}

		m_lzyWidth = new Integer(neoWidth);
	}

	public void rightAlign(int minWidth) {
		final int width = Math.max(minWidth, width());
		final int srcCount = m_xl.size();
		for (int isrc = 0; isrc < srcCount; isrc++) {
			final StringBuilder b = m_xl.get(isrc);
			final int exWidth = b.length();
			if (exWidth > 0 && exWidth < width) {
				leftPad(b, width - exWidth, ' ');
				m_lzyWidth = null;
			}
		}
	}

	@Override
	public String toString() {
		return zFormat("\n");
	}

	public int width() {
		if (m_lzyWidth == null) {
			m_lzyWidth = new Integer(hardWidth());
		}
		return m_lzyWidth.intValue();
	}

	public String zFormat(String zLineFeed) {
		assert zLineFeed != null;
		final StringBuilder b = new StringBuilder();
		final int last = m_xl.size() - 1;
		for (int i = 0; i < last; i++) {
			b.append(m_xl.get(i));
			b.append(zLineFeed);
		}
		b.append(m_xl.get(last));
		return b.toString();
	}

	private static void leftPad(StringBuilder b, int count, char ch) {
		assert b != null;
		if (count <= 0) return;

		final char[] pad = new char[count];
		for (int i = 0; i < count; i++) {
			pad[i] = ch;
		}
		b.insert(0, pad);
	}

	private static boolean leftTrim(StringBuilder b) {
		assert b != null;
		final int len = b.length();
		int blackStart = 0;
		for (int i = 0; i < len && Character.isWhitespace(b.charAt(i)); i++) {
			blackStart++;
		}
		if (blackStart > 0) {
			b.delete(0, blackStart);
			return true;
		}

		return false;
	}

	private static void rightPad(StringBuilder b, int count, char ch) {
		assert b != null;
		for (int i = 0; i < count; i++) {
			b.append(ch);
		}
	}

	private static List<String> xlqWords(String q) {
		assert q != null && q.length() > 0;

		final int lenq = q.length();
		if (lenq == 0) return Collections.emptyList();

		final List<String> xlWords = new ArrayList<String>(16);
		final StringBuilder bword = new StringBuilder();
		for (int i = 0; i < lenq; i++) {
			final char ch = q.charAt(i);
			if (ch == ' ') {
				if (bword.length() > 0) {
					xlWords.add(bword.toString());
					bword.setLength(0);
				}
			} else {
				bword.append(ch);
			}
		}
		if (bword.length() > 0) {
			xlWords.add(bword.toString());
		}

		return xlWords;
	}

	private ArgonTextVector(ArrayList<StringBuilder> xl) {
		assert xl != null;
		m_xl = xl;
	}

	public ArgonTextVector() {
		m_xl = new ArrayList<StringBuilder>(8);
		m_xl.add(new StringBuilder());
	}

	private final ArrayList<StringBuilder> m_xl;

	private Integer m_lzyWidth;
}
