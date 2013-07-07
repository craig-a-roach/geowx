/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

import com.metservice.gallium.GalliumPointD;

/**
 * @author roach
 */
public interface IGalliumProjection {

	public double clippingMaxLatitudeDegrees();

	public double clippingMinLatitudeDegrees();

	public boolean hasInverse();

	public GalliumPointD inverseDegrees(double xMetres, double yMetres)
			throws GalliumProjectionException;

	public boolean isConformal();

	public boolean isEqualArea();

	public boolean isInside(double lonDeg, double latDeg)
			throws GalliumProjectionException;

	public boolean isRectilinear();

	public double totalScale();

	public GalliumPointD transform(double lonDeg, double latDeg)
			throws GalliumProjectionException;
}
