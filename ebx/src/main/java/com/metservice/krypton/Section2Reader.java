/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
abstract class Section2Reader extends SectionReader {

	public final float float4(int octetStart) {
		return UGrib.float4OctetG2(m_section, octetStart);
	}

	public final boolean hasMoreOctets(int octetPos) {
		return UGrib.hasMoreOctetsG2(m_section, octetPos);
	}

	public final int int2(int octetStart) {
		return UGrib.int2OctetG2(m_section, octetStart);
	}

	public final int int4(int octetStart) {
		return UGrib.int4OctetG2(m_section, octetStart);
	}

	public final int intu1(int octet) {
		return UGrib.intu1OctetG2(m_section, octet);
	}

	public final short shortu1(int octet) {
		return UGrib.shortu1OctetG2(m_section, octet);
	}

	protected Section2Reader(byte[] section) {
		super(section);
	}

	protected Section2Reader(Section2Reader base) {
		super(base);
	}
}
