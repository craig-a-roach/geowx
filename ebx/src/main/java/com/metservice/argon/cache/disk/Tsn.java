/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.cache.disk;

import java.util.Date;

import com.metservice.argon.DateFactory;
import com.metservice.argon.DateFormatter;

/**
 * @author roach
 */
class Tsn {

	static final long TSNIL = 0L;

	public static final Tsn Nil = new Tsn(TSNIL);

	public static Date getDate(long tsn) {
		return isNil(tsn) ? null : DateFactory.newDate(tsn);
	}

	public static boolean isNil(long tsn) {
		return tsn == TSNIL;
	}

	public static Tsn newInstance(Date oDate) {
		if (oDate == null) return Nil;
		return new Tsn(oDate.getTime());
	}

	public static String toString(long tsn) {
		return isNil(tsn) ? "nil" : DateFormatter.newT8FromTs(tsn);
	}

	public boolean isNil() {
		return isNil(m_tsn);
	}

	public long toLong() {
		return m_tsn;
	}

	@Override
	public String toString() {
		return isNil() ? "nil" : DateFormatter.newT8FromTs(m_tsn);
	}

	private Tsn(long tsn) {
		m_tsn = tsn;
	}
	private final long m_tsn;
}
