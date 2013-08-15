/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.esri;

/**
 * @author roach
 */
class UProj {

	public static double niceNumber(double x, boolean round) {
		final int expv = (int) Math.floor(Math.log(x) / Math.log(10));
		final double f = x / Math.pow(10.0, expv);
		final double nf;
		if (round) {
			if (f < 1.5) {
				nf = 1.0;
			} else if (f < 3.0) {
				nf = 2.0;
			} else if (f < 7.0) {
				nf = 5.0;
			} else {
				nf = 10.0;
			}
		} else if (f <= 1.0) {
			nf = 1.0;
		} else if (f <= 2.0) {
			nf = 2.0;
		} else if (f <= 5.0) {
			nf = 5.0;
		} else {
			nf = 10.0;
		}
		return nf * Math.pow(10.0, expv);
	}
}
