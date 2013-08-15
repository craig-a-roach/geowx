/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emboron;

import com.metservice.argon.Ds;
import com.metservice.argon.json.JsonObject;
import com.metservice.neon.EmAbstractInstaller;
import com.metservice.neon.EsExecutionContext;

/**
 * @author roach
 */
public class BoronEmCaller extends EmAbstractInstaller {

	public static final BoronEmCaller None = new BoronEmCaller();

	@Override
	public void install(EsExecutionContext ecx)
			throws InterruptedException {
		if (m_oCaller != null) {
			putView(ecx, CProp.GlobalCaller, m_oCaller);
		} else if (m_ozJsonSpec != null) {
			putViewJson(ecx, CProp.GlobalCaller, m_ozJsonSpec, false);
		}
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("caller.jsonObject", m_oCaller);
		ds.a("caller.jsonSpec", m_ozJsonSpec);
		return ds.s();
	}

	private BoronEmCaller() {
		m_oCaller = null;
		m_ozJsonSpec = null;
	}

	public BoronEmCaller(JsonObject caller) {
		if (caller == null) throw new IllegalArgumentException("object is null");
		m_oCaller = caller;
		m_ozJsonSpec = null;
	}

	public BoronEmCaller(String zJsonSpec) {
		if (zJsonSpec == null) throw new IllegalArgumentException("object is null");
		m_oCaller = null;
		m_ozJsonSpec = zJsonSpec;
	}

	private final JsonObject m_oCaller;
	private final String m_ozJsonSpec;
}
