/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

/**
 * 
 * @author roach
 */
enum BooleanLiteral {
	TRUE, FALSE;

	public static BooleanLiteral find(String qcc) {
		if (qcc == null || qcc.length() == 0) throw new IllegalArgumentException("qcc is empty");
		if (qcc.equals("true")) return TRUE;
		if (qcc.equals("false")) return FALSE;

		return null;
	}
}
