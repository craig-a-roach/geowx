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
class VmRemoveLabel extends VmLabel {

	public String qccName() {
		return m_qccName;
	}

	@Override
	public String show(int depth) {
		return "RemoveLabel (" + m_qccName + ")";
	}

	public static VmRemoveLabel newBreak() {
		return new VmRemoveLabel(LABEL_BREAK);
	}

	public static VmRemoveLabel newContinue() {
		return new VmRemoveLabel(LABEL_CONTINUE);
	}

	public VmRemoveLabel(String qccName) {
		m_qccName = qccName;
	}

	private final String m_qccName;
}
