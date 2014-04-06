/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
public class KryptonRecord2Builder {

	public static final int METEOROLOGICAL = 0;

	public KryptonRecord2Builder newInstance(short discipline) {
		return new KryptonRecord2Builder(discipline);
	}

	public KryptonRecord2Builder newMeteorological() {
		return new KryptonRecord2Builder(METEOROLOGICAL);
	}

	private KryptonRecord2Builder(int discipline) {
		m_discipline = discipline;
	}
	private final int m_discipline;
}
