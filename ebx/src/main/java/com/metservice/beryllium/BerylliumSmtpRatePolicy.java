/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
public class BerylliumSmtpRatePolicy {

	public static final BerylliumSmtpRatePolicy InstanceUnthrottled = new BerylliumSmtpRatePolicy(0L, 0L);

	public static BerylliumSmtpRatePolicy newMin(long msInterval) {
		final long msClamped = Math.max(0L, msInterval);
		return new BerylliumSmtpRatePolicy(msClamped, msClamped);
	}

	public static BerylliumSmtpRatePolicy newMinMax(long msMinInterval, long msMaxInterval) {
		final long msClampedMin = Math.max(0, msMinInterval);
		final long msClampedMax = Math.max(msClampedMin, Math.max(0, msMaxInterval));
		return new BerylliumSmtpRatePolicy(msClampedMin, msClampedMax);
	}

	public long msMaxSendInterval() {
		return m_msMaxSendInterval;
	}

	public long msMinSendInterval() {
		return m_msMinSendInterval;
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("msMinSendInterval", m_msMinSendInterval);
		ds.a("msMaxSendInterval", m_msMaxSendInterval);
		return ds.s();
	}

	private BerylliumSmtpRatePolicy(long msMinSendInterval, long msMaxSendInterval) {
		m_msMinSendInterval = msMinSendInterval;
		m_msMaxSendInterval = msMaxSendInterval;
	}
	private final long m_msMinSendInterval;
	private final long m_msMaxSendInterval;
}
