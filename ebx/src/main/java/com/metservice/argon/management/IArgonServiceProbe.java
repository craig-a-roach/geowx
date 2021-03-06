/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.management;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
public interface IArgonServiceProbe {

	public void infoShutdown(String message);

	public void infoStartup(String message);

	public void warnShutdown(Ds diagnostic);
}
