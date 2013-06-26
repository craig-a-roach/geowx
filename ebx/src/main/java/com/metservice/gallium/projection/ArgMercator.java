/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

/**
 * @author roach
 */
class ArgMercator {

	public ArgMercator(ParameterMap pmapDefault, ParameterMap pmap, GeographicCoordinateSystem gcs)
			throws GalliumProjectionException {
		if (pmapDefault == null) throw new IllegalArgumentException("object is null");
		if (pmap == null) throw new IllegalArgumentException("object is null");

		pmap.find(ParameterDefinition.Scale_Factor);
	}
}
