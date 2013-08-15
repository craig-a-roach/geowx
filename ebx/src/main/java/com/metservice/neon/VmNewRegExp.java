/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.regex.Pattern;

/**
 * 
 * @author roach
 */
class VmNewRegExp extends VmStackInstruction {

	@Override
	int exec(EsExecutionContext ecx, OperandStack operandStack, int pc) {
		operandStack.push(ecx.global().newIntrinsicRegExp(m_pattern));
		return pcNoJump(pc);
	}

	@Override
	public String show(int depth) {
		return "NewRegExp /" + m_pattern.pattern() + "/";
	}

	public VmNewRegExp(Pattern pattern) {
		if (pattern == null) throw new IllegalArgumentException("object is null");
		m_pattern = pattern;
	}

	private final Pattern m_pattern;
}
