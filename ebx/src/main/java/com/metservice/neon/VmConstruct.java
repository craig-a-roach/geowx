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
class VmConstruct extends VmInstruction {
	/**
	 * @see ECMA 11.2.2
	 */
	public EsExecutionContext newExecutionContext(EsExecutionContext ecx, OperandStack operandStack) {
		final EsList argumentList = asList(operandStack.pop());
		final IEsOperand functionOperand = operandStack.pop();
		final EsFunction function = asFunction(asNativeObject(definedValue(functionOperand)));

		final IEsOperand functionPrototype = function.esGet("prototype");
		final EsObject thisPrototype;
		if (functionPrototype instanceof EsObject) {
			thisPrototype = (EsObject) functionPrototype;
		} else {
			thisPrototype = ecx.global().prototypeObject;
		}

		EsObject vThisObject = thisPrototype.createObject();
		if (vThisObject == null) {
			vThisObject = new EsIntrinsicObject(thisPrototype);
		}
		final EsActivation activation = EsActivation.newInstance(ecx.global(), function, argumentList);
		return ecx.newInstance(function, activation, vThisObject);
	}

	@Override
	public String show(int depth) {
		return "Construct";
	}

	@Override
	public boolean stepHere() {
		return true;
	}

	public VmConstruct() {
	}
}
