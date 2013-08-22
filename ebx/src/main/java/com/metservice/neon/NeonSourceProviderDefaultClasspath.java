/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.ArgonQuotaException;
import com.metservice.argon.ArgonSplitter;
import com.metservice.argon.ArgonStreamReadException;
import com.metservice.argon.ArgonStreamWriteException;
import com.metservice.argon.Binary;
import com.metservice.argon.Ds;
import com.metservice.argon.IArgonFileProbe;

/**
 * @author roach
 */
public class NeonSourceProviderDefaultClasspath implements INeonSourceProvider {

	private static final String IndexResource = "NeonIndex.txt";
	private static final String Unsupported = "Classpath provider does not support this operation";

	private String qccIndexResourcePath(String zccPath) {
		assert zccPath != null;
		final StringBuilder b = new StringBuilder();
		b.append(zccPath);
		if (zccPath.length() > 0 && !zccPath.endsWith("/")) {
			b.append('/');
		}
		b.append(IndexResource);
		return b.toString();
	}

	@Override
	public void copy(String qccPathFrom, String qccPathTo)
			throws EsSourceSaveException {
		throw new EsSourceSaveException(Unsupported);
	}

	@Override
	public INeonSourceDescriptor descriptor(String qccPath) {
		if (qccPath == null || qccPath.length() == 0) throw new IllegalArgumentException("string is null or empty");
		NeonSourceDescriptorType type = NeonSourceDescriptorType.newInstance(qccPath);
		if (type != NeonSourceDescriptorType.EcmaScript) {
			final String qccResource = qccIndexResourcePath(qccPath);
			final InputStream oIn = m_resourceBase.getResourceAsStream(qccResource);
			if (oIn != null) {
				type = NeonSourceDescriptorType.Container;
			}
		}
		return new Descriptor(type, qccPath);
	}

	@Override
	public boolean exists(String qccPath) {
		if (qccPath == null || qccPath.length() == 0) throw new IllegalArgumentException("string is null or empty");
		InputStream oIn = null;
		try {
			oIn = m_resourceBase.getResourceAsStream(qccPath);
			return oIn != null;
		} finally {
			if (oIn != null) {
				try {
					oIn.close();
				} catch (final IOException ex) {
				}
			}
		}
	}

	@Override
	public void freshSource(String qccPath, String ozSource)
			throws EsSourceSaveException {
		throw new EsSourceSaveException(Unsupported);
	}

	@Override
	public void makeDirectory(String qccPath)
			throws EsSourceSaveException {
		throw new EsSourceSaveException(Unsupported);
	}

	@Override
	public File ocndirHome() {
		try {
			final URL oIndex = m_resourceBase.getResource(IndexResource);
			final String ozFilePath = oIndex == null ? null : oIndex.toURI().getPath();
			if (ozFilePath != null && ozFilePath.length() > 0) {
				final File indexFile = new File(ozFilePath);
				return indexFile.getParentFile();
			}
		} catch (final URISyntaxException ex) {
		}
		return null;
	}

	@Override
	public void putSource(String qccPath, String ozSource)
			throws EsSourceSaveException {
		if (qccPath == null || qccPath.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final URL oResource = m_resourceBase.getResource(qccPath);
		if (oResource == null) {
			final String m = "Classpath resource " + qccPath + "' does not already exist; provider only supports update";
			throw new EsSourceSaveException(m);
		}
		final String ozFileName = oResource.getFile();
		if (ozFileName == null || ozFileName.length() == 0) {
			final String m = "Cannot resolve URL '" + oResource + "' to a file name";
			throw new EsSourceSaveException(m);
		}
		final File destFile = new File(ozFileName);
		final Binary binary = Binary.newFromStringUTF8(ozSource);
		try {
			binary.save(destFile, false);
		} catch (final ArgonPermissionException ex) {
			final Ds ds = Ds.triedTo("Save script to class path", ex, EsSourceSaveException.class);
			ds.a("qccPath", qccPath);
			ds.a("resourceBase", m_resourceBase.getName());
			System.err.println(ds);
			throw new EsSourceSaveException(ds.s());
		} catch (final ArgonStreamWriteException ex) {
			final Ds ds = Ds.triedTo("Save script to class path", ex, EsSourceSaveException.class);
			ds.a("qccPath", qccPath);
			ds.a("resourceBase", m_resourceBase.getName());
			System.err.println(ds);
			throw new EsSourceSaveException(ds.s());
		}
	}

	@Override
	public void registerProbe(IArgonFileProbe probe) {
	}

	@Override
	public void remove(String qccPath)
			throws EsSourceSaveException {
		throw new EsSourceSaveException(Unsupported);
	}

	@Override
	public void removeDirectory(String qccPath)
			throws EsSourceSaveException {
		throw new EsSourceSaveException(Unsupported);
	}

	@Override
	public void rename(String qccPathFrom, String qccPathTo)
			throws EsSourceSaveException {
		throw new EsSourceSaveException(Unsupported);
	}

	@Override
	public void renameDirectory(String qccPathFrom, String qccPathTo)
			throws EsSourceSaveException {
		throw new EsSourceSaveException(Unsupported);
	}

	@Override
	public String source(String qccPath)
			throws EsSourceLoadException {
		if (qccPath == null || qccPath.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final InputStream oIn = m_resourceBase.getResourceAsStream(qccPath);
		if (oIn == null) {
			final String m = "Script file '" + qccPath + "' not found";
			throw new EsSourceLoadException(m);
		}
		try {
			final Binary resource = Binary.newFromInputStream(oIn, 0L, qccPath, CNeon.QuotaClasspathSourceBc);
			return resource.newStringUTF8();
		} catch (final ArgonQuotaException ex) {
			final Ds ds = Ds.triedTo("Load script from class path", ex, EsSourceLoadException.class);
			ds.a("qccPath", qccPath);
			ds.a("resourceBase", m_resourceBase.getName());
			System.err.println(ds);
			throw new EsSourceLoadException(ds.s());
		} catch (final ArgonStreamReadException ex) {
			final Ds ds = Ds.triedTo("Load script from class path", ex, EsSourceLoadException.class);
			ds.a("qccPath", qccPath);
			ds.a("resourceBase", m_resourceBase.getName());
			System.err.println(ds);
			throw new EsSourceLoadException(ds.s());
		}
	}

	@Override
	public List<? extends INeonSourceDescriptor> zlDescriptorsAsc(String zccPath) {
		if (zccPath == null) throw new IllegalArgumentException("object is null");
		final String qccResource = qccIndexResourcePath(zccPath);
		final InputStream oIn = m_resourceBase.getResourceAsStream(qccResource);
		if (oIn == null) return Collections.emptyList();
		try {
			final Binary resourceBinary = Binary.newFromInputStream(oIn, 0L, qccResource, CNeon.QuotaClasspathSourceBc);
			final String qResource = resourceBinary.newStringUTF8();
			final String[] zptqtwLines = ArgonSplitter.zptzLines(qResource, true, false);
			final int lineCount = zptqtwLines.length;
			final List<INeonSourceDescriptor> zlAsc = new ArrayList<INeonSourceDescriptor>(lineCount);
			for (int i = 0; i < zptqtwLines.length; i++) {
				final String qtwLine = zptqtwLines[i];
				final int lenLine = qtwLine.length();
				final INeonSourceDescriptor oDescriptor;
				if (qtwLine.endsWith("/")) {
					if (lenLine == 1) {
						oDescriptor = null;
					} else {
						final String qccNode = qtwLine.substring(0, lenLine - 1);
						oDescriptor = new Descriptor(NeonSourceDescriptorType.Container, qccNode);
					}
				} else {
					final NeonSourceDescriptorType type = NeonSourceDescriptorType.newInstance(qtwLine);
					oDescriptor = new Descriptor(type, qtwLine);
				}
				if (oDescriptor != null) {
					zlAsc.add(oDescriptor);
				}
			}
			Collections.sort(zlAsc);
			return zlAsc;
		} catch (final ArgonQuotaException ex) {
			final Ds ds = Ds.triedTo("Load index from class path", ex);
			ds.a("qccResource", qccResource);
			ds.a("resourceBase", m_resourceBase.getName());
			System.err.println(ds);
		} catch (final ArgonStreamReadException ex) {
			final Ds ds = Ds.triedTo("Load index from class path", ex);
			ds.a("qccResource", qccResource);
			ds.a("resourceBase", m_resourceBase.getName());
			System.err.println(ds);
		}
		return Collections.emptyList();
	}

	public NeonSourceProviderDefaultClasspath(Class<?> resourceBase) {
		if (resourceBase == null) throw new IllegalArgumentException("object is null");
		m_resourceBase = resourceBase;
	}

	private final Class<?> m_resourceBase;

	private static class Descriptor implements INeonSourceDescriptor {

		@Override
		public int compareTo(INeonSourceDescriptor rhs) {
			final int c0 = NeonSourceDescriptorType.ByRank.compare(m_type, rhs.type());
			if (c0 != 0) return c0;
			final int c1 = qccNode().compareTo(rhs.qccNode());
			return c1;
		}

		@Override
		public boolean isAssure() {
			return true;
		}

		@Override
		public boolean isWip() {
			return false;
		}

		@Override
		public String qccNode() {
			return m_qccNode;
		}

		@Override
		public String toString() {
			return m_qccNode + (m_type == NeonSourceDescriptorType.Container ? "/" : "");
		}

		@Override
		public long tsLastModified() {
			return System.currentTimeMillis();
		}

		@Override
		public NeonSourceDescriptorType type() {
			return m_type;
		}

		public Descriptor(NeonSourceDescriptorType type, String qccNode) {
			assert type != null;
			assert qccNode != null && qccNode.length() > 0;
			m_type = type;
			m_qccNode = qccNode;
		}
		private final NeonSourceDescriptorType m_type;
		private final String m_qccNode;
	}
}
