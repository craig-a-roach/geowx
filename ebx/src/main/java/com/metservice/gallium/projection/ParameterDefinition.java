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

	Central_Meridian("Central_Meridian", UnitType.Angle),
	False_Easting("False_Easting", UnitType.Length),
	False_Northing("False_Northing", UnitType.Length),
	Latitude_Of_Origin("Latitude_Of_Origin", UnitType.Angle),
	Scale_Factor("Scale_Factor", UnitType.Ratio);

	@Override
	public String toString() {
		return name() + "(" + type + ")";
	}

	private ParameterDefinition(String title, UnitType type) {
		if (type == null) throw new IllegalArgumentException("object is null");
		this.title = Title.newInstance(title);
		this.type = type;
	}
	public final Title title;
	public final UnitType type;
}
