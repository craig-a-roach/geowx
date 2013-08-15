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
class VmPushCallable extends VmStackInstruction {
	/**
	 * @see ECMA 13
	 */
	@Override
	int exec(EsExecutionContext ecx, OperandStack operandStack, int pc) {
		final EsFunction function = ecx.newFunction(m_callableCode);
		function.enableConstruction(ecx);
		operandStack.push(function);
		return pcNoJump(pc);
	}

	@Override
	public String show(int depth) {
		final StringBuilder sb = new StringBuilder();
		sb.append("PushCallable");
		if (depth > 0) {
			sb.append("\n");
			sb.append(m_callableCode.show(depth - 1));
		} else {
			final String oqccName = m_callableCode.oqccName();
			sb.append("(");
			sb.append(oqccName == null ? "Anonymous" : oqccName);
			sb.append(")");
		}
		return sb.toString();
	}

	public VmPushCallable(SourceCallable callableCode) {
		if (callableCode == null) throw new IllegalArgumentException("callableCode is null");
		m_callableCode = callableCode;
	}

	private final SourceCallable m_callableCode;
}
