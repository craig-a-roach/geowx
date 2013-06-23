/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

/**
 * @author roach
 */
enum ParameterDefinition {

	Central_Meridian(UnitType.Angle, "Central_Meridian", "Longitude_Of_Center"),
	False_Easting(UnitType.Length, "False_Easting"),
	False_Northing(UnitType.Length, "False_Northing"),
	Latitude_Of_Origin(UnitType.Angle, "Latitude_Of_Origin"),
	Scale_Factor(UnitType.Ratio, "Scale_Factor");

	@Override
	public String toString() {
		return name() + "(" + type + ")";
	}

	private ParameterDefinition(UnitType type, String... xptTitles) {
		if (type == null) throw new IllegalArgumentException("object is null");
		if (xptTitles == null) throw new IllegalArgumentException("object is null");
		this.type = type;
		final int titleCount = xptTitles.length;
		if (titleCount == 0) throw new IllegalArgumentException("require at least one title");
		this.title = Title.newInstance(xptTitles[0]);
		final Title[] zptAlt = new Title[titleCount - 1];
		for (int r = 1, w = 0; r < titleCount; r++, w++) {
			zptAlt[w] = Title.newInstance(xptTitles[r]);
		}
		this.zptAltTitles = zptAlt;
	}
	public final Title title;
	public final UnitType type;
	public final Title[] zptAltTitles;
}
