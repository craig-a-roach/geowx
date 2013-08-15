/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emboron;

import java.io.File;
import java.util.Map;

import com.metservice.neon.EmViewObject;
import com.metservice.neon.EsExecutionContext;

/**
 * @author roach
 */
class FileSystemEm extends EmViewObject {

	public EmBoronFileSystemHomes fileSystemHomes() {
		return m_fileSystemHomes;
	}

	@Override
	public void putProperties(EsExecutionContext ecx)
			throws InterruptedException {
		final Map<String, File> zmcndirHomes = m_fileSystemHomes.zmcndirHomes();
		for (final Map.Entry<String, File> e : zmcndirHomes.entrySet()) {
			putView(e.getKey(), qccPath(e.getValue()));
		}
	}

	private static String qccPath(File f) {
		assert f != null;
		final String qcc = f.getPath();
		final int len = qcc.length();
		if (len > 1) {
			if (qcc.endsWith("/") || qcc.endsWith("\\")) return qcc.substring(0, len - 1);
		}
		return qcc;
	}

	public FileSystemEm(EmBoronFileSystemHomes fileSystemHomes) {
		super(FileSystemEmClass.Instance);
		if (fileSystemHomes == null) throw new IllegalArgumentException("object is null");
		m_fileSystemHomes = fileSystemHomes;
	}
	private final EmBoronFileSystemHomes m_fileSystemHomes;
}
