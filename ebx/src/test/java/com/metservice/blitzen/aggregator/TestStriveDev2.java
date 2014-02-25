/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.aggregator;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * @author roach
 */
public class TestStriveDev2 {

	private static final double RTD = 180.0 / Math.PI;
	private static final double DTR = Math.PI / 180.0;
	private static final double HALFPI = Math.PI / 2.0;
	private static final double PI = Math.PI;
	private static final double TWOPI = Math.PI * 2.0;

	private static float _eval(Pt a, Pt b, Pt c) {
		if (a.x == c.x && a.y == c.y) return (float) TWOPI;
		final float crossProduct = crossProduct(a, b, c);
		if (crossProduct == 0.0f) return (float) PI;
		final boolean rightTurn = crossProduct < 0.0f;
		final float abx = b.x - a.x;
		final float aby = b.y - a.y;
		final float bcx = c.x - b.x;
		final float bcy = c.y - b.y;
		final double AB = Math.atan2(aby, abx);
		System.out.println("AB=" + (AB * RTD));
		final double BC = Math.atan2(bcy, bcx);
		System.out.println("BC=" + (BC * RTD));
		double result = 0.0;
		if (rightTurn) {
			final double ABC = AB - BC + PI;
			System.out.println("ABC=" + (ABC * RTD));
			result = ABC;
		} else {
			final double ABC = AB - BC + PI;
			System.out.println("ABC=" + (ABC * RTD));
			result = ABC;
		}
		final double n = result < 0.0 ? result + TWOPI : result;
		return (float) n;
	}

	private static float crossProduct(Pt a, Pt b, Pt c) {
		return ((b.x - a.x) * (c.y - a.y)) - ((b.y - a.y) * (c.x - a.x));
	}

	private static float eval(Pt a, Pt b, Pt c) {
		if (a.x == c.x && a.y == c.y) return (float) TWOPI;
		final float abx = b.x - a.x;
		final float aby = b.y - a.y;
		final float bcx = c.x - b.x;
		final float bcy = c.y - b.y;
		final double AB = Math.atan2(aby, abx);
		final double BC = Math.atan2(bcy, bcx);
		final double ABC = AB - BC + PI;
		final double result = ABC > 0.0 ? (ABC > TWOPI ? (ABC - TWOPI) : ABC) : (TWOPI + ABC);
		return (float) result;
	}

	@Test
	public void a30() {
		final Pt a = new Pt(3, 2.5);
		final Pt b = new Pt(2.5, 2.5);
		final List<Pt> clist = b.generate(Math.sqrt(2.0));
		final int ccount = clist.size();
		for (int i = 0; i < ccount; i++) {
			final Pt c = clist.get(i);
			final float z = eval(a, b, c);
			System.out.println(c + "->" + (z * RTD));
		}
	}

	@Test
	public void a40() {
		final Pt a = new Pt(2, 2);
		final Pt b = new Pt(1.5, 2.5);
		final List<Pt> clist = b.generate(Math.sqrt(2.0));
		final int ccount = clist.size();
		for (int i = 0; i < ccount; i++) {
			final Pt c = clist.get(i);
			final float z = eval(a, b, c);
			System.out.println(c + "->" + (z * RTD));
		}
	}

	@Test
	public void a50() {
		final Pt a = new Pt(1, 7);
		final Pt b = new Pt(3, 5);
		final List<Pt> clist = b.generate(Math.sqrt(8.0));
		final int ccount = clist.size();
		for (int i = 0; i < ccount; i++) {
			final Pt c = clist.get(i);
			final float z = eval(a, b, c);
			System.out.println(c + "->" + (z * RTD));
		}
	}

	private static class Pt {

		public List<Pt> generate(double r) {
			final List<Pt> list = new ArrayList<Pt>();
			for (int deg = 0; deg < 360; deg += 15) {
				final double theta = Math.PI - deg * DTR;
				final double xr = x + (r * Math.cos(theta));
				final double yr = y + (r * Math.sin(theta));
				list.add(new Pt(xr, yr));
			}
			return list;
		}

		@Override
		public String toString() {
			return "(" + x + "," + y + ")";
		}

		public Pt(double x, double y) {
			this.x = (float) x;
			this.y = (float) y;
		}
		final float x;
		final float y;
	}

}
