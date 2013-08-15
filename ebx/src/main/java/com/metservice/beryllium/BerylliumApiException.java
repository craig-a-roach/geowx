/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

/**
 * @author roach
 */
public class BerylliumApiException extends Exception {

	public BerylliumApiException(String message) {
		super(message);
	}

	public BerylliumApiException(String message, Throwable cause) {
		super(message, cause);
	}

	public BerylliumApiException(Throwable cause) {
		super(cause);
	}
}
