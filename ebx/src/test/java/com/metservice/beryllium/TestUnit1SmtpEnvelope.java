/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author roach
 */
public class TestUnit1SmtpEnvelope {

	@Test
	public void t50_dups() {
		final String subject = "Red Green";
		final String fromA = "sys@mail.met.co.nz";
		final String fromB = "SYS@mail.met.co.nz";
		final String toA = "BETA_1@Dev-2.met.co.nz;alpha@ops.met.co.nz;beta_1@DEV-2.met.co.nz;";
		final String toB = "alpha@ops.met.co.nz;;;beta_1@dev-2.met.co.nz;";
		final String cc = "Gamma.3@pom.metra.com";
		final String toX = "alpha@ops.met.co.nz";
		try {
			final BerylliumSmtpEnvelope eA = BerylliumSmtpEnvelope.newInstance(subject, fromA, toA, cc);
			final BerylliumSmtpEnvelope eB = BerylliumSmtpEnvelope.newInstance(subject, fromB, toB, cc);
			final BerylliumSmtpEnvelope eX = BerylliumSmtpEnvelope.newInstance(subject, fromA, toX, cc);
			Assert.assertTrue("eA=eB", eA.equals(eB));
			Assert.assertTrue("eA<>eX", !eA.equals(eX));
			Assert.assertTrue("eA = eA", eA.compareTo(eA) == 0);
			Assert.assertTrue("eX < eA", eX.compareTo(eA) < 0);
			Assert.assertTrue("eB == eA", eB.compareTo(eA) == 0);
			Assert.assertTrue("eX < eB", eX.compareTo(eB) < 0);
		} catch (final BerylliumApiException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t60_reject() {
		try {
			BerylliumSmtpEnvelope.newInstance("bad TO", "sys@mail.met.co.nz", "jim@com;ops@met.co.nz;bill:rad@met.co.nz",
					null);
			Assert.fail("Malformed TO-address");
		} catch (final BerylliumApiException ex) {
			final String msg = ex.getMessage();
			Assert.assertTrue("GOOD: " + msg, msg.length() > 0);
		}
	}

}
