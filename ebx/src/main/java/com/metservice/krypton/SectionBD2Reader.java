/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
class SectionBD2Reader extends Section2Reader implements IOctetIndexer, IOctetArray {

	@Override
	public int firstDataOctetIndex() {
		return 6;
	}

	@Override
	public int octetValue(int octetPos) {
		return intu1(octetPos);
	}

	@Override
	public byte[] payloadOctets() {
		return m_section;
	}

	public SectionBD2Reader(byte[] section) {
		super(section);
	}

	public SectionBD2Reader(SectionBD2Reader base) {
		super(base);
	}
}
