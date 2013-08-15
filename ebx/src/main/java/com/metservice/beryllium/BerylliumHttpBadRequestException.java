/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import com.metservice.argon.ArgonApiException;
import com.metservice.argon.ArgonFormatException;

/**
 * @author roach
 */
public class BerylliumHttpBadRequestException extends Exception {

	public BerylliumHttpBadRequestException(ArgonApiException ex, String pname) {
		super("Invalid '" + pname + "'..." + ex.getMessage());
	}

	public BerylliumHttpBadRequestException(ArgonFormatException ex, String pname) {
		super("Malformed '" + pname + "'..." + ex.getMessage());
	}

	public BerylliumHttpBadRequestException(NumberFormatException ex, String subType, String pname) {
		super("Malformed '" + pname + "'...expecting a " + subType + " numeric value");
	}

	public BerylliumHttpBadRequestException(String message) {
		super(message);
	}

}
