/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;

/**
 * @author roach
 */
public class KryptonFileReader {

	private static final int MinimalBc = 16;
	private static final int InitBc = 8;
	private static final int IndicatorSectionG2Bc = 16;

	private static final char[] MarkerGRIB = { 'G', 'R', 'I', 'B' };

	private static boolean foundMarker(byte[] buffer, int pos, char[] marker) {
		for (int i = 0; i < marker.length; i++) {
			final char mk = marker[i];
			final char ch = UGrib.char1(buffer, pos + i);
			if (mk != ch) return false;
		}
		return true;
	}

	public static KryptonFileReader createInstance(KryptonDecoder decoder, File srcFile) {
		if (decoder == null) throw new IllegalArgumentException("object is null");
		if (srcFile == null) throw new IllegalArgumentException("object is null");
		final long bcFile = srcFile.length();
		if (bcFile == 0L) return null;
		final RandomAccessFile raf;
		try {
			raf = new RandomAccessFile(srcFile, "r");
		} catch (final FileNotFoundException ex) {
			return null;
		}
		return new KryptonFileReader(decoder, srcFile, bcFile, raf);
	}

	public static KryptonFileReader createInstance(KryptonDecoder decoder, Path srcPath) {
		if (srcPath == null) throw new IllegalArgumentException("object is null");
		return createInstance(decoder, srcPath.toFile());
	}

	public void close() {
		try {
			m_raf.close();
		} catch (final IOException ex) {
		}
	}

	public KryptonDecoder decoder() {
		return m_decoder;
	}

	public boolean hasNextRecordReader() {
		if (m_oCursor != null && m_oCursor.hasMoreSections()) return true;
		return m_biPos < m_bcFile;
	}

	public KryptonRecordReader nextRecordReader()
			throws KryptonRecordException, KryptonReadException {
		if (m_oCursor != null && m_oCursor.hasMoreSections()) {
			m_oCursor.advanceSubRecordIndex();
			return m_oCursor;
		}
		final long bcRem = m_bcFile - m_biPos;
		if (bcRem < MinimalBc) {
			final String p = "Record is only " + bcRem + " bytes";
			throw new KryptonRecordException("Indicator", m_biPos, 0L, m_recCount, p);
		}
		boolean fail = true;
		try {
			final byte[] initSection = new byte[InitBc];
			m_raf.seek(m_biPos);
			m_raf.readFully(initSection);
			if (!foundMarker(initSection, 0, MarkerGRIB)) {
				final String p = "Missing 'GRIB' marker";
				throw new KryptonRecordException("Indicator", m_biPos, 0L, m_recCount, p);
			}
			final int gribEdition = UGrib.intu1(initSection, 7);
			if (gribEdition != 1 && gribEdition != 2) {
				final String p = "Unsupported GRIB edition " + gribEdition + "; expecting 1 or 2";
				throw new KryptonRecordException("Indicator", m_biPos, 7L, m_recCount, p);
			}
			final long bcRec;
			final long biPosReRec;
			final short discipline;
			if (gribEdition == 1) {
				discipline = 0;
				bcRec = UGrib.intu3(initSection, 4);
				biPosReRec = InitBc;
			} else {
				final byte[] indicatorSection = new byte[IndicatorSectionG2Bc];
				m_raf.seek(m_biPos);
				m_raf.readFully(indicatorSection);
				discipline = UGrib.shortu1(indicatorSection, 6);
				bcRec = UGrib.long8(indicatorSection, 8);
				biPosReRec = IndicatorSectionG2Bc;
			}
			if (bcRec > bcRem) {
				final String p = "Incomplete file. Record indicator specifies " + bcRec + " bytes, but only " + bcRem
						+ " bytes remain in file";
				throw new KryptonRecordException("Indicator", m_biPos, biPosReRec, m_recCount, p);
			}
			final KryptonRecordReader mr;
			if (gribEdition == 1) {
				mr = new Record1Reader(this, bcRec, m_biPos, biPosReRec, m_recCount);
			} else {
				mr = new Record2Reader(this, bcRec, m_biPos, biPosReRec, m_recCount, discipline);
			}
			m_oCursor = mr;
			m_biPos += bcRec;
			m_recCount++;
			fail = false;
			return mr;
		} catch (final IOException ex) {
			throw new KryptonReadException("Indicator Section", m_biPos, 0L, ex);
		} finally {
			if (fail) {
				close();
			}
		}
	}

	public RandomAccessFile raf() {
		return m_raf;
	}

	public File sourceFile() {
		return m_srcFile;
	}

	private KryptonFileReader(KryptonDecoder decoder, File src, long bcFile, RandomAccessFile raf) {
		if (decoder == null) throw new IllegalArgumentException("object is null");
		if (src == null) throw new IllegalArgumentException("object is null");
		if (raf == null) throw new IllegalArgumentException("object is null");
		m_decoder = decoder;
		m_srcFile = src;
		m_bcFile = bcFile;
		m_raf = raf;
		m_biPos = 0L;
	}

	private final KryptonDecoder m_decoder;
	private final File m_srcFile;
	private final long m_bcFile;
	private final RandomAccessFile m_raf;
	private long m_biPos;
	private int m_recCount;
	private KryptonRecordReader m_oCursor;
}
