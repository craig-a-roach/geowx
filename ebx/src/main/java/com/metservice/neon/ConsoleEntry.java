/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

/**
 * @author roach
 */
class ConsoleEntry {

	public ConsoleEntry(ConsoleType type, String zLine) {
		if (type == null) throw new IllegalArgumentException("object is null");
		if (zLine == null) throw new IllegalArgumentException("object is null");
		this.type = type;
		this.zLine = zLine;
		this.ts = System.currentTimeMillis();
	}
	public final ConsoleType type;
	public final String zLine;
	public final long ts;
}
