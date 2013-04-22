/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
class Jpeg2000PackingSpec {

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("compressionMethod", compressionMethod);
		ds.a("compressionRatio", compressionRatio);
		return ds.s();
	}

	public Jpeg2000PackingSpec(int compressionMethod, int compressionRatio) {
		this.compressionMethod = compressionMethod;
		this.compressionRatio = compressionRatio;
	}
	public final int compressionMethod;
	public final int compressionRatio;
}
