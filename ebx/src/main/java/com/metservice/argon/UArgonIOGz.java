/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author roach
 */
class UArgonIOGz {

	public static final double InflateRatio = 2.0;

	public static final int DefaultBinaryBcQuota = 1024 * CArgon.M;

	private static byte[] zptDecoded(InputStream ins, int bcSource, String ozSourceName, int bcQuota)
			throws ArgonQuotaException, ArgonStreamReadException {
		assert ins != null;
		final GZIPInputStream gzins;
		try {
			gzins = new GZIPInputStream(ins);
		} catch (final IOException ex) {
			final Ds ds = Ds.triedTo("Open GZ stream", ex, ArgonStreamReadException.class);
			ds.a("sourceName", ozSourceName);
			ds.a("bcSource", bcSource);
			ds.a("bcQuota", bcQuota);
			throw new ArgonStreamReadException(ds.s());
		} finally {
			try {
				ins.close();
			} catch (final IOException ex) {
			}
		}
		try {
			return UArgonIODeflate.zptInflated(gzins, bcSource, ozSourceName, bcQuota, InflateRatio);
		} finally {
			try {
				gzins.close();
			} catch (final IOException exIO) {
			}
		}
	}

	private static Binary zptEncoded(byte[] zptSource)
			throws ArgonStreamWriteException {
		GZIPOutputStream ogout = null;
		final int estSize = zptSource.length;
		try {
			final BinaryOutputStream bout = new BinaryOutputStream(estSize);
			ogout = new GZIPOutputStream(bout);
			ogout.write(zptSource);
			ogout.close();
			ogout = null;
			return bout.newBinary();
		} catch (final IOException ex) {
			final Ds ds = Ds.triedTo("Create GZ stream", ex, ArgonStreamWriteException.class);
			ds.a("estSize", estSize);
			throw new ArgonStreamWriteException(ds.s());
		} finally {
			if (ogout != null) {
				try {
					ogout.close();
				} catch (final IOException exIO) {
				}
			}
		}
	}

	public static Binary newDecoded(Binary source)
			throws ArgonQuotaException, ArgonStreamReadException {
		return newDecoded(source, null, DefaultBinaryBcQuota);
	}

	public static Binary newDecoded(Binary source, String ozSourceName, int bcQuota)
			throws ArgonQuotaException, ArgonStreamReadException {
		if (source == null) throw new IllegalArgumentException("object is null");
		final int bcSource = source.byteCount();
		final InputStream ins = source.getInputStream();
		try {
			final byte[] zptDecoded = zptDecoded(ins, bcSource, ozSourceName, bcQuota);
			return Binary.newFromTransient(zptDecoded);
		} finally {
			try {
				ins.close();
			} catch (final IOException ex) {
			}
		}
	}

	public static Binary newEncoded(Binary source)
			throws ArgonStreamWriteException {
		return zptEncoded(source.zptReadOnly);
	}
}
