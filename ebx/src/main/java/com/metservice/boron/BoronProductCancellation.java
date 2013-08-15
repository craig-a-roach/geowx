/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

/**
 * @author roach
 */
public class BoronProductCancellation implements IBoronProduct {

	@Override
	public boolean isTerminal() {
		return true;
	}

	@Override
	public String toString() {
		return "CANCELLATION";
	}

	private BoronProductCancellation() {
	}

	public static final BoronProductCancellation Instance = new BoronProductCancellation();
}
