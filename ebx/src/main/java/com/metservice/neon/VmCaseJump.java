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
class VmCaseJump extends VmStackInstruction {
	@Override
	int exec(EsExecutionContext ecx, OperandStack operandStack, int pc)
			throws InterruptedException {
		final IEsOperand rhs = value(operandStack.pop());
		final IEsOperand lhs = value(operandStack.pop());
		final boolean isEqual = ecx.isEqual(lhs, rhs, true);
		if (!isEqual) {
			operandStack.push(lhs);
		}
		return pc(isEqual, pc);
	}

	@Override
	public String show(int depth) {
		return "CaseJump " + qJumpAddress();
	}

	@Override
	public boolean stepHere() {
		return false;
	}

	public VmCaseJump() {
	}
}
