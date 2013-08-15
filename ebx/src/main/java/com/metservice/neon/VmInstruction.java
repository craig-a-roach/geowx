/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.text.ArgonNumber;

/**
 * 
 * @author roach
 */
abstract class VmInstruction {

	protected static EsFunction asFunction(EsObject nativeObject) {
		if (nativeObject instanceof EsFunction) return (EsFunction) nativeObject;

		throw new EsTypeCodeException("Cannot convert native object '" + nativeObject.show(1) + "' to a Function");
	}

	protected static EsList asList(IEsOperand operand) {
		if (operand instanceof EsList) return (EsList) operand;
		throw new EsInterpreterException("Cannot convert " + operand.getClass().getName() + " (" + operand.show(1)
				+ ") to a List");
	}

	protected static EsObject asNativeObject(IEsOperand operand)
			throws EsTypeCodeException {
		if (operand instanceof EsObject) return (EsObject) operand;
		throw new EsTypeCodeException("Cannot convert '" + operand.show(1) + "' to a Native Object");
	}

	protected static IEsOperand definedValue(IEsOperand operand) {
		return value(operand, true);
	}

	protected static IEsOperand value(IEsOperand operand) {
		return value(operand, false);
	}

	protected static IEsOperand value(IEsOperand operand, boolean expectDefined) {
		assert operand != null;
		if (operand instanceof EsReference) {
			final EsReference reference = (EsReference) operand;
			final EsObject oBase = reference.getBase();
			final String zccPropertyKey = reference.zccPropertyKey();
			if (oBase == null) {
				if (expectDefined) throw new EsReferenceCodeException("Property '" + zccPropertyKey + "' not found");
				return EsPrimitiveUndefined.Instance;
			}
			final IEsOperand valueOperand = oBase.esGet(zccPropertyKey);
			if (expectDefined && valueOperand == EsPrimitiveUndefined.Instance)
				throw new EsTypeCodeException("The value of '" + reference + "' is undefined");
			return valueOperand;
		}
		return operand;
	}

	public InstructionAddress getJump() {
		return m_oJump;
	}

	public final int lineIndex() {
		return m_lineIndex;
	}

	public int pc(boolean jump, int pc) {
		return (jump && m_oJump != null) ? m_oJump.pc() : (pc + 1);
	}

	public int pcJump(int pc) {
		return (m_oJump != null) ? m_oJump.pc() : (pc + 1);
	}

	public int pcNoJump(int pc) {
		return pc + 1;
	}

	public String qJumpAddress() {
		return m_oJump == null ? "Next" : "[" + m_oJump + "]";
	}

	public String qLineNumber() {
		return ArgonNumber.intToDec3(m_lineIndex + 1);
	}

	public void setJumpAddress(InstructionAddress jump) {
		if (jump == null) throw new IllegalArgumentException("jump is null");
		m_oJump = jump;
	}

	public void setLineIndex(int lineIndex) {
		m_lineIndex = lineIndex;
	}

	public abstract String show(int depth);

	public boolean stepHere() {
		return false;
	}

	@Override
	public String toString() {
		return (stepHere() ? qLineNumber() : "---") + " " + show(0);
	}

	protected VmInstruction() {
	}

	private int m_lineIndex;
	private InstructionAddress m_oJump;
}
