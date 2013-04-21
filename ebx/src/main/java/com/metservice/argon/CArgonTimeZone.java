/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

/**
 * @author roach
 */
interface CArgonTimeZone extends CArgon {

	public static final String[] TZALIAS_GMT = { "GMT", "UTC", "Z" };
	public static final String[][] TZALIAS_REGIONAL = { { "GMT+10:00", "AEST" }, { "GMT+11:00", "AEDT" },
			{ "GMT+12:00", "NZST" }, { "GMT+13:00", "NZDT" }, { "Pacific/Auckland", "NZ" }, { "GMT-04:00", "EDT" },
			{ "GMT-05:00", "EST", "CDT" }, { "GMT-06:00", "CST", "MDT" }, { "GMT-07:00", "MST", "PDT" },
			{ "GMT-08:00", "PST" } };

}
