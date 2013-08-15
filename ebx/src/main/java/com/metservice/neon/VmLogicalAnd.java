/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

/**
 * @see ECMA 11.11
 * @author roach
 */
class VmLogicalAnd extends VmStackInstruction {
	@Override
	int exec(EsExecutionContext ecx, OperandStack operandStack, int pc) {
		final IEsOperand conditionOperand = value(operandStack.pop());
		final boolean conditionResult = conditionOperand.toCanonicalBoolean();
		if (conditionResult) return pcNoJump(pc);
		operandStack.push(conditionOperand);
		return pcJump(pc);
	}

	@Override
	public String show(int depth) {
		return "LogicalAND " + qJumpAddress();
	}

	public VmLogicalAnd() {
	}
}
