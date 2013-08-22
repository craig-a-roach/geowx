/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium;

import com.metservice.argon.ArgonNumber;

/**
 * @author roach
 */
class TestImpContourable implements IGalliumContourable {

	public static TestImpContourable newInstance(float latS, float lonW, float res, float[][] matrixNSWE) {
		final int ny = matrixNSWE.length;
		if (ny == 0) throw new IllegalArgumentException("Empty matrix");
		final int nx = matrixNSWE[0].length;
		final float[] array = new float[ny * nx];
		int pos = 0;
		for (int j = 0; j < ny; j++) {
			for (int i = 0; i < nx; i++) {
				array[pos] = matrixNSWE[j][i];
				pos++;
			}
		}
		return new TestImpContourable(latS, lonW, res, nx, ny, array);
	}

	private int arrayIndex(int y, int x) {
		final int ya = ny - y - 1;
		return (nx * ya) + x;
	}

	@Override
	public float datum(int y, int x) {
		return array[arrayIndex(y, x)];
	}

	@Override
	public float latitude(int y) {
		return latS + (y * resY);
	}

	@Override
	public float longitude(int x) {
		return lonW + (x * resY);
	}

	@Override
	public int pointCountX() {
		return nx;
	}

	@Override
	public int pointCountY() {
		return ny;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("-----lon>");
		for (int x = 0; x < nx; x++) {
			if (x > 0) {
				sb.append("|");
			}
			sb.append(longitude(x));
		}
		sb.append("\n");
		sb.append("lat :y x>");
		for (int x = 0; x < nx; x++) {
			if (x > 0) {
				sb.append("|");
			}
			sb.append(ArgonNumber.intToDec3(x));
		}
		sb.append("\n");
		for (int y = ny - 1; y >= 0; y--) {
			final float latitude = latitude(y);
			sb.append(latitude).append(":");
			sb.append(ArgonNumber.intToDec3(y)).append("|");
			for (int x = 0; x < nx; x++) {
				final float datum = datum(y, x);
				if (x > 0) {
					sb.append(" ");
				}
				sb.append(datum);
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	private TestImpContourable(float latS, float lonW, float res, int nx, int ny, float[] array) {
		this.latS = latS;
		this.latN = latS + (res * (ny - 1));
		this.lonW = lonW;
		this.lonE = lonW + (res * (nx - 1));
		this.resX = res;
		this.resY = res;
		this.nx = nx;
		this.ny = ny;
		this.array = array;
	}
	final float latS;
	final float latN;
	final float lonW;
	final float lonE;
	final float resX;
	final float resY;
	final int nx;
	final int ny;
	float[] array;
}
