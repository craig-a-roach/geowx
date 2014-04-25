/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
abstract class Section2Builder {

	public int estimatedOctetCount() {
		return 256;
	}

	public final Section2Buffer newBuffer()
			throws KryptonBuildException {
		final int sectionNo = sectionNo();
		final int estimatedOctetCount = estimatedOctetCount();
		final Section2Buffer buffer = new Section2Buffer(sectionNo, estimatedOctetCount);
		save(buffer);
		return buffer;
	}

	public abstract void save(Section2Buffer dst)
			throws KryptonBuildException;

	public abstract int sectionNo();

}
