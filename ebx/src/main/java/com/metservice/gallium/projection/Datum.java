/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

/**
 * @author roach
 */
class Datum {

	public static final Datum[] Datums = { new Datum("Australian Geodetic 1966", Ellipsoid.AUSTRALIAN, -133, -48, 148),
			new Datum("Australian Geodetic 984", Ellipsoid.AUSTRALIAN, -134, -48, 149),
			new Datum("European Datum 1950", Ellipsoid.INTERNATIONAL_1967, -87, -98, -121),
			new Datum("European Datum 1979", Ellipsoid.INTERNATIONAL_1967, -86, -98, -119),
			new Datum("Geodetic Datum 1949", Ellipsoid.INTERNATIONAL_1967, 84, -22, 209),
			new Datum("Hong Kong 1963", Ellipsoid.INTERNATIONAL_1967, -156, -271, -189),
			new Datum("Hu Tzu Shan", Ellipsoid.INTERNATIONAL_1967, -634, -549, -201),
			new Datum("NAD83", Ellipsoid.GRS_1980, 0, 0, 0),
			new Datum("Ordnance Survey 1936", Ellipsoid.AIRY, 375, -111, 431),
			new Datum("Pulkovo 1942", Ellipsoid.KRASOVSKY, 27, -135, -89),
			new Datum("PROVISIONAL_S_AMERICAN_1956", Ellipsoid.INTERNATIONAL_1967, -288, 175, -376),
			new Datum("Tokyo", Ellipsoid.BESSEL, -128, 481, 664), new Datum("WGS72", Ellipsoid.WGS_1972, 0, 0, -4.5),
			new Datum("WGS84", Ellipsoid.WGS_1984, 0, 0, 0) };

	public Datum(String name, Ellipsoid ellipsoid, double deltaX, double deltaY, double deltaZ) {
		this.name = name;
		this.ellipsoid = ellipsoid;
		this.deltaX = deltaX;
		this.deltaY = deltaY;
		this.deltaZ = deltaZ;
	}
	public final String name;
	public final Ellipsoid ellipsoid;
	public final double deltaX;
	public final double deltaY;
	public final double deltaZ;

}
