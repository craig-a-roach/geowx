/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import com.metservice.argon.ArgonSplitter;

/**
 * @author roach
 */
public class BerylliumPathExt {

	@Override
	public String toString() {
		return qlctwExt + (oencoding == null ? "" : " (" + oencoding + ")");
	}

	public static BerylliumPathExt createInstance(BerylliumPath path) {
		if (path == null) throw new IllegalArgumentException("object is null");
		if (path.depth == 0) return null;
		final String qlctwNode = path.qtwNode(-1).toLowerCase();
		final String[] zptqlctwSplit = ArgonSplitter.zptqtwSplit(qlctwNode, '.');
		final int partCount = zptqlctwSplit.length;
		if (partCount < 2) return null;
		if (partCount == 2) return new BerylliumPathExt(zptqlctwSplit[1], null);
		final String qlctwTail = zptqlctwSplit[partCount - 1];
		final boolean isGz = qlctwTail.equals(CBeryllium.ExtGz1) || qlctwTail.equals(CBeryllium.ExtGz2);
		if (isGz) return new BerylliumPathExt(zptqlctwSplit[partCount - 2], Encoding.gzip);
		return new BerylliumPathExt(qlctwTail, null);
	}

	public BerylliumPathExt(String qlctwExt, Encoding oencoding) {
		if (qlctwExt == null || qlctwExt.length() == 0) throw new IllegalArgumentException("string is null or empty");
		this.qlctwExt = qlctwExt;
		this.oencoding = oencoding;
	}

	public final String qlctwExt;
	public final Encoding oencoding;

	public static enum Encoding {
		gzip("application/gzip");

		private Encoding(String mimeType) {
			this.qlctwMimeType = mimeType;
		}
		public final String qlctwMimeType;
	}
}
