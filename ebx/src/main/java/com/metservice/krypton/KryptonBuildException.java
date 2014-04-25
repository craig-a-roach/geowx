/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
public class KryptonBuildException extends Exception {

	public KryptonBuildException(KryptonBuildException src, String message) {
		super(src == null ? message : (src.getMessage() + ", " + message));
	}

	public KryptonBuildException(String message) {
		super(message);
	}

}
