/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
public class KryptonDataBinary2Builder extends Section2Builder {

	@Override
	public int estimatedOctetCount() {
		final int bmc = m_oEmitter == null ? 0 : m_oEmitter.dataByteCount();
		return 5 + bmc;
	}

	@Override
	public void save(Section2Buffer dst) {
		if (m_oEmitter != null) {
			m_oEmitter.saveData(dst);
		}
	}

	@Override
	public int sectionNo() {
		return 7;
	}

	public KryptonDataBinary2Builder(IData2Emitter oEmitter) {
		m_oEmitter = oEmitter;
	}

	private final IData2Emitter m_oEmitter;
}
