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
class MainProcessImage {

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("processId", processId);
		ds.a("cndirProcess", cndirProcess);
		ds.a("processBuilder.command", processBuilder.command());
		ds.a("processBuilder.redirectErrorStream", processBuilder.redirectErrorStream());
		ds.a("processBuilder.directory", processBuilder.directory());
		ds.a("processBuilder.environment", processBuilder.environment());
		return ds.s();
	}

	public MainProcessImage(BoronProcessId processId, File cndirProcess, ProcessBuilder processBuilder) {
		if (processId == null) throw new IllegalArgumentException("object is null");
		if (cndirProcess == null) throw new IllegalArgumentException("object is null");
		if (processBuilder == null) throw new IllegalArgumentException("object is null");

		this.processId = processId;
		this.cndirProcess = cndirProcess;
		this.processBuilder = processBuilder;
	}

	public final BoronProcessId processId;
	public final File cndirProcess;
	public final ProcessBuilder processBuilder;
}
