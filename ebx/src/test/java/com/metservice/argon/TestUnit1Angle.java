/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import org.junit.Assert;

/**
 * @author roach
 */
public class TestUnit1Angle {

	private static final double Delta = 1e-9;

	public void t10_dms() {
		try {
			Assert.assertEquals(-41.5, AngleFactory.newDegrees("S41\u00B030m"), Delta);
		} catch (final ArgonFormatException ex) {
			Assert.assertEquals("ok", ex.getMessage());
		}
	}

}
