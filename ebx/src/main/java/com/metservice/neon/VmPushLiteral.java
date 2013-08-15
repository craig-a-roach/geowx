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
class VmPushLiteral extends VmStackInstruction {
	@Override
	int exec(EsExecutionContext ecx, OperandStack operandStack, int pc) {
		operandStack.push(m_operand);
		return pcNoJump(pc);
	}

	@Override
	public String show(int depth) {
		return "PushLiteral (" + m_operand.show(1) + ")";
	}

	public VmPushLiteral(IEsOperand operand) {
		if (operand == null) throw new IllegalArgumentException("operand is null");
		m_operand = operand;
	}

	private final IEsOperand m_operand;
}
