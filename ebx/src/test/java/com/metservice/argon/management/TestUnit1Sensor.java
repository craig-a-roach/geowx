/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.management;

import org.junit.Assert;
import org.junit.Test;

import com.metservice.argon.CArgon;
import com.metservice.argon.DateFactory;
import com.metservice.argon.DateFormatter;
import com.metservice.argon.ElapsedFactory;

/**
 * @author roach
 */
public class TestUnit1Sensor {

	private long sim(ArgonSensorHitRate sensor, int hitCount, int missCount, int minutes, long tsInit) {
		long tsNow = tsInit;
		for (int i = 0; i < minutes; i++) {
			for (int h = 0; h < hitCount; h++) {
				sensor.addSample(true, tsInit);
			}
			for (int m = 0; m < missCount; m++) {
				sensor.addSample(false, tsInit);
			}
			tsNow += CArgon.MIN_TO_MS;
			sensor.tick(tsNow);
		}
		return tsNow;
	}

	@Test
	public void t10_hitRate() {

		final double Delta = 0.001;
		long tsNow = DateFactory.newTsConstantFromT8("20130615T0000Z00M000");
		final ArgonSensorHitRate sensor = new ArgonSensorHitRate(ElapsedFactory.newElapsedConstant("10s"), "t10");
		Assert.assertTrue(Float.isNaN(sensor.ratio()));
		sensor.addSample(false, tsNow);
		sensor.addSample(false, tsNow);
		sensor.addSample(false, tsNow);
		sensor.addSample(true, tsNow);
		sensor.addSample(false, tsNow);
		Assert.assertEquals(0.20, sensor.ratio(), Delta);
		tsNow += CArgon.MIN_TO_MS;
		sensor.tick(tsNow);
		Assert.assertEquals(0.20, sensor.ratio(), Delta);
		tsNow = sim(sensor, 1, 3, 5, tsNow);
		Assert.assertEquals(0.2196, sensor.ratio(), Delta);
		tsNow = sim(sensor, 1, 3, 5, tsNow);
		Assert.assertEquals(0.2316, sensor.ratio(), Delta);
		tsNow = sim(sensor, 1, 3, 20, tsNow);
		Assert.assertEquals(0.2475, sensor.ratio(), Delta);
		tsNow = sim(sensor, 1, 2, 5, tsNow);
		Assert.assertEquals(0.2813, sensor.ratio(), Delta);
		tsNow = sim(sensor, 0, 4, 5, tsNow);
		Assert.assertEquals(0.1706, sensor.ratio(), Delta);
		tsNow = sim(sensor, 1, 3, 5, tsNow);
		Assert.assertEquals(0.2018, sensor.ratio(), Delta);
		System.out.println(DateFormatter.newT8FromDate(sensor.getLastSampleTime()) + " " + sensor);
	}
}
