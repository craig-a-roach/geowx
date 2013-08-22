/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author roach
 */
public class ArgonSplitter {

	private static final String[] ZSTRING = new String[0];

	private static boolean accept(String zLine, Pattern oInPattern, Pattern oExPattern) {
		if (oInPattern == null || oInPattern.matcher(zLine).matches()) {
			if (oExPattern == null || !oExPattern.matcher(zLine).matches()) return true;
		}
		return false;
	}

	private static String[] newArrayFromStringList(List<String> slist) {
		assert slist != null;
		final int len = slist.size();
		return slist.toArray(new String[len]);
	}

	public static String[] zptqtwSplit(String ozSrc, char delimiter) {
		if (ozSrc == null) return ZSTRING;
		final int srclen = ozSrc.length();
		if (srclen == 0) return ZSTRING;
		final List<String> slist = new ArrayList<String>(16);
		final StringBuilder sb = new StringBuilder(16);
		for (int i = 0; i < srclen; i++) {
			final char ch = ozSrc.charAt(i);
			if (ch == delimiter) {
				if (sb.length() > 0) {
					final String ztw = sb.toString().trim();
					if (ztw.length() > 0) {
						slist.add(ztw);
					}
					sb.setLength(0);
				}
			} else {
				sb.append(ch);
			}
		}
		final String ztw = sb.toString().trim();
		if (ztw.length() > 0) {
			slist.add(ztw);
		}
		return slist.toArray(new String[slist.size()]);
	}

	public static String[] zptqtwSplit(String ozSrc, Pattern delimiter) {
		if (delimiter == null) throw new IllegalArgumentException("object is null");
		if (ozSrc == null) return ZSTRING;
		final int srclen = ozSrc.length();
		if (srclen == 0) return ZSTRING;
		final String[] xpt = delimiter.split(ozSrc);
		final int pcount = xpt.length;
		final List<String> zl = new ArrayList<String>(pcount);
		for (int i = 0; i < pcount; i++) {
			final String ztw = xpt[i].trim();
			if (ztw.length() > 0) {
				zl.add(ztw);
			}
		}
		final int tcount = zl.size();
		if (tcount == pcount) return xpt;
		return zl.toArray(new String[tcount]);
	}

	public static String[] zptzDelimited(String ozCsv, char delimiter) {
		if (ozCsv == null) return ZSTRING;
		final int srclen = ozCsv.length();
		if (srclen == 0) return ZSTRING;

		final List<String> slist = new ArrayList<String>(16);
		final StringBuilder sb = new StringBuilder(16);
		StringBuilder oqb = null;
		char quoteMark = '\"';
		int pos = 0;
		while (pos <= srclen) {
			final boolean eos = pos == srclen;
			final char ch = eos ? '\u0000' : ozCsv.charAt(pos);
			if (eos) {
				if (oqb != null) {
					sb.append(oqb);
				}
				slist.add(sb.toString());
				pos++;
			} else if (ch == delimiter) {
				if (oqb == null) {
					slist.add(sb.toString());
					sb.setLength(0);
				} else {
					oqb.append(ch);
				}
				pos++;
			} else if (ch == '\"' || ch == '\'') {
				if (oqb == null) {
					if (sb.length() == 0) {
						oqb = new StringBuilder();
						quoteMark = ch;
					} else {
						sb.append(ch);
					}
				} else {
					if (ch == quoteMark) {
						final int posNext = pos + 1;
						if (posNext < srclen && ozCsv.charAt(posNext) == quoteMark) {
							oqb.append(quoteMark);
							pos = posNext;
						} else {
							sb.append(oqb);
							oqb = null;
						}
					} else {
						oqb.append(ch);
					}
				}
				pos++;
			} else if (ch == ' ' || ch == '\t') {
				if (oqb == null) {
					if (sb.length() == 0) {
						pos++;
					} else {
						int iblack = pos + 1;
						while (iblack < srclen) {
							final char wch = ozCsv.charAt(iblack);
							if (wch != ' ' && wch != '\t') {
								break;
							}
							iblack++;
						}
						if (iblack < srclen) {
							final char bch = ozCsv.charAt(iblack);
							if (bch != delimiter) {
								sb.append(ozCsv.substring(pos, iblack));
							}
						}
						pos = iblack;
					}
				} else {
					oqb.append(ch);
					pos++;
				}
			} else {
				if (oqb == null) {
					sb.append(ch);
				} else {
					oqb.append(ch);
				}
				pos++;
			}
		}
		return newArrayFromStringList(slist);
	}

	public static String[] zptzLines(String ozLines, boolean trim, boolean retainBlankLines) {
		return zptzLines(ozLines, trim, retainBlankLines, null, null);
	}

	public static String[] zptzLines(String ozLines, boolean trim, boolean retainBlankLines, Pattern oInPattern,
			Pattern oExPattern) {
		if (ozLines == null) return ZSTRING;
		final int srclen = ozLines.length();
		if (srclen == 0) return ZSTRING;

		final boolean unfiltered = oInPattern == null && oExPattern == null;
		final List<String> lineList = new ArrayList<String>(64);
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i < srclen; i++) {
			final char ch = ozLines.charAt(i);
			if (ch == '\n') {
				String zLine = b.toString();
				b.setLength(0);
				if (trim) {
					zLine = zLine.trim();
				}
				if (zLine.length() == 0) {
					if (retainBlankLines) {
						lineList.add("");
					}
				} else {
					if (unfiltered || accept(zLine, oInPattern, oExPattern)) {
						lineList.add(zLine);
					}
				}
			} else {
				if (ch != '\r' && ch != '\f') {
					b.append(ch);
				}
			}
		}
		final String ztwLine = b.toString().trim();
		if (ztwLine.length() > 0) {
			if (unfiltered || accept(ztwLine, oInPattern, oExPattern)) {
				lineList.add(b.toString());
			}
		}
		return newArrayFromStringList(lineList);
	}
}
