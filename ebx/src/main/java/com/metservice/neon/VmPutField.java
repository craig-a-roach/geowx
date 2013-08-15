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
class VmPutField extends VmStackInstruction {
	@Override
	int exec(EsExecutionContext ecx, OperandStack operandStack, int pc) {
		final IEsOperand propertyValue = value(operandStack.pop());
		final IEsOperand targetOperand = operandStack.pop();
		if (targetOperand instanceof EsObject) {
			final EsObject targetObject = (EsObject) targetOperand;
			targetObject.esPut(m_qccFieldIdentifier, propertyValue);
			operandStack.push(targetObject);
		} else
			throw new EsReferenceCodeException("No Left-Side Target Object for Put");

		return pcNoJump(pc);
	}

	@Override
	public String show(int depth) {
		return "PutField (" + m_qccFieldIdentifier + ")";
	}

	public VmPutField(String qccFieldIdentifier) {
		if (qccFieldIdentifier == null || qccFieldIdentifier.length() == 0)
			throw new IllegalArgumentException("qccFieldIdentifier is empty");
		m_qccFieldIdentifier = qccFieldIdentifier;
	}

	private final String m_qccFieldIdentifier;
}
