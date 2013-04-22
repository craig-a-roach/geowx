/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import java.io.File;
import java.util.Set;

import com.metservice.cobalt.CobaltDimensionException;
import com.metservice.cobalt.CobaltRecord;

/**
 * @author roach
 */
public abstract class KryptonAbstractPlaceEditionTableFactory<T extends IKryptonEditionTable<T>> extends
		KryptonAbstractEditionTableFactory<T> {

	private void putAccepted(IKryptonFileProbe probe, T editionTable, KryptonDataRecord dataRecord)
			throws CobaltDimensionException, KryptonUnpackException, KryptonUnsupportedException {
		final KryptonMetaRecord meta = dataRecord.meta();
		final KryptonCentre centre = meta.centre();
		final CobaltRecord ncubeRec = meta.ncubeRecord();
		final KryptonInterpolator interpolator = dataRecord.newInterpolator();
		for (final KryptonPlaceId placeId : m_places) {
			final float datum = interpolator.bilinear(placeId.longitude, placeId.latitude);
			putEdition(editionTable, new KryptonPlaceEdition(centre, ncubeRec, placeId, datum));
		}
	}

	protected abstract boolean accept(KryptonCentre centre, CobaltRecord ncubeRec)
			throws CobaltDimensionException;

	protected abstract void putEdition(T editionTable, KryptonPlaceEdition ed);

	@Override
	protected void putTable(IKryptonFileProbe probe, T editionTable, KryptonDataRecord dataRecord)
			throws CobaltDimensionException, KryptonUnpackException, KryptonUnsupportedException {
		final KryptonMetaRecord meta = dataRecord.meta();
		final KryptonCentre centre = meta.centre();
		final CobaltRecord ncubeRec = meta.ncubeRecord();
		if (accept(centre, ncubeRec)) {
			putAccepted(probe, editionTable, dataRecord);
		}
	}

	protected KryptonAbstractPlaceEditionTableFactory(IKryptonFileProbe probe, KryptonDecoder decoder, File gridFile,
			Set<KryptonPlaceId> places) {
		super(probe, decoder, gridFile);
		if (places == null) throw new IllegalArgumentException("object is null");
		m_places = places;
	}

	private final Set<KryptonPlaceId> m_places;
}
