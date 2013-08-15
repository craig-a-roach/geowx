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
class VmCall extends VmInstruction {
	/**
	 * @see ECMA 11.2.3
	 */
	public EsExecutionContext newExecutionContext(EsExecutionContext ecx, OperandStack operandStack) {
		final EsList argumentList = asList(operandStack.pop());
		final IEsOperand functionOperand = operandStack.pop();
		final EsFunction function = asFunction(asNativeObject(definedValue(functionOperand)));
		EsObject oFunctionBase = null;
		if (functionOperand instanceof EsReference) {
			oFunctionBase = ((EsReference) functionOperand).getBase();
		}
		final EsObject oThis;
		if (oFunctionBase instanceof EsActivation) {
			oThis = null;
		} else {
			oThis = oFunctionBase;
		}
		final EsActivation activation = EsActivation.newInstance(ecx.global(), function, argumentList);
		return ecx.newInstance(function, activation, oThis);
	}

	@Override
	public String show(int depth) {
		return "Call";
	}

	@Override
	public boolean stepHere() {
		return true;
	}

	public VmCall() {
	}
}
