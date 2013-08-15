/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import org.xml.sax.SAXException;

/**
 * @author roach
 */
public class TestTargetJvmA {

	public static int fa(String[] a1, Integer a2, int a3) {
		return (a1 == null ? 0 : a1.length) + a2 + a3;
	}

	public static String fb(boolean a1, String a2, double a3) {
		return a1 + ":" + a2 + ":" + a3;
	}

	public static void fc() {
	}

	public static void willFail(String a1)
			throws SAXException {
		throw new SAXException("invalid a1>" + a1 + "<");
	}

}
