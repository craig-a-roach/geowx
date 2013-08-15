/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.ArrayList;
import java.util.List;

import com.metservice.argon.Ds;
import com.metservice.beryllium.BerylliumSupportId;

/**
 * @author roach
 */
public class EsRequest {

	public void add(IEmInstaller installer) {
		if (installer == null) throw new IllegalArgumentException("installer is null");
		m_zlInstallers.add(installer);
	}

	public void add(IEmInstaller[] zptInstallers) {
		if (zptInstallers == null) throw new IllegalArgumentException("zptInstallers is null");
		for (int i = 0; i < zptInstallers.length; i++) {
			m_zlInstallers.add(zptInstallers[i]);
		}
	}

	public EsCallableEntryPoint getEntryPoint() {
		return m_oEntryPoint;
	}

	public BerylliumSupportId idSupport() {
		return m_idSupport;
	}

	public String qccSourcePath() {
		return m_qccSourcePath;
	}

	public void setEntryPoint(EsCallableEntryPoint oEntryPoint) {
		m_oEntryPoint = oEntryPoint;
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("qccSourcePath", m_qccSourcePath);
		ds.a("idSupport", m_idSupport);
		ds.a("oEntryPoint", m_oEntryPoint);
		ds.a("installers", m_zlInstallers);
		return ds.s();
	}

	public List<IEmInstaller> zlInstallers() {
		return m_zlInstallers;
	}

	public EsRequest(BerylliumSupportId idSupport, String qccSourcePath) {
		if (idSupport == null) throw new IllegalArgumentException("object is null");
		if (qccSourcePath == null || qccSourcePath.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		m_idSupport = idSupport;
		m_qccSourcePath = qccSourcePath;
	}

	private final BerylliumSupportId m_idSupport;
	private final String m_qccSourcePath;
	private final List<IEmInstaller> m_zlInstallers = new ArrayList<IEmInstaller>(4);
	private EsCallableEntryPoint m_oEntryPoint;
}
