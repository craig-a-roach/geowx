/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import org.junit.Assert;
import org.junit.Test;

import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.file.ArgonFileManifest;
import com.metservice.cobalt.CobaltParameter;

/**
 * @author roach
 */
public class TestUnit1ParameterDecoder {

	@Test
	public void t50() {
		final TestImpProbe probe = new TestImpProbe();
		final ParameterDecoder decoder = new ParameterDecoder(probe);
		try {
			final CobaltParameter MX2T6 = decoder.selectG1("t50", (short) 98, (short) 0, (short) 128, (short) 121);
			Assert.assertEquals("MX2T6", MX2T6.qccId());
			Assert.assertEquals("K", MX2T6.zccUnit());
		} catch (final KryptonCodeException ex) {
			Assert.fail(ex.getMessage());
		} catch (final KryptonTableException ex) {
			Assert.fail(ex.getMessage());
		}

		try {
			final CobaltParameter Q = decoder.selectG1("t50", (short) 98, (short) 0, (short) 128, (short) 133);
			Assert.assertEquals("Q", Q.qccId());
			Assert.assertEquals("kg kg-1", Q.zccUnit());
		} catch (final KryptonCodeException ex) {
			Assert.fail(ex.getMessage());
		} catch (final KryptonTableException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t60()
			throws ArgonPermissionException {
		final TestImpProbe probe = new TestImpProbe();
		final String[] paths = { TestHelpCopy.newFile(this, "parameter", "G1C017S005T017.xml").getAbsolutePath(),
				TestHelpCopy.newFile(this, "parameter", "G1C017S005T019.xml").getAbsolutePath() };
		final ParameterDecoder decoder = new ParameterDecoder(probe);
		final ArgonFileManifest fm = ArgonFileManifest.newInstance(paths, null, null);
		decoder.setFileMap(fm.newFileNameMap());
		try {
			final CobaltParameter ORO = decoder.selectG1("t60", (short) 17, (short) 5, (short) 17, (short) 13);
			Assert.assertEquals("ORO", ORO.qccId());
		} catch (final KryptonCodeException ex) {
			Assert.fail(ex.getMessage());
		} catch (final KryptonTableException ex) {
			Assert.fail(ex.getMessage());
		}
		try {
			decoder.selectG1("t60", (short) 17, (short) 5, (short) 19, (short) 13);
			Assert.fail("Failed to detect malformed table 19");
		} catch (final KryptonCodeException ex) {
			Assert.fail(ex.getMessage());
		} catch (final KryptonTableException ex) {
			System.out.println("Good exception: " + ex.getMessage());
		}
	}
}
