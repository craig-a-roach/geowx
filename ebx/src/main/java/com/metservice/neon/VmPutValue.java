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
class VmPutValue extends VmStackInstruction {

	/**
	 * @see ECMA 8.7.2
	 */
	@Override
	int exec(EsExecutionContext ecx, OperandStack operandStack, int pc)
			throws InterruptedException {
		final IEsOperand propertyValue = value(operandStack.pop());
		final IEsOperand target = operandStack.pop();
		if (target instanceof EsReference) {
			final EsReference reference = (EsReference) target;
			EsObject oBase = reference.getBase();
			if (oBase == null) {
				oBase = ecx.global();
			}
			final String zccPropertyKey = reference.zccPropertyKey();
			oBase.esPut(zccPropertyKey, propertyValue);
		} else
			throw new EsReferenceCodeException("No Left-Side Target for Put");
		if (m_pushReference) {
			operandStack.push(target);
		}
		if (m_pushValue) {
			operandStack.push(propertyValue);
		}
		return pcNoJump(pc);
	}

	@Override
	public String show(int depth) {
		return "PutValue" + (m_pushReference ? "+PushReference" : "") + (m_pushValue ? "+PushValue" : "");
	}

	public static VmPutValue newDiscardOperands() {
		return new VmPutValue(false, false);
	}

	public static VmPutValue newKeepReference() {
		return new VmPutValue(true, false);
	}

	public static VmPutValue newKeepValue() {
		return new VmPutValue(false, true);
	}

	private VmPutValue(boolean pushReference, boolean pushValue) {
		m_pushReference = pushReference;
		m_pushValue = pushValue;
	}

	private final boolean m_pushReference;
	private final boolean m_pushValue;
}
