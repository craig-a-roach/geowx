/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium;

import org.junit.Assert;
import org.junit.Test;

import com.metservice.gallium.GalliumPoly.Builder;

/**
 * @author roach
 */
public class TestUnit1Poly {

	private static final float Epsilon = Math.ulp(1.0f);

	@Test
	public void t10_bounds() {
		final Builder b = GalliumPoly.newBuilder(1);
		b.addTail(10.0f, 20.f);
		b.addTail(11.0f, 21.f);
		b.addTail(12.0f, 22.f);
		b.addTail(13.0f, 23.f);
		final GalliumPoly p = GalliumPoly.createInstance(b);
		final GalliumBoundingBoxF bb = p.bounds();
		Assert.assertEquals("bb.yLo", 10.0f, bb.yLo(), Epsilon);
		Assert.assertEquals("bb.xLo", 20.0f, bb.xLo(), Epsilon);
		Assert.assertEquals("bb.yHi", 13.0f, bb.yHi(), Epsilon);
		Assert.assertEquals("bb.xHi", 23.0f, bb.xHi(), Epsilon);
		Assert.assertEquals("pointCount", 4, p.pointCount());
		Assert.assertEquals("Y1", 11.0f, p.pointY(1), Epsilon);
		Assert.assertEquals("X1", 21.0f, p.pointX(1), Epsilon);
	}

	@Test
	public void t20_reverse() {
		final Builder b3 = GalliumPoly.newBuilder(1);
		b3.addTail(10.0f, 20.f);
		b3.addTail(11.0f, 21.f);
		b3.addTail(12.0f, 22.f);
		b3.reverse();
		final GalliumPoly p3 = GalliumPoly.createInstance(b3);
		Assert.assertEquals("Y0", 12.0f, p3.pointY(0), Epsilon);
		Assert.assertEquals("X0", 22.0f, p3.pointX(0), Epsilon);
		Assert.assertEquals("Y1", 11.0f, p3.pointY(1), Epsilon);
		Assert.assertEquals("X1", 21.0f, p3.pointX(1), Epsilon);
		Assert.assertEquals("Y2", 10.0f, p3.pointY(2), Epsilon);
		Assert.assertEquals("X3", 20.0f, p3.pointX(2), Epsilon);

		final Builder b4 = GalliumPoly.newBuilder(1);
		b4.addTail(10.0f, 20.f);
		b4.addTail(11.0f, 21.f);
		b4.addTail(12.0f, 22.f);
		b4.addTail(13.0f, 23.f);
		b4.reverse();
		final GalliumPoly p4 = GalliumPoly.createInstance(b4);
		Assert.assertEquals("Y1", 12.0f, p4.pointY(1), Epsilon);
		Assert.assertEquals("X1", 22.0f, p4.pointX(1), Epsilon);
		Assert.assertEquals("Y2", 11.0f, p4.pointY(2), Epsilon);
		Assert.assertEquals("X3", 21.0f, p4.pointX(2), Epsilon);
	}

	@Test
	public void t30_join() {
		final Builder b1 = GalliumPoly.newBuilder();
		b1.addTail(10.0f, 5.0f);
		b1.addTail(11.0f, 6.0f);
		b1.addTail(12.0f, 7.0f);
		final GalliumPoly.Builder b2 = GalliumPoly.newBuilder();
		b2.addTail(9.0f, 4.0f);
		b2.addTail(8.0f, 3.0f);
		final float[] expected = { 8.0f, 3.0f, 9.0f, 4.0f, 10.0f, 5.0f, 11.0f, 6.0f, 12.0f, 7.0f };
		final int pc = expected.length / 2;

		final Builder b1A = b1.newMutableClone();
		final Builder b2A = b2.newMutableClone();
		final Builder b1B = b1.newMutableClone();
		final Builder b2B = b2.newMutableClone();
		final Builder b1C = b1.newMutableClone();
		final Builder b2C = b2.newMutableClone();
		final Builder b1D = b1.newMutableClone();
		final Builder b2D = b2.newMutableClone();

		// 1 >>>>
		// 2: [[[[

		b2A.reverse(); // ]]]]]
		b1A.insertHead(b2A); // ]]]]]>>>>

		b2B.reverse(); // ]]]]
		b2B.addTail(b1B); // ]]]]>>>>

		b1C.reverse(); // <<<<
		b2C.insertHead(b1C); // <<<<[[[[[

		b1D.reverse(); // <<<<
		b1D.addTail(b2D); // <<<<[[[[

		final GalliumPoly polyA = GalliumPoly.createInstance(b1A);
		final GalliumPoly polyB = GalliumPoly.createInstance(b2B);
		final GalliumPoly polyC = GalliumPoly.createInstance(b2C);
		final GalliumPoly polyD = GalliumPoly.createInstance(b1D);
		Assert.assertEquals("pointCount", pc, polyA.pointCount());
		Assert.assertEquals("pointCount", pc, polyB.pointCount());
		Assert.assertEquals("pointCount", pc, polyC.pointCount());
		Assert.assertEquals("pointCount", pc, polyD.pointCount());
		for (int p = 0, pr = pc - 1, i = 0; p < pc; p++, pr--, i += 2) {
			Assert.assertEquals("Ay" + p, expected[i], polyA.pointY(p), Epsilon);
			Assert.assertEquals("Ax" + p, expected[i + 1], polyA.pointX(p), Epsilon);
			Assert.assertEquals("By" + p, expected[i], polyB.pointY(p), Epsilon);
			Assert.assertEquals("Bx" + p, expected[i + 1], polyB.pointX(p), Epsilon);
			Assert.assertEquals("Cy" + p, expected[i], polyC.pointY(pr), Epsilon);
			Assert.assertEquals("Cx" + p, expected[i + 1], polyC.pointX(pr), Epsilon);
			Assert.assertEquals("Dy" + p, expected[i], polyD.pointY(pr), Epsilon);
			Assert.assertEquals("Dx" + p, expected[i + 1], polyD.pointX(pr), Epsilon);
		}
	}

}
