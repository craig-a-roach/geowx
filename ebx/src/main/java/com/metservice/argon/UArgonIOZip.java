/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author roach
 */
class UArgonIOZip {

	public static final int DefaultEst = 64 * CArgon.K;

	public static final double InflateRatio = 2.0;

	private static void closeSilent(InputStream oins) {
		if (oins != null) {
			try {
				oins.close();
			} catch (final IOException ex) {
			}
		}
	}

	private static List<ArgonZipItem> zlDecodedAscName(InputStream ins, String ozSourceName, int bcQuota)
			throws ArgonQuotaException, ArgonStreamReadException {
		assert ins != null;
		final List<ArgonZipItem> zlOutAscName = new ArrayList<ArgonZipItem>();
		final ZipInputStream zins = new ZipInputStream(ins);
		try {
			int bcRemQuota = bcQuota;
			boolean more = true;
			while (more) {
				final ZipEntry oEntry = zins.getNextEntry();
				if (oEntry == null) {
					more = false;
				} else {
					if (!oEntry.isDirectory()) {
						final String qccFileName = oEntry.getName();
						final Date lastModifiedAt = DateFactory.newDate(oEntry.getTime());
						final long bcSourceEst;
						final double inflateRatio;
						final long oUncompressedSize = oEntry.getSize();
						final long oCompressedSize = oEntry.getCompressedSize();
						if (oUncompressedSize == -1L) {
							if (oCompressedSize == -1L) {
								bcSourceEst = DefaultEst;
								inflateRatio = InflateRatio;
							} else {
								bcSourceEst = oCompressedSize;
								inflateRatio = InflateRatio;
							}
						} else {
							final double bcInflated = oUncompressedSize;
							if (oCompressedSize == -1L) {
								bcSourceEst = Math.round(bcInflated / InflateRatio);
								inflateRatio = InflateRatio;
							} else {
								bcSourceEst = oCompressedSize;
								inflateRatio = oCompressedSize == 0L ? InflateRatio
										: (bcInflated / oCompressedSize);
							}
						}
						final byte[] zptDecoded = UArgonIODeflate.zptInflated(zins, bcSourceEst, qccFileName,
								bcRemQuota, inflateRatio);
						final Binary decoded = Binary.newFromTransient(zptDecoded);

						bcRemQuota -= zptDecoded.length;
						zlOutAscName.add(new ArgonZipItem(qccFileName, lastModifiedAt, decoded));
					}
					zins.closeEntry();
				}
			}
			Collections.sort(zlOutAscName);
			return zlOutAscName;
		} catch (final IOException ex) {
			final Ds ds = Ds.triedTo("Read ZIP stream", ex, ArgonStreamReadException.class);
			ds.a("sourceName", ozSourceName);
			ds.a("bcQuota", bcQuota);
			throw new ArgonStreamReadException(ds.s());
		} finally {
			closeSilent(zins);
		}
	}

	public static List<ArgonZipItem> zlDecodedAscName(Binary source, String ozSourceName, int bcQuota)
			throws ArgonQuotaException, ArgonStreamReadException {
		if (source == null) throw new IllegalArgumentException("object is null");
		final InputStream ins = source.getInputStream();
		try {
			return zlDecodedAscName(ins, ozSourceName, bcQuota);
		} finally {
			closeSilent(ins);
		}
	}
}
