/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
abstract class SectionReader {

	protected SectionReader(byte[] section) {
		assert section != null;
		m_section = section;
	}

	protected SectionReader(SectionReader base) {
		assert base != null;
		m_section = base.m_section;
	}
	protected final byte[] m_section;
}
