/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emboron;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.metservice.argon.file.ArgonDirectoryManagement;

/**
 * @author roach
 */
public class EmBoronFileSystemHomes {

	public File find(String qccName) {
		if (qccName == null || qccName.length() == 0) throw new IllegalArgumentException("string is null or empty");
		return m_zmcndirHomes.get(qccName);
	}

	public File findSoftware() {
		return m_zmcndirHomes.get(CProp.software);
	}

	public void put(String qccPropertyName, File cndir) {
		if (qccPropertyName == null || qccPropertyName.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		if (cndir == null) throw new IllegalArgumentException("object is null");
		m_zmcndirHomes.put(qccPropertyName, cndir);
	}

	public void putSoftware(File cndir) {
		if (cndir == null) throw new IllegalArgumentException("object is null");
		m_zmcndirHomes.put(CProp.software, cndir);
	}

	@Override
	public String toString() {
		final List<String> zlPropAsc = new ArrayList<String>(m_zmcndirHomes.keySet());
		Collections.sort(zlPropAsc);
		final StringBuilder sb = new StringBuilder();
		for (final String pname : zlPropAsc) {
			if (sb.length() > 0) {
				sb.append('\n');
			}
			sb.append(pname);
			sb.append('=');
			sb.append(m_zmcndirHomes.get(pname).getPath());
		}
		return sb.toString();
	}

	public Map<String, File> zmcndirHomes() {
		return m_zmcndirHomes;
	}

	public EmBoronFileSystemHomes() {
		m_zmcndirHomes.put(CProp.user, ArgonDirectoryManagement.CnDirUserHome);
	}

	private final Map<String, File> m_zmcndirHomes = new HashMap<String, File>(8);
}
