/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import com.metservice.cobalt.CobaltAnalysis;
import com.metservice.cobalt.CobaltMember;
import com.metservice.cobalt.CobaltParameter;
import com.metservice.cobalt.ICobaltPrognosis;
import com.metservice.cobalt.ICobaltSurface;

/**
 * @author roach
 */
public class KryptonDecoder {

	private KryptonCentre newCentre(String source, int centre, int subCentre)
			throws KryptonTableException, KryptonCodeException {
		final CodeDescription centreDesc = centreDecoder.select(source, centre);
		final CodeDescription oSubCentreDesc = subCentreDecoder.find(source, centre, subCentre);
		final String oqccSubCentre = oSubCentreDesc == null ? null : oSubCentreDesc.qcctwDesc;
		return new KryptonCentre(centre, subCentre, centreDesc.qcctwDesc, oqccSubCentre);
	}

	private String problemG1(String m, String ctRef) {
		return m + " (GRIB1 Code Table " + ctRef + ")";
	}

	private String problemG2(String m, String ctRef) {
		return m + " (GRIB2 Code Table " + ctRef + ")";
	}

	public IKryptonBitmapSource createBitmapSource(SectionBM2Reader rBM, IKryptonBitmapSource oExBitmap)
			throws KryptonCodeException {
		if (rBM == null) throw new IllegalArgumentException("object is null");
		final int bitmapIndicator = rBM.b06_bitmapIndicator();
		if (bitmapIndicator == 0) {
			final SectionBM2TemplateReader template = new SectionBM2Template00Reader(rBM);
			return template.newBitmapSource();
		}
		if (bitmapIndicator == 255) return null;
		if (bitmapIndicator == 254) return oExBitmap;
		final String m = "Bitmap indicator " + bitmapIndicator + " not supported";
		throw new KryptonCodeException(CSection.BM2("6"), problemG2(m, "6.0"));
	}

	public CobaltAnalysis newAnalysis(SectionID2Reader r)
			throws KryptonCodeException {
		final String source = CSection.ID2("12,13-19");
		final short significanceRef = r.b12_significanceRef();
		if (significanceRef != 0 && significanceRef != 1) {
			final String m = "Significance of reference time " + significanceRef + " not supported";
			throw new KryptonCodeException(source, problemG2(m, "1.2"));
		}
		final long ts = r.b1319_ref();
		return new CobaltAnalysis(ts);
	}

	public CobaltAnalysis newAnalysis(SectionPD1Reader r)
			throws KryptonCodeException {
		final long ts = r.b1317_25_ref();
		return new CobaltAnalysis(ts);
	}

	public IKryptonDataSource newDataSource(SectionDR2Reader rDR, IKryptonBitmapSource oBitmap, SectionBD2Reader rBD)
			throws KryptonCodeException, KryptonTableException {
		if (rDR == null) throw new IllegalArgumentException("object is null");
		if (rBD == null) throw new IllegalArgumentException("object is null");
		final String source = CSection.DR2("10-11");
		final int templateNo = rDR.b1011_templateNo();
		SectionDR2TemplateReader tr;
		switch (templateNo) {
			case 0:
				tr = new SectionDR2Template00Reader(rDR);
			break;
			case 3:
				tr = new SectionDR2Template03Reader(rDR);
			break;
			case 40:
				tr = new SectionDR2Template40Reader(rDR);
			break;
			default:
				final String m = "Data representation template " + templateNo + " not supported";
				throw new KryptonCodeException(source, problemG2(m, "5.0"));
		}
		return tr.newDataSource(this, oBitmap, rBD);
	}

	public IKryptonDataSource newDataSource(SectionPD1Reader rPD, SectionBD1Reader rBD)
			throws KryptonCodeException {
		if (rPD == null) throw new IllegalArgumentException("object is null");
		if (rBD == null) throw new IllegalArgumentException("object is null");
		final String source = CSection.BD1("4");
		final short flag = rBD.b04hi_flag();
		final int flag01 = flag & 0x0C;
		switch (flag01) {
			case 0:
				return dataDecoder.newType00(source, rPD, rBD);
		}
		final String m = "Binary packing type " + flag + " not supported";
		throw new KryptonCodeException(source, problemG1(m, "11"));
	}

	public IKryptonName newGeneratingProcessId(SectionPD1Reader r)
			throws KryptonCodeException, KryptonTableException {
		if (r == null) throw new IllegalArgumentException("object is null");
		final String source = CSection.PD1("6");
		final int process = r.b06_process();
		return generatingProcessDecoder.select(source, process);
	}

	public KryptonGridDecode newGridDecode(SectionGD1Reader r)
			throws KryptonCodeException {
		if (r == null) throw new IllegalArgumentException("object is null");
		final String source = CSection.GD1("6");
		final short type = r.b06_type();
		switch (type) {
			case 0:
				return gridDecoder.newGrid(new SectionGD1Type00Reader(r));
			case 1:
				return gridDecoder.newGrid(new SectionGD1Type01Reader(r));
			default:
				final String m = "Data representation type " + type + " not supported";
				throw new KryptonCodeException(source, problemG1(m, "6"));
		}
	}

	public KryptonGridDecode newGridDecode(SectionGD2Reader r)
			throws KryptonCodeException {
		if (r == null) throw new IllegalArgumentException("object is null");
		final short sourceOfDefinition = r.b06_sourceOfDefinition();
		if (sourceOfDefinition != 0) {
			final String m = "Source of grid definition " + sourceOfDefinition + " not supported";
			throw new KryptonCodeException(CSection.GD2("6"), problemG2(m, "3.0"));
		}
		final int b11 = r.b11();
		if (b11 != 0) {
			final String m = "Quasi-regular grids (Octet 11=" + b11 + ") not supported";
			throw new KryptonCodeException(CSection.GD2("11"), m);
		}
		final int b12 = r.b12();
		if (b12 != 0) {
			final String m = "Interpolation of list " + b12 + " not supported";
			throw new KryptonCodeException(CSection.GD2("12"), problemG2(m, "3.11"));
		}
		final int templateNo = r.b1314_template();
		switch (templateNo) {
			case 0:
				return gridDecoder.newGrid(new SectionGD2Template00Reader(r));
			default:
				final String m = "Grid definition template  " + templateNo + " not supported";
				throw new KryptonCodeException(CSection.GD2("13-14"), problemG2(m, "3.1"));
		}
	}

	public CobaltMember newMember(SectionPD1Reader r)
			throws KryptonCodeException {
		final int id = r.ensembleMember();
		return CobaltMember.newInstance(id);
	}

	public ICobaltPrognosis newPrognosis(SectionPD1Reader r)
			throws KryptonCodeException {
		if (r == null) throw new IllegalArgumentException("object is null");
		final String source = CSection.PD1("18-21");
		final short timeunit = r.b18_timeunit();
		final int P1 = r.b19_P1();
		final int P2 = r.b20_P2();
		final int timerange = r.b21_timerange();
		return validityDecoder.newPrognosisG1(source, timeunit, P1, P2, timerange);
	}

	public ICobaltSurface newSurface(SectionPD1Reader r)
			throws KryptonCodeException {
		if (r == null) throw new IllegalArgumentException("object is null");
		final String source = CSection.PD1("10-12");
		final short leveltype = r.b10_leveltype();
		final int L1 = r.b11_L1();
		final int L2 = r.b12_L2();
		return surfaceDecoder.newSurfaceG1(source, leveltype, L1, L2);
	}

	public KryptonCentre selectCentre(SectionID2Reader r)
			throws KryptonCodeException, KryptonTableException {
		if (r == null) throw new IllegalArgumentException("object is null");
		final String source = CSection.ID2("6-9");
		final int centre = r.b0607_originatingCentre();
		final int subCentre = r.b0809_originatingSubCentre();
		return newCentre(source, centre, subCentre);
	}

	public KryptonCentre selectCentre(SectionPD1Reader r)
			throws KryptonCodeException, KryptonTableException {
		if (r == null) throw new IllegalArgumentException("object is null");
		final String source = CSection.PD1("5,26");
		final short centre = r.b05_centre();
		final short subCentre = r.b26_subcentre();
		return newCentre(source, centre, subCentre);
	}

	public CobaltParameter selectParameter(SectionPD1Reader r)
			throws KryptonCodeException, KryptonTableException {
		if (r == null) throw new IllegalArgumentException("object is null");
		final String source = CSection.PD1("4,5,9,26");
		final short centre = r.b05_centre();
		final short subcentre = r.b26_subcentre();
		final short table = r.b04_table();
		final short param = r.b09_param();
		return parameterDecoder.selectG1(source, centre, subcentre, table, param);
	}

	public KryptonProductDecode selectProduct(short discipline, long tsAnalysis, SectionPD2Reader r)
			throws KryptonCodeException, KryptonTableException {
		if (r == null) throw new IllegalArgumentException("object is null");
		final int coordinateValues = r.b0607_coordinateValues();
		if (coordinateValues != 0) {
			final String m = "Coordinate values " + coordinateValues + " not supported";
			throw new KryptonCodeException(CSection.PD2("6-7"), m);
		}

		final int templateNo = r.b0809_templateNo();
		final SectionPD2TemplateReader tr;
		switch (templateNo) {
			case 0:
				tr = new SectionPD2Template00Reader(discipline, tsAnalysis, r);
			break;
			case 1:
				tr = new SectionPD2Template01Reader(discipline, tsAnalysis, r);
			break;
			case 8:
				tr = new SectionPD2Template08Reader(discipline, tsAnalysis, r);
			break;
			case 11:
				tr = new SectionPD2Template11Reader(discipline, tsAnalysis, r);
			break;
			default: {
				final String m = "Product definition template  " + templateNo + " not supported";
				throw new KryptonCodeException(CSection.PD2("8-9"), problemG2(m, "4.0"));
			}
		}
		return tr.newProduct(this);
	}

	public KryptonDecoder(IKryptonProbe probe, KryptonDecoderConfig cfg) {
		if (probe == null) throw new IllegalArgumentException("object is null");
		if (cfg == null) throw new IllegalArgumentException("object is null");
		this.centreDecoder = cfg.newDecoderCentre(probe);
		this.subCentreDecoder = cfg.newDecoderSubCentre(probe);
		this.generatingProcessDecoder = cfg.newGeneratingProcess(probe);
		this.parameterDecoder = cfg.newDecoderParameter(probe);
		this.validityDecoder = new ValidityDecoder();
		this.surfaceDecoder = new SurfaceDecoder();
		this.gridDecoder = new GridDecoder();
		this.dataDecoder = new DataDecoder();
	}
	public final CentreDecoder centreDecoder;
	public final CentreDecoder subCentreDecoder;
	public final GeneratingProcessDecoder generatingProcessDecoder;
	public final ParameterDecoder parameterDecoder;
	public final ValidityDecoder validityDecoder;
	public final SurfaceDecoder surfaceDecoder;
	public final GridDecoder gridDecoder;
	public final DataDecoder dataDecoder;
}
