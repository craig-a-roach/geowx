/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.cache.disk;

import java.io.InputStream;
import java.util.Date;

import com.metservice.argon.ArgonQuotaException;
import com.metservice.argon.ArgonStreamReadException;
import com.metservice.argon.ArgonText;
import com.metservice.argon.Binary;
import com.metservice.argon.cache.ArgonCacheException;

/**
 * @author roach
 */
public class ArgonDiskCacheClasspathSupplier implements IArgonDiskCacheSupplier<ArgonDiskCacheClasspathRequest> {

	public static final String DefaultContentType = "application/octet-stream";

	private String failLoad(Class<?> resourceRef, Throwable ex) {
		final String cp = resourceRef.getName();
		return "Failed to load resource from classpath " + cp + "..." + ex.getMessage();
	}

	@Override
	public IArgonDiskCacheable find(ArgonDiskCacheClasspathRequest request)
			throws ArgonCacheException {
		if (request == null) throw new IllegalArgumentException("object is null");
		final Class<?> resourceRef = request.resourceRef();
		if (resourceRef == null) throw new IllegalStateException("resource ref not implemented");
		final String qccResourcePath = request.qccResourcePath();
		if (qccResourcePath == null) throw new IllegalStateException("resource path not implemented");
		final String oqtwRequestContentType = ArgonText.oqtw(request.getContentType());
		final String qtwContentType = oqtwRequestContentType == null ? m_qtwDefaultContentType : oqtwRequestContentType;
		final InputStream oins = resourceRef.getResourceAsStream(qccResourcePath);
		if (oins == null) return null;
		try {
			final Binary src = Binary.newFromInputStream(oins, m_bcSizeEst, qccResourcePath, m_bcSizeQuota);
			return new Cacheable(src, qtwContentType);
		} catch (final ArgonQuotaException ex) {
			throw new ArgonCacheException(failLoad(resourceRef, ex));
		} catch (final ArgonStreamReadException ex) {
			throw new ArgonCacheException(failLoad(resourceRef, ex));
		}
	}

	public ArgonDiskCacheClasspathSupplier(int bcSizeQuota, int bcSizeEst) {
		this(bcSizeQuota, bcSizeEst, null);
	}

	public ArgonDiskCacheClasspathSupplier(int bcSizeQuota, int bcSizeEst, String oqDefaultContentType) {
		m_bcSizeQuota = bcSizeQuota;
		m_bcSizeEst = bcSizeEst;
		final String oqtwDefaultContentType = ArgonText.oqtw(oqDefaultContentType);
		final String qtwDefaultContentType = oqtwDefaultContentType == null ? DefaultContentType : oqtwDefaultContentType;
		m_qtwDefaultContentType = qtwDefaultContentType;
	}
	private final int m_bcSizeQuota;
	private final int m_bcSizeEst;
	private final String m_qtwDefaultContentType;

	private static class Cacheable implements IArgonDiskCacheable {

		@Override
		public Binary getContent() {
			return m_src;
		}

		@Override
		public Date getLastModified() {
			return null;
		}

		@Override
		public String qlcContentType() {
			return m_qlcContentType;
		}

		public Cacheable(Binary src, String qtwContentType) {
			assert qtwContentType != null && qtwContentType.length() > 0;
			m_src = src;
			m_qlcContentType = qtwContentType.toLowerCase();
		}
		private final Binary m_src;
		private final String m_qlcContentType;
	}
}
