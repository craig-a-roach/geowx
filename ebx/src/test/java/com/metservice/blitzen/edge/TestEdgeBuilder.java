/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.edge;

/**
 * @author roach
 */
public class TestEdgeBuilder {

	public void t50() {
		//@formatter:off
		final Bearing[] bearings = {
				Bearing.NE, Bearing.E, Bearing.E,
				Bearing.NE, Bearing.E, Bearing.E,
				Bearing.NE, Bearing.NE,
				Bearing.N, Bearing.E,
				Bearing.N, Bearing.E,
				Bearing.SE, Bearing.S, Bearing.SW,
				Bearing.SE, Bearing.S, Bearing.SW,
				Bearing.SE, Bearing.S, Bearing.SW,
				Bearing.W};
		//@formatter:on
		final EdgeBuilder rb = new EdgeBuilder(new Vertex(10, 20), bearings[0]);
		for (int i = 1; i < bearings.length; i++) {
			rb.add(bearings[i]);
		}
	}

}
