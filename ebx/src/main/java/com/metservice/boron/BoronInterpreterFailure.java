/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

import java.io.File;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
public class BoronInterpreterFailure {

	public File cndirProcess() {
		return m_mpi.cndirProcess;
	}

	public ProcessBuilder processBuilder() {
		return m_mpi.processBuilder;
	}

	public BoronProcessId processId() {
		return m_mpi.processId;
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("mainProcessImage", m_mpi);
		ds.a("ztwReason", m_ztwReason);
		return ds.s();
	}

	public String ztwReason() {
		return m_ztwReason;
	}

	BoronInterpreterFailure(MainProcessImage mpi, Throwable oCause) {
		if (mpi == null) throw new IllegalArgumentException("object is null");
		m_mpi = mpi;
		final String ozReason = oCause == null ? "" : oCause.getMessage();
		m_ztwReason = ozReason == null ? null : ozReason.trim();
	}

	private final MainProcessImage m_mpi;
	private final String m_ztwReason;
}
