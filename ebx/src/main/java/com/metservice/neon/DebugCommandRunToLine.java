/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

/**
 * @author roach
 */
class DebugCommandRunToLine extends DebugCommand {

	@Override
	public String toString() {
		return "Run To Line " + lineNo;
	}

	public DebugCommandRunToLine(EsBreakpointLines breakpointLines, int lineNo) {
		super(breakpointLines);
		this.lineNo = lineNo;
	}

	public final int lineNo;
}
