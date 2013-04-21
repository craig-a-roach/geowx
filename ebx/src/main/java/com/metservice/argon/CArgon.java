/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

/**
 * @author roach
 */
public interface CArgon {

	public static final int K = 1024;
	public static final int M = K * K;
	public static final long G = 1024L * M;

	public static final int LIMIT_PORT_USERLO = 1024;
	public static final int LIMIT_PORT_HI = 65535;

	public static final double MAXD_INIT = -Double.MAX_VALUE;
	public static final double MIND_INIT = Double.MAX_VALUE;
	public static final float MAXF_INIT = -Float.MAX_VALUE;
	public static final float MINF_INIT = Float.MAX_VALUE;

	public static final String UserHomeNode = "~";
	public static final int UserHomeNodeL = UserHomeNode.length();

	public static final char ELAPSED_UNIT_LDAYS = 'd';
	public static final char ELAPSED_UNIT_UDAYS = 'D';
	public static final char ELAPSED_UNIT_LHOURS = 'h';
	public static final char ELAPSED_UNIT_UHOURS = 'H';
	public static final char ELAPSED_UNIT_LMINUTES = 'm';
	public static final char ELAPSED_UNIT_UMINUTES = 'M';
	public static final char ELAPSED_UNIT_LSECONDS = 's';
	public static final char ELAPSED_UNIT_USECONDS = 'S';
	public static final char ELAPSED_UNIT_LMILLISECONDS = 't';
	public static final char ELAPSED_UNIT_UMILLISECONDS = 'T';
	public static final String ELAPSED_UNIT_LEGEND = "(D)ays, (H)ours, (M)inutes, (S)econds, (T)housandths/milliseconds";

	public static final char DATE_SEPARATOR_DAYHOUR = 'T';
	public static final char DATE_SEPARATOR_MINSEC = 'Z';
	public static final char DATE_SEPARATOR_SECMILLI = 'M';
	static final String DATE_SUFFIX1 = DATE_SEPARATOR_SECMILLI + "000";
	static final String DATE_SUFFIX2 = "00" + DATE_SUFFIX1;

	public static final char REAL_UEXPONENT = 'R';
	public static final char REAL_LEXPONENT = 'r';

	public static final int HR_PER_DAY = 24;
	public static final int MIN_PER_HR = 60;
	public static final int SEC_PER_MIN = 60;
	public static final int MS_PER_SEC = 1000;
	public static final long LMS_PER_SEC = 1000L;

	public static final int MIN_PER_DAY = MIN_PER_HR * HR_PER_DAY;

	public static final int SEC_PER_HR = SEC_PER_MIN * MIN_PER_HR;
	public static final int SEC_PER_DAY = SEC_PER_MIN * MIN_PER_DAY;
	public static final int SEC_PER_DAY2 = SEC_PER_MIN * MIN_PER_DAY / 2;

	public static final int MS_PER_HR = MS_PER_SEC * SEC_PER_HR;
	public static final int MS_PER_MIN = MS_PER_SEC * SEC_PER_MIN;
	public static final int MS_PER_DAY = MS_PER_SEC * SEC_PER_DAY;
	public static final int MS_PER_DAY2 = MS_PER_SEC * SEC_PER_DAY / 2;
	public static final long LMS_PER_MIN = LMS_PER_SEC * SEC_PER_MIN;
	public static final long LMS_PER_MIN2 = LMS_PER_SEC * SEC_PER_MIN / 2;
	public static final long LMS_PER_HR = LMS_PER_MIN * MIN_PER_HR;

	public static final int SEC_TO_MS = MS_PER_SEC;
	public static final int MIN_TO_MS = MS_PER_MIN;
	public static final int HR_TO_MS = MS_PER_HR;
	public static final long DAY_TO_MS = MS_PER_DAY;

	public static final int MIN_TO_SEC = SEC_PER_MIN;
}
