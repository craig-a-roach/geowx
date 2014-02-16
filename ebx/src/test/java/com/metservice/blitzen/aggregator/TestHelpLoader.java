/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.aggregator;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author roach
 */
class TestHelpLoader {

	private static final Pattern CommaSeparator = Pattern.compile("[,]");

	private static int fieldCat(String in, int lineno, String tag) {
		final String quctw = qtw(in, lineno, tag).toUpperCase();
		if (quctw.equals("GROUND")) return 1;
		if (quctw.equals("CLOUD")) return 2;
		return 0;
	}

	private static float fieldFloat(String in, int lineno, String tag) {
		try {
			return Float.parseFloat(qtw(in, lineno, tag));
		} catch (final NumberFormatException ex) {
			throw new IllegalArgumentException("Malformed " + tag + " at line #" + lineno);
		}
	}

	private static long fieldLong(String in, int lineno, String tag) {
		try {
			return Long.parseLong(qtw(in, lineno, tag));
		} catch (final NumberFormatException ex) {
			throw new IllegalArgumentException("Malformed " + tag + " at line #" + lineno);
		}
	}

	private static String qtw(String in, int lineno, String tag) {
		final String ztw = in.trim();
		if (ztw.length() == 0) throw new IllegalArgumentException("Missing " + tag + " at line #" + lineno);
		return ztw;
	}

	public static Strike createStrike(String src, int lineno) {
		if (src == null) return null;
		final String ztwSrc = src.trim();
		if (ztwSrc.length() == 0) return null;
		final String[] fields = CommaSeparator.split(ztwSrc);
		if (fields.length != 5) return null;
		final long t = fieldLong(fields[0], lineno, "time");
		final float y = fieldFloat(fields[1], lineno, "latitude");
		final float x = fieldFloat(fields[2], lineno, "longitude");
		final float qty = fieldFloat(fields[3], lineno, "qty");
		final int cat = fieldCat(fields[4], lineno, "category");
		return new Strike(t, y, x, qty, cat);
	}

	public static Strike[] newArrayFromLines(String[] zlines) {
		final List<Strike> zl = new ArrayList<>(zlines.length);
		for (int i = 0; i < zlines.length; i++) {
			final String line = zlines[i].trim();
			final Strike oStrike = createStrike(line, (i + 1));
			if (oStrike != null) {
				zl.add(oStrike);
			}
		}
		final int sz = zl.size();
		return zl.toArray(new Strike[sz]);
	}

}
