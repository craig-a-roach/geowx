/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

/**
 * @author roach
 */
class ProfileSample {

	public void lineCallDone(long ntStart, int lineIndex, IEsCallable callable) {
		final long ntEnd = System.nanoTime();
		final long nsElapsed = ntEnd - ntStart;
		final String oqccCallableName = callable.oqccName();
		final String qccCallableName = oqccCallableName == null ? "()" : oqccCallableName;
		program.lineCallDone(lineIndex, nsElapsed, qccCallableName);
	}

	public long lineDone(long ntStart, int lineIndex) {
		final long ntEnd = System.nanoTime();
		final long nsElapsed = ntEnd - ntStart;
		if (linesExecuted == 0) {
			ntFirstLineStart = ntStart;
		}
		ntLatestLineEnd = ntEnd;
		linesExecuted++;
		program.lineDone(lineIndex, nsElapsed);
		return ntEnd;
	}

	public void runEnd() {
		final long nsRunElapsed = System.nanoTime() - ntRunStart;
		final long nsLinesElapsed = ntLatestLineEnd - ntFirstLineStart;
		program.endRun(nsRunElapsed, nsLinesElapsed, linesExecuted);
	}

	public void runStart() {
		ntRunStart = System.nanoTime();
	}

	public static ProfileSample newInstance(EsSource source, EsSourceHtml sourceHtml) {
		if (source == null) throw new IllegalArgumentException("object is null");
		if (sourceHtml == null) throw new IllegalArgumentException("object is null");
		final int lineCount = source.lineCount();
		final int checksum = source.checksumAdler32();
		return new ProfileSample(lineCount, checksum, sourceHtml);
	}

	private ProfileSample(int lineCount, int checksum, EsSourceHtml sourceHtml) {
		this.lineCount = lineCount;
		this.checksum = checksum;
		this.sourceHtml = sourceHtml;
		program = new ProfileSamplePI(lineCount);
	}

	final int lineCount;
	final int checksum;
	final EsSourceHtml sourceHtml;
	final ProfileSamplePI program;
	long ntRunStart;
	int linesExecuted;
	long ntFirstLineStart;
	long ntLatestLineEnd;
}
