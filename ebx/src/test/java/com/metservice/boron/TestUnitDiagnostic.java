/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author roach
 */
public class TestUnitDiagnostic {

	@Test(timeout = 15000)
	public void t50()
			throws BoronException, InterruptedException {
		final BoronSpaceId id = BoronSpaceId.newInstance("testdiagnostic");
		final BoronSpaceCfg cfg = new BoronSpaceCfg();
		final BoronSpace space = new BoronSpace(id, cfg);
		space.start();
		final BoronDiagnosticScript script = BoronDiagnosticScript.javaVersion();
		final String emit = space.emit(script);
		Assert.assertTrue(emit.toLowerCase().contains("java"));
		Assert.assertTrue("idle", space.waitNotBusy(5000));
		space.shutdown();
	}

}
