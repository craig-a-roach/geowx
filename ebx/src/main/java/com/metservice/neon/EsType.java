/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

/**
 * @see ECMA 8
 * @author roach
 */
public enum EsType {
	TUndefined(true, false, false, false, false),
	TNull(true, true, false, false, false),
	TBoolean(true, true, true, true, false),
	TString(true, true, true, true, false),
	TNumber(true, true, true, true, false),
	TObject(true, true, true, false, true),
	TReference(false, false, false, false, false),
	TList(false, false, false, false, false);

	private EsType(boolean isPublished, boolean isDefined, boolean isDatum, boolean isPrimitiveDatum, boolean isObject) {
		this.isPublished = isPublished;
		this.isDefined = isDefined;
		this.isDatum = isDatum;
		this.isPrimitiveDatum = isPrimitiveDatum;
		this.isObject = isObject;
	}

	public final boolean isPublished;
	public final boolean isDefined;
	public final boolean isDatum;
	public final boolean isPrimitiveDatum;
	public final boolean isObject;
}
