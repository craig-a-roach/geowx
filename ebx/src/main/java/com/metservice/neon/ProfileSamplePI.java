/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

/**
 * @author roach
 */
class ProfileSamplePI {

	private ProfileSampleLI declare(int lineIndex) {
		ProfileSampleLI vLineItem = lineItemMap[lineIndex];
		if (vLineItem == null) {
			vLineItem = new ProfileSampleLI();
			lineItemMap[lineIndex] = vLineItem;
		}
		return vLineItem;
	}

	public void endRun(long nsRunElapsed, long nsLinesElapsed, int linesExecuted) {
		nsCumRun += nsRunElapsed;
		nsCumLines += nsLinesElapsed;
		cumLinesExecuted += linesExecuted;
		count++;
	}

	public void lineCallDone(int lineIndex, long ns, String qccName) {
		declare(lineIndex).callDone(ns, qccName);
	}

	public void lineDone(int lineIndex, long nsElapsed) {
		declare(lineIndex).done(nsElapsed);
	}

	public static ProfileSamplePI union(ProfileSamplePI oLhs, ProfileSamplePI oRhs) {
		if (oLhs == null) return oRhs;
		if (oRhs == null) return oLhs;

		final ProfileSamplePI lhs = oLhs;
		final ProfileSamplePI rhs = oRhs;

		final int lhsLineCount = lhs.lineItemMap.length;
		final int rhsLineCount = rhs.lineItemMap.length;
		final int neoLineCount = Math.max(lhsLineCount, rhsLineCount);

		final ProfileSamplePI neo = new ProfileSamplePI(neoLineCount);
		neo.nsCumRun = lhs.nsCumRun + rhs.nsCumRun;
		neo.nsCumLines = lhs.nsCumLines + rhs.nsCumLines;
		neo.cumLinesExecuted = lhs.cumLinesExecuted + rhs.cumLinesExecuted;
		neo.count = lhs.count + rhs.count;

		for (int i = 0; i < lhsLineCount && i < rhsLineCount; i++) {
			final ProfileSampleLI oLhsLine = lhs.lineItemMap[i];
			final ProfileSampleLI oRhsLine = rhs.lineItemMap[i];
			final ProfileSampleLI oUnionLine = ProfileSampleLI.union(oLhsLine, oRhsLine);
			neo.lineItemMap[i] = oUnionLine;
		}
		for (int i = lhsLineCount; i < neoLineCount; i++) {
			neo.lineItemMap[i] = lhs.lineItemMap[i];
		}
		for (int i = rhsLineCount; i < neoLineCount; i++) {
			neo.lineItemMap[i] = rhs.lineItemMap[i];
		}
		return neo;
	}

	ProfileSamplePI(int lineCount) {
		lineItemMap = new ProfileSampleLI[lineCount];
	}

	final ProfileSampleLI[] lineItemMap;
	long nsCumRun;
	long nsCumLines;
	int cumLinesExecuted;
	int count;
}
