/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.cache.disk;

import java.io.File;
import java.util.Date;

import com.metservice.argon.Ds;

public class MruDescriptor {

	public File createRef(File cndir) {
		if (!exists) return null;
		return new File(cndir, qccFileName);
	}

	public boolean isFresh(long tsNow) {
		return tsNow < tsExpires;
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("fileName", qccFileName);
		ds.at8("lastModified", oLastModified);
		ds.at8("lastAccess", tsLastAccess);
		ds.at8("expires", tsExpires);
		ds.a("exists", exists);
		return ds.s();
	}

	MruDescriptor(String qccFileName, Date oLastModified, long tsLastAccess, long tsExpires, boolean exists) {
		assert qccFileName != null && qccFileName.length() > 0;
		this.qccFileName = qccFileName;
		this.oLastModified = oLastModified;
		this.tsLastAccess = tsLastAccess;
		this.tsExpires = tsExpires;
		this.exists = exists;
	}
	public final String qccFileName;
	public final Date oLastModified;
	public final long tsLastAccess;
	public final long tsExpires;
	public final boolean exists;
}