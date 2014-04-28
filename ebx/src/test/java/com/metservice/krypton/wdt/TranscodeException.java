/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton.wdt;


/**
 * @author roach
 */
class TranscodeException extends Exception {

	public TranscodeException(String message) {
		super(message);
	}

	public TranscodeException(String message, Throwable cause) {
		super(message, cause);
	}

	public TranscodeException(Throwable src, String message) {
		super(src == null ? message : (src.getMessage() + ", " + message));
	}
}
