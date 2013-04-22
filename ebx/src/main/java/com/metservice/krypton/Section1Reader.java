/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
abstract class Section1Reader extends SectionReader {

	public final double double2(int octetStart, double divider) {
		return UGrib.double2OctetG1(m_section, octetStart, divider);
	}

	public final float float4(int octetStart) {
		return UGrib.float4OctetG1(m_section, octetStart);
	}

	public final boolean hasMoreOctets(int octetPos) {
		return UGrib.hasMoreOctetsG1(m_section, octetPos);
	}

	public final boolean hasOctets(int octetStart, int octetCount) {
		return UGrib.hasOctetsG1(m_section, octetStart, octetCount);
	}

	public final int int2(int octetStart) {
		return UGrib.int2OctetG1(m_section, octetStart);
	}

	public final int int3(int octetStart) {
		return UGrib.int3OctetG1(m_section, octetStart);
	}

	public final int intu1(int octet) {
		return UGrib.intu1OctetG1(m_section, octet);
	}

	public final short shortu1(int octet) {
		return UGrib.shortu1OctetG1(m_section, octet);
	}

	public final short shortu1hi(int octet) {
		return UGrib.shortu1OctetHiG1(m_section, octet);
	}

	public final short shortu1lo(int octet) {
		return UGrib.shortu1OctetLoG1(m_section, octet);
	}

	public final short shortu2(int octetStart) {
		return UGrib.shortu2OctetG1(m_section, octetStart);
	}

	protected Section1Reader(byte[] section) {
		super(section);
	}

	protected Section1Reader(Section1Reader base) {
		super(base);
	}
}
