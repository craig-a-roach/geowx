/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

/**
 * @author roach
 */
public class BoronProductManagementFailure implements IBoronProduct {

	@Override
	public boolean isTerminal() {
		return true;
	}

	@Override
	public String toString() {
		return "MANAGEMENT-FAILURE";
	}

	private BoronProductManagementFailure() {
	}

	public static final BoronProductManagementFailure Instance = new BoronProductManagementFailure();
}
