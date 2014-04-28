/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author roach
 */
public class KryptonRecord2Builder {

	public static final int METEOROLOGICAL = 0;
	private static final int Section0Length = 16;
	private static final int GribEditionNo = 2;

	private static final byte[] EndSectionBinary = newEndSection();

	private static int bix(int octet) {
		return octet - 1;
	}

	private static byte[] newEndSection() {
		final byte[] out = new byte[4];
		UGrib.stringu1(out, 0, "7777");
		return out;
	}

	public static KryptonRecord2Builder newMeteorological(KryptonSection2Builder... builders) {
		final KryptonRecord2Builder neo = new KryptonRecord2Builder(METEOROLOGICAL);
		neo.put(builders);
		return neo;
	}

	private byte[] newIndicatorSection(long recordLength) {
		final byte[] out = new byte[16];
		UGrib.stringu1(out, bix(1), "GRIB");
		UGrib.intu1(out, bix(7), m_discipline);
		UGrib.intu1(out, bix(8), GribEditionNo);
		UGrib.long8(out, bix(9), recordLength);
		return out;
	}

	private void save(OutputStream bos, Section2Buffer[] bufferArray, long recordLength)
			throws IOException {
		final byte[] IndicatorSection = newIndicatorSection(recordLength);
		bos.write(IndicatorSection);
		for (int sectionNo = 1; sectionNo <= 7; sectionNo++) {
			final Section2Buffer vBuffer = bufferArray[sectionNo];
			if (vBuffer == null) {
				continue;
			}
			vBuffer.save(bos);
		}
		bos.write(EndSectionBinary);
	}

	public KryptonRecord2Builder newInstance(short discipline) {
		return new KryptonRecord2Builder(discipline);
	}

	public void put(KryptonSection2Builder... builders) {
		if (builders == null) throw new IllegalArgumentException("object is null");
		for (int i = 0; i < builders.length; i++) {
			final KryptonSection2Builder vBuilder = builders[i];
			if (vBuilder == null) {
				continue;
			}
			final int sectionNo = vBuilder.sectionNo();
			m_sectionBuilderMap[sectionNo] = vBuilder;
		}
	}

	public long save(OutputStream bos)
			throws KryptonBuildException, IOException {
		if (bos == null) throw new IllegalArgumentException("object is null");
		long recordLength = Section0Length;
		final Section2Buffer[] bufferArray = new Section2Buffer[8];
		for (int sectionNo = 1; sectionNo <= 7; sectionNo++) {
			if (sectionNo == 2) {
				continue;
			}
			final KryptonSection2Builder vBuilder = m_sectionBuilderMap[sectionNo];
			if (vBuilder == null) throw new KryptonBuildException("Missing section number " + sectionNo);
			try {
				final Section2Buffer buffer = vBuilder.newBuffer();
				bufferArray[sectionNo] = buffer;
				recordLength += buffer.sectionOctetCount();
			} catch (final KryptonBuildException ex) {
				throw new KryptonBuildException(ex, "in section " + sectionNo);
			}
		}
		recordLength += EndSectionBinary.length;
		save(bos, bufferArray, recordLength);
		return recordLength;
	}

	private KryptonRecord2Builder(int discipline) {
		m_discipline = discipline;
	}
	private final int m_discipline;
	private final KryptonSection2Builder[] m_sectionBuilderMap = new KryptonSection2Builder[8];
}
