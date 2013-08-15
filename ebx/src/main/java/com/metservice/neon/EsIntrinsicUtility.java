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
public abstract class EsIntrinsicUtility extends EsProviderCallable {

	public final String show(int depth) {
		return "Intrinsic Utility " + qccName();
	}

	protected EsIntrinsicUtility(String qccName, String[] zptFormalParameterNames, int requiredArgumentCount) {
		super(qccName, zptFormalParameterNames, requiredArgumentCount);
	}

	public static final EsIntrinsicUtility Void = new EsIntrinsicUtility("Void", NOARGS, 0) {
		@Override
		protected IEsOperand eval(EsExecutionContext ecx) {
			return null;
		}
	};
}
