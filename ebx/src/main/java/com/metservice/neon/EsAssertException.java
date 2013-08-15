/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

/**
 * 
 * @author roach
 */
public class EsAssertException extends EsRunException {

	private static String format(String description, String zExpected, String qTypeExpected, String zActual, String qTypeActual) {
		final StringBuilder sb = new StringBuilder();
		sb.append("Assert Failed: ").append(description);
		sb.append("\nEXPECTED (").append(qTypeExpected).append("){{");
		sb.append(zExpected).append("}}");
		sb.append("\nACTUAL (").append(qTypeActual).append("){{");
		sb.append(zActual).append("}}");
		return sb.toString();
	}

	public EsAssertException(String description, String zExpected, String qTypeExpected, String zActual, String qTypeActual) {
		super(format(description, zExpected, qTypeExpected, zActual, qTypeActual));
	}
}
