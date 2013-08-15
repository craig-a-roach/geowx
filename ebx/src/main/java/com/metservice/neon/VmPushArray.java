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
class VmPushArray extends VmStackInstruction {

	@Override
	int exec(EsExecutionContext ecx, OperandStack operandStack, int pc) {
		final EsList list = asList(operandStack.pop());
		final int length = list.length();
		final EsIntrinsicArray array = ecx.global().newIntrinsicArray();
		array.setLength(length);
		for (int index = 0; index < length; index++) {
			array.putByIndex(index, list.operand(index));
		}
		operandStack.push(array);
		return pcNoJump(pc);
	}

	@Override
	public String show(int depth) {
		return "PushArray";
	}

	public VmPushArray() {
	}
}
