/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import org.junit.Test;

/**
 * @author roach
 */
public class TestUnit1GeoProjector {

	@Test
	public void t50_latlon() {

		final KryptonArrayFactory af = new KryptonArrayFactory(41, 21);
		final GridScan scan = new GridScan(af, 0);
		new GeoProjectorLatitudeLongitude(scan, 1.5, 1.5, 64.5, -15.0, 34.5, 45.0, null);

	}
}
