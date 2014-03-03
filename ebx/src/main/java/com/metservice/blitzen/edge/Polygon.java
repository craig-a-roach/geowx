/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.edge;

import java.util.List;

/**
 * @author roach
 */
class Polygon implements IPolyline {

	@Override
	public String toString() {
		return m_vertices.toString();
	}

	public Polygon(List<Vertex> vertices) {
		m_vertices = vertices;
	}
	private final List<Vertex> m_vertices;
}
