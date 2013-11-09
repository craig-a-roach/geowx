/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author roach
 */
public class TestUnit1Zip {

	private static String gen(int ikey, int cc) {
		final StringBuilder sb = new StringBuilder(1024);
		for (int k = 0; k < ikey; k++) {
			for (int i = 0; i < cc; i++) {
				sb.append("ABCDEFG");
			}
		}
		return sb.toString();
	}

	@Test
	public void t30_zip()
			throws ArgonPermissionException, ArgonStreamWriteException, ArgonQuotaException, ArgonStreamReadException {
		m_cndirHome = TestHelpC.cndirScratch("zip30");
		final File[] destFiles = new File[10];
		for (int i = 0; i < destFiles.length; i++) {
			final int id = i + 100;
			final File destFile = new File(m_cndirHome, "s" + id + ".txt");
			final Binary b = Binary.newFromStringUTF8(gen(i, 5000));
			b.save(destFile, false);
			destFiles[i] = destFile;
		}

		final File zf = new File(m_cndirHome, "t30.zip");
		final File dirUnzip = new File(m_cndirHome, "t30");
		try {
			ArgonZip.encodeToFile(destFiles, zf, null, null, true, File.separatorChar);
			final String[] xptFilter = { "t30" };
			ArgonZip.decodeToDirectory(zf, dirUnzip, xptFilter);
		} catch (final ArgonApiException ex) {
			Assert.fail(ex.getMessage());
		} catch (final ArgonPermissionException ex) {
			Assert.fail(ex.getMessage());
		} catch (final ArgonStreamReadException ex) {
			Assert.fail(ex.getMessage());
		} catch (final ArgonStreamWriteException ex) {
			Assert.fail(ex.getMessage());
		}
		final File[] zptUnzip = dirUnzip.listFiles();
		Arrays.sort(zptUnzip);
		Assert.assertEquals(destFiles.length, zptUnzip.length);
		for (int i = 0; i < zptUnzip.length; i++) {
			final File s = destFiles[i];
			final File t = zptUnzip[i];
			Assert.assertEquals(s.getName(), t.getName());
			Assert.assertEquals(s.length(), t.length());
		}
		final Binary bzf = Binary.createFromFile(zf, CArgon.M * 10);
		final List<ArgonZipItem> zlZipDecodedAscName = bzf.newZipDecodedAscName(CArgon.M * 40);
		final int zlc = zlZipDecodedAscName.size();
		Assert.assertEquals(destFiles.length, zlc);
		for (int i = 0; i < zlc; i++) {
			final File s = destFiles[i];
			final ArgonZipItem item = zlZipDecodedAscName.get(i);
			Assert.assertEquals(s.getName(), item.qccFileName);
			Assert.assertEquals(s.length(), item.content.byteCount());
		}
	}

	private File m_cndirHome;
}
