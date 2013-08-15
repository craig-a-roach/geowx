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
class VmAccessProperty extends VmStackInstruction {

	/**
	 * @see ECMA 11.2.1
	 */
	@Override
	int exec(EsExecutionContext ecx, OperandStack operandStack, int pc)
			throws InterruptedException {
		final IEsOperand accessorOperand = definedValue(operandStack.pop());
		final IEsOperand baseOperand = definedValue(operandStack.pop());
		final EsObject baseObject = baseOperand.toObject(ecx);
		final String zccProperty = accessorOperand.toCanonicalString(ecx);
		final EsReference reference = new EsReference(baseObject, zccProperty);
		operandStack.push(reference);
		return pcNoJump(pc);
	}

	@Override
	public String show(int depth) {
		return "AccessProperty";
	}

	public VmAccessProperty() {
	}
}
