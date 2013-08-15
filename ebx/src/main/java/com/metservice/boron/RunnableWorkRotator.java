/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

/**
 * @author roach
 */
class RunnableWorkRotator extends ACyclicRunnable {

	@Override
	protected void doCycle(long now) {
		m_workRotator.trim();
	}

	public RunnableWorkRotator(KernelCfg kc, WorkRotator workRotator) {
		super(kc, CBoron.WorkRotateYieldMs);
		if (workRotator == null) throw new IllegalArgumentException("object is null");
		m_workRotator = workRotator;
	}

	private final WorkRotator m_workRotator;
}
