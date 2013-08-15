/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

/**
 * Operands are SP-1 and SP, result becomes SP
 * 
 * @author roach
 */
class VmAdditive extends VmStackInstruction {
	/**
	 * See ECMA 11.6.1
	 * 
	 * @throws InterruptedException
	 */
	@Override
	int exec(EsExecutionContext ecx, OperandStack operandStack, int pc)
			throws InterruptedException {
		final IEsOperand rhsOperand = value(operandStack.pop());
		final IEsOperand lhsOperand = value(operandStack.pop());
		final EsPrimitive rhsPrimitive = rhsOperand.toPrimitive(ecx, null);
		final EsPrimitive lhsPrimitive = lhsOperand.toPrimitive(ecx, null);

		final boolean isRhsString = (rhsPrimitive instanceof EsPrimitiveString);
		final boolean isLhsString = (lhsPrimitive instanceof EsPrimitiveString);

		final IEsOperand result;
		if (m_operator == EsPrimitiveNumber.BinaryOp.ADD && (isRhsString || isLhsString)) {
			final String rhsS = rhsPrimitive.toCanonicalString(ecx);
			final String lhsS = lhsPrimitive.toCanonicalString(ecx);
			result = new EsPrimitiveString(lhsS + rhsS);
		} else {
			final EsPrimitiveNumber rhs = rhsPrimitive.toNumber(ecx);
			final EsPrimitiveNumber lhs = lhsPrimitive.toNumber(ecx);
			result = EsPrimitiveNumber.operate(lhs, m_operator, rhs);
		}

		operandStack.push(result);
		return pcNoJump(pc);
	}

	@Override
	public String show(int depth) {
		return "Additive " + m_operator;
	}

	public static VmAdditive newMinus() {
		return new VmAdditive(EsPrimitiveNumber.BinaryOp.SUB);
	}

	public static VmAdditive newPlus() {
		return new VmAdditive(EsPrimitiveNumber.BinaryOp.ADD);
	}

	private VmAdditive(EsPrimitiveNumber.BinaryOp operator) {
		m_operator = operator;
	}

	private final EsPrimitiveNumber.BinaryOp m_operator;
}
