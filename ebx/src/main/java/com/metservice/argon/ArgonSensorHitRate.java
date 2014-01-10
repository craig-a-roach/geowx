/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.util.Date;

import com.metservice.argon.management.IArgonSensorClocked;

/**
 * @author roach
 */
public class ArgonSensorHitRate implements IArgonSensorRatio, IArgonSensorClocked {

	private static final long TsNil = -1L;
	private static final long MinIntervalMs = 1000L;

	private float rateLk() {
		if (m_hitCount == 0 && m_missCount == 0) return Float.NaN;
		return m_hitCount / ((float) (m_hitCount + m_missCount));
	}

	private void updateLk(long msInterval) {
		final float rate = rateLk();
		if (Float.isNaN(rate)) return;
		final double ewmaNeo;
		if (Float.isNaN(m_ewmaRate)) {
			ewmaNeo = rate;
		} else {
			final double calpha = Math.exp(-msInterval / m_msBase);
			ewmaNeo = (rate * (1.0 - calpha)) + (m_ewmaRate * calpha);
		}
		m_ewmaRate = (float) ewmaNeo;
		m_hitCount = 0;
		m_missCount = 0;
	}

	public void addSample(boolean hit, long tsNow) {
		synchronized (this) {
			if (hit) {
				m_hitCount++;
			} else {
				m_missCount++;
			}
			m_tsLastSample = tsNow;
			if (m_tsLastUpdate == TsNil) {
				m_tsLastUpdate = tsNow;
			}
		}
	}

	@Override
	public String description() {
		return m_description;
	}

	@Override
	public Date getLastSampleTime() {
		return DateFactory.createDate(m_tsLastSample);
	}

	@Override
	public float ratio() {
		synchronized (this) {
			return Float.isNaN(m_ewmaRate) ? rateLk() : m_ewmaRate;
		}
	}

	@Override
	public void tick(long tsNow) {
		synchronized (this) {
			if (m_tsLastUpdate != TsNil) {
				final long msInterval = tsNow - m_tsLastUpdate;
				if (msInterval >= MinIntervalMs) {
					updateLk(msInterval);
					m_tsLastUpdate = tsNow;
				}
			}
		}
	}

	@Override
	public String toString() {
		final float ratio = ratio();
		return Float.isNaN(ratio) ? "n/a" : Float.toString(ratio * 100.0f) + "%";
	}

	public ArgonSensorHitRate(Elapsed baseInterval, String description) {
		if (baseInterval == null) throw new IllegalArgumentException("object is null");
		if (description == null || description.length() == 0) throw new IllegalArgumentException("string is null or empty");
		m_msBase = baseInterval.atLeast(1L);
		m_description = description;
	}
	private final float m_msBase;
	private final String m_description;
	private long m_tsLastSample = TsNil;
	private long m_hitCount;
	private long m_missCount;
	private long m_tsLastUpdate = TsNil;
	private float m_ewmaRate = Float.NaN;
}
