/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.metservice.argon.ArgonQuotaException;
import com.metservice.argon.ArgonSplitter;
import com.metservice.argon.ArgonStreamReadException;
import com.metservice.argon.ArgonText;
import com.metservice.argon.Binary;
import com.metservice.argon.CArgon;

/**
 * @author roach
 */
abstract class ResourceFolderDecoder<T extends IResourceTable> {

	private static final int QuotaBc = 16 * CArgon.M;

	private T loadResourceTableLk(String qccTableKey)
			throws KryptonTableException {
		assert qccTableKey != null && qccTableKey.length() > 0;
		final T oExResourceTable = m_mapResource.get(qccTableKey);
		if (oExResourceTable != null) return oExResourceTable;
		final boolean reject = m_blackResourceKeys.contains(qccTableKey);
		if (reject) throw new KryptonTableException(m_qccType, qccTableKey, "not available");
		final String qccResourceFileName = qccTableKey + m_qccFileSuffix;
		boolean blacklist = true;
		try {
			Binary oResource = null;
			final File oFile = m_mapFile.get(qccResourceFileName);
			if (oFile == null) {
				final String qccPath = m_qccFolderPath + qccResourceFileName;
				oResource = Binary.createFromClassPath(getClass(), qccPath, QuotaBc);
				if (oResource == null) {
					probe.resourceNotFound(m_qccType, qccPath);
				}
			} else {
				oResource = Binary.createFromFile(oFile, QuotaBc);
				if (oResource == null) {
					probe.resourceNotFound(m_qccType, oFile.getPath());
				}
			}
			if (oResource == null) throw new KryptonTableException(m_qccType, qccTableKey, "not found");
			final T neoTable = newTable(qccTableKey);
			final boolean parsed = parseTableSource(neoTable, oResource);
			if (!parsed) throw new KryptonTableException(m_qccType, qccTableKey, "malformed");
			blacklist = false;
			m_mapResource.put(qccTableKey, neoTable);
			return neoTable;
		} catch (final ArgonQuotaException ex) {
			probe.resourceQuota(m_qccType, qccTableKey, ex);
			throw new KryptonTableException(m_qccType, qccTableKey, "exceeds size quota");
		} catch (final ArgonStreamReadException ex) {
			probe.resourceRead(m_qccType, qccTableKey, ex);
			throw new KryptonTableException(m_qccType, qccTableKey, "read error");
		} finally {
			if (blacklist) {
				m_blackResourceKeys.add(qccTableKey);
			}
		}
	}

	abstract T newTable(String qccTableKey);

	abstract boolean parseTableSource(T target, Binary source);

	protected final T loadResourceTable(String qccTableKey)
			throws KryptonTableException {
		m_lock.lock();
		try {
			return loadResourceTableLk(qccTableKey);
		} finally {
			m_lock.unlock();
		}
	}

	protected final boolean parseDelimitedTableSource(IDescriptiveResourceTable<T> target, Binary source) {
		if (target == null) throw new IllegalArgumentException("object is null");
		if (source == null) throw new IllegalArgumentException("object is null");
		final String zLines = source.newStringUTF8();
		final String[] zptqtwLines = ArgonSplitter.zptzLines(zLines, true, false);
		int rejected = 0;
		for (int i = 0; i < zptqtwLines.length; i++) {
			final String qtwLine = zptqtwLines[i];
			final String[] zptqtwParts = ArgonSplitter.zptqtwSplit(qtwLine, '|');
			if (zptqtwParts.length < 2) {
				final String m = "Missing fields (2 expected) at line index " + i + "; '" + qtwLine + "'";
				probe.resourceParse(m_qccType, target.qccKey(), m);
				rejected++;
			} else {
				final String qtwCodeRange = zptqtwParts[0];
				final String qtwDesc = zptqtwParts[1];
				final String[] zptqtwCodeRangeParts = ArgonSplitter.zptqtwSplit(qtwCodeRange, '-');
				final int rangePartCount = zptqtwCodeRangeParts.length;
				if (rangePartCount == 0 || rangePartCount > 2) {
					final String m = "Malformed code range '" + qtwCodeRange + "' at line index " + i + "; '" + qtwLine
							+ "'";
					probe.resourceParse(m_qccType, target.qccKey(), m);
					rejected++;
				} else {
					final int codeLo = ArgonText.parse(zptqtwCodeRangeParts[0], -1);
					final int codeHi = rangePartCount == 1 ? codeLo : ArgonText.parse(zptqtwCodeRangeParts[1], -1);
					if (codeLo < 0 || codeHi < 0) {
						final String m = "Malformed code range '" + qtwCodeRange + "' at line index " + i + "; '"
								+ qtwLine + "'";
						probe.resourceParse(m_qccType, target.qccKey(), m);
						rejected++;
					} else {
						for (int code = codeLo; code <= codeHi; code++) {
							target.putDescription(code, qtwDesc);
						}
					}
				}
			}
		}
		return rejected == 0;
	}

	public void setFileMap(Map<String, File> fileNameMap) {
		if (fileNameMap == null) throw new IllegalArgumentException("object is null");
		m_lock.lock();
		try {
			m_mapFile = fileNameMap;
		} finally {
			m_lock.unlock();
		}
	}

	protected ResourceFolderDecoder(IKryptonProbe probe, String qccType, String qccFolderPath, String qccFileSuffix) {
		if (probe == null) throw new IllegalArgumentException("object is null");
		if (qccType == null || qccType.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (qccFolderPath == null || qccFolderPath.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		if (qccFileSuffix == null || qccFileSuffix.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		this.probe = probe;
		m_qccType = qccType;
		m_qccFolderPath = qccFolderPath;
		m_qccFileSuffix = qccFileSuffix;
	}

	protected final IKryptonProbe probe;
	private final String m_qccType;
	private final String m_qccFolderPath;
	private final String m_qccFileSuffix;
	private final Lock m_lock = new ReentrantLock();
	private final Set<String> m_blackResourceKeys = new HashSet<String>();
	private final Map<String, T> m_mapResource = new HashMap<String, T>();
	private Map<String, File> m_mapFile = Collections.emptyMap();
}
