/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;

import org.junit.Assert;
import org.junit.Test;

import com.metservice.argon.ArgonClock;
import com.metservice.argon.ArgonJoiner;
import com.metservice.argon.DateFormatter;

/**
 * @author roach
 */
public class TestUnit1SmtpManager {

	private static void drainQueue(List<String> zl, ConcurrentLinkedQueue<Message> mq) {
		boolean more = true;
		while (more) {
			final Message oM = mq.poll();
			if (oM == null) {
				more = false;
			} else {
				dumpMessage(zl, oM);
			}
		}
	}

	private static String dumpAddressArray(Address[] ozpt) {
		if (ozpt == null || ozpt.length == 0) return "";
		final String[] zpt = new String[ozpt.length];
		for (int i = 0; i < ozpt.length; i++) {
			zpt[i] = ozpt[i].toString();
		}
		Arrays.sort(zpt);
		return ArgonJoiner.zJoin(zpt, ";");
	}

	private static void dumpMessage(List<String> zl, Message m) {
		try {
			zl.add("TO:" + dumpAddressArray(m.getRecipients(RecipientType.TO)));
			final String CC = dumpAddressArray(m.getRecipients(RecipientType.CC));
			if (CC.length() > 0) {
				zl.add("CC:" + CC);
			}
			zl.add("FROM:" + dumpAddressArray(m.getFrom()));
			zl.add("SUBJECT:" + m.getSubject());
			zl.add("======");
			System.out.println(DateFormatter.newT8FromTs(ArgonClock.tsNow()) + ":" + m.getSubject());
			System.out.println(m.getContent());
		} catch (final MessagingException ex) {
			zl.add("FAIL:" + ex.getMessage());
		} catch (final IOException ex) {
			zl.add("FAIL:" + ex.getMessage());
		}
	}

	private static ConcurrentLinkedQueue<Message> getMessageQueue(BerylliumSmtpManager m, String qccId) {
		final BerylliumSmtpConnection oCx = m.findConnection(qccId);
		return oCx == null ? null : oCx.getMessageQueue();
	}

	private static ConcurrentLinkedQueue<Message> messageQueue(BerylliumSmtpManager m, String qccId) {
		final ConcurrentLinkedQueue<Message> oQueue = getMessageQueue(m, qccId);
		if (oQueue == null) return new ConcurrentLinkedQueue<Message>();
		return oQueue;
	}

	@Test
	public void t40_pool()
			throws BerylliumApiException, InterruptedException {
		final BerylliumSmtpUrl url1 = BerylliumSmtpUrl.newSave("alpha.media.com", "unit1");
		final BerylliumSmtpUrl url2 = BerylliumSmtpUrl.newSave("alpha.media.com", "unit2");
		final BerylliumSmtpUrl url3 = BerylliumSmtpUrl.newSave("beta.media.com", "unit1");
		final String password1 = "wordpass1";
		final String password2 = "wordpass2";
		final String passwordbad = "*";
		final TestImpSmtpProbe probe = new TestImpSmtpProbe(true, true);
		final BerylliumSmtpManager.Config cfg = BerylliumSmtpManager.newConfig(probe, "ut40");
		final BerylliumSmtpManager m = BerylliumSmtpManager.newInstance(cfg);
		m.register("A", url1, password1, false);
		m.register("B", url3, password2, false);
		final BerylliumSmtpConnection oA1 = m.ensureConnection("A");
		Assert.assertNotNull("Connection A(1)", oA1);
		Assert.assertEquals("smtp://unit1@alpha.media.com:1 CONNECTED", oA1.toString());
		final BerylliumSmtpConnection oB1 = m.ensureConnection("B");
		Assert.assertNotNull("Connection B(1)", oB1);
		Assert.assertEquals("smtp://unit1@beta.media.com:1 CONNECTED", oB1.toString());

		m.register("A", url2, password1, false);
		final BerylliumSmtpConnection oA2 = m.ensureConnection("A");
		Assert.assertNotNull("Connection A(2)", oA2);
		Assert.assertEquals("smtp://unit2@alpha.media.com:1 CONNECTED", oA2.toString());

		m.register("A", url2, password1, false);
		final BerylliumSmtpConnection oA3 = m.ensureConnection("A");
		Assert.assertNotNull("Connection A(3)", oA3);
		Assert.assertEquals("smtp://unit2@alpha.media.com:1 CONNECTED", oA3.toString());

		m.register("A", url2, passwordbad, false);
		final BerylliumSmtpConnection oA4 = m.ensureConnection("A");
		Assert.assertNull("No Connection A(4)", oA4);

		m.register("A", url2, password1, false);
		final BerylliumSmtpConnection oA5 = m.ensureConnection("A");
		Assert.assertNotNull("Connection A(5)", oA5);
		Assert.assertEquals("smtp://unit2@alpha.media.com:1 CONNECTED", oA5.toString());
		m.shutdown();
	}

	@Test
	public void t50_throttle()
			throws BerylliumApiException, InterruptedException {
		final BerylliumSmtpUrl url = BerylliumSmtpUrl.newSave("alpha.media.com", "unit");
		final String password = "wordpass";
		final String from = "beryllium@metservice.com";
		final String toX = "xray@red.com;xerces@blue.com";
		final TestImpSmtpProbe probe = new TestImpSmtpProbe(true, false);
		final BerylliumSmtpManager.Config cfg = BerylliumSmtpManager.newConfig(probe, "ut50");
		cfg.msTimerDelay = 1000;
		cfg.msTimerTick = 5000;
		final BerylliumSmtpManager m = BerylliumSmtpManager.newInstance(cfg);
		m.register("A", url, password, false);
		final BerylliumSmtpEnvelope eApple = BerylliumSmtpEnvelope.newInstance("Apple", from, toX, null);
		final BerylliumSmtpEnvelope ePeach = BerylliumSmtpEnvelope.newInstance("Peach", from, toX, null);
		final IBerylliumSmtpHtml html1 = new Html("<p>Line1</p><p>Line2</p>");
		final IBerylliumSmtpHtml html2 = new Html("<ul><li>Item1</li><li>Item2</li></ul>");
		final BerylliumSmtpRatePolicy policy = BerylliumSmtpRatePolicy.newMinMax(8000, 15000);
		final List<String> actual = new ArrayList<String>();
		m.send("A", eApple, html1, policy);
		actual.add("[[t00]]");
		drainQueue(actual, messageQueue(m, "A"));
		Thread.sleep(2000);
		m.send("A", eApple, html2, policy);
		actual.add("[[t02]]");
		drainQueue(actual, messageQueue(m, "A"));
		Thread.sleep(1000);
		m.send("A", eApple, html1, policy);
		m.send("A", ePeach, html2, policy);
		actual.add("[[t03]]");
		drainQueue(actual, messageQueue(m, "A"));
		Thread.sleep(7000);
		m.send("A", eApple, html2, policy);
		actual.add("[[t10]]");
		drainQueue(actual, messageQueue(m, "A"));
		Thread.sleep(1000);
		m.send("A", eApple, html1, policy);
		actual.add("[[t11]]");
		drainQueue(actual, messageQueue(m, "A"));
		Thread.sleep(1000);
		final ConcurrentLinkedQueue<Message> mqPreShutdown = messageQueue(m, "A");
		m.shutdown();
		actual.add("[[t12]]");
		drainQueue(actual, mqPreShutdown);
		final List<String> expected = new ArrayList<String>();
		expected.add("[[t00]]");
		expected.add("TO:xerces@blue.com;xray@red.com");
		expected.add("FROM:beryllium@metservice.com");
		expected.add("SUBJECT:Apple");
		expected.add("======");
		expected.add("[[t02]]");
		expected.add("[[t03]]");
		expected.add("TO:xerces@blue.com;xray@red.com");
		expected.add("FROM:beryllium@metservice.com");
		expected.add("SUBJECT:Peach");
		expected.add("======");
		expected.add("[[t10]]");
		expected.add("TO:xerces@blue.com;xray@red.com");
		expected.add("FROM:beryllium@metservice.com");
		expected.add("SUBJECT:Apple");
		expected.add("======");
		expected.add("[[t11]]");
		expected.add("[[t12]]");
		expected.add("TO:xerces@blue.com;xray@red.com");
		expected.add("FROM:beryllium@metservice.com");
		expected.add("SUBJECT:Apple");
		expected.add("======");
		Assert.assertArrayEquals(ArgonJoiner.zJoin(actual, "\n"), expected.toArray(), actual.toArray());
	}

	@Test
	public void t60_abate()
			throws BerylliumApiException, InterruptedException {
		final BerylliumSmtpUrl url = BerylliumSmtpUrl.newSave("alpha.media.com", "unit");
		final String password = "wordpass";
		final String from = "beryllium@metservice.com";
		final String toX = "xray@red.com;xerces@blue.com";
		final TestImpSmtpProbe probe = new TestImpSmtpProbe(true, true);
		final BerylliumSmtpManager.Config cfg = BerylliumSmtpManager.newConfig(probe, "ut60");
		cfg.msTimerDelay = 1000;
		cfg.msTimerTick = 2000;
		final BerylliumSmtpManager m = BerylliumSmtpManager.newInstance(cfg);
		m.register("A", url, password, false);
		final BerylliumSmtpEnvelope eApple = BerylliumSmtpEnvelope.newInstance("Apple", from, toX, null);
		final IBerylliumSmtpHtml html1 = new Html("<p>State 1</p>");
		final IBerylliumSmtpHtml html2 = new Html("<p>State 2</p>");
		final BerylliumSmtpRatePolicy policy = BerylliumSmtpRatePolicy.newMinMax(8000, 13000);
		final List<String> actual = new ArrayList<String>();
		m.send("A", eApple, html1, policy);
		actual.add("[[t00]]");
		drainQueue(actual, messageQueue(m, "A"));

		Thread.sleep(2000);
		m.send("A", eApple, html2, policy);
		actual.add("[[t02]]");
		drainQueue(actual, messageQueue(m, "A"));

		Thread.sleep(8000);
		actual.add("[[t10]]");
		drainQueue(actual, messageQueue(m, "A"));

		Thread.sleep(16000);
		actual.add("[[t26]]");
		drainQueue(actual, messageQueue(m, "A"));

		Thread.sleep(4000);
		actual.add("[[t30]]");
		m.send("A", eApple, html2, policy);
		drainQueue(actual, messageQueue(m, "A"));
		m.shutdown();

		final List<String> expected = new ArrayList<String>();
		expected.add("[[t00]]");
		expected.add("TO:xerces@blue.com;xray@red.com");
		expected.add("FROM:beryllium@metservice.com");
		expected.add("SUBJECT:Apple");
		expected.add("======");
		expected.add("[[t02]]");
		expected.add("[[t10]]");
		expected.add("TO:xerces@blue.com;xray@red.com");
		expected.add("FROM:beryllium@metservice.com");
		expected.add("SUBJECT:Apple");
		expected.add("======");
		expected.add("[[t26]]");
		expected.add("TO:xerces@blue.com;xray@red.com");
		expected.add("FROM:beryllium@metservice.com");
		expected.add("SUBJECT:Apple - Abated");
		expected.add("======");
		expected.add("[[t30]]");
		expected.add("TO:xerces@blue.com;xray@red.com");
		expected.add("FROM:beryllium@metservice.com");
		expected.add("SUBJECT:Apple");
		expected.add("======");
		Assert.assertArrayEquals(ArgonJoiner.zJoin(actual, "\n"), expected.toArray(), actual.toArray());
	}

	@Test
	public void t70_unthrottled()
			throws BerylliumApiException, InterruptedException {
		final BerylliumSmtpUrl url = BerylliumSmtpUrl.newSave("alpha.media.com", "unit");
		final String password = "wordpass";
		final String from = "beryllium@metservice.com";
		final String toX = "xray@red.com;xerces@blue.com";
		final TestImpSmtpProbe probe = new TestImpSmtpProbe(true, false);
		final BerylliumSmtpManager.Config cfg = BerylliumSmtpManager.newConfig(probe, "ut70");
		cfg.msTimerDelay = 1000;
		cfg.msTimerTick = 5000;
		final BerylliumSmtpManager m = BerylliumSmtpManager.newInstance(cfg);
		m.register("A", url, password, false);
		final BerylliumSmtpEnvelope eApple = BerylliumSmtpEnvelope.newInstance("Apple", from, toX, null);
		final BerylliumSmtpEnvelope ePeach = BerylliumSmtpEnvelope.newInstance("Peach", from, toX, null);
		final IBerylliumSmtpHtml html1 = new Html("<p>Line1</p><p>Line2</p>");
		final IBerylliumSmtpHtml html2 = new Html("<ul><li>Item1</li><li>Item2</li></ul>");
		final BerylliumSmtpRatePolicy policy = BerylliumSmtpRatePolicy.InstanceUnthrottled;
		final List<String> actual = new ArrayList<String>();
		m.send("A", eApple, html1, policy);
		actual.add("[[t00]]");
		drainQueue(actual, messageQueue(m, "A"));
		Thread.sleep(2000);
		m.send("A", eApple, html2, policy);
		actual.add("[[t02]]");
		drainQueue(actual, messageQueue(m, "A"));
		Thread.sleep(1000);
		m.send("A", eApple, html1, policy);
		m.send("A", ePeach, html2, policy);
		actual.add("[[t03]]");
		drainQueue(actual, messageQueue(m, "A"));
		final List<String> expected = new ArrayList<String>();
		expected.add("[[t00]]");
		expected.add("TO:xerces@blue.com;xray@red.com");
		expected.add("FROM:beryllium@metservice.com");
		expected.add("SUBJECT:Apple");
		expected.add("======");
		expected.add("[[t02]]");
		expected.add("TO:xerces@blue.com;xray@red.com");
		expected.add("FROM:beryllium@metservice.com");
		expected.add("SUBJECT:Apple");
		expected.add("======");
		expected.add("[[t03]]");
		expected.add("TO:xerces@blue.com;xray@red.com");
		expected.add("FROM:beryllium@metservice.com");
		expected.add("SUBJECT:Apple");
		expected.add("======");
		expected.add("TO:xerces@blue.com;xray@red.com");
		expected.add("FROM:beryllium@metservice.com");
		expected.add("SUBJECT:Peach");
		expected.add("======");
		Assert.assertArrayEquals(ArgonJoiner.zJoin(actual, "\n"), expected.toArray(), actual.toArray());
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

}
