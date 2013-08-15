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
public abstract class EsIntrinsicMethod extends EsProviderCallable {

	public final String show(int depth) {
		return "Intrinsic Method " + qccName();
	}

	protected EsIntrinsicMethod(String qccName) {
		this(qccName, NOARGS, 0);
	}

	protected EsIntrinsicMethod(String qccName, String[] zptFormalParameterNames, int requiredArgumentCount) {
		super(qccName, zptFormalParameterNames, requiredArgumentCount);
	}
}
