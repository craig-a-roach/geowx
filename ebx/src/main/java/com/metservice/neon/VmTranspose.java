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
class VmTranspose extends VmStackInstruction {
	@Override
	int exec(EsExecutionContext ecx, OperandStack operandStack, int pc) {
		if (operandStack.depth() >= 2) {
			final IEsOperand top = operandStack.pop();
			final IEsOperand next = operandStack.pop();
			operandStack.push(top);
			operandStack.push(next);
		}
		return pcNoJump(pc);
	}

	@Override
	public String show(int depth) {
		return "Transpose";
	}

	public VmTranspose() {
	}
}
