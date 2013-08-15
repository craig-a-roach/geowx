/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.earlybird;

import com.metservice.argon.ArgonArgsException;
import com.metservice.argon.ArgonFormatException;

/**
 * @author roach
 */
class CfgSyntaxException extends SpaceException {

	public CfgSyntaxException(ArgonArgsException diagnostic) {
		super(diagnostic.getMessage());
	}

	public CfgSyntaxException(ArgonFormatException diagnostic) {
		super(diagnostic.getMessage());
	}

	public CfgSyntaxException(String diagnostic) {
		super(diagnostic);
	}

}
