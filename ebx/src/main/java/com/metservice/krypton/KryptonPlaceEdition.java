/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import com.metservice.argon.Ds;
import com.metservice.cobalt.CobaltAnalysis;
import com.metservice.cobalt.CobaltDimensionException;
import com.metservice.cobalt.CobaltMember;
import com.metservice.cobalt.CobaltParameter;
import com.metservice.cobalt.CobaltRecord;
import com.metservice.cobalt.ICobaltPrognosis;
import com.metservice.cobalt.ICobaltSurface;

/**
 * @author roach
 */
public class KryptonPlaceEdition {

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("centre", centre);
		ds.a("analysis", analysis);
		ds.a("prognosis", prognosis);
		ds.a("parameter", parameter);
		ds.a("surface", surface);
		ds.a("placeId", placeId);
		ds.a("member", member);
		ds.a("datum", analysis);
		return ds.s();
	}

	public KryptonPlaceEdition(KryptonCentre centre, CobaltRecord ncubeRec, KryptonPlaceId placeId, float datum)
			throws CobaltDimensionException {
		if (centre == null) throw new IllegalArgumentException("object is null");
		if (ncubeRec == null) throw new IllegalArgumentException("object is null");
		if (placeId == null) throw new IllegalArgumentException("object is null");
		this.centre = centre;
		this.analysis = ncubeRec.analysis();
		this.prognosis = ncubeRec.prognosis();
		this.parameter = ncubeRec.parameter();
		this.surface = ncubeRec.surface();
		this.placeId = placeId;
		this.member = ncubeRec.member();
		this.datum = datum;
	}
	public final KryptonCentre centre;
	public final CobaltAnalysis analysis;
	public final ICobaltPrognosis prognosis;
	public final CobaltParameter parameter;
	public final ICobaltSurface surface;
	public final KryptonPlaceId placeId;
	public final CobaltMember member;
	public final float datum;
}
