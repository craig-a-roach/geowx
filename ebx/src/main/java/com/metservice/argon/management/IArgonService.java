/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.management;

import java.util.concurrent.ExecutorService;

import com.metservice.argon.ArgonPlatformException;

/**
 * @author roach
 */
public interface IArgonService {

	public String name();

	public void serviceEnd()
			throws InterruptedException;

	public void serviceStart(ExecutorService xc)
			throws ArgonPlatformException, InterruptedException;

}
