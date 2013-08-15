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
class VmGetValue extends VmStackInstruction {
	/**
	 * @see ECMA 8.7.1
	 */
	@Override
	int exec(EsExecutionContext ecx, OperandStack operandStack, int pc) {
		final IEsOperand operand = operandStack.pop();
		if (m_keepReference) {
			operandStack.push(operand);
		}
		operandStack.push(value(operand));
		return pcNoJump(pc);
	}

	@Override
	public String show(int depth) {
		return "GetValue" + (m_keepReference ? "+KeepReference" : "");
	}

	public VmGetValue(boolean keepReference) {
		m_keepReference = keepReference;
	}

	private final boolean m_keepReference;
}
