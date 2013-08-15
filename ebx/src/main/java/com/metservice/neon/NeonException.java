/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
public abstract class NeonException extends Exception {

	protected NeonException(Ds ds) {
		super(ds == null ? "" : ds.s());
	}

	protected NeonException(String message) {
		super(message);
	}

}
