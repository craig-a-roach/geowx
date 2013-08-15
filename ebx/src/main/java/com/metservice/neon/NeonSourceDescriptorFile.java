/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.metservice.argon.ArgonCompare;

/**
 * @author roach
 */
public class NeonSourceDescriptorFile implements INeonSourceDescriptor {

	public static NeonSourceDescriptorFile newHomeInstance(File home) {
		return newNodeInstance(home, false);
	}

	public static NeonSourceDescriptorFile newHomeInstance(File home, String zcctwPath) {
		if (home == null) throw new IllegalArgumentException("object is null");
		if (zcctwPath.length() == 0) return newNodeInstance(home, false);
		final File nodePath = new File(home, zcctwPath);
		final boolean isAssure = NeonFileExtension.isAssurePath(zcctwPath);
		return newNodeInstance(nodePath, isAssure);
	}

	public static NeonSourceDescriptorFile newNodeInstance(File node, boolean isAssure) {
		if (node == null) throw new IllegalArgumentException("object is null");
		final NeonSourceDescriptorType type = NeonSourceDescriptorType.newInstance(node);
		final String qcctwNodeName = node.getName().trim();
		final boolean isWip = NeonFileExtension.isWipName(qcctwNodeName);
		return new NeonSourceDescriptorFile(type, node, isWip, isAssure);
	}

	@Override
	public int compareTo(INeonSourceDescriptor rhs) {
		final int c0 = NeonSourceDescriptorType.ByRank.compare(m_type, rhs.type());
		if (c0 != 0) return c0;
		final int c1 = ArgonCompare.fwd(m_isWip, rhs.isWip());
		if (c1 != 0) return c1;
		final int c2 = ArgonCompare.fwd(m_isAssure, rhs.isAssure());
		if (c2 != 0) return c2;
		final int c3 = qccNode().compareTo(rhs.qccNode());
		return c3;
	}

	public File file() {
		return m_node;
	}

	@Override
	public boolean isAssure() {
		return m_isAssure;
	}

	@Override
	public boolean isWip() {
		return m_isWip;
	}

	public List<NeonSourceDescriptorFile> new_zlSubAsc(boolean includeWip) {
		if (m_type != NeonSourceDescriptorType.Container) return Collections.emptyList();
		final File[] ozptFiles = m_node.listFiles();
		if (ozptFiles == null) return Collections.emptyList();
		final int count = ozptFiles.length;
		if (count == 0) return Collections.emptyList();
		final List<NeonSourceDescriptorFile> zlAsc = new ArrayList<NeonSourceDescriptorFile>(count);
		for (int i = 0; i < count; i++) {
			final File nodeFile = ozptFiles[i];
			final boolean subAssure = m_isAssure || NeonFileExtension.isAssureName(nodeFile.getName());
			final NeonSourceDescriptorFile sdf = newNodeInstance(nodeFile, subAssure);
			if (sdf.m_type == NeonSourceDescriptorType.Ignore) {
				continue;
			}
			if (sdf.m_isWip && !includeWip) {
				continue;
			}
			zlAsc.add(sdf);
		}
		Collections.sort(zlAsc);
		return zlAsc;
	}

	@Override
	public String qccNode() {
		return m_node.getName();
	}

	@Override
	public String toString() {
		return m_node.toString();
	}

	@Override
	public long tsLastModified() {
		return m_node.lastModified();
	}

	@Override
	public NeonSourceDescriptorType type() {
		return m_type;
	}

	private NeonSourceDescriptorFile(NeonSourceDescriptorType type, File node, boolean isWip, boolean isAssure) {
		assert type != null;
		assert node != null;
		m_type = type;
		m_node = node;
		m_isWip = isWip;
		m_isAssure = isAssure;
	}
	private final NeonSourceDescriptorType m_type;
	private final File m_node;
	private final boolean m_isWip;
	private final boolean m_isAssure;
}
