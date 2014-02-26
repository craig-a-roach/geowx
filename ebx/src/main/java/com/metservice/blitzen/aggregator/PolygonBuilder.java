/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.aggregator;

/**
 * @author roach
 */
class PolygonBuilder {

	public PolygonBuilder(Strike[] strikes) {
		this.strikes = strikes;
		m_visits = new boolean[strikes.length];
	}
	public final Strike[] strikes;
	private final boolean[] m_visits;
}
