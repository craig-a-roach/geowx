/*
 * Copyright 2009 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

/**
 * @author roach
 */
public interface INeonLogger {
	public void failure(String message);

	public void information(String message);

	public void live(String message);

	public void warning(String message);
}
