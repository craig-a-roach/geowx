/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

/**
 * @author roach
 */
public class ArgonTransformer {

	public static String base64UrlDecode(String zB64)
			throws ArgonFormatException {
		if (zB64 == null) throw new IllegalArgumentException("object is null");
		final int sl = zB64.length();
		if (sl == 0) return zB64;
		return UArgonB64.newURLFromB64UTF8(zB64);
	}

	public static String base64UrlEncode(String zURL)
			throws ArgonFormatException {
		if (zURL == null) throw new IllegalArgumentException("object is null");
		final int sl = zURL.length();
		if (sl == 0) return zURL;
		return UArgonB64.newB64UTF8FromURL(zURL);
	}

	public static Properties newProperties(String ozSpec) {
		final Properties p = new Properties();
		if (ozSpec != null && ozSpec.length() > 0) {
			final StringReader sr = new StringReader(ozSpec);
			try {
				p.load(sr);
			} catch (final IOException ex) {
				throw new IllegalStateException(ex);
			} finally {
				sr.close();
			}
		}
		return p;
	}

	public static Properties newPropertiesFromAssignments(String[] zptAssignments) {
		final StringBuilder bprops = new StringBuilder();
		for (int i = 0; i < zptAssignments.length; i++) {
			final String qtwAssign = zptAssignments[i];
			bprops.append(qtwAssign);
			bprops.append('\n');
		}
		return newProperties(bprops.toString());
	}

	public static String zHtmlEncode(String zIn, boolean isAmpAlreadyEscaped, String ozSPExpansion, String ozLFExpansion,
			String ozTABExpansion, String ozAPOSExpansion, String ozQUOTExpansion) {
		if (zIn == null) throw new IllegalArgumentException("object is null");
		final int sl = zIn.length();
		final String zLFExpansion = ozLFExpansion == null ? "<br>" : ozLFExpansion;
		final String zTABExpansion = ozTABExpansion == null ? "" : ozTABExpansion;
		final String zAPOSExpansion = ozAPOSExpansion == null ? "&apos;" : ozAPOSExpansion;
		final String zQUOTExpansion = ozQUOTExpansion == null ? "&quot;" : ozQUOTExpansion;

		if (sl == 0) return zIn;

		final StringBuilder b = new StringBuilder(sl + 32);
		boolean preBlack = true;
		for (int i = 0; i < sl; i++) {
			final char ch = zIn.charAt(i);
			boolean black = true;
			if (ch == '<') {
				b.append("&lt;");
			} else if (ch == '>') {
				b.append("&gt;");
			} else if (ch == '\'') {
				b.append(zAPOSExpansion);
			} else if (ch == '\"') {
				b.append(zQUOTExpansion);
			} else if (ch == '&') {
				b.append(isAmpAlreadyEscaped ? "&" : "&amp;");
			} else if (ch == ' ') {
				black = false;
				if (ozSPExpansion == null) {
					if (preBlack) {
						b.append(' ');
					}
				} else {
					b.append(ozSPExpansion);
				}
			} else {
				final int ic = ch;
				if (ic < 32) {
					black = false;
					if (ic == 9) {
						b.append(zTABExpansion);
					} else if (ic == 10) {
						b.append(zLFExpansion);
					}
				} else if (ic > 126) {
					b.append("&#");
					b.append(ic);
					b.append(';');
				} else {
					b.append(ch);
				}
			}
			preBlack = black;
		}
		return b.toString();
	}

	public static String zHtmlEncodeATTVAL(String zIn) {
		return zHtmlEncode(zIn, false, " ", "&#10;", "&#9;", "&apos;", "&quot;");
	}

	public static String zHtmlEncodePCDATA(String zIn) {
		return zHtmlEncode(zIn, false, " ", "<br>", "&nbsp;", "\'", "\"");
	}

	public static String zHtmlEncodePCDATA(String zIn, boolean nowrap) {
		return zHtmlEncode(zIn, false, nowrap ? "&nbsp;" : " ", "<br>", "&nbsp;", "\'", "\"");
	}

	public static String zLeftContinued(String zIn, int limit, String ozCont) {
		if (zIn == null) throw new IllegalArgumentException("object is null");
		final int len = zIn.length();
		if (len <= limit) return zIn;
		final String zCont = ozCont == null ? "" : ozCont;
		final int lenCont = zCont.length();
		if (lenCont == 0) return zIn.substring(0, limit);
		if (lenCont >= limit) return zCont.substring(0, limit);
		final int end = limit - lenCont;
		return zIn.substring(0, end) + zCont;
	}

	public static String zLeftDot3(String zIn, int limit) {
		return zLeftContinued(zIn, limit, "...");
	}

	public static String zNoControl(String zIn) {
		return zNoControl(zIn, false, false);
	}

	public static String zNoControl(String zIn, boolean allowLF, boolean allowTAB) {
		if (zIn == null) throw new IllegalArgumentException("object is null");
		final int sl = zIn.length();
		if (sl == 0) return zIn;

		StringBuilder lzyB = null;
		for (int i = 0; i < sl; i++) {
			final char ch = zIn.charAt(i);
			boolean reject = ArgonText.isIsoControl(ch);
			if (reject) {
				if ((allowLF && ch == '\n') || (allowTAB && ch == '\t')) {
					reject = false;
				}
			}
			if (reject) {
				if (lzyB == null) {
					lzyB = new StringBuilder(sl);
					lzyB.append(zIn.substring(0, i));
				}
			} else {
				if (lzyB != null) {
					lzyB.append(ch);
				}
			}
		}
		return lzyB == null ? zIn : lzyB.toString();
	}

	public static String zPosixSanitized(String zIn) {
		if (zIn == null) throw new IllegalArgumentException("object is null");
		final int sl = zIn.length();
		if (sl == 0) return zIn;
		StringBuilder lzyB = null;
		for (int i = 0; i < sl; i++) {
			final boolean lead = i == 0;
			final char ch = zIn.charAt(i);
			final boolean unsafe = !ArgonText.isPosixName(ch, lead);
			if (unsafe) {
				if (lzyB == null) {
					lzyB = new StringBuilder(sl);
					lzyB.append(zIn.substring(0, i));
				}
				lzyB.append('_');
			} else {
				if (lzyB != null) {
					lzyB.append(ch);
				}
			}
		}
		return lzyB == null ? zIn : lzyB.toString();
	}

	public static String zSanitized(String zIn, String qUnsafe, String zSafe) {
		if (zIn == null) throw new IllegalArgumentException("object is null");
		if (qUnsafe == null || qUnsafe.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (zSafe == null) throw new IllegalArgumentException("object is null");
		final int sl = zIn.length();
		if (sl == 0) return zIn;

		StringBuilder lzyB = null;
		for (int i = 0; i < sl; i++) {
			final char ch = zIn.charAt(i);
			final boolean unsafe = qUnsafe.indexOf(ch) >= 0;
			if (unsafe) {
				if (lzyB == null) {
					lzyB = new StringBuilder(sl);
					lzyB.append(zIn.substring(0, i));
				}
				lzyB.append(zSafe);
			} else {
				if (lzyB != null) {
					lzyB.append(ch);
				}
			}
		}
		return lzyB == null ? zIn : lzyB.toString();
	}

	public static String zXmlDecode(String zIn) {
		if (zIn == null) throw new IllegalArgumentException("object is null");
		final int sl = zIn.length();
		if (sl == 0) return zIn;

		final StringBuilder b = new StringBuilder(sl + 32);
		final StringBuilder eb = new StringBuilder();
		boolean expanding = false;
		for (int i = 0; i < sl; i++) {
			final char ch = zIn.charAt(i);
			switch (ch) {
				case '&': {
					expanding = true;
				}
				break;
				case ';': {
					if (expanding) {
						final String zen = eb.toString();
						eb.setLength(0);
						expanding = false;
						if (zen.equals("lt")) {
							b.append('<');
						} else if (zen.equals("gt")) {
							b.append('>');
						} else if (zen.equals("amp")) {
							b.append('&');
						} else if (zen.equals("apos")) {
							b.append('\'');
						} else if (zen.equals("quot")) {
							b.append('\"');
						} else if (zen.startsWith("#")) {
							final int zl = zen.length();
							if (zl >= 2 && zl <= 6) {
								final boolean hex = (zl >= 3) && (zen.charAt(1) == 'x');
								try {
									final int startIndex = hex ? 2 : 1;
									final int radix = hex ? 16 : 10;
									b.append((char) Integer.parseInt(zen.substring(startIndex), radix));
								} catch (final NumberFormatException exNF) {
								}
							}
						}
					} else {
						b.append(ch);
					}
				}
				break;
				default: {
					if (expanding) {
						eb.append(ch);
					} else {
						b.append(ch);
					}
				}
			}
		}
		return b.toString();
	}

	public static String zXmlEncode(String zIn, boolean isAmpAlreadyEscaped, boolean escapeTABCRLF, boolean escapeAPOS,
			boolean escapeQUOT) {
		if (zIn == null) throw new IllegalArgumentException("object is null");
		final int sl = zIn.length();
		if (sl == 0) return zIn;

		final StringBuilder b = new StringBuilder(sl + 32);
		for (int i = 0; i < sl; i++) {
			final char ch = zIn.charAt(i);
			if (ch == '<') {
				b.append("&lt;");
			} else if (ch == '>') {
				b.append("&gt;");
			} else if (ch == '\'') {
				b.append(escapeAPOS ? "&apos;" : ch);
			} else if (ch == '\"') {
				b.append(escapeQUOT ? "&quot;" : ch);
			} else if (ch == '&') {
				b.append(isAmpAlreadyEscaped ? "&" : "&amp;");
			} else {
				final int ic = ch;
				final boolean noEntity = (ic >= 32 && ic <= 126) || (!escapeTABCRLF && (ic == 9 || ic == 10 || ic == 13));
				if (noEntity) {
					b.append(ch);
				} else {
					b.append("&#");
					b.append(ic);
					b.append(';');
				}
			}
		}
		return b.toString();
	}
}
