/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.file;

import java.nio.ByteBuffer;

/**
 * @author roach
 */
class ThreadLocalBufferPool extends ThreadLocal<ByteBuffer> {

	@Override
	protected ByteBuffer initialValue() {
		if (m_direct) return ByteBuffer.allocateDirect(m_bcCapacity);
		return ByteBuffer.allocate(m_bcCapacity);
	}

	ThreadLocalBufferPool(int bcCapacity, boolean direct) {
		m_bcCapacity = Math.max(8, bcCapacity);
		m_direct = direct;
	}
	private final int m_bcCapacity;
	private final boolean m_direct;
}
