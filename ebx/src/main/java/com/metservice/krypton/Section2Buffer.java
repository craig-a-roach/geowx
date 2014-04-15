/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import java.util.Calendar;

/**
 * @author roach
 */
class Section2Buffer {

	private static final int Min = 5;

	private void ensure(int plus) {
		final int neoOctet = m_octet + plus;
		if (neoOctet <= m_buffer.length) return;
		final int neoCap = Math.max(neoOctet, m_buffer.length * 3 / 2);
		final byte[] save = m_buffer;
		m_buffer = new byte[neoCap];
		System.arraycopy(save, 0, m_buffer, 0, m_octet);
	}

	public int emit(byte[] dest, int destPos) {
		UGrib.int4(m_buffer, 0, m_octet);
		System.arraycopy(m_buffer, 0, dest, destPos, m_octet);
		return m_octet;
	}

	public void float4(float value) {
		ensure(4);
		UGrib.float4IEEE(m_buffer, m_octet, value);
		m_octet += 4;
	}

	public void increaseCapacityBy(int plus) {
		ensure(plus);
	}

	public void int2(int value) {
		ensure(2);
		UGrib.int2(m_buffer, m_octet, value);
		m_octet += 2;
	}

	public void int4(int value) {
		ensure(4);
		UGrib.int4(m_buffer, m_octet, value);
		m_octet += 4;
	}

	public void octet(byte value) {
		ensure(1);
		m_buffer[m_octet] = value;
		m_octet++;
	}

	public void octets(byte[] octetArray) {
		final int srcLen = octetArray.length;
		ensure(srcLen);
		System.arraycopy(octetArray, 0, m_buffer, m_octet, srcLen);
		m_octet += srcLen;
	}

	public void ts(long value) {
		ensure(7);
		final Calendar cal = UGrib.newGMT(value);
		final int year = UGrib.calYear(cal);
		final int moy = UGrib.calMonthOfYear(cal);
		final int dom = UGrib.calDayOfMonth(cal);
		final int hod = UGrib.calHourOfDay(cal);
		final int moh = UGrib.calMinuteOfHour(cal);
		final int som = UGrib.calSecondOfMinute(cal);
		UGrib.int2(m_buffer, m_octet, year);
		m_octet += 2;
		UGrib.intu1(m_buffer, m_octet, moy);
		m_octet++;
		UGrib.intu1(m_buffer, m_octet, dom);
		m_octet++;
		UGrib.intu1(m_buffer, m_octet, hod);
		m_octet++;
		UGrib.intu1(m_buffer, m_octet, moh);
		m_octet++;
		UGrib.intu1(m_buffer, m_octet, som);
		m_octet++;
	}

	public void u1(int value) {
		ensure(1);
		UGrib.intu1(m_buffer, m_octet, value);
		m_octet++;
	}

	public void u2(int value) {
		ensure(2);
		UGrib.intu2(m_buffer, m_octet, value);
		m_octet += 2;
	}

	public void u3(int value) {
		ensure(3);
		UGrib.intu3(m_buffer, m_octet, value);
		m_octet += 3;
	}

	public Section2Buffer(int sectionNo, int est) {
		m_buffer = new byte[Math.max(Min, est)];
		m_buffer[4] = (byte) sectionNo;
		m_octet = 5;
	}
	private byte[] m_buffer;
	private int m_octet;
}
