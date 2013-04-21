/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

/**
 * @author roach
 */
public class CSystemExit {

	public static final int OK = 0;
	public static final int GeneralError = 1;

	public static final int UsageError = 64;
	public static final int QuotaExceeded = 66;
	public static final int UnknownHost = 68;
	public static final int ServiceUnavailable = 69;
	public static final int SoftwareError = 70;
	public static final int OSError = 71;
	public static final int IOError = 74;
	public static final int TemporaryFailure = 75;
	public static final int ProtocolError = 76;
	public static final int PermissionDenied = 77;
	public static final int ConfigurationError = 78;
	public static final int ApplicationBase = 80;

	public static final int ApplicationHi = 124;
	public static final int ApplicationGeneral = 125;
	public static final int CancelledByOperator = 130;

	public static int application(int relative) {
		final int code = ApplicationBase + relative;
		return (code > ApplicationHi) ? ApplicationGeneral : code;
	}

	public static boolean isDaemonComplete(int exitCode) {
		return exitCode != GeneralError && exitCode != SoftwareError && exitCode != TemporaryFailure;
	}
}
