/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emboron;

import java.io.File;

import org.junit.Test;

import com.metservice.boron.BoronException;
import com.metservice.boron.BoronSpace;
import com.metservice.boron.BoronSpaceCfg;
import com.metservice.boron.BoronSpaceId;
import com.metservice.carbon.TestCarbon;

/**
 * @author roach
 */
public class TestUnitLaunch extends TestCarbon {

	@Test(timeout = 15000)
	public void t50()
			throws BoronException {
		final BoronSpaceId id = BoronSpaceId.newInstance("testemboron");
		final BoronSpaceCfg cfg = new BoronSpaceCfg();
		cfg.setWorkHistoryDepth(10);
		cfg.setCooldownSecs(20);
		final BoronSpace bspace = new BoronSpace(id, cfg);
		bspace.start();

		final EmBoronFileSystemHomes bhomes = new EmBoronFileSystemHomes();
		bhomes.put("temp", new File("/usr/local/bin"));
		final BoronEmCaller caller = new BoronEmCaller("{jarExists: false}");
		final BoronEmInstaller installer = new BoronEmInstaller(bspace, bhomes, caller);

		final Expectation x = new Expectation("throw", "Error: Unable to access jarfile bogus.jar");
		jsassert(x, "launch", installer);

		bspace.shutdown();

	}
}
