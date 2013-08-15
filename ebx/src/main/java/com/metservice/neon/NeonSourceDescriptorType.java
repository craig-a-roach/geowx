/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.io.File;
import java.util.Comparator;

import com.metservice.argon.ArgonCompare;

/**
 * @author roach
 */
public enum NeonSourceDescriptorType {

	Container(0), EcmaScript(1), Unresolved(2), Ignore(3);

	public static NeonSourceDescriptorType newInstance(File file) {
		if (file == null) throw new IllegalArgumentException("object is null");
		if (file.isHidden()) return Ignore;
		if (file.isDirectory()) return Container;
		return newInstance(file.getName());
	}

	public static NeonSourceDescriptorType newInstance(String qcctwName) {
		if (qcctwName == null || qcctwName.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (qcctwName.startsWith(NeonFileExtension.PrefixIgnore)) return Ignore;
		if (qcctwName.endsWith(NeonFileExtension.SuffixBackup)) return Ignore;
		final String qcctwBase = NeonFileExtension.qcctwBaseName(qcctwName);
		if (qcctwBase.endsWith(NeonFileExtension.SuffixEcmascript)) return EcmaScript;
		return Unresolved;
	}

	public int rank() {
		return m_rank;
	}

	private NeonSourceDescriptorType(int rank) {
		m_rank = rank;
	}
	private final int m_rank;

	public static final Comparator<NeonSourceDescriptorType> ByRank = new Comparator<NeonSourceDescriptorType>() {

		@Override
		public int compare(NeonSourceDescriptorType lhs, NeonSourceDescriptorType rhs) {
			return ArgonCompare.fwd(lhs.rank(), rhs.rank());
		}
	};

}
