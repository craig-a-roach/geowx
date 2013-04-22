/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.cobalt;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.metservice.argon.ArgonQuotaException;
import com.metservice.argon.ArgonStreamReadException;
import com.metservice.argon.Binary;
import com.metservice.argon.CArgon;

/**
 * @author roach
 */
class TestResource {

	private static final int ResourceQuotaBc = 8 * CArgon.M;

	public String[] lines(boolean retainEmpty) {
		return lines(zValueLF, retainEmpty);
	}

	@Override
	public String toString() {
		return zValueLF;
	}

	public String toStringCRLF() {
		return zEnsureCR(zValueLF);
	}

	private static String zEnsureCR(String zIn) {
		final int len = zIn.length();
		final StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			final char ch = zIn.charAt(i);
			if (ch != '\r') {
				if (ch == '\n') {
					sb.append("\r\n");
				} else {
					sb.append(ch);
				}
			}
		}
		return sb.toString();
	}

	private static String zStripCR(String zIn) {
		final int len = zIn.length();
		final StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			final char ch = zIn.charAt(i);
			if (ch != '\r') {
				sb.append(ch);
			}
		}
		return sb.toString();
	}

	public static String[] lines(Binary bin, boolean retainEmpty) {
		if (bin == null) throw new IllegalArgumentException("object is null");
		return lines(bin.newStringUTF8(), retainEmpty);
	}

	public static String[] lines(String zin, boolean retainEmpty) {
		final List<String> zl = new ArrayList<String>();
		final int len = zin.length();
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < len; i++) {
			final char ch = zin.charAt(i);
			if (ch == '\r') {
				continue;
			}
			if (ch == '\n') {
				if (retainEmpty || sb.length() > 0) {
					zl.add(sb.toString());
				}
				sb.setLength(0);
			} else {
				sb.append(ch);
			}
		}
		if (sb.length() > 0) {
			zl.add(sb.toString());
		}
		return zl.toArray(new String[zl.size()]);
	}

	public TestResource(String fileName) {
		if (fileName == null || fileName.length() == 0) throw new IllegalArgumentException("string is null or empty");
		this.fileName = fileName;
		final InputStream oIn = getClass().getResourceAsStream(fileName);
		if (oIn == null) throw new IllegalArgumentException("resource not found>" + fileName + "<");
		try {
			final Binary resource = Binary.newFromInputStream(oIn, 0L, fileName, ResourceQuotaBc);
			final String zResource = resource.newStringUTF8();
			zValueLF = zStripCR(zResource);
		} catch (final ArgonQuotaException ex) {
			throw new IllegalArgumentException("Invalid resource" + fileName, ex);
		} catch (final ArgonStreamReadException ex) {
			throw new IllegalArgumentException("Cannot read resource" + fileName, ex);
		}
	}
	public final String fileName;
	public final String zValueLF;
}
