/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.cache.disk;

import java.util.Date;

/**
 * @author roach
 */
public class ArgonDiskCacheClasspathRequest implements IArgonDiskCacheRequest {

	@Override
	public String getContentType() {
		return m_oqContentType;
	}

	@Override
	public Date getValidator() {
		return null;
	}

	@Override
	public boolean isValid(Date validator) {
		return true;
	}

	@Override
	public String qccResourceId() {
		return m_resourceRef.getName() + "/" + m_qccResourcePath;
	}

	public String qccResourcePath() {
		return m_qccResourcePath;
	}

	public Class<?> resourceRef() {
		return m_resourceRef;
	}

	public ArgonDiskCacheClasspathRequest(Class<?> resourceRef, String qccResourcePath) {
		this(resourceRef, qccResourcePath, null);
	}

	public ArgonDiskCacheClasspathRequest(Class<?> resourceRef, String qccResourcePath, String oqContentType) {
		if (resourceRef == null) throw new IllegalArgumentException("object is null");
		if (qccResourcePath == null || qccResourcePath.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		m_resourceRef = resourceRef;
		m_qccResourcePath = qccResourcePath;
		m_oqContentType = oqContentType;
	}
	private final Class<?> m_resourceRef;
	private final String m_qccResourcePath;
	private final String m_oqContentType;
}
