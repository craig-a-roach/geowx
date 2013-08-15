/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

/**
 * @author roach
 */
class DebugState {

	public String lineHere() {
		final StringBuilder sb = new StringBuilder();
		sb.append(source.lineHere(lineIndex));
		if (oRunException != null) {
			sb.append("\n");
			sb.append(oRunException.getMessage());
		}
		return sb.toString();
	}

	public DebugState(IEsCallable callable, EsExecutionContext ecx, ShellHook sh, int pc, int lineIndex, boolean stepHere,
			IEsOperand oResult, EsRunException oRunException) {
		this.callable = callable;
		this.ecx = ecx;
		this.source = sh.source;
		this.oHtml = sh.oSourceHtml;
		this.pc = pc;
		this.lineIndex = lineIndex;
		this.stepHere = stepHere;
		this.oResult = oResult;
		this.oRunException = oRunException;
	}

	public final IEsCallable callable;
	public final EsExecutionContext ecx;
	public final EsSource source;
	public final EsSourceHtml oHtml;
	public final int pc;
	public final int lineIndex;
	public final boolean stepHere;
	public final IEsOperand oResult;
	public final EsRunException oRunException;
}
