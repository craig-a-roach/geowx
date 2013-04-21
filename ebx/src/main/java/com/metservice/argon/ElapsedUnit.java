/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

/**
 * @author roach
 */
public enum ElapsedUnit implements CArgon {
	Days(ELAPSED_UNIT_LDAYS, MS_PER_DAY),
	Hours(ELAPSED_UNIT_LHOURS, MS_PER_HR),
	Minutes(ELAPSED_UNIT_LMINUTES, MS_PER_MIN),
	Seconds(ELAPSED_UNIT_LSECONDS, MS_PER_SEC),
	Milliseconds(ELAPSED_UNIT_LMILLISECONDS, 1);

	public static ElapsedUnit find(String qncName) {
		if (qncName == null || qncName.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final String zlctw = qncName.trim().toLowerCase();
		if (zlctw.length() == 0) return null;
		final char ch0 = zlctw.charAt(0);
		return findLower(ch0);
	}

	public static ElapsedUnit findLower(char lch) {
		switch (lch) {
			case ELAPSED_UNIT_LDAYS:
				return Days;
			case ELAPSED_UNIT_LHOURS:
				return Hours;
			case ELAPSED_UNIT_LMINUTES:
				return Minutes;
			case ELAPSED_UNIT_LSECONDS:
				return Seconds;
			case ELAPSED_UNIT_LMILLISECONDS:
				return Milliseconds;
		}
		return null;
	}

	public static boolean isCodeLower(char lch) {
		return findLower(lch) != null;
	}

	public static ElapsedUnit select(String qncName)
			throws ArgonApiException {
		final ElapsedUnit oUnit = find(qncName);
		if (oUnit == null) {
			final String m = "Unsupported elapsed time unit '" + qncName + "'. Valid options are " + ELAPSED_UNIT_LEGEND;
			throw new ArgonApiException(m);
		}
		return oUnit;
	}

	private ElapsedUnit(char lcsuffix, int ms) {
		this.lcsuffix = lcsuffix;
		this.ms = ms;
	}
	public final char lcsuffix;

	public final int ms;
}
