/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.edge;

/**
 * @author roach
 */
public class TestRampBuilder {

	public void t50() {
		//@formatter:off
		final Bearing[] bearings = {
				Bearing.NE, Bearing.E, Bearing.E,
				Bearing.NE, Bearing.E, Bearing.E,
				Bearing.NE, Bearing.NE,
				Bearing.N, Bearing.E };
		//@formatter:on
		final RampBuilder rb = new RampBuilder(new Vertex(10, 20), bearings[0]);
		for (int i = 1; i < bearings.length; i++) {
			rb.add(bearings[i]);
		}
	}

}
