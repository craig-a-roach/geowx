/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import java.util.ArrayList;
import java.util.List;

import com.metservice.argon.ArgonText;

/**
 * @author roach
 */
public class BerylliumQuery {

	public static final BerylliumQuery Empty = new BerylliumQuery(new String[0]);

	private String fmt(boolean asSuffix) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < m_zptqtw.length; i++) {
			final String qtw = m_zptqtw[i];
			if (i == 0) {
				if (asSuffix) {
					sb.append('?');
				}
			} else {
				sb.append('&');
			}
			sb.append(qtw);
		}
		return sb.toString();
	}

	public String format() {
		return fmt(false);
	}

	public boolean isEmpty() {
		return m_zptqtw.length == 0;
	}

	public BerylliumQuery newQuery(BerylliumQuery rhs) {
		if (rhs == null) throw new IllegalArgumentException("object is null");
		final String[] lzpt = m_zptqtw;
		final String[] rzpt = rhs.m_zptqtw;
		final String[] zpt = new String[lzpt.length + rzpt.length];
		System.arraycopy(lzpt, 0, zpt, 0, lzpt.length);
		System.arraycopy(rzpt, 0, zpt, lzpt.length, rzpt.length);
		return new BerylliumQuery(zpt);
	}

	public BerylliumQuery newQuery(Object... ozptRhsQueryNameValues) {
		return newQuery(newInstance(ozptRhsQueryNameValues));
	}

	@Override
	public String toString() {
		return fmt(false);
	}

	public String zUriQuery() {
		return fmt(true);
	}

	public static BerylliumQuery newConstant(Object... ozptQueryNameValues) {
		return newInstance(ozptQueryNameValues);
	}

	public static BerylliumQuery newInstance(List<Object> ozlQueryNameValues) {
		if (ozlQueryNameValues == null) return Empty;
		final int size = ozlQueryNameValues.size();
		if (size == 0) return Empty;
		final String[] zpt = ozlQueryNameValues.toArray(new String[size]);
		return newInstance(zpt);
	}

	public static BerylliumQuery newInstance(Object[] ozptQueryNameValues) {
		if (ozptQueryNameValues == null) return Empty;
		final int len = ozptQueryNameValues.length;
		if (len == 0) return Empty;
		final List<String> zl = new ArrayList<String>(len / 2);
		for (int i = 0; i < len; i += 2) {
			final int iname = i;
			final Object oname = ozptQueryNameValues[iname];
			final int ivalue = i + 1;
			final Object ovalue = ivalue < len ? ozptQueryNameValues[ivalue] : null;
			final String ztwName = oname == null ? "" : oname.toString().trim();
			if (ztwName.length() == 0) {
				continue;
			}
			final StringBuilder sb = new StringBuilder();
			sb.append(ztwName);
			final String zValue = ovalue == null ? "" : ovalue.toString();
			if (zValue.length() > 0) {
				sb.append('=');
				final String qenc = UBeryllium.w3c_form_urlencoded(zValue, ArgonText.UTF8);
				sb.append(qenc);
			}
			zl.add(sb.toString());
		}
		final int count = zl.size();
		final String[] zptqtw = zl.toArray(new String[count]);
		return new BerylliumQuery(zptqtw);
	}

	private BerylliumQuery(String[] zptqtw) {
		assert zptqtw != null;
		m_zptqtw = zptqtw;
	}

	private final String[] m_zptqtw;
}
