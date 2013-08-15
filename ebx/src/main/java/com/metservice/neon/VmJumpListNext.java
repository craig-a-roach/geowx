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
class VmJumpListNext extends VmLoopInstruction {
	@Override
	int exec(EsExecutionContext ecx, OperandStack operandStack, ListStack listStack, int pc) {
		final EsList zlPropertyNames = listStack.pop();
		final IEsOperand oHeadPropertyName = zlPropertyNames.popHead();
		if (oHeadPropertyName == null) return pcJump(pc);
		listStack.push(zlPropertyNames);
		operandStack.push(oHeadPropertyName);
		return pcNoJump(pc);
	}

	@Override
	public String show(int depth) {
		return "JumpListNext " + qJumpAddress();
	}

	public VmJumpListNext() {
	}
}
