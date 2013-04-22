/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import com.metservice.cobalt.CobaltSurfaceTopBottom;
import com.metservice.cobalt.CobaltSurfaceUnary;
import com.metservice.cobalt.CobaltSurfaceZero;
import com.metservice.cobalt.ICobaltSurface;

/**
 * @author roach
 */
class SurfaceDecoder {

	private static final CobaltSurfaceZero[] GRIB1Zero_1_9 = { CobaltSurfaceZero.Ground, CobaltSurfaceZero.CloudBase,
			CobaltSurfaceZero.CloudTops, CobaltSurfaceZero.IsothermC0, CobaltSurfaceZero.AdiabaticCondensationLifted,
			CobaltSurfaceZero.MaximumWind, CobaltSurfaceZero.Tropopause, CobaltSurfaceZero.NominalTopOfAtmosphere,
			CobaltSurfaceZero.SeaBottom };

	private static final CobaltSurfaceZero[] GRIB2Zero_1_9 = { CobaltSurfaceZero.Ground, CobaltSurfaceZero.CloudBase,
			CobaltSurfaceZero.CloudTops, CobaltSurfaceZero.IsothermC0, CobaltSurfaceZero.AdiabaticCondensationLifted,
			CobaltSurfaceZero.MaximumWind, CobaltSurfaceZero.Tropopause, CobaltSurfaceZero.NominalTopOfAtmosphere,
			CobaltSurfaceZero.SeaBottom };

	private static String reservedG1(short type) {
		return "Surface type " + type + " is reserved (GRIB1 Code Table 3)";
	}

	private static String reservedG2(short type) {
		return "Surface type " + type + " is reserved (GRIB2 Code Table 4.5)";
	}

	private ICobaltSurface newSurfaceG2(String source, short type, int scaleFactor, int scaledValue)
			throws KryptonCodeException {

		if (type == 0) throw new KryptonCodeException(source, reservedG2(type));

		if (type >= 1 && type <= 9) return GRIB2Zero_1_9[type - 1];

		switch (type) {
			case 200:
				return CobaltSurfaceZero.EntireAtmosphereAsSingleLayer;
			case 201:
				return CobaltSurfaceZero.EntireOceanAsSingleLayer;
			case 204:
				return CobaltSurfaceZero.HighestTroposphericFreezingLevel;
			case 209:
				return CobaltSurfaceZero.BoundaryCloudBottom;
			case 210:
				return CobaltSurfaceZero.BoundaryCloudTop;
			case 211:
				return CobaltSurfaceZero.BoundaryCloudLayer;
			case 212:
				return CobaltSurfaceZero.LowCloudBottom;
			case 213:
				return CobaltSurfaceZero.LowCloudTop;
			case 214:
				return CobaltSurfaceZero.LowCloudLayer;
			case 222:
				return CobaltSurfaceZero.MiddleCloudBottom;
			case 223:
				return CobaltSurfaceZero.MiddleCloudTop;
			case 224:
				return CobaltSurfaceZero.MiddleCloudLayer;
			case 232:
				return CobaltSurfaceZero.HighCloudBottom;
			case 233:
				return CobaltSurfaceZero.HighCloudTop;
			case 234:
				return CobaltSurfaceZero.HighCloudLayer;
			case 220:
				return CobaltSurfaceZero.PlanetaryBoundaryLayer;
			case 242:
				return CobaltSurfaceZero.ConvectiveCloudBottom;
			case 243:
				return CobaltSurfaceZero.ConvectiveCloudTop;
			case 244:
				return CobaltSurfaceZero.ConvectiveCloudLayer;
		}

		final double datum = UGrib.descale(scaleFactor, scaledValue);
		switch (type) {
			case 100: {
				final double hPa = datum / 100.0;
				return CobaltSurfaceUnary.newIsobaric(hPa);
			}
			case 101:
				return CobaltSurfaceZero.MSL;
			case 102:
				return CobaltSurfaceUnary.newAboveMSL(datum);
			case 103:
				return CobaltSurfaceUnary.newAboveGround(datum);
			case 104:
				return CobaltSurfaceUnary.newSigma(datum);
			case 109:
				return CobaltSurfaceUnary.newPotentialVorticity(datum);
		}
		throw new KryptonCodeException(source, "Surface (single) type " + type + " not supported (GRIB2 Code Table 4.5)");
	}

	private ICobaltSurface newSurfaceG2(String source, short type, int scaleFactor1, int scaledValue1, int scaleFactor2,
			int scaledValue2)
			throws KryptonCodeException {
		final double datum1 = UGrib.descale(scaleFactor1, scaledValue1);
		final double datum2 = UGrib.descale(scaleFactor2, scaledValue2);

		switch (type) {
			case 102:
				return CobaltSurfaceTopBottom.newAboveMSL(datum1, datum2);
			case 103:
				return CobaltSurfaceTopBottom.newAboveGround(datum1, datum2);
			case 104: {
				return CobaltSurfaceTopBottom.newSigma(datum1, datum2);
			}
			case 106: {
				final double cmTop = datum1 * 100.0;
				final double cmBot = datum2 * 100.0;
				return CobaltSurfaceTopBottom.newBelowGround(cmTop, cmBot);
			}
			case 108: {
				final double hPaTop = datum1 / 100.0;
				final double hPaBot = datum2 / 100.0;
				return CobaltSurfaceTopBottom.newPressureDifferenceFromGround(hPaTop, hPaBot);
			}
		}
		throw new KryptonCodeException(source, "Surface (range) type " + type + " not supported (GRIB2 Code Table 4.5)");
	}

	public ICobaltSurface newSurfaceG1(String source, short type, int v1, int v2)
			throws KryptonCodeException {

		if (type < 0 || type >= 255) {
			final String m = "Surface type " + type + " is undefined";
			throw new KryptonCodeException(source, m);
		}
		if (type == 0) throw new KryptonCodeException(source, reservedG1(type));

		if (type >= 1 && type <= 9) return GRIB1Zero_1_9[type - 1];

		if (type >= 10 && type <= 19) throw new KryptonCodeException(source, reservedG1(type));

		final int v12 = UGrib.intu2(v1, v2);

		if (type == 20) return CobaltSurfaceUnary.newIsothermal(v12 * 0.01f);

		if (type >= 21 && type <= 99) throw new KryptonCodeException(source, reservedG1(type));

		switch (type) {
			case 100:
				return CobaltSurfaceUnary.newIsobaric(v12);
			case 101:
				return CobaltSurfaceTopBottom.newIsobaric(v1 * 10.0f, v2 * 10.0f);
			case 102:
				return CobaltSurfaceZero.MSL;
			case 103:
				return CobaltSurfaceUnary.newAboveMSL(v12);
			case 104:
				return CobaltSurfaceTopBottom.newAboveMSL(v1 * 100.0, v2 * 100.);
			case 105:
				return CobaltSurfaceUnary.newAboveGround(v12);
			case 106:
				return CobaltSurfaceTopBottom.newAboveGround(v1 * 100.0, v2 * 100.0);
			case 107:
				return CobaltSurfaceUnary.newSigma(v12 * 1.0e-4);
			case 108:
				return CobaltSurfaceTopBottom.newSigma(v1 * 0.01, v2 * 0.01);
			case 109:
				return CobaltSurfaceUnary.newHybrid(v12);
			case 110:
				return CobaltSurfaceTopBottom.newHybrid(v1, v2);
			case 111:
				return CobaltSurfaceUnary.newBelowGround(v12);
			case 112:
				return CobaltSurfaceTopBottom.newBelowGround(v1, v2);
			case 113:
				return CobaltSurfaceUnary.newIsentropic(v12);
			case 114:
				return CobaltSurfaceTopBottom.newIsentropic(475.0 - v1, 475.0 - v2);
			case 115:
				return CobaltSurfaceUnary.newPressureDifferenceFromGround(v12);
			case 116:
				return CobaltSurfaceTopBottom.newPressureDifferenceFromGround(v1, v2);
			case 117:
				return CobaltSurfaceUnary.newPotentialVorticity(v12 * 1.0e-9);
		}

		if (type == 118) throw new KryptonCodeException(source, reservedG1(type));

		switch (type) {
			case 119:
				return CobaltSurfaceUnary.newEta(v12 * 1.0e-4);
			case 120:
				return CobaltSurfaceTopBottom.newEta(v1 * 0.01, v2 * 0.01);
			case 121:
				return CobaltSurfaceTopBottom.newIsobaric(1100.0 - v1, 1100.0 - v2);
		}

		if (type >= 122 && type <= 124) throw new KryptonCodeException(source, reservedG1(type));

		if (type == 125) return CobaltSurfaceUnary.newAboveGround(v12 * 0.01);

		if (type >= 126 && type <= 127) throw new KryptonCodeException(source, reservedG1(type));

		if (type == 128) return CobaltSurfaceTopBottom.newSigma(1.1 - (v1 * 0.001), 1.1 - (v2 * 0.001));

		if (type >= 129 && type <= 140) throw new KryptonCodeException(source, reservedG1(type));

		if (type == 141) return CobaltSurfaceTopBottom.newIsobaric(v1, 1100.0 - v2);

		if (type >= 142 && type <= 159) throw new KryptonCodeException(source, reservedG1(type));

		if (type == 160) return CobaltSurfaceUnary.newBelowMSL(v12);

		if (type >= 161 && type <= 199) throw new KryptonCodeException(source, reservedG1(type));

		switch (type) {
			case 200:
				return CobaltSurfaceZero.EntireAtmosphereAsSingleLayer;
			case 201:
				return CobaltSurfaceZero.EntireOceanAsSingleLayer;
		}

		if (type >= 202 && type <= 254) throw new KryptonCodeException(source, reservedG1(type));

		throw new KryptonCodeException(source, "Surface type " + type + " not supported (GRIB1 Code Table 3)");
	}

	public ICobaltSurface newSurfaceG2(String source, short type1, int scaleFactor1, int scaledValue1, short type2,
			int scaleFactor2, int scaledValue2)
			throws KryptonCodeException {
		if (type1 < 0 || type1 >= 255) {
			final String m = "Surface 1 type " + type1 + " is undefined";
			throw new KryptonCodeException(source, m);
		}
		if (type2 < 0 || type2 > 255) {
			final String m = "Surface 2 type " + type2 + " is undefined";
			throw new KryptonCodeException(source, m);
		}
		if (type2 == 255) return newSurfaceG2(source, type1, scaleFactor1, scaledValue1);
		if (type1 == type2) return newSurfaceG2(source, type1, scaleFactor1, scaledValue1, scaleFactor2, scaledValue2);
		throw new KryptonCodeException(source, "Surface range " + type1 + "," + type2 + " not supported (GRIB2 Code Table 4.5)");
	}

	public SurfaceDecoder() {
	}
}
