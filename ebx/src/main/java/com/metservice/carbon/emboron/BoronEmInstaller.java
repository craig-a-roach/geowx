/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emboron;

import com.metservice.argon.Ds;
import com.metservice.boron.BoronSpace;
import com.metservice.neon.EmAbstractInstaller;
import com.metservice.neon.EsExecutionContext;

/**
 * @author roach
 */
public class BoronEmInstaller extends EmAbstractInstaller {

	@Override
	public void install(EsExecutionContext ecx)
			throws InterruptedException {
		putView(ecx, CProp.GlobalProcessSpace, m_emProcessSpace);
		putView(ecx, CProp.GlobalFileSystem, m_emFileSystem);
		m_caller.install(ecx);
		putClass(ecx, ScriptEmClass.Constructor);
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("processSpace", m_emProcessSpace);
		ds.a("fileSystem", m_emFileSystem);
		ds.a("caller", m_caller);
		return ds.s();
	}

	public BoronEmInstaller(BoronSpace bspace, EmBoronFileSystemHomes bhomes) {
		this(bspace, bhomes, BoronEmCaller.None);
	}

	public BoronEmInstaller(BoronSpace bspace, EmBoronFileSystemHomes bhomes, BoronEmCaller caller) {
		if (bspace == null) throw new IllegalArgumentException("object is null");
		if (bhomes == null) throw new IllegalArgumentException("object is null");
		m_emProcessSpace = new ProcessSpaceEm(bspace);
		m_emFileSystem = new FileSystemEm(bhomes);
		m_caller = caller;
	}

	private final ProcessSpaceEm m_emProcessSpace;
	private final FileSystemEm m_emFileSystem;
	private final BoronEmCaller m_caller;
}
