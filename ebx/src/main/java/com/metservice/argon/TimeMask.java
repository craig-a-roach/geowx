/*
 * Copyright 2009 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * @author roach
 */
public class TimeMask {

	public static final TimeMask Default = new TimeMask(new Object[] { TimeMaskToken.YEAR, "-", TimeMaskToken.MONC, "-",
			TimeMaskToken.DOMZ, " (", TimeMaskToken.DOWC, ") ", TimeMaskToken.H24Z, ":", TimeMaskToken.MINZ, ":",
			TimeMaskToken.SECZ, ".", TimeMaskToken.MILLISECZ, " ", TimeMaskToken.GMTHM });

	public String format(TimeFactors timeFactors) {
		if (timeFactors == null) throw new IllegalArgumentException("object is null");
		final StringBuilder sb = new StringBuilder(32);
		final TimeContext tc = new TimeContext(timeFactors);
		format(tc, sb, m_zptParts);
		return sb.toString();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		report(sb, m_zptParts);
		return sb.toString();
	}

	private static List<Object> addPart(List<Object> zlMain, boolean inGroup, List<Object> ozlGroup, Object part) {
		assert zlMain != null;
		assert part != null;

		if (inGroup) {
			final List<Object> xlGroup;
			if (ozlGroup == null) {
				xlGroup = new ArrayList<Object>();
			} else {
				xlGroup = ozlGroup;
			}
			xlGroup.add(part);
			return xlGroup;
		}

		zlMain.add(part);
		return ozlGroup;
	}

	private static void applyUsage(TimeMaskToken token, TimeContext tc) {
		switch (token) {
			case USAGE_UNTIL:
				tc.enableUsageUntil();
			break;
			case USAGE_AT:
				tc.enableUsageAt();
			break;
			default:
				throw new UnsupportedOperationException("Usage token " + token + " not yet implemented");
		}
	}

	private static void consumeLiteral(StringBuilder sb, String literal) {
		sb.append(literal);
	}

	private static void consumeToken(StringBuilder sb, TimeMaskToken token, TimeContext tc) {
		final TimeFactors tf = tc.active();
		switch (token) {
			case AMPM:
				sb.append(tf.isAM() ? "am" : "pm");
			break;

			case DOM:
				sb.append(tf.dom1);
			break;

			case DOMTH:
				sb.append(UArgon.nth(tf.dom1, 1));
			break;

			case DOMZ:
				sb.append(UArgon.intToDec2(tf.dom1));
			break;

			case DOWC:
				sb.append(tf.dowCode());
			break;

			case DOWN:
				sb.append(tf.dowName());
			break;

			case GMTHM:
				sb.append(fieldGMTHM(tf));
			break;

			case H12:
				sb.append(tf.hour12());
			break;

			case H12Z:
				sb.append(UArgon.intToDec2(tf.hour12()));
			break;

			case H24:
				sb.append(tf.hour24);
			break;

			case H24Z:
				sb.append(UArgon.intToDec2(tf.hour24));
			break;

			case MILLISECZ:
				sb.append(UArgon.intToDec3(tf.millisecond));
			break;

			case MIN:
				sb.append(tf.minute);
			break;

			case MINZ:
				sb.append(UArgon.intToDec2(tf.minute));
			break;

			case MON:
				sb.append(tf.moyJan0 + 1);
			break;

			case MONC:
				sb.append(UArgon.MoyCodeJan0[tf.moyJan0]);
			break;

			case MONN:
				sb.append(UArgon.MoyNameJan0[tf.moyJan0]);
			break;

			case MONZ:
				sb.append(UArgon.intToDec2(tf.moyJan0 + 1));
			break;

			case SEC:
				sb.append(tf.second);
			break;

			case SECZ:
				sb.append(UArgon.intToDec2(tf.second));
			break;

			case TZC:
				sb.append(fieldTZC(tf));
			break;

			case TZN:
				sb.append(fieldTZN(tf));
			break;

			case TZRID:
				sb.append(fieldTZRID(tf));
			break;

			case YEAR:
				sb.append(UArgon.intToDec4(tf.year));
			break;

			case YOCZ:
				sb.append(UArgon.intToDec2(tf.year % 100));
			break;

			default:
				throw new UnsupportedOperationException("Token " + token + " not yet implemented");
		}
	}

	private static String err(String reason, int i, String zSpec) {
		final StringBuilder sb = new StringBuilder();
		sb.append(reason);
		sb.append("; at position ").append(i).append(" in '").append(zSpec).append("'");
		return sb.toString();
	}

	private static int evaluateConditional(TimeMaskToken token, TimeContext tc) {
		final TimeFactors tf = tc.active();
		final boolean isUsageUntil = tc.isUsageUntil();
		switch (token) {
			case IS_DST: {
				return tf.isDaylightSavingInEffect() ? 1 : 2;
			}

			case IS_NOON: {
				final boolean isNoon = isUsageUntil ? tf.isNoonUsageUntil() : tf.isNoonUsageAt();
				return isNoon ? 1 : 2;
			}

			case CASE_AM_NOON: {
				final boolean isNoon = isUsageUntil ? tf.isNoonUsageUntil() : tf.isNoonUsageAt();
				if (isNoon) return 2;
				final boolean isAM = tf.isAM();
				if (isAM) return 1;
				return 3;
			}

			case CASE_NOON_END: {
				final boolean isNoon = isUsageUntil ? tf.isNoonUsageUntil() : tf.isNoonUsageAt();
				if (isNoon) return 1;
				final boolean isMidnight = isUsageUntil && tf.isMidnightUsageUntil();
				if (isMidnight) return 2;
				return 3;
			}

			default:
				throw new UnsupportedOperationException("Conditional token " + token + " not yet implemented");
		}
	}

	private static String fieldGMTHM(TimeFactors tf) {
		assert tf != null;
		final int smins = tf.minsGMTAdjustedOffset;
		final StringBuilder sb = new StringBuilder();
		sb.append("GMT");
		if (smins != 0) {
			final int hm;
			if (smins < 0) {
				sb.append('-');
				hm = -smins;
			} else {
				sb.append('+');
				hm = smins;
			}
			final int h = hm / 60;
			final int m = hm - (h * 60);
			sb.append(UArgon.intToDec2(h));
			sb.append(':');
			sb.append(UArgon.intToDec2(m));
		}
		return sb.toString();
	}

	private static String fieldTZC(TimeFactors tf) {
		final boolean daylight = tf.isDaylightSavingInEffect();
		return tf.timeZone.getDisplayName(daylight, TimeZone.SHORT).toUpperCase();
	}

	private static String fieldTZN(TimeFactors tf) {
		final boolean daylight = tf.isDaylightSavingInEffect();
		return tf.timeZone.getDisplayName(daylight, TimeZone.LONG);
	}

	private static String fieldTZRID(TimeFactors tf) {
		return tf.timeZone.getID();
	}

	private static void format(TimeContext tc, StringBuilder sb, Object[] zptParts) {
		int p = 0;
		while (p < zptParts.length) {
			p = formatPart(tc, sb, zptParts, p);
		}
	}

	private static int formatPart(TimeContext tc, StringBuilder sb, Object[] zptParts, int pindex) {
		if (pindex >= zptParts.length) return pindex;

		final Object part = zptParts[pindex];

		if (part instanceof String) {
			consumeLiteral(sb, part.toString());
			return pindex + 1;
		}

		if (part instanceof TimeMaskToken) {
			final TimeMaskToken maskToken = (TimeMaskToken) part;

			if (maskToken.operands > 1) {
				final int result = evaluateConditional(maskToken, tc);
				formatPart(tc, sb, zptParts, pindex + result);
				return pindex + 1 + maskToken.operands;
			}

			if (maskToken.usage) {
				applyUsage(maskToken, tc);
				return pindex + 1;
			}

			consumeToken(sb, maskToken, tc);
			return pindex + 1;
		}

		if (part instanceof Object[]) {
			final Object[] zptSub = (Object[]) part;
			format(tc, sb, zptSub);
			return pindex + 1;
		}

		final String m = "Part " + part.getClass() + " at " + pindex + " not yet implemented";
		throw new UnsupportedOperationException(m);
	}

	private static void report(StringBuilder sb, Object[] zptParts) {
		for (int i = 0; i < zptParts.length; i++) {
			final Object part = zptParts[i];
			if (part instanceof TimeMaskToken) {
				sb.append('[').append(part).append(']');
			} else if (part instanceof String) {
				sb.append(part);
			} else if (part instanceof Object[]) {
				final Object[] zptGroup = (Object[]) part;
				sb.append('<');
				report(sb, zptGroup);
				sb.append('>');
			}
		}
	}

	private static List<Object> save(List<Object> zlMain, boolean inGroup, List<Object> ozlGroup, Object part) {
		if (part instanceof StringBuilder) {
			final StringBuilder bpart = (StringBuilder) part;
			if (bpart.length() == 0) return ozlGroup;
			final String qLiteral = bpart.toString();
			bpart.setLength(0);
			return addPart(zlMain, inGroup, ozlGroup, qLiteral);
		}

		if (part instanceof TimeMaskToken) {
			final TimeMaskToken tpart = (TimeMaskToken) part;
			return addPart(zlMain, inGroup, ozlGroup, tpart);
		}

		throw new IllegalArgumentException("invalid part>" + part + "<");
	}

	public static TimeMask newInstance(Object... zptParts)
			throws ArgonApiException {
		if (zptParts == null) throw new IllegalArgumentException("object is null");
		return new TimeMask(zptParts);
	}

	public static TimeMask newInstance(String zSpec)
			throws ArgonApiException {
		if (zSpec == null) throw new IllegalArgumentException("object is null");

		final List<Object> zlMain = new ArrayList<Object>();
		List<Object> ozlGroup = null;
		final StringBuilder bpart = new StringBuilder();
		boolean inToken = false;
		boolean inGroup = false;
		final int len = zSpec.length();
		for (int i = 0; i <= len; i++) {
			final char ch = i < len ? zSpec.charAt(i) : '\u0000';
			switch (ch) {
				case '[': {
					if (inToken) {
						if (bpart.length() > 0) {
							final String m = err("Invalid token character '['", i, zSpec);
							throw new ArgonApiException(m);
						}
						zlMain.add("[");
						inToken = false;
					} else {
						ozlGroup = save(zlMain, inGroup, ozlGroup, bpart);
						inToken = true;
					}
				}
				break;

				case ']': {
					if (inToken) {
						if (bpart.length() > 0) {
							final String qtwPart = bpart.toString().trim().replace(' ', '_');
							bpart.setLength(0);
							final char ch0 = qtwPart.charAt(0);
							final String zToken;
							final boolean expectConditional;
							final boolean expectUsage;
							if (ch0 == '?') {
								zToken = qtwPart.substring(1);
								expectConditional = true;
								expectUsage = false;
							} else {
								if (qtwPart.indexOf('=') > 0) {
									zToken = qtwPart.replace('=', '_');
									expectUsage = true;
								} else {
									zToken = qtwPart;
									expectUsage = false;
								}
								expectConditional = false;
							}
							if (zToken.length() == 0) {
								final String m = err("Malformed token '" + qtwPart + "'", i, zSpec);
								throw new ArgonApiException(m);
							}
							final TimeMaskToken oToken = TimeMaskToken.Table.find(zToken);
							if (oToken == null) {
								final String m = err("Unsupported token '" + qtwPart + "'", i, zSpec);
								throw new ArgonApiException(m);
							}
							final boolean isConditionalToken = oToken.operands > 1;
							if (isConditionalToken != expectConditional) {
								final String m = err("Conditional token mismatch '" + qtwPart + "'", i, zSpec);
								throw new ArgonApiException(m);
							}
							if (oToken.usage != expectUsage) {
								final String m = err("Usage token mismatch '" + qtwPart + "'", i, zSpec);
								throw new ArgonApiException(m);
							}
							ozlGroup = save(zlMain, inGroup, ozlGroup, oToken);
						}
						inToken = false;
					} else {
						bpart.append("]");
					}
				}
				break;

				case '<': {
					if (inGroup) {
						if (bpart.length() > 0) {
							final String m = err("Cannot nest groups", i, zSpec);
							throw new ArgonApiException(m);
						}
						zlMain.add("<");
						inGroup = false;
					} else {
						inGroup = true;
					}
				}
				break;

				case '>': {
					if (inGroup) {
						ozlGroup = save(zlMain, inGroup, ozlGroup, bpart);
						if (ozlGroup != null) {
							zlMain.add(ozlGroup.toArray());
							ozlGroup = null;
						}
						inGroup = false;
					} else {
						zlMain.add(">");
					}
				}
				break;

				case '\u0000': {
					if (inToken) throw new ArgonApiException(err("Token is missing ']' character", i, zSpec));
					if (inGroup) throw new ArgonApiException(err("Group is missing '>' character", i, zSpec));
					ozlGroup = save(zlMain, inGroup, ozlGroup, bpart);
				}
				break;

				default: {
					bpart.append(ch);
				}
			}// switch
		}// for

		return new TimeMask(zlMain.toArray());
	}

	private TimeMask(Object[] zptParts) {
		assert zptParts != null;
		m_zptParts = zptParts;
	}

	private final Object[] m_zptParts;

	private static class TimeContext {

		public TimeFactors active() {
			return oUsageUntil == null ? usageAt : oUsageUntil;
		}

		public void enableUsageAt() {
			oUsageUntil = null;
		}

		public void enableUsageUntil() {
			if (oUsageUntil == null) {
				oUsageUntil = usageAt.newUsageUntil();
			}
		}

		public boolean isUsageUntil() {
			return oUsageUntil != null;
		}

		public TimeContext(TimeFactors base) {
			this.usageAt = base;
		}

		final TimeFactors usageAt;
		TimeFactors oUsageUntil;
	}

}
