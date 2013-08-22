/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import com.metservice.argon.ArgonSplitter;
import com.metservice.argon.Ds;

/**
 * @author roach
 */
public class BerylliumPathMime {

	public EntityHeaders newEntityHeaders(String ozAcceptEncoding) {
		final BerylliumPathExt.Encoding oencoding = opathExt == null ? null : opathExt.oencoding;
		if (oencoding == null) return new EntityHeaders(qlctwMimeType, null);

		final String zlctwAcceptEncoding = ozAcceptEncoding == null ? "" : ozAcceptEncoding.trim().toLowerCase();

		final String qtwEnc = oencoding.name();

		final String[] zptqlctwParts = ArgonSplitter.zptqtwSplit(zlctwAcceptEncoding, ',');
		for (int i = 0; i < zptqlctwParts.length; i++) {
			final String qlctwAccept = zptqlctwParts[i];
			final boolean encode = qlctwAccept.equals(qtwEnc);
			if (encode) return new EntityHeaders(qlctwMimeType, qlctwAccept);
		}

		return new EntityHeaders(oencoding.qlctwMimeType, null);
	}

	@Override
	public String toString() {
		return qlctwMimeType + (opathExt == null ? "" : " :" + opathExt);
	}

	public BerylliumPathMime(String qlctwMimeType, BerylliumPathExt opathExt) {
		if (qlctwMimeType == null || qlctwMimeType.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		this.qlctwMimeType = qlctwMimeType;
		this.opathExt = opathExt;
	}

	public final String qlctwMimeType;

	public final BerylliumPathExt opathExt;

	public static class EntityHeaders {

		@Override
		public String toString() {
			final Ds ds = Ds.o("EntityHeaders");
			ds.a("contentType", qlcContentType);
			ds.a("contentEncoding", oqlcContentEncoding);
			return ds.s();
		}

		public EntityHeaders(String qlcContentType, String oqlcContentEncoding) {
			assert qlcContentType != null;
			this.qlcContentType = qlcContentType;
			this.oqlcContentEncoding = oqlcContentEncoding;
		}
		public final String qlcContentType;

		public final String oqlcContentEncoding;
	}
}
