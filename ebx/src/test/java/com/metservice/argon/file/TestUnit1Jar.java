/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.file;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;

import com.metservice.argon.ArgonDiskException;
import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.ArgonPlatformException;
import com.metservice.argon.ArgonQuotaException;
import com.metservice.argon.ArgonServiceId;
import com.metservice.argon.Ds;
import com.metservice.argon.IArgonFileRunProbe;
import com.metservice.argon.IArgonSpaceId;
import com.metservice.argon.TestHelpC;

/**
 * @author roach
 */
public class TestUnit1Jar {

	@Test
	public void t50() {
		final ArgonServiceId SID = TestHelpC.SID;
		final SpaceId SPACE = new SpaceId("t50");
		final Probe probe = new Probe();
		try {
			final ArgonClasspathExtractor.Config cfg = ArgonClasspathExtractor.newConfig(probe, SID, SPACE);
			cfg.enableSafeNaming(false);
			cfg.enableClean(true);
			final ArgonClasspathExtractor dcc = ArgonClasspathExtractor.newInstance(cfg);
			try {
				final File oFile = dcc.find(TestResourceRef.class, "alpha.txt");
				Assert.assertNotNull("Found alpha", oFile);
				Assert.assertTrue("alpha exists", oFile.exists());
				Assert.assertEquals("alpha length", 3, oFile.length());
			} catch (final ArgonQuotaException ex) {
				Assert.fail(ex.getMessage());
			} catch (final ArgonDiskException ex) {
				Assert.fail(ex.getMessage());
			}
		} catch (final ArgonPermissionException ex) {
			Assert.fail(ex.getMessage());
		} catch (final ArgonPlatformException ex) {
			Assert.fail(ex.getMessage());
		} catch (final InterruptedException ex) {
			Assert.fail("Latch interrupted");
		}
	}

	private static class Probe implements IArgonFileRunProbe {

		@Override
		public void failFile(Ds diagnostic, File ofile) {
			if (show.get()) {
				System.out.println(diagnostic);
				if (ofile != null) {
					System.out.println(ofile);
				}
			}
			failFile.set(true);
		}

		@Override
		public void failSoftware(Ds diagnostic) {
			if (show.get()) {
				System.out.println(diagnostic);
			}
			failSoftware.set(true);
		}

		@Override
		public void failSoftware(RuntimeException exRT) {
			if (show.get()) {
				System.out.println(Ds.format(exRT, true));
			}
			failSoftware.set(true);
		}

		@Override
		public void warnFile(Ds diagnostic, File ofile) {
			if (show.get()) {
				System.out.println(diagnostic);
				if (ofile != null) {
					System.out.println(ofile);
				}
			}
			warnFile.set(true);
		}

		public Probe() {
		}
		final AtomicBoolean show = new AtomicBoolean(true);
		final AtomicBoolean failFile = new AtomicBoolean(false);
		final AtomicBoolean warnFile = new AtomicBoolean(false);
		final AtomicBoolean failSoftware = new AtomicBoolean(false);
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
