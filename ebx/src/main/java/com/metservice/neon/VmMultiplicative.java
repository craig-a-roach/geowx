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
class VmMultiplicative extends VmStackInstruction {
	@Override
	int exec(EsExecutionContext ecx, OperandStack operandStack, int pc)
			throws InterruptedException {
		final EsPrimitiveNumber rhs = value(operandStack.pop()).toNumber(ecx);
		final EsPrimitiveNumber lhs = value(operandStack.pop()).toNumber(ecx);
		final EsPrimitiveNumber result = EsPrimitiveNumber.operate(lhs, m_operator, rhs);
		operandStack.push(result);
		return pcNoJump(pc);
	}

	@Override
	public String show(int depth) {
		return "Multiplicative " + m_operator;
	}

	public static VmMultiplicative newDivide() {
		return new VmMultiplicative(EsPrimitiveNumber.BinaryOp.DIV);
	}

	public static VmMultiplicative newMultiply() {
		return new VmMultiplicative(EsPrimitiveNumber.BinaryOp.MUL);
	}

	public static VmMultiplicative newRemainder() {
		return new VmMultiplicative(EsPrimitiveNumber.BinaryOp.REM);
	}

	private VmMultiplicative(EsPrimitiveNumber.BinaryOp operator) {
		m_operator = operator;
	}

	private final EsPrimitiveNumber.BinaryOp m_operator;
}
