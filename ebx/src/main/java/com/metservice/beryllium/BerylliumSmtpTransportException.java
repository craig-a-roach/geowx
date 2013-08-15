/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

/**
 * @author roach
 */
public class BerylliumSmtpTransportException extends Exception {

	public BerylliumSmtpTransportException(String message) {
		super(message);
	}

	public BerylliumSmtpTransportException(String message, Throwable cause) {
		super(message, cause);
	}
}
