/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emmail;

import org.junit.Test;

import com.metservice.beryllium.BerylliumSmtpManager;
import com.metservice.carbon.TestImpSmtpProbe;
import com.metservice.carbon.TestNeon;

/**
 * @author roach
 */
public class TestUnitMailSend extends TestNeon {

	@Test
	public void t50() {
		final TestImpSmtpProbe probe = new TestImpSmtpProbe(true, true);
		final BerylliumSmtpManager.Config cfg = BerylliumSmtpManager.newConfig(probe, "ut50");
		final BerylliumSmtpManager imp = BerylliumSmtpManager.newInstance(cfg);
		final MailEmInstaller installer = new MailEmInstaller(imp);
		final Expectation x = new Expectation("rp1max", "10m", "from1", "rock@carbon.met.co.nz", "cc1", "support@met.com");
		jsassert(x, "send_html1", installer);
		imp.shutdown();
	}

	// @Test
	public void t99() {
		final TestImpSmtpProbe probe = new TestImpSmtpProbe(true, true);
		final BerylliumSmtpManager.Config cfg = BerylliumSmtpManager.newConfig(probe, "ut99");
		final BerylliumSmtpManager imp = BerylliumSmtpManager.newInstance(cfg);
		final MailEmInstaller installer = new MailEmInstaller(imp);
		final Expectation x = new Expectation();
		jsassert(x, "send_html2", installer);
		imp.shutdown();
	}

}
