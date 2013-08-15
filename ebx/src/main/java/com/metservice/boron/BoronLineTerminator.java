/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

/**
 * @author roach
 */
public class BoronLineTerminator {

	public static byte[] select(String ozName) {
		if (ozName == null || ozName.length() == 0) return DETECTED;
		final String quctw = ozName.trim().toUpperCase();
		if (quctw.equals("LF")) return LF;
		if (quctw.equals("CRLF")) return CRLF;
		if (quctw.equals("*")) return DETECTED;
		if (quctw.startsWith("WIN")) return CRLF;
		return LF;
	}

	public static final byte[] LF = UBoron.LF;
	public static final byte[] CRLF = UBoron.CRLF;
	public static final byte[] DETECTED = UBoron.detectLineTerminator();
}
