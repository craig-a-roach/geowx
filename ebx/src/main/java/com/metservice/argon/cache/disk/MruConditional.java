/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.cache.disk;

import java.util.Date;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
class MruConditional {

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.at8("lastModified", lastModified);
		ds.a("dcu", dcu);
		return ds.s();
	}

	public MruConditional(Date lastModified, Dcu dcu) {
		assert lastModified != null;
		assert dcu != null;
		this.lastModified = lastModified;
		this.dcu = dcu;
	}
	public final Date lastModified;
	public final Dcu dcu;
}
