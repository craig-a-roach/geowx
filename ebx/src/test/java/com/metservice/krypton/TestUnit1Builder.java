/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.metservice.argon.DateFactory;

/**
 * @author roach
 */
public class TestUnit1Builder {

	private int decode(byte[] dest, int octet, String typ) {
		final int pos = octet - 1;
		if (typ.equals("u1")) return UGrib.intu1(dest, pos);
		if (typ.equals("u2")) return UGrib.intu2(dest, pos);
		if (typ.equals("i2")) return UGrib.int2(dest, pos);
		if (typ.equals("i4")) return UGrib.int4(dest, pos);
		throw new IllegalArgumentException("invalid typ>" + typ + "<");
	}

	private float decodeFloat(byte[] dest, int octet, String typ) {
		final int pos = octet - 1;
		if (typ.equals("ieee4")) return UGrib.float4IEEE(dest, pos);
		throw new IllegalArgumentException("invalid typ>" + typ + "<");
	}

	@Test
	public void testBMS1() {
		final float[] data = { 101.4f, 102.7f, Float.NaN, 101.9f, 101.0f, Float.NaN, 101.6f, 101.2f, Float.NaN, Float.NaN,
				101.1f, 101.8f, 102.9f, Float.NaN };

		final KryptonData2Packer00 p = KryptonData2Packer00.newInstance(data, 1.0f, 1, 10, true);
		final KryptonBitmap2Builder b = new KryptonBitmap2Builder(p);
		final Section2Buffer out = new Section2Buffer(b.sectionNo(), b.estimatedOctetCount());
		b.save(out);
		final byte[] dest = new byte[100];
		final int bc = out.emit(dest, 0);
		Assert.assertEquals(8, bc);
		Assert.assertEquals(8, decode(dest, 1, "i4"));
		Assert.assertEquals(6, decode(dest, 5, "u1"));
		Assert.assertEquals(0, decode(dest, 6, "u1"));
	}

	@Test
	public void testBMS2() {
		final KryptonBitmap2Builder b = new KryptonBitmap2Builder(null);
		final Section2Buffer out = new Section2Buffer(b.sectionNo(), b.estimatedOctetCount());
		b.save(out);
		final byte[] dest = new byte[100];
		final int bc = out.emit(dest, 0);
		Assert.assertEquals(6, bc);
		Assert.assertEquals(6, decode(dest, 1, "i4"));
		Assert.assertEquals(6, decode(dest, 5, "u1"));
		Assert.assertEquals(255, decode(dest, 6, "u1"));
	}

	@Test
	public void testBMS3() {
		final float[] data = { 101.4f, 102.7f, 101.9f, 101.0f, 101.6f, 101.2f, 101.1f, 101.8f, 102.9f };
		final KryptonData2Packer00 p = KryptonData2Packer00.newInstance(data, 1.0f, 1, 10, true);
		final KryptonBitmap2Builder b = new KryptonBitmap2Builder(p);
		final Section2Buffer out = new Section2Buffer(b.sectionNo(), b.estimatedOctetCount());
		b.save(out);
		final byte[] dest = new byte[100];
		final int bc = out.emit(dest, 0);
		Assert.assertEquals(6, bc);
		Assert.assertEquals(6, decode(dest, 1, "i4"));
		Assert.assertEquals(6, decode(dest, 5, "u1"));
		Assert.assertEquals(255, decode(dest, 6, "u1"));
	}

	@Test
	public void testDRS() {
		final float[] data = { 101.4f, 102.7f, Float.NaN, 101.9f, 101.0f, Float.NaN, 101.6f, 101.2f, Float.NaN, Float.NaN,
				101.1f, 101.8f, 102.9f, Float.NaN };

		final KryptonData2Packer00 p = KryptonData2Packer00.newInstance(data, 1.0f, 1, 10, true);
		final KryptonDataRepresentation2Builder b = new KryptonDataRepresentation2Builder(p);
		final Section2Buffer out = new Section2Buffer(b.sectionNo(), 21);
		b.save(out);
		final byte[] dest = new byte[100];
		final int bc = out.emit(dest, 0);
		Assert.assertEquals(21, bc);
		Assert.assertEquals(21, decode(dest, 1, "i4"));
		Assert.assertEquals(5, decode(dest, 5, "u1"));
		Assert.assertEquals(9, decode(dest, 6, "i4"));
		Assert.assertEquals(0, decode(dest, 10, "u2"));
		Assert.assertEquals(1010.0f, decodeFloat(dest, 12, "ieee4"), 0.1f);
		Assert.assertEquals(0, decode(dest, 16, "i2"));
		Assert.assertEquals(1, decode(dest, 18, "i2"));
		Assert.assertEquals(8, decode(dest, 20, "u1"));
		Assert.assertEquals(0, decode(dest, 21, "u1"));
	}

	@Test
	public void testDS1() {
		final float[] data = { 101.4f, 102.7f, Float.NaN, 101.9f, 101.0f, Float.NaN, 101.6f, 101.2f, Float.NaN, Float.NaN,
				101.1f, 101.8f, 102.9f, Float.NaN };

		final KryptonData2Packer00 p = KryptonData2Packer00.newInstance(data, 1.0f, 1, 10, true);
		final KryptonDataBinary2Builder b = new KryptonDataBinary2Builder(p);
		final Section2Buffer out = new Section2Buffer(b.sectionNo(), b.estimatedOctetCount());
		b.save(out);
		final byte[] dest = new byte[100];
		final int bc = out.emit(dest, 0);
		Assert.assertEquals(14, bc);
		Assert.assertEquals(14, decode(dest, 1, "i4"));
		Assert.assertEquals(7, decode(dest, 5, "u1"));
	}

	@Test
	public void testGDS() {
		final KryptonGrid2Builder b = new KryptonGrid2Builder();
		b.newTemplate3_0(360, 1.0f, 181, 1.0f, 90.0f, 0.0f, -90.0f, 359.0f);
		final Section2Buffer out = new Section2Buffer(b.sectionNo(), 13);
		b.save(out);
		final byte[] dest = new byte[100];
		final int bc = out.emit(dest, 0);
		Assert.assertEquals(72, bc);
		Assert.assertEquals(72, decode(dest, 1, "i4"));
		Assert.assertEquals(3, decode(dest, 5, "u1"));
		Assert.assertEquals(65160, decode(dest, 7, "i4"));
		Assert.assertEquals(0, decode(dest, 13, "i2"));
		Assert.assertEquals(6, decode(dest, 15, "u1"));
		Assert.assertEquals(0, decode(dest, 16, "u1"));
		Assert.assertEquals(0, decode(dest, 17, "i4"));
		Assert.assertEquals(0, decode(dest, 21, "u1"));
		Assert.assertEquals(0, decode(dest, 22, "i4"));
		Assert.assertEquals(0, decode(dest, 26, "u1"));
		Assert.assertEquals(0, decode(dest, 27, "i4"));
		Assert.assertEquals(360, decode(dest, 31, "i4"));
		Assert.assertEquals(181, decode(dest, 35, "i4"));
		Assert.assertEquals(0, decode(dest, 39, "i4"));
		Assert.assertEquals(0, decode(dest, 43, "i4"));
		Assert.assertEquals(90_000_000, decode(dest, 47, "i4"));
		Assert.assertEquals(0, decode(dest, 51, "i4"));
		Assert.assertEquals(48, decode(dest, 55, "u1"));
		Assert.assertEquals(-90_000_000, decode(dest, 56, "i4"));
		Assert.assertEquals(359_000_000, decode(dest, 60, "i4"));
		Assert.assertEquals(1_000_000, decode(dest, 64, "i4"));
		Assert.assertEquals(1_000_000, decode(dest, 68, "i4"));
		Assert.assertEquals(0, decode(dest, 72, "u1"));
	}

	@Test
	public void testIDS() {

		final int centre = 72;
		final int typeOfData = KryptonIdentification2Builder.Table1_4.Forecast_Products;
		final Date refTime = DateFactory.newDateConstantFromTX("20030401T1800Z");
		final KryptonIdentification2Builder b = new KryptonIdentification2Builder(centre, typeOfData, refTime);
		final Section2Buffer out = new Section2Buffer(b.sectionNo(), 13);
		b.save(out);
		final byte[] dest = new byte[100];
		final int bc = out.emit(dest, 0);
		Assert.assertEquals(21, bc);
		Assert.assertEquals(21, decode(dest, 1, "i4"));
		Assert.assertEquals(1, decode(dest, 5, "u1"));
		Assert.assertEquals(2003, decode(dest, 13, "i2"));
		Assert.assertEquals(4, decode(dest, 15, "u1"));
		Assert.assertEquals(1, decode(dest, 16, "u1"));
		Assert.assertEquals(18, decode(dest, 17, "u1"));
	}

}
