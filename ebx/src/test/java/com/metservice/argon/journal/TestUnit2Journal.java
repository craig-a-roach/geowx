/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.journal;

import java.io.File;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;

import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.ArgonServiceId;
import com.metservice.argon.Binary;
import com.metservice.argon.Ds;
import com.metservice.argon.IArgonFileProbe;
import com.metservice.argon.file.ArgonDirectoryManagement;
import com.metservice.argon.management.IArgonSpaceId;

/**
 * @author roach
 */
public class TestUnit2Journal {

	private static ArgonJournalOut newEntry(int k, String qccType) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < k; i++) {
			sb.append("ABCDEFGHI\n");
		}
		final Binary source = Binary.newFromStringUTF8(sb.toString());
		return new ArgonJournalOut(qccType, source);
	}

	@Test
	public void t50()
			throws InterruptedException {
		final FileProbe probe = new FileProbe();
		final ArgonServiceId Sid = new ArgonServiceId("unittest.metservice", "argon");
		final SpaceId T50 = new SpaceId("t50");
		try {
			final ArgonJournalController.Config cfg = ArgonJournalController.newConfig(probe, Sid, T50, null, null);
			cfg.nominalArchiveInflatedSize(100);
			cfg.compressionHoldoff(TimeUnit.SECONDS, 3);
			cfg.compressionPeriod(TimeUnit.SECONDS, 5);
			cfg.recoveryWindow(TimeUnit.SECONDS, 100);
			cfg.blockEstimatorSize(10);
			ArgonDirectoryManagement.removeExceptSelf(cfg.cndir);
			{
				final ArgonJournalController jc = ArgonJournalController.newInstance(cfg);
				jc.commit(jc.newTransaction(newEntry(1, "signal"))); // 1
				jc.commit(jc.newTransaction(newEntry(2, "signal"))); // 2
				jc.commit(jc.newTransaction(newEntry(4, "signal"))); // 3
				jc.commit(jc.newTransaction(newEntry(5, "signal"))); // 4
				Thread.sleep(9000);
				jc.commit(jc.newTransaction(newEntry(1, "detect"))); // 5
				jc.commit(jc.newTransaction(newEntry(3, "detect"))); // 6
				jc.commit(jc.newTransaction(newEntry(7, "detect"))); // 7
				Thread.sleep(9000);
				jc.commit(jc.newTransaction(newEntry(5, "cube"))); // 8
				jc.cancel();
			}
			Thread.sleep(3000);

			final ArgonJournalController.Config cfgr = ArgonJournalController.newConfig(probe, Sid, T50, null, null);
			final ArgonJournalController jcr = ArgonJournalController.newInstance(cfgr);
			{
				final Iterator<ArgonJournalIn> ri = jcr.recoveryIterator();
				final StringBuilder act = new StringBuilder();
				while (ri.hasNext()) {
					final ArgonJournalIn je = ri.next();
					if (act.length() > 0) {
						act.append(", ");
					}
					act.append(je.serial() + ":" + je.qccType());
				}
				final String zact = act.toString();
				Assert.assertFalse("No failures", probe.fail.get());
				Assert.assertFalse("No warnings", probe.warn.get());
				Assert.assertEquals("1:signal, 2:signal, 3:signal, 4:signal, 5:detect, 6:detect, 7:detect, 8:cube", zact);
				jcr.commit(jcr.newTransaction(newEntry(1, "cube"))); // 9
				jcr.cancel();
			}
			Thread.sleep(3000);

			probe.show.set(false); // Force quota exceptions
			final ArgonJournalController.Config cfge = ArgonJournalController.newConfig(probe, Sid, T50, null, null);
			cfge.maxDeflatedSize(20);
			cfge.maxInflatedSize(30);
			final ArgonJournalController jce = ArgonJournalController.newInstance(cfge);
			final Iterator<ArgonJournalIn> re = jce.recoveryIterator();
			final StringBuilder acte = new StringBuilder();
			while (re.hasNext()) {
				final ArgonJournalIn je = re.next();
				if (acte.length() > 0) {
					acte.append(", ");
				}
				acte.append(je.serial() + ":" + je.qccType());
			}
			final String zacte = acte.toString();
			Assert.assertTrue("Some failures", probe.fail.get());
			Assert.assertEquals("9:cube", zacte);
			jce.cancel();
			Thread.sleep(3000);

			final ArgonJournalController.Config cfgp = ArgonJournalController.newConfig(probe, Sid, T50, null, null);
			cfgp.compressionHoldoff(TimeUnit.SECONDS, 3);
			cfgp.compressionPeriod(TimeUnit.SECONDS, 5);
			cfgp.recoveryWindow(TimeUnit.SECONDS, 50);
			ArgonJournalController.newInstance(cfgp);
			Thread.sleep(50000);
			final String[] ozptRetain = cfgp.cndir.list();
			final int retainCount = ozptRetain == null ? 0 : ozptRetain.length;
			Assert.assertTrue("Oldest zip(s) purged", retainCount < 4);
		} catch (final ArgonPermissionException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	private static class FileProbe implements IArgonFileProbe {

		@Override
		public void failFile(Ds diagnostic, File ofile) {
			if (show.get()) {
				System.out.println(diagnostic);
				if (ofile != null) {
					System.out.println(ofile);
				}
			}
			fail.set(true);
		}

		@Override
		public void warnFile(Ds diagnostic, File ofile) {
			if (show.get()) {
				System.out.println(diagnostic);
				if (ofile != null) {
					System.out.println(ofile);
				}
			}
			warn.set(true);
		}

		public FileProbe() {
		}

		final AtomicBoolean show = new AtomicBoolean(true);
		final AtomicBoolean fail = new AtomicBoolean(false);
		final AtomicBoolean warn = new AtomicBoolean(false);
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
