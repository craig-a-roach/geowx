/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.edge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author roach
 */
class TestHelpLoader {

	private static final Pattern CommaSeparator = Pattern.compile("[,]");
	private static final long MsToHour = 1000 * 3600;

	private static final Comparator<BzeStrike> StrikesByTime = new Comparator<BzeStrike>() {

		@Override
		public int compare(BzeStrike lhs, BzeStrike rhs) {
			if (lhs.t < rhs.t) return -1;
			if (lhs.t > rhs.t) return +1;
			return 0;
		}
	};

	private static float fieldFloat(String in, int lineno, String tag) {
		try {
			return Float.parseFloat(qtw(in, lineno, tag));
		} catch (final NumberFormatException ex) {
			throw new IllegalArgumentException("Malformed " + tag + " at line #" + lineno + "..." + in);
		}
	}

	private static long fieldLong(String in, int lineno, String tag) {
		try {
			return Long.parseLong(qtw(in, lineno, tag));
		} catch (final NumberFormatException ex) {
			throw new IllegalArgumentException("Malformed " + tag + " at line #" + lineno + "..." + in);
		}
	}

	private static BzeStrikeType fieldType(String in, int lineno, String tag) {
		final String quctw = qtw(in, lineno, tag).toUpperCase();
		if (quctw.equals("GROUND")) return BzeStrikeType.GROUND;
		if (quctw.equals("CLOUD")) return BzeStrikeType.CLOUD;
		throw new IllegalArgumentException("Malformed " + tag + " at line #" + lineno + "..." + in);
	}

	private static String qtw(String in, int lineno, String tag) {
		final String ztw = in.trim();
		if (ztw.length() == 0) throw new IllegalArgumentException("Missing " + tag + " at line #" + lineno);
		return ztw;
	}

	public static BzeStrike createStrike(String src, int lineno) {
		if (src == null) return null;
		final String ztwSrc = src.trim();
		if (ztwSrc.length() == 0) return null;
		final String[] fields = CommaSeparator.split(ztwSrc);
		if (fields.length != 5) return null;
		final long t = fieldLong(fields[0], lineno, "time");
		final float y = fieldFloat(fields[1], lineno, "latitude");
		final float x = fieldFloat(fields[2], lineno, "longitude");
		final float qty = fieldFloat(fields[3], lineno, "qty");
		final BzeStrikeType type = fieldType(fields[4], lineno, "type");
		return new BzeStrike(t, y, x, qty, type);
	}

	public static BzeStrike[] newArrayFromLines(String[] zlines) {
		final List<BzeStrike> zl = newListFromLines(zlines);
		final int sz = zl.size();
		return zl.toArray(new BzeStrike[sz]);
	}

	public static BitMesh newBitMeshFromLines(String[] zlines, char mark) {
		final int height = zlines.length;
		int width = 0;
		for (int i = 0; i < height; i++) {
			width = Math.max(width, zlines[i].length());
		}
		final BitMesh bm = new BitMesh(width, height);
		for (int i = 0, y = height - 1; i < height; i++, y--) {
			final String line = zlines[i];
			final int lineLen = line.length();
			for (int x = 0; x < lineLen; x++) {
				if (line.charAt(x) == mark) {
					bm.set(x, y, true);
				}
			}
		}
		return bm;
	}

	public static List<BzeStrike> newHourFromListByTime(List<BzeStrike> strikesAsc, int startHourOfDay, int hourCount) {
		if (strikesAsc == null) throw new IllegalArgumentException("object is null");
		final int srcCount = strikesAsc.size();
		if (srcCount == 0) return Collections.emptyList();
		final long tRef = strikesAsc.get(0).t;
		final long tStart = tRef + (startHourOfDay * MsToHour);
		final long tEnd = tStart + (hourCount * MsToHour);
		final List<BzeStrike> out = new ArrayList<BzeStrike>(srcCount / 2);
		for (int i = 0; i < srcCount; i++) {
			final BzeStrike s = strikesAsc.get(i);
			if (s.t >= tStart && s.t < tEnd) {
				out.add(s);
			}
		}
		return out;
	}

	public static List<BzeStrike> newListByTimeFromResource(Class<?> ref, String path) {
		return newListByTimeFromResource(ref, path, 0.0f);
	}

	public static List<BzeStrike> newListByTimeFromResource(Class<?> ref, String path, float minMag) {
		final InputStream ins = ref.getResourceAsStream(path);
		if (ins == null) throw new IllegalArgumentException("Resource '" + path + "' not found under " + ref.getName());
		final BufferedReader rdr = new BufferedReader(new InputStreamReader(ins));
		try {
			final List<BzeStrike> strikesAsc = new ArrayList<BzeStrike>();
			int lineNo = 0;
			boolean more = true;
			while (more) {
				final String oLine = rdr.readLine();
				if (oLine == null) {
					more = false;
					continue;
				}
				lineNo++;
				final String ztwLine = oLine.trim();
				if (ztwLine.length() == 0) {
					continue;
				}
				final BzeStrike oStrike = createStrike(ztwLine, lineNo);
				if (oStrike == null) {
					continue;
				}
				if (Math.abs(oStrike.qty) >= minMag) {
					strikesAsc.add(oStrike);
				}
			}
			Collections.sort(strikesAsc, StrikesByTime);
			return strikesAsc;
		} catch (final IOException ex) {
			throw new IllegalArgumentException("Cannot read '" + path + "'..." + ex.getMessage());
		} finally {
			try {
				rdr.close();
			} catch (final IOException ex) {
			}
		}
	}

	public static List<BzeStrike> newListFromGenerator(String spec) {
		final Pattern lineSplitter = Pattern.compile("[|]");
		final Pattern genSplitter = Pattern.compile("[,:]");
		final List<BzeStrike> zl = new ArrayList<>();
		final String[] lines = lineSplitter.split(spec);
		long t = 1000L;
		for (int iline = 0; iline < lines.length; iline++) {
			final String[] gen = genSplitter.split(lines[iline]);
			final String yg = gen[0];
			for (int ic = 1; ic < gen.length; ic++) {
				final String xg = gen[ic];
				final String src = t + "," + yg + "," + xg + ",1,GROUND";
				final BzeStrike strike = createStrike(src, iline);
				zl.add(strike);
				t++;
			}
		}
		return zl;
	}

	public static List<BzeStrike> newListFromLines(String[] zlines) {
		final List<BzeStrike> zl = new ArrayList<>(zlines.length);
		for (int i = 0; i < zlines.length; i++) {
			final String line = zlines[i].trim();
			final BzeStrike oStrike = createStrike(line, (i + 1));
			if (oStrike != null) {
				zl.add(oStrike);
			}
		}
		return zl;
	}

	public static BzeStrike[] toArray(List<BzeStrike> strikes) {
		final int strikeCount = strikes.size();
		return strikes.toArray(new BzeStrike[strikeCount]);
	}

}
