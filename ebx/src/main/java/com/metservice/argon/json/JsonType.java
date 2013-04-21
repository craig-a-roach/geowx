/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.json;

/**
 * @author roach
 */
public enum JsonType {
	TNull("Null"),
	TBoolean("Boolean"),
	TString("String"),
	TNumberDouble("Number:Double"),
	TNumberElapsed("Number:Elapsed"),
	TNumberInteger("Number:Integer"),
	TNumberTime("Number:Time"),
	TArray("Array"),
	TObject("Object"),
	TBinary("Binary");

	private JsonType(String title) {
		assert title != null && title.length() > 0;
		this.title = title;
	}

	public final String title;

}
