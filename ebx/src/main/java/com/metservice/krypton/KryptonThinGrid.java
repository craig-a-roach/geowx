/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
public class KryptonThinGrid {

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("mode", mode);
		ds.a("nx", nx);
		ds.a("ny", ny);
		ds.a("dx", dx);
		ds.a("dy", dy);
		ds.a("varptmap", varptmap);
		return ds.s();
	}

	public static KryptonThinGrid newInstance(ISectionGD1ThinGridReader r, int octetStart) {
		if (r == null) throw new IllegalArgumentException("object is null");

		final int scanningMode = r.scanningMode();
		final KryptonThinGridMode mode;
		int fixedDim;
		if ((scanningMode & 32) == 0) {
			mode = KryptonThinGridMode.FixYVarX;
			fixedDim = r.NY();
		} else {
			mode = KryptonThinGridMode.FixXVarY;
			fixedDim = r.NX();
		}

		int maxVarPts = 0;
		int octet = octetStart;
		final int[] varptmap = new int[fixedDim];
		for (int i = 0; i < fixedDim; i++) {
			final int ptcount = r.int2(octet);
			octet += 2;
			varptmap[i] = ptcount;
			if (maxVarPts < ptcount) {
				maxVarPts = ptcount;
			}
		}
		final int nx;
		final int ny;
		if (mode == KryptonThinGridMode.FixYVarX) {
			nx = maxVarPts;
			ny = r.NY();
		} else {
			nx = r.NX();
			ny = maxVarPts;
		}
		final double lon1 = r.longitude1();
		final double lon2 = r.longitude2();
		final double nlon2 = (lon2 < lon1) ? lon2 + 360.0 : lon2;
		final double dx = (nlon2 - lon1) / (nx - 1);
		final double dy = r.DY();
		return new KryptonThinGrid(mode, varptmap, nx, ny, dx, dy);
	}

	private KryptonThinGrid(KryptonThinGridMode mode, int[] varptmap, int nx, int ny, double dx, double dy) {
		assert mode != null;
		assert varptmap != null && varptmap.length > 0;
		this.mode = mode;
		this.varptmap = varptmap;
		this.nx = nx;
		this.ny = ny;
		this.dx = dx;
		this.dy = dy;
	}
	public final KryptonThinGridMode mode;
	public final int[] varptmap;
	public final int nx;
	public final int ny;
	public final double dx;
	public final double dy;

}
