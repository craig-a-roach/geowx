/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

/**
 * @author roach
 */
public class ArgonStreamWriteException extends Exception {

	public ArgonStreamWriteException(String message) {
		super(message);
	}

	public ArgonStreamWriteException(String message, Throwable cause) {
		super(message, cause);
	}

	public ArgonStreamWriteException(Throwable cause) {
		super(cause);
	}
}
