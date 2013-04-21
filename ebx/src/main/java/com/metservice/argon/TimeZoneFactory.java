/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.util.TimeZone;

/**
 * @author roach
 */
public class TimeZoneFactory {

	public static final String NAME_GMT = UArgon.TIMEZONE_NAME_GMT;
	public static final TimeZone GMT = UArgon.GMT;

	public static TimeZone findById(String qccId) {
		if (qccId == null || qccId.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final String zuctw = qccId.toUpperCase().trim();
		if (zuctw.length() == 0) return null;

		for (int i = 0; i < CArgonTimeZone.TZALIAS_GMT.length; i++) {
			if (zuctw.equals(CArgonTimeZone.TZALIAS_GMT[i])) return UArgon.GMT;
		}

		for (int r = 0; r < CArgonTimeZone.TZALIAS_REGIONAL.length; r++) {
			final String[] region = CArgonTimeZone.TZALIAS_REGIONAL[r];
			final String qccRegionId = region[0];
			for (int i = 1; i < region.length; i++) {
				if (zuctw.equals(region[i])) return TimeZone.getTimeZone(qccRegionId);
			}
		}

		final TimeZone oTimeZone = TimeZone.getTimeZone(qccId);
		if (oTimeZone == null || oTimeZone.equals(UArgon.GMT)) return null;
		return oTimeZone;
	}

	public static TimeZone selectById(String qccId)
			throws ArgonApiException {
		final TimeZone oTimeZone = findById(qccId);
		if (oTimeZone == null)
			throw new ArgonApiException("Time zone '" + qccId + "' is not in JRE database and is not a valid alias");
		return oTimeZone;
	}

	private TimeZoneFactory() {
	}
}
