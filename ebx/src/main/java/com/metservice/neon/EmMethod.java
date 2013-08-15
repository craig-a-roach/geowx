/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.text.ArgonJoiner;

/**
 * 
 * @author roach
 */
public abstract class EmMethod extends EsProviderCallable {

	public final String show(int depth) {
		return "Model Method " + qccName();
	}

	public EmMethod(String qccName) {
		super(qccName, NOARGS, 0);
	}

	public EmMethod(String qccName, int requiredArgumentCount, String... zptFormalParameterNames) {
		super(qccName, zptFormalParameterNames, requiredArgumentCount);
	}

	public EmMethod(String qccName, int requiredArgumentCount, String[] ozptFormalParameterNames0,
			String... ozptFormalParameterNames1) {
		super(qccName, ArgonJoiner.zptAppend(ozptFormalParameterNames0, ozptFormalParameterNames1), requiredArgumentCount);
	}

	public EmMethod(String qccName, int requiredArgumentCount, String[] ozptFormalParameterNames0,
			String[] ozptFormalParameterNames1, String... ozptFormalParameterNames2) {
		super(qccName, ArgonJoiner.zptAppend(ozptFormalParameterNames0, ozptFormalParameterNames1, ozptFormalParameterNames2),
				requiredArgumentCount);
	}

	protected static final String StdName_toString = EsObject.MethodName_toString;
	protected static final String StdName_valueOf = EsObject.MethodName_valueOf;
	protected static final String StdName_equals = EsObject.MethodName_equals;

	protected static final String[] StdArgs_rhs = { "rhs" };
}
