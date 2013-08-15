/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.json.IJsonObject;

/**
 * @author roach
 */
public class EsIntrinsicObjectBuilder {

	public static EsIntrinsicObjectBuilder newInstance(EsExecutionContext ecx) {
		if (ecx == null) throw new IllegalArgumentException("object is null");
		final EsIntrinsicObject neo = ecx.global().newIntrinsicObject();
		return new EsIntrinsicObjectBuilder(ecx, neo);
	}

	public static EsIntrinsicObjectBuilder newInstance(EsExecutionContext ecx, IJsonObject src) {
		final EsIntrinsicObject neo = JsonTranscoder.newIntrinsicObject(ecx, src, false);
		return new EsIntrinsicObjectBuilder(ecx, neo);
	}

	private EsIntrinsicObjectBuilder(EsExecutionContext ecx, EsIntrinsicObject target) {
		assert ecx != null;
		assert target != null;
		this.ecx = ecx;
		this.target = target;
	}
	public final EsExecutionContext ecx;
	public final EsIntrinsicObject target;
}
