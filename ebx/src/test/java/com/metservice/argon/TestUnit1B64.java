/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author roach
 */
public class TestUnit1B64 {

	@Test
	public void a70() {
		final String src1 = "h:/";
		final String src2 = "h:/w";
		final String enc1 = Binary.newFromStringUTF8(src1).newB64URL();
		final String enc2 = Binary.newFromStringUTF8(src2).newB64URL();
		try {
			final String dst1 = Binary.newFromB64URL(enc1).newStringUTF8();
			final String dst2 = Binary.newFromB64URL(enc2).newStringUTF8();
			Assert.assertEquals(src1, dst1);
			Assert.assertEquals(src2, dst2);
		} catch (final ArgonFormatException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t30() {
		final byte[] datum = new byte[1000];
		for (int i = 0; i < datum.length; i++) {
			datum[i] = (byte) (i % 256);
		}
		final Binary src = Binary.newFromTransient(datum);
		final String b64 = src.newB64MIME();
		try {
			final Binary out = Binary.newFromB64MIME(b64);
			final byte[] zptout = out.zptReadOnly;
			for (int i = 0; i < datum.length && i < zptout.length; i++) {
				Assert.assertTrue("match at " + i, datum[i] == zptout[i]);
			}
			Assert.assertEquals(datum.length, zptout.length);
		} catch (final ArgonFormatException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t31() {
		final byte[] datum = new byte[1];
		for (int i = 0; i < datum.length; i++) {
			datum[i] = (byte) (i % 256);
		}
		final Binary src = Binary.newFromTransient(datum);
		final String b64 = src.newB64MIME();
		try {
			final Binary out = Binary.newFromB64MIME(b64);
			final byte[] zptout = out.zptReadOnly;
			for (int i = 0; i < datum.length && i < zptout.length; i++) {
				Assert.assertTrue("match at " + i, datum[i] == zptout[i]);
			}
			Assert.assertEquals(datum.length, zptout.length);
		} catch (final ArgonFormatException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t32() {
		final byte[] datum = { 0x13, 0x06 };
		final Binary src = Binary.newFromTransient(datum);
		final String b64 = src.newB64MIME();
		try {
			final Binary out = Binary.newFromB64MIME(b64);
			final byte[] zptout = out.zptReadOnly;
			for (int i = 0; i < datum.length && i < zptout.length; i++) {
				Assert.assertTrue("match at " + i, datum[i] == zptout[i]);
			}
			Assert.assertEquals(datum.length, zptout.length);
		} catch (final ArgonFormatException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t40() {
		final byte[] datum = new byte[0];
		final Binary src = Binary.newFromTransient(datum);
		final String b64 = src.newB64MIME();
		try {
			final Binary out = Binary.newFromB64MIME(b64);
			final byte[] zptout = out.zptReadOnly;
			for (int i = 0; i < datum.length && i < zptout.length; i++) {
				Assert.assertTrue("match at " + i, datum[i] == zptout[i]);
			}
			Assert.assertEquals(datum.length, zptout.length);
		} catch (final ArgonFormatException ex) {
			Assert.fail(ex.getMessage());
		}
	}

}
