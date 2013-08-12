/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.shapefile;

/**
 * @author roach
 */
public class FormatException extends Exception {

	private static String zState(Object ostate) {
		final String z = ostate == null ? "" : ostate.toString();
		if (z.length() == 0) return "";
		return " at " + z;
	}

	public static FormatException eof(String reading, Object ostate) {
		final String m = "Unexpected end-of-file while reading " + reading + zState(ostate);
		return new FormatException(m);
	}

	public static FormatException unsupportedShape(int type, int recNo, Object ostate) {
		final String m = "Unsupported shape " + type + " in record no " + recNo + zState(ostate);
		return new FormatException(m);
	}

	private FormatException(String message) {
		super(message);
	}
}
