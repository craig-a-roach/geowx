/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emmail;

import com.metservice.argon.CArgon;
import com.metservice.beryllium.BerylliumSmtpRatePolicy;
import com.metservice.neon.EsExecutionContext;
import com.metservice.neon.EsIntrinsicObject;
import com.metservice.neon.EsObjectAccessor;

/**
 * @author roach
 */
class RatePolicyFactory {

	public static final BerylliumSmtpRatePolicy Default = BerylliumSmtpRatePolicy.newMin(2 * CArgon.MIN_TO_MS);

	public static EsIntrinsicObject newEsObject(EsExecutionContext ecx, BerylliumSmtpRatePolicy policy) {
		if (policy == null) throw new IllegalArgumentException("object is null");
		final EsIntrinsicObject neo = ecx.global().newIntrinsicObject();
		neo.putViewElapsed(CProp.min, policy.msMinSendInterval());
		neo.putViewElapsed(CProp.max, policy.msMaxSendInterval());
		return neo;
	}

	public static BerylliumSmtpRatePolicy newPolicy(EsObjectAccessor acc)
			throws InterruptedException {
		if (acc == null) throw new IllegalArgumentException("object is null");
		final long msMinInterval;
		if (acc.defaulted(CProp.min)) {
			msMinInterval = Default.msMinSendInterval();
		} else {
			msMinInterval = acc.elapsedValue(CProp.min).sms;
		}
		if (acc.defaulted(CProp.max)) return BerylliumSmtpRatePolicy.newMin(msMinInterval);
		final long msMaxInterval = acc.elapsedValue(CProp.max).sms;
		return BerylliumSmtpRatePolicy.newMinMax(msMinInterval, msMaxInterval);
	}

	private RatePolicyFactory() {
	}

}
