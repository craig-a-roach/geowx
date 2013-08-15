/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author roach
 */
public class TestUnit3SmtpTransport {

	private static final String HOST = System.getProperty("smtpHost", "hurricrane.met.co.nz"); // roach
	private static final String USER = System.getProperty("smtpUser", "myUser"); // roach
	private static final String PASSWORD = System.getProperty("smtpPassword", "myPassword"); // AUTOcoot

	private static final BerylliumSmtpUrl url = BerylliumSmtpUrl.newInstance(HOST, USER);

	@Test
	public void t20() {
		final TestImpSmtpProbe probe = new TestImpSmtpProbe(true, true);
		BerylliumSmtpConnection ocx = null;
		try {
			ocx = BerylliumSmtpConnectionFactory.newConnection(url, PASSWORD, false);
			final List<String[]> head = new ArrayList<String[]>();
			final String[] h1 = { "Head 1:", "Data 1" };
			final String[] h2 = { "Head 2:", "Data 2" };
			head.add(h1);
			head.add(h2);
			final List<String[]> foot = new ArrayList<String[]>();
			final String[] f1 = { "Foot 1" };
			final String[] f2 = { "Foot 2" };
			foot.add(f1);
			foot.add(f2);
			final BerylliumSmtpBorder border = BerylliumSmtpBorder.newInstance("demo", head, foot);
			final BerylliumSmtpEnvelope e = BerylliumSmtpEnvelope.newInstance("AF Test", "afdirector@metservice.com",
					"craig.roach@metservice.com;craig.roach@met.co.nz", "roach@metservice.com;roach@met.co.nz");
			final IBerylliumSmtpText text = new Text("alpha\nbeta");
			Assert.assertTrue("sent test", ocx.send(probe, e, text, border));
			final IBerylliumSmtpHtml html = new Html("<h1>Al&lt;pha&#x2717;</h1><p>beta</p>");
			ocx.send(e, html, border);
		} catch (final BerylliumSmtpPlatformException ex) {
			Assert.fail(ex.getMessage());
		} catch (final BerylliumSmtpAuthenticationException ex) {
			Assert.fail(ex.getMessage());
		} catch (final BerylliumSmtpTransportException ex) {
			Assert.fail(ex.getMessage());
		} catch (final BerylliumApiException ex) {
			Assert.fail(ex.getMessage());
		} finally {
			if (ocx != null) {
				ocx.close(probe);
			}
		}
	}

	@Test
	public void t50_throttle()
			throws BerylliumApiException, InterruptedException {
		final String from = "beryllium@metservice.com";
		final String to = "craig.roach@metservice.com";
		final TestImpSmtpProbe probe = new TestImpSmtpProbe(true, false);
		final BerylliumSmtpManager.Config cfg = BerylliumSmtpManager.newConfig(probe, "ut50");
		cfg.msTimerDelay = 1000;
		cfg.msTimerTick = 2000;
		final BerylliumSmtpManager m = BerylliumSmtpManager.newInstance(cfg);
		m.register("A", url, PASSWORD, false);
		final BerylliumSmtpEnvelope eApple = BerylliumSmtpEnvelope.newInstance("Apple", from, to, null);
		final IBerylliumSmtpHtml html1 = new Html("<p>Line1</p><p>Line2</p>");
		final IBerylliumSmtpHtml html2 = new Html("<ul><li>Item1</li><li>Item2</li></ul>");
		final BerylliumSmtpRatePolicy policy = BerylliumSmtpRatePolicy.newMinMax(8000, 15000);
		m.send("A", eApple, html1, policy);
		Thread.sleep(2000);
		m.send("A", eApple, html2, policy);
		Thread.sleep(1000);
		m.send("A", eApple, html1, policy);
		Thread.sleep(1000);
		m.send("A", eApple, html1, policy);
		Thread.sleep(28000);
	}

	private static class Html implements IBerylliumSmtpHtml {

		@Override
		public String zeHtml() {
			return m_q;
		}

		public Html(String q) {
			m_q = q;
		}
		private final String m_q;
	}

	private static class Text implements IBerylliumSmtpText {

		@Override
		public String zText() {
			return m_q;
		}

		public Text(String q) {
			m_q = q;
		}
		private final String m_q;

	}

}
