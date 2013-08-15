/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author roach
 */
class DebuggerSessionSource {

	private void runToLine(DebugCommandRunToLine command)
			throws InterruptedException {
		assert command != null;
		final int lineIndex = command.lineNo - 1;
		boolean satisfied = false;
		DebugState oNeo = null;
		while (!satisfied && m_controlledThreadId.get() != NOCONTROL) {
			oNeo = m_box.poll(1L, TimeUnit.SECONDS);
			if (oNeo == null) {
				continue;
			}
			if (oNeo.oRunException != null) {
				satisfied = true;
				continue;
			}
			if (!oNeo.stepHere) {
				continue;
			}
			if (command.breakpointLines.hit(oNeo.lineIndex)) {
				satisfied = true;
				continue;
			}
			satisfied = oNeo.lineIndex == lineIndex;
		}
		m_refState.set(oNeo);
	}

	private void step(DebugCommandStep command)
			throws InterruptedException {
		assert command != null;
		final DebugState oInit = m_refState.get();
		final int lineIndexInit = (oInit == null) ? 0 : oInit.lineIndex;
		final int depthInit = (oInit == null) ? 0 : oInit.ecx.depth();

		boolean satisfied = false;
		DebugState oNeo = null;
		while (!satisfied && m_controlledThreadId.get() != NOCONTROL) {
			oNeo = m_box.poll(1L, TimeUnit.SECONDS);
			if (oNeo == null) {
				continue;
			}
			if (oNeo.oRunException != null) {
				satisfied = true;
				continue;
			}
			if (!oNeo.stepHere) {
				continue;
			}
			if (oInit == null) {
				satisfied = true;
				continue;
			}

			final int lineIndexNeo = oNeo.lineIndex;
			final int depthNeo = oNeo.ecx.depth();

			if (lineIndexNeo != lineIndexInit) {
				if (command.breakpointLines.hit(lineIndexNeo)) {
					satisfied = true;
					continue;
				}
			}

			switch (command.sense) {
				case Next: {
					satisfied = lineIndexNeo != lineIndexInit || depthNeo != depthInit;
				}
				break;

				case Over: {
					satisfied = lineIndexNeo != lineIndexInit && depthNeo == depthInit;
				}
				break;

				case Out: {
					satisfied = depthNeo < depthInit;
				}
				break;

				case Completion: {
					satisfied = oNeo.oResult != null;
				}
				break;

				case Continue:
				break;

				default: {
					final String m = "unsupported command>" + command + "<";
					throw new IllegalArgumentException(m);
				}
			}
		}
		m_refState.set(oNeo);
	}

	public void apply(DebugCommand command)
			throws InterruptedException {

		if (command instanceof DebugCommandStep) {
			step((DebugCommandStep) command);
			return;
		}

		if (command instanceof DebugCommandRunToLine) {
			runToLine((DebugCommandRunToLine) command);
		}

		if (command instanceof DebugCommandResume) {
			resume();
			return;
		}
	}

	public DebugState getState() {
		return m_refState.get();
	}

	public void pushState(DebugState state)
			throws InterruptedException {
		if (state == null) throw new IllegalArgumentException("object is null");
		final long pushingThreadId = Thread.currentThread().getId();
		if (pushingThreadId == m_controlledThreadId.get()) {
			m_box.put(state);
			if (!m_started.get()) {
				m_refState.set(state);
				m_started.set(true);
			}
		}
	}

	public void resume() {
		m_controlledThreadId.set(NOCONTROL);
		m_box.poll();
		m_refState.set(null);
	}

	public void start() {
		m_controlledThreadId.set(Thread.currentThread().getId());
		m_started.set(false);
		m_box.poll();
		m_refState.set(null);
	}

	public DebuggerSessionSource(String qccSourcePath) {
		if (qccSourcePath == null || qccSourcePath.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		this.qccSourcePath = qccSourcePath;
	}

	private static final long NOCONTROL = -1L;

	public final String qccSourcePath;
	private final AtomicBoolean m_started = new AtomicBoolean(false);
	private final BlockingQueue<DebugState> m_box = new ArrayBlockingQueue<DebugState>(1);
	private final AtomicReference<DebugState> m_refState = new AtomicReference<DebugState>();
	private final AtomicLong m_controlledThreadId = new AtomicLong(NOCONTROL);
}
