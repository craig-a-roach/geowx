/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author roach
 */
public class TestUnit1GribBytes {

	@Test
	public void t30_integer2() {
		final List<I2> tests = new ArrayList<I2>();
		tests.add(new I2(UGrib.INT_UNDEFINED, 0xFF, 0xFF));
		tests.add(new I2(-1000, 0x83, 0xE8));
		tests.add(new I2(-23, 0x80, 0x17));
		tests.add(new I2(0, 0x00, 0x00));
		tests.add(new I2(23, 0x00, 0x17));
		tests.add(new I2(1000, 0x03, 0xE8));
		for (final I2 t : tests) {
			Assert.assertEquals(t.i2, UGrib.int2(t.bytes, 0));
			Assert.assertArrayEquals(t.bytes, UGrib.int2(new byte[2], 0, t.i2));
		}
	}

	@Test
	public void t35_integeru2() {
		final List<I2> tests = new ArrayList<I2>();
		tests.add(new I2(0, 0x00, 0x00));
		tests.add(new I2(23, 0x00, 0x17));
		tests.add(new I2(1000, 0x03, 0xE8));
		tests.add(new I2(51000, 0xC7, 0x38));
		for (final I2 t : tests) {
			Assert.assertEquals(t.i2, UGrib.intu2(t.bytes, 0));
			Assert.assertArrayEquals(t.bytes, UGrib.intu2(new byte[2], 0, t.i2));
		}
	}

	@Test
	public void t40_integer3() {
		final List<I3> tests = new ArrayList<I3>();
		tests.add(new I3(UGrib.INT_UNDEFINED, 0xFF, 0xFF, 0xFF));
		tests.add(new I3(-7000000, 0xEA, 0xCF, 0xC0));
		tests.add(new I3(-1000, 0x80, 0x03, 0xE8));
		tests.add(new I3(-23, 0x80, 0x00, 0x17));
		tests.add(new I3(0, 0x00, 0x00, 0x00));
		tests.add(new I3(23, 0x00, 0x00, 0x17));
		tests.add(new I3(1000, 0x00, 0x03, 0xE8));
		tests.add(new I3(7000000, 0x6A, 0xCF, 0xC0));
		for (final I3 t : tests) {
			Assert.assertEquals(t.i3, UGrib.int3(t.bytes, 0));
			Assert.assertArrayEquals(t.bytes, UGrib.int3(new byte[3], 0, t.i3));
		}
	}

	@Test
	public void t45_integeru3() {
		final List<I3> tests = new ArrayList<I3>();
		tests.add(new I3(0, 0x00, 0x00, 0x00));
		tests.add(new I3(23, 0x00, 0x00, 0x17));
		tests.add(new I3(1000, 0x00, 0x03, 0xE8));
		tests.add(new I3(51000, 0x00, 0xC7, 0x38));
		tests.add(new I3(16700001, 0xFE, 0xD2, 0x61));
		for (final I3 t : tests) {
			Assert.assertEquals(t.i3, UGrib.intu3(t.bytes, 0));
			Assert.assertArrayEquals(t.bytes, UGrib.intu3(new byte[3], 0, t.i3));
		}
	}

	@Test
	public void t50_integer4() {
		final List<I4> tests = new ArrayList<I4>();
		tests.add(new I4(UGrib.INT_UNDEFINED, 0xFF, 0xFF, 0xFF, 0xFF));
		tests.add(new I4(-1300000000, 0xCD, 0x7C, 0x6D, 0x00));
		tests.add(new I4(-7000000, 0x80, 0x6A, 0xCF, 0xC0));
		tests.add(new I4(-23, 0x80, 0x00, 0x00, 0x17));
		tests.add(new I4(0, 0x00, 0x00, 0x00, 0x00));
		tests.add(new I4(23, 0x00, 0x00, 0x00, 0x17));
		tests.add(new I4(7000000, 0x00, 0x6A, 0xCF, 0xC0));
		tests.add(new I4(1300000000, 0x4D, 0x7C, 0x6D, 0x00));
		for (final I4 t : tests) {
			Assert.assertEquals(t.i4, UGrib.int4(t.bytes, 0));
			Assert.assertArrayEquals(t.bytes, UGrib.int4(new byte[4], 0, t.i4));
		}
	}

	@Test
	public void t60_long8() {
		final List<L8> tests = new ArrayList<L8>();
		tests.add(new L8(-2105843009213693953L, 0x9D, 0x39, 0x75, 0x0F, 0x44, 0xEC, 0x00, 0x01));
		tests.add(new L8(2105843009213693953L, 0x1D, 0x39, 0x75, 0x0F, 0x44, 0xEC, 0x00, 0x01));
		for (final L8 t : tests) {
			Assert.assertEquals(t.l8, UGrib.long8(t.bytes, 0));
			Assert.assertArrayEquals(t.bytes, UGrib.long8(new byte[8], 0, t.l8));
		}
	}

	@Test
	public void t70_floatIBM() {

		final List<F4> tests = new ArrayList<F4>();
		tests.add(new F4(65399.5f, 68, 255, 119, 128, 10000.0f));
		tests.add(new F4(273.96875f, 67, 17, 31, 128, 100.0f));
		tests.add(new F4(264.98682f, 67, 16, 143, 202, 100.0f));

		tests.add(new F4(-10.0f, 0xC1, 0xA0, 0x00, 0x00, 10.0f));
		tests.add(new F4(-9.9f, 0xC1, 0x9E, 0x66, 0x66, 10.0f));
		tests.add(new F4(-9.3f, 0xC1, 0x94, 0xCC, 0xCD, 10.0f));
		tests.add(new F4(-0.7f, 0xC0, 0xB3, 0x33, 0x33, 1.0f));
		tests.add(new F4(0.0f, 0x00, 0x00, 0x00, 0x00, 0.1f));
		tests.add(new F4(0.1f, 0x40, 0x19, 0x99, 0x99, 1.0f));
		tests.add(new F4(0.3f, 0x40, 0x4C, 0xCC, 0xCD, 1.0f));
		tests.add(new F4(9.9f, 0x41, 0x9E, 0x66, 0x66, 10.0f));

		for (final F4 t : tests) {
			Assert.assertEquals(t.f4, UGrib.float4IBM(t.bytes, 0), Math.ulp(t.ulp));
			Assert.assertArrayEquals(t.bytes, UGrib.float4IBM(new byte[4], 0, t.f4));
		}
	}

	@Test
	public void t75_floatIEEE() {

		final List<F4> tests = new ArrayList<F4>();
		tests.add(new F4(65399.5f, 0x47, 0x7f, 0x77, 0x80, 10000.0f));
		tests.add(new F4(273.96875f, 0x43, 0x88, 0xfc, 0x00, 100.0f));
		tests.add(new F4(264.98682f, 0x43, 0x84, 0x7e, 0x50, 100.0f));

		tests.add(new F4(-10.0f, 0xc1, 0x20, 0x0, 0x0, 10.0f));
		tests.add(new F4(-9.9f, 0xc1, 0x1e, 0x66, 0x66, 10.0f));
		tests.add(new F4(-9.3f, 0xc1, 0x14, 0xcc, 0xcd, 10.0f));
		tests.add(new F4(-0.7f, 0xbf, 0x33, 0x33, 0x33, 1.0f));
		tests.add(new F4(0.0f, 0x00, 0x00, 0x00, 0x00, 0.1f));
		tests.add(new F4(0.1f, 0x3d, 0xcc, 0xcc, 0xcd, 1.0f));
		tests.add(new F4(0.3f, 0x3e, 0x99, 0x99, 0x9a, 1.0f));
		tests.add(new F4(9.9f, 0x41, 0x1e, 0x66, 0x66, 10.0f));

		for (final F4 t : tests) {
			System.out.println(t.showBytesIEEE());
			Assert.assertEquals(t.f4, UGrib.float4IEEE(t.bytes, 0), Math.ulp(t.ulp));
			Assert.assertArrayEquals(t.bytes, UGrib.float4IEEE(new byte[4], 0, t.f4));
		}
	}

	private static class F4 {

		private static String showBytes(byte[] ba) {
			final StringBuilder sb = new StringBuilder();
			for (int i = 0; i < ba.length; i++) {
				if (i > 0) {
					sb.append(',');
				}
				sb.append("0x");
				sb.append(Integer.toHexString(ba[i] & 0xFF));
			}
			return sb.toString();
		}

		public String showBytesIEEE() {
			final byte[] buffer = new byte[4];
			UGrib.float4IEEE(buffer, 0, f4);
			return showBytes(buffer);
		}

		@Override
		public String toString() {
			return showBytes(bytes);
		}

		public F4(float f4, int b0, int b1, int b2, int b3, float ulp) {
			this.f4 = f4;
			this.bytes[0] = (byte) b0;
			this.bytes[1] = (byte) b1;
			this.bytes[2] = (byte) b2;
			this.bytes[3] = (byte) b3;
			this.ulp = ulp;
		}
		public final float f4;
		public final byte[] bytes = new byte[4];
		public final float ulp;
	}

	private static class I2 {

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append("0x");
			for (int i = 0; i < bytes.length; i++) {
				sb.append(Integer.toHexString(bytes[i] & 0xFF));
			}
			return sb.toString();
		}

		public I2(int i2, int b0, int b1) {
			this.i2 = i2;
			this.bytes[0] = (byte) b0;
			this.bytes[1] = (byte) b1;
		}
		public final int i2;
		public final byte[] bytes = new byte[2];
	}

	private static class I3 {

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append("0x");
			for (int i = 0; i < bytes.length; i++) {
				sb.append(Integer.toHexString(bytes[i] & 0xFF));
			}
			return sb.toString();
		}

		public I3(int i3, int b0, int b1, int b2) {
			this.i3 = i3;
			this.bytes[0] = (byte) b0;
			this.bytes[1] = (byte) b1;
			this.bytes[2] = (byte) b2;
		}
		public final int i3;
		public final byte[] bytes = new byte[3];
	}

	private static class I4 {

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append("0x");
			for (int i = 0; i < bytes.length; i++) {
				sb.append(Integer.toHexString(bytes[i] & 0xFF));
			}
			return sb.toString();
		}

		public I4(int i4, int b0, int b1, int b2, int b3) {
			this.i4 = i4;
			this.bytes[0] = (byte) b0;
			this.bytes[1] = (byte) b1;
			this.bytes[2] = (byte) b2;
			this.bytes[3] = (byte) b3;
		}
		public final int i4;
		public final byte[] bytes = new byte[4];
	}

	private static class L8 {

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append("0x");
			for (int i = 0; i < bytes.length; i++) {
				sb.append(Integer.toHexString(bytes[i] & 0xFF));
			}
			return sb.toString();
		}

		public L8(long l8, int b0, int b1, int b2, int b3, int b4, int b5, int b6, int b7) {
			this.l8 = l8;
			this.bytes[0] = (byte) b0;
			this.bytes[1] = (byte) b1;
			this.bytes[2] = (byte) b2;
			this.bytes[3] = (byte) b3;
			this.bytes[4] = (byte) b4;
			this.bytes[5] = (byte) b5;
			this.bytes[6] = (byte) b6;
			this.bytes[7] = (byte) b7;
		}
		public final long l8;
		public final byte[] bytes = new byte[8];
	}

}
