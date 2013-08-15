/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.esri;

/**
 * @author roach
 */
public interface IProjectionFactory {

	public IGalliumProjection newProjection(ParameterMap pmap, GeographicCoordinateSystem gcs, Unit lu)
			throws GalliumProjectionException;

	public void setAuthority(Authority a);

	public void setTitle(Title t);

	public void setZone(Zone zone);
}
