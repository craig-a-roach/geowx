/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.cache.disk;

import com.metservice.argon.IArgonFileRunProbe;

/**
 * @author roach
 */
public interface IArgonDiskCacheProbe extends IArgonFileRunProbe {

	boolean isLiveMruManagement();

	boolean isLiveMruRequest();

	void liveMruManagement(String message, Object... args);

	void liveMruRequestHit(String qccResourceId);

	void liveMruRequestMiss(String qccResourceId);
}
