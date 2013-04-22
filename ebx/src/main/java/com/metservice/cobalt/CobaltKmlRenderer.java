/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.cobalt;

import com.metservice.argon.Binary;

/**
 * @author roach
 */
public class CobaltKmlRenderer {

	public Binary newXml(CobaltProviderName providerName, CobaltNCube ncube)
			throws CobaltDimensionException {
		if (providerName == null) throw new IllegalArgumentException("object is null");
		if (ncube == null) throw new IllegalArgumentException("object is null");
		final String qccProviderName = providerName.format();
		final KmlFeatureName fnProvider = new KmlFeatureName();
		fnProvider.addText("Provider: " + qccProviderName);
		final GeoModel model = GeoModel.newInstance(ncube);
		final int polyCount = model.placemarkCount();
		final KmlStyleFactory styleFactory = new KmlStyleFactory(m_style, polyCount);
		final KmlDocument doc = new KmlDocument(styleFactory);
		doc.add(fnProvider);
		doc.addOpen(true);
		model.addTo(doc);
		final String xml = doc.toXml();
		return Binary.newFromStringUTF8(xml);
	}

	public CobaltKmlRenderer(CobaltKmlStyle style) {
		if (style == null) throw new IllegalArgumentException("object is null");
		m_style = style;
	}
	private final CobaltKmlStyle m_style;
}
