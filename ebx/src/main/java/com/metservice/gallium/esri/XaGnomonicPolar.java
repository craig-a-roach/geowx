/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.esri;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
class XaGnomonicPolar extends XaGnomonic {

	@Override
	public double projectionLatitudeRads() {
		return north ? MapMath.HALFPI : -MapMath.HALFPI;
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("pole", (north ? "NORTH" : "SOUTH"));
		return ds.s();
	}

	public XaGnomonicPolar(boolean north) {
		this.north = north;
	}
	public final boolean north;
}
