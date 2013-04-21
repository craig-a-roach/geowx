/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author roach
 */
public class TestUnit1TimeZone {

	@Test
	public void t50()
			throws ArgonApiException {
		final TimeZone GMT = TimeZoneFactory.GMT;
		final TimeZone NZST = TimeZoneFactory.selectById("NZST");
		final TimeZone AKL = TimeZoneFactory.selectById("Pacific/Auckland");
		final TimeZone BNE = TimeZoneFactory.selectById("Australia/Brisbane");
		final TimeZone ADL = TimeZoneFactory.selectById("Australia/Adelaide");
		final TimeZone NY = TimeZoneFactory.selectById("America/New_York");
		final TimeZone GMT10 = TimeZoneFactory.selectById("GMT+10");
		final TimeZone GMTM10 = TimeZoneFactory.selectById("GMT-10");

		Assert.assertEquals("GMT", TimeZoneFormatter.id(GMT));
		Assert.assertEquals("00:00", TimeZoneFormatter.stdDST(GMT));
		Assert.assertEquals("GMT", TimeZoneFormatter.gmtStdDST(GMT));
		Assert.assertEquals("GMT+12:00", TimeZoneFormatter.id(NZST));
		Assert.assertEquals("+12:00", TimeZoneFormatter.stdDST(NZST));
		Assert.assertEquals("GMT+12:00", TimeZoneFormatter.gmtStdDST(NZST));
		Assert.assertEquals("Pacific/Auckland", TimeZoneFormatter.id(AKL));
		Assert.assertEquals("+12:00+1h", TimeZoneFormatter.stdDST(AKL));
		Assert.assertEquals("GMT+12:00+1h", TimeZoneFormatter.gmtStdDST(AKL));
		Assert.assertEquals("Australia/Brisbane", TimeZoneFormatter.id(BNE));
		Assert.assertEquals("+10:00", TimeZoneFormatter.stdDST(BNE));
		Assert.assertEquals("Australia/Adelaide", TimeZoneFormatter.id(ADL));
		Assert.assertEquals("+09:30+1h", TimeZoneFormatter.stdDST(ADL));
		Assert.assertEquals("America/New_York", TimeZoneFormatter.id(NY));
		Assert.assertEquals("-05:00+1h", TimeZoneFormatter.stdDST(NY));
		Assert.assertEquals("GMT-05:00+1h", TimeZoneFormatter.gmtStdDST(NY));
		Assert.assertEquals("GMT+10:00", TimeZoneFormatter.id(GMT10));
		Assert.assertEquals("+10:00", TimeZoneFormatter.stdDST(GMT10));
		Assert.assertEquals("GMT+10:00", TimeZoneFormatter.gmtStdDST(GMT10));
		Assert.assertEquals("GMT-10:00", TimeZoneFormatter.id(GMTM10));
		Assert.assertEquals("-10:00", TimeZoneFormatter.stdDST(GMTM10));
		Assert.assertEquals("GMT-10:00", TimeZoneFormatter.gmtStdDST(GMTM10));
	}

}
