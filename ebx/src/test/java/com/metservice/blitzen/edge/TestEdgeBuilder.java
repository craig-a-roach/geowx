/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.edge;

import java.util.List;

import org.junit.Test;

/**
 * @author roach
 */
public class TestEdgeBuilder {

	@Test
	public void t50() {
		//@formatter:off
		final Bearing[] bearings = {
				Bearing.N,
				Bearing.NE, Bearing.E, Bearing.E,
				Bearing.NE, Bearing.E, Bearing.E,
				Bearing.NE, Bearing.NE,
				Bearing.N, Bearing.E,
				Bearing.N, Bearing.E,
				Bearing.S, Bearing.W, Bearing.SW, Bearing.SW,
				Bearing.S, Bearing.W, Bearing.SW, Bearing.SW,
				Bearing.S, Bearing.W, Bearing.SW, Bearing.SW,
				Bearing.NW};
		//@formatter:on

		final EdgeBuilder eb = new EdgeBuilder(new Vertex(10, 20), bearings[0], 3, true);
		for (int i = 1; i < bearings.length; i++) {
			eb.add(bearings[i]);
		}
		System.out.println(eb);
		// 0| N*1
		// 1|NE*1
		// 2|E*2
		// 3|NE*1
		// 4|E*2
		// 5|NE*2
		// 6|N*1
		// 7|E*1
		// 8|N*1
		// 9|E*1
		// 10|SE*1
		// 11|S*2
		// 12|SW*1
		// 13|SE*1
		// 14|S*2
		// 15|SW*1
		// 16|SE*1
		// 17|S*2
		// 18|SW*1
		// 19|W*1|
		final List<Vertex> vertices = eb.newVertices();
		System.out.println(vertices);
	}
}
