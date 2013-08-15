/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.Date;

import com.metservice.argon.ArgonApiException;
import com.metservice.argon.Binary;

/**
 * @author roach
 */
public class EmMutablePropertyAccessor {

	public Binary binaryValue()
			throws InterruptedException {
		return acc.src.property_binary(acc.ecx, qccPropertyName);
	}

	public String exType(ArgonApiException ex) {
		return acc.src.exType(qccPropertyName, ex);
	}

	public int intValue()
			throws InterruptedException {
		return acc.src.property_int(acc.ecx, qccPropertyName);
	}

	public String qtwStringValue()
			throws InterruptedException {
		return acc.src.property_qtwString(acc.ecx, qccPropertyName);
	}

	public Date timeValue()
			throws InterruptedException {
		return acc.src.property_time(acc.ecx, qccPropertyName);
	}

	@Override
	public String toString() {
		return qccPropertyName;
	}

	public long tsTimeValue()
			throws InterruptedException {
		return acc.src.property_tsTime(acc.ecx, qccPropertyName);
	}

	public String ztwStringValue()
			throws InterruptedException {
		return acc.src.property_ztwString(acc.ecx, qccPropertyName);
	}

	EmMutablePropertyAccessor(EmMutableAccessor accessor, String qccPropertyName) {
		if (accessor == null) throw new IllegalArgumentException("object is null");
		if (qccPropertyName == null || qccPropertyName.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		this.acc = accessor;
		this.qccPropertyName = qccPropertyName;
		this.esType = accessor.esType(qccPropertyName);
	}
	private final EmMutableAccessor acc;
	public final String qccPropertyName;
	public final EsType esType;
}
