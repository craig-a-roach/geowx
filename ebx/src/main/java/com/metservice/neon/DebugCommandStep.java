/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

/**
 * @author roach
 */
class DebugCommandStep extends DebugCommand {

	@Override
	public String toString() {
		return sense.toString();
	}

	public DebugCommandStep(EsBreakpointLines breakpointLines, Sense sense) {
		super(breakpointLines);
		assert sense != null;
		this.sense = sense;
	}

	public final Sense sense;

	public static enum Sense {
		Next, Over, Out, Completion, Continue
	}

}
