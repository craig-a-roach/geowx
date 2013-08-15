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
class CompilationContext {
	public boolean add(VmInstruction instruction) {
		if (instruction == null) throw new IllegalArgumentException("instruction is null");
		instruction.setLineIndex(m_lineIndex);
		return m_target.add(instruction);
	}

	public void advanceLineIndex() {
		m_lineIndex++;
	}

	public boolean declareVariable(String qccIdentifier) {
		return m_target.declareVariable(qccIdentifier);
	}

	public String here() {
		return m_source.lineHere(m_lineIndex);
	}

	public String here(Token token) {
		return m_source.lineHere(token.lineIndex);
	}

	public int lineIndex() {
		return m_lineIndex;
	}

	public InstructionAddress nextAddress() {
		return m_target.nextAddress();
	}

	public void setLineIndex(int lineIndex) {
		m_lineIndex = lineIndex;
	}

	public EsSource source() {
		return m_source;
	}

	public CompilationContext(EsSource source, SourceCallable target) {
		if (source == null) throw new IllegalArgumentException("source is null");
		if (target == null) throw new IllegalArgumentException("target is null");
		m_source = source;
		m_target = target;
	}
	private final EsSource m_source;

	private final SourceCallable m_target;
	private int m_lineIndex;
}
