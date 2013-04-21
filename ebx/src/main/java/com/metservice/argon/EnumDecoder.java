/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

/**
 * @author roach
 */
public class EnumDecoder<T extends Enum<?>> {

	public T find(String qTarget) {
		return find(m_zptValues, qTarget, false);
	}

	public T find(String qTarget, boolean caseSensitive) {
		return find(m_zptValues, qTarget, caseSensitive);
	}

	public T select(String qTarget)
			throws ArgonApiException {
		return select(m_qTitle, m_zptValues, qTarget, false);
	}

	public T select(String qTarget, boolean caseSensitive)
			throws ArgonApiException {
		return select(m_qTitle, m_zptValues, qTarget, caseSensitive);
	}

	@Override
	public String toString() {
		return m_qTitle + "[" + UArgon.msgComma(m_zptValues) + "]";
	}

	public static <T extends Enum<?>> T find(T[] zptValues, String qTarget) {
		return find(zptValues, qTarget, false);
	}

	public static <T extends Enum<?>> T find(T[] zptValues, String qTarget, boolean caseSensitive) {
		if (zptValues == null) throw new IllegalArgumentException("array is null");
		if (qTarget == null) throw new IllegalArgumentException("object is null");

		final String ztwTarget = qTarget.trim();
		if (ztwTarget.length() == 0) return null;
		final String qcctwTarget = caseSensitive ? ztwTarget : ztwTarget.toUpperCase();

		for (int i = 0; i < zptValues.length; i++) {
			final T value = zptValues[i];
			final String qtwValue = value.name();
			final String qcctwValue = caseSensitive ? qtwValue : qtwValue.toUpperCase();
			if (qcctwTarget.equals(qcctwValue)) return value;
		}

		return null;
	}

	public static <T extends Enum<?>> T select(String qTitle, T[] zptValues, String qTarget, boolean caseSensitive)
			throws ArgonApiException {
		if (qTarget == null || qTarget.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final T oMatch = find(zptValues, qTarget, caseSensitive);
		if (oMatch == null) {
			final StringBuilder bm = new StringBuilder();
			bm.append("Unsupported ").append(qTitle).append(" value '").append(qTarget);
			bm.append("' (").append(caseSensitive ? "case-sensitive" : "case-insensitive").append(")");
			bm.append("...valid values are\n");
			bm.append(UArgon.msgComma(zptValues));
			throw new ArgonApiException(bm.toString());
		}
		return oMatch;
	}

	public EnumDecoder(T[] zptValues, String qTitle) {
		if (zptValues == null) throw new IllegalArgumentException("object is null");
		if (qTitle == null || qTitle.length() == 0) throw new IllegalArgumentException("string is null or empty");
		m_zptValues = zptValues;
		m_qTitle = qTitle;
	}

	private final T[] m_zptValues;
	private final String m_qTitle;
}
