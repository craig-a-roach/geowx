/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.cache.disk;

import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;

/**
 * @author roach
 */
class DiskMruTable {

	static final String p_fileName = "fn";
	static final String p_lastAccess = "la";
	static final String p_lastModified = "lm";
	static final String p_fileKB = "kb";
	static final String p_contentType = "ct";

	public DiskMruTable(long bcCacheSizeQuota, int cacheFileLimit) {
		m_bcCacheSizeQuota = bcCacheSizeQuota;
		m_cacheFileLimit = cacheFileLimit;
	}
	private final long m_bcCacheSizeQuota;
	private final int m_cacheFileLimit;

	private static class Descriptor {

		public void registerAccess(long tsNow) {
			if (tsNow > m_tsLastAccess) {
				m_tsLastAccess = tsNow;
			}
		}

		public void registerReload(long tsLastModified, int kbFile, String qlcContentType) {
			m_tsLastModified = tsLastModified;
			m_kbFile = kbFile;
			m_qlcContentType = qlcContentType;
		}

		public void save(JsonObject dst) {
			dst.putTime(p_lastAccess, m_tsLastAccess);
			dst.putInteger(p_fileKB, m_kbFile);
			dst.putTime(p_lastModified, m_tsLastModified);
			dst.putString(p_contentType, m_qlcContentType);
		}

		public Descriptor(JsonObject src) throws JsonSchemaException {
			if (src == null) throw new IllegalArgumentException("object is null");
			m_tsLastAccess = src.accessor(p_lastAccess).datumTs();
			m_kbFile = src.accessor(p_fileKB).datumInteger();
			m_tsLastModified = src.accessor(p_lastModified).datumTs();
			m_qlcContentType = src.accessor(p_contentType).datumQtwString();
		}

		public Descriptor(long tsLastModified, int kbFile, String qlcContentType, long tsNow) {
			m_tsLastAccess = tsNow;
			m_kbFile = kbFile;
			m_tsLastModified = tsLastModified;
			m_qlcContentType = qlcContentType;
		}

		private long m_tsLastAccess;
		private int m_kbFile;
		private long m_tsLastModified;
		private String m_qlcContentType;
	}

}
