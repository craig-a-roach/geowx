/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
class SectionPD2Reader extends Section2Reader {

	public int b0607_coordinateValues() {
		return int2(6);
	}

	public int b0809_templateNo() {
		return int2(8);
	}

	public SectionPD2Reader(byte[] section) {
		super(section);
	}

	public SectionPD2Reader(SectionPD2Reader base) {
		super(base);
	}
}
