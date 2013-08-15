/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

/**
 * @author roach
 */
public class NeonAssuranceContext {

	public IEmInstaller[] zptInstallers() {
		return m_zptInstallers;
	}

	public NeonAssuranceContext(IEmInstaller[] zptInstallers) {
		if (zptInstallers == null) throw new IllegalArgumentException("object is null");
		m_zptInstallers = zptInstallers;
	}
	private final IEmInstaller[] m_zptInstallers;
}
