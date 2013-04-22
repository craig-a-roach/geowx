/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import com.metservice.cobalt.CobaltMember;
import com.metservice.cobalt.CobaltParameter;
import com.metservice.cobalt.ICobaltPrognosis;
import com.metservice.cobalt.ICobaltSurface;

/**
 * @author roach
 */
class SectionPD2Template00Reader extends SectionPD2TemplateReader {

	@Override
	public KryptonProductDecode newProduct(KryptonDecoder d)
			throws KryptonTableException, KryptonCodeException {
		final CobaltParameter parameter = select(d.parameterDecoder);
		final CodeDescription gpt = select(d.generatingProcessDecoder);
		final ICobaltPrognosis prognosis = selectPoint(d.validityDecoder);
		final ICobaltSurface surface = selectSurface(d.surfaceDecoder);
		final CobaltMember member = CobaltMember.Singleton;
		return new KryptonProductDecode(parameter, prognosis, surface, member, gpt);
	}

	public SectionPD2Template00Reader(short discipline, long tsAnalysis, SectionPD2Reader base) {
		super(discipline, tsAnalysis, base, 0);
	}
}
