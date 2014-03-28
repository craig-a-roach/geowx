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

	public static int gridX(BzeStrike s, BzeStrikeBounds bounds, float grid) {
		return Math.round((s.x - bounds.xL) / grid);
	}

	public static int gridY(BzeStrike s, BzeStrikeBounds bounds, float grid) {
		return Math.round((s.y - bounds.yB) / grid);
	}

	public static float strikeX(Vertex v, float xL, float grid) {
		return (grid * v.x) + xL;
	}

	public static float strikeY(Vertex v, float yB, float grid) {
		return (grid * v.y) + yB;
	}

	public float strikeX(BzeStrikeBounds bounds, float grid) {
		return strikeX(this, bounds.xL, grid);
	}

	public float strikeY(BzeStrikeBounds bounds, float grid) {
		return strikeY(this, bounds.yB, grid);
	}

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
