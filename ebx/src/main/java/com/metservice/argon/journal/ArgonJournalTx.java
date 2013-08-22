/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.journal;

import java.io.File;

import com.metservice.argon.ArgonNumber;

/**
 * @author roach
 */
public class ArgonJournalTx {

	private static String newCommitFileName(long serial, String qccType) {
		return ArgonNumber.longToDec(serial, 12) + "." + qccType;
	}

	public static String newArchiveFileName(long serial) {
		return newCommitFileName(serial, ArgonJournalController.ArchiveType);
	}

	public static ArgonJournalTx newInstance(File cndir, long serial, String qccType, boolean autoCommit) {
		if (cndir == null) throw new IllegalArgumentException("object is null");
		if (qccType == null || qccType.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final String commitFileName = newCommitFileName(serial, qccType);
		final File commitFile = new File(cndir, commitFileName);
		if (autoCommit) return new ArgonJournalTx(serial, qccType, commitFile, commitFile, true);
		final String wipFileName = commitFileName + ArgonJournalController.SuffixWip;
		final File wipFile = new File(cndir, wipFileName);
		return new ArgonJournalTx(serial, qccType, wipFile, commitFile, false);
	}

	public File commitFile() {
		return m_commitFile;
	}

	public boolean isAutoCommit() {
		return m_autoCommit;
	}

	public String qccType() {
		return m_qccType;
	}

	public long serial() {
		return m_serial;
	}

	@Override
	public String toString() {
		return m_commitFile.getName() + (m_autoCommit ? " AUTOCOMMIT" : "");
	}

	public File wipFile() {
		return m_wipFile;
	}

	private ArgonJournalTx(long serial, String qccType, File wip, File commit, boolean autoCommit) {
		assert qccType != null && qccType.length() > 0;
		assert wip != null;
		assert commit != null;
		m_serial = serial;
		m_qccType = qccType;
		m_wipFile = wip;
		m_commitFile = commit;
		m_autoCommit = autoCommit;
	}
	private final long m_serial;
	private final String m_qccType;
	private final File m_wipFile;
	private final File m_commitFile;
	private final boolean m_autoCommit;
}
