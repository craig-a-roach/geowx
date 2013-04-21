/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.metservice.argon.file.ArgonCompactLoader;
import com.metservice.argon.file.ArgonDirectoryManagement;
import com.metservice.argon.file.ArgonSaver;

/**
 * @author roach
 */
public class TestUnit1Saver {

	@Before
	public void setupDir()
			throws ArgonPermissionException {
		m_cndirHome = ArgonDirectoryManagement.cndirEnsureUserWriteable("unittest.argon", "saver");
		ArgonDirectoryManagement.removeExceptSelf(m_cndirHome);
		m_probe = new Probe(true);
	}

	@Test
	public void t50()
			throws ArgonPermissionException, ArgonApiException, ArgonStreamWriteException, ArgonQuotaException,
			ArgonStreamReadException {
		final ArgonSaver cp = new ArgonSaver(10);
		final File destFile = new File(m_cndirHome, "t50.txt");
		final String s0 = ArgonCompactLoader.load(m_probe, destFile, false, 256).newStringASCII();
		Assert.assertEquals("", s0);
		cp.save(m_probe, destFile, true, iArray("ab", "cdefg", "hijklmn"));
		Assert.assertFalse(m_probe.hasFailed());
		Assert.assertEquals(14L, destFile.length());
		final String s1 = ArgonCompactLoader.load(m_probe, destFile, false, 256).newStringASCII();
		Assert.assertEquals("abcdefghijklmn", s1);
		cp.save(m_probe, destFile, false, iArray("ABCDEF", "", "G"));
		Assert.assertFalse(m_probe.hasFailed());
		Assert.assertEquals(7L, destFile.length());
		final String s2 = ArgonCompactLoader.load(m_probe, destFile, false, 256).newStringASCII();
		Assert.assertEquals("ABCDEFG", s2);
		cp.save(m_probe, destFile, false, iArray("0123456789", "01234567890"));
		Assert.assertFalse(m_probe.hasFailed());
		Assert.assertEquals(21L, destFile.length());
		final String s3 = ArgonCompactLoader.load(m_probe, destFile, false, 256).newStringASCII();
		Assert.assertEquals("012345678901234567890", s3);
		cp.save(m_probe, destFile, false, iArrayN("abcd", "defgh", "ijk"));
		Assert.assertFalse(m_probe.hasFailed());
		Assert.assertEquals(15L, destFile.length());
		final String s4 = ArgonCompactLoader.load(m_probe, destFile, false, 256).newStringASCII();
		Assert.assertEquals("abcd\ndefgh\nijk\n", s4);
		try {
			ArgonCompactLoader.load(new Probe(false), destFile, false, 10);
			Assert.fail("Expected quota exception");
		} catch (final ArgonQuotaException ex) {
			System.out.println("Good exception: " + ex.getMessage());
		}
	}

	private static Iterator<byte[]> iArray(String... lines) {
		return ArgonText.iterator(Arrays.asList(lines), ArgonText.ASCII, "");
	}

	private static Iterator<byte[]> iArrayN(String... lines) {
		return ArgonText.iterator(Arrays.asList(lines), ArgonText.ASCII, "\n");
	}

	private File m_cndirHome;
	private Probe m_probe;

	private static class Probe implements IArgonFileProbe {

		@Override
		public void failFile(Ds diagnostic, File ofile) {
			if (m_syserr) {
				System.err.println(diagnostic);
			}
			m_fail.set(true);
		}

		public boolean hasFailed() {
			return m_fail.get();
		}

		@Override
		public void warnFile(Ds diagnostic, File ofile) {
			if (m_syserr) {
				System.err.println(diagnostic);
			}
			m_fail.set(true);
		}

		public Probe(boolean syserr) {
			m_syserr = syserr;
		}
		private final boolean m_syserr;
		private final AtomicBoolean m_fail = new AtomicBoolean(false);
	}
}
