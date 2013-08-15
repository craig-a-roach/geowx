/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.URIUtil;

import com.metservice.argon.ArgonCompare;
import com.metservice.argon.HashCoder;
import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;

/**
 * @author roach
 */
public class BerylliumPath implements Comparable<BerylliumPath>, Iterable<String> {

	private static final BerylliumPath Empty = new BerylliumPath(false, new String[0]);

	public static BerylliumPath newAbsolute(String... zptqtwNodes) {
		if (zptqtwNodes == null) throw new IllegalArgumentException("object is null");
		return new BerylliumPath(true, zptqtwNodes);
	}

	public static BerylliumPath newInstance(JsonObject src, String pname)
			throws JsonSchemaException {
		if (src == null) throw new IllegalArgumentException("object is null");
		return newInstance(src.accessor(pname).datumQtwString());
	}

	public static BerylliumPath newInstance(Request baseRequest) {
		return newInstance(baseRequest.getPathInfo());
	}

	public static BerylliumPath newInstance(String zPathInfo) {
		if (zPathInfo == null) throw new IllegalArgumentException("object is null");
		final List<String> zl = new ArrayList<String>();
		final StringBuilder sb = new StringBuilder();
		final int len = zPathInfo.length();
		boolean isAbsolute = false;
		for (int i = 0; i <= len; i++) {
			final char ch = i == len ? '/' : zPathInfo.charAt(i);
			if (ch == '/') {
				if (i == 0) {
					isAbsolute = true;
				}
				final String ztw = sb.toString().trim();
				if (ztw.length() > 0) {
					zl.add(sb.toString());
				}
				sb.setLength(0);
			} else {
				sb.append(ch);
			}
		}
		final int depth = zl.size();
		if (depth == 0) return Empty;
		final String[] zptqtw = zl.toArray(new String[depth]);
		return new BerylliumPath(isAbsolute, zptqtw);
	}

	public static BerylliumPath newRelative(String... zptqtwNodes) {
		if (zptqtwNodes == null) throw new IllegalArgumentException("object is null");
		return new BerylliumPath(false, zptqtwNodes);
	}

	public static String qAbsolutePath(String... zptqtwNodes) {
		if (zptqtwNodes == null) throw new IllegalArgumentException("object is null");
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < zptqtwNodes.length; i++) {
			sb.append('/');
			sb.append(zptqtwNodes[i]);
		}
		return sb.toString();
	}

	private String qPath(boolean encode) {
		final String ztw = zPath(encode);
		if (ztw.length() == 0) throw new IllegalStateException("Path is empty");
		return ztw;
	}

	private String zPath(boolean encode) {
		final StringBuilder sb = new StringBuilder();
		if (isAbsolute) {
			sb.append('/');
		}
		for (int i = 0; i < depth; i++) {
			if (i > 0) {
				sb.append('/');
			}
			final String qtw = m_zptqtw[i];
			if (encode) {
				URIUtil.encodePath(sb, qtw);
			} else {
				sb.append(qtw);
			}
		}
		return sb.toString();
	}

	@Override
	public int compareTo(BerylliumPath rhs) {
		final int c0 = ArgonCompare.fwd(isAbsolute, rhs.isAbsolute);
		if (c0 != 0) return c0;
		final int c1 = ArgonCompare.fwd(depth, rhs.depth);
		if (c1 != 0) return c1;
		for (int i = 0; i < depth; i++) {
			final int ci = m_zptqtw[i].compareTo(rhs.m_zptqtw[i]);
			if (ci != 0) return ci;
		}
		return 0;
	}

	public BerylliumPathExt createExtension() {
		return BerylliumPathExt.createInstance(this);
	}

	public boolean equals(BerylliumPath rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		if (m_hc != rhs.m_hc) return false;
		if (isAbsolute != rhs.isAbsolute) return false;
		if (depth != rhs.depth) return false;
		for (int i = 0; i < depth; i++) {
			if (!m_zptqtw[i].equals(rhs.m_zptqtw[i])) return false;
		}
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof BerylliumPath)) return false;
		return equals((BerylliumPath) o);
	}

	@Override
	public int hashCode() {
		return m_hc;
	}

	public boolean isFavouriteIcon() {
		return depth == 1 && m_zptqtw[0].equals(CBeryllium.UriNode_FavouriteIcon);
	}

	@Override
	public Iterator<String> iterator() {
		return new Iter(m_zptqtw);
	}

	public boolean match(int index, String qtwNode) {
		final int cindex = index < 0 ? (depth + index) : index;
		return (cindex >= 0 && cindex < depth) ? m_zptqtw[cindex].equals(qtwNode) : false;
	}

	public BerylliumPath newPath(BerylliumPath ext) {
		if (ext == null) throw new IllegalArgumentException("object is null");
		return newPath(ext.m_zptqtw);
	}

	public BerylliumPath newPath(String... zptNodes) {
		if (zptNodes == null) throw new IllegalArgumentException("object is null");
		final int extlen = zptNodes.length;
		if (extlen == 0) return this;
		final int neoDepth = depth + extlen;
		if (neoDepth == 0) return Empty;
		final String[] zptNeo = new String[neoDepth];
		System.arraycopy(m_zptqtw, 0, zptNeo, 0, depth);
		System.arraycopy(zptNodes, 0, zptNeo, depth, extlen);
		return new BerylliumPath(isAbsolute, zptNeo);
	}

	public BerylliumPathQuery newPathQuery(Object... ozptQueryNameValues) {
		return new BerylliumPathQuery(this, BerylliumQuery.newInstance(ozptQueryNameValues));
	}

	public String qtwEncodedPath() {
		return qPath(true);
	}

	public String qtwNode(int index) {
		final int cindex = index < 0 ? (depth + index) : index;
		if (cindex >= 0 && cindex < depth) return m_zptqtw[cindex];
		throw new IllegalArgumentException("invalid index>" + index + "< for depth " + depth);
	}

	public String qtwNode(int index, String diagnostic)
			throws BerylliumHttpBadRequestException {
		final int cindex = index < 0 ? (depth + index) : index;
		if (cindex >= 0 && cindex < depth) return m_zptqtw[cindex];
		final String nn = diagnostic == null || diagnostic.length() == 0 ? Integer.toString(cindex) : diagnostic;
		throw new BerylliumHttpBadRequestException("Incomplete uri; expecting a '" + nn + "' node");
	}

	public String qtwPath() {
		return qPath(false);
	}

	public String qtwPathQuery(Object... ozptQueryNameValues) {
		return zPath(true) + BerylliumQuery.newInstance(ozptQueryNameValues).zUriQuery();
	}

	public BerylliumPath relative() {
		if (!isAbsolute) return this;
		return new BerylliumPath(false, m_zptqtw);
	}

	public void saveTo(JsonObject dst, String pname) {
		if (dst == null) throw new IllegalArgumentException("object is null");
		dst.putString(pname, zPath(false));
	}

	public BerylliumPath subPath(int indexFrom, int indexToex) {
		final int cindexToex = indexToex < 0 ? (depth + indexToex) : indexToex;
		if (indexFrom == 0 && cindexToex == depth) return this;
		if (indexFrom < 0) throw new IllegalArgumentException("invalid indexFrom>" + indexFrom + "<");
		if (cindexToex > depth) throw new IllegalArgumentException("invalid indexToex>" + cindexToex + "<");
		final int neoDepth = cindexToex - indexFrom;
		if (neoDepth < 0) throw new IllegalArgumentException("invalid from/to>" + indexFrom + "/" + cindexToex + "<");
		if (neoDepth == 0) return Empty;
		final String[] zptNeo = new String[neoDepth];
		System.arraycopy(m_zptqtw, indexFrom, zptNeo, 0, neoDepth);
		final boolean isNeoAbsolute = indexFrom == 0 && isAbsolute;
		return new BerylliumPath(isNeoAbsolute, zptNeo);
	}

	public BerylliumPath subPathHead(int indexToex) {
		return subPath(0, indexToex);
	}

	public BerylliumPath subPathTail(int indexFrom) {
		return subPath(indexFrom, depth);
	}

	@Override
	public String toString() {
		return zPath(false);
	}

	public String[] zptqtwRelative() {
		return m_zptqtw;
	}

	public String ztwPath() {
		return zPath(false);
	}

	public String ztwPathEncoded() {
		return zPath(true);
	}

	public String ztwPathEncoded(BerylliumQuery query) {
		return zPath(true) + query.zUriQuery();
	}

	private BerylliumPath(boolean isAbsolute, String[] zptqtw) {
		assert zptqtw != null;
		this.isAbsolute = isAbsolute;
		m_zptqtw = zptqtw;
		this.depth = zptqtw.length;
		int hc = HashCoder.field(isAbsolute);
		for (int i = 0; i < depth; i++) {
			hc = HashCoder.and(hc, zptqtw[i]);
		}
		m_hc = hc;
	}
	public final boolean isAbsolute;
	public final int depth;
	private final String[] m_zptqtw;
	private final int m_hc;

	private static class Iter implements Iterator<String> {

		@Override
		public boolean hasNext() {
			return m_index < m_zptqtw.length;
		}

		@Override
		public String next() {
			final String qtw = m_zptqtw[m_index];
			m_index++;
			return qtw;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Cannot remove");
		}

		public Iter(String[] zptqtw) {
			assert zptqtw != null;
			m_zptqtw = zptqtw;
		}
		private final String[] m_zptqtw;
		private int m_index;
	}
}
