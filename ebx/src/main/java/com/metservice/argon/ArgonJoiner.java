/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.util.List;

/**
 * @author roach
 */
public class ArgonJoiner {

	private static final String[] ZSTRING = new String[0];

	public static String zComma(List<?> ozl) {
		if (ozl == null) return "";
		final int count = ozl.size();
		if (count == 0) return "";
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < count; i++) {
			final Object ovalue = ozl.get(i);
			if (ovalue != null) {
				final String ztw = ovalue.toString().trim();
				if (ztw.length() > 0) {
					if (sb.length() > 0) {
						sb.append(", ");
					}
					sb.append(ztw);
				}
			}
		}
		return sb.toString();
	}

	public static String zComma(Object[] ozpt) {
		if (ozpt == null || ozpt.length == 0) return "";
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < ozpt.length; i++) {
			final Object ovalue = ozpt[i];
			if (ovalue != null) {
				final String ztw = ovalue.toString().trim();
				if (ztw.length() > 0) {
					if (sb.length() > 0) {
						sb.append(", ");
					}
					sb.append(ztw);
				}
			}
		}
		return sb.toString();
	}

	public static String zCsv(int[] array) {
		if (array == null) throw new IllegalArgumentException("object is null");
		final int len = array.length;
		final StringBuilder sb = new StringBuilder(len * 8);
		if (len > 0) {
			sb.append(array[0]);
		}
		for (int i = 1; i < len; i++) {
			sb.append(',');
			sb.append(array[i]);
		}
		return sb.toString();
	}

	public static String zCsv(String[] zptIn) {
		return zQuotedDelimited(zptIn, ',', true);
	}

	public static String zJoin(List<?> ozl, String ozSeparator) {
		if (ozl == null) return "";
		final int count = ozl.size();
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < count; i++) {
			final Object ovalue = ozl.get(i);
			final String oz = ovalue == null ? null : ovalue.toString();
			final String ztw = oz == null ? "" : oz.trim();
			if (i > 0 && ozSeparator != null) {
				sb.append(ozSeparator);
			}
			sb.append(ztw);
		}
		return sb.toString();
	}

	public static String zJoin(Object[] ozpt, String ozSeparator) {
		if (ozpt == null || ozpt.length == 0) return "";
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < ozpt.length; i++) {
			final Object ovalue = ozpt[i];
			final String oz = ovalue == null ? null : ovalue.toString();
			final String ztw = oz == null ? "" : oz.trim();
			if (i > 0 && ozSeparator != null) {
				sb.append(ozSeparator);
			}
			sb.append(ztw);
		}
		return sb.toString();
	}

	public static String[] zptAppend(String[] ozpt0, String[] ozpt1) {
		final int c0 = ozpt0 == null ? 0 : ozpt0.length;
		final int c1 = ozpt1 == null ? 0 : ozpt1.length;
		final int c = c0 + c1;
		if (c == 0) return ZSTRING;
		final String[] zpt = new String[c];
		int dstPos = 0;
		if (c0 > 0) {
			System.arraycopy(ozpt0, 0, zpt, dstPos, c0);
			dstPos += c0;
		}
		if (c1 > 0) {
			System.arraycopy(ozpt1, 0, zpt, dstPos, c1);
		}
		return zpt;
	}

	public static String[] zptAppend(String[] ozpt0, String[] ozpt1, String[] ozpt2) {
		final int c0 = ozpt0 == null ? 0 : ozpt0.length;
		final int c1 = ozpt1 == null ? 0 : ozpt1.length;
		final int c2 = ozpt2 == null ? 0 : ozpt2.length;
		final int c = c0 + c1 + c2;
		if (c == 0) return ZSTRING;
		final String[] zpt = new String[c];
		int dstPos = 0;
		if (c0 > 0) {
			System.arraycopy(ozpt0, 0, zpt, dstPos, c0);
			dstPos += c0;
		}
		if (c1 > 0) {
			System.arraycopy(ozpt1, 0, zpt, dstPos, c1);
			dstPos += c1;
		}
		if (c2 > 0) {
			System.arraycopy(ozpt2, 0, zpt, dstPos, c2);
		}
		return zpt;
	}

	public static String zQuotedDelimited(String[] zptIn, char delimiter, boolean noControl) {
		if (zptIn == null) throw new IllegalArgumentException("object is null");
		final int inlen = zptIn.length;
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i < inlen; i++) {
			if (i > 0) {
				b.append(delimiter);
			}
			final String ozRaw = zptIn[i];
			if (ozRaw == null) {
				continue;
			}
			final String z = noControl ? ArgonTransformer.zNoControl(ozRaw) : ozRaw;
			final int lenz = z.length();
			if (lenz == 0) {
				continue;
			}
			final String ztw = z.trim();
			final int lenztw = ztw.length();
			final boolean cm = z.indexOf(delimiter) >= 0;
			final boolean ws = lenztw < lenz;
			if (cm || ws) {
				final boolean cdq = z.indexOf('\"') >= 0;
				if (cdq) {
					b.append("\"");
					b.append(z.replace("\"", "\"\""));
					b.append("\"");
				} else {
					b.append("\"");
					b.append(z);
					b.append("\"");
				}
			} else {
				b.append(ztw);
			}
		}
		return b.toString();
	}
}
