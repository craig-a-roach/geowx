/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.metservice.argon.DateFormatter;
import com.metservice.beryllium.BerylliumPath;

/**
 * @author roach
 */
public class NeonAssuranceReport {

	public void add(BerylliumPath path, INeonSourceDescriptor descriptor, AssuranceRunReport runReport) {
		if (path == null) throw new IllegalArgumentException("object is null");
		if (descriptor == null) throw new IllegalArgumentException("object is null");
		if (runReport == null) throw new IllegalArgumentException("object is null");

		final boolean failed = !runReport.isPass();
		if (failed) {
			m_failCount.incrementAndGet();
		}
		m_entries.add(new Entry(path, descriptor, runReport));
	}

	public boolean allPassed() {
		return failCount() == 0;
	}

	public int failCount() {
		return m_failCount.get();
	}

	public List<Entry> new_zlEntriesAsc() {
		final List<Entry> neo = new ArrayList<Entry>(m_entries);
		Collections.sort(neo);
		return neo;
	}

	public String qSummary() {
		final StringBuilder sb = new StringBuilder();
		final int count = m_entries.size();
		final int failCount = m_failCount.get();
		if (count == 0) {
			sb.append("No Tests Found");
		} else {
			sb.append(count).append(" Tests Found...");
			if (failCount == 0) {
				sb.append("ALL PASSED");
			} else {
				sb.append(m_failCount).append(" FAILED");
			}
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		final int count = m_entries.size();
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < count; i++) {
			sb.append("\n").append(i + 1).append(". ");
			sb.append(m_entries.get(i));
		}
		return sb.toString();
	}

	public NeonAssuranceReport() {
	}
	private final List<Entry> m_entries = new ArrayList<Entry>();
	private final AtomicInteger m_failCount = new AtomicInteger();

	public static class Entry implements Comparable<Entry> {

		@Override
		public int compareTo(Entry rhs) {
			return path.compareTo(rhs.path);
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append(runReport.qState());
			sb.append(" ");
			sb.append(path);
			sb.append(" (");
			sb.append(DateFormatter.newPlatformDHMSYFromTs(descriptor.tsLastModified()));
			sb.append(")");
			final String ztwReport = runReport.ztwReport();
			if (ztwReport.length() > 0) {
				sb.append("\n").append(ztwReport);
			}
			return sb.toString();
		}

		public Entry(BerylliumPath path, INeonSourceDescriptor descriptor, AssuranceRunReport runReport) {
			assert path != null;
			assert descriptor != null;
			assert runReport != null;
			this.path = path;
			this.descriptor = descriptor;
			this.runReport = runReport;
		}
		public final BerylliumPath path;
		public final INeonSourceDescriptor descriptor;
		public final AssuranceRunReport runReport;
	}
}
