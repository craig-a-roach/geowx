/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author roach
 */
public class ArgonClock {

	private static final AtomicLong SMSADJ = new AtomicLong(0L);

	public static void simulatedNow(long tsSimulated) {
		SMSADJ.set(tsSimulated - System.currentTimeMillis());
	}

	public static long tsNow() {
		return System.currentTimeMillis() + SMSADJ.get();
	}
}
