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
class VmSetCompletion extends VmInstruction {

	public CompletionType completionType() {
		return m_type;
	}

	@Override
	public String show(int depth) {
		return "SetCompletion " + m_type;
	}

	@Override
	public boolean stepHere() {
		return m_type != CompletionType.NORMAL;
	}

	public static VmSetCompletion newBreak() {
		return new VmSetCompletion(CompletionType.BREAK);
	}

	public static VmSetCompletion newContinue() {
		return new VmSetCompletion(CompletionType.CONTINUE);
	}

	public static VmSetCompletion newNormal() {
		return new VmSetCompletion(CompletionType.NORMAL);
	}

	public static VmSetCompletion newReturn() {
		return new VmSetCompletion(CompletionType.RETURN);
	}

	public static VmSetCompletion newThrow() {
		return new VmSetCompletion(CompletionType.THROW);
	}

	private VmSetCompletion(CompletionType type) {
		m_type = type;
	}

	private final CompletionType m_type;
}
