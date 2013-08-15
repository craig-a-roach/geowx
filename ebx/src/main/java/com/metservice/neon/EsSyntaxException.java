/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

/**
 * 
 * @author roach
 */
public class EsSyntaxException extends Exception {

	public EsSyntaxException(String problem) {
		super(problem);
		this.problem = problem;
		this.lineIndex = 0;
	}

	public EsSyntaxException(String problem, CompilationContext cc) {
		this(problem, cc.here(), cc.lineIndex());
	}

	public EsSyntaxException(String problem, String here, int lineIndex) {
		super(problem + "\n" + here);
		this.problem = problem;
		this.lineIndex = lineIndex;
	}

	public EsSyntaxException(String problem, TokenReader tr) {
		this(problem, tr.here(), tr.lineIndex());
	}

	public final String problem;
	public final int lineIndex;
}
