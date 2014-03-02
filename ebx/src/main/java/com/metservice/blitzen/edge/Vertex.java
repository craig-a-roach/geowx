/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.edge;

/**
 * @author roach
 */
class Vertex implements IPolyline {

	@Override
	public String toString() {
		return "(" + x + "," + y + ")";
	}

	public Vertex(int x, int y) {
		this.x = x;
		this.y = y;
	}
	public final int x;
	public final int y;
}
