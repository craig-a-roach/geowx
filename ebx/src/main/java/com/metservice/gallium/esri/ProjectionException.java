/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.esri;

/**
 * @author roach
 */
public class ProjectionException extends Exception {

	public static ProjectionException coordinateOutsideBounds() {
		return new ProjectionException("Coordinate outside projection bounds");
	}

	public static ProjectionException infiniteAngle() {
		return new ProjectionException("Infinite angle");
	}

	public static ProjectionException infiniteLatitude() {
		return new ProjectionException("Infinite latitude");
	}

	public static ProjectionException infiniteLongitude() {
		return new ProjectionException("Infinite longitude");
	}

	public static ProjectionException latitudeOutsideBounds() {
		return new ProjectionException("Latitude outside projection bounds");
	}

	public static ProjectionException longitudeOutsideBounds() {
		return new ProjectionException("Longitude outside projection bounds");
	}

	public static ProjectionException noConverge(int count, double delta) {
		final String m = "Failed to converge after " + count + " iterations; delta is " + delta;
		return new ProjectionException(m);
	}

	private ProjectionException(String message) {
		super(message);
	}

}
