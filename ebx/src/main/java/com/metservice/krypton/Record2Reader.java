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
class Record2Reader extends KryptonRecordReader {

	private void endSubSection(SectionID2Reader rID, SectionGD2Reader rGD)
			throws KryptonReadException, KryptonRecordException {
		if (rID == null) throw new IllegalArgumentException("object is null");
		if (rGD == null) throw new IllegalArgumentException("object is null");
		if (!hasMoreSections()) {
			endSection();
			m_oStack = null;
			return;
		}
		final Section2Tag tagPD = newTag();
		if (tagPD.no == CSection.PD2_No) {
			m_oStack = new Section2StackGD(tagPD, rID, rGD);
			return;
		}
		final String p = "Unexpected repeat structure starting at " + tagPD;
		throw new KryptonRecordException("EndSub", m_biRecReFile, m_biPosReRec, m_ri, p);
	}

	private SectionBD2Reader newSectionBD2Reader()
			throws KryptonRecordException, KryptonReadException {
		final String Section = CSection.BD2;
		final int bcSection = inSectionLength4(Section, CSection.BD2_bcLo);
		inSectionNo(Section, CSection.BD2_No);
		final SectionBD2Reader r = new SectionBD2Reader(inSection(Section, UGrib.bcBodyG2(bcSection)));
		return r;
	}

	private Section2Tag newSectionBD2Tag()
			throws KryptonRecordException, KryptonReadException {
		final String Section = CSection.BD2;
		final int SectionNo = CSection.BD2_No;
		final int bcSection = inSectionLength4(Section, CSection.BD2_bcLo);
		inSectionNo(Section, SectionNo);
		return new Section2Tag(bcSection, SectionNo);
	}

	private SectionBM2Reader newSectionBM2Reader()
			throws KryptonRecordException, KryptonReadException {
		final String Section = CSection.BM2;
		final int bcSection = inSectionLength4(Section, CSection.BM2_bcLo);
		inSectionNo(Section, CSection.BM2_No);
		final SectionBM2Reader r = new SectionBM2Reader(inSection(Section, UGrib.bcBodyG2(bcSection)));
		return r;
	}

	private Section2Tag newSectionBM2Tag()
			throws KryptonRecordException, KryptonReadException {
		final String Section = CSection.BM2;
		final int SectionNo = CSection.BM2_No;
		final int bcSection = inSectionLength4(Section, CSection.BM2_bcLo);
		inSectionNo(Section, SectionNo);
		return new Section2Tag(bcSection, SectionNo);
	}

	private SectionDR2Reader newSectionDR2Reader()
			throws KryptonRecordException, KryptonReadException {
		final String Section = CSection.DR2;
		final int bcSection = inSectionLength4(Section, CSection.DR2_bcLo);
		inSectionNo(Section, CSection.DR2_No);
		final SectionDR2Reader r = new SectionDR2Reader(inSection(Section, UGrib.bcBodyG2(bcSection)));
		return r;
	}

	private Section2Tag newSectionDR2Tag()
			throws KryptonRecordException, KryptonReadException {
		final String Section = CSection.DR2;
		final int SectionNo = CSection.DR2_No;
		final int bcSection = inSectionLength4(Section, CSection.DR2_bcLo);
		inSectionNo(Section, SectionNo);
		return new Section2Tag(bcSection, SectionNo);
	}

	private SectionGD2Reader newSectionGD2Reader()
			throws KryptonRecordException, KryptonReadException {
		if (m_oStack != null) return m_oStack.sectionGD2Reader();
		Section2Tag tagLU_GD = newTag();
		if (tagLU_GD.no == CSection.LU2_No) {
			skipSectionLU2(tagLU_GD);
			tagLU_GD = newTag();
		}
		final SectionGD2Reader rGD = newSectionGD2Reader(tagLU_GD);
		return rGD;
	}

	private SectionGD2Reader newSectionGD2Reader(Section2Tag tag)
			throws KryptonRecordException, KryptonReadException {
		final String Section = CSection.GD2;
		validateSectionNo(Section, tag.no, CSection.GD2_No);
		validateSectionLength(Section, tag.bc, CSection.GD2_bcLo);
		final SectionGD2Reader r = new SectionGD2Reader(inSection(Section, tag.bcBody()));
		return r;
	}

	private SectionID2Reader newSectionID2Reader()
			throws KryptonRecordException, KryptonReadException {
		if (m_oStack != null) return m_oStack.sectionID2Reader();
		final String Section = CSection.ID2;
		final int bcSection = inSectionLength4(Section, CSection.ID2_bcLo);
		inSectionNo(Section, CSection.ID2_No);
		final SectionID2Reader r = new SectionID2Reader(inSection(Section, UGrib.bcBodyG2(bcSection)));
		return r;
	}

	private SectionPD2Reader newSectionPD2Reader()
			throws KryptonRecordException, KryptonReadException {
		if (m_oStack != null) return newSectionPD2Reader(m_oStack.tagPD());
		final String Section = CSection.PD2;
		final int bcSection = inSectionLength4(Section, CSection.PD2_bcLo);
		inSectionNo(Section, CSection.PD2_No);
		final SectionPD2Reader r = new SectionPD2Reader(inSection(Section, UGrib.bcBodyG2(bcSection)));
		return r;
	}

	private SectionPD2Reader newSectionPD2Reader(Section2Tag tagPD)
			throws KryptonRecordException, KryptonReadException {
		final String Section = CSection.PD2;
		validateSectionNo(Section, tagPD.no, CSection.PD2_No);
		validateSectionLength(Section, tagPD.bc, CSection.PD2_bcLo);
		final SectionPD2Reader r = new SectionPD2Reader(inSection(Section, tagPD.bcBody()));
		return r;
	}

	private Section2Tag newTag()
			throws KryptonReadException {
		final int bc = inSectionLength4();
		final int no = inSectionNo();
		return new Section2Tag(bc, no);
	}

	private KryptonMetaRecord parseMeta(KryptonDecoder dc, SectionID2Reader rID, SectionGD2Reader rGD, SectionPD2Reader rPD)
			throws KryptonTableException, KryptonCodeException {
		assert dc != null;
		assert rID != null;
		final KryptonCentre centre = dc.selectCentre(rID);
		final CobaltAnalysis A = dc.newAnalysis(rID);
		final KryptonProductDecode kP = dc.selectProduct(m_discipline, A.ts, rPD);
		final CobaltParameter Pa = kP.parameter;
		final ICobaltPrognosis Pr = kP.prognosis;
		final ICobaltSurface S = kP.surface;
		final CobaltMember M = kP.member;
		final IKryptonName gp = kP.generatingProcess;
		final KryptonGridDecode gridDecode = dc.newGridDecode(rGD);
		final ICobaltResolution R = gridDecode.resolution;
		final ICobaltGeography G = gridDecode.geography;
		final CobaltRecord ncubeRecord = CobaltRecord.newInstance(A, Pa, Pr, S, M, R, G);
		return new KryptonMetaRecord(ncubeRecord, centre, gridDecode, gp);
	}

	private void skipSectionBD2()
			throws KryptonRecordException, KryptonReadException {
		final Section2Tag tag = newSectionBD2Tag();
		skipSection(CSection.BD2, tag.bcBody());
	}

	private void skipSectionBM2()
			throws KryptonRecordException, KryptonReadException {
		final Section2Tag tag = newSectionBM2Tag();
		skipSection(CSection.BM2, tag.bcBody());
	}

	private void skipSectionDR2()
			throws KryptonRecordException, KryptonReadException {
		final Section2Tag tag = newSectionDR2Tag();
		skipSection(CSection.DR2, tag.bcBody());
	}

	private void skipSectionLU2(Section2Tag tag)
			throws KryptonReadException {
		assert tag != null;
		skipSection(CSection.LU2, tag.bcBody());
	}

	@Override
	public boolean moreSections() {
		return m_oStack != null;
	}

	@Override
	public KryptonDataRecord newDataRecord()
			throws KryptonCodeException, KryptonTableException, KryptonRecordException, KryptonReadException {
		final SectionID2Reader rID = newSectionID2Reader();
		final SectionGD2Reader rGD = newSectionGD2Reader();
		final SectionPD2Reader rPD = newSectionPD2Reader();
		final SectionDR2Reader rDR = newSectionDR2Reader();
		final SectionBM2Reader rBM = newSectionBM2Reader();
		final SectionBD2Reader rBD = newSectionBD2Reader();
		endSubSection(rID, rGD);

		final KryptonDecoder dc = decoder();
		final KryptonMetaRecord meta = parseMeta(dc, rID, rGD, rPD);
		final KryptonGridDecode gd = meta.gridDecode();
		m_oBitmap = dc.createBitmapSource(rBM, m_oBitmap);
		final IKryptonDataSource ds = dc.newDataSource(rDR, m_oBitmap, rBD);
		return new KryptonDataRecord(meta, gd, ds);
	}

	@Override
	public KryptonMetaRecord newMetaRecord()
			throws KryptonCodeException, KryptonTableException, KryptonRecordException, KryptonReadException {
		final SectionID2Reader rID = newSectionID2Reader();
		final SectionGD2Reader rGD = newSectionGD2Reader();
		final SectionPD2Reader rPD = newSectionPD2Reader();
		skipSectionDR2();
		skipSectionBM2();
		skipSectionBD2();
		endSubSection(rID, rGD);

		final KryptonDecoder dc = decoder();
		return parseMeta(dc, rID, rGD, rPD);
	}

	public Record2Reader(KryptonFileReader gfr, long bcMsg, long biRecReFile, long biPosReRec, int ri, short discipline) {
		super(gfr, bcMsg, biRecReFile, biPosReRec, ri);
		m_discipline = discipline;
	}
	private final short m_discipline;
	private Section2StackGD m_oStack;
	private IKryptonBitmapSource m_oBitmap;
}
