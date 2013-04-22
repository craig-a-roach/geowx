/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author roach
 */
public class KryptonManifestCounter {

	public long fileClockMs() {
		return m_fileClockMs.get();
	}

	public void fileClockMs(long ms) {
		m_fileClockMs.addAndGet(ms);
	}

	public int filesGood() {
		return m_filesGood.get();
	}

	public int filesPartial() {
		return m_filesPartial.get();
	}

	public int filesSkipped() {
		return m_filesSkipped.get();
	}

	public long fileSumMs() {
		return m_fileSumMs.get();
	}

	public void fileSumMs(long ms) {
		m_fileSumMs.addAndGet(ms);
	}

	public void goodFile() {
		m_filesGood.incrementAndGet();
	}

	public void goodRecord() {
		m_recordsGood.incrementAndGet();
	}

	public KryptonManifestCounter newSum(KryptonManifestCounter rhs) {
		if (rhs == null) throw new IllegalArgumentException("object is null");
		return new KryptonManifestCounter(this, rhs);
	}

	public void partialFile() {
		m_filesPartial.incrementAndGet();
	}

	public int recordsGood() {
		return m_recordsGood.get();
	}

	public int recordsSkipped() {
		return m_recordsSkipped.get();
	}

	public void skipFile() {
		m_filesSkipped.incrementAndGet();
	}

	public void skipRecord() {
		m_recordsSkipped.incrementAndGet();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Files: good=").append(m_filesGood.get());
		sb.append(", partial=").append(m_filesPartial.get());
		sb.append(", skipped=").append(m_filesSkipped.get());
		sb.append("\nRecords: good=").append(m_recordsGood.get());
		sb.append(", skipped=").append(m_recordsSkipped.get());
		sb.append("\nFile elapsed=").append(m_fileClockMs.get()).append("ms");
		sb.append(", sum=").append(m_fileSumMs.get()).append("ms");
		return sb.toString();
	}

	private KryptonManifestCounter(KryptonManifestCounter lhs, KryptonManifestCounter rhs) {
		if (lhs == null) throw new IllegalArgumentException("object is null");
		if (rhs == null) throw new IllegalArgumentException("object is null");
		m_filesGood = new AtomicInteger(lhs.filesGood() + rhs.filesGood());
		m_filesPartial = new AtomicInteger(lhs.filesPartial() + rhs.filesPartial());
		m_filesSkipped = new AtomicInteger(lhs.filesSkipped() + rhs.filesSkipped());
		m_recordsGood = new AtomicInteger(lhs.recordsGood() + rhs.recordsGood());
		m_recordsSkipped = new AtomicInteger(lhs.recordsSkipped() + rhs.recordsSkipped());
		m_fileSumMs = new AtomicLong(lhs.fileSumMs() + rhs.fileSumMs());
		m_fileClockMs = new AtomicLong(lhs.fileClockMs() + rhs.fileClockMs());
	}

	public KryptonManifestCounter() {
		m_filesGood = new AtomicInteger();
		m_filesPartial = new AtomicInteger();
		m_filesSkipped = new AtomicInteger();
		m_recordsGood = new AtomicInteger();
		m_recordsSkipped = new AtomicInteger();
		m_fileSumMs = new AtomicLong();
		m_fileClockMs = new AtomicLong();
	}
	private final AtomicInteger m_filesGood;
	private final AtomicInteger m_filesPartial;
	private final AtomicInteger m_filesSkipped;
	private final AtomicInteger m_recordsGood;
	private final AtomicInteger m_recordsSkipped;
	private final AtomicLong m_fileSumMs;
	private final AtomicLong m_fileClockMs;
}
