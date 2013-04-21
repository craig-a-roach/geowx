/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.util.Arrays;

/**
 * @author roach
 */
public class TimeOfDayRule {

	public String format() {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < m_xptTerms.length; i++) {
			if (i > 0) {
				sb.append(',');
			}
			sb.append(m_xptTerms[i].format());
		}
		return sb.toString();
	}

	public int[] secondsOfDayAsc() {

		final int[][] matrix = new int[m_xptTerms.length][];
		int deepCount = 0;
		for (int irow = 0; irow < m_xptTerms.length; irow++) {
			final int[] xptRowAsc = m_xptTerms[irow].secondsOfDayAsc();
			matrix[irow] = xptRowAsc;
			deepCount += xptRowAsc.length;
		}

		final int[] xptFlatAsc = new int[deepCount];
		int pos = 0;
		for (int irow = 0; irow < m_xptTerms.length; irow++) {
			final int[] src = matrix[irow];
			System.arraycopy(src, 0, xptFlatAsc, pos, src.length);
			pos += src.length;
		}

		Arrays.sort(xptFlatAsc);
		int reducedCount = 1;
		int lhs = xptFlatAsc[0];
		for (int irhs = 1; irhs < deepCount; irhs++) {
			final int rhs = xptFlatAsc[irhs];
			if (lhs != rhs) {
				xptFlatAsc[reducedCount] = rhs;
				reducedCount++;
				lhs = rhs;
			}
		}
		if (reducedCount == deepCount) return xptFlatAsc;
		final int[] xptReducedAsc = new int[reducedCount];
		System.arraycopy(xptFlatAsc, 0, xptReducedAsc, 0, reducedCount);
		return xptReducedAsc;
	}

	@Override
	public String toString() {
		return format();
	}

	public TimeOfDayRule(TimeOfDayRuleTerm[] xptTerms) {
		if (xptTerms == null || xptTerms.length == 0) throw new IllegalArgumentException("array is null or empty");
		m_xptTerms = xptTerms;
	}

	private final TimeOfDayRuleTerm[] m_xptTerms;
}
