/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import com.metservice.argon.Ds;
import com.metservice.cobalt.CobaltRecord;

/**
 * @author roach
 */
public class KryptonMetaRecord {

	public KryptonCentre centre() {
		return m_centre;
	}

	public IKryptonName generatingProcess() {
		return m_generatingProcess;
	}

	public KryptonGridDecode gridDecode() {
		return m_gridDecode;
	}

	public CobaltRecord ncubeRecord() {
		return m_ncubeRecord;
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("ncubeRecord", m_ncubeRecord);
		ds.a("centre", m_centre);
		ds.a("gridDecode", m_gridDecode);
		ds.a("generatingProcess", m_generatingProcess);
		return ds.s();
	}

	public KryptonMetaRecord(CobaltRecord ncubeRecord, KryptonCentre centre, KryptonGridDecode gridDecode,
			IKryptonName generatingProcess) {
		if (ncubeRecord == null) throw new IllegalArgumentException("object is null");
		if (centre == null) throw new IllegalArgumentException("object is null");
		if (gridDecode == null) throw new IllegalArgumentException("object is null");
		if (generatingProcess == null) throw new IllegalArgumentException("object is null");
		m_ncubeRecord = ncubeRecord;
		m_centre = centre;
		m_gridDecode = gridDecode;
		m_generatingProcess = generatingProcess;
	}
	private final CobaltRecord m_ncubeRecord;
	private final KryptonCentre m_centre;
	private final KryptonGridDecode m_gridDecode;
	private final IKryptonName m_generatingProcess;
}
