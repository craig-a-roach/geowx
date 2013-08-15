/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emboron;

import com.metservice.boron.BoronSpace;
import com.metservice.neon.EmViewObject;
import com.metservice.neon.EsExecutionContext;

/**
 * @author roach
 */
class ProcessSpaceEm extends EmViewObject {

	public BoronSpace bspace() {
		return m_bspace;
	}

	@Override
	public void putProperties(EsExecutionContext ecx)
			throws InterruptedException {
		putViewBoolean(CProp.isWinOS, m_bspace.isWinOS());
	}

	public ProcessSpaceEm(BoronSpace bspace) {
		super(ProcessSpaceEmClass.Instance);
		if (bspace == null) throw new IllegalArgumentException("object is null");
		m_bspace = bspace;
	}

	private final BoronSpace m_bspace;
}
