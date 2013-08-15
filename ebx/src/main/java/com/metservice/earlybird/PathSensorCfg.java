/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.earlybird;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.metservice.argon.Ds;
import com.metservice.argon.text.ArgonSplitter;

/**
 * @author roach
 */
class PathSensorCfg {

	public static PathSensorCfg newInstance(String qtwPaths)
			throws CfgSyntaxException {
		if (qtwPaths == null) throw new IllegalArgumentException("object is null");
		final String[] zptqtwPaths = ArgonSplitter.zptqtwSplit(qtwPaths, ':');
		if (zptqtwPaths.length == 0) {
			final String m = "Empty grid path";
			throw new CfgSyntaxException(m);
		}
		final int pathCount = zptqtwPaths.length;
		final Path[] xptPaths = new Path[pathCount];
		for (int i = 0; i < pathCount; i++) {
			final Path path = Paths.get(zptqtwPaths[i]);
			final Path normal = path.normalize();
			if (normal.getNameCount() == 0) {
				final String m = "Malformed grid path '" + path + "'";
				throw new CfgSyntaxException(m);
			}
			xptPaths[i] = normal;
		}
		return new PathSensorCfg(xptPaths);
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("paths", m_xptPaths);
		return ds.s();
	}

	public Path[] xptPaths() {
		return m_xptPaths;
	}

	private PathSensorCfg(Path[] xptPaths) {
		assert xptPaths != null && xptPaths.length > 0;
		m_xptPaths = xptPaths;
	}

	private final Path[] m_xptPaths;
}
