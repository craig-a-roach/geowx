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
class VmResolveIdentifier extends VmStackInstruction {
	/**
	 * @see ECMA 11.1.2
	 */
	@Override
	int exec(EsExecutionContext ecx, OperandStack operandStack, int pc) {
		final EsReference reference = ecx.scopeChain().resolve(m_qccIdentifier);
		operandStack.push(reference);
		return pcNoJump(pc);
	}

	@Override
	public String show(int depth) {
		return "ResolveIdentifier " + m_qccIdentifier;
	}

	public VmResolveIdentifier(String qccIdentifier) {
		if (qccIdentifier == null || qccIdentifier.length() == 0) throw new IllegalArgumentException("qccIdentifier is empty");
		m_qccIdentifier = qccIdentifier;
	}

	private final String m_qccIdentifier;
}
