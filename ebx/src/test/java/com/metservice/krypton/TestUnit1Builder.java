/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

	private long decodeLong(byte[] dest, int octet) {
		final int pos = octet - 1;
		return UGrib.long8(dest, pos);
	}

	@Test
	public void testBMS1()
			throws KryptonBuildException {
		final float[] data = { 101.4f, 102.7f, Float.NaN, 101.9f, 101.0f, Float.NaN, 101.6f, 101.2f, Float.NaN, Float.NaN,
				101.1f, 101.8f, 102.9f, Float.NaN };

		final KryptonData2Packer00 p = KryptonData2Packer00.newInstance(data, 1.0f, 1, 10, true);
		final KryptonBitmap2Builder bms = new KryptonBitmap2Builder(p);
		final Section2Buffer out = bms.newBuffer();
		final byte[] dest = out.emit();
		Assert.assertEquals(8, dest.length);
		Assert.assertEquals(8, decode(dest, 1, "i4"));
		Assert.assertEquals(6, decode(dest, 5, "u1"));
		Assert.assertEquals(0, decode(dest, 6, "u1"));
	}

	@Test
	public void testBMS2()
			throws KryptonBuildException {
		final KryptonBitmap2Builder b = new KryptonBitmap2Builder(null);
		final Section2Buffer out = b.newBuffer();
		final byte[] dest = out.emit();
		Assert.assertEquals(6, dest.length);
		Assert.assertEquals(6, decode(dest, 1, "i4"));
		Assert.assertEquals(6, decode(dest, 5, "u1"));
		Assert.assertEquals(255, decode(dest, 6, "u1"));
	}

	@Test
	public void testBMS3()
			throws KryptonBuildException {
		final float[] data = { 101.4f, 102.7f, 101.9f, 101.0f, 101.6f, 101.2f, 101.1f, 101.8f, 102.9f };
		final KryptonData2Packer00 p = KryptonData2Packer00.newInstance(data, 1.0f, 1, 10, true);
		final KryptonBitmap2Builder b = new KryptonBitmap2Builder(p);
		final Section2Buffer out = b.newBuffer();
		final byte[] dest = out.emit();
		Assert.assertEquals(6, dest.length);
		Assert.assertEquals(6, decode(dest, 1, "i4"));
		Assert.assertEquals(6, decode(dest, 5, "u1"));
		Assert.assertEquals(255, decode(dest, 6, "u1"));
	}

	@Test
	public void testDBS1()
			throws KryptonBuildException {
		final float[] data = { Float.NaN, 101.4f, 102.7f, 101.9f, 101.0f, Float.NaN, 101.6f, 101.2f, Float.NaN, Float.NaN,
				101.1f, 101.8f, 102.9f, Float.NaN };

		final KryptonData2Packer00 p = KryptonData2Packer00.newInstance(data, 1.0f, 1, 10, true);
		final KryptonDataBinary2Builder b = new KryptonDataBinary2Builder(p);
		final Section2Buffer out = b.newBuffer();
		final byte[] dest = out.emit();
		Assert.assertEquals(14, dest.length);
		Assert.assertEquals(14, decode(dest, 1, "i4"));
		Assert.assertEquals(7, decode(dest, 5, "u1"));
	}

	@Test
	public void testDBS2()
			throws KryptonBuildException {
		final float[] data = { 101.4f, 101.4f, 101.4f };
		final KryptonData2Packer00 p = KryptonData2Packer00.newInstance(data, 1.0f, 1, 10, true);
		final KryptonDataBinary2Builder b = new KryptonDataBinary2Builder(p);
		final Section2Buffer out = b.newBuffer();
		final byte[] dest = out.emit();
		Assert.assertEquals(6, dest.length);
		Assert.assertEquals(6, decode(dest, 1, "i4"));
		Assert.assertEquals(7, decode(dest, 5, "u1"));
	}

	@Test
	public void testDRS()
			throws KryptonBuildException {
		final float[] data = { 101.4f, 102.7f, Float.NaN, 101.9f, 101.0f, Float.NaN, 101.6f, 101.2f, Float.NaN, Float.NaN,
				101.1f, 101.8f, 102.9f, Float.NaN };

		final KryptonData2Packer00 p = KryptonData2Packer00.newInstance(data, 1.0f, 1, 10, true);
		final KryptonDataRepresentation2Builder b = new KryptonDataRepresentation2Builder(p);
		final Section2Buffer out = b.newBuffer();
		final byte[] dest = out.emit();
		Assert.assertEquals(21, dest.length);
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
	public void testGDS()
			throws KryptonBuildException {
		final KryptonGrid2Builder b = new KryptonGrid2Builder();
		b.newTemplate3_0(360, 1.0f, 181, 1.0f, 90.0f, 0.0f, -90.0f, 359.0f);
		final Section2Buffer out = b.newBuffer();
		final byte[] dest = out.emit();
		Assert.assertEquals(72, dest.length);
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
	public void testIDS()
			throws KryptonBuildException {
		final int centre = 72;
		final int typeOfData = KryptonIdentification2Builder.Table1_4.Forecast_Products;
		final Date refTime = DateFactory.newDateConstantFromTX("20030401T1800Z");
		final KryptonIdentification2Builder b = new KryptonIdentification2Builder(centre, typeOfData, refTime);
		final Section2Buffer out = b.newBuffer();
		final byte[] dest = out.emit();
		Assert.assertEquals(21, dest.length);
		Assert.assertEquals(21, decode(dest, 1, "i4"));
		Assert.assertEquals(1, decode(dest, 5, "u1"));
		Assert.assertEquals(2003, decode(dest, 13, "i2"));
		Assert.assertEquals(4, decode(dest, 15, "u1"));
		Assert.assertEquals(1, decode(dest, 16, "u1"));
		Assert.assertEquals(18, decode(dest, 17, "u1"));
	}

	@Test
	public void testRecord() {
		final int centre = 72;
		final int typeOfData = KryptonIdentification2Builder.Table1_4.Forecast_Products;
		final Date refTime = DateFactory.newDateConstantFromTX("20030401T1800Z");
		final KryptonIdentification2Builder ids = new KryptonIdentification2Builder(centre, typeOfData, refTime);

		final KryptonGrid2Builder gds = new KryptonGrid2Builder();
		gds.newTemplate3_0(360, 1.0f, 181, 1.0f, 90.0f, 0.0f, -90.0f, 359.0f);

		final KryptonProduct2Builder pds = new KryptonProduct2Builder();
		final int parameterCategory = 16; // Forecast radar imagery
		final int parameterNo = 4; // Reflectivity
		pds.newTemplate4_0(parameterCategory, parameterNo);

		final float[] data = { 101.4f, 102.7f, Float.NaN, 101.9f, 101.0f, Float.NaN, 101.6f, 101.2f, Float.NaN, Float.NaN,
				101.1f, 101.8f, 102.9f, Float.NaN };

		final KryptonData2Packer00 p = KryptonData2Packer00.newInstance(data, 1.0f, 1, 10, true);
		final KryptonDataRepresentation2Builder drs = new KryptonDataRepresentation2Builder(p);
		final KryptonBitmap2Builder bms = new KryptonBitmap2Builder(p);
		final KryptonDataBinary2Builder dbs = new KryptonDataBinary2Builder(p);

		final KryptonRecord2Builder r = KryptonRecord2Builder.newMeteorological(ids, gds, pds, drs, bms, dbs);
		final ByteArrayOutputStream ba = new ByteArrayOutputStream();
		final long ExpectedByteCount = 168;
		try {
			final long bc = r.save(ba);
			Assert.assertEquals(ExpectedByteCount, bc);
		} catch (KryptonBuildException | IOException ex) {
			Assert.fail(ex.getMessage());
		} finally {
			try {
				ba.close();
			} catch (final IOException ex) {
			}
		}
		final byte[] dest = ba.toByteArray();
		Assert.assertEquals(ExpectedByteCount, dest.length);
		Assert.assertEquals(ExpectedByteCount, decodeLong(dest, 9));
		Assert.assertEquals(21, decode(dest, 16 + 1, "i4"));
		Assert.assertEquals(1, decode(dest, 16 + 5, "u1"));
	}

}
