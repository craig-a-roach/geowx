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
class VmRelational extends VmStackInstruction {
	@Override
	int exec(EsExecutionContext ecx, OperandStack operandStack, int pc)
			throws InterruptedException {
		final IEsOperand rhs = value(operandStack.pop());
		final IEsOperand lhs = value(operandStack.pop());
		boolean bresult = false;
		switch (m_operator) {
			case LT: {
				final Boolean isLT = ecx.isLessThan(lhs, rhs);
				bresult = isLT != null && isLT.booleanValue();
			}
			break;
			case LEQ: {
				final Boolean isGT = ecx.isLessThan(rhs, lhs);
				bresult = !(isGT == null || isGT.booleanValue());
			}
			break;
			case GT: {
				final Boolean isGT = ecx.isLessThan(rhs, lhs);
				bresult = isGT != null && isGT.booleanValue();
			}
			break;
			case GEQ: {
				final Boolean isLT = ecx.isLessThan(lhs, rhs);
				bresult = !(isLT == null || isLT.booleanValue());
			}
			break;
		}
		final EsPrimitiveBoolean result = EsPrimitiveBoolean.instance(bresult);
		operandStack.push(result);
		return pcNoJump(pc);
	}

	@Override
	public String show(int depth) {
		return "Relational " + m_operator;
	}

	public static VmRelational newGEQ() {
		return new VmRelational(Operator.GEQ);
	}

	public static VmRelational newGT() {
		return new VmRelational(Operator.GT);
	}

	public static VmRelational newLEQ() {
		return new VmRelational(Operator.LEQ);
	}

	public static VmRelational newLT() {
		return new VmRelational(Operator.LT);
	}

	private VmRelational(Operator operator) {
		m_operator = operator;
	}

	private final Operator m_operator;

	private static enum Operator {
		LT, LEQ, GT, GEQ;
	}
}
