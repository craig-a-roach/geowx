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
class VmJumpAlways extends VmStackInstruction {
	@Override
	int exec(EsExecutionContext ecx, OperandStack operandStack, int pc) {
		return pcJump(pc);
	}

	@Override
	public String show(int depth) {
		return "JumpAlways " + qJumpAddress();
	}

	@Override
	public boolean stepHere() {
		return false;
	}

	public VmJumpAlways() {
	}
}
