/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import com.metservice.cobalt.CobaltPrognosisAggregate;
import com.metservice.cobalt.CobaltPrognosisPoint;
import com.metservice.cobalt.ICobaltPrognosis;

/**
 * @author roach
 */
class ValidityDecoder {

	public ICobaltPrognosis newPrognosisAggregateG2(String source, short processType, int ssecsFrom, int ssecsToex)
			throws KryptonCodeException {
		switch (processType) {
			case 0:
				return CobaltPrognosisAggregate.newAverage(ssecsFrom, ssecsToex);
			case 1:
				return CobaltPrognosisAggregate.newAccumulation(ssecsFrom, ssecsToex);
			case 2:
				return CobaltPrognosisAggregate.newMaximum(ssecsFrom, ssecsToex);
			case 3:
				return CobaltPrognosisAggregate.newMinimum(ssecsFrom, ssecsToex);
			case 4:
			case 8:
				return CobaltPrognosisAggregate.newDifference(ssecsFrom, ssecsToex);
			case 255:
				return CobaltPrognosisAggregate.newBetween(ssecsFrom, ssecsToex);
			default: {
				final String m = "Statistical process type  " + processType + " not supported (GRIB2 Code Table 4.10)";
				throw new KryptonCodeException(source, m);
			}
		}
	}

	public ICobaltPrognosis newPrognosisAggregateG2(String source, short processType, short unitFrom, int valueFrom, int ssecsToex)
			throws KryptonCodeException {
		final int ssecsFrom = UGrib.ssecsG2(source, unitFrom, valueFrom);
		return newPrognosisAggregateG2(source, processType, ssecsFrom, ssecsToex);
	}

	public ICobaltPrognosis newPrognosisG1(String source, short fUnit, int p1, int p2, int timeRangeValue)
			throws KryptonCodeException {
		switch (timeRangeValue) {
			case 0: {
				final int ssecs = UGrib.ssecsG1(source, fUnit, p1);
				return CobaltPrognosisPoint.newAt(ssecs);
			}

			case 1: {
				return CobaltPrognosisPoint.Zero;
			}

			case 2: {
				final int ssecsFrom = UGrib.ssecsG1(source, fUnit, p1);
				final int ssecsToex = UGrib.ssecsG1(source, fUnit, p2);
				return CobaltPrognosisAggregate.newBetween(ssecsFrom, ssecsToex);
			}

			case 3: {
				final int ssecsFrom = UGrib.ssecsG1(source, fUnit, p1);
				final int ssecsToex = UGrib.ssecsG1(source, fUnit, p2);
				return CobaltPrognosisAggregate.newAverage(ssecsFrom, ssecsToex);
			}

			case 4: {
				final int ssecsFrom = UGrib.ssecsG1(source, fUnit, p1);
				final int ssecsToex = UGrib.ssecsG1(source, fUnit, p2);
				return CobaltPrognosisAggregate.newAccumulation(ssecsFrom, ssecsToex);
			}

			case 5: {
				final int ssecsFrom = UGrib.ssecsG1(source, fUnit, p1);
				final int ssecsToex = UGrib.ssecsG1(source, fUnit, p2);
				return CobaltPrognosisAggregate.newDifference(ssecsFrom, ssecsToex);
			}

			case 10: {
				final int ssecs = UGrib.ssecsG1(source, fUnit, UGrib.int2(p1, p2));
				return CobaltPrognosisPoint.newAt(ssecs);
			}

			default: {
				final String m = "Time range " + timeRangeValue + " not supported (GRIB1 Code Table 5)";
				throw new KryptonCodeException(source, m);
			}
		}
	}

	public ICobaltPrognosis newPrognosisPointG2(String source, short unit, int value)
			throws KryptonCodeException {
		final int ssecs = UGrib.ssecsG2(source, unit, value);
		return CobaltPrognosisPoint.newAt(ssecs);
	}

	public ValidityDecoder() {
	}

	// 18(1) 19(0) end(0h) 50(0) 51(2) 52(1) 53(0) 57(255) 58(0)
	// 18(1) 19(0) end(0h) 50(0) 51(2) 52(1) 53(0) 57(255) 58(0)
	// 18(1) 19(0) end(0h) 50(0) 51(2) 52(1) 53(0) 57(255) 58(0)
	// 18(1) 19(0) end(0h) 50(0) 51(2) 52(1) 53(0) 57(255) 58(0)
	// 18(1) 19(0) end(0h) 50(0) 51(2) 52(1) 53(0) 57(255) 58(0)
	// 18(1) 19(0) end(0h) 50(0) 51(2) 52(1) 53(0) 57(255) 58(0)
	// 18(1) 19(0) end(0h) 50(0) 51(2) 52(1) 53(0) 57(255) 58(0)
	// 18(1) 19(0) end(0h) 50(0) 51(2) 52(1) 53(0) 57(255) 58(0)
	// 18(1) 19(0) end(6h) 50(2) 51(2) 52(1) 53(6) 57(255) 58(0)
	// 18(1) 19(0) end(6h) 50(3) 51(2) 52(1) 53(6) 57(255) 58(0)
	// 18(1) 19(0) end(6h) 50(1) 51(2) 52(1) 53(6) 57(255) 58(0)
	// 18(1) 19(0) end(6h) 50(0) 51(2) 52(1) 53(6) 57(255) 58(0)
	// 18(1) 19(0) end(6h) 50(0) 51(2) 52(1) 53(6) 57(255) 58(0)
	// 18(1) 19(0) end(6h) 50(0) 51(2) 52(1) 53(6) 57(255) 58(0)
	// 18(1) 19(0) end(6h) 50(0) 51(2) 52(1) 53(6) 57(255) 58(0)
	// 18(1) 19(0) end(6h) 50(0) 51(2) 52(1) 53(6) 57(255) 58(0)
	// 18(1) 19(0) end(6h) 50(0) 51(2) 52(1) 53(6) 57(255) 58(0)
	// 18(1) 19(0) end(6h) 50(0) 51(2) 52(1) 53(6) 57(255) 58(0)
	// 18(1) 19(0) end(6h) 50(0) 51(2) 52(1) 53(6) 57(255) 58(0)
	// 18(1) 19(0) end(6h) 50(0) 51(2) 52(1) 53(6) 57(255) 58(0)
	// 18(1) 19(0) end(6h) 50(0) 51(2) 52(1) 53(6) 57(255) 58(0)
	// 18(1) 19(0) end(6h) 50(0) 51(2) 52(1) 53(6) 57(255) 58(0)
	// 18(1) 19(0) end(6h) 50(0) 51(2) 52(1) 53(6) 57(255) 58(0)
	// 18(1) 19(6) end(12h) 50(2) 51(2) 52(1) 53(6) 57(255) 58(0)
	// 18(1) 19(6) end(12h) 50(3) 51(2) 52(1) 53(6) 57(255) 58(0)
	// 18(1) 19(6) end(12h) 50(1) 51(2) 52(1) 53(6) 57(255) 58(0)
	// 18(1) 19(6) end(12h) 50(0) 51(2) 52(1) 53(6) 57(255) 58(0)
	// 18(1) 19(6) end(12h) 50(0) 51(2) 52(1) 53(6) 57(255) 58(0)
	// 18(1) 19(6) end(12h) 50(0) 51(2) 52(1) 53(6) 57(255) 58(0)
	// 18(1) 19(6) end(12h) 50(0) 51(2) 52(1) 53(6) 57(255) 58(0)
	// 18(1) 19(6) end(12h) 50(0) 51(2) 52(1) 53(6) 57(255) 58(0)
	// 18(1) 19(6) end(12h) 50(0) 51(2) 52(1) 53(6) 57(255) 58(0)
	// 18(1) 19(6) end(12h) 50(0) 51(2) 52(1) 53(6) 57(255) 58(0)
	// 18(1) 19(6) end(12h) 50(0) 51(2) 52(1) 53(6) 57(255) 58(0)
	// 18(1) 19(6) end(12h) 50(0) 51(2) 52(1) 53(6) 57(255) 58(0)
	// 18(1) 19(6) end(12h) 50(0) 51(2) 52(1) 53(6) 57(255) 58(0)
	// 18(1) 19(6) end(12h) 50(0) 51(2) 52(1) 53(6) 57(255) 58(0)
	// 18(1) 19(6) end(12h) 50(0) 51(2) 52(1) 53(6) 57(255) 58(0)
}
