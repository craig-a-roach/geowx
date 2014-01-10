/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.file;

import java.io.File;
import java.io.InputStream;

import com.metservice.argon.ArgonDigester;
import com.metservice.argon.ArgonDiskException;
import com.metservice.argon.ArgonNameLock;
import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.ArgonPlatformException;
import com.metservice.argon.ArgonQuotaException;
import com.metservice.argon.ArgonServiceId;
import com.metservice.argon.ArgonStreamReadException;
import com.metservice.argon.ArgonStreamWriteException;
import com.metservice.argon.ArgonTransformer;
import com.metservice.argon.Binary;
import com.metservice.argon.CArgon;
import com.metservice.argon.Ds;
import com.metservice.argon.IArgonFileRunProbe;
import com.metservice.argon.IArgonSpaceId;

/**
 * @author roach
 */
public class ArgonClasspathExtractor {

	public static final String SubDir = "extracts";
	public static final int FileSizeLimit = Integer.MAX_VALUE;

	private static final String TrySave = "Save content to file";
	private static final String TryLoad = "Load content from classpath";

	private static String qccResourceId(Class<?> resourceRef, String qccResourcePath) {
		assert resourceRef != null;
		assert qccResourcePath != null && qccResourcePath.length() > 0;
		return resourceRef.getPackage().getName() + "." + qccResourcePath;
	}

	public static Config newConfig(IArgonFileRunProbe probe, ArgonServiceId sid, IArgonSpaceId idSpace)
			throws ArgonPermissionException {
		if (probe == null) throw new IllegalArgumentException("object is null");
		if (sid == null) throw new IllegalArgumentException("object is null");
		if (idSpace == null) throw new IllegalArgumentException("object is null");
		final String spc = idSpace.format();
		final File cndir = ArgonDirectoryManagement.cndirEnsureUserWriteable(sid.qtwVendor, sid.qtwService, SubDir, spc);
		return new Config(probe, sid, idSpace, cndir);
	}

	public static ArgonClasspathExtractor newInstance(Config cfg)
			throws ArgonPlatformException {
		if (cfg == null) throw new IllegalArgumentException("object is null");
		final ArgonDigester oDigester = cfg.safeNaming ? ArgonDigester.newSHA1() : null;
		if (cfg.clean) {
			ArgonDirectoryManagement.remove(cfg.probe, cfg.cndir, true);
		}
		return new ArgonClasspathExtractor(cfg, oDigester);
	}

	private String failLoad(Throwable ex) {
		return "Failed to load resource content from class path..." + ex.getMessage();
	}

	private String failSave(Throwable ex) {
		return "Failed to save resource content..." + ex.getMessage();
	}

	private Binary getBinaryFromClasspath(File ref, String qccResourcePath, Class<?> resourceRef)
			throws ArgonPlatformException {
		final InputStream oins = resourceRef.getResourceAsStream(qccResourcePath);
		if (oins == null) return null;
		try {
			return Binary.newFromInputStream(oins, m_bcSizeEst, qccResourcePath, FileSizeLimit);
		} catch (final ArgonQuotaException ex) {
			throw new ArgonPlatformException(failLoad(ex));
		} catch (final ArgonStreamReadException ex) {
			probeLoad(resourceRef, ex);
			throw new ArgonPlatformException(failLoad(ex));
		}
	}

	private File newFile(String qccFileName) {
		return new File(m_cndir, qccFileName);
	}

	private void probeLoad(Class<?> resourceRef, Throwable cause) {
		final Ds ds = Ds.triedTo(TryLoad, cause, ArgonPlatformException.class);
		ds.a("resourceRef", resourceRef);
		ds.a("resourceRef.classLoader", resourceRef.getClassLoader());
		m_probe.failSoftware(ds);
	}

	private void probeSave(File dst, Throwable cause, Binary content) {
		final Ds ds = Ds.triedTo(TrySave, cause, ArgonDiskException.class);
		ds.a("byteCount", content.zptReadOnly);
		m_probe.failFile(ds, dst);
	}

	private String qccFileName(String qccResourceId) {
		assert qccResourceId != null && qccResourceId.length() > 0;
		if (m_oDigester == null) return ArgonTransformer.zPosixSanitized(qccResourceId);
		return m_oDigester.digestUTF8B64URL(qccResourceId);
	}

	private void save(File ref, Binary content)
			throws ArgonDiskException {
		assert ref != null;
		assert content != null;
		try {
			content.save(ref, false);
		} catch (final ArgonPermissionException ex) {
			probeSave(ref, ex, content);
			throw new ArgonDiskException(failSave(ex));
		} catch (final ArgonStreamWriteException ex) {
			probeSave(ref, ex, content);
			throw new ArgonDiskException(failSave(ex));
		}
	}

	public File find(Class<?> resourceRef, String qccResourcePath)
			throws ArgonPlatformException, ArgonDiskException, InterruptedException {
		if (resourceRef == null) throw new IllegalArgumentException("object is null");
		if (qccResourcePath == null || qccResourcePath.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		final String qccResourceId = qccResourceId(resourceRef, qccResourcePath);
		final String qccFileName = qccFileName(qccResourceId);
		final File ref = newFile(qccFileName);
		if (ref.exists()) return ref;
		m_lock.lock(qccFileName);
		try {
			if (ref.exists()) return ref;
			final Binary oContent = getBinaryFromClasspath(ref, qccResourcePath, resourceRef);
			if (oContent != null) {
				save(ref, oContent);
			}
			return oContent == null ? null : ref;
		} finally {
			m_lock.unlock(qccFileName);
		}
	}

	private ArgonClasspathExtractor(Config config, ArgonDigester oDigester) {
		assert config != null;
		m_probe = config.probe;
		m_oDigester = oDigester;
		m_cndir = config.cndir;
		m_bcSizeEst = config.bcSizeEst;
	}
	private final IArgonFileRunProbe m_probe;
	private final ArgonDigester m_oDigester;
	private final File m_cndir;
	private final int m_bcSizeEst;
	private final ArgonNameLock m_lock = new ArgonNameLock();

	public static class Config {

		public static final boolean DefaultClean = true;
		public static final boolean DefaultSafeNaming = true;
		public static final int DefaultSizeEst = 64 * CArgon.K;

		public File directory() {
			return cndir;
		}

		public Config directory(String qccPath)
				throws ArgonPermissionException {
			if (qccPath == null || qccPath.length() == 0) throw new IllegalArgumentException("string is null or empty");
			cndir = ArgonDirectoryManagement.cndirEnsureWriteable(qccPath);
			return this;
		}

		public Config enableClean(boolean enable) {
			clean = enable;
			return this;
		}

		public Config enableSafeNaming(boolean enable) {
			safeNaming = enable;
			return this;
		}

		@Override
		public String toString() {
			final Ds ds = Ds.o("ArgonClasspathExtractor.Config");
			ds.a("cleanJAR", clean);
			ds.a("safeNaming", safeNaming);
			ds.a("idService", idService);
			ds.a("idSpace", idSpace);
			return ds.s();
		}

		Config(IArgonFileRunProbe probe, ArgonServiceId sid, IArgonSpaceId idSpace, File cndir) {
			assert probe != null;
			assert sid != null;
			assert idSpace != null;
			assert cndir != null;
			this.probe = probe;
			this.idService = sid;
			this.idSpace = idSpace;
			this.cndir = cndir;
		}
		final IArgonFileRunProbe probe;
		final ArgonServiceId idService;
		final IArgonSpaceId idSpace;
		File cndir;
		boolean clean = DefaultClean;
		boolean safeNaming = DefaultSafeNaming;
		int bcSizeEst = DefaultSizeEst;
	}

}
