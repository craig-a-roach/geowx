/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.cobalt;

import com.metservice.argon.ArgonText;
import com.metservice.argon.text.ArgonNumber;

/**
 * @author roach
 */
class CobaltKmlColor {

	private static final char Wild = '?';

	private String hex2(int nvalue) {
		return ArgonNumber.intToHex(nvalue, 2);
	}

	public String format() {
		return format(0, 1);
	}

	public String format(int index, int count) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < m_abgr.length; i++) {
			final Color oColor = m_abgr[i];
			final int ncolor = oColor == null ? ncolor(index, count) : oColor.nvalue;
			sb.append(hex2(ncolor));
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return format();
	}

	private static String canonSpec(String zSpec) {
		final String zuctwSpec = zSpec.trim().toUpperCase();
		final int len = zuctwSpec.length();
		final StringBuilder bhex = new StringBuilder(8);
		for (int i = 0; i < len; i++) {
			final char ch = zuctwSpec.charAt(i);
			if (ch == Wild || ArgonText.isHexDigit(ch)) {
				bhex.append(ch);
			}
		}
		final int rpad = 8 - bhex.length();
		for (int i = 0; i < rpad; i++) {
			bhex.append(Wild);
		}
		return bhex.toString();
	}

	private static int ncolor(int index, int count) {
		if (index == 0) return 0;
		final int countCanon = Math.max(index + 1, count) % 256;
		final int indexCanon = Math.max(0, index) % 256;
		return Math.min(255, indexCanon * 256 / countCanon);
	}

	public static CobaltKmlColor newABGR(String zSpec) {
		if (zSpec == null) throw new IllegalArgumentException("object is null");
		final String spec8 = canonSpec(zSpec);
		final Color[] argb = new Color[4];
		for (int i = 0, channel = 0; i < 8; i += 2, channel++) {
			final String spec2 = spec8.substring(i, i + 2);
			if (spec2.charAt(0) != Wild && spec2.charAt(1) != Wild) {
				try {
					final int channelValue = Integer.parseInt(spec2, 16);
					argb[channel] = new Color(channelValue);
				} catch (final NumberFormatException ex) {
				}
			}
		}
		return new CobaltKmlColor(argb);
	}

	private CobaltKmlColor(Color[] argb) {
		m_abgr = argb;
	}

	private final Color[] m_abgr;

	private static class Color {

		Color(int value) {
			this.nvalue = value % 256;
		}
		final int nvalue;
	}
}
