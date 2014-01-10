/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium.mirror;

import com.metservice.argon.ArgonNumber;
import com.metservice.argon.IArgonSpaceId;

class SpaceId implements IArgonSpaceId {

	@Override
	public String format() {
		return m_qId;
	}

	@Override
	public String toString() {
		return m_qId;
	}

	public SpaceId(int listenPort) {
		m_qId = ArgonNumber.intToDec(listenPort, 5);
	}

	private final String m_qId;
}