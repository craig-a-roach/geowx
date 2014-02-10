/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.cache.disk;

import java.io.File;
import java.util.Date;

import com.metservice.argon.Ds;

class MruDescriptor {

	public boolean isFound() {
		return dcu.exists();
	}

	public boolean isFresh(long tsNow) {
		return tsNow < tsExpires;
	}

	public File newRef(File cndir) {
		return new File(cndir, qccFileName);
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("fileName", qccFileName);
		ds.at8("lastModified", oLastModified);
		ds.at8("lastAccess", tsLastAccess);
		ds.at8("expires", tsExpires);
		ds.a("dcu", dcu);
		return ds.s();
	}

	public MruDescriptor(String qccFileName, Date oLastModified, long tsLastAccess, long tsExpires, Dcu dcu) {
		assert qccFileName != null && qccFileName.length() > 0;
		assert dcu != null;
		this.qccFileName = qccFileName;
		this.oLastModified = oLastModified;
		this.tsLastAccess = tsLastAccess;
		this.tsExpires = tsExpires;
		this.dcu = dcu;
	}
	public final String qccFileName;
	public final Date oLastModified;
	public final long tsLastAccess;
	public final long tsExpires;
	public final Dcu dcu;
}