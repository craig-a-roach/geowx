/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import java.io.IOException;

/**
 * @author roach
 */
public abstract class KryptonRecordReader {

	protected long biReFile() {
		return m_biRecReFile + m_biPosReRec;
	}

	protected final KryptonDecoder decoder() {
		return m_fr.decoder();
	}

	protected final void endSection()
			throws KryptonReadException, KryptonRecordException {
		try {
			final int bc = CSection.END_bc;
			final byte[] buffer = new byte[bc];
			m_fr.raf().readFully(buffer);
			m_biPosReRec += bc;
			for (int i = 0; i < bc; i++) {
				final char ch = UGrib.char1(buffer, i);
				if (ch != '7') {
					final String p = "Expecting '7' at " + i;
					throw new KryptonRecordException("End", m_biRecReFile, m_biPosReRec, m_ri, p);
				}
			}
		} catch (final IOException ex) {
			final String part = "End Section";
			throw new KryptonReadException(part, m_biRecReFile, m_biPosReRec, ex);
		}
	}

	protected final byte[] inSection(String name, int bc)
			throws KryptonReadException {
		try {
			final byte[] buffer = new byte[bc];
			m_fr.raf().readFully(buffer);
			m_biPosReRec += bc;
			return buffer;
		} catch (final IOException ex) {
			final String part = name + " Section content (" + bc + ")";
			throw new KryptonReadException(part, m_biRecReFile, m_biPosReRec, ex);
		}
	}

	protected final int inSectionLength3(String name)
			throws KryptonReadException {
		try {
			final byte[] buffer = new byte[3];
			m_fr.raf().readFully(buffer);
			m_biPosReRec += 3L;
			return UGrib.intu3(buffer, 0);
		} catch (final IOException ex) {
			final String part = name + " Section length";
			throw new KryptonReadException(part, m_biRecReFile, m_biPosReRec, ex);
		}
	}

	protected final int inSectionLength3(String name, int bcLo)
			throws KryptonRecordException, KryptonReadException {
		final int bcSection = inSectionLength3(name);
		return validateSectionLength(name, bcSection, bcLo);
	}

	protected final int inSectionLength4()
			throws KryptonReadException {
		try {
			final byte[] buffer = new byte[4];
			m_fr.raf().readFully(buffer);
			m_biPosReRec += 4L;
			return UGrib.int4(buffer, 0);
		} catch (final IOException ex) {
			final String part = "Section length (probe)";
			throw new KryptonReadException(part, m_biRecReFile, m_biPosReRec, ex);
		}
	}

	protected final int inSectionLength4(String name)
			throws KryptonReadException {
		try {
			final byte[] buffer = new byte[4];
			m_fr.raf().readFully(buffer);
			m_biPosReRec += 4L;
			return UGrib.int4(buffer, 0);
		} catch (final IOException ex) {
			final String part = name + " Section length";
			throw new KryptonReadException(part, m_biRecReFile, m_biPosReRec, ex);
		}
	}

	protected final int inSectionLength4(String name, int bcLo)
			throws KryptonRecordException, KryptonReadException {
		final int bcSection = inSectionLength4(name);
		return validateSectionLength(name, bcSection, bcLo);
	}

	protected final int inSectionNo()
			throws KryptonReadException {
		try {
			final byte[] buffer = new byte[1];
			m_fr.raf().readFully(buffer);
			m_biPosReRec += 1L;
			return UGrib.intu1(buffer, 0);
		} catch (final IOException ex) {
			final String part = "Section number (probe)";
			throw new KryptonReadException(part, m_biRecReFile, m_biPosReRec, ex);
		}
	}

	protected final int inSectionNo(String name, int required)
			throws KryptonRecordException, KryptonReadException {
		final int sectionNo = inSectionNo();
		return validateSectionNo(name, sectionNo, required);
	}

	protected final void skipSection(String name, int bc)
			throws KryptonReadException {
		try {
			final long biPosNeo = m_biRecReFile + m_biPosReRec + bc;
			m_fr.raf().seek(biPosNeo);
			m_biPosReRec += bc;
		} catch (final IOException ex) {
			final String part = name + " Section content (" + bc + ") skip";
			throw new KryptonReadException(part, m_biRecReFile, m_biPosReRec, ex);
		}
	}

	protected final int validateSectionLength(String name, int bcSection, int bcLo)
			throws KryptonRecordException, KryptonReadException {
		if (bcSection < bcLo) {
			final String p = "Section too small (" + bcSection + ")";
			throw new KryptonRecordException(name, m_biRecReFile, m_biPosReRec, m_ri, p);
		}
		return bcSection;
	}

	protected final int validateSectionNo(String name, int sectionNo, int required)
			throws KryptonReadException, KryptonRecordException {
		if (sectionNo != required) {
			final String p = "Expected section number " + required + " but found " + sectionNo;
			throw new KryptonRecordException(name, m_biRecReFile, m_biPosReRec, m_ri, p);
		}
		return sectionNo;
	}

	public void advanceSubRecordIndex() {
		m_subRecIndex++;
	}

	public boolean hasMoreSections() {
		final long bcRem = m_bcRec - m_biPosReRec;
		return bcRem > CSection.END_bc;
	}

	public abstract boolean moreSections();

	public abstract KryptonDataRecord newDataRecord()
			throws KryptonTableException, KryptonCodeException, KryptonRecordException, KryptonReadException;

	public abstract KryptonMetaRecord newMetaRecord()
			throws KryptonTableException, KryptonCodeException, KryptonRecordException, KryptonReadException;

	public int recordIndex() {
		return m_ri;
	}

	public int subRecordIndex() {
		return m_subRecIndex;
	}

	protected KryptonRecordReader(KryptonFileReader fr, long bcRec, long biRecReFile, long biPosReRec, int ri) {
		assert fr != null;
		m_fr = fr;
		m_bcRec = bcRec;
		m_biRecReFile = biRecReFile;
		m_biPosReRec = biPosReRec;
		m_ri = ri;
	}
	protected final KryptonFileReader m_fr;
	protected final long m_bcRec;
	protected final long m_biRecReFile;
	protected final int m_ri;
	protected long m_biPosReRec;
	protected int m_subRecIndex;
}
