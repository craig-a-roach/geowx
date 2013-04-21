/*
 * Copyright 2009 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

/**
 * @author roach
 */
public enum TimeMaskToken implements ICodedEnum {
	AMPM("am or pm"),

	DOM("1 or 2 digit day of month (1,2..31)"),

	DOMTH("1 or 2 digit day of month with ordinal suffix (1st,2nd..31st)"),

	DOMZ("2 digit (zero padded) day of month (01,02..31)"),

	DOWC("Three letter day of week code (Mon,Tue...Sun)"),

	DOWN("Full day of week name (Monday,Tuesday...Sunday)"),

	IS_DST("Use term 1 if daylight-saving in effect, otherwise term 2", 2),

	IS_NOON("Use term 1 at noon (12pm USAGE=AT, 1159am UNTIL) , otherwise term 2 (seconds ignored)", 2),

	CASE_AM_NOON("Use term 1 before noon, term 2 at noon (12pm USAGE=AT, 1159am UNTIL), otherwise term 3", 3),

	CASE_NOON_END("Use term 1 at noon (12pm USAGE=AT, 1159am UNTIL), term 2 at 2359 when USAGE=UNTIL, otherwise term 3", 3),

	GMTHM("GMT offset as +-hours:minutes"),

	H12("1 or 2 digit hour of day by 12 hour clock (12, 1..11)"),

	H12Z("2 digit (zero padded) hour of day by 12 hour clock (12, 01..11)"),

	H24("1 or 2 digit hour of day by 24 hour clock (0,1..23)"),

	H24Z("2 digit (zero padded) hour of day by 24 hour clock (00,01..23)"),

	MIN("1 or 2 digit minute of hour (0,1..59)"),

	MINZ("2 digit (zero padded) minute of hour (00,01..59)"),

	MILLISECZ("3 digit (zero padded) millisecond of second (000,001..999)"),

	MON("1 or 2 digit month (Jan is 1)"),

	MONC("Three letter month code. e.g. Sep"),

	MONN("Full month name. e.g. September"),

	MONZ("2 digit (zero padded) month (Jan is 01)"),

	SEC("1 or 2 digit second of minute (0,1..59)"),

	SECZ("2 digit (zero padded) second of minute (00,01..59)"),

	TZC("Daylight-saving-sensitive timezone code (upper-case)"),

	TZN("Daylight-saving-sensitive full timezone name"),

	TZRID("Timezone rule identifier (upper-case)"),

	USAGE_UNTIL("Format date as the inclusive end of a period", true),

	USAGE_AT("Format date as a point-in-time or the inclusive start of a period (default)", true),

	YEAR("4 digit year, including century. e.g. 2009"),

	YOCZ("2 digit (zero padded) year of century. e.g. 09")

	;

	public static final CodedEnumTable<TimeMaskToken> Table = new CodedEnumTable<TimeMaskToken>(TimeMaskToken.class, true,
			TimeMaskToken.values());

	@Override
	public String qCode() {
		return name();
	}

	private TimeMaskToken(String description) {
		this(description, 1, false);
	}

	private TimeMaskToken(String description, boolean usage) {
		this(description, 1, usage);
	}

	private TimeMaskToken(String description, int operands) {
		this(description, operands, false);
	}

	private TimeMaskToken(String description, int operands, boolean usage) {
		assert description != null && description.length() > 0;
		this.description = description;
		this.operands = operands;
		this.usage = usage;
	}

	public final String description;
	public final int operands;
	public final boolean usage;
}
