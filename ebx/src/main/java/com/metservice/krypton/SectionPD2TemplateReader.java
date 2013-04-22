/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import com.metservice.argon.DateFormatter;
import com.metservice.cobalt.CobaltMember;
import com.metservice.cobalt.CobaltParameter;
import com.metservice.cobalt.ICobaltPrognosis;
import com.metservice.cobalt.ICobaltSurface;

/**
 * @author roach
 */
abstract class SectionPD2TemplateReader extends SectionPD2Reader {

	protected String templateSpec() {
		return "4." + m_templateNo;
	}

	public CobaltMember newEnsembleMember() {
		final short typeOfEnsembleForecast = shortu1(35);
		final short perturbationNumber = shortu1(36);
		if (typeOfEnsembleForecast == 0 || typeOfEnsembleForecast == 1) return CobaltMember.Control;
		return CobaltMember.newInstance(perturbationNumber);
	}

	public abstract KryptonProductDecode newProduct(KryptonDecoder d)
			throws KryptonTableException, KryptonCodeException;

	public CodeDescription select(GeneratingProcessDecoder decoder)
			throws KryptonTableException, KryptonCodeException {
		if (decoder == null) throw new IllegalArgumentException("object is null");
		final short code = shortu1(12);
		final String source = CSection.PD2(templateSpec(), "12");
		return decoder.select(source, code);
	}

	public CobaltParameter select(ParameterDecoder decoder)
			throws KryptonTableException, KryptonCodeException {
		if (decoder == null) throw new IllegalArgumentException("object is null");
		final short category = shortu1(10);
		final short number = shortu1(11);
		final String source = CSection.PD2(templateSpec(), "10-11");
		return decoder.selectG2(source, m_discipline, category, number);
	}

	public ICobaltPrognosis selectAggregate(ValidityDecoder decoder, int baseOctetEnd, int baseOctetRange)
			throws KryptonTableException, KryptonCodeException {
		final String specOctetEnd = baseOctetEnd + "-" + (baseOctetEnd + 6);
		final String source = CSection.PD2(templateSpec(), specOctetEnd);
		final short unitFrom = shortu1(18);
		final int valueFrom = int4(19);
		final int year = int2(baseOctetEnd);
		final int moy = intu1(baseOctetEnd + 2);
		final int dom = intu1(baseOctetEnd + 3);
		final int hod = intu1(baseOctetEnd + 4);
		final int moh = intu1(baseOctetEnd + 5);
		final int sec = intu1(baseOctetEnd + 6);
		final long tsEnd = UGrib.tsG2(CSection.PD2(templateSpec(), specOctetEnd), year, moy, dom, hod, moh, sec);
		final long ssecsToexL = (tsEnd - m_tsAnalysis) / 1000L;
		if (ssecsToexL < Integer.MIN_VALUE || ssecsToexL > Integer.MAX_VALUE) {
			final String m = "End time " + DateFormatter.newT8FromTs(tsEnd) + " out of range";
			throw new KryptonCodeException(source, m);
		}
		final int ssecsToex = (int) ssecsToexL;

		final int n = intu1(baseOctetRange);
		short processType = 255;
		if (n == 1) {
			processType = shortu1(baseOctetRange + 5);
		}
		return decoder.newPrognosisAggregateG2(source, processType, unitFrom, valueFrom, ssecsToex);
	}

	public ICobaltPrognosis selectPoint(ValidityDecoder decoder)
			throws KryptonTableException, KryptonCodeException {
		final short unit = shortu1(18);
		final int value = int4(19);
		final String source = CSection.PD2(templateSpec(), "18-19");
		return decoder.newPrognosisPointG2(source, unit, value);
	}

	public ICobaltSurface selectSurface(SurfaceDecoder decoder)
			throws KryptonCodeException {
		final short type1 = shortu1(23);
		final int scale1 = intu1(24);
		final int value1 = int4(25);
		final short type2 = shortu1(29);
		final int scale2 = intu1(30);
		final int value2 = int4(31);
		final String source = CSection.PD2(templateSpec(), "23-34");
		return decoder.newSurfaceG2(source, type1, scale1, value1, type2, scale2, value2);
	}

	public SectionPD2TemplateReader(short discipline, long tsAnalysis, SectionPD2Reader base, int templateNo) {
		super(base);
		m_discipline = discipline;
		m_tsAnalysis = tsAnalysis;
		m_templateNo = templateNo;
	}
	protected final short m_discipline;
	protected final long m_tsAnalysis;
	protected final int m_templateNo;
}
