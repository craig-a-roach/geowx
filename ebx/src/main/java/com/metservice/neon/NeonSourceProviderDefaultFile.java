/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.ArgonQuotaException;
import com.metservice.argon.ArgonStreamReadException;
import com.metservice.argon.ArgonStreamWriteException;
import com.metservice.argon.Binary;
import com.metservice.argon.Ds;
import com.metservice.argon.IArgonFileProbe;
import com.metservice.argon.file.ArgonDirectoryManagement;
import com.metservice.argon.file.ArgonFileManagement;

/**
 * @author roach
 */
public class NeonSourceProviderDefaultFile implements INeonSourceProvider {

	private Binary load(String qccPath)
			throws EsSourceLoadException {
		if (qccPath == null || qccPath.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final IArgonFileProbe oprobe = m_probe.get();
		final File sourceFile = new File(m_cndirHome, qccPath);
		try {
			final Binary oResource = Binary.createFromFile(sourceFile, m_bcQuota);
			if (oResource == null) {
				final String m = "Script file '" + sourceFile + "' not found; it may have been renamed or deleted";
				throw new EsSourceLoadException(m);
			}
			return oResource;
		} catch (final ArgonQuotaException ex) {
			if (oprobe != null) {
				final Ds ds = Ds.triedTo("Load script from directory", ex, EsSourceLoadException.class);
				ds.a("qccPath", qccPath);
				ds.a("dirHome", m_cndirHome);
				oprobe.warnFile(ds, sourceFile);
			}
			throw new EsSourceLoadException("Script file '" + sourceFile + "' is too large to load");
		} catch (final ArgonStreamReadException ex) {
			if (oprobe != null) {
				final Ds ds = Ds.triedTo("Read script stream from directory", ex, EsSourceLoadException.class);
				ds.a("qccPath", qccPath);
				ds.a("dirHome", m_cndirHome);
				oprobe.failFile(ds, sourceFile);
			}
			throw new EsSourceLoadException("Could not read script file '" + sourceFile + "'");
		}
	}

	private void putSource(String qccPath, Binary source, boolean allowOverwrite)
			throws EsSourceSaveException {
		if (qccPath == null || qccPath.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (source == null) throw new IllegalArgumentException("object is null");
		final IArgonFileProbe oprobe = m_probe.get();
		final File destFile = new File(m_cndirHome, qccPath);
		if (!allowOverwrite && destFile.exists()) throw new EsSourceSaveException("File '" + destFile + "' already exists");
		try {
			source.save(destFile, false);
		} catch (final ArgonPermissionException ex) {
			if (oprobe != null) {
				final Ds ds = Ds.triedTo("Save script to directory", ex, EsSourceSaveException.class);
				ds.a("qccPath", qccPath);
				ds.a("dirHome", m_cndirHome);
				oprobe.failFile(ds, destFile);
			}
			throw new EsSourceSaveException("Not permitted to write to '" + destFile + "'");
		} catch (final ArgonStreamWriteException ex) {
			if (oprobe != null) {
				final Ds ds = Ds.triedTo("Save script to directory", ex, EsSourceSaveException.class);
				ds.a("qccPath", qccPath);
				ds.a("dirHome", m_cndirHome);
				oprobe.failFile(ds, destFile);
			}
			throw new EsSourceSaveException("Could not write script file '" + destFile + "'");
		}
	}

	@Override
	public void copy(String qccPathFrom, String qccPathTo)
			throws EsSourceLoadException, EsSourceSaveException {
		if (!qccPathFrom.equals(qccPathTo)) {
			putSource(qccPathTo, load(qccPathFrom), false);
		}
	}

	@Override
	public INeonSourceDescriptor descriptor(String qccPath) {
		if (qccPath == null || qccPath.length() == 0) throw new IllegalArgumentException("string is null or empty");
		return NeonSourceDescriptorFile.newHomeInstance(m_cndirHome, qccPath);
	}

	@Override
	public boolean exists(String qccPath) {
		if (qccPath == null || qccPath.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final File sourceFile = new File(m_cndirHome, qccPath);
		return sourceFile.exists();
	}

	@Override
	public void freshSource(String qccPath, String ozSource)
			throws EsSourceSaveException {
		putSource(qccPath, Binary.newFromStringUTF8(ozSource), false);
	}

	@Override
	public void makeDirectory(String qccPath)
			throws EsSourceSaveException {
		if (qccPath == null || qccPath.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final File destDir = new File(m_cndirHome, qccPath);
		try {
			final File cndir = ArgonDirectoryManagement.cndir(destDir);
			ArgonDirectoryManagement.cndirEnsureWriteable(cndir);
		} catch (final ArgonPermissionException ex) {
			throw new EsSourceSaveException("Not permitted to create '" + destDir + "'");
		}
	}

	@Override
	public File ocndirHome() {
		return m_cndirHome;
	}

	@Override
	public void putSource(String qccPath, String ozSource)
			throws EsSourceSaveException {
		putSource(qccPath, Binary.newFromStringUTF8(ozSource), true);
	}

	@Override
	public void registerProbe(IArgonFileProbe oprobe) {
		m_probe.set(oprobe);
	}

	@Override
	public void remove(String qccPath)
			throws EsSourceSaveException {
		if (qccPath == null || qccPath.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final File destFile = new File(m_cndirHome, qccPath);
		try {
			ArgonFileManagement.deleteFile(destFile);
		} catch (final ArgonPermissionException ex) {
			throw new EsSourceSaveException("Not permitted to remove '" + destFile + "'");
		}
	}

	@Override
	public void removeDirectory(String qccPath)
			throws EsSourceSaveException {
		if (qccPath == null || qccPath.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final File destDir = new File(m_cndirHome, qccPath);
		final boolean removed = ArgonDirectoryManagement.removeSelfOnlyIfEmpty(destDir);
		if (!removed) throw new EsSourceSaveException("Could not remove directory  '" + destDir + "'");
	}

	@Override
	public void rename(String qccPathFrom, String qccPathTo)
			throws EsSourceSaveException {
		if (qccPathFrom == null || qccPathFrom.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (qccPathTo == null || qccPathTo.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final File fromFile = new File(m_cndirHome, qccPathFrom);
		final File toFile = new File(m_cndirHome, qccPathTo);
		try {
			if (!fromFile.equals(toFile)) {
				ArgonFileManagement.renameFile(fromFile, toFile);
			}
		} catch (final ArgonPermissionException ex) {
			throw new EsSourceSaveException("Not permitted to rename '" + fromFile + "' to '" + toFile + "'");
		}
	}

	@Override
	public void renameDirectory(String qccPathFrom, String qccPathTo)
			throws EsSourceSaveException {
		if (qccPathFrom == null || qccPathFrom.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (qccPathTo == null || qccPathTo.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final File fromDir = new File(m_cndirHome, qccPathFrom);
		final File toDir = new File(m_cndirHome, qccPathTo);
		final boolean renamed = ArgonDirectoryManagement.renameDirectoryOnlyIfUnique(fromDir, toDir);
		if (!renamed) throw new EsSourceSaveException("Could not rename directory  '" + fromDir + "' to '" + toDir + "'");
	}

	@Override
	public String source(String qccPath)
			throws EsSourceLoadException {
		return load(qccPath).newStringUTF8();
	}

	@Override
	public List<? extends INeonSourceDescriptor> zlDescriptorsAsc(String zccPath) {
		if (zccPath == null) throw new IllegalArgumentException("object is null");
		final NeonSourceDescriptorFile sdDir = NeonSourceDescriptorFile.newHomeInstance(m_cndirHome, zccPath);
		return sdDir.new_zlSubAsc(true);
	}

	public NeonSourceProviderDefaultFile(File cndirHome) {
		this(cndirHome, CNeon.DefaultQuotaFileSourceBc);
	}

	public NeonSourceProviderDefaultFile(File cndirHome, int bcQuota) {
		if (cndirHome == null) throw new IllegalArgumentException("object is null");
		m_cndirHome = cndirHome;
		m_bcQuota = bcQuota;
	}
	private final AtomicReference<IArgonFileProbe> m_probe = new AtomicReference<IArgonFileProbe>();
	private final File m_cndirHome;
	private final int m_bcQuota;
}
