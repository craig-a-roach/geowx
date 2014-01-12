/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.cache.disk;

import java.util.Date;

import com.metservice.argon.cache.ArgonCacheException;

/**
 * @author roach
 */
public interface IArgonDiskCacheSupplier<R extends IArgonDiskCacheRequest> {

	public IArgonDiskCacheable getCacheable(R request)
			throws ArgonCacheException, InterruptedException;

	public IArgonDiskCacheable getCacheableConditional(R request, Date lastModified)
			throws ArgonCacheException, InterruptedException;

	public Date getNow();
}
