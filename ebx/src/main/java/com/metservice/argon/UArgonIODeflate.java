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
class UArgonIODeflate {

	private static final int DecompressMinBc = 4 * CArgon.K;

	private static int clampedInflatedEst(long bcSource, String ozSourceName, int bcInflatedQuota, long bcEst)
			throws ArgonQuotaException {
		if (bcEst > bcInflatedQuota) {
			final String m = msgExceedsQuota(bcSource, ozSourceName, bcInflatedQuota, bcEst);
			throw new ArgonQuotaException(m);
		}
		return (int) bcEst;
	}

	private static void closeSilent(InputStream oins) {
		if (oins != null) {
			try {
				oins.close();
			} catch (final IOException ex) {
			}
		}
	}

	private static String msgExceedsQuota(long bcSource, String ozSourceName, int bcInflatedQuota, long bcEst) {
		final StringBuilder sb = new StringBuilder();
		sb.append("Compressed stream");
		if (ozSourceName != null && ozSourceName.length() > 0) {
			sb.append(" '").append(ozSourceName).append("'");
		}
		sb.append(" is ").append(bcSource).append(" bytes in length.");
		sb.append(" Estimated size after inflation is ").append(bcEst);
		sb.append(", which exceeds the remaining application-defined quota of ").append(bcInflatedQuota).append(" bytes");
		return sb.toString();
	}

	public static byte[] zptInflated(InputStream ins, long bcSource, String ozSourceName, int bcInflatedQuota, double inflateRatio)
			throws ArgonQuotaException, ArgonStreamReadException {
		if (ins == null) throw new IllegalArgumentException("object is null");
		boolean close = true;
		try {
			final long bcEst = Math.round(bcSource * inflateRatio);
			final int bcCEst = clampedInflatedEst(bcSource, ozSourceName, bcInflatedQuota, bcEst);
			final int bufferSize = Math.max(DecompressMinBc, bcCEst);
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
						final int bcCNeoEst = clampedInflatedEst(bcSource, ozSourceName, bcInflatedQuota, bcNeoEst);
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
			close = false;
			return xptByteArray;
		} catch (final IOException ex) {
			final Ds ds = Ds.triedTo("Read compressed stream component", ex, ArgonStreamReadException.class);
			ds.a("sourceName", ozSourceName);
			ds.a("bcSource", bcSource);
			throw new ArgonStreamReadException(ds.s());
		} finally {
			if (close) {
				closeSilent(ins);
			}
		}
	}
}
