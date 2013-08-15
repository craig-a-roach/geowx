/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emboron;

import com.metservice.boron.BoronProductIterator;
import com.metservice.neon.EmViewObject;
import com.metservice.neon.EsExecutionContext;

/**
 * @author roach
 */
class ProcessProductIteratorEm extends EmViewObject {

	public BoronProductIterator biterator() {
		return m_biterator;
	}

	@Override
	public void putProperties(EsExecutionContext ecx)
			throws InterruptedException {
	}

	public ProcessProductIteratorEm(BoronProductIterator biterator) {
		super(ProcessProductIteratorEmClass.Instance);
		m_biterator = biterator;
	}

	private final BoronProductIterator m_biterator;
}
