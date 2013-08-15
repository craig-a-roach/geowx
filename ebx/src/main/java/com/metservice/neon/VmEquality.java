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
class VmEquality extends VmStackInstruction {
	@Override
	int exec(EsExecutionContext ecx, OperandStack operandStack, int pc)
			throws InterruptedException {
		final IEsOperand rhs = value(operandStack.pop());
		final IEsOperand lhs = value(operandStack.pop());
		final boolean isEqual = ecx.isEqual(lhs, rhs, m_strict);
		final boolean bresult = m_negation ? !isEqual : isEqual;
		final EsPrimitiveBoolean result = EsPrimitiveBoolean.instance(bresult);
		operandStack.push(result);
		return pcNoJump(pc);
	}

	@Override
	public String show(int depth) {
		return "Equality" + (m_negation ? " NOT" : "") + (m_strict ? " (STRICT)" : "");
	}

	public static VmEquality newNegative() {
		return new VmEquality(true, false);
	}

	public static VmEquality newNegativeStrict() {
		return new VmEquality(true, true);
	}

	public static VmEquality newPositive() {
		return new VmEquality(false, false);
	}

	public static VmEquality newPositiveStrict() {
		return new VmEquality(false, true);
	}

	private VmEquality(boolean negation, boolean strict) {
		m_negation = negation;
		m_strict = strict;
	}

	private final boolean m_negation;
	private final boolean m_strict;
}
