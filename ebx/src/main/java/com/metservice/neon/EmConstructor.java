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
public abstract class EmConstructor extends EsProviderCallable {

	public abstract EmObject declarePrototype();

	public final String show(int depth) {
		return "Model Constructor " + qccName();
	}

	public EmConstructor(String qccName) {
		super(qccName, NOARGS, 0);
	}

	public EmConstructor(String qccName, int requiredArgumentCount, String... zptFormalParameterNames) {
		super(qccName, zptFormalParameterNames, requiredArgumentCount);
	}
}
