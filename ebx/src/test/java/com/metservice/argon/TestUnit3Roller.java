/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.metservice.argon.management.ArgonProbeFormatter;
import com.metservice.argon.management.ArgonRecordType;
import com.metservice.argon.management.ArgonRoller;
import com.metservice.argon.management.IArgonSpaceId;

/**
 * @author roach
 */
public class TestUnit3Roller {

	@Test
	public void t40_probeFormatter()
			throws ArgonFormatException, ArgonApiException {
		final long tbase = DateFactory.newInstance("2010010412", TimeZoneFactory.GMT).getTime();
		ArgonClock.simulatedNow(tbase);
		final ArgonServiceId sid = TestHelpC.SID;
		final SpaceId alpha = new SpaceId("roller40");
		final ArgonProbeFormatter f = ArgonProbeFormatter.newInstance(sid, alpha);
		final String s1 = f.logger(tbase + 1560, "info", "Net", "Single Line");
		final String s2 = f.logger(tbase + 2032, "info", "Net", "Multi Line 1\nMulti Line 2");
		System.out.println("Goal 1...");
		System.out.println("roller40 info Net +1.560@20100104T1200Z01M560 (05-Jan 0100+13:00)| Single Line");
		System.out.println("Actual 1...");
		System.out.println(s1);
		System.out.println("Goal 2...");
		System.out.println("roller40 info Net +2.032@20100104T1200Z02M032 (05-Jan 0100+13:00)...\n. Multi Line 1\n. Multi Line 2\n\n");
		System.out.println("Actual 2...");
		System.out.println(s2);
		Assert.assertTrue("s1 info Net", s1.contains("info") && s1.contains("Net"));
		Assert.assertTrue("s2 info Net", s2.contains("info") && s2.contains("Net"));
	}

	@Test
	public void t50_default()
			throws ArgonPermissionException {
		final ArgonServiceId sid = TestHelpC.SID;
		final ArgonRecordType recType = new ArgonRecordType("line");
		final SpaceId alpha = new SpaceId("roller50-alpha");
		final SpaceId beta = new SpaceId("roller50-beta");
		for (int i = 0; i < 10; i++) {
			ArgonRoller.printStream(sid, recType, alpha).println("Line " + i);
			ArgonRoller.printStream(sid, recType, beta).println("Item " + i);
		}
		ArgonRoller.shutdown("Test Complete");
	}

	@Test
	public void t60_lifecycle()
			throws ArgonPermissionException, InterruptedException {
		final ArgonServiceId sid = TestHelpC.SID;
		final ArgonRecordType recType = new ArgonRecordType("line");
		final SpaceId gamma = new SpaceId("roller60-gamma");
		final SpaceId epsilon = new SpaceId("roller60-epsilon");
		final ArgonRoller ar = new ArgonRoller("t60-roller", TimeUnit.MINUTES, 3);
		for (int i = 0; i < 300; i++) {
			ar.printStreamInstance(sid, recType, gamma).println("Line " + i);
			ar.printStreamInstance(sid, recType, epsilon).println("Item " + i);
			Thread.sleep(1000L);
		}
	}

	private static class SpaceId implements IArgonSpaceId {

		@Override
		public String format() {
			return m_qId;
		}

		public SpaceId(String qId) {
			m_qId = qId;
		}

		private final String m_qId;
	}

}
