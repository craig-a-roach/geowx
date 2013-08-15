/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.xml.W3cDom;

/**
 * 
 * @author roach
 */
class VmNewW3cDom extends VmStackInstruction {
	@Override
	int exec(EsExecutionContext ecx, OperandStack operandStack, int pc) {
		operandStack.push(ecx.global().newIntrinsicW3cDom(ecx, m_dom));
		return pcNoJump(pc);
	}

	@Override
	public String show(int depth) {
		return "NewW3cDom " + m_dom.toString();
	}

	public VmNewW3cDom(W3cDom dom) {
		if (dom == null) throw new IllegalArgumentException("object is null");
		m_dom = dom;
	}

	private final W3cDom m_dom;
}
