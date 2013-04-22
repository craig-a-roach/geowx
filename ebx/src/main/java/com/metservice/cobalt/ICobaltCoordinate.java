/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.cobalt;

import com.metservice.argon.json.JsonObject;

/**
 * @author roach
 */
public interface ICobaltCoordinate extends ICobaltProduct {

	public void addTo(KmlFeatureText kft);

	public CobaltDimensionName dimensionName();

	public void saveTo(JsonObject dst);

	public String show();
}
