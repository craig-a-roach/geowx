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
class VmJumpConditional extends VmStackInstruction {
	@Override
	int exec(EsExecutionContext ecx, OperandStack operandStack, int pc) {
		final IEsOperand conditionOperand = value(operandStack.pop());
		final boolean conditionResult = conditionOperand.toCanonicalBoolean();
		final boolean jump = m_condition == conditionResult;
		return pc(jump, pc);
	}

	@Override
	public String show(int depth) {
		return "JumpConditional " + (m_condition ? "T" : "F") + qJumpAddress();
	}

	@Override
	public boolean stepHere() {
		return false;
	}

	public VmJumpConditional(boolean condition) {
		m_condition = condition;
	}

	private final boolean m_condition;
}
