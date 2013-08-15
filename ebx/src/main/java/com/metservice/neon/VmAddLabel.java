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
class VmAddLabel extends VmLabel {
	public String qccName() {
		return m_qccName;
	}

	@Override
	public String show(int depth) {
		return "AddLabel (" + m_qccName + ")" + qJumpAddress();
	}

	public static VmAddLabel newBreak() {
		return new VmAddLabel(LABEL_BREAK);
	}

	public static VmAddLabel newContinue() {
		return new VmAddLabel(LABEL_CONTINUE);
	}

	public VmAddLabel(String qccName) {
		m_qccName = qccName;
	}

	private final String m_qccName;
}
