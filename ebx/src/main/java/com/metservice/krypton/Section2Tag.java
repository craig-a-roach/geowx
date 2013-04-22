/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
class Section2Tag {

	public int bcBody() {
		return UGrib.bcBodyG2(bc);
	}

	@Override
	public String toString() {
		return "Section " + no + ", " + bc + " bytes";
	}

	public Section2Tag(int bc, int no) {
		this.bc = bc;
		this.no = no;
	}
	public final int bc;
	public final int no;
}
