/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

/**
 * @author roach
 */
public class BerylliumFileLoader {

	public void handle(BerylliumPath path, Request rq, HttpServletResponse rp)
			throws IOException {
		if (path == null) throw new IllegalArgumentException("object is null");
		if (rq == null) throw new IllegalArgumentException("object is null");
		if (rp == null) throw new IllegalArgumentException("object is null");
		if (path.depth == 0) {
			rp.sendError(HttpServletResponse.SC_BAD_REQUEST, rq.getPathInfo());
		} else {
			final BerylliumPath rpath = path.relative();
			final BerylliumPathMime pathMime = m_mimeTable.mimeTypeByExtension(rpath);
			final File srcFile = new File(m_cndirHome, rpath.qtwPath());
			BerylliumIO.writeStream(rq, rp, pathMime, srcFile);
		}
	}

	public BerylliumFileLoader(File cndirHome) {
		if (cndirHome == null) throw new IllegalArgumentException("object is null");
		m_cndirHome = cndirHome;
		m_mimeTable = new BerylliumMimeTypeTable();
	}

	private final File m_cndirHome;
	private final BerylliumMimeTypeTable m_mimeTable;
}
