/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author roach
 */
public class PathComponents implements Comparable<PathComponents> {

	private static boolean isSep(char ch) {
		return ch == '/' || ch == '\\';
	}

	private static String qcn(File src) {
		assert src != null;
		try {
			return src.getCanonicalPath();
		} catch (final IOException ex) {
			return src.getAbsolutePath();
		}
	}

	private static String zPrefix(String qcn) {
		final int len = qcn.length();

		String zDrive = "";
		if (len >= 2) {
			if (Character.isLetter(qcn.charAt(0))) {
				if (qcn.charAt(1) == ':') {
					zDrive = qcn.substring(0, 2);
				}
			}
		}
		final StringBuilder sb = new StringBuilder();
		final int lenDrive = zDrive.length();
		sb.append(zDrive);
		boolean more = true;
		for (int i = lenDrive; more && i < len; i++) {
			final char ch = qcn.charAt(i);
			if (isSep(ch)) {
				sb.append(ch);
			} else {
				more = false;
			}
		}
		return sb.toString();
	}

	@Override
	public int compareTo(PathComponents rhs) {
		final int c0 = m_qPrefix.compareTo(rhs.m_qPrefix);
		if (c0 != 0) return c0;
		final int lhsDepth = m_zptNodes.length;
		final int rhsDepth = rhs.m_zptNodes.length;
		final int depth = Math.min(lhsDepth, rhsDepth);
		for (int i = 0; i < depth; i++) {
			final String lhsNode = m_zptNodes[i];
			final String rhsNode = rhs.m_zptNodes[i];
			final int cn = lhsNode.compareTo(rhsNode);
			if (cn != 0) return cn;
		}
		if (lhsDepth < rhsDepth) return -1;
		if (lhsDepth > rhsDepth) return +1;
		return 0;
	}

	public boolean contains(PathComponents rhs) {
		if (rhs == null) throw new IllegalArgumentException("object is null");
		if (rhs == this) return false;
		if (!m_qPrefix.equals(rhs.m_qPrefix)) return false;
		final int lhsDepth = m_zptNodes.length;
		final int rhsDepth = rhs.m_zptNodes.length;
		if (rhsDepth <= lhsDepth) return false;
		for (int i = 0; i < lhsDepth; i++) {
			final String lhsNode = m_zptNodes[i];
			final String rhsNode = rhs.m_zptNodes[i];
			if (!lhsNode.equals(rhsNode)) return false;
		}
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof PathComponents)) return false;
		return equals((PathComponents) o);
	}

	public boolean equals(PathComponents rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		if (!m_qPrefix.equals(rhs.m_qPrefix)) return false;
		final int lhsDepth = m_zptNodes.length;
		final int rhsDepth = rhs.m_zptNodes.length;
		if (lhsDepth != rhsDepth) return false;
		for (int i = 0; i < lhsDepth; i++) {
			final String lhsNode = m_zptNodes[i];
			final String rhsNode = rhs.m_zptNodes[i];
			if (!lhsNode.equals(rhsNode)) return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hc = HashCoder.INIT;
		hc = HashCoder.and(hc, m_qPrefix);
		final int depth = m_zptNodes.length;
		for (int i = 0; i < depth; i++) {
			hc = HashCoder.and(hc, m_zptNodes[i]);
		}
		return hc;
	}

	public File newFile(char separator) {
		return new File(qccAbsolutePath(separator));
	}

	public PathComponents newParent() {
		final int depth = m_zptNodes.length;
		if (depth == 0) return this;
		final int pdepth = depth - 1;
		final String[] zptNodes = new String[pdepth];
		System.arraycopy(m_zptNodes, 0, zptNodes, 0, pdepth);
		return new PathComponents(m_qPrefix, zptNodes);
	}

	public String oqccRelativePath(PathComponents rhs, String[] ozptPrefix, char separator) {
		if (rhs == null) throw new IllegalArgumentException("object is null");
		final String[] oxptRelative = oxptRelative(rhs);
		if (oxptRelative == null) return null;
		final StringBuilder sb = new StringBuilder();
		if (ozptPrefix != null) {
			for (int i = 0; i < ozptPrefix.length; i++) {
				if (sb.length() > 0) {
					sb.append(separator);
				}
				sb.append(ozptPrefix[i]);
			}
		}
		for (int i = 0; i < oxptRelative.length; i++) {
			if (sb.length() > 0) {
				sb.append(separator);
			}
			sb.append(oxptRelative[i]);
		}
		return sb.toString();
	}

	public String[] oxptRelative(PathComponents rhs) {
		if (rhs == null) throw new IllegalArgumentException("object is null");
		if (!contains(rhs)) return null;
		final int lhsDepth = m_zptNodes.length;
		final int rhsDepth = rhs.m_zptNodes.length;
		final int relativeDepth = rhsDepth - lhsDepth;
		if (relativeDepth <= 0) return null;
		final String[] xptRel = new String[relativeDepth];
		System.arraycopy(rhs.m_zptNodes, lhsDepth, xptRel, 0, relativeDepth);
		return xptRel;
	}

	public String qccAbsolutePath(char separator) {
		final StringBuilder sb = new StringBuilder();
		sb.append(m_qPrefix);
		for (int i = 0; i < m_zptNodes.length; i++) {
			if (i > 0) {
				sb.append(separator);
			}
			sb.append(m_zptNodes[i]);
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return qccAbsolutePath(File.separatorChar);
	}

	private PathComponents(String qPrefix, String[] zptNodes) {
		assert qPrefix != null && qPrefix.length() > 0;
		assert zptNodes != null;
		m_qPrefix = qPrefix;
		m_zptNodes = zptNodes;
	}

	public PathComponents(File src) {
		if (src == null) throw new IllegalArgumentException("object is null");
		final String qcn = qcn(src);
		final int len = qcn.length();
		if (len == 0) throw new IllegalArgumentException("invalid file name>" + src + "<");
		final String zPrefix = zPrefix(qcn);
		final int lenPrefix = zPrefix.length();
		if (lenPrefix == 0) throw new IllegalArgumentException("invalid file name>" + src + "<");
		m_qPrefix = zPrefix;
		final StringBuilder sb = new StringBuilder();
		final List<String> zlNodes = new ArrayList<String>(8);
		for (int i = lenPrefix; i <= len; i++) {
			final char ch = (i == len) ? '/' : qcn.charAt(i);
			if (isSep(ch)) {
				final String zNode = sb.toString();
				zlNodes.add(zNode);
				sb.setLength(0);
			} else {
				sb.append(ch);
			}
		}
		final int nodeCount = zlNodes.size();
		m_zptNodes = zlNodes.toArray(new String[nodeCount]);
	}

	private final String m_qPrefix;
	private final String[] m_zptNodes;
}
