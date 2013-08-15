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
class VmDeclareFunction extends VmInstruction {
	public SourceCallable callableCode() {
		return m_callableCode;
	}

	public String qccFunctionName() {
		return m_qccFunctionName;
	}

	@Override
	public String show(int depth) {
		final StringBuilder sb = new StringBuilder();
		sb.append("DeclareFunction ");
		sb.append(m_qccFunctionName);
		if (depth > 0) {
			sb.append("{\n");
			sb.append(m_callableCode.show(depth - 1));
			sb.append("}");
		}
		return sb.toString();
	}

	public VmDeclareFunction(String qccFunctionName, SourceCallable callableCode) {
		if (qccFunctionName == null || qccFunctionName.length() == 0)
			throw new IllegalArgumentException("qccFunctionName is empty");
		if (callableCode == null) throw new IllegalArgumentException("callableCode is null");
		m_qccFunctionName = qccFunctionName;
		m_callableCode = callableCode;
	}
	private final String m_qccFunctionName;

	private final SourceCallable m_callableCode;
}
