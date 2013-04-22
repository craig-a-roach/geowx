/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
class Section2StackGD {

	public SectionGD2Reader sectionGD2Reader() {
		return m_rGD;
	}

	public SectionID2Reader sectionID2Reader() {
		return m_rID;
	}

	public Section2Tag tagPD() {
		return m_tagPD;
	}

	public Section2StackGD(Section2Tag tagPD, SectionID2Reader rID, SectionGD2Reader rGD) {
		if (tagPD == null) throw new IllegalArgumentException("object is null");
		if (rID == null) throw new IllegalArgumentException("object is null");
		if (rGD == null) throw new IllegalArgumentException("object is null");
		m_tagPD = tagPD;
		m_rID = rID;
		m_rGD = rGD;
	}
	private final Section2Tag m_tagPD;
	private final SectionID2Reader m_rID;
	private final SectionGD2Reader m_rGD;
}
