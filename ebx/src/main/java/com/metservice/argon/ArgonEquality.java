/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

/**
 * @author roach
 */
public class ArgonEquality {

	public static boolean equalsNull(Object olhs, Object orhs) {
		if (olhs == null && orhs == null) return true;
		if (olhs == null || orhs == null) return false;
		return olhs.equals(orhs);
	}

	private ArgonEquality() {
	}
}
