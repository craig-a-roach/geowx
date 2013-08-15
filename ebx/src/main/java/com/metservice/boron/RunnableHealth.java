/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

/**
 * @author roach
 */
class RunnableHealth extends ACyclicRunnable {

	@Override
	protected void doCycle(long tsNow) {
		m_monitoringTable.onHealth(tsNow);
	}

	public RunnableHealth(KernelCfg kc, MonitoringTable monitoringTable) {
		super(kc, CBoron.HealthYieldMs);
		if (monitoringTable == null) throw new IllegalArgumentException("object is null");
		m_monitoringTable = monitoringTable;
	}

	private final MonitoringTable m_monitoringTable;
}
