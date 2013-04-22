/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
class UGeo {

	public static double angle180(double angle) {
		double d = angle;
		while (d < -180.0 || d >= 180.0) {
			d = d >= 180.0 ? d - 360.0 : d;
			d = d < -180.0 ? d + 360.0 : d;
		}
		return d;
	}

	public static double angle360(double angle) {
		double d = angle;
		while (d < 0.0 || d >= 360.0) {
			d = d >= 360.0 ? d - 360.0 : d;
			d = d < 0.0 ? d + 360.0 : d;
		}
		return d;
	}

	public static double clamp90(double angle) {
		if (angle < -90.0) return -90;
		if (angle > +90.0) return +90;
		return angle;
	}

	private UGeo() {
	}
}
