/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
public class BoronPlatformException extends BoronException {

	BoronPlatformException(Ds diagnostic) {
		super(diagnostic);
	}

	BoronPlatformException(String message) {
		super(message);
	}
}
