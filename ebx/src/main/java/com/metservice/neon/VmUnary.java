/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

/**
 * @see ECMA 11.4
 * @author roach
 */
class VmUnary extends VmStackInstruction {

	private static final String OP_TONUMBER = "TONUMBER";
	private static final String OP_NEGATE = "NEGATE";
	private static final String OP_NOT = "NOT";
	private static final String OP_DELETE = "DELETE";
	private static final String OP_VOID = "VOID";
	private static final String OP_ISDEFINED = "ISDEFINED";
	private static final String OP_TYPEOF = "TYPEOF";
	private static final String OP_SUBTYPEOF = "SUBTYPEOF";

	public static VmUnary newDelete() {
		return new VmUnary(OP_DELETE);
	}

	public static VmUnary newIsDefined() {
		return new VmUnary(OP_ISDEFINED);
	}

	public static VmUnary newLogicalNot() {
		return new VmUnary(OP_NOT);
	}

	public static VmUnary newNegate() {
		return new VmUnary(OP_NEGATE);
	}

	public static VmUnary newSubTypeOf() {
		return new VmUnary(OP_SUBTYPEOF);
	}

	public static VmUnary newToNumber() {
		return new VmUnary(OP_TONUMBER);
	}

	public static VmUnary newTypeOf() {
		return new VmUnary(OP_TYPEOF);
	}

	public static VmUnary newVoid() {
		return new VmUnary(OP_VOID);
	}

	private IEsOperand delete(EsExecutionContext ecx, OperandStack operandStack) {
		final IEsOperand target = operandStack.pop();
		final boolean result;
		if (target instanceof EsReference) {
			final EsReference reference = (EsReference) target;
			EsObject oBase = reference.getBase();
			if (oBase == null) {
				oBase = ecx.global();
			}
			final String zccPropertyKey = reference.zccPropertyKey();
			result = oBase.esDelete(zccPropertyKey);
		} else {
			result = true;
		}
		return EsPrimitiveBoolean.instance(result);
	}

	private boolean hasType(IEsOperand target) {
		if (target instanceof EsReference) {
			final EsReference reference = (EsReference) target;
			return reference.getBase() != null;
		}
		return true;
	}

	private IEsOperand isDefined(EsExecutionContext ecx, OperandStack operandStack) {
		final IEsOperand target = operandStack.pop();
		final boolean hasType = hasType(target);
		final boolean isDefined = hasType && value(target).esType().isDefined;
		return EsPrimitiveBoolean.instance(isDefined);
	}

	private IEsOperand negate(EsExecutionContext ecx, OperandStack operandStack)
			throws InterruptedException {
		return value(operandStack.pop()).toNumber(ecx).negated();
	}

	private IEsOperand not(EsExecutionContext ecx, OperandStack operandStack) {
		final boolean value = value(operandStack.pop()).toCanonicalBoolean();
		return EsPrimitiveBoolean.instance(!value);
	}

	private IEsOperand subTypeOf(EsExecutionContext ecx, OperandStack operandStack) {
		final IEsOperand target = operandStack.pop();
		final boolean hasType = hasType(target);
		String subTypeName = "undefined";
		if (hasType) {
			final IEsOperand value = value(target);
			subTypeName = UNeon.subTypeName(value);
		}
		return new EsPrimitiveString(subTypeName);
	}

	private IEsOperand toNumber(EsExecutionContext ecx, OperandStack operandStack)
			throws InterruptedException {
		return value(operandStack.pop()).toNumber(ecx);
	}

	private IEsOperand typeOf(EsExecutionContext ecx, OperandStack operandStack) {
		final IEsOperand target = operandStack.pop();
		final boolean hasType = hasType(target);
		String typeName = "undefined";
		if (hasType) {
			final IEsOperand value = value(target);
			final EsType majorType = value.esType();
			switch (majorType) {
				case TNull: {
					typeName = "object";
				}
				break;
				case TBoolean: {
					typeName = "boolean";
				}
				break;
				case TNumber: {
					typeName = "number";
				}
				break;
				case TString: {
					typeName = "string";
				}
				break;
				case TObject: {
					if (value instanceof EsFunction) {
						typeName = "function";
					} else {
						typeName = "object";
					}
				}
				break;
				default:
			}
		}
		return new EsPrimitiveString(typeName);
	}

	private IEsOperand voidEval(EsExecutionContext ecx, OperandStack operandStack) {
		value(operandStack.pop());
		return EsPrimitiveUndefined.Instance;
	}

	@Override
	int exec(EsExecutionContext ecx, OperandStack operandStack, int pc)
			throws InterruptedException {
		final IEsOperand result;
		if (m_operator == OP_TONUMBER) {
			result = toNumber(ecx, operandStack);
		} else if (m_operator == OP_NEGATE) {
			result = negate(ecx, operandStack);
		} else if (m_operator == OP_NOT) {
			result = not(ecx, operandStack);
		} else if (m_operator == OP_ISDEFINED) {
			result = isDefined(ecx, operandStack);
		} else if (m_operator == OP_SUBTYPEOF) {
			result = subTypeOf(ecx, operandStack);
		} else if (m_operator == OP_TYPEOF) {
			result = typeOf(ecx, operandStack);
		} else if (m_operator == OP_DELETE) {
			result = delete(ecx, operandStack);
		} else if (m_operator == OP_VOID) {
			result = voidEval(ecx, operandStack);
		} else {
			final String m = "Operator " + m_operator + " not implemented";
			throw new EsInterpreterException(m);
		}
		operandStack.push(result);
		return pcNoJump(pc);
	}

	@Override
	public String show(int depth) {
		return "Unary " + m_operator;
	}

	private VmUnary(String operator) {
		m_operator = operator;
	}

	private final String m_operator;
}
