/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import java.io.IOException;

/**
 * @author roach
 */
public class KryptonReadException extends Exception {

	public KryptonReadException(String part, long biMsgReFile, long biPosReMsg, IOException cause) {
		super("GRIB '" + part + "' read failure at " + biPosReMsg + " bytes into message starting at " + biMsgReFile, cause);
	}
}
