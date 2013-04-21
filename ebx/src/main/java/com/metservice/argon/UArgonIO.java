/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author roach
 */
class UArgonIO {

	private static final int ReadMinBc = 4 * CArgon.K;

	private static int clampedEst(String ozSourceName, int bcQuota, long bcEst)
			throws ArgonQuotaException {
		if (bcEst > bcQuota) {
			final String m = msgExceedsQuota(ozSourceName, bcQuota, bcEst);
			throw new ArgonQuotaException(m);
		}
		return (int) bcEst;
	}

	private static String msgExceedsQuota(String ozSourceName, int bcQuota, long bcEst) {
		final StringBuilder sb = new StringBuilder();
		sb.append("Input stream");
		if (ozSourceName != null && ozSourceName.length() > 0) {
			sb.append(" '").append(ozSourceName).append("'");
		}
		sb.append(" estimated size is ").append(bcEst).append(" bytes");
		sb.append(", which exceeds the application-defined quota of ").append(bcQuota).append(" bytes");
		return sb.toString();
	}

	public static Binary newInstance(InputStream ins, long bcEst, String ozSourceName, int bcQuota)
			throws ArgonQuotaException, ArgonStreamReadException {
		if (ins == null) throw new IllegalArgumentException("object is null");
		final byte[] zptRead = zptRead(ins, bcEst, ozSourceName, bcQuota);
		return Binary.newFromTransient(zptRead);
	}

	public static byte[] zptRead(InputStream ins, long bcEst, String ozSourceName, int bcQuota)
			throws ArgonQuotaException, ArgonStreamReadException {
		if (ins == null) throw new IllegalArgumentException("object is null");
		try {
			final int bcCEst = clampedEst(ozSourceName, bcQuota, bcEst);
			final int bufferSize = Math.max(ReadMinBc, bcCEst);
			byte[] xpByteArray = new byte[bufferSize];
			int pos = 0;
			int rem = xpByteArray.length - pos;
			boolean eof = false;
			while (!eof) {
				final int reqd = Math.min(rem, bufferSize);
				final int actual = ins.read(xpByteArray, pos, reqd);
				if (actual == -1) {
					eof = true;
				} else {
					pos += actual;
					rem -= actual;
					if (rem == 0) {
						final long bcNeoEst = xpByteArray.length * 3 / 2;
						final int bcCNeoEst = clampedEst(ozSourceName, bcQuota, bcNeoEst);
						final byte[] save = xpByteArray;
						xpByteArray = new byte[bcCNeoEst];
						System.arraycopy(save, 0, xpByteArray, 0, pos);
						rem = xpByteArray.length - pos;
					}
				}
			}

			final byte[] xptByteArray;
			if (pos == xpByteArray.length) {
				xptByteArray = xpByteArray;
			} else {
				xptByteArray = new byte[pos];
				System.arraycopy(xpByteArray, 0, xptByteArray, 0, pos);
			}
			return xptByteArray;
		} catch (final IOException ex) {
			final Ds ds = Ds.triedTo("Read stream", ex, ArgonStreamReadException.class);
			ds.a("sourceName", ozSourceName);
			ds.a("bcEst", bcEst);
			throw new ArgonStreamReadException(ds.s());
		} finally {
			try {
				ins.close();
			} catch (final IOException exIO) {
			}
		}
	}
}
