/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.metservice.argon.ArgonText;

/**
 * @author roach
 */
public class TestUnitDiskController {

	@Test
	public void t40()
			throws IOException, DiskException {

		final TestImpSpaceProbe probe = new TestImpSpaceProbe();
		final String[] lines = { "line 1", "", "line3", "" };
		final List<String> zlIn = new ArrayList<String>();
		for (int i = 0; i < lines.length; i++) {
			zlIn.add(lines[i]);
		}

		final File file = File.createTempFile("test40", ".cp");
		UBoron.saveText(probe, file, ArgonText.ISO8859_1, "\n", zlIn);

		final String[] zptOut = UBoron.loadText(probe, file, ArgonText.ISO8859_1);
		Assert.assertArrayEquals(lines, zptOut);
	}

}
