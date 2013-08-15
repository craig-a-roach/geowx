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
class VmPop extends VmStackInstruction {
	@Override
	int exec(EsExecutionContext ecx, OperandStack operandStack, int pc) {
		for (int i = 0; i < m_count && !operandStack.isEmpty(); i++) {
			operandStack.pop();
		}
		return pcNoJump(pc);
	}

	@Override
	public String show(int depth) {
		return "Pop *" + m_count;
	}

	public VmPop(int count) {
		m_count = count;
	}

	private final int m_count;
}
