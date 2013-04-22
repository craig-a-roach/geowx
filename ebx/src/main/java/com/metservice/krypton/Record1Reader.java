/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import com.metservice.cobalt.CobaltAnalysis;
import com.metservice.cobalt.CobaltMember;
import com.metservice.cobalt.CobaltParameter;
import com.metservice.cobalt.CobaltRecord;
import com.metservice.cobalt.ICobaltGeography;
import com.metservice.cobalt.ICobaltPrognosis;
import com.metservice.cobalt.ICobaltResolution;
import com.metservice.cobalt.ICobaltSurface;

/**
 * @author roach
 */
class Record1Reader extends KryptonRecordReader {

	private SectionBD1Reader newSectionBD1Reader()
			throws KryptonRecordException, KryptonReadException {
		final String Section = CSection.BD1;
		final int bcSection = inSectionLength3(Section, CSection.BD1_bcLo);
		final SectionBD1Reader r = new SectionBD1Reader(inSection(CSection.BD1, bcSection - 3));
		return r;
	}

	private SectionGD1Reader newSectionGD1Reader()
			throws KryptonRecordException, KryptonReadException {
		final String Section = CSection.GD1;
		final int bcSection = inSectionLength3(Section, CSection.GD1_bcLo);
		final SectionGD1Reader r = new SectionGD1Reader(inSection(Section, bcSection - 3));
		if (r.hasPV()) {
			final String spec = CSection.GD1("4,5") + "=" + r.b04_NV() + "," + r.b05_PV_or_PL();
			final String m = "GDS vertical coordinate lists are not supported; " + spec;
			throw new KryptonRecordException(Section, m_biRecReFile, m_biPosReRec, m_ri, m);
		}
		return r;
	}

	private SectionPD1Reader newSectionPD1Reader()
			throws KryptonRecordException, KryptonReadException {
		final String Section = CSection.PD1;
		final int bcSection = inSectionLength3(Section, CSection.PD1_bcLo);
		final SectionPD1Reader r = new SectionPD1Reader(inSection(CSection.PD1, bcSection - 3));
		if (!r.haveGridDescriptionSection()) {
			final String m = "GDS required for grid " + r.b07_grid();
			throw new KryptonRecordException(Section, m_biRecReFile, m_biPosReRec, m_ri, m);
		}
		return r;
	}

	private KryptonMetaRecord parseMeta(KryptonDecoder dc, SectionPD1Reader rPD, SectionGD1Reader rGD)
			throws KryptonTableException, KryptonCodeException {
		assert dc != null;
		assert rPD != null;
		assert rGD != null;
		final KryptonCentre centre = dc.selectCentre(rPD);
		final CobaltAnalysis A = dc.newAnalysis(rPD);
		final CobaltParameter Pa = dc.selectParameter(rPD);
		final ICobaltPrognosis Pr = dc.newPrognosis(rPD);
		final ICobaltSurface S = dc.newSurface(rPD);
		final CobaltMember M = dc.newMember(rPD);
		final IKryptonName gpt = dc.newGeneratingProcessId(rPD);
		final KryptonGridDecode gridDecode = dc.newGridDecode(rGD);
		final ICobaltResolution R = gridDecode.resolution;
		final ICobaltGeography G = gridDecode.geography;
		final CobaltRecord ncubeRecord = CobaltRecord.newInstance(A, Pa, Pr, S, M, R, G);
		return new KryptonMetaRecord(ncubeRecord, centre, gridDecode, gpt);
	}

	@Override
	public boolean moreSections() {
		return false;
	}

	@Override
	public KryptonDataRecord newDataRecord()
			throws KryptonTableException, KryptonCodeException, KryptonRecordException, KryptonReadException {
		final SectionPD1Reader rPD = newSectionPD1Reader();
		final SectionGD1Reader rGD = newSectionGD1Reader();
		skipSectionBM1(rPD);
		final SectionBD1Reader rBD = newSectionBD1Reader();
		endSection();
		final KryptonDecoder dc = decoder();
		final KryptonMetaRecord meta = parseMeta(dc, rPD, rGD);
		final KryptonGridDecode gd = meta.gridDecode();
		final IKryptonDataSource ds = dc.newDataSource(rPD, rBD);
		return new KryptonDataRecord(meta, gd, ds);
	}

	@Override
	public KryptonMetaRecord newMetaRecord()
			throws KryptonCodeException, KryptonTableException, KryptonRecordException, KryptonReadException {
		final SectionPD1Reader rPD = newSectionPD1Reader();
		final SectionGD1Reader rGD = newSectionGD1Reader();
		skipSectionBM1(rPD);
		skipSectionBD1();
		endSection();
		final KryptonDecoder dc = decoder();
		return parseMeta(dc, rPD, rGD);
	}

	public void skipSectionBD1()
			throws KryptonRecordException, KryptonReadException {
		final String Section = CSection.BD1;
		final int bcSection = inSectionLength3(Section, CSection.BD1_bcLo);
		skipSection(Section, bcSection - 3);
	}

	public void skipSectionBM1(SectionPD1Reader rPD)
			throws KryptonRecordException, KryptonReadException {
		if (!rPD.haveBitmapSection()) return;
		final String Section = CSection.BM1;
		final int bcSection = inSectionLength3(Section, CSection.BM1_bcLo);
		skipSection(Section, bcSection - 3);
	}

	public Record1Reader(KryptonFileReader gfr, long bcMsg, long biRecReFile, long biPosReRec, int ri) {
		super(gfr, bcMsg, biRecReFile, biPosReRec, ri);
	}
}
