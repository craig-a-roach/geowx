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
public abstract class EsIntrinsicConstructor extends EsProviderCallable {
	public abstract EsObject declarePrototype(EsGlobal global);

	public final String show(int depth) {
		return "Intrinsic Constructor " + qccName();
	}

	protected static <T> T thisIntrinsicObject(EsExecutionContext ecx, Class<T> intrinsicClass) {
		final EsObject thisObject = ecx.thisObject();
		if (intrinsicClass.isInstance(thisObject)) return intrinsicClass.cast(thisObject);
		throw new EsTypeCodeException("This object (" + thisObject.esClass() + ") is not an intrinsic "
				+ intrinsicClass.getName());
	}

	protected EsIntrinsicConstructor(String qccName, String[] zptFormalParameterNames, int requiredArgumentCount) {
		super(qccName, zptFormalParameterNames, requiredArgumentCount);
	}
}
